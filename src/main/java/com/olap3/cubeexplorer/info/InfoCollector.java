package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.QueryPart;

import java.util.Set;

public abstract class InfoCollector {
    DataAccessor dataSource;
    MLModel model;


    // We will decompose the intentional query as query parts to express the "subcube"
    public InfoCollector(Set<QueryPart> dims , Set<QueryPart> measures, Set<QueryPart> selections){

    }

    public abstract int timeEstimate();
    public abstract int realTime();
    public abstract ECube execute();
}
