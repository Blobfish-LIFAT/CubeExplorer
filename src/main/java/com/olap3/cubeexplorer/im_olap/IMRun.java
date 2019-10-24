package com.olap3.cubeexplorer.im_olap;

import com.olap3.cubeexplorer.im_olap.graph.Graphs;
import com.olap3.cubeexplorer.im_olap.graph.OGraph;
import com.olap3.cubeexplorer.im_olap.graph.PageRank;
import com.olap3.cubeexplorer.im_olap.model.LoadSessions;
import com.olap3.cubeexplorer.im_olap.model.QueryPart;
import com.olap3.cubeexplorer.im_olap.model.Session;
import com.olap3.cubeexplorer.im_olap.model.SessionGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.olap3.cubeexplorer.im_olap.graph.PageRank.normalizeRowsi;

public class IMRun {
    static String explo = "Explorative", sliceDrill = "Slice and Drill", goal = "Goal Oriented", sliceAll = "Slice All";
    static String[] explos = new String[]{explo, sliceDrill, goal, sliceAll};
    static String sessionsDir = "data/session_set_1", schemaPath = "data/schema.xml", userProfile = goal;
    static int baseSize = 43, userSize = 7;
    static double alpha = 0.5, epsilon = 0.005;

    static DecimalFormat df = new DecimalFormat("#.##");

    static PrintWriter output = null;
    static String outputPath = "data/hellinger.csv";

    public static void main(String[] args) throws Exception {
        OutputStream out = Files.newOutputStream(Paths.get(outputPath));
        output = new PrintWriter(out);

        //System.out.println("userProfile;evalProfile;alpha;IM");
        output.println("userProfile;evalProfile;alpha;hellinger");

        for (int k = 0; k < 50; k++) {
            for (int j = 0; j < explos.length; j++) {
                userProfile = explos[j];
                for (int l = 0; l < explos.length; l++) {

                    for (int i = 1; i < 10; i++) {
                        alpha = i / 10.0;
                        runTest(explos[l]);
                    }
                }
            }
        }

        output.close();
        out.close();
    }

    private static void runTest(String evalProfile) {
        List<Session> sessions = LoadSessions.loadFromDir(sessionsDir);
        Collections.shuffle(sessions);

        List<Session> user = new ArrayList<>();
        List<Session> learning = new ArrayList<>();
        List<Session> eval = new ArrayList<>();

        int qota = userSize, qota2 = sessions.size() - userSize - baseSize;
        for (Session session : sessions){
             if (session.getType().equals(evalProfile) && qota2 > 0) {
                eval.add(session);
                qota2--;
            }else if (session.getType().equals(userProfile) && qota > 0) {
                user.add(session);
                qota--;
            }
            else
                learning.add(session);
        }

        //System.out.printf("sessions=%d, user=%d, learning=%d, eval=%d%n", sessions.size(), user.size(), learning.size(), eval.size());
        /**
         * WARNING THIS IS ONLY FOR DOLAP
         */
        learning.addAll(eval);

        OGraph<Double, QueryPart> base = SessionGraph.buildTopologyGraph(learning, schemaPath);
        SessionGraph.injectCousins(base, sessions);

        OGraph<Double, QueryPart> usage = SessionGraph.buildUsageGraph(base.getNodes(), user);

        usage.getNodes().forEach(base::addNode);
        base.getNodes().forEach(n -> base.setEdge(n,n,1.0));


        INDArray topology = Graphs.sortedINDMatrix(base);
        INDArray tp = Graphs.sortedINDMatrix(usage);


        INDArray uniform = Nd4j.ones(topology.shape());

        normalizeRowsi(topology);
        normalizeRowsi(tp);
        normalizeRowsi(uniform);

        // (1-e)*((1-a)*topo - a*tp) + e*uniform
        //INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha)).mul(1 - epsilon).add(uniform.mul(epsilon));
        INDArray pr = topology.mul(1-alpha).add(tp.mul(alpha));
        // without user
        INDArray pru = topology.mul(1 - epsilon).add(uniform.mul(epsilon));

        //TODO could do 15 iterations and check if converged ?
        INDArray pinf = PageRank.pageRank(pr, 42);
        System.out.println(userProfile + ";" + df.format(alpha) + ";" + Nd4jUtils.vecToString(pinf, ","));
        INDArray pinfu = PageRank.pageRank(pru, 42);
        System.out.println("Page Rank" + ";" + df.format(alpha) + ";" + Nd4jUtils.vecToString(pinfu, ","));
        runDivergenceTest(pinfu.getRow(0), pinf.getRow(0), evalProfile);

        //System.out.println(pinfu.sum(1));

        /*

        TreeMap<QueryPart, Integer> querryMap = new TreeMap<>();
        List<QueryPart> baseParts = new ArrayList<>(base.getNodes());

        Collections.sort(baseParts);

        //baseParts.forEach(System.out::println);

        for (int i = 0; i < baseParts.size(); i++) {
            querryMap.putIfAbsent(baseParts.get(i), i);
        }

        for (Session session : eval){
            List<QueryPart> parts = new ArrayList<>();
            session.queries.forEach(query -> parts.addAll(Arrays.asList(query.flat())));

            double sum = 0;
            int size = parts.size();

            for (QueryPart queryPart : parts) {
                try {
                    sum += log2(pinf.getDouble(0, querryMap.get(queryPart)));
                }catch (NullPointerException e){
                    System.err.println(queryPart);
                    size--;
                }
            }

            System.out.printf("%s;%s;%s;%s%n", userProfile, evalProfile, String.valueOf(alpha), String.valueOf(-sum/size));
        }
        */
    }

    private static void runDivergenceTest(INDArray pr, INDArray bpr, String evalProfile) {
        //double divergence = log2(distribution.columns()) - distribution.shannonEntropyNumber().doubleValue();
        //INDArray uniform = Nd4j.ones(distribution.shape());
        //normalizeRowsi(uniform);
        output.printf("%s;%s;%s;%s%n", userProfile, evalProfile, String.valueOf(alpha), Nd4jUtils.hellinger(pr, bpr));

    }
}
