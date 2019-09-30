package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.julien.Qfset;

public class MDXAccessor extends DataAccessor {

    public MDXAccessor(Qfset query) {
        this.internal = query;
    }

    @Override
    public DataSet execute() {
        String mdxQuery = ""; //Todo Conversion to runnable MDX (or finish my star join thing)
        return null;
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
