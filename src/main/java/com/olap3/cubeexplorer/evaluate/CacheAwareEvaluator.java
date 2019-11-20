package com.olap3.cubeexplorer.evaluate;

import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.infocolectors.DataAccessor;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.infocolectors.MLModel;
import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.optimize.AprioriMetric;
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

public class CacheAwareEvaluator implements Evaluator {
    HashMap<DataAccessor, Set<MLModel>> algos;
    List<DataAccessor> queries;
    CubeUtils utils;
    AprioriMetric im;

    ExecutorService poolExecutor;

    public CacheAwareEvaluator(CubeUtils utils, AprioriMetric im) {
        this.utils = utils;
        this.im = im;
        poolExecutor = new ThreadPoolExecutor(1,2,5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    public List<ECube> evaluate() {
        //TODO
        return null;
    }

    @Override
    public boolean setup(ExecutionPlan p, Stopwatch runtime, boolean reoptEnable) {
        buildExecOrder(p.getOperations());
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

        //TODO
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
