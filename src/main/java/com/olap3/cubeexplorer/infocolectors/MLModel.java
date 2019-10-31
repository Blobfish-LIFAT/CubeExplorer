package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public interface MLModel extends TimeableOp {
    public ECube process();
    public void setDataSource(DataAccessor da);
}
