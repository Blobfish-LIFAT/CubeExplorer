package com.olap3.cubeexplorer.mondrian;

import com.alexscode.utilities.Reflect;
import mondrian.olap.*;
import mondrian.rolap.RolapCube;
import mondrian.rolap.RolapCubeHierarchy;
import mondrian.rolap.RolapCubeLevel;
import mondrian.rolap.RolapLevel;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CubeUtils {
    private static final Logger LOGGER = Logger.getLogger( CubeUtils.class.getName() );

    private Connection con;
    private Cube cube;
    private String factTable = null ;

    //Caches
    private Map<String, Member> memberCache;
    private Set<Hierarchy> membersCached;
    private boolean membersCacheComplete = false;

    // singleton stuff
    private static String defaultCubeName = "";
    private static CubeUtils defaultCube = null;

    public CubeUtils(Connection con, String cubeName) {
        this.con = con;
        this.cube = getCubeByName(cubeName);
        if (cube == null){
            LOGGER.warning("Couldn't find cube '"+cubeName+"' in schema '"+con.getSchema().getName()+"'");
        }
        //init caches
        memberCache = new HashMap<>();
        membersCached = new HashSet<>();
    }

    /**
     * Init code to fetch the targeted cube object
     * @param name Name of the cube as specified in schema
     * @return the corresponding mondrian cube object
     */
    private Cube getCubeByName(String name){
        Schema schema = con.getSchema();

        for (Cube c : schema.getCubes()){
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    /**
     * Fetches the member by name, warning this is expensive as heck, caching strategy is in place
     * @param name the name of the member as is in the database, (i.e. Partial like 'Paris' instead of the 'unique names' like
     *             '[Geography.fine hierarchy].[City].[Paris]' use getMemberExact if you have this type of name)
     * @return the member object
     */
    public Member getMember(String name){
        Member cacheTry = memberCache.get(name);
        if (cacheTry != null) // Yeah cache hit !
            return cacheTry;

        SchemaReader schemaReader = cube.getSchemaReader(null).withLocus();
        for (Dimension d : cube.getDimensions()){
            if (!d.isMeasures()){
                for (Hierarchy h : d.getHierarchies()){
                    if (membersCached.contains(h))
                        continue;
                    for (Level l : h.getLevels()){
                        for (Member member :schemaReader.getLevelMembers(l, true)){
                            String cname = member.getName();
                            memberCache.putIfAbsent(cname, member);
                            if (cname.equals(name))
                                return member;
                        }
                    }
                    membersCached.add(h);
                }
            }
        }

        System.err.printf("Member '%s' no found in cube '%s'%n", name, cube);
        return null;
    }

    /**
     * Forces the building of the full member cache of the cube
     */
    public void forceMembersCaching(){
        LOGGER.info("Full member caching forced on '" + cube.getName() + "' cube, this may take a few minutes...");
        if (membersCacheComplete)
            return;
        SchemaReader schemaReader = cube.getSchemaReader(null).withLocus();
        for (Dimension d : cube.getDimensions()){
            if (!d.isMeasures()){
                for (Hierarchy h : d.getHierarchies()){
                    if (membersCached.contains(h))
                        continue;
                    for (Level l : h.getLevels()){
                        for (Member member :schemaReader.getLevelMembers(l, true)){
                            memberCache.putIfAbsent(member.getName(), member);
                        }
                    }
                    membersCached.add(h);
                }
            }
        }
        membersCacheComplete = true;
    }

    /**
     * Gets the table name for the fact table of the active cube
     * @return te fact table name as specified in the schema
     */
    public String getFactTableName(){
        if (factTable == null) {
            RolapCube actualCube = (RolapCube) cube;
            MondrianDef.Table rel = (MondrianDef.Table) Reflect.getField(actualCube, "fact");
            factTable = rel.name;
        }
        return factTable;

    }

    public String getForeignKey(Dimension dim){
        MondrianDef.CubeDimension actualDim = (MondrianDef.CubeDimension) Reflect.getField(dim, "xmlDimension");
        return actualDim.foreignKey;
    }

    /**
     * Fetches the table name for a given hierarchy in the cube
     * @param h a hierarchy in the schema
     * @return the table name
     */
    @RolapOnly
    public String getTableName(Hierarchy h){
        MondrianDef.Hierarchy xmlHierarchy = (MondrianDef.Hierarchy) Reflect.getField(((RolapCubeHierarchy)h).getRolapHierarchy(), "xmlHierarchy");
        return xmlHierarchy.relation.toString();
    }

    /**
     * Fetches the primary key from the dimension table of the given hierarchy
     * @param h a hierarchy in the current schema
     * @return the primary key name
     */
    public String getPrimaryKey(Hierarchy h){
        MondrianDef.Hierarchy xmlHierarchy = (MondrianDef.Hierarchy) Reflect.getField(((RolapCubeHierarchy)h).getRolapHierarchy(), "xmlHierarchy");
        return xmlHierarchy.primaryKey;
    }

    public String getColumn(Level l){
        RolapLevel actualLevel = ((RolapCubeLevel) l).getRolapLevel();
        MondrianDef.Expression keyExp = (MondrianDef.Expression) Reflect.getField(actualLevel, "keyExp");
        MondrianDef.Expression nameExp = (MondrianDef.Expression) Reflect.getField(actualLevel, "nameExp");

        if (keyExp == null && nameExp == null) // To my knowledge only happens for 'All' levels
            System.err.println("level " + l.toString() + " has no known expression");
        if (nameExp == null)
            return keyExp.getGenericExpression();
        else
            return nameExp.getGenericExpression();
    }

    public static Dimension getDimensionByName(Cube c, String name){
        for (Dimension d : c.getDimensions()){
            if (d.getName().equals(name))
                return d;
        }
        return null;
    }

    public Dimension getDimensionByName(String name){
        return getDimensionByName(this.cube, name);
    }

    public List<Member> fetchMembers(Level l){
        SchemaReader schemaReader = cube.getSchemaReader(null).withLocus();
        if (l == null) {
            LOGGER.warning("Level can't be null !");
            return new ArrayList<>();
        }
        return schemaReader.getLevelMembers(l, true);
    }

    public Member fetchMember(String dimName, String levelName, String name){
        Level l = this.getLevel(levelName, dimName);
        Member ret = null;
        for (Member m : this.fetchMembers(l)){
            if (m.getName().equals(name))
                ret = m;
        }
        return ret;
    }

    public List<List<Member>> fetchMembers(Hierarchy h){
        return Arrays.stream(h.getLevels()).map(this::fetchMembers).collect(Collectors.toList());
    }

    public List<Level> fetchLevels(Hierarchy h){
        return Arrays.asList(h.getLevels());
    }

    public Cube getCube() {
        return cube;
    }

    public  HashSet<Hierarchy> getHierarchies() {
        Dimension[] cubeDims = cube.getDimensions();
        HashSet<Hierarchy> hierachies = new HashSet<>();

        for (int i = 0; i < cubeDims.length; i++) {
            Hierarchy[] tmpH = cubeDims[i].getHierarchies();
            hierachies.addAll(Arrays.asList(tmpH));
        }

        return hierachies;
    }


    /**
     * This method reads the mondrian schema and returns the mondrian level
     * corresponding to the input attribute
     * Warnign Julien's code avoid using
     * @param attributeName: the name of the attribute we want to
     * retrieve
     * @param hierarchyName: the name of the hierarchy which the
     * attribute belongs to
     * @return the mondrian level corresponding to the attribute we want to
     * retrieve
     */
    @Deprecated
    public Level getLevel(String attributeName, String hierarchyName) {
        for (Hierarchy h : this.getHierarchies()) {
            // Note from alex: fixed this with a little split
            //System.out.printf("%s/%s%n",h.getName().split("\\.")[0].trim().toUpperCase(), hierarchyName.trim().toUpperCase());
            if (h.getName().split("\\.")[0].trim().toUpperCase().equals(hierarchyName.trim().toUpperCase())) {
                for (Level level : h.getLevels()) {
                    if (level.getName().trim().toUpperCase().equals(attributeName.trim().toUpperCase())) {
                        return level;
                    }
                }
                break;
            }
        }

        return null;
    }

    /**
     * Fetches a level by name by doing a full hierarchy traversal
     * @param exactName the exact name 'unique name' of the elvel (e.g. '[Dimension one.simple_hierarchy].[My precious level]')
     * @return the mondrian level object
     */
    public Level getLevel(String exactName){
        for (Hierarchy h : getHierarchies()){
            for (Level l : h.getLevels()){
                //System.out.println(l.getUniqueName() + "/" + exactName);
                if (l.getUniqueName().equals(exactName))
                    return l;
            }
        }
        return null;
    }

    /**
     * This method reads the mondrian schema and returns the mondrian member
     * corresponding to the input measure
     *
     * @param measureName: the name of the measure we want to retrieve
     * @return the member corresponding to the measure we want to retrieve
     */
    public Member getMeasure(String measureName) {
        Level l = getLevel("MeasuresLevel", "MEASURES");
        String mName = measureName.trim().toUpperCase();
        if (mName.startsWith("[MEASURES].["))
            mName = mName.substring(12, mName.length() - 1);
        List<Member> members = cube.getSchemaReader(null).withLocus().getLevelMembers(l, true);
        for (Member member : members) {
            //System.out.println(member.getName().trim().toUpperCase() + "/" + mName);
            if (member.getName().trim().toUpperCase().equals(mName)) {
                return member;
            }
        }

        return null;
    }

    /**
     * The default measure of a cube per Mondrian documentation is the first one to appear in the XML schema file
     * @return The default measure of the active cube
     */
    public Member getDefaultMeasure(){
        Level l = getLevel("MeasuresLevel", "MEASURES");
        return cube.getSchemaReader(null).withLocus().getLevelMembers(l, true).get(0);
    }

    public Member getSelection(String attributeName, String hierarchyName, String value) {

        Level l = getLevel(attributeName, hierarchyName);
        if (l == null) {
            System.out.printf("%s/%s/%s%n", l, attributeName, hierarchyName);
            l = getLevel(attributeName, hierarchyName.split("\\.")[0]);
        }
        List<Member> members = cube.getSchemaReader(null).withLocus().getLevelMembers(l, true);

        for (Member member : members) {
            /*if (member.getName().contains("2+")) {
                System.out.println(member);
            }*/
            //System.out.printf("%s==%s ?%n", member.getName().trim().toUpperCase(), value.trim().toUpperCase().substring(0,4));
            if (member.getName().trim().toUpperCase().equals(value.trim().toUpperCase())) {
                return member;
            } else if (member.getName().trim().toUpperCase().equals(value.trim().toUpperCase().substring(0,4))){
                return member;
            }
        }
        return null;
    }

    /**
     * Returns the CubeUtils for the default cube either set on the global property of this class,
     * manually by invoking setDefault(),
     * or the first cube of the schema.
     * @return the default cube utility class
     */
    public static CubeUtils getDefault(){
        if (defaultCube == null){
            if (defaultCubeName.equals("")) {
                String fetchedCube = MondrianConfig.getMondrianConnection().getSchema().getCubes()[0].getName();
                System.err.printf("Warning default cube not set returned '%s' as the default, use CubeUtils.setDefault() to override !%n", fetchedCube);
                defaultCube = new CubeUtils(MondrianConfig.getMondrianConnection(), fetchedCube);
            } else
                defaultCube = new CubeUtils(MondrianConfig.getMondrianConnection(), defaultCubeName);
        }
        return defaultCube;
    }

    public static void setDefault(CubeUtils cubeUtils){
        defaultCube = cubeUtils;
    }

}
