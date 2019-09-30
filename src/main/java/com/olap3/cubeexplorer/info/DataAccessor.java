package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public abstract class DataAccessor  implements TimeableOp {
    Qfset internal;

    public abstract DataSet execute();

    public Qfset getInternal() {
        return internal;
    }
}
