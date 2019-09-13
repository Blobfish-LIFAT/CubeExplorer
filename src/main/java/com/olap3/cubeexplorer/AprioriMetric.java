package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.info.InfoCollector;

public interface AprioriMetric {
    /**
     *
     * @param ic
     * @return the metric value bounded between 0 and 1
     */
    public double rate(InfoCollector ic);
}
