package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.infocolectors.algos.MLModel;
import com.olap3.cubeexplorer.model.ECube;

/**
 * This is basically a drill down operation
 */
public class ICRestriction extends InfoCollector {
    public ICRestriction(DataAccessor da, MLModel ml) {
        dataSource = da;
        model = ml;
    }

    @Override
    public ECube executeInternal() {
        return null; //TODO
    }


}
