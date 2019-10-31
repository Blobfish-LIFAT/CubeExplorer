package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.evaluate.Evaluator;
import com.olap3.cubeexplorer.optimize.Optimizer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the 'main' class for the algorithm (Actual public void main elswhere ...)
 */
public class AutoCube {
    static final String main = "WITH +(\\w*) +EXPLAIN +([\\w, ]*) +FOR +([^;]+) +USING +((\\w+\\([\\w,]*\\))(,\\w+\\([\\w,]*\\))*);";
    static Pattern mainP = Pattern.compile(main, Pattern.CASE_INSENSITIVE);
    static final String sub = "(\\w*\\([\\w,]*\\))";
    static Pattern subP = Pattern.compile(sub, Pattern.CASE_INSENSITIVE);

    Optimizer optimizer;
    Evaluator evaluator;
    Sequencer sequencer;

    public List<ECube> answer(IntentionalQuery q){

        /*
        List<ExecutionPlan> possiblePlans = optimizer.genPlans(q);

        // TODO choose one maybe ....
        List<ECube> result = evaluator.evaluate(possiblePlans.get(0));

        result = sequencer.order(result, q);


        return result;
        */
        return null;
    }

    /**
     * Primitive parser for the grammar
     * @param q the query
     * @return the corresponding intentional query object
     */
    static public IntentionalQuery parse(String q){
        IntentionalQuery iq = new IntentionalQuery();

        Matcher m1 = mainP.matcher(q);
        if (m1.matches()) {
            // Group 1 is cube
            iq.setCube(m1.group(1));

            // Group 2 is measure(s)
            String[] measures = m1.group(2).split(",");

            // Group 3 is subcube definition


            // Group 4 is algorithms
            System.out.println(m1.group(4));
            String[] subs = m1.group(4).split("\\),");

            System.out.println(Arrays.toString(subs));

        }
        return null;
    }

    public static void main(String[] args) {
        String q = "with sales Explain revenue,sales for countries in{France,Italy}, year=2018 using interpolation(years,location,product),test(years,location,product),toto(years,location,product);";
        parse(q);
    }



}
