package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.infocolectors.algos.MLModel;
import com.olap3.cubeexplorer.model.ECube;

public class ICCheckParents extends InfoCollector{

    public ICCheckParents(DataAccessor da, MLModel model) {
        this.dataSource = da;
        this.model = model;
    }

    @Override
    public ECube executeInternal() {
        return null; //TODO
    }

}
