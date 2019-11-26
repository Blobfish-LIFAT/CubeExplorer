package com.olap3.cubeexplorer.infocolectors.builders;

import com.olap3.cubeexplorer.infocolectors.ICCorrelate;
import com.olap3.cubeexplorer.model.IntentionalQuery;

import java.util.Set;

public class CorrType implements ICType<ICCorrelate> {
    @Override
    public Set<ICCorrelate> produce(IntentionalQuery q) {
        return null; //TODO
    }
}
