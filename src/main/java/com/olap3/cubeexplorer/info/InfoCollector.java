package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;

public abstract class InfoCollector {
    DataAccessor dataSource;
    MLModel model;

    public abstract double timeEstimate();
    public abstract double realTime();
    public abstract ECube execute();
}
