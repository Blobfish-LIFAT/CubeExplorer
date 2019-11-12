package com.olap3.cubeexplorer.optimize.tsp;

public interface Measurable<T> {

    public double dist(T other);

}
