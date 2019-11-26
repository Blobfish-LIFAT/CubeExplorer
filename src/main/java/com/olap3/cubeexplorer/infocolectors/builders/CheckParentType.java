package com.olap3.cubeexplorer.infocolectors.builders;

import com.olap3.cubeexplorer.infocolectors.ICCheckParents;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.model.IntentionalQuery;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;

import java.util.HashSet;
import java.util.Set;

public class CheckParentType implements ICType<ICCheckParents> {
    @Override
    public Set<ICCheckParents> produce(IntentionalQuery iq) {
        Set<ICCheckParents> possibleICs = new HashSet<>();
        Qfset q = iq.getStart();
        // We will check all hierarchies for possible parents
        for (ProjectionFragment f : q.getAttributes()){
            ProjectionFragment p  = ProjectionFragment.newInstance(f.getLevel().getParentLevel());

            HashSet<ProjectionFragment> tmp = new HashSet<>(q.getAttributes());
            tmp.remove(f);
            tmp.add(p);

            Qfset query = new Qfset(tmp, new HashSet<>(q.getSelectionPredicates()), new HashSet<>(q.getMeasures()));
            MDXAccessor src = new MDXAccessor(query);

                possibleICs.add(new ICCheckParents(src, null)); //TODO add models

        }


        return possibleICs;
    }
}
