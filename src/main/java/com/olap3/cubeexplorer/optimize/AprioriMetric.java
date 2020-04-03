package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;

/**
 * This is the interface for any interestingness metric computed for ICs,
 * this MUST work on the unevaluated state of the IC !
 */
public interface AprioriMetric {
    /**
     * Rates the IC with the metric
     * @param ic the IC to rate according to the metric
     * @return the metric value bounded between 0 and 1
     */
    public double rate(InfoCollector ic);
}
