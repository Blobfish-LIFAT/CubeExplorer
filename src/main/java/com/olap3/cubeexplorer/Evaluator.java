package com.olap3.cubeexplorer;

import java.util.List;

/**
 * Handle actual query eval (jdbc, mondrian ...) and running ML libs here
 */
public abstract class Evaluator {
    public abstract List<ECube> evaluate(Plan p);
}
