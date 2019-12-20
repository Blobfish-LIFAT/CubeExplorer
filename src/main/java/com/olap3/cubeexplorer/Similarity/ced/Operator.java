package com.olap3.cubeexplorer.Similarity.ced;

import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.julien.QuerySession;

import java.util.function.BiFunction;

public abstract class Operator {
    BiFunction sim;
    QuerySession session;
    Qfset newQuery;
    int k;

    public abstract QuerySession edit();
    public abstract double getCost();
}
