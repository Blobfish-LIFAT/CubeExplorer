package com.olap3.cubeexplorer.model;

/**
 * An intentional query over a cube represented by it's starting point q0
 */
public class IntentionalQuery {
    Qfset start;
    String cube;

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
