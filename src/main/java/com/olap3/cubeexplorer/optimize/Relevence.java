package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import mondrian.olap.Hierarchy;

import java.util.ArrayList;
import java.util.List;

public class Relevence implements AprioriMetric {

    /**
     * Might not use actually
     */
    @Override
    public double rate(InfoCollector ic) {
        Qfset q = ic.getDataSource().getInternal();
        Qfset q0 = ic.getQ0();

        var cube = CubeUtils.getDefault();
        List<Hierarchy> hierarchies = new ArrayList<>(cube.getHierarchies());

        return 0;
    }
}
