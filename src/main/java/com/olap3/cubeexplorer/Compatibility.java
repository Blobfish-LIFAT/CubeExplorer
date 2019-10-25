package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.im_olap.model.QueryPart;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Query;

import java.util.HashSet;
import java.util.Set;

public class Compatibility {
    public static Set<QueryPart> partsFromQfset(Qfset q){
        Set<QueryPart> parts = new HashSet<>();

        // Handle projections
        for (var pf : q.getAttributes()){
            parts.add(new QueryPart(QueryPart.Type.DIMENSION, pf.toString()));//TODO check if string is the proper one
            System.out.println(projectionToDimension(pf));
        }

        // Handle measures
        for (var mf : q.getMeasures()){
            parts.add(new QueryPart(QueryPart.Type.MEASURE, mf.toString()));//TODO check if string is the proper one
        }

        //Handle Selection predicates
        for (var sf : q.getSelectionPredicates()){
            parts.add(new QueryPart(QueryPart.Type.MEASURE, sf.getLevel().toString()));//TODO check if string is the proper one
        }

        return parts;
    }

    public static Qfset QfsetFromMDX(String q){
        mondrian.olap.Connection cnx = MondrianConfig.getMondrianConnection();
        Query query = cnx.parseQuery(q);
        return new Qfset(query);
    }

    public static QueryPart projectionToDimension(ProjectionFragment pf){
        var dim = pf.toString().replace("].[", "");
        dim = dim.substring(1, dim.length()-1);
        return new QueryPart(QueryPart.Type.DIMENSION, dim);
    }
}
