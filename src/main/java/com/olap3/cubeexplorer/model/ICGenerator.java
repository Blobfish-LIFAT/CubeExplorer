package com.olap3.cubeexplorer.model;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;

import java.util.List;

public interface ICGenerator {
    public List<InfoCollector> produce(IntentionalQuery q);
}
