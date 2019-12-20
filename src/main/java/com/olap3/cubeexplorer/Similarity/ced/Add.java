package com.olap3.cubeexplorer.Similarity.ced;

import com.olap3.cubeexplorer.model.julien.QuerySession;

public class Add extends Operator{

    @Override
    public QuerySession edit() {
        return null;
    }

    @Override
    public double getCost() {
        //double[] sim_v = new double[session.size()];
        //for (int k = 0; k < sim_v.length; k++) {
        //    sim_v[k] = sim.apply()
        //}
        return 0;
    }
}
