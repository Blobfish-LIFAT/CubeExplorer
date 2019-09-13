package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.QueryPart;
import com.olap3.cubeexplorer.julien.Fragment;

import java.util.Set;

public abstract class InfoCollector {
    DataAccessor dataSource;
    MLModel model;
    Set<Fragment> dimensions, measures, selections;


    // We will decompose the intentional query as query parts to express the "subcube"
    public InfoCollector(Set<Fragment> dims , Set<Fragment> measures, Set<Fragment> selections){
        this.dimensions = dims;
        this.measures = measures;
        this.selections = selections;
    }

    public abstract int timeEstimate();
    public abstract int realTime();
    public abstract ECube execute();
}
