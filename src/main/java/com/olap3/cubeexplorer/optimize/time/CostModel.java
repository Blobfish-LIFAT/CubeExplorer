package com.olap3.cubeexplorer.optimize.time;

import com.olap3.cubeexplorer.optimize.TimeableOp;

public interface CostModel {
    /**
     * Provide a cost estimate for a given operation in milliseconds
     * @param operation any TimeableOperation
     * @return it's estimated cost in ms
     */
    public long estimateCost(TimeableOp operation);
}
