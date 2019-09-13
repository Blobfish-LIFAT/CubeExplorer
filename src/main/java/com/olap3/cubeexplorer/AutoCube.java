package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.optimize.Optimizer;

import java.util.List;

/**
 * This is the 'main' class for the algorithm (Actual public void main elswhere ...)
 */
public class AutoCube {
    Optimizer optimizer;
    Evaluator evaluator;
    Sequencer sequencer;

    public List<ECube> answer(IntentionalQuery q){
        List<Plan> possiblePlans = optimizer.genPlans(q);

        // TODO choose one maybe ....
        List<ECube> result = evaluator.evaluate(possiblePlans.get(0));

        result = sequencer.order(result, q);


        return result;
    }



}
