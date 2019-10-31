package com.olap3.cubeexplorer;

import com.alexscode.utilities.collection.Pair;
import com.google.common.base.Stopwatch;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.data.castor.session.CrSession;
import com.olap3.cubeexplorer.data.castor.session.QueryRequest;
import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.infocolectors.ICView;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.measures.compute.PageRank;
import com.olap3.cubeexplorer.measures.graph.DimensionsGraph;
import com.olap3.cubeexplorer.measures.graph.FiltersGraph;
import com.olap3.cubeexplorer.measures.graph.SessionGraph;
import com.olap3.cubeexplorer.model.*;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.optimize.AprioriMetric;
import com.olap3.cubeexplorer.optimize.BudgetManager;
import com.olap3.cubeexplorer.optimize.KnapsackManager;
import com.olap3.cubeexplorer.tsp.LinKernighan;
import com.olap3.cubeexplorer.tsp.Measurable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import mondrian.olap.Connection;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.Result;
import mondrian.rolap.RolapResult;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.memory.MemoryManager;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.alexscode.utilities.math.Distribution.log2;

public class DOLAP {
    @Data @AllArgsConstructor @ToString
    static class TAPStats {
        Qfset q0;
        Stopwatch genTime, optTime, execTime;
        ExecutionPlan finalPlan;
    }

    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());

    public static final String testData = "./data/import_ideb";
    private static CubeUtils utils;
    private static MemoryManager mem = Nd4j.getMemoryManager();
    private static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization() //Necessary as QP map has "complex" key
            .setPrettyPrinting().create();
    // For testing only
    private static Qfset testQuery;


    public static void main(String[] args) throws Exception{
        LOGGER.info("Initializing Mondrian");// Init code
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null) {
            LOGGER.severe("Couldn't initialize db/mondrian connection, check stack trace for details. Exiting now.");
            System.exit(1); //Crash the app can't do anything w/o mondrian
        }
        utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
        LOGGER.info("Mondrian connection init complete");

        LOGGER.info("Loading test data from " + testData);
        var sessions = DopanLoader.loadDir(testData);
        //Dodgy I know
        testQuery = new Qfset(olap.parseQuery((String) sessions.get(0).queries.get(0).getProperties().get("mdx")));
        testQuery.getMeasures().add(MeasureFragment.newInstance(utils.getMeasure("Distance trajet domicile - travail (moyenne)")));
        LOGGER.info("Test data loaded");

        LOGGER.info("Computing interestigness scores");
        Type qpMapType = new TypeToken<Map<QueryPart, Double>>() {}.getType(); // Type erasure is a pain
        LOGGER.warning("Using precomputed IM scores debug only");
        HashMap<QueryPart, Double> interest = gson.fromJson(new String(Files.readAllBytes(Paths.get("data/cache/im_testing.json"))), qpMapType);
        //Map<QueryPart, Double> interest = getInterestingness(sessions);
        //Files.write(Paths.get("data/cache/im_testing.json"), gson.toJson(interest, qpMapType).getBytes());
        LOGGER.info("IM Compute done");

        /*
                    Fin des pre-calculs mettre le code de test ci apres
         */
        LOGGER.info("Begin test phase");
        AprioriMetric im = new AprioriMetric() {
            @Override
            public double rate(InfoCollector ic) {
                var qps = Compatibility.partsFromQfset(ic.getDataSource().getInternal());
                double sum = 0;
                for (var qp : qps){
                    Double i = interest.get(qp);
                    if(i==null) {
                        System.err.printf("Warning no IM found for %s %n", qp.toString());
                        return 0;
                    }
                    else
                        sum -= log2(i);
                }
                return sum/qps.size();
            }
        };

        System.out.println(runTAPHeuristic(testQuery, 10000, im));

    }

    public static TAPStats runTAPHeuristic(Qfset q0, int budgetms, AprioriMetric interestingness){
        BudgetManager ks = new KnapsackManager(interestingness);

        Stopwatch genTime = Stopwatch.createStarted();
        List<InfoCollector> candidates = generateCandidates(q0);
        genTime.stop();

        System.out.printf("--- Found %s candidates ---%n", candidates.size());
        //candidates.forEach(c -> System.out.printf("  %s%n    cost=%s, im=%s%n", c, c.estimatedTime(), interestingness.rate(c)));

        Stopwatch optTime = Stopwatch.createStarted();
        ExecutionPlan plan = ks.findBestPlan(candidates, budgetms);
        optTime.stop();

        System.out.println("--- Plan summary ---");
        System.out.printf("Chose %s ICs with total cost of %s ms%nICs Chosen:%n", plan.getOperations().size(), plan.getOperations().stream().mapToLong(InfoCollector::estimatedTime).sum());
        for (InfoCollector ic : plan.getOperations()){
            System.out.println("  " + ic);
            System.out.println("    Estimated cost " + ic.estimatedTime());
        }
        //Exec phase
        Set<Pair<Qfset, Result>> executed = new HashSet<>();
        Stopwatch execTime = Stopwatch.createUnstarted();

        for (InfoCollector ic : plan.getOperations()){
            execTime.start();
            executed.add(new Pair<>(ic.getDataSource().getInternal(), runMDX(ic)));
            execTime.stop();
        }


        //Ordering phase
        List<Qfset> toOrder = executed.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<Integer> ids = IntStream.range(0, toOrder.size()).boxed().collect(Collectors.toList());

        optTime.start();
        LinKernighan tsp = new LinKernighan(toOrder.stream().map(q -> (Measurable) q).collect(Collectors.toList()), ids);
        tsp.runAlgorithm();
        optTime.stop();

        System.out.println(tsp);

        return new TAPStats(q0, genTime,optTime, execTime, plan);
    }

    private static Result runMDX(InfoCollector ic) {
        mondrian.olap.Connection cnx = MondrianConfig.getMondrianConnection();
        mondrian.olap.Query query = ic.getDataSource().getInternal().toMDX();
        RolapResult result = (RolapResult) cnx.execute(query);
        return result;
    }

    private static Map<QueryPart, Double> getInterestingness(List<Session> sessions) {
        System.out.println("Building topology graph...");
        MutableValueGraph<QueryPart, Double> topoGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        DimensionsGraph.injectSchema(topoGraph, utils);
        FiltersGraph.injectCompressedFilters(topoGraph, utils);
        System.out.println("Building Logs graph...");
        MutableValueGraph<QueryPart, Double> logGraph = SessionGraph.buildFromLog(sessions);

        MutableValueGraph<QueryPart, Double> base = SessionEvaluator
                .<QueryPart>linearInterpolation(0.5, true)
                .interpolate(topoGraph, ValueGraphBuilder.directed().allowsSelfLoops(true).build(), logGraph);

        System.out.println("Freeing memory"); // I know I know, don't judge me laptop has only 16GB of RAM
        topoGraph = null;
        logGraph = null;
        mem.invokeGc();

        System.out.println("Computing PageRank...");
        Pair<INDArray, HashMap<QueryPart, Integer>> ref = PageRank.pagerank(base, 50);
        INDArray rawScores = ref.getLeft();
        HashMap<QueryPart, Double> interest = new HashMap<>(ref.getRight().size());
        ref.right.forEach((key, value) -> interest.put(key, rawScores.getDouble(value)));

        System.out.println("Freeing memory");
        base = null;
        ref = null;
        mem.invokeGc();
        return interest;
    }

    public static List<InfoCollector> generateCandidates(Qfset q0){
        List<InfoCollector> queries = new ArrayList<>();

        // Build roll-ups
        for (ProjectionFragment f : q0.getAttributes()){
            if (f.getLevel().isAll())
                continue;
            ProjectionFragment p  = ProjectionFragment.newInstance(f.getLevel().getParentLevel());

            HashSet<ProjectionFragment> tmp = new HashSet<>(q0.getAttributes());
            tmp.remove(f); tmp.add(p);

            Qfset query = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
            queries.add(new ICView(new MDXAccessor(query), "R-Up ON " + p.getHierarchy()));
        }

        // Build drill-downs
        for (var sf : q0.getAttributes()){
            Level target = sf.getLevel().getChildLevel();
            if (target==null)
                continue;
            var tmp = new HashSet<>(q0.getAttributes());
            tmp.add(ProjectionFragment.newInstance(target));
            tmp.remove(sf);

            Qfset req = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
            queries.add(new ICView(new MDXAccessor(req), "D-Down ON " + target.getHierarchy()));
        }

        // Build siblings
        for (var sel : q0.getSelectionPredicates()){
            Level target = sel.getLevel();
            Member original = sel.getValue();

            for (var other : utils.fetchMembers(target)){
                if (other.equals(original))
                    continue;
                var tmp = new HashSet<>(q0.getSelectionPredicates());
                tmp.remove(original);
                tmp.add(SelectionFragment.newInstance(other));
                Qfset req = new Qfset(new HashSet<>(q0.getAttributes()), tmp, new HashSet<>(q0.getMeasures()));
                queries.add(new ICView(new MDXAccessor(req)));
            }
        }

        return queries;
    }


    public static List<Session> convertFromCr(List<CrSession> sessions){
        ArrayList<Session> outSess = new ArrayList<>(sessions.size());

        for (CrSession in : sessions){
            ArrayList<Query> queries = new ArrayList<>(in.getQueries().size());
            for (QueryRequest qr : in.getQueries()){
                queries.add(new Query(Compatibility.partsFromQfset(Compatibility.QfsetFromMDX(qr.getQuery()))));
            }
            Session current = new Session(queries, in.getUser().getName(), in.getTitle()); // TODO check properties we want
            outSess.add(current);
        }

        return outSess;
    }
}
