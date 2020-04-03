package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;

/**
 * This is the interface for any interestingness metric computed for ICs,
 * this MUST work on the unevaluated state of the IC !
 */
public interface AprioriMetric {
    /**
     *
     * @param ic
     * @return the metric value bounded between 0 and 1
     */
    public double rate(InfoCollector ic);
}
