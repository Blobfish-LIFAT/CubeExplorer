package com.olap3.cubeexplorer.optimize;

public interface CostModel {
    public void updateCost(TimeableOp operation);
    public long correctedEstimate(TimeableOp operation);
}
