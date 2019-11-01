package com.olap3.cubeexplorer.optimize;

import com.alexscode.utilities.math.IntRangeNormalizer;
import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.evaluate.ExecutionPlan;
import com.olap3.cubeexplorer.infocolectors.InfoCollector;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class KnapsackManager implements BudgetManager {
    Logger LOGGER = Logger.getLogger(KnapsackManager.class.getName());
    AprioriMetric metric;
    // resolution for discretization
    static int res = 8192;
    private double epsilon = 0.05; // bound on divergence from optimal value

    public KnapsackManager(AprioriMetric value, double epsilon) {
        metric = value;
        this.epsilon = epsilon;
    }

    @Override
    public ExecutionPlan findBestPlan(List<InfoCollector> candidates, int timeBudget) {
        int n = candidates.size();
        if (n ==0)
            throw new IllegalArgumentException("Cannot find best Knapsack solution for ZERO possible items !");

        int[] value;
        double[] rawValue = new double[n];
        int[] weight = new int[n];
        int[] valueApprox = new int[n];

        Stopwatch initTime = Stopwatch.createStarted();
        for (int i = 0; i < candidates.size(); i++) {
            rawValue[i] =metric.rate(candidates.get(i));
            weight[i] = Math.toIntExact(candidates.get(i).estimatedTime());
        }

        //LOGGER.info("KS init. took " + initTime.stop());
        IntRangeNormalizer norm = new IntRangeNormalizer(1, res, rawValue);
        value = norm.normalized();

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
        //System.out.println(totalValue);

        // Calculate Approximation Solution
        boolean[] isPickedApprox = getKnapsackSolution(weight, valueApprox, timeBudget, totalValue);

        HashSet<InfoCollector> solution = new HashSet<>();
        for (int i = 0; i < n; i++) {
            if (isPickedApprox[i])
                solution.add(candidates.get(i));
        }

        var result = new ExecutionPlan(solution);
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

    public boolean[] getKnapsackSolution(
            int[] weight, int[] value, int capcity) {
        int n = weight.length;
        int[][] cost = new int[n][capcity + 1];

        for ( int i = 0; i <= capcity; ++ i ) {
            int currentWeight = weight[n - 1],
                    currentValue = value[n - 1];
            cost[n - 1][i] = i < currentWeight ? 0 : currentValue;
        }
        for ( int i = n - 2; i >= 0; -- i ) {
            int currentWeight = weight[i],
                    currentValue = value[i];
            for ( int j = 0; j <= capcity; ++ j ) {
                cost[i][j] = j < currentWeight ? cost[i + 1][j] :
                        Math.max(cost[i + 1][j],
                                cost[i + 1][j - currentWeight] + currentValue);
            }
        }
        return getPickedItem(cost, weight, capcity);
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

    public boolean[] getPickedItem(int[][] cost, int[] weight, int capcity) {
        int n = cost.length;
        int j = capcity;
        boolean[] isPicked = new boolean[n];

        for ( int i = 0; i < n - 1; ++ i ) {
            if ( cost[i][j] != cost[i + 1][j] ) {
                isPicked[i] = true;
                j -= weight[i];
            }
        }
        isPicked[n - 1] = cost[n - 1][j] != 0;
        return isPicked;
    }

    private static final int INFINITY = Integer.MAX_VALUE / 2;
}
