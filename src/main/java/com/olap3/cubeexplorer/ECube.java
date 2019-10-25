package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.julien.Jaccard;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.olap.CellSet;
import com.olap3.cubeexplorer.olap.HeaderTree;
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
