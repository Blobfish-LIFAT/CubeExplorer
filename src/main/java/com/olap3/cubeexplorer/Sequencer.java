package com.olap3.cubeexplorer;

import java.util.List;

public interface Sequencer {
    public List<ECube> order(List<ECube> results, IntentionalQuery q);
}
