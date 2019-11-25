package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import mondrian.olap.OlapElement;

import java.util.stream.Collectors;

public class ICCorrelate extends InfoCollector{
    String measure;

    public ICCorrelate(DataAccessor source, String measure) {
        this.dataSource = source;
        this.measure = measure;
    }

    //TODO test code finish class
    @Override
    public ECube executeInternal() {
        return new Correlation(dataSource, measure, CubeUtils.getDefault().getMeasures().stream().map(OlapElement::getName).collect(Collectors.toList())).process();
    }
}
