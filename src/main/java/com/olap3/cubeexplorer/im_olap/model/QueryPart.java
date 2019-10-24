package com.olap3.cubeexplorer.im_olap.model;

import java.util.HashMap;
import java.util.Optional;

public class QueryPart implements Comparable<QueryPart> {
    public enum Type {
        DIMENSION, FILTER, MEASURE
    }

    static final HashMap<Type, String> display = new HashMap<>();

    static {
        display.put(Type.DIMENSION, "Dimension");
        display.put(Type.FILTER, "Filter");
        display.put(Type.MEASURE, "Measure");
    }

    Type t;
    String value;

    public QueryPart(Type t, String value) {
        this.t = t;
        this.value = value;
    }

    public Optional<String> getHierarchy(){
        if (t == Type.MEASURE)
            return Optional.empty();
        return Optional.of(value.split("\\.")[0]);
    }

    public boolean isFilter(){
        return t == Type.FILTER;
    }

    @Override
    public String toString() {
        return "QueryPart{" +
                "" + display.get(t) +
                ", '" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;
        return this.t == ((QueryPart)obj).t && ((QueryPart)obj).value.equals(this.value);
    }

    @Override
    public int compareTo(QueryPart o) {
        if (this.equals(o))
            return 0;
        else
            return this.value.compareTo(o.value);
    }

    @Override
    public int hashCode() {
        return t.hashCode() * value.hashCode();
    }
}
