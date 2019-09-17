package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.julien.Qfset;
import mondrian.olap.Result;

public class MDXAccessor implements DataAccessor {

    Qfset internal;




    public Result execute(){
        return null;//TODO
    }


    @Override
    public int getTimeEstimate() {
        return 0;
    }

    @Override
    public int getRealTime() {
        return 0;
    }
}
