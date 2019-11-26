package com.olap3.cubeexplorer.model;

import com.olap3.cubeexplorer.measures.Jaccard;
import com.olap3.cubeexplorer.optimize.tsp.Measurable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

/**
 * An enhanced cube (ie cells + other info)
 */
public class ECube implements Measurable<ECube> {
    @Getter @Setter
    Qfset cubeQuery;

    private String algorithmType;

    HashMap<String, Object> explProperties;

    double interestingness;

    public ECube(String algorithmType) {
        this.cubeQuery = cubeQuery;
        this.algorithmType = algorithmType;
        explProperties = new HashMap<>();
    }

    public HashMap<String, Object> getExplProperties() {
        return explProperties;
    }

    @Override
    public double dist(ECube other) {
        return 1 - Jaccard.similarity(this.cubeQuery, other.cubeQuery);
    }
}
