package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.julien.Qfset;
import mondrian.olap.Result;

public class MDXAccessor implements DataAccessor {

    Qfset internal;

    public Result execute(){
        return null;//TODO
    }

    @Override
    public int aprioriTuples() {
        return 0;
    }

    @Override
    public int aposterioriTuples() {
        return 0;
    }

    @Override
    public int aprioriTime() {
        return 0;
    }

    @Override
    public int aposterioriTime() {
        return 0;
    }

    @Override
    public double aprioriInterest() {
        return 0;
    }

    @Override
    public double aposterioriInterest() {
        return 0;
    }
}
