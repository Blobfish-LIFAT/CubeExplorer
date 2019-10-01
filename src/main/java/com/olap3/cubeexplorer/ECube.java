package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.olap.CellSet;
import com.olap3.cubeexplorer.olap.HeaderTree;

import java.util.HashMap;

/**
 * An enhanced cube (ie cells + other info)
 */
public class ECube {
    Qfset cubeQuery;

    private String algorithmType;

    HashMap<String, Object> explProperties;

    double interestingness;

    public ECube(Qfset cubeQuery, String algorithmType) {
        this.cubeQuery = cubeQuery;
        this.algorithmType = algorithmType;
        explProperties = new HashMap<>();
    }

    public HashMap<String, Object> getExplProperties() {
        return explProperties;
    }
}
