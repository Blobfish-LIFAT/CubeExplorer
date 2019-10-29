package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.julien.Qfset;


public abstract class InfoCollector {
    Qfset q0;
    DataAccessor dataSource;
    MLModel model;
    long time_estimate = -1;
    //For debugging only
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
    public abstract ECube execute();

    public Qfset getQ0() {
        return q0;
    }

    public void setQ0(Qfset q0) {
        this.q0 = q0;
    }

    public DataAccessor getDataSource() {
        return dataSource;
    }

    public MLModel getModel() {
        return model;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
