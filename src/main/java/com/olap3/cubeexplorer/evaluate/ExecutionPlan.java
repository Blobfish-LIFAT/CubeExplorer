package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ExecutionPlan implements Iterator<InfoCollector>{
    @Getter
    Set<InfoCollector> executed;
    @Getter
    Set<InfoCollector> operations;

    public ExecutionPlan(Collection<InfoCollector> base){
        operations = new HashSet<>(base);
        executed = new HashSet<>();
    }

    public Set<InfoCollector> getLeft() {
        return operations;
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
