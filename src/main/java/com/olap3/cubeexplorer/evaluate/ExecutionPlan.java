package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.info.InfoCollector;

import java.util.List;
import java.util.Set;

public class ExecutionPlan {
    List<InfoCollector> operations;


    public List<InfoCollector> getOperations() {
        return operations;
    }

    public void setOperations(List<InfoCollector> operations) {
        this.operations = operations;
    }
}
