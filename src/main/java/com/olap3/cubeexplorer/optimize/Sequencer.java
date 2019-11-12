package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.IntentionalQuery;

import java.util.List;

public interface Sequencer {
    public List<ECube> order(List<ECube> results, IntentionalQuery q);
}
