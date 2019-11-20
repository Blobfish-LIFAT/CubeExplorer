package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.DataSet;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public abstract class DataAccessor  implements TimeableOp {
    Qfset internal;

    public abstract DataSet execute();

    public Qfset getInternal() {
        return internal;
    }


}
