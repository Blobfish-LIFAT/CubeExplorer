package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;

import java.util.List;

public class ExecutionPlan {
    List<InfoCollector> operations;


    public List<InfoCollector> getOperations() {
        return operations;
    }

    public void setOperations(List<InfoCollector> operations) {
        this.operations = operations;
    }
}
