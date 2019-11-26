package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class ExecutionPlan implements Iterator<InfoCollector>{
    @Getter
    Set<InfoCollector> executed;
    @Getter
    List<InfoCollector> operations;
    @Getter @Setter
    long predictedRuntime;

    public ExecutionPlan(Collection<InfoCollector> base){
        operations = new ArrayList<>(base);
        operations.sort(Comparator.comparing(InfoCollector::estimatedTime));
        executed = new HashSet<>();
    }

    public ExecutionPlan(Collection<InfoCollector> base, boolean sortedByTime){
        operations = new ArrayList<>(base);
        if (sortedByTime)
            operations.sort(Comparator.comparing(InfoCollector::estimatedTime));
        executed = new HashSet<>();
    }

    public Set<InfoCollector> getLeft() {
        return new HashSet<>(operations);
    }

    public void setExecuted(InfoCollector op) {
        this.operations.remove(op);
        this.executed.add(op);
    }

    public void add(InfoCollector ic){
        operations.add(ic);
    }

    public void addAll(Collection<InfoCollector> ics){
        operations.addAll(ics);
    }

    public void remove(InfoCollector ic){
        operations.remove(ic);
    }

    public void removeAll(Collection<InfoCollector> ics){
        operations.removeAll(ics);
    }

    @Override
    public boolean hasNext() {
        return operations.size()>0;
    }

    @Override
    public InfoCollector next() {
        Iterator<InfoCollector> nativeIt = operations.iterator();
        InfoCollector ic = nativeIt.next();
        nativeIt.remove();
        return ic;
    }
}

