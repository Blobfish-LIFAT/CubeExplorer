package com.olap3.cubeexplorer.evaluate;

import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.model.ECube;

import java.util.List;

/**
 * Handle actual query eval (jdbc, mondrian ...) and running ML libs here
 */
interface Evaluator {
    boolean setup(ExecutionPlan p, Stopwatch globalRuntime, boolean reoptEnable);
    List<ECube> evaluate();
    EXPRunStats getStats();

}
