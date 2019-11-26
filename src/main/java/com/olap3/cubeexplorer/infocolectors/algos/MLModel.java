package com.olap3.cubeexplorer.infocolectors.algos;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public interface MLModel extends TimeableOp {
    public ECube process(DataSet ds);
}
