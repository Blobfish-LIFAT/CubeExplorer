package com.olap3.cubeexplorer.optimize;

import com.olap3.cubeexplorer.info.InfoCollector;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
