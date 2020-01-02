package com.olap3.cubeexplorer.dolap;

import com.olap3.cubeexplorer.model.MeasureFragment;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.SelectionFragment;
import com.olap3.cubeexplorer.model.julien.QuerySession;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import mondrian.olap.Level;
import mondrian.olap.Member;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class CubeLoadTextLogLoader {
    public static List<QuerySession> loadFile(Path p, CubeUtils utils){
        List<QuerySession> sessions = new ArrayList<>();
        try {
            Iterator<String> it = Files.lines(p).iterator();
            for (;it.hasNext();){
                String info = it.next();
                String[] infos = info.split(", template: ");
                int sid = Integer.parseInt(infos[0].replace("Session n.",""));
                String ssid = sid > 9 ? String.valueOf(sid) : ("0" + sid);
                QuerySession qs = new QuerySession( ssid + "/" + infos[1]);

                String line = "";
                while (it.hasNext() && !(line = it.next()).equals("-------------------------------")){
                    if (line.matches("\\d+")){
                        int qid = Integer.parseInt(line);

                        line = it.next();
                        String[] projections = line.split(", ");
                        HashSet<ProjectionFragment> pfs = new HashSet<>(projections.length);
                        for (String pname : projections){
                            String[] tmp = pname.split("\\.");
                            String lname = "[" + tmp[0] + "].[" + tmp[1] + "]";
                            Level level = utils.getLevel(lname);
                            pfs.add(ProjectionFragment.newInstance(level));
                        }

                        line = it.next();
                        String[] selections = line.split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        HashSet<SelectionFragment> sfs = new HashSet<>(selections.length);
                        for (String sname : selections){
                            if (sname.equals(""))
                                continue;
                            String[] lp = sname.split(" = ");
                            String[] tmp = lp[0].split("\\.");
                            Level level = utils.getLevel("[" + tmp[0] + "].[" + tmp[1] + "]");
                            Member member = utils.getMember(level, lp[1]);
                            if (member == null)
                                System.err.printf("Erroneous member for selection %s%n", sname);
                            else
                                sfs.add(SelectionFragment.newInstance(member));
                        }

                        line = it.next();
                        String[] measures = line.split(", ");
                        HashSet<MeasureFragment> mfs = new HashSet<>(measures.length);
                        for (String mname : measures){
                            if (!mname.equals("NONE"))
                                mfs.add(MeasureFragment.newInstance(utils.getMeasure(mname)));
                        }

                        Qfset query = new Qfset(pfs, sfs, mfs);
                        qs.add(query);
                    }
                }
                sessions.add(qs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sessions;
    }
}
