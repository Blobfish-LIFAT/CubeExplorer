package com.olap3.cubeexplorer.optimize;

public interface TimeableOp {
    public int getTimeEstimate();
    public int getRealTime();
}
