package com.olap3.cubeexplorer.infocolectors.algos;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.ArrayList;
import java.util.List;

public class Correlation implements MLModel {
    String ref;
    List<String> others;


    public Correlation(String refMeasure, List<String> otherMeasures){
        ref = refMeasure;
        others = otherMeasures;
    }

    @Override
    public ECube process(DataSet ds) {
        double[][] data = new double[1+others.size()][];
        data[0] = ds.getDoubleColumn(ref);

        for (int i = 0; i < others.size(); i++) {
            data[i + 1] = ds.getDoubleColumn(others.get(i));
        }

        RealMatrix res = new PearsonsCorrelation().computeCorrelationMatrix(data);
        var cube = new ECube("correlation");
        List<String> header = new ArrayList<>();
        header.add(ref);
        header.addAll(others);
        cube.getExplProperties().put("corr_header", header);
        cube.getExplProperties().put("corr_matrix", res);

        return cube;
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
