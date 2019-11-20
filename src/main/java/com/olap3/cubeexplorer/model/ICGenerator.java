package com.olap3.cubeexplorer.model;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;

import java.util.Set;

public interface ICGenerator {
    public Set<InfoCollector> produce(IntentionalQuery q);
}
