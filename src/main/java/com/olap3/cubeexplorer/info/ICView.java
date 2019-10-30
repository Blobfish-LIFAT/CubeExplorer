package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.data.ECube;

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
    public ECube execute() {
        return null;
    }

    @Override
    public String toString() {
        return "ICView: " + comment;
    }
}
