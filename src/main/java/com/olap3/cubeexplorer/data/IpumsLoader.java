package com.olap3.cubeexplorer.data;

import com.olap3.cubeexplorer.Similarity.Session.CompareSessions;
import com.olap3.cubeexplorer.model.MeasureFragment;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.SelectionFragment;
import com.olap3.cubeexplorer.model.julien.QuerySession;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Level;
import mondrian.olap.Member;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class IpumsLoader {
    CubeUtils utils;
    String directoryPath;

    public IpumsLoader(CubeUtils utils, String directoryPath) {
        this.utils = utils;
        this.directoryPath = directoryPath;
    }

    public QuerySession loadFile(Path path) {
        System.out.println(path.getFileName());
        List<String> rawFile;
        try {
            rawFile = Files.readAllLines(path);
        } catch (IOException e) {
            System.err.printf("Error parsing Ipums file %s%n", path.getFileName());
            return null;
        }
        QuerySession session = new QuerySession(path.getFileName().toString().replace(".txt", ""));

        String line = "";
        int i = 6;
        while (!(line = rawFile.get(i++)).equals("#endSession")){
            //System.out.println(line);
            int qid = Integer.parseInt(line.replace("#", ""));

            //Parse measures
            line = rawFile.get(i++);
            String[] measures = line.split(",");
            HashSet<MeasureFragment> mfs = new HashSet<>(measures.length);
            for (String mname : measures){
                if (!mname.equals("NONE"))
                    mfs.add(MeasureFragment.newInstance(utils.getMeasure(mname)));
            }

            //Parse projections
            line = rawFile.get(i++);
            String[] projections = line.split(",");
            HashSet<ProjectionFragment> pfs = new HashSet<>(projections.length);
            for (String pname : projections){
                String[] tmp = pname.split("\\.");
                String lname = "[" + tmp[0] + "].[" + tmp[1] + "]";
                //System.out.println(lname);
                Level level = utils.getLevel(lname);
                pfs.add(ProjectionFragment.newInstance(level));
            }

            //Parse selections
            line = rawFile.get(i++);
            String[] selections = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            HashSet<SelectionFragment> sfs = new HashSet<>(selections.length);
            for (String sname : selections){
                if (sname.equals("NONE"))
                    continue;
                String[] lp = sname.split("=");
                String[] tmp = lp[0].split("\\.");
                Level level = utils.getLevel("[" + tmp[0] + "].[" + tmp[1] + "]");
                Member member = utils.getMember(level, lp[1].substring(1, lp[1].length() - 1));
                if (member == null)
                    System.err.printf("Erroneous member for selection %s%n", sname);
                else
                    sfs.add(SelectionFragment.newInstance(member));
            }

            Qfset query = new Qfset(pfs, sfs, mfs);
            session.add(query);
        }
        return session;
    }

    public List<QuerySession> loadDir(){
        if (!Files.isDirectory(Paths.get(directoryPath))){
            System.err.printf("Warning '%s' is not a valid directory !", directoryPath);
        }
        try {
            return Files.walk(Paths.get(directoryPath)).filter(p -> p.toFile().isFile()).map(this::loadFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) throws IOException{
        PrintWriter out = new PrintWriter(new FileOutputStream(new File("data/sim_ipums.csv")));

        MondrianConfig.defaultConfigFile = args[0];
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null)
            System.exit(1); //Crash the app can't do anything w/o mondrian
        CubeUtils utils = new CubeUtils(olap, "IPUMS");
        CubeUtils.setDefault(utils);
        MondrianConfig.setMondrianConnection(olap);

        IpumsLoader loader = new IpumsLoader(utils, "data/ipumsLogs");
        QuerySession sq = loader.loadFile(Paths.get("data/ipumsLogs/01_Benjamin_1_5_74969_06-06-2014-11-08-16.txt"));
        //System.out.println(sq);

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
