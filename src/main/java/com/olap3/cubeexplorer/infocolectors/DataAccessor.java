package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.optimize.TimeableOp;

/**
 * The data accessor is abstract data access element, usually this is a database query
 */
public abstract class DataAccessor  implements TimeableOp {
    Qfset internal;

    public abstract DataSet execute();

    public Qfset getInternal() {
        return internal;
    }


}
