package com.olap3.cubeexplorer.evaluate;

import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.infocolectors.DataAccessor;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.infocolectors.algos.MLModel;
import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.optimize.AprioriMetric;
import com.olap3.cubeexplorer.optimize.BudgetManager;
import com.olap3.cubeexplorer.optimize.KnapsackManager;
import com.olap3.cubeexplorer.optimize.tsp.LinKernighan;
import com.olap3.cubeexplorer.optimize.tsp.Measurable;
import mondrian.olap.Dimension;
import mondrian.olap.Level;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CacheAwareEvaluator implements Evaluator {
    Map<DataAccessor, List<MLModel>> algos;
    Set<DataAccessor> reoptAfter;
    Set<InfoCollector> all;
    Map<InfoCollector, ECube> results = new HashMap<>();

    ExecutionPlan current;
    CubeUtils utils;
    AprioriMetric im;

    boolean setupDone = false;
    boolean reopt;
    long budget;
    Stopwatch runTime;

    ExecutorService poolExecutor;

    public CacheAwareEvaluator(CubeUtils utils, AprioriMetric im, long budgetms) {
        this.utils = utils;
        this.im = im;
        budget = budgetms;
    }

    @Override
    public List<ECube> evaluate() {
        KnapsackManager ks = new KnapsackManager(im, 0.05);
        if (!setupDone)
            throw new IllegalStateException("Call setup first to initialize the evaluator !");

        for (;current.hasNext();) {
            InfoCollector ic = current.next();

            results.put(ic, runAndGetResult(ic));
            current.setExecuted(ic);
            if (!reopt)
                continue;
            List<InfoCollector> candidates = all.stream().filter(c -> !current.getLeft().contains(c))
                    .filter(c -> !current.getExecuted().contains(c)).collect(Collectors.toList());
            reoptRoutine(current, candidates, runTime, budget, ks);
        }

        //Ordering phase
        List<InfoCollector> executed = new ArrayList<>(current.getExecuted());
        List<Qfset> toOrder = executed.stream().map(ic -> ic.getDataSource().getInternal()).collect(Collectors.toList());
        List<Integer> ids = IntStream.range(0, toOrder.size()).boxed().collect(Collectors.toList());
        LinKernighan tsp = new LinKernighan(toOrder.stream().map(q -> (Measurable) q).collect(Collectors.toList()), ids);
        tsp.runAlgorithm();

        List<ECube> ordered = new ArrayList<>(executed.size());
        for (int index : tsp.tour){
            ordered.add(results.get(executed.get(index)));
        }

        return ordered;
    }

    private ECube runAndGetResult(InfoCollector ic) {
        DataAccessor da = ic.getDataSource();
        DataSet ds = da.execute();
        MLModel model = ic.getModel();
        ECube res = model.process(ds);
        res.getExplProperties().put("query", da.getInternal());
        return res;
    }

    @Override
    public boolean setup(ExecutionPlan p, boolean reoptEnable) {
        runTime = Stopwatch.createStarted();
        all = new HashSet<>(p.getOperations());
        buildExecOrder(p.getOperations());
        this.reopt = reoptEnable;
        poolExecutor = new ThreadPoolExecutor(1,2,5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        setupDone = true;
        return true;
    }

    @Override
    public EXPRunStats getStats() {
        return null;
    }

    public void buildExecOrder(Collection<InfoCollector> base){
        List<SimpleDirectedGraph<InfoCollector, ICEdge>> ranks = new ArrayList<>();
        List<InfoCollector> baseList = new ArrayList<>(base);

        for (Dimension dim : utils.fetchAllDimensions(false)){
            HashMap<Level, Integer> localOrder = new HashMap<>();
            int i = 0;
            System.out.println("found " + dim.getHierarchies().length + " hierarchies");
            for (Level l : dim.getHierarchies()[0].getLevels()){
                localOrder.put(l, i++);
            }

            // Compares ICs based on the order described by the dimension 'dim'
            Comparator<InfoCollector> icComp = (ic1, ic2) -> {
                Level l1 = ic1.getDataSource().getInternal().getAttributes()
                        .stream().map(ProjectionFragment::getLevel).filter(lvl -> lvl.getDimension().equals(dim)).findFirst().get();
                Level l2 = ic2.getDataSource().getInternal().getAttributes()
                        .stream().map(ProjectionFragment::getLevel).filter(lvl -> lvl.getDimension().equals(dim)).findFirst().get();
                return Integer.compare(localOrder.get(l1), localOrder.get(l2));
            };

            SimpleDirectedGraph<InfoCollector, ICEdge> order = new SimpleDirectedGraph<InfoCollector, ICEdge>(ICEdge.class);
            base.forEach(order::addVertex);
            for (int j = 0; j < base.size(); j++) {
                for (int k = j + 1; k < base.size(); k++) {
                    if (icComp.compare(baseList.get(j), baseList.get(k)) > 0){
                        order.addEdge(baseList.get(j), baseList.get(k));
                    }
                }
            }

            ranks.add(order);
        }

        // Pareto combination of the orders (no conflict allowed)
        SimpleDirectedGraph<InfoCollector, ICEdge> pOrder = new SimpleDirectedGraph<InfoCollector, ICEdge>(ICEdge.class);
        ranks.forEach(g -> Graphs.addGraph(pOrder, g));

        Set<ICEdge> conflicts = new HashSet<>();
        for (ICEdge e : pOrder.edgeSet()){
            InfoCollector u = e.getU(); InfoCollector v = e.getV();
            if (pOrder.containsEdge(v, u)) {
                conflicts.add(pOrder.getEdge(v,u));
                conflicts.add(pOrder.getEdge(u, v));
            }
        }

        pOrder.removeAllEdges(conflicts);

        //We extract all connected components those are total orders they form the tree's branches
        KosarajuStrongConnectivityInspector<InfoCollector, ICEdge> inspector = new KosarajuStrongConnectivityInspector<>(pOrder);
        List<Graph<InfoCollector, ICEdge>> branches = inspector.getStronglyConnectedComponents();

        // We need to consider the most interesting branch first
        branches.sort(Comparator.comparingDouble(o -> o.vertexSet().stream().mapToDouble(im::rate).sum()));

        //TODO remove duplicate queries
        List<InfoCollector> ics = new ArrayList<>();
        for (Graph<InfoCollector, ICEdge> branch : branches){
            InfoCollector first = topVertex(branch, branch.vertexSet().iterator().next());
            ics.add(first);
            InfoCollector current = first;
            while ((current = branch.outgoingEdgesOf(current).iterator().next().getV()) != null){
                ics.add(current);
            }
        }
        current = new ExecutionPlan(ics, false);
    }

    /**
     * Re-optimization routine
     * @param plan the current execution plan
     * @param candidates the original search space
     * @param runTime the total run time stopwatch
     * @param budget the original time budget
     * @param bm A Budget Manager to perform the re-optimization
     */
     void reoptRoutine(ExecutionPlan plan, List<InfoCollector> candidates, Stopwatch runTime, long budget, BudgetManager bm) {
        long left = budget - runTime.elapsed(TimeUnit.MILLISECONDS);

        // Case one we OUTATIME
        if (left < 1){
            //kill the plan !
            plan.removeAll(plan.getOperations());
            return;
        }

        long predictedRunTime = plan.getLeft().stream().mapToLong(InfoCollector::estimatedTime).sum();

        // Case two less time left than anticipated
        if (predictedRunTime > left+budget*0.01){
            if (plan.getOperations().size() == 0)
                return;
            List<InfoCollector> restOfExec = bm.findBestPlan(new ArrayList<>(plan.getOperations()), (int)left).getOperations();
            plan.getOperations().retainAll(restOfExec);
            return;
        }

        //Case three we have more time than anticipated
        List<InfoCollector> possibleStuff = candidates.stream().filter(ic -> ic.estimatedTime() < left).collect(Collectors.toList());
        possibleStuff.removeAll(plan.getOperations());
        possibleStuff.removeAll(plan.getExecuted());
        try {
            List<InfoCollector> newStuff = bm.findBestPlan(possibleStuff,(int)left).getOperations();
            //System.out.println("Added " + newStuff.size());
            plan.addAll(newStuff);
        } catch (IllegalArgumentException e){ // No more stuff to run anyway
            return;
        }
        return;
    }

    /**
     * Warning this only works if the graph is a linked list or a tree
     * @param branch a tree
     * @return the root
     */
    private static InfoCollector topVertex(Graph<InfoCollector, ICEdge> branch, InfoCollector from){
        if (branch.incomingEdgesOf(from).size() > 1)
            throw new IllegalArgumentException("This is not a tree !");
        if (branch.incomingEdgesOf(from).isEmpty())
            return from;
        else
            return topVertex(branch, branch.incomingEdgesOf(from).stream().findFirst().get().getU());
    }
}
