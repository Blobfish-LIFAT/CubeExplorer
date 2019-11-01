package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.Qfset;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


public abstract class InfoCollector {
    @Getter @Setter
    Qfset q0;
    DataAccessor dataSource;
    MLModel model;
    private long time_estimate = -1;
    @Getter
    private boolean executed;
    //For debugging only
    @Accessors
    String comment;


    // We will decompose the intentional query as query parts to express the "subcube"
    public InfoCollector(){

    }

    public long estimatedTime(){
        if (time_estimate == -1)
            time_estimate = dataSource.aprioriTime() + model.aprioriTime();
        return time_estimate;
    }

    public long realTime(){
        return dataSource.aposterioriTime() + model.aposterioriTime();
    }

    protected abstract ECube executeInternal();

    public final ECube execute(){
        ECube res = executeInternal();
        executed = true;
        return res;
    }

    public DataAccessor getDataSource() {
        return dataSource;
    }

    public MLModel getModel() {
        return model;
    }

}
