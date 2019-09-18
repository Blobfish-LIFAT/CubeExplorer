package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.QueryPart;
import com.olap3.cubeexplorer.julien.Fragment;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.SelectionFragment;

import java.util.List;
import java.util.Set;

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
