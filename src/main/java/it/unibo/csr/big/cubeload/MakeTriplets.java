package it.unibo.csr.big.cubeload;

import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import it.unibo.csr.big.cubeload.generator.OlapGenerator;
import it.unibo.csr.big.cubeload.generator.Profile;
import it.unibo.csr.big.cubeload.io.XMLReader;
import it.unibo.csr.big.cubeload.schema.Session;
import mondrian.olap.Connection;

import java.io.File;
import java.util.List;

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

        int maxMeasures = 4, minReportSize = 5, maxReportSize = 30, surprisingQueries = 2;

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
