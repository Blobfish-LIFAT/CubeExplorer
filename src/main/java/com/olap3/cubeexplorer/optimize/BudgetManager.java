package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.info.InfoCollector;

import java.util.*;
import java.util.stream.Collectors;

public interface BudgetManager {
    /**
     * Time budget is an integer in milliseconds (simpler to solve)
     * @param candidates
     * @param timeBudget
     * @return
     */
    public ExecutionPlan findBestPlan(List<InfoCollector> candidates, int timeBudget);



    /**
     * Given an existing solution and a search space of info collectors find all equivalent solution by swapping 'identical'
     * items in the solution.
     * 'identical' is defined using time estimation and the metric, two ic are considered equal if they have equal time and metric value
     * @param solution
     * @param metric
     * @return
     */
    static Set<Set<InfoCollector>> expandToEqSolutions(List<InfoCollector> serchSpace, Set<InfoCollector> solution, AprioriMetric metric){

        serchSpace.sort(Comparator.comparingDouble(metric::rate));
        Map<Double, List<InfoCollector>> groups = serchSpace.stream().collect(Collectors.groupingBy(metric::rate));

        //TODO look for swapping candidates within groups ...

        return null;
    }
}
