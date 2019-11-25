package com.olap3.cubeexplorer.model.columnStore;

import lombok.Getter;

public abstract class Predicate {
    @Getter
    String col;
    abstract boolean[] getBinaryVector(Object input);


}
