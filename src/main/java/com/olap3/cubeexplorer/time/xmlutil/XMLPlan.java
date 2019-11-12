package com.olap3.cubeexplorer.time.xmlutil;

public class XMLPlan {
    public final long estimated_tuples;
    public final long estimated_time;
    public final double total_cost;
    public final long full_row_cost;

    public XMLPlan(long estimated_tuples, long estimated_time, double total_cost, long full_row_cost) {
        this.estimated_tuples = estimated_tuples;
        this.estimated_time = estimated_time;
        this.total_cost = total_cost;
        this.full_row_cost = full_row_cost;
    }

    @Override
    public String toString() {
        return String.format("estimated tuples: %d%nestimated cost: %f", this.estimated_tuples, this.total_cost);
    }
}