package com.olap3.cubeexplorer.mondrian;

import mondrian.olap.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CubeUtils {
    private static final Logger LOGGER = Logger.getLogger( CubeUtils.class.getName() );
    Connection con;
    Cube cube;
    static String defaultCubeName = "";
    static CubeUtils defaultCube = null;

    public CubeUtils(Connection con, String cubeName) {
        this.con = con;
        this.cube = getCubeByName(cubeName);
        if (cube == null){
            LOGGER.warning("Couldn't find cube '"+cubeName+"' in schema '"+con.getSchema().getName()+"'");
        }
    }

    private Cube getCubeByName(String name){
        Schema schema = con.getSchema();

        for (Cube c : schema.getCubes()){
            if (c.getName().equals(name))
                return c;
        }
        return null;
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
        List<Member> levelMembers = schemaReader.getLevelMembers(l, true);
        return levelMembers;
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
        HashSet<Hierarchy> hierachies = new HashSet<Hierarchy>();

        for (int i = 0; i < cubeDims.length; i++) {
            Hierarchy[] tmpH = cubeDims[i].getHierarchies();
            hierachies.addAll(Arrays.asList(tmpH));
        }

        return hierachies;
    }

    public static CubeUtils getDefault(){
        if (defaultCube == null){
            if (defaultCubeName.equals(""))
                defaultCube = new CubeUtils(MondrianConfig.getMondrianConnection(), MondrianConfig.getMondrianConnection().getSchema().getCubes()[0].getName());
            else
                defaultCube = new CubeUtils(MondrianConfig.getMondrianConnection(), defaultCubeName);
        }
        return defaultCube;
    }

    public static void setDefault(CubeUtils cubeUtils){
        defaultCube = cubeUtils;
    }

}
