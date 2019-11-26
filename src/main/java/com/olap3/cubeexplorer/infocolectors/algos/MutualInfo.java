package com.olap3.cubeexplorer.infocolectors.algos;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.columnStore.DataSet;

import java.util.List;

public class MutualInfo implements MLModel {
    String measure;
    List<String> otherMeasure;

    public MutualInfo(String measure, List<String> otherMeasure) {
        this.measure = measure;
        this.otherMeasure = otherMeasure;
    }

    @Override
    public ECube process(DataSet ds) {
        ECube out = new ECube("Mutual Information");
        double[] ref = ds.getDoubleColumn(measure);
        for (String other : otherMeasure){
            double mi = JavaMI.MutualInformation.calculateMutualInformation(ref, ds.getDoubleColumn(other));
            out.getExplProperties().put("MutualInfo("+measure+", "+other+")", mi);
        }
        return out;
    }

    @Override
    public long aprioriTime() {
        return 0;
    }

    @Override
    public long aposterioriTime() {
        return 0;
    }
}
