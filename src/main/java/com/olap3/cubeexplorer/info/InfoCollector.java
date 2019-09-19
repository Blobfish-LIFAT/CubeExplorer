package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;


public abstract class InfoCollector {
    DataAccessor dataSource;
    MLModel model;
    //Set<ProjectionFragment> dimensions;
    //Set<MeasureFragment> measures;
    //Set<SelectionFragment> selections;


    // We will decompose the intentional query as query parts to express the "subcube"
    public InfoCollector(){

    }

    public abstract int timeEstimate();
    public abstract int realTime();
    public abstract ECube execute();
}
