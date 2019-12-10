package it.unibo.csr.big.cubeload.generator;

import com.olap3.cubeexplorer.mondrian.CubeUtils;
import it.unibo.csr.big.cubeload.io.CSVReader;
import it.unibo.csr.big.cubeload.io.XMLReader;
import it.unibo.csr.big.cubeload.io.XMLWriter;
import it.unibo.csr.big.cubeload.schema.*;
import mondrian.olap.Member;
import mondrian.rolap.RolapHierarchy;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This class implements the initialization of the generator's
 * parameters, both global (valid for all profiles) and local
 * (specific for every profile).
 *
 * @author Luca Spadazzi
 */

public class OlapGenerator {
    private static final int SECOND_LAST_PADDING = 2;
    private static final int NUMBER_OF_STARTING_PREDICATES = 2;

    private int numberOfProfiles = 0;
    private int maxMeasures;
    private int minReportSize;
    private int maxReportSize;
    private int numberOfSurprisingQueries;
    private String cubeName;
    private String schemaPath;
    private String csvPath;
    private List<Profile> profiles;
    List<Session> sessions;

    private List<Query> surprisingQueries = new ArrayList<Query>();
    Random rand = new Random();

    /**
     * Class constructor.
     *
     * @param numberOfProfiles          Number of profiles to be generated.
     * @param maxMeasures               Maximum number of measures in a query.
     * @param minReportSize             Minimum size of the report for starting queries.
     * @param maxReportSize             Maximum size of the report for starting queries.
     * @param numberOfSurprisingQueries Number of surprising queries.
     */
    public OlapGenerator(int numberOfProfiles, int maxMeasures, int minReportSize,
                         int maxReportSize, int numberOfSurprisingQueries, String cubeName,
                         String schemaPath, String csvPath, List<Profile> profiles) {
        this.numberOfProfiles = numberOfProfiles;
        this.maxMeasures = maxMeasures;
        this.minReportSize = minReportSize;
        this.maxReportSize = maxReportSize;
        this.numberOfSurprisingQueries = numberOfSurprisingQueries;
        this.cubeName = cubeName;
        this.schemaPath = schemaPath;
        this.csvPath = csvPath;
        this.profiles = profiles;
    }

    /**
     * This method invokes an XMLReader to parse a multidimensional schema.
     *
     * @param schemaPath The canonical path of the schema file.
     * @param cubeName   The name of the cube to be built.
     * @return An empty cube, fully structured.
     * @throws Exception
     */
    private Cube getCube(String schemaPath, String cubeName) throws Exception {
        XMLReader xmlReader = new XMLReader();

        return xmlReader.getCube(schemaPath, cubeName);
    }

    /**
     * This method creates a fixed number of surprising queries,
     * which will frequently emerge in Explorative-based
     * sessions. Each query is set up on random hierarchy levels,
     * has casual Measures and a fixed number of selection predicates,
     * chosen among any non-temporal Dimension.
     *
     * @param cube The Cube containing the schema information.
     */
    private void createSurprisingQueries(Cube cube) {
        for (int i = 0; i < this.numberOfSurprisingQueries; ++i) {
            Query query = new Query();
            Level level;

            for (Hierarchy hie : cube.getHierarchies()) {
                level = hie.getRandomLevel(true); // The level can be an ALL level
                query.addGroupByElement(hie, level);
            }

            // The visibility of the query's Hierarchies is checked
            query.checkValidity(cube);

            // A fixed amount of distinct Measures is added.
            query.setMeasures(cube.getRandomMeasures(maxMeasures));

            List<Hierarchy> selectableHierarchies = new ArrayList<Hierarchy>();

            for (Hierarchy hierarchy : cube.getHierarchies()) {
                String queryLevel = query.findLevel(hierarchy.getName());
                int position = hierarchy.findPosition(queryLevel);

                if (position < hierarchy.getLevelCount() - SECOND_LAST_PADDING) {
                    selectableHierarchies.add(hierarchy);
                }
            }

            if (selectableHierarchies.size() < NUMBER_OF_STARTING_PREDICATES) {
                --i;
                continue;
            }

            List<Hierarchy> selectedHierarchies = new ArrayList<Hierarchy>();

            // A fixed amount of predicates on different hierarchies is set
            for (int j = 0; j < NUMBER_OF_STARTING_PREDICATES; ++j) {
                Hierarchy hie;
                String queryLevel;
                int position;

                hie = selectableHierarchies.get(rand.nextInt(selectableHierarchies.size()));

                selectedHierarchies.add(hie); // One more hierarchy selected
                selectableHierarchies.remove(hie); // One less selectable hierarchy

                queryLevel = query.findLevel(hie.getName());
                position = hie.findPosition(queryLevel);

                // The selection predicate must restrict a "non-ALL" hierarchy level
                // more aggregated than the current query level in the given hierarchy
                level = hie.getLevel(hie.getValidPredicatePosition(position));

                query.addSelectionPredicate(new SelectionPredicate(hie.getName(),
                        level.getName(),
                        level.getRandomValue(),
                        false,
                        false));
            }

            surprisingQueries.add(query);
        }
    }

