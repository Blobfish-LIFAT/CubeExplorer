package com.olap3.cubeexplorer.infocolectors.builders;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.model.IntentionalQuery;

import java.util.Set;

public interface ICType<T extends InfoCollector> {
    Set<T> produce(IntentionalQuery q);
}
