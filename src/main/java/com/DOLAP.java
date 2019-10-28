package com;

import com.alexscode.utilities.Nd4jUtils;
import com.alexscode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
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

    // For testing stuff
    static Qfset testQuery;


    public static void main(String[] args) {
        LOGGER.info("Init Starting");
        Connection olap = MondrianConfig.getMondrianConnection();
        utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
        LOGGER.info("DB Connection init complete");

        LOGGER.info("Loading test data from " + testData);
        var sessions = DopanLoader.loadDir(testData);
        testQuery = Compatibility.QPsToQfset(sessions.get(0).getQueries().get(0));

        LOGGER.info("Computing interestigness scores");
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
        topoGraph = null; logGraph = null;
        mem.invokeGc();

        System.out.println("Computing PageRank...");
        Pair<INDArray, HashMap<QueryPart, Integer>> ref = PageRank.pagerank(base, 50);
        INDArray rawScores = ref.getLeft();
        HashMap<QueryPart, Double> interest = new HashMap<>(ref.getRight().size());
        ref.right.entrySet().forEach(e -> {
            interest.put(e.getKey(), rawScores.getDouble(e.getValue()));
        });

        System.out.println("Freeing memory");
        base = null; ref = null;
        mem.invokeGc();

        /*
                    Fin des pre-calculs mettre le code de test ci apres
         */
        LOGGER.info("Begin test phase");
        // Init KS
        BudgetManager ks = new KnapsackManager(ic -> {
            var qps = Compatibility.partsFromQfset(ic.getDataSource().getInternal());
            double sum = qps.stream().mapToDouble(interest::get).sum();
            return sum/qps.size();
        });

        //Test query 0
        System.out.println(ks.findBestPlan(generateCandidates(testQuery)));

    }

    public static List<InfoCollector> generateCandidates(Qfset q0){
        List<InfoCollector> queries = new ArrayList<>();

        // Build roll-ups
        for (ProjectionFragment f : q0.getAttributes()){
            ProjectionFragment p  = ProjectionFragment.newInstance(f.getLevel().getParentLevel());

            HashSet<ProjectionFragment> tmp = new HashSet<>(q0.getAttributes());
            tmp.remove(f); tmp.add(p);

            Qfset query = new Qfset(tmp, new HashSet<>(q0.getSelectionPredicates()), new HashSet<>(q0.getMeasures()));
            queries.add(new ICView(new MDXAccessor(query)));
        }

        // Build drill-downs
        for (var sf : q0.getAttributes()){
            Level target = sf.getLevel().getChildLevel();
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
