package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;


public abstract class InfoCollector {
    DataAccessor dataSource;
    MLModel model;


    // We will decompose the intentional query as query parts to express the "subcube"
    public InfoCollector(){

    }

    public  int estimatedTime(){
        return dataSource.aprioriTime() + model.aprioriTime();
    }
    public int realTime(){
        return dataSource.aposterioriTime() + model.aposterioriTime();
    }
    public abstract ECube execute();
}
