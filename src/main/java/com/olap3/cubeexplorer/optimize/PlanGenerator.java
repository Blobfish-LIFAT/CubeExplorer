package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.IntentionalQuery;
import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.info.InfoCollector;

import java.util.List;

public interface PlanGenerator {
    public List<InfoCollector> produce(IntentionalQuery q);
}
