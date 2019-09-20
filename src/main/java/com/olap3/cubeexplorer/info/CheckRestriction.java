package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.ECube;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.julien.SelectionFragment;
import mondrian.olap.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is basically a drill down operation
 */
public class CheckRestriction extends InfoCollector {
    public CheckRestriction(DataAccessor da, MLModel ml) {
        dataSource = da;
        model = ml;
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

    public static List<CheckRestriction> build(Set<ProjectionFragment> dims, Set<MeasureFragment> measures, Set<SelectionFragment> selections){
        List<CheckRestriction> possibleICs = new ArrayList<>();

        for (var sf : selections){
            Level target = sf.getLevel().getChildLevel();
            var tmp = new HashSet<>(dims);
            tmp.add(new ProjectionFragment(target));

            Qfset q = new Qfset(tmp, new HashSet<>(), new HashSet<>(measures));
            possibleICs.add(new CheckRestriction(new MDXAccessor(q), null));//TODO missing ml stuff
        }

        return possibleICs;
    }
}