    private void getHierarchyInformation(Cube cube, String csvPath) throws IOException {
        if (csvPath != null)
            getHInfoCSV(cube, csvPath);
        else
            getHInfoMondrian(cube, CubeUtils.getDefault());
    }


    /**
     * Fetches members from mondrian and the underlying database
     * @param cube the cubeload cube struct
     * @param utils matching cube utilities object
     */
    private void getHInfoMondrian(Cube cube, CubeUtils utils) {
        for (Hierarchy hie : cube.getHierarchies()) {

            mondrian.olap.Hierarchy mdHie = getMDHierarchy(utils, hie);
            hie.setMd(mdHie);

            List<Level> toDel = new ArrayList<>();
            for (Level l : hie.getLevels()) {
                if (!l.isLeaf(hie)) {
                    mondrian.olap.Level mdLevel = getMondrianLevel(l, mdHie);

                    // The level was probably wrongly inferred by cubeload remove it
                    if (mdLevel == null){
                        toDel.add(l);
                        continue;
                    }

                    //System.out.println(hie.getName() + "." + l.getName());
                    //System.out.println("  " + mdHie + mdLevel);
                    for (Member m : utils.fetchMembers(mdLevel)){
                        l.addDistinctValues(m.getName());
                    }

                    l.setMd(mdLevel);

                }
            }

            for (Level l : toDel){
                hie.getLevels().remove(l);
            }
        }
    }

    /**
     * This method cycles through CSV files containing the Dimension Table tuples,
     * to collect distinct values for every "non-all" level of each hierarchy.
     *
     * @param cube    The object containing the cube's information and data.
     * @param csvPath The canonical path of the directory containing the CSV files.
     * @throws IOException
     */
    private void getHInfoCSV(Cube cube, String csvPath) throws IOException {
        CSVReader reader;
        String fileName;

        for (Hierarchy hie : cube.getHierarchies()) {
            System.out.println(hie);
            fileName = csvPath + "/" + hie.getName().toUpperCase() + ".csv";
            reader = new CSVReader(new FileReader(fileName), ',', '\"');
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                List<String> tempList = new ArrayList<String>();

                for (int i = 1; i < nextLine.length; ++i) {
                    tempList.add(nextLine[i]);
                }

                hie.setLevelValues(tempList);
            }

            reader.close();
        }
    }

    /**
     * Main method for the class.
     *
     * @throws Exception
     */
    public void generateWorkload() throws Exception {
        Cube cube = getCube(schemaPath, cubeName);

        System.out.println("Hierarchy traversal start");
        getHierarchyInformation(cube, csvPath);
        System.out.println("Hierarchy traversal done");

        // Creation of the surprising queries
        createSurprisingQueries(cube);

        // Creation of sessions
        sessions = new ArrayList<Session>();

        for (Profile profile : profiles) {
            sessions.addAll(profile.getSessions(cube,
                    maxMeasures,
                    minReportSize,
                    maxReportSize,
                    surprisingQueries));
        }


    }

    public void saveWorkload(String path) {
        // Generation of the XML output file
        XMLWriter.XMLGenerator(path, numberOfProfiles,
                maxMeasures,
                minReportSize,
                maxReportSize,
                numberOfSurprisingQueries,
                profiles,
                sessions);
    }

	public List<Session> getSessions() {
		return sessions;
	}

	public static mondrian.olap.Hierarchy getMDHierarchy(CubeUtils utils, Hierarchy hie){
        mondrian.olap.Hierarchy mdHie = null;

        for (mondrian.olap.Hierarchy candidate : utils.getHierarchies()){
            if (candidate.getDimension().isMeasures())
                continue;

            RolapHierarchy rh = (RolapHierarchy) candidate;
            if (rh.getDimension().getName().equals(hie.getName())) {
                mdHie = candidate;
                break;
            } else if (rh.getSubName().equals(hie.getName())){
                mdHie = candidate;
            }
        }

        return mdHie;
    }

    public static mondrian.olap.Level getMondrianLevel(Level l, mondrian.olap.Hierarchy mdHie){
        mondrian.olap.Level mdLevel = null;

        for (mondrian.olap.Level candidate : mdHie.getLevels()){
            if (candidate.getName().equals(l.getName()))
                mdLevel = candidate;
        }

        return mdLevel;
    }
}