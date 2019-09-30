package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.olap.CellSet;
import com.olap3.cubeexplorer.olap.HeaderTree;

import java.util.HashMap;

/**
 * An enhanced cube (ie cells + other info)
 */
public class ECube {
    CellSet rawData;

    private String algorithmType;

    HashMap<String, Object> explProperties;

    double interestingness;

    public ECube(CellSet rawData) {
        this.rawData = rawData;
    }
}
