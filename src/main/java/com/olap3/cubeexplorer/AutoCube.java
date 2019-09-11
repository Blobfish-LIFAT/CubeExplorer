package com.olap3.cubeexplorer;

import java.util.List;

/**
 * This is the interface the main class shall respect
 */
public interface AutoCube {
    public List<ECube> answer(IntentionalQuery q);
}
