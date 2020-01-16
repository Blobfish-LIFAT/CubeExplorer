package com.olap3.cubeexplorer;

import com.alexscode.utilities.collection.Pair;
import com.google.common.base.Stopwatch;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.olap3.cubeexplorer.dolap.CubeLoad;
import com.olap3.cubeexplorer.dolap.DOLAP;
import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.infocolectors.ICView;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.measures.IMMetric;
import com.olap3.cubeexplorer.measures.Jaccard;
import com.olap3.cubeexplorer.measures.compute.PageRank;
import com.olap3.cubeexplorer.measures.graph.DimensionsGraph;
import com.olap3.cubeexplorer.measures.graph.FiltersGraph;
import com.olap3.cubeexplorer.measures.graph.SessionGraph;
import com.olap3.cubeexplorer.model.Compatibility;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.legacy.Query;
import com.olap3.cubeexplorer.model.legacy.QueryPart;
import com.olap3.cubeexplorer.model.legacy.Session;
import com.olap3.cubeexplorer.model.legacy.SessionEvaluator;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.optimize.AprioriMetric;
import com.olap3.cubeexplorer.optimize.BudgetManager;
import com.olap3.cubeexplorer.optimize.KnapsackManager;
import com.olap3.cubeexplorer.optimize.tsp.LinKernighan;
import com.olap3.cubeexplorer.optimize.tsp.Measurable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import mondrian.olap.Connection;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for test code and such
 * Class model for this is on google drive (link in the emails)
 */
public class Dev {
    @Data
    @AllArgsConstructor
    @ToString
    static class TAPStats {
        Qfset q0;
        Stopwatch genTime, optTime, execTime;
        List<Qfset> finalPlan;
        int candidatesNb;
        double im, dist;
        int reoptGood, reoptBad;
    }

    static Logger LOGGER = Logger.getLogger(CubeLoad.class.getName());
    private static String dataDir = "data/ssb_converted/";
    static CubeUtils utils;
    private static PrintWriter out = new PrintWriter(System.out);
    static Connection olap;
    static java.sql.Connection con;
    private static String[] cubeloadProfiles = new String[]{"explorative", "goal_oriented", "slice_all", "slice_and_drill"};

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        MondrianConfig.defaultConfigFile = "./data/ssb.properties";
        olap = MondrianConfig.getMondrianConnection();
        if (olap == null)
            System.exit(1); //Crash the app can't do anything w/o mondrian
        utils = new CubeUtils(olap, "SSB");
        CubeUtils.setDefault(utils);
        MondrianConfig.setMondrianConnection(olap);
        con = MondrianConfig.getNewJdbcConnection();
        LOGGER.info("Server Connection established");

        List<Session> learning = new ArrayList<>();
        Map<String, List<Session>> profiles = new HashMap<>();
        for (String cubeloadProfile : cubeloadProfiles) {
            List<Session> profile = CubeLoad.loadCubeloadXML(dataDir + cubeloadProfile + ".xml", olap, "SSB").subList(0,10);
            profiles.put(cubeloadProfile, profile);
            learning.addAll(profile);
        }
        LOGGER.info("Loaded Cubeload Sessions");

        Map<QueryPart, Double> interest = getInterestingness(learning);
        LOGGER.info("Interestingness Computation done");

        LOGGER.info("Begin test phase");
        AprioriMetric im = new IMMetric(interest);

