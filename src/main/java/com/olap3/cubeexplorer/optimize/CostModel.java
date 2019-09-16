package com.olap3.cubeexplorer.optimize;

public interface CostModel {
    public int updateCost(TimeableOp baseoperation);
}
