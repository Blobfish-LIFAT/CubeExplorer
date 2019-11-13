package com.olap3.cubeexplorer.optimize.time;

import com.olap3.cubeexplorer.evaluate.QueryStats;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public class LinearTimeEstimatorDOPAN implements CostModel {
    static SQLFactory queryFactory = null;
    static double ascii_len_c = 8e-05,
            projNb_c= 3e-03,
            selNb_c = 0,
            tableNb_c = 5e-03
            , aggNb_c = 4.5e-03;
    static double intercept = 0.01;

    public static long estimateQfsetMs(Qfset qfset){
        if (queryFactory == null) {
            queryFactory = new SQLFactory(CubeUtils.getDefault());
        }
        String q = queryFactory.getStarJoin(qfset);
        QueryStats qs = queryFactory.getLastStarStats();
        qfset.setStats(qs);
        qfset.setSql(q);

        double estRaw = q.length() * ascii_len_c + qs.getProjNb() * projNb_c + qs.getSelNb() * selNb_c + qs.getTableNb() * tableNb_c + qs.getAggNb() * aggNb_c ;
        estRaw = estRaw + intercept;
        //System.out.println(estRaw);
        return Math.round(estRaw*1000);
    }

    @Override
    public long estimateCost(TimeableOp operation) {
        if (operation instanceof MDXAccessor){
            return estimateQfsetMs(((MDXAccessor)operation).getInternal());
        } else
            throw new UnsupportedOperationException();
    }
}
