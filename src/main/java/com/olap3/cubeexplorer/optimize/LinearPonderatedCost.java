package com.olap3.cubeexplorer.optimize;

import java.util.ArrayList;
import java.util.List;

public class LinearPonderatedCost implements CostModel{
    List<Double> factors;

    public LinearPonderatedCost() {
        factors = new ArrayList<>();
    }

    @Override
    public void updateCost(TimeableOp operation) {
        double factor = ((double)operation.aposterioriTime())/operation.aprioriTime();
        factors.add(factor);
    }

    @Override
    public long correctedEstimate(TimeableOp operation) {
        if (factors.size() == 0)
            return operation.aprioriTime();
        else {
            double factor = factors.stream().mapToDouble(Double::doubleValue).sum()/factors.size();
            return Math.round(factor * operation.aprioriTime());
        }
    }
}
