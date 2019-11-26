package com.olap3.cubeexplorer.infocolectors.builders;

import com.olap3.cubeexplorer.infocolectors.ICRestriction;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.model.IntentionalQuery;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import mondrian.olap.Level;

import java.util.HashSet;
import java.util.Set;

public class RestrictionType implements ICType<ICRestriction> {

    @Override
    public Set<ICRestriction> produce(IntentionalQuery iq) {
        Set<ICRestriction> possibleICs = new HashSet<>();
        Qfset q = iq.getStart();
        for (var sf : q.getAttributes()){
            Level target = sf.getLevel().getChildLevel();
            var tmp = new HashSet<>(q.getAttributes());
            tmp.add(ProjectionFragment.newInstance(target));

            Qfset req = new Qfset(tmp, new HashSet<>(), new HashSet<>(q.getMeasures()));
            var da = new MDXAccessor(req);

                possibleICs.add(new ICRestriction(da, null));//TODO add models

        }

        return possibleICs;
    }
}
