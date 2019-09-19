package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public interface MLModel extends TimeableOp {
    public ECube process(DataSet input);
}
