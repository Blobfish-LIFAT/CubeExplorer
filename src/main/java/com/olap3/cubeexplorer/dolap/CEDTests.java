package com.olap3.cubeexplorer.dolap;

import com.olap3.cubeexplorer.Similarity.Session.CompareSessions;
import com.olap3.cubeexplorer.data.IpumsLoader;
import com.olap3.cubeexplorer.model.julien.QuerySession;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class CEDTests {
    public static void main(String[] args) throws IOException {

        PrintWriter out = new PrintWriter(new FileOutputStream(new File("data/sim_ipums.csv")));

        MondrianConfig.defaultConfigFile = args[0];
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null)
            System.exit(1); //Crash the app can't do anything w/o mondrian
        CubeUtils utils = new CubeUtils(olap, "IPUMS");
        CubeUtils.setDefault(utils);
        MondrianConfig.setMondrianConnection(olap);

        IpumsLoader loader = new IpumsLoader(utils, "data/ipumsLogs");

        List<QuerySession> sessions = loader.loadDir();
        sessions.sort(Comparator.comparing(QuerySession::getId));
        double[][] smatrix = new double[sessions.size()][];
        for (int i = 0; i < smatrix.length; i++) {
            smatrix[i] = new double[sessions.size()];
        }

        System.out.println("--- Sim matrix ---");
        var header = sessions.stream().map(qs -> qs.getId()).collect(Collectors.toList());
        for (int i = 0; i < header.size(); i++) {
            String s = header.get(i);
            out.print(s);
            if (i != header.size() - 1)
                out.print(";");
        }
        out.print("\n");

        for (int i = 0; i < sessions.size(); i++) {
            QuerySession s1 = sessions.get(i);
            smatrix[i][i] = 0;
            for (int j = i + 1; j < sessions.size(); j++) {
                QuerySession s2 = sessions.get(j);

                CompareSessions compare = new CompareSessions(s1, s2);
                double s = 1 - compare.compareTwoSessionsBySW();
                smatrix[i][j] = s;
                smatrix[j][i] = s;
            }
        }

        System.out.println(Arrays.deepToString(smatrix));
        for (double[] line : smatrix){
            var tmp = Arrays.toString(line);
            out.println(Arrays.toString(line).substring(1, tmp.length()-1).replace(", ", ";"));
        }

        out.close();



    }
}
