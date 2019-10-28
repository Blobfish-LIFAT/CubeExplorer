package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;

public class ICView extends InfoCollector {
    public ICView(DataAccessor dataAccessor) {
        this.dataSource = dataAccessor;
    }

    @Override
    public long estimatedTime() {
        return dataSource.aprioriTime();
    }

    @Override
    public ECube execute() {
        return null;
    }
}
