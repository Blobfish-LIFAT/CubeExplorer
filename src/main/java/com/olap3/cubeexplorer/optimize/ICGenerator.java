package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.IntentionalQuery;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;

import java.util.List;

public interface ICGenerator {
    public List<InfoCollector> produce(IntentionalQuery q);
}
