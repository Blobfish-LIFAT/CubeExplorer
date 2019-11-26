package com.olap3.cubeexplorer.infocolectors.algos;

import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.concurrent.TimeUnit;

public class BasicStats implements MLModel {
    long runtime = 0;

    @Override
    public ECube process(DataSet ds) {
        Stopwatch runTime = Stopwatch.createStarted();
        ECube out = new ECube("basestats");

        for (String head : ds.getHeader()){
            if (ds.getColDatatype(head).isReal()){
                DescriptiveStatistics stats = new DescriptiveStatistics();
                for (double d : ds.getDoubleColumn(head))
                    stats.addValue(d);
                out.getExplProperties().put("Stats (" + head + ")", stats);
            }
        }

        runtime = runTime.stop().elapsed(TimeUnit.MILLISECONDS);
        return out;
    }

    @Override
    public long aprioriTime() {
        return 5;
    }

    @Override
    public long aposterioriTime() {
        return runtime;
    }
}
