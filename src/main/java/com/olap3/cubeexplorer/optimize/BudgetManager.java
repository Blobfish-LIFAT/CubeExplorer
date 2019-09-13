package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.info.InfoCollector;

import java.util.List;
import java.util.Set;

public interface BudgetManager {
    /**
     * Time budget is an integer in milliseconds (simpler to solve)
     * @param candidates
     * @param timeBudget
     * @return
     */
    public Set<InfoCollector> findPossibleSet(List<InfoCollector> candidates, int timeBudget);
}
