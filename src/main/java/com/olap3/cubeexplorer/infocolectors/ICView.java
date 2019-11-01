package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.ECube;

public class ICView extends InfoCollector {
    public ICView(DataAccessor dataAccessor) {
        this.dataSource = dataAccessor;
    }
    public ICView(DataAccessor dataAccessor, String comment) {
        this.dataSource = dataAccessor;
        this.comment = comment;
    }

    @Override
    public long estimatedTime() {
        return dataSource.aprioriTime();
    }

    @Override
    public long realTime() {
        return dataSource.aposterioriTime();
    }

    @Override
    public ECube executeInternal() {
        return null;
    }

    @Override
    public String toString() {
        return "ICView: " + comment;
    }
}
