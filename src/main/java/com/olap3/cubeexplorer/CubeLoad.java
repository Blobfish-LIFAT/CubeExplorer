package com.olap3.cubeexplorer;

import com.alexscode.utilities.collection.Pair;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.olap3.cubeexplorer.data.cubeloadBeans.*;
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
import com.olap3.cubeexplorer.model.Query;
import com.olap3.cubeexplorer.model.Session;
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
import mondrian.olap.Dimension;
import mondrian.olap.SchemaReader;
import org.nd4j.linalg.api.ndarray.INDArray;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
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

public class CubeLoad {
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
    private static String dataDir = "data/ssb_converted/",
        outputFile = "data/stats/res_ssb_demo.csv";
    static CubeUtils utils;
    private static PrintWriter out;
    static Connection olap;
    static java.sql.Connection con;
    private static String[] cubeloadProfiles = new String[]{"explorative", "goal_oriented", "slice_all", "slice_and_drill"};
    //private static String[] cubeloadProfiles = new String[]{"goal_oriented"};

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        MondrianConfig.defaultConfigFile = args[0];
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
            List<Session> profile = loadCubeloadXML(dataDir + cubeloadProfile + ".xml", olap, "SSB").subList(0,10);
            profiles.put(cubeloadProfile, profile);
            learning.addAll(profile);
        }
        LOGGER.info("Loaded Cubeload Sessions");

        LOGGER.info("Computing interestigness scores");
        //Type qpMapType = new TypeToken<Map<QueryPart, Double>>() {}.getType(); // Type erasure is a pain
        //LOGGER.warning("Using precomputed IM scores debug only");
        //HashMap<QueryPart, Double> interest = gson.fromJson(new String(Files.readAllBytes(Paths.get("data/cache/im_testing.json"))), qpMapType);
        Map<QueryPart, Double> interest = getInterestingness(learning);
        //Files.write(Paths.get("data/cache/im_testing.json"), gson.toJson(interest, qpMapType).getBytes());
        LOGGER.info("Interestingness Computation done");

        LOGGER.info("Begin test phase");
        out = new PrintWriter(new FileOutputStream(new File(outputFile)));
        AprioriMetric im = new IMMetric(interest);

        System.out.printf("session,candidateSize,budget,algo,planSize,executedSize,im,avgDist,optiTime,execTime,reoptGood,reoptBad,imOfOPT%n");
        for (Session s : learning){
            if (s.length() < 2){
                continue; //skip useless sessions
            }
            System.out.println("--- Session " + s.getFilename() + " ---");
            Query firstQuery = s.getQueries().get(0);
            Qfset firstTriplet = Compatibility.QPsToQfset(firstQuery, utils);

            for (int i = 1; i <= 10; i++){
                int budget = 500 * i;

                System.out.println("Running OPT budget " + budget/1000 + "s");
                Pair<TAPStats, Set<List<InfoCollector>>> raw = runOptimalWithOracle(firstTriplet, budget, im);
                TAPStats optimal = raw.left;
                System.out.println("Running NAIVE budget " + budget/1000 + "s");
                TAPStats naive = runTAPHeuristic(firstTriplet, budget, im, 0.005, false);
                System.out.println("Running TAP budget " + budget/1000 + "s");
                TAPStats tapReopt = runTAPHeuristic(firstTriplet, budget, im, 0.005, true);

                int optSize = optimal.finalPlan.size();
                int naiveSize = naive.finalPlan.size();
                int tapSize = tapReopt.finalPlan.size();

                System.out.printf("--- Optimal IM:%s D:%s TIME:%s TimeEst:%s---%n",
                        optimal.im, optimal.dist/((double)optSize), optimal.execTime.elapsed(TimeUnit.MICROSECONDS)/10e3d,
                        optimal.reoptGood);
                System.out.println(optimal.finalPlan);
                System.out.printf("--- Naive IM:%s D:%s TIME:%s---%n", naive.im, naive.dist/((double)naiveSize), (naive.execTime.elapsed(TimeUnit.MICROSECONDS)/10e3d)+(naive.optTime.elapsed(TimeUnit.MICROSECONDS)/10e3d));
                System.out.println(naive.finalPlan);
                System.out.printf("--- ReOpt IM:%s D:%s TIME:%s---%n", tapReopt.im, tapReopt.dist/((double)tapSize), (tapReopt.execTime.elapsed(TimeUnit.MICROSECONDS)/10e3d) + (tapReopt.optTime.elapsed(TimeUnit.MICROSECONDS)/10e3d));
                System.out.println(tapReopt.finalPlan);
                System.exit(0);

                if(optSize>0){
                System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,1%n", s.getFilename(), optimal.candidatesNb, budget,
                        "OPT", optSize, optSize, optimal.im, optimal.dist/((double)optSize), optimal.optTime.elapsed(TimeUnit.MILLISECONDS),
                        optimal.execTime.elapsed(TimeUnit.MILLISECONDS), 0, 0);
                System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", s.getFilename(), naive.candidatesNb, budget,
                        "NAIVE", naive.candidatesNb, naiveSize, naive.im, naive.dist/((double)naiveSize), naive.optTime.elapsed(TimeUnit.MICROSECONDS)/10e3d,
                        naive.execTime.elapsed(TimeUnit.MILLISECONDS), 0, 0,
                        raw.right.stream().mapToDouble(set -> naive.im/set.stream().mapToDouble(im::rate).sum()).average());
                System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", s.getFilename(), tapReopt.candidatesNb, budget,
                        "TAP", tapSize, tapReopt.candidatesNb, tapReopt.im, tapReopt.dist/((double)tapSize), tapReopt.optTime.elapsed(TimeUnit.MILLISECONDS),
                        tapReopt.execTime.elapsed(TimeUnit.MILLISECONDS), tapReopt.reoptGood, tapReopt.reoptBad,
                        raw.right.stream().mapToDouble(set -> tapReopt.im/set.stream().mapToDouble(im::rate).sum()).average());
                }
                out.flush();
            }


        }

        out.close();
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

    public static Pair<TAPStats,Set<List<InfoCollector>>> runOptimal(Qfset q0, int budgetms, AprioriMetric interestingness){
        Stopwatch genTime = Stopwatch.createStarted();
        Set<InfoCollector> candidates = new HashSet<>(generateCandidates(q0));
        genTime.stop();

        Stopwatch optTime = Stopwatch.createStarted();
        ExecutionPlan plan;
        Set<List<InfoCollector>> otherSolutions = null;
        try {
            otherSolutions = OptimalSolver.optimalSolver(candidates, interestingness, budgetms);
            plan = new ExecutionPlan(otherSolutions.iterator().next());
        } catch (NoSuchElementException e){
            System.err.println("No optimal found try higher budget than " + budgetms + " ms");
            return new Pair<>(new TAPStats(q0, genTime, optTime.stop(), Stopwatch.createUnstarted(), new ArrayList<>(), candidates.size(), 0, 0, 0, 0), otherSolutions);
        }
        optTime.stop();

        //Exec phase
        List<Qfset> executed = new ArrayList<>();
        Stopwatch execTime = Stopwatch.createUnstarted();

        //Main execution Loop (Algorithm 2 line 5)
        for (;plan.hasNext();) {
            InfoCollector ic = plan.next();
            execTime.start();
            runQuery(ic);
            plan.setExecuted(ic);
            execTime.stop();
            executed.add(ic.getDataSource().getInternal());
        }
        double dist = 0d;
        List<InfoCollector> ics = new ArrayList<>(plan.getExecuted());
        for (int i = 0; i < ics.size() - 1; i++) {
            dist += 1 - Jaccard.similarity(ics.get(i).getDataSource().getInternal(), ics.get(i+1).getDataSource().getInternal());
        }

        return new Pair<>(new TAPStats(q0, genTime,optTime, execTime, executed, candidates.size(),
                plan.getExecuted().stream().mapToDouble(interestingness::rate).sum(),
                dist, 0, 0), otherSolutions);
    }

    public static Pair<TAPStats,Set<List<InfoCollector>>> runOptimalWithOracle(Qfset q0, int budgetms, AprioriMetric interestingness){
        Stopwatch genTime = Stopwatch.createStarted();
        Set<InfoCollector> candidates = new HashSet<>(generateCandidates(q0));
        genTime.stop();

        // Oh magic mirror tell me ho long my queries will take to run !
        for (InfoCollector ic : candidates){
            Stopwatch magic = Stopwatch.createStarted();
            runQuery(ic);
            long time = magic.stop().elapsed(TimeUnit.MILLISECONDS);
            ic.setTime_estimate(time);
        }

        Stopwatch optTime = Stopwatch.createStarted();
        ExecutionPlan plan;
        Set<List<InfoCollector>> otherSolutions = null;
        int tmpTime = 0;
        try {
            otherSolutions = OptimalSolver.optimalSolver(candidates, interestingness, budgetms);
            List<List<InfoCollector>> solutions = new ArrayList<>(otherSolutions);
            solutions.sort(Comparator.comparing(s -> s.stream().mapToDouble(ic -> interestingness.rate(ic)).sum()));
            solutions = Lists.reverse(solutions);
            tmpTime = (int) solutions.get(0).stream().mapToDouble(ic -> interestingness.rate(ic)).sum();
            plan = new ExecutionPlan(solutions.get(0));
        } catch (NoSuchElementException e){
            System.err.println("No optimal found try higher budget than " + budgetms + " ms");
            return new Pair<>(new TAPStats(q0, genTime, optTime.stop(), Stopwatch.createUnstarted(), new ArrayList<>(), candidates.size(), 0, 0, 0, 0), otherSolutions);
        }
        optTime.stop();

        //Exec phase
        List<Qfset> executed = new ArrayList<>();
        Stopwatch execTime = Stopwatch.createUnstarted();

        //Main execution Loop (Algorithm 2 line 5)
        for (;plan.hasNext();) {
            InfoCollector ic = plan.next();
            execTime.start();
            runQuery(ic);
            plan.setExecuted(ic);
            execTime.stop();
            executed.add(ic.getDataSource().getInternal());
        }
        double dist = 0d;
        List<InfoCollector> ics = new ArrayList<>(plan.getExecuted());
        for (int i = 0; i < ics.size() - 1; i++) {
            dist += 1 - Jaccard.similarity(ics.get(i).getDataSource().getInternal(), ics.get(i+1).getDataSource().getInternal());
        }

        return new Pair<>(new TAPStats(q0, genTime,optTime, execTime, executed, candidates.size(),
                plan.getExecuted().stream().mapToDouble(interestingness::rate).sum(),
                dist, tmpTime, 0), otherSolutions);
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

    public static List<Session> loadCubeloadXML(String filePath, Connection connection, String cubeName){
        ArrayList<Session> sessions = new ArrayList<>();
        int count = 0;
        try {
            JAXBContext context = JAXBContext.newInstance(Benchmark.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Benchmark benchmark = (Benchmark) unmarshaller.unmarshal(new File(filePath));

            CubeUtils cube = new CubeUtils(connection, cubeName);
            SchemaReader reader = cube.getCube().getSchemaReader(null).withLocus();

            for (com.olap3.cubeexplorer.data.cubeloadBeans.Session s : benchmark.getSession()){
                List<Query> queries = new ArrayList<>();
                for (com.olap3.cubeexplorer.data.cubeloadBeans.Query q : s.getQuery()){
                    List<QueryPart> parts = new ArrayList<>();
                    // Load Dimensions
                    for (Element dim : q.getGroupBy().getElement()){
                        String hName = ((Hierarchy) dim.getContent().stream().filter(e -> e instanceof Hierarchy).findFirst().get()).getValue();
                        String levelName = ((Level) dim.getContent().stream().filter(e -> e instanceof Level).findFirst().get()).getValue();
                        String tmp = "["+hName+"]";
                        Dimension mdDim = reader.getCubeDimensions(cube.getCube()).stream()
                                .filter(dimension -> Arrays.stream(dimension.getHierarchies()).anyMatch(h -> h.toString().equals(tmp)))
                                .findFirst().get();
                        parts.add(QueryPart.newDimension("[" + mdDim.getName() +"].[" + levelName + "]"));
                    }

                    //Load Filters
                    for (Object obj : q.getSelectionPredicates().getContent()){
                        if (obj instanceof String)
                            continue;
                        Element sel = (Element) obj;
                        String hName = ((Hierarchy) sel.getContent().stream().filter(e -> e instanceof Hierarchy).findFirst().get()).getValue();
                        String levelName = ((Level) sel.getContent().stream().filter(e -> e instanceof Level).findFirst().get()).getValue();
                        String predicate = ((Predicate) sel.getContent().stream().filter(e -> e instanceof Predicate).findFirst().get()).getValue();
                        String tmp = "["+hName+"]";
                        Dimension mdDim = reader.getCubeDimensions(cube.getCube()).stream()
                                .filter(dimension -> Arrays.stream(dimension.getHierarchies()).anyMatch(h -> h.toString().equals(tmp)))
                                .findFirst().get();
                        parts.add(QueryPart.newFilter(predicate, "[" + mdDim.getName() + "].[" + levelName + "]"));
                        //parts.add(QueryPart.newFilter(predicate));
                    }

                    //Load Measures
                    for (Element meas : q.getMeasures().getElement()){
                        String measName =  meas.getValue();
                        parts.add(QueryPart.newMeasure("[Measures].["+measName+"]"));
                    }

                    queries.add(new Query(parts));
                }

                sessions.add(new Session(queries, s.getTemplate(), filePath + "_" + count++, cubeName));
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return sessions;
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
        ExecutionPlan plan = ks.findBestPlan(candidates, budgetms);
        optTime.stop();

        //Exec phase
        Set<Pair<Qfset, mondrian.olap.Result>> executed = new HashSet<>();
        Stopwatch execTime = Stopwatch.createUnstarted();
        int reoptGood = 0, reoptBad = 0;

        //Main execution Loop (Algorithm 2 line 5)
        Stopwatch self = Stopwatch.createStarted();
        for (;plan.hasNext();) {
            InfoCollector ic = plan.next();

            execTime.start();
            runQuery(ic);
            execTime.stop();
            executed.add(new Pair<>(ic.getDataSource().getInternal(), null));
            plan.setExecuted(ic);


            if (!reoptEnabled)
                continue;
            optTime.start();
            if(DOLAP.reoptRoutine(plan, candidates, optTime.elapsed(TimeUnit.MILLISECONDS) + execTime.elapsed(TimeUnit.MILLISECONDS), budgetms, ks))
                reoptGood++;
            else
                reoptBad++;
            optTime.stop();
        }
        System.out.println("SELF REOPT = " + self.elapsed(TimeUnit.MILLISECONDS) + " EXEC = " + execTime.elapsed(TimeUnit.MILLISECONDS));


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
            query = query.replace("SELECT", "SELECT SQL_NO_CACHE");
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
