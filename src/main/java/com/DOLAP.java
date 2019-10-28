package com;

import com.alexscode.utilities.Nd4jUtils;
import com.alexscode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olap3.cubeexplorer.Compatibility;
import com.olap3.cubeexplorer.StudentParser;
import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.data.castor.session.CrSession;
import com.olap3.cubeexplorer.data.castor.session.QueryRequest;

import com.olap3.cubeexplorer.im_olap.compute.PageRank;
import com.olap3.cubeexplorer.im_olap.model.*;
import com.olap3.cubeexplorer.info.ICView;
import com.olap3.cubeexplorer.info.InfoCollector;
import com.olap3.cubeexplorer.info.MDXAccessor;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.julien.SelectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.optimize.AprioriMetric;
import com.olap3.cubeexplorer.optimize.BudgetManager;
import com.olap3.cubeexplorer.optimize.KnapsackManager;
import mondrian.olap.Connection;
import mondrian.olap.Level;
import mondrian.olap.Member;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.memory.MemoryManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;


public class DOLAP {
    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());
    static final String testData = "./data/import_ideb",
            schemaPath  = "./data/cubeSchemas/DOPAN_DW3.xml";
    static CubeUtils utils;
    static double alpha = 0.5, epsilon = 0.005;
    static DecimalFormat df = new DecimalFormat("#.##");
    static MemoryManager mem = Nd4j.getMemoryManager();
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // For testing stuff
    static Qfset testQuery;


    public static void main(String[] args) throws Exception{
        LOGGER.info("Init Starting");
        Connection olap = MondrianConfig.getMondrianConnection();
        utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
        LOGGER.info("DB Connection init complete");

        LOGGER.info("Loading test data from " + testData);
        var sessions = DopanLoader.loadDir(testData);
        //Dodgy I know
        testQuery = new Qfset(olap.parseQuery((String) sessions.get(0).queries.get(0).getProperties().get("mdx")));
        testQuery.getMeasures().add(new MeasureFragment(utils.getMeasure("Distance trajet domicile - travail (moyenne)")));

        LOGGER.info("Computing interestigness scores");
        //System.err.println("!!! WARNING !!! Using precomputed IM scores debug only !!! WARNING !!!");
        //HashMap<QueryPart, Double> interest = gson.fromJson(new String(Files.readAllBytes(Paths.get("data/cache/im_testing.json"))), HashMap.class);
        Map<QueryPart, Double> interest = getInterestingness(sessions);
        Files.write(Paths.get("data/cache/im_testing.json"), gson.toJson(interest).getBytes());

        /*
                    Fin des pre-calculs mettre le code de test ci apres
         */
        LOGGER.info("Begin test phase");
        // Init KS
        BudgetManager ks = new KnapsackManager(ic -> {
            var qps = Compatibility.partsFromQfset(ic.getDataSource().getInternal());
            double sum = qps.stream().mapToDouble(key -> {
                var i = interest.get(key);
                if(i==null) {
                    System.err.println("Error for "+ key.toString());
                    return 0.001;
                }else
                    return i;
            }).sum(); // .peek(qp -> System.out.println(qp + "|" + interest.get(qp)))
            return sum/qps.size();
        });

        List<InfoCollector> candidates = generateCandidates(testQuery);
        System.out.printf("Found %s candidates%n", candidates.size());
        System.out.println(ks.findBestPlan(candidates, 6000));

    }

    private static Map<QueryPart, Double> getInterestingness(List<Session> sessions) {
        System.out.println("Building topology graph...");
        MutableValueGraph<QueryPart, Double> topoGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        DimensionsGraph.injectSchema(topoGraph, schemaPath);
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
            queries.add(new ICView(new MDXAccessor(query)));
        }

        // Build drill-downs
        for (var sf : q0.getAttributes()){
            Level target = sf.getLevel().getChildLevel();
            if (target==null)
                continue;
            var tmp = new HashSet<>(q0.getAttributes());
            tmp.add(new ProjectionFragment(target));

            Qfset req = new Qfset(tmp, new HashSet<>(), new HashSet<>(q0.getMeasures()));
            queries.add(new ICView(new MDXAccessor(req)));
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
                tmp.add(new SelectionFragment(other));
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
