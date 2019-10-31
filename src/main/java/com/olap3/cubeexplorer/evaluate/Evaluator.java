package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.model.ECube;

import java.util.List;

/**
 * Handle actual query eval (jdbc, mondrian ...) and running ML libs here
 */
public abstract class Evaluator {
    public abstract List<ECube> evaluate(ExecutionPlan p);
}
