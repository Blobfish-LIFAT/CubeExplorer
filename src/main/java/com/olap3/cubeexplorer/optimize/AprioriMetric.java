package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;

public interface AprioriMetric {
    /**
     *
     * @param ic
     * @return the metric value bounded between 0 and 1
     */
    public double rate(InfoCollector ic);
}
