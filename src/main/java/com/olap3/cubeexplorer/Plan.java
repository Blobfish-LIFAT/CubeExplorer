package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.info.InfoCollector;

import java.util.List;

public class Plan {
    public final long estimated_tuples;
    public final long estimated_time;
    public final double total_cost;

    public Plan(long estimated_tuples, long estimated_time, double total_cost) {
        this.estimated_tuples = estimated_tuples;
        this.estimated_time = estimated_time;
        this.total_cost = total_cost;
    }

    @Override
    public String toString() {
        return String.format("estimated tuples: %d%nestimated cost: %f", this.estimated_tuples, this.total_cost);
    }
}