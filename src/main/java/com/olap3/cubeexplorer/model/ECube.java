package com.olap3.cubeexplorer.model;

import com.olap3.cubeexplorer.measures.Jaccard;
import com.olap3.cubeexplorer.tsp.Measurable;

import java.util.HashMap;

/**
 * An enhanced cube (ie cells + other info)
 */
public class ECube implements Measurable<ECube> {
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

    @Override
    public double dist(ECube other) {
        return Jaccard.similarity(this.cubeQuery, other.cubeQuery);
    }
}
