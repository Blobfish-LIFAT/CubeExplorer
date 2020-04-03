package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the interface for the interestingness vs runtime part of the TAP heuristics
 */
public interface BudgetManager {
    /**
     * Time budget is an integer in milliseconds (simpler to solve)
     * @param candidates candidates ICs that could be run
     * @param timeBudget the budget in milliseconds
     * @return an execution plan compliant with the budget
     */
    public ExecutionPlan findBestPlan(List<InfoCollector> candidates, int timeBudget);



    /**
     * Given an existing solution and a search space of info collectors find all equivalent solution by swapping 'identical'
     * items in the solution.
     * 'identical' is defined using time estimation and the metric, two ic are considered equal if they have equal time and metric value
     * @param searchSpace .
     * @param  solution .
     * @param metric .
     * @return  .
     */
    static Set<Set<InfoCollector>> expandToEqSolutions(List<InfoCollector> searchSpace, Set<InfoCollector> solution, AprioriMetric metric){

        searchSpace.sort(Comparator.comparingDouble(metric::rate));
        Map<Double, List<InfoCollector>> groups = searchSpace.stream().collect(Collectors.groupingBy(metric::rate));

        //TODO look for swapping candidates within groups ...

        return null;
    }
}
