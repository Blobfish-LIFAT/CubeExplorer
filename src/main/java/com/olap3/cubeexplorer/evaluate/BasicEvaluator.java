package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.model.ECube;

import java.util.List;

//TODO
public class BasicEvaluator implements Evaluator {
    @Override
    public boolean setup(ExecutionPlan p, boolean reoptEnable) {
        return false;
    }

    @Override
    public List<ECube> evaluate() {
        return null;
    }

    @Override
    public EXPRunStats getStats() {
        return null;
    }
}
