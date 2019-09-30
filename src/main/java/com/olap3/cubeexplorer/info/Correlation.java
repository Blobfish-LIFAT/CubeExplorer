package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;

import java.util.ArrayList;
import java.util.List;

public class Correlation implements MLModel {
    DataAccessor da;
    String ref;
    List<String> others;


    public Correlation(DataAccessor source, String refMeasure, List<String> otherMeasures){
        da = source;
        ref = refMeasure;
        others = otherMeasures;
    }

    @Override
    public ECube process() {
        return null;
    }

    @Override
    public void setDataSource(DataAccessor da) {
        this.da = da;
    }

    private DataAccessor getDataSource() {
        if (da != null)
            return da;
        else
            throw new IllegalStateException("[MLModel] expected data source before operation ! No data source set !");
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
    public int aprioriTuples() {
        return 0;
    }

    @Override
    public int aposterioriTuples() {
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
