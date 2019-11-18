package com.olap3.cubeexplorer.dolap;

import com.alexscode.utilities.Stuff;
import com.alexscode.utilities.collection.Pair;
import com.google.common.base.Stopwatch;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.evaluate.QueryStats;
import com.olap3.cubeexplorer.infocolectors.ICView;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.measures.IMMetric;
import com.olap3.cubeexplorer.measures.Jaccard;
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
import com.olap3.cubeexplorer.optimize.OptimalSolver;
import com.olap3.cubeexplorer.optimize.tsp.LinKernighan;
import com.olap3.cubeexplorer.optimize.tsp.Measurable;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DOLAP {
    @Data @AllArgsConstructor @ToString
    static class TAPStats {
        Qfset q0;
        Stopwatch genTime, optTime, execTime;
        List<Qfset> finalPlan;
        int candidatesNb;
    }

    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());

    public static final String testData = "./data/import_ideb",
                                statsFile = "./data/stats/timings.csv",
                                resultFile = "./data/recal.csv";
    private static CubeUtils utils;
    private static MemoryManager mem = Nd4j.getMemoryManager();
    private static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization() //Necessary as QP map has "complex" key
            .setPrettyPrinting().create();
    private static PrintWriter stats, res;
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

        utils.forceMembersCaching();
        LOGGER.info("Members cached");

        stats = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(statsFile), true)));
        res = new PrintWriter(new FileOutputStream(new File(resultFile), false));
        res.printf("sessionFile,question,sessionLen,candidatesNb,icNb,execTimeMs,optTimeMs,budgetMs,thres,recall%n");

        LOGGER.info("Loading test data from " + testData);
        var sessions = DopanLoader.loadDir(testData);
        //Dodgy I know
        testQuery = new Qfset(olap.parseQuery((String) sessions.get(0).queries.get(0).getProperties().get("mdx")));
        testQuery.getMeasures().add(MeasureFragment.newInstance(utils.getMeasure("Distance trajet domicile - travail (moyenne)")));
        var learning = DopanLoader.loadDir("./data/dopan_converted");
        LOGGER.info("Test data loaded");

        LOGGER.info("Computing interestigness scores");
        Type qpMapType = new TypeToken<Map<QueryPart, Double>>() {}.getType(); // Type erasure is a pain
        LOGGER.warning("Using precomputed IM scores debug only");
        HashMap<QueryPart, Double> interest = gson.fromJson(new String(Files.readAllBytes(Paths.get("data/cache/im_testing.json"))), qpMapType);
        //Map<QueryPart, Double> interest = getInterestingness(learning);
        //Files.write(Paths.get("data/cache/im_testing.json"), gson.toJson(interest, qpMapType).getBytes());
        LOGGER.info("IM Compute done");

        /**
         *     Test Loop
         */
        LOGGER.info("Begin test phase");
        AprioriMetric im = new IMMetric(interest);

        Function<Query, Qfset> mapable = query -> Compatibility.QPsToQfset(query, utils);
        int budget = 10000;

        for (Session s : sessions){
            if (s.length() < 2 || s.getFilename().equals("5-22.json")|| s.getFilename().equals("5-23.json")){
                continue; //skip useless sessions
            }
            System.out.println("--- Session " + s.getFilename() + " ---");
            Query firstQuery = s.getQueries().get(0);
            Qfset firstTriplet = Compatibility.QPsToQfset(firstQuery, utils);
            TAPStats results = runOptimal(firstTriplet, budget, im, 0.005, false);

            List<Qfset> originals = s.getQueries().subList(1, s.length() ).stream().map(mapable).collect(Collectors.toList());
            List<Pair<Qfset, Double>> bestMatches = findMostSimilars(originals, results.finalPlan);

            IntStream.range(500,1001).mapToDouble(i-> i/1000.0).forEach(thres -> {
                res.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", s.getFilename(), s.getFilename().split("-")[0],s.length(), results.candidatesNb,
                        results.finalPlan.size(), results.execTime.elapsed().toMillis(),
                        results.optTime.elapsed().toMillis(), budget, thres, computeRecall(bestMatches, thres));
                res.flush();
            });

        }



        stats.flush(); stats.close();
        res.close();
    }

    private static double computeRecall(List<Pair<Qfset, Double>> bestMatches, double threshold) {
        //System.out.println(bestMatches.stream().map(Pair::getRight).collect(Collectors.toList()));
        return bestMatches.stream().mapToDouble(Pair::getRight).filter(value -> value > threshold).count()/((double)bestMatches.size());
    }

    private static List<Pair<Qfset, Double>> findMostSimilars(List<Qfset> toMatch, List<Qfset> searchSpace) {
        List<Pair<Qfset, Double>> found = new ArrayList<>(toMatch.size());

        for (Qfset q : toMatch){
            Qfset best = null; double sim = 0;
            for (Qfset candidate : searchSpace){
                double j = Jaccard.similarity(q, candidate);
                if (j > sim){
                    best = candidate;
                    sim = j;
                }
            }
            found.add(new Pair<>(best, sim));
        }

        return found;
    }

    public static TAPStats runTAPHeuristic(Qfset q0, int budgetms, AprioriMetric interestingness, double ks_epsilon, boolean reoptEnabled){
        Stopwatch runTime = Stopwatch.createStarted();
        BudgetManager ks = new KnapsackManager(interestingness, ks_epsilon);

        Stopwatch genTime = Stopwatch.createStarted();
        List<InfoCollector> candidates = generateCandidates(q0);
        genTime.stop();

        System.out.printf("Found %s candidates%n", candidates.size());
        //candidates.forEach(c -> System.out.printf("cost=%s,im=%s|", c.estimatedTime(), interestingness.rate(c)));

        Stopwatch optTime = Stopwatch.createStarted();
        ExecutionPlan plan = ks.findBestPlan(candidates, budgetms);
        optTime.stop();

        //Exec phase
        Set<Pair<Qfset, Result>> executed = new HashSet<>();
        Stopwatch execTime = Stopwatch.createUnstarted();

        //Main execution Loop (Algorithm 2 line 5)
        for (;plan.hasNext();) {
            InfoCollector ic = plan.next();
            execTime.start();
            executed.add(new Pair<>(ic.getDataSource().getInternal(), runMDX(ic)));
            plan.setExecuted(ic);
            execTime.stop();
            if (!reoptEnabled)
                continue;
            optTime.start();
            reoptRoutine(plan, candidates, runTime, budgetms, ks);
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
        return new TAPStats(q0, genTime,optTime, execTime, Arrays.stream(tsp.tour).mapToObj(toOrder::get).collect(Collectors.toList()), candidates.size());
    }

    public static TAPStats runOptimal(Qfset q0, int budgetms, AprioriMetric interestingness, double ks_epsilon, boolean reoptEnabled){
        Stopwatch runTime = Stopwatch.createStarted();

        Stopwatch genTime = Stopwatch.createStarted();
        Set<InfoCollector> candidates = new HashSet<>(generateCandidates(q0));
        genTime.stop();

        System.out.printf("Found %s candidates%n", candidates.size());
        //candidates.forEach(c -> System.out.printf("cost=%s,im=%s|", c.estimatedTime(), interestingness.rate(c)));

        Stopwatch optTime = Stopwatch.createStarted();
        ExecutionPlan plan = new ExecutionPlan(OptimalSolver.optimalSolver(candidates.stream().limit(30).collect(Collectors.toSet()), interestingness, budgetms).iterator().next());
        optTime.stop();

        //Exec phase
        List<Qfset> executed = new ArrayList<>();
        Stopwatch execTime = Stopwatch.createUnstarted();

        //Main execution Loop (Algorithm 2 line 5)
        for (;plan.hasNext();) {
            InfoCollector ic = plan.next();
            execTime.start();
            plan.setExecuted(ic);
            execTime.stop();
            executed.add(ic.getDataSource().getInternal());
        }

        return new TAPStats(q0, genTime,optTime, execTime, executed, candidates.size());
    }

    /**
     * Re-optimization routine
     * @param plan the current execution plan
     * @param candidates the original search space
     * @param runTime the total run time stopwatch
     * @param budget the original time budget
     * @param bm A Budget Manager to perform the re-optimization
     */
    public static boolean reoptRoutine(ExecutionPlan plan, List<InfoCollector> candidates, Stopwatch runTime, long budget, BudgetManager bm) {
        long left = budget - runTime.elapsed(TimeUnit.MILLISECONDS);

        // Case one we OUTATIME
        if (left < 1){
            //kill the plan !
            plan.removeAll(plan.getOperations());
            return false;
        }

        long predictedRunTime = plan.getLeft().stream().mapToLong(InfoCollector::estimatedTime).sum();

        // Case two less time left than anticipated
        if (predictedRunTime > left){
            if (plan.getOperations().size() == 0)
                return false;
            List<InfoCollector> restOfExec = bm.findBestPlan(new ArrayList<>(plan.getOperations()), (int)left).getOperations();
            plan.getOperations().retainAll(restOfExec);
            return false;
        }

        //Case three we have more time than anticipated
        List<InfoCollector> possibleStuff = candidates.stream().filter(ic -> ic.estimatedTime() < left).collect(Collectors.toList());
        possibleStuff.removeAll(plan.getOperations());
        possibleStuff.removeAll(plan.getExecuted());
        try {
            List<InfoCollector> newStuff = bm.findBestPlan(possibleStuff,(int)left).getOperations();
            plan.addAll(newStuff);
        } catch (IllegalArgumentException e){ // No more stuff to run anyway
            return true;
        }
        return true;
    }

    /**
     * Run MDX for the tests and add the time to the MDXAccessor
     * @param ic the IC to run
     * @return the mondrian result of the MDX, doesn't run any model
     */
    private static Result runMDX(InfoCollector ic) {
        Qfset toRun = ic.getDataSource().getInternal();
        mondrian.olap.Connection cnx = MondrianConfig.getMondrianConnection();
        Stopwatch runTime = Stopwatch.createStarted();
        mondrian.olap.Query query = toRun.toMDX();
        RolapResult result = (RolapResult) cnx.execute(query);
        runTime.stop();
        QueryStats qs = toRun.getStats();
        stats.printf("%s,%s,%s,%s,%s,%s,%s%n", Stuff.md5(toRun.getSql()), toRun.getSql().length(), qs.getProjNb(), qs.getSelNb(), qs.getTableNb(), qs.getAggNb(), runTime.elapsed().toMillis()/1000d);
        ((MDXAccessor)ic.getDataSource()).setMesuredTime(runTime.elapsed(TimeUnit.MILLISECONDS));
        ic.execute();//Just to flag it
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
/*
            if (f.getLevel().getParentLevel().isAll())
                continue;
            Qfset query2 = query.copy();
            query2.rollupLevel(f.getLevel(), 1);
            queries.add(new ICView(new MDXAccessor(query2), "R-Up (+2) ON " + p.getHierarchy()));*/
        }

        // Build drill-downs
        for (var sf : q0.getAttributes()){
            //FIXME We can't drill down on comunnes kills the server ...
            if (sf.getLevel().getUniqueName().contains("Commune de"))
                continue;
            Level target = sf.getLevel().getChildLevel();
            if (target==null)
                continue;
            var tmp = new HashSet<>(q0.getAttributes());
            tmp.add(ProjectionFragment.newInstance(target));
            tmp.remove(sf);

            Qfset req = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
            queries.add(new ICView(new MDXAccessor(req), "D-Down ON " + target.getHierarchy()));
/*

            if (target.getChildLevel() == null)
                continue;
            Qfset req2 = req.copy();
            req2.drillDownLevel(target, 1);
            queries.add(new ICView(new MDXAccessor(req2), "D-Down (-2) ON " + target.getHierarchy()));*/
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

}
