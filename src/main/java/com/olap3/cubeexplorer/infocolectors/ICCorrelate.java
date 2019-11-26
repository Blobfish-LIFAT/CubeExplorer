package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.infocolectors.algos.Correlation;
import com.olap3.cubeexplorer.model.ECube;

import java.util.List;
import java.util.stream.Collectors;

public class ICCorrelate extends InfoCollector{
    private String measure;

    public ICCorrelate(DataAccessor source, String measure) {
        this.dataSource = source;
        this.measure = measure;
    }

    @Override
    public ECube executeInternal() {
        List<String> others = dataSource.getInternal().getMeasures().stream().filter(m -> !m.getAttribute().getName().equals(measure)).map(m -> m.getAttribute().getName()).collect(Collectors.toList());
        return new Correlation(dataSource, measure, others).process();
    }
}
