package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.IntentionalQuery;
import com.olap3.cubeexplorer.evaluate.ExecutionPlan;

import java.util.List;

public interface PlanGenerator {
    public List<ExecutionPlan> produce(IntentionalQuery q);
}
