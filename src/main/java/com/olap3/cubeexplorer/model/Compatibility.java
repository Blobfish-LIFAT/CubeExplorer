package com.olap3.cubeexplorer.model;

import com.olap3.cubeexplorer.data.castor.session.CrSession;
import com.olap3.cubeexplorer.data.castor.session.QueryRequest;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Alex
 * Various conversion functions to go between data formats for queries and parts
 */
public class Compatibility {

    /**
     * Extracts query parts from a triplet
     * @param q a triplet
     * @return the query with all parts from the triplet
     */
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

    /**
     * Builds a triplet from MDX using default connection and cube (Selections are WIP)
     * This process is destructive ! MDX describes a cross table structure in the query this will be lost
     * as query triplets are closed over the set of cuboids and thus can only express a cuboid structure.
     * @param q the MDX query in text format
     * @return the triplet corresponding the the MDX query
     */
    public static Qfset QfsetFromMDX(String q){
        mondrian.olap.Connection cnx = MondrianConfig.getMondrianConnection();
        return QfsetFromMDX(q, cnx, CubeUtils.getDefault());
    }

    public static Qfset QfsetFromMDX(String q, mondrian.olap.Connection cnx, CubeUtils utils){
        Query query = cnx.parseQuery(q);
        MdxToTripletConverter converter = new MdxToTripletConverter(utils);
        Qfset cv = new Qfset(query);
        converter.extractMeasures(q, cv);
        return cv;
    }

    /**
     * Turns a projection fragment into it's equivalent dimension QP
     * @param pf a Projection fragment to be converted
     * @return a dimension type QP
     */
    public static QueryPart projectionToDimensionQP(ProjectionFragment pf){
        var dim = pf.toString();//.replace("].[", "");
        //dim = dim.substring(1, dim.length()-1);
        //System.out.println(dim);
        return QueryPart.newDimension(dim);
    }

    public static QueryPart selectionToFilterQP(SelectionFragment sf){
        var member = sf.getValue().toString(); //TODO unify format
        //System.out.println(member);
        return QueryPart.newFilter(member, sf.getLevel().getUniqueName());
    }

    public static QueryPart measureToMeasureQP(MeasureFragment mf){
        var measure = mf.getAttribute().getUniqueName(); //TODO unify format
        return QueryPart.newMeasure(measure);
    }

    public static Qfset QPsToQfset(com.olap3.cubeexplorer.model.Query query, CubeUtils utils) {
        var proj = new HashSet<ProjectionFragment>();
        var sel = new HashSet<SelectionFragment>();
        var meas = new HashSet<MeasureFragment>();

        for(QueryPart dim : query.getDimensions()){
            Level l = utils.getLevel(dim.getValue());
            if (l == null)
                System.out.printf("call debugger");
            proj.add(ProjectionFragment.newInstance(l));
        }

        for (QueryPart filter : query.getFilters())
            sel.add(selFragFromFilter(filter, utils));

        for (QueryPart measure : query.getMeasures()){
            Member m = utils.getMeasure(measure.getValue());
            meas.add(MeasureFragment.newInstance(m));
        }

        return new Qfset(proj, sel, meas);
    }

    public static SelectionFragment selFragFromFilter(QueryPart filter, CubeUtils utils){
        //Attempt more efficient conversion
        if (filter.level != null){
            Level l = utils.getLevel(filter.level);
            for (Member candidate : utils.fetchMembers(l)){
                if (candidate.getUniqueName().equals(filter.value)){
                    return SelectionFragment.newInstance(candidate);
                }
            }
        }
        return SelectionFragment.newInstance(utils.getMember(filter.getValue()));
    }

    public static List<Session> convertFromCr(List<CrSession> sessions){
        ArrayList<Session> outSess = new ArrayList<>(sessions.size());

        for (CrSession in : sessions){
            ArrayList<com.olap3.cubeexplorer.model.Query> queries = new ArrayList<>(in.getQueries().size());
            for (QueryRequest qr : in.getQueries()){
                queries.add(new com.olap3.cubeexplorer.model.Query(Compatibility.partsFromQfset(Compatibility.QfsetFromMDX(qr.getQuery()))));
            }
            Session current = new Session(queries, in.getUser().getName(), in.getTitle());
            outSess.add(current);
        }

        return outSess;
    }
}
