package com.olap3.cubeexplorer.data;

import com.olap3.cubeexplorer.im_olap.model.QueryPart;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.julien.SelectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Query;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Compatibility {
    public static Set<QueryPart> partsFromQfset(Qfset q){
        Set<QueryPart> parts = new HashSet<>();

        // Handle projections
        parts.addAll(q.getAttributes().stream().map(Compatibility::projectionToDimensionQP).collect(Collectors.toSet()));
        // Handle measures
        //System.out.println("Measures: " + q.getMeasures());
        parts.addAll(q.getMeasures().stream().map(Compatibility::measureToMeasureQP).collect(Collectors.toSet()));
        // Handle selections
        parts.addAll(q.getSelectionPredicates().stream().map(Compatibility::selectionToFilterQP).collect(Collectors.toSet()));

        return parts;
    }

    public static Qfset QfsetFromMDX(String q){
        mondrian.olap.Connection cnx = MondrianConfig.getMondrianConnection();
        Query query = cnx.parseQuery(q);
        return new Qfset(query);
    }

    public static QueryPart projectionToDimensionQP(ProjectionFragment pf){
        var dim = pf.toString();//.replace("].[", "");
        //dim = dim.substring(1, dim.length()-1);
        //System.out.println(dim);
        return QueryPart.newDimension(dim);
    }

    public static QueryPart selectionToFilterQP(SelectionFragment sf){
        var member = sf.getValue().toString(); //TODO unify format
        //System.out.println(member);
        return QueryPart.newFilter(member);
    }

    public static QueryPart measureToMeasureQP(MeasureFragment mf){
        var measure = mf.getAttribute().toString(); //TODO unify format
        return QueryPart.newMeasure(measure);
    }

    //TODO finih this
    public static Qfset QPsToQfset(com.olap3.cubeexplorer.im_olap.model.Query query, CubeUtils utils) {
        var proj = new HashSet<ProjectionFragment>();
        var sel = new HashSet<SelectionFragment>();
        var meas = new HashSet<MeasureFragment>();



        return new Qfset(proj, sel, meas);
    }
}
