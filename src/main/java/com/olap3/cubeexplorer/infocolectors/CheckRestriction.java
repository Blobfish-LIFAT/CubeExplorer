package com.olap3.cubeexplorer.infocolectors;

import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import mondrian.olap.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This is basically a drill down operation
 */
public class CheckRestriction extends InfoCollector {
    public CheckRestriction(DataAccessor da, MLModel ml) {
        dataSource = da;
        model = ml;
    }

    @Override
    public ECube execute() {
        return null;
    }

    public static List<CheckRestriction> build(Qfset q, List<String> models){
        List<CheckRestriction> possibleICs = new ArrayList<>();

        for (var sf : q.getAttributes()){
            Level target = sf.getLevel().getChildLevel();
            var tmp = new HashSet<>(q.getAttributes());
            tmp.add(ProjectionFragment.newInstance(target));

            Qfset req = new Qfset(tmp, new HashSet<>(), new HashSet<>(q.getMeasures()));
            var da = new MDXAccessor(req);
            for (String model : models){
                var ml = MLModelFactory.newInstance(model);
                possibleICs.add(new CheckRestriction(da, ml));
            }
        }

        return possibleICs;
    }
}
