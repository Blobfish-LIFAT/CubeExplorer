package it.unibo.csr.big.cubeload;

import com.olap3.cubeexplorer.model.MeasureFragment;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.SelectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import it.unibo.csr.big.cubeload.generator.OlapGenerator;
import it.unibo.csr.big.cubeload.generator.Profile;
import it.unibo.csr.big.cubeload.io.XMLReader;
import it.unibo.csr.big.cubeload.schema.GroupByElement;
import it.unibo.csr.big.cubeload.schema.Measure;
import it.unibo.csr.big.cubeload.schema.SelectionPredicate;
import it.unibo.csr.big.cubeload.schema.Session;
import mondrian.olap.Connection;
import mondrian.olap.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static it.unibo.csr.big.cubeload.generator.OlapGenerator.getMDHierarchy;

public class MakeTriplets {
    public static void main(String[] args) {
        Profile p = getProfile("/home/alex/IdeaProjects/CubeExplorer/lib/nn.xml");
        String schemaPath = "/home/alex/IdeaProjects/CubeExplorer/data/cubeSchemas/DOPAN_DW3.xml";
        String cubeName = "Cube1MobProInd";

        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null) {
            System.err.println("Couldn't initialize db/mondrian connection, check stack trace for details. Exiting now.");
            System.exit(1); //Crash the app can't do anything w/o mondrian
        }
        CubeUtils utils = new CubeUtils(olap, cubeName);
        CubeUtils.setDefault(utils);

        int maxMeasures = 4, minReportSize = 1, maxReportSize = Integer.MAX_VALUE, surprisingQueries = 2;

        System.out.println("Starting generation");
        OlapGenerator og = new OlapGenerator(1,
                maxMeasures,
                minReportSize,
                maxReportSize,
                surprisingQueries,
                cubeName,
                schemaPath,
                null,
                List.of(p));
        try {
            og.generateWorkload();
        } catch (Exception e){
            e.printStackTrace();
        }

        List<Session> sessions = og.getSessions();
        List<Qfset> triples = new ArrayList<>();

        System.out.println("Begin conversion");
        for (Session s : sessions){
            for (it.unibo.csr.big.cubeload.schema.Query q : s.getQueryList()){

                //Convert measures
                HashSet<MeasureFragment> mfs = new HashSet<>();
                for (Measure m : q.getMeasures()){
                    mfs.add(MeasureFragment.newInstance(utils.getMeasure(m.getName())));
                }

                //convert group by set
                HashSet<ProjectionFragment> pfs = new HashSet<>();
                for (GroupByElement gbe : q.getGroupBySet()){
                    Level l = OlapGenerator.getMondrianLevel(gbe.getL(), getMDHierarchy(utils, gbe.getH()));
                    pfs.add(ProjectionFragment.newInstance(l));
                }

                //convert selections
                HashSet<SelectionFragment> sfs = new HashSet<>();
                for (SelectionPredicate sp : q.getPredicates()){
                    //TODO
                }

                triples.add(new Qfset(pfs, sfs, mfs));
            }
        }

        for (Qfset t : triples){
            System.out.println(t);
            System.out.println();
        }
    }

    private static Profile getProfile(String path) {
        Profile p = null;
        try {
            p = new XMLReader().getProfile(new File(path).getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
}
