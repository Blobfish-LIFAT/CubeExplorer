package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.IntentionalQuery;

import java.util.List;

/**
 * The interface for the ordering algorithm of ECube mainly TSP
 */
public interface Sequencer {
    public List<ECube> order(List<ECube> results, IntentionalQuery q);
}
