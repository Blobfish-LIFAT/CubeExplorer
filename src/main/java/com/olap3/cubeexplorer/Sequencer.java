package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.model.ECube;

import java.util.List;

public interface Sequencer {
    public List<ECube> order(List<ECube> results, IntentionalQuery q);
}