        out.printf("session,candidateSize,budget,algo,planSize,executedSize,im,avgDist,optiTime,execTime,reoptGood,reoptBad%n");
        for (Session s : learning.subList(0,5)){
            System.out.println("--- Session " + s.getFilename() + " ---");
            Query firstQuery = s.getQueries().get(0);
            Qfset firstTriplet = Compatibility.QPsToQfset(firstQuery, utils);

            int budget = 2500;

            System.out.println("Running TAP budget " + budget/1000 + "s");
            TAPStats tapReopt = runTAPHeuristic(firstTriplet, budget, im, 0.005, true);

            int tapSize = tapReopt.finalPlan.size();
                out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", s.getFilename(), tapReopt.candidatesNb, budget,
                        "TAP", tapSize, tapReopt.candidatesNb, tapReopt.im, tapReopt.dist/((double)tapSize), tapReopt.optTime.elapsed(TimeUnit.MILLISECONDS),
                        tapReopt.execTime.elapsed(TimeUnit.MILLISECONDS), tapReopt.reoptGood, tapReopt.reoptBad);

            }

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
            mondrian.olap.Level target = sf.getLevel().getChildLevel();
            if (target==null)
                continue;
            var tmp = new HashSet<>(q0.getAttributes());
            tmp.add(ProjectionFragment.newInstance(target));
            tmp.remove(sf);

            Qfset req = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
            queries.add(new ICView(new MDXAccessor(req), "D-Down ON " + target.getHierarchy()));

        }

        // Build siblings
        //Can't do that with the exact solver it has O(|candidates|!) complexity
        /*
        for (var sel : q0.getSelectionPredicates()){
            mondrian.olap.Level target = sel.getLevel();
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
        */
        return queries;
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
        //mem.invokeGc();

        System.out.println("Computing PageRank...");
        Pair<INDArray, HashMap<QueryPart, Integer>> ref = PageRank.pagerank(base, 50);
        INDArray rawScores = ref.getLeft();
        HashMap<QueryPart, Double> interest = new HashMap<>(ref.getRight().size());
        ref.right.forEach((key, value) -> interest.put(key, rawScores.getDouble(value)));

        System.out.println("Freeing memory");
        base = null;
        ref = null;
        //mem.invokeGc();
        return interest;
    }
    

    public static TAPStats runTAPHeuristic(Qfset q0, int budgetms, AprioriMetric interestingness, double ks_epsilon, boolean reoptEnabled){
        Stopwatch runTime = Stopwatch.createStarted();
        BudgetManager ks = new KnapsackManager(interestingness, ks_epsilon);

        Stopwatch genTime = Stopwatch.createStarted();
        List<InfoCollector> candidates = generateCandidates(q0);
        genTime.stop();

        //System.out.printf("Found %s candidates%n", candidates.size());
        //candidates.forEach(c -> System.out.printf("cost=%s,im=%s|", c.estimatedTime(), interestingness.rate(c)));

        Stopwatch optTime = Stopwatch.createStarted();
        ExecutionPlan plan = new ExecutionPlan(ks.findBestPlan(candidates, budgetms).getOperations());
        optTime.stop();

        //Exec phase
        Set<Pair<Qfset, mondrian.olap.Result>> executed = new HashSet<>();
        Stopwatch execTime = Stopwatch.createUnstarted();
        int reoptGood = 0, reoptBad = 0;

        //Main execution Loop (Algorithm 2 line 5)
        for (;plan.hasNext();) {
            InfoCollector ic = plan.next();
            execTime.start();
            runQuery(ic);
            executed.add(new Pair<>(ic.getDataSource().getInternal(), null));
            plan.setExecuted(ic);
            execTime.stop();
            if (!reoptEnabled)
                continue;
            optTime.start();
            if(DOLAP.reoptRoutine(plan, candidates, runTime, budgetms, ks))
                reoptGood++;
            else
                reoptBad++;
            optTime.stop();
        }


        //Ordering phase
        List<Qfset> toOrder = executed.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<Integer> ids = IntStream.range(0, toOrder.size()).boxed().collect(Collectors.toList());

        optTime.start();
        LinKernighan tsp = new LinKernighan(toOrder.stream().map(q -> (Measurable) q).collect(Collectors.toList()), ids);
        tsp.runAlgorithm();
        optTime.stop();

        runTime.stop();

        double dist = 0d;
        List<InfoCollector> ics = new ArrayList<>(plan.getExecuted());
        for (int i = 0; i < ics.size() - 1; i++) {
            dist += 1 - Jaccard.similarity(ics.get(i).getDataSource().getInternal(), ics.get(i+1).getDataSource().getInternal());
        }

        return new TAPStats(q0, genTime,optTime, execTime,
                Arrays.stream(tsp.tour).mapToObj(toOrder::get).collect(Collectors.toList()), candidates.size(),
                plan.getExecuted().stream().mapToDouble(interestingness::rate).sum(), dist, reoptGood, reoptBad);
    }


    /**
     * Run MDX for the tests and add the time to the MDXAccessor
     * @param ic the IC to run
     * @return the mondrian result of the MDX, doesn't run any model
     */
    private static void runQuery(InfoCollector ic) {
        try {
            Stopwatch exec = Stopwatch.createStarted();
            MDXAccessor data = (MDXAccessor) ic.getDataSource();
            SQLFactory factory = new SQLFactory(utils);
            String query = factory.getStarJoin(data.getInternal());
            Statement planON = con.createStatement();

            ResultSet rs = planON.executeQuery(query);

            double sum = 0.0;
            while (rs.next()){
                sum = sum + 0.9; //Dummy loop don't need the results but go through the rs to be realist
            }

            planON.close();
            data.setMesuredTime(exec.stop().elapsed(TimeUnit.MILLISECONDS));
        }catch (SQLException e){

        }
    }

}
