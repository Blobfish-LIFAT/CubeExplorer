package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.info.InfoCollector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KnapsackManager implements BudgetManager {
    AprioriMetric metric;
    // resolution for discretization
    int res = 65536;
    double epsilon = 0.25; // bound on divergence from optimal value

    public KnapsackManager(AprioriMetric value) {
        metric = value;
    }

    @Override
    public ExecutionPlan findBestPlan(List<InfoCollector> candidates, int timeBudget) {
        int n = candidates.size();
        int value[] = new int[n];
        int weight[] = new int[n];
        int[] valueApprox = new int[n];

        int i1 = 0;
        for (InfoCollector ic : candidates) {
            value[i1] = (int) Math.round(metric.rate(ic) * res); //discretization might need something smart
            weight[i1] = ic.estimatedTime();
        }

        // Source for the FPTAS algorithm https://github.com/hzxie/Algorithm
        // Rounding
        int maxValue = 0;
        double k = n / epsilon;
        for ( int i = 0; i < n; ++ i ) {
            if ( value[i] > maxValue ) {
                maxValue = value[i];
            }
        }
        int totalValue = 0;
        for ( int i = 0; i < n; ++ i ) {
            valueApprox[i] = (int)Math.floor(value[i] * k / maxValue);
            totalValue += valueApprox[i];
        }

        // Calculate Approximation Solution
        boolean[] isPickedApprox = getKnapsackSolution(weight, value, timeBudget, totalValue);

        HashSet<InfoCollector> solution = new HashSet<>();
        for (int i = 0; i < n; i++) {
            if (isPickedApprox[i])
                solution.add(candidates.get(i));
        }

        var result = new ExecutionPlan();
        result.setOperations(new ArrayList<>(solution));//FIXME order ....
        return result;
    }


    public boolean[] getKnapsackSolution(
            int[] weight, int[] value, int capcity, int totalValue) {
        int n = weight.length;
        int[][] cost = new int[n][totalValue + 1];

        for ( int i = 1; i <= totalValue; ++ i ) {
            int currentWeight = weight[n - 1],
                    currentValue = value[n - 1];
            cost[n - 1][i] = i == currentValue ?
                    currentWeight : INFINITY;
        }
        for ( int i = n - 2; i >= 0; -- i ) {
            int currentWeight = weight[i],
                    currentValue = value[i];
            for ( int j = 0; j <= totalValue; ++ j ) {
                cost[i][j] = j < currentValue ? cost[i + 1][j] :
                        Math.min(cost[i + 1][j],
                                cost[i + 1][j - currentValue] + currentWeight);
            }
        }
        return getPickedItem(cost, weight, value, capcity, totalValue);
    }

    public boolean[] getPickedItem(int[][] cost, int[] weight,
                                   int[] value, int capcity, int totalValue) {
        int n = cost.length;
        int j = totalValue;
        boolean[] isPicked = new boolean[n];

        while ( cost[0][j] > capcity ) {
            -- j;
        }
        for ( int i = 0; i < n - 1; ++ i ) {
            if ( cost[i][j] != cost[i + 1][j] ) {
                isPicked[i] = true;
                j -= value[i];
            }
        }
        isPicked[n - 1] = cost[n - 1][j] == weight[n - 1];
        return isPicked;
    }

    private static final int INFINITY = Integer.MAX_VALUE / 2;
}
