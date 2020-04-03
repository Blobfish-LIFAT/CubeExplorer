package com.olap3.cubeexplorer.measures;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.model.Compatibility;
import com.olap3.cubeexplorer.model.legacy.QueryPart;
import com.olap3.cubeexplorer.optimize.AprioriMetric;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alexscode.utilities.math.Distribution.log2;

/**
 * Interestingness metric store has to be fed a pre computed map using page rank
 */
public class IMMetric implements AprioriMetric {
    //static Pattern qpF = Pattern.compile("\\.\\[([^\\]]*)\\]$");
    static Pattern qpF = Pattern.compile(".*\\.\\[([^]]*)]");
    Map<QueryPart, Double> interest;

    public IMMetric(Map<QueryPart, Double> interest) {
        this.interest = interest;
    }

    @Override
    public double rate(InfoCollector ic) {
        var qps = Compatibility.partsFromQfset(ic.getDataSource().getInternal());
        double sum = 0;
        for (var qp : qps){
            Double i = interest.get(qp);
            if(i==null) {
                if (qp.isFilter()){
                    Matcher m = qpF.matcher(qp.getValue());
                    if (m.matches())
                        i = interest.get(QueryPart.newFilter(m.group(1)));
                    else
                        i = null;
                    //String[] tmp = qp.getValue().split("]\\.\\[");
                    //String name = tmp[tmp.length-1];
                    //i = interest.get(QueryPart.newFilter(name.substring(0, name.length()-1)));

                    if(i==null){
                        System.err.printf("Warning no IM found for %s %n", qp.toString());
                        return 0;
                    }
                } else {
                    System.err.printf("Warning no IM found for %s %n", qp.toString());
                    return 0;
                }
            }
            else
                sum -= log2(i);
        }
        return sum/qps.size();
    }
}
