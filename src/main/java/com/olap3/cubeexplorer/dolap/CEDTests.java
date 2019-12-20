package com.olap3.cubeexplorer.dolap;

import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;


public class CEDTests {
    public static void main(String[] args) {

        MondrianConfig.defaultConfigFile = args[0];
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null)
            System.exit(1); //Crash the app can't do anything w/o mondrian
        CubeUtils utils = new CubeUtils(olap, "IPUMS");
        CubeUtils.setDefault(utils);
        MondrianConfig.setMondrianConnection(olap);



    }
}
