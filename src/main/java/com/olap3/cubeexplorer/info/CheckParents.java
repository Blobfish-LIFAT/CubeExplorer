package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.QueryPart;
import com.olap3.cubeexplorer.julien.Fragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CheckParents extends InfoCollector {
    public CheckParents(Set<Fragment> dims, Set<Fragment> measures, Set<Fragment> selections) {
        super(dims, measures, selections);

        // We will check all hierarchies for possible parents
        List<ProjectionFragment> parents = new ArrayList<>();
        for (Fragment f : selections){
            ProjectionFragment p = (ProjectionFragment) f;
            parents.add(ProjectionFragment.newInstance(p.getLevel().getParentLevel()));
        }

        //TODO generate data accessors from the parents we found

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
