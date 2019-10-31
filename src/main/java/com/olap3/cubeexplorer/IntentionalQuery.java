package com.olap3.cubeexplorer;

import com.alexscode.utilities.collection.Pair;
import com.olap3.cubeexplorer.model.Qfset;

import java.util.List;

public class IntentionalQuery {
    Qfset start;
    String cube;
    List<Pair<String, List<String>>> algorithms;

    public Qfset getStart() {
        return start;
    }

    public void setStart(Qfset start) {
        this.start = start;
    }

    public String getCube() {
        return cube;
    }

    public void setCube(String cube) {
        this.cube = cube;
    }
}
