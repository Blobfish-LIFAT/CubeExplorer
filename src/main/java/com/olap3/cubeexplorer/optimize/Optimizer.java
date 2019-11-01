package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.optimize.stats.StatBasedMetric;

import java.util.List;

public class Optimizer {
    public static Optimizer opt;
    BudgetManager manager;

    static {
        opt = new Optimizer(new KnapsackManager(new StatBasedMetric(), 0.1));
    }

    private Optimizer(BudgetManager manager) {
        this.manager = manager;
    }

    public ExecutionPlan getOptimalPlan(List<InfoCollector> ics, int timeBudget){
        return manager.findBestPlan(ics, timeBudget);
    }


}
