package com.olap3.cubeexplorer;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DOLAPFig3 {
    @Data
    @AllArgsConstructor
    @ToString
    static class TAPStats {
        Qfset q0;
        Stopwatch genTime, optTime, execTime;
        List<Qfset> finalPlan;
        int candidatesNb;
        double im, dist;
    }

    private static final Logger LOGGER = Logger.getLogger(DOLAPFig3.class.getName());

    public static final String testData = "./data/import_ideb",
            resultFile = "./data/stats/res_dopan.csv";
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

        //stats = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(statsFile), true)));
        res = new PrintWriter(new FileOutputStream(new File(resultFile), false));
        res.printf("id,budget,algorithm,im,avgDist,optTime,execTime%n");

        LOGGER.info("Loading test data from " + testData);
        var sessions = DopanLoader.loadDir(testData);
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

        for (Session s : sessions){
            if (s.length() < 2 || s.getFilename().equals("5-22.json")|| s.getFilename().equals("5-23.json")){
                continue; //skip useless sessions
            }
            System.out.println("--- Session " + s.getFilename() + " ---");
            Query firstQuery = s.getQueries().get(0);
            Qfset firstTriplet = Compatibility.QPsToQfset(firstQuery, utils);

            for (int i = 1; i <= 10; i++) {
                int budget = i*1000;
                TAPStats naive = runTAPHeuristic(firstTriplet, budget, im, 0.005, false);
                TAPStats reopt = runTAPHeuristic(firstTriplet, budget, im, 0.005, true);
                res.printf("%s,%s,%s,%s,%s,%s,%s%n",s.getFilename(), budget, "NAIVE", naive.im, naive.dist,
                        naive.optTime.elapsed(TimeUnit.MILLISECONDS), naive.execTime.elapsed(TimeUnit.MILLISECONDS));
                res.printf("%s,%s,%s,%s,%s,%s,%s%n",s.getFilename(), budget, "TAP", reopt.im, reopt.dist,
                        reopt.optTime.elapsed(TimeUnit.MILLISECONDS), reopt.execTime.elapsed(TimeUnit.MILLISECONDS));
            }

        }



        //stats.flush(); stats.close();
        res.close();
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
        double dist = 0d;
        List<InfoCollector> ics = new ArrayList<>(plan.getExecuted());
        for (int i = 0; i < ics.size() - 1; i++) {
            dist += 1 - Jaccard.similarity(ics.get(i).getDataSource().getInternal(), ics.get(i+1).getDataSource().getInternal());
        }

        return new TAPStats(q0, genTime,optTime, execTime,
                Arrays.stream(tsp.tour).mapToObj(toOrder::get).collect(Collectors.toList()), candidates.size(),
                plan.getExecuted().stream().mapToDouble(interestingness::rate).sum(), dist);
    }

    /**
     * Re-optimization routine
     * @param plan the current execution plan
     * @param candidates the original search space
     * @param runTime the total run time stopwatch
     * @param budget the original time budget
     * @param bm A Budget Manager to perform the re-optimization
     */
    static boolean reoptRoutine(ExecutionPlan plan, List<InfoCollector> candidates, Stopwatch runTime, long budget, BudgetManager bm) {
        Set<InfoCollector> executed = plan.getExecuted();
        long left = budget - runTime.elapsed(TimeUnit.MILLISECONDS);

        // Case one we OUTATIME
        if (left < 1){
            //kill the plan !
            plan.removeAll(plan.getOperations());
            return false;
        }

        long predictedRunTime = executed.stream().mapToLong(InfoCollector::estimatedTime).sum();

        // Case two less time left than anticipated
        if (predictedRunTime > runTime.elapsed(TimeUnit.MILLISECONDS)){
            if (plan.getOperations().size() == 0)
                return false;
            Set<InfoCollector> restOfExec = bm.findBestPlan(new ArrayList<>(plan.getOperations()), (int)left).getOperations();
            plan.getOperations().retainAll(restOfExec);
            return false;
        }

        //Case three we have more time than anticipated
        List<InfoCollector> possibleStuff = new ArrayList<>(candidates);
        possibleStuff.removeAll(plan.getOperations());
        possibleStuff.removeAll(plan.getExecuted());
        try {
            Set<InfoCollector> newStuff = bm.findBestPlan(possibleStuff,(int)left).getOperations();
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
        Connection cnx = MondrianConfig.getMondrianConnection();
        Stopwatch runTime = Stopwatch.createStarted();
        mondrian.olap.Query query = toRun.toMDX();
        RolapResult result = (RolapResult) cnx.execute(query);
        runTime.stop();
        QueryStats qs = toRun.getStats();
        //stats.printf("%s,%s,%s,%s,%s,%s,%s%n", Stuff.md5(toRun.getSql()), toRun.getSql().length(), qs.getProjNb(), qs.getSelNb(), qs.getTableNb(), qs.getAggNb(), runTime.elapsed().toMillis()/1000d);
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
            Level target = f.getLevel().getParentLevel();
            while(target != null){
                ProjectionFragment p  = ProjectionFragment.newInstance(target);
                HashSet<ProjectionFragment> tmp = new HashSet<>(q0.getAttributes());
                tmp.remove(f); tmp.add(p);

                Qfset query = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
                queries.add(new ICView(new MDXAccessor(query), "R-Up ON " + p.getHierarchy()));

                target = target.getParentLevel();
            }
        }

        // Build drill-downs
        for (var sf : q0.getAttributes()){
            //FIXME We can't drill down on comunnes kills the server ...
            if (sf.getLevel().getUniqueName().contains("Commune de"))
                continue;

            Level target = sf.getLevel().getChildLevel();
            while (target!=null) {
                var tmp = new HashSet<>(q0.getAttributes());
                tmp.add(ProjectionFragment.newInstance(target));
                tmp.remove(sf);

                Qfset req = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
                queries.add(new ICView(new MDXAccessor(req), "D-Down ON " + target.getHierarchy()));
                target = target.getChildLevel();
            }
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
