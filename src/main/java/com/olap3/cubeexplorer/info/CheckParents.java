package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.julien.*;

import javax.management.loading.MLet;
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

    public static List<CheckParents> build(Qfset q, List<String> models){
        List<CheckParents> possibleICs = new ArrayList<>();

        // We will check all hierarchies for possible parents
        for (ProjectionFragment f : q.getAttributes()){
            ProjectionFragment p  = ProjectionFragment.newInstance(f.getLevel().getParentLevel());

            HashSet<ProjectionFragment> tmp = new HashSet<>(q.getAttributes());
            tmp.remove(f);
            tmp.add(p);

            Qfset query = new Qfset(tmp, new HashSet<>(q.getSelectionPredicates()), new HashSet<>(q.getMeasures()));
            MDXAccessor src = new MDXAccessor(query);

            for (String alg : models) {
                var ml = MLModelFactory.newInstance(alg);
                CheckParents cp = new CheckParents(src, ml);
                possibleICs.add(cp);
            }
        }


        return possibleICs;

    }
}
