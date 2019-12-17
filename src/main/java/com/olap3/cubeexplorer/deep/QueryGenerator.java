package com.olap3.cubeexplorer.deep;

import com.alexscode.utilities.Future;
import com.olap3.cubeexplorer.data.CellSet;
import com.olap3.cubeexplorer.model.*;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.*;
import net.razorvine.pyro.serializer.PickleSerializer;
import net.razorvine.pyro.serializer.PyroSerializer;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class QueryGenerator {
    public static final double muMeasures = 3, sigmaMeasures = 2.5,
                            muSelect = 2, sigmaSelect = 1;
    public static int queryNb = 10;
    public static Random rand = new Random(42);
    public static String outfile = "./data/stats/bigFatTrainset.pickle";
    public static boolean testing = false;

    public static void main(String[] args) throws Exception{
        if (args.length < 2 ){
            if (!testing) {
                System.out.println("Usage java -jar qgen.jar <queryNumber> <outfile>");
                System.exit(1);
            } else {
                System.out.println("Using default parameters");
            }
        } else {
            queryNb = Integer.parseInt(args[0]);
            outfile = args[1];
        }

        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null) {
            System.out.println("Couldn't initialize db/mondrian connection, check stack trace for details. Exiting now.");
            System.exit(1); //Crash the app can't do anything w/o mondrian
        }
        CubeUtils utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);

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

        FileOutputStream out = new FileOutputStream(new File(outfile));
        PyroSerializer serializer = new PickleSerializer();
        //PrintWriter feat = new PrintWriter(new FileOutputStream(new File(outfile), false));

        //Building header
        StringBuilder header = new StringBuilder();
        header.append("queryID,");
        header.append(Future.join(measures, ",") + ",");
        header.append(Future.join(projections, ",") + ",");
        header.append(Future.join(selections, ",") + ",");
        header.append("avg,variance,skewness,1stq,median,3rdq,mu1,sigma1,pi1,mu2,sigma2,pi2,mu3,sigma3,pi3,raw\n");

        out.write(serializer.serializeData(header));

        java.sql.Connection connection = java.sql.DriverManager.getConnection(MondrianConfig.getURL());
        OlapWrapper wrapper = (OlapWrapper) connection;
        OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);

        List<Member> allMeasures = utils.getMeasures();
        List<Hierarchy> hierarchies = utils.getHierarchies().stream().filter(h -> !h.getName().equals("Measures")).collect(Collectors.toList());

        for (int i = queryNb; i > 0; i--) {
            int nd = (int) (rand.nextGaussian() * sigmaMeasures + muMeasures);
            int measuresToDraw = Math.max(nd, 1);

            List<Member> selectedMeasures = getNFromList(allMeasures, measuresToDraw);
            HashSet<MeasureFragment> mfs = new HashSet<>(selectedMeasures.size());
            for (Member m : selectedMeasures){
                mfs.add(MeasureFragment.newInstance(m));
            }

            Set<Hierarchy> selectedHierarchies = new HashSet<>();
            for (Hierarchy h : hierarchies){
                if (h.getName().equals("Measures"))
                    continue;
                if (rand.nextBoolean())
                    selectedHierarchies.add(h);
            }

            HashSet<ProjectionFragment> pfs = new HashSet<>(hierarchies.size());
            for (Hierarchy h : hierarchies){
                if (selectedHierarchies.contains(h)){
                    Level[] levels = h.getLevels();
                    Level l = levels[rand.nextInt(levels.length)];
                    pfs.add(ProjectionFragment.newInstance(l));
                }else {
                    // Should add all member ? This is implicit ?
                    //pfs.add(ProjectionFragment.newInstance(h.getAllMember().getLevel()));
                }
            }

            HashSet<SelectionFragment> sfs = new HashSet<>();
            if (rand.nextBoolean()){
                List<Hierarchy> toSelect = getNFromList(hierarchies, (int) Math.max(1, rand.nextGaussian()*sigmaSelect + muSelect));
                for (Hierarchy h : toSelect){
                    Level[] levels = h.getLevels();
                    Level l = levels[rand.nextInt(levels.length)];

                    List<Member> members = utils.fetchMembers(l);
                    Member m = members.get(rand.nextInt(members.size()));

                    sfs.add(SelectionFragment.newInstance(m));
                }
            }

            Qfset triplet = new Qfset(pfs, sfs, mfs);
            System.out.println("--- --- --- --- --- --- --- ---");
            System.out.println(triplet);
            System.out.println(Compatibility.QfsetToMDX(triplet));

            //FIXME
            runQueryAndWriteStats(null, String.valueOf(i), olapConnection, triplet, measures, projections, selections);
        }
    }

    public static void runQueryAndWriteStats(PrintWriter feat, String id, OlapConnection olapConnection, Qfset triplet, List<String> measures, List<String> projections, List<String> selections) throws OlapException {
        OlapStatement statement = olapConnection.createStatement();
        //CellSet cs = new CellSet(statement.executeOlapQuery(internal.toMDXString()));

        CellSet cs = new CellSet(statement.executeOlapQuery(Compatibility.QfsetToMDX(triplet)));
        DataSet ds;
        try {
            ds = Compatibility.cellSetToDataSet(cs, true);
        }catch (Exception e){
            System.err.println("Error parsing CellSet: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        for (MeasureFragment measure : triplet.getMeasures()){
            double[] targets = TrainSetBuilder.computeTargets(ds, measure.getAttribute().getUniqueName());
            if (targets == null)
                continue;

            feat.print(id + ","); // Query id

            //measure encoding
            boolean[] m = new boolean[measures.size()];
            Arrays.fill(m, false);
            int mind = getMeasureIndex(measures, measure);
            m[mind] = true;
            feat.print(Future.arrayToString(m, ",") + ",");

            //projection encoding
            boolean[] p = new boolean[projections.size()];
            Arrays.fill(p, false);
            for (ProjectionFragment proj : triplet.getAttributes()){
                p[projections.indexOf(proj.getLevel().getUniqueName())] = true;
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

            feat.print(Future.arrayToString(targets, ",") + ",\"" + Arrays.toString(TrainSetBuilder.getDataCol(ds, measure.getAttribute().getName())) + "\"\n");

        }

        feat.flush();
    }

    public static int getMeasureIndex(List<String> measures, MeasureFragment measure) {
        int index = measures.indexOf(measure.getAttribute().getName());
        if (index != -1)
            return index;

        String name = measure.getAttribute().getUniqueName().split("]\\.\\[")[1].replace("]", "");
        return measures.indexOf(name);
    }

    private static <T> List<T> getNFromList(List<T> elements, int nb) {
        List<T> drawn = new ArrayList<>();

        if (nb >= elements.size())
            return new ArrayList<>(elements);

        Set<Integer> alreadyDone = new TreeSet<>();

        while (nb != 0){
            int c = rand.nextInt(elements.size());
            if (!alreadyDone.contains(c)){
                alreadyDone.add(c);
                nb--;
            }
        }

        for (int index : alreadyDone){
            drawn.add(elements.get(index));
        }

        return drawn;
    }
}
