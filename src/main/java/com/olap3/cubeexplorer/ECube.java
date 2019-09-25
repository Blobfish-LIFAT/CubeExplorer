package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.olap.HeaderTree;

import java.util.HashMap;

/**
 * An enhanced cube (ie cells + other info)
 */
public class ECube {
    private Double[][] data;
    private HeaderTree rowHeaders;
    private HeaderTree columnHeaders;

    private String algorithmType;

    HashMap<String, Object> explProperties;

    double interestingness;
}
