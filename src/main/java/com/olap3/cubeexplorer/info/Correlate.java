package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.julien.Fragment;

import java.util.Set;

public class Correlate extends InfoCollector{
    public Correlate(Set<Fragment> dims, Set<Fragment> measures, Set<Fragment> selections) {
        super(dims, measures, selections);


    }

    @Override
    public int timeEstimate() {
        return 0;
    }

    @Override
    public int realTime() {
        return 0;
    }

    @Override
    public ECube execute() {
        return null;
    }
}
