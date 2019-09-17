package com.olap3.cubeexplorer.optimize;

public interface CostModel {
    public void updateCost(TimeableOp operation);
    public int correctedEstimate(TimeableOp operation);
}
