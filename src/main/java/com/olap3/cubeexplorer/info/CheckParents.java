package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.julien.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckParents extends InfoCollector {

    public CheckParents(DataAccessor da, MLModel model) {
        this.dataSource = da;
        this.model = model;
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

    public static List<CheckParents> build(Set<ProjectionFragment> dims, Set<MeasureFragment> measures, Set<SelectionFragment> selections){
        List<CheckParents> possibleICs = new ArrayList<>();

        // We will check all hierarchies for possible parents
        for (ProjectionFragment f : dims){
            ProjectionFragment p  = ProjectionFragment.newInstance(f.getLevel().getParentLevel());

            HashSet<ProjectionFragment> tmp = new HashSet<>(dims);
            tmp.remove(f);
            tmp.add(p);

            Qfset query = new Qfset(tmp, new HashSet<>(selections), new HashSet<>(measures));
            MDXAccessor src = new MDXAccessor(query);

            CheckParents cp = new CheckParents(src, null);//TODO generate ML models

            possibleICs.add(cp);
        }


        return possibleICs;

    }
}
