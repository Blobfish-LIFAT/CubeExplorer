package com.olap3.cubeexplorer.deep;

import com.alexscode.utilities.Future;
import com.olap3.cubeexplorer.data.CellSet;
import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.model.Compatibility;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.SelectionFragment;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.model.legacy.Query;
import com.olap3.cubeexplorer.model.legacy.QueryPart;
import com.olap3.cubeexplorer.model.legacy.Session;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.OlapElement;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import smile.stat.distribution.GaussianMixture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TrainSetBuilder {
    private static final Logger LOGGER = Logger.getLogger(TrainSetBuilder.class.getName());

    public static final String testData = "./data/import_ideb",
                            featuresExport = "./data/stats/im_trainset.csv",
                            cubeName = "Cube1MobProInd";


    public static void main(String[] args) throws Exception{
        LOGGER.info("Initializing Mondrian");// Init code
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null) {
            LOGGER.severe("Couldn't initialize db/mondrian connection, check stack trace for details. Exiting now.");
            System.exit(1); //Crash the app can't do anything w/o mondrian
        }
        CubeUtils utils = new CubeUtils(olap, cubeName);
        CubeUtils.setDefault(utils);
        LOGGER.info("Mondrian connection init complete");

        utils.forceMembersCaching();
        LOGGER.info("Members cached");

        LOGGER.info("Loading test data from " + testData);
        List<Session> sessions = DopanLoader.loadDir(testData);
        LOGGER.info("Test data loaded");

        PrintWriter feat = new PrintWriter(new FileOutputStream(new File(featuresExport), false));

        //Build one hot map
        List<String> measures = utils.getMeasures().stream().map(OlapElement::getName).collect(Collectors.toUnmodifiableList());
        List<String> projections = utils.fetchAllDimensions(false)
                .stream().flatMap(dim -> Arrays.stream(dim.getHierarchies()))
                .flatMap(h -> Arrays.stream(h.getLevels())).map(OlapElement::getUniqueName).collect(Collectors.toUnmodifiableList());
        List<String> selections = new ArrayList<>(projections.size()*2);
        for (String projection :projections){
            selections.add(projection + "$Active");
            selections.add(projection + "$CountMembers");
        }

        //Building header
        feat.print("queryID,");
        feat.print(Future.join(measures, ",") + ",");
        feat.print(Future.join(projections, ",") + ",");
        feat.print(Future.join(selections, ",") + ",");
        feat.print("avg,variance,skewness,1stq,median,3rdq,mu1,sigma1,pi1,mu2,sigma2,pi2,mu3,sigma3,pi3,raw\n");
        feat.flush();

        for (Session sess : sessions){
            System.out.println("Parsing session " + sess.getFilename());
            int qnb = 0;
            for (Query q : sess.getQueries()){
                System.out.println("    Parsing Q" + qnb);
                //Run query
                java.sql.Connection connection = DriverManager.getConnection(MondrianConfig.getURL());
                OlapWrapper wrapper = (OlapWrapper) connection;
                OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
                OlapStatement statement = olapConnection.createStatement();
                //CellSet cs = new CellSet(statement.executeOlapQuery(internal.toMDXString()));
                final Qfset triplet = Compatibility.QPsToQfset(q, utils);
                CellSet cs = new CellSet(statement.executeOlapQuery(Compatibility.QfsetToMDX(triplet)));
                DataSet ds;
                try {
                    ds = Compatibility.cellSetToDataSet(cs, true);
                }catch (Exception e){
                    System.out.println("        error: " + e.getMessage());
                    qnb++;
                    continue;
                }
                for (QueryPart measure : q.getMeasures()){
                    double[] targets = computeTargets(ds, measure.getValue());
                    if (targets == null)
                        continue;

                    String id = sess.getFilename() + "$" + qnb;
                    feat.print(id + ","); // Query id

                    //measure encoding
                    int[] m = new int[measures.size()];
                    Arrays.fill(m, 0);
                    int mind = getMeasureIndex(measures, measure);
                    m[mind] = 1;
                    feat.print(Future.arrayToString(m, ",") + ",");

                    //projection encoding
                    int[] p = new int[projections.size()];
                    Arrays.fill(p, 0);
                    for (QueryPart proj : q.getDimensions()){
                        p[projections.indexOf(proj.getValue())] = 1;
                    }
                    feat.print(Future.arrayToString(p, ",") + ",");

                    //selection encoding
                    int[] s = new int[selections.size()];
                    Arrays.fill(s, 0);
                    for (SelectionFragment sf : triplet.getSelectionPredicates()){
                        int sind = selections.indexOf(sf.getLevel().getUniqueName() + "$Active");
                        s[sind] = 1;
                        s[sind + 1] = 0; //FIXME count members ?
                    }
                    feat.print(Future.arrayToString(s, ",") + ",");

                    feat.print(Future.arrayToString(targets, ",") + ",\"" + Arrays.toString(getDataCol(ds, measure.getValue())) + "\"\n");

                }

                feat.flush();
                qnb++;
            }
        }

        feat.close();
    }

    public static int getMeasureIndex(List<String> measures, QueryPart measure) {
        int index = measures.indexOf(measure.getValue());
        if (index != -1)
            return index;

        String name = measure.getValue().split("]\\.\\[")[1].replace("]", "");
        return measures.indexOf(name);
    }

    public static double[] computeTargets(DataSet ds, String measureValue) {
        double[] data = getDataCol(ds, measureValue);
        if (data == null) return null;

        DescriptiveStatistics stats = new DescriptiveStatistics(data);

        int k = 3;
        GaussianMixture gm = GaussianMixture.fit(k, data);
        double[] gmm = new double[3 * k];

        int i = 0;
        for (var component : gm.components){
            gmm[i++] = component.distribution.mean();
            gmm[i++] = component.distribution.sd();
            gmm[i++] = component.priori;
        }

        return ArrayUtils.addAll(new double[]{stats.getMean(), stats.getVariance(), stats.getSkewness(),
                stats.getPercentile(0.25), stats.getPercentile(0.5), stats.getPercentile(0.75)},
                gmm);
    }

    public static double[] getDataCol(DataSet ds, String measureValue) {
        double[] data = ds.getDoubleColumn(measureValue);
        if (data == null){
            String name = measureValue.split("]\\.\\[")[1].replace("]", "");
            data = ds.getDoubleColumn(name);
        }
        if (data == null)
            return null;
        return data;
    }
}
