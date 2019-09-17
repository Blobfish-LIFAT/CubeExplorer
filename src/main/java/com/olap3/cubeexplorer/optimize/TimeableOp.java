package com.olap3.cubeexplorer.optimize;

public interface TimeableOp {
    public int aprioriTime();
    public int aposterioriTime();
    public int aprioriTuples();
    public int aposterioriTuples();
    public double aprioriInterest();
    public double aposterioriInterest();
}
