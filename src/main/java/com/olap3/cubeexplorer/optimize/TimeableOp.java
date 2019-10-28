package com.olap3.cubeexplorer.optimize;

public interface TimeableOp {
    public long aprioriTime();
    public long aposterioriTime();
    public int aprioriTuples();
    public int aposterioriTuples();
    public double aprioriInterest();
    public double aposterioriInterest();
}
