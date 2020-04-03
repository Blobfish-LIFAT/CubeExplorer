package com.olap3.cubeexplorer.optimize;

/**
 * Any operation whose run time can be estimated and then measured
 */
public interface TimeableOp {
    public long aprioriTime();
    public long aposterioriTime();
}
