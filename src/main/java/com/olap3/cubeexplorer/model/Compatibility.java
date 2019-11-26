package com.olap3.cubeexplorer.model;

import com.alexscode.utilities.Future;
import com.alexscode.utilities.collection.Pair;
import com.olap3.cubeexplorer.data.CellSet;
import com.olap3.cubeexplorer.data.HeaderTree;
import com.olap3.cubeexplorer.data.castor.session.CrSession;
import com.olap3.cubeexplorer.data.castor.session.QueryRequest;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.model.columnStore.Datatype;
import com.olap3.cubeexplorer.model.columnStore.Predicate;
import com.olap3.cubeexplorer.model.columnStore.StringEqPredicate;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.Query;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.olap3.cubeexplorer.data.HeaderTree.getCrossTabPos;
import static com.olap3.cubeexplorer.data.HeaderTree.getLevelDescriptors;

/**
 * @author Alex
 * Various conversion functions to go between data formats for queries and parts
 */
public class Compatibility {

    static class SelectElement {
        public static final int PROJ = 0, SEL = 1;
        int type;
        Level l;
        List<Member> m;

        public SelectElement(int type, Level l, List<Member> m) {
            this.type = type;
            this.l = l;
            this.m = m;
        }

        public String getRepr(){
            if (type == PROJ){
                return l.getUniqueName() + ".MEMBERS";
            }
            else if (type == SEL){
                return "{" +
                        Future.join(m.stream().map(Member::getUniqueName).collect(Collectors.toList()), ",")
                        + "}";
            }
            return "! error Compatibility.java triplet format !";
        }
    }

    /**
     * Creates a basic MDX query to represent the query triplet
     * @param q the input triplet
     * @return the MDX query as a String
     */
    public static String QfsetToMDX(Qfset q){
        final String lsep = System.lineSeparator();

        List<Member> columns = new ArrayList<>();
        List<SelectElement> onRows = new ArrayList<>();

        // put measures on columns
        for(MeasureFragment m_tmp : q.getMeasures()) {
            columns.add(m_tmp.getAttribute());
        }

        // Handle selection predicates
        // Members from same hierarchy need to be grouped and put in a set {}
        Map<Hierarchy, List<SelectionFragment>> selections = q.getSelectionPredicates().stream().collect(Collectors.groupingBy(sf -> sf.getLevel().getHierarchy()));
        selections.forEach((key, val) -> {
            List<Member> members = val.stream().map(SelectionFragment::getValue).collect(Collectors.toList());
            onRows.add(new SelectElement(SelectElement.SEL, null, members));
        });

        // put all other hierarchies in rows (only non All levels) not described by selection predicates
        Set<Level> banned = q.getSelectionPredicates().stream().map(SelectionFragment::getLevel).collect(Collectors.toSet());
        for(ProjectionFragment pf : q.getAttributes()) {
            if(!pf.getLevel().isAll() && !banned.contains(pf.getLevel())) {
                onRows.add(new SelectElement(SelectElement.PROJ, pf.getLevel(), null));
            }
        }

        StringBuilder mdx  = new StringBuilder();

        // SELECT CLAUSE
        mdx.append("SELECT NON EMPTY ");
        mdx.append(lsep);

        // ON COLUMNS
        mdx.append("{");
        for(int i = 0; i < columns.size(); i++) {
            mdx.append(columns.get(i).getUniqueName());
            if(i +1 < columns.size()) mdx.append(", ");
        }
        mdx.append("}");
        mdx.append(" ON COLUMNS,");
        mdx.append(lsep);
        mdx.append("NON EMPTY ");

        // ON ROWS
        switch (onRows.size()) {
            case 1 -> {
                mdx.append("{").append(onRows.iterator().next().getRepr()).append("} ON ROWS");
                mdx.append(lsep);
            }
            default -> {
                mdx.append("{");
                String rowsText = "";
                for (int i = 0; i < onRows.size(); i++) {
                    SelectElement el = onRows.get(i);
                    if (i == 0)
                        rowsText = el.getRepr();
                    else
                        rowsText = "CROSSJOIN({" + el.getRepr() + "}, " + rowsText + ")";
                }
                mdx.append(rowsText);
                mdx.append("} ON ROWS");
                mdx.append(lsep);
            }
        }

        // FROM CLAUSE
        String cubeName = CubeUtils.getDefault().getCube().getName();//FIXME can't we use references from a fragment ?
        mdx.append("FROM [").append(cubeName).append("]");

        //System.out.println(mdx);
        return mdx.toString();
    }

    public static DataSet cellSetToDataSet(CellSet cs, boolean splitMeasures) {
        if (!splitMeasures) return cellSetToDataSet(cs);
        DataSet original = cellSetToDataSet(cs);

        // --- Sorta group by by hand ---
        List<Pair<String, Datatype>> descriptors = original.getDescriptors();
        Pair<String, Datatype> mLevel = descriptors.stream().filter(p -> p.getLeft().equals("MeasuresLevel")).findFirst().get();
        Pair<String, Datatype> mVal = descriptors.stream().filter(p -> p.getLeft().equals("MeasureValue")).findFirst().get();
        descriptors.remove(mLevel);
        descriptors.remove(mVal);

        //get measures
        List<String> newMeasures = Stream.of(original.getStringColumn(mLevel.getLeft())).distinct().collect(Collectors.toList());

        // Add a column for each measure
        descriptors.addAll(newMeasures.stream().map(s -> new Pair<>(s, Datatype.REAL)).collect(Collectors.toList()));
        DataSet output = new DataSet(descriptors, original.getNumberOfRows() / newMeasures.size());

        HashSet<String> parsedCoordinates = new HashSet<>();

        int mlInd = original.getColIndex("MeasuresLevel");
        int mvInd = original.getNumberOfColumns() - 1; // MeasureValue column should always be last (by construction)
        String[] originalCols = original.getHeader();
        for (int i = 0; i < original.getNumberOfRows(); i++) {
            Object[] line = original.getLine(i);
            String gbKey = "";
            for (int j = 0; j < line.length; j++) {
                if (j != mlInd && j != mvInd){
                    gbKey += line[j];
                }
            }
            if (parsedCoordinates.contains(gbKey))
                continue;

            parsedCoordinates.add(gbKey);
            List<Predicate> gbMask = new ArrayList<>();
            for (int j = 0; j < line.length; j++) {
                if (j != mlInd && j != mvInd) {
                    gbMask.add(new StringEqPredicate(originalCols[j], (String) line[j]));
                }
            }
            List<Object[]> toGroup = original.selectConjunctive(gbMask);
            Object[] newLine = new Object[originalCols.length - 2 + newMeasures.size()];
            int index = 0;
            for (int j = 0; j < line.length; j++) {
                if (j != mlInd && j != mvInd){
                    newLine[index++] = line[j];
                }
            }
            for (String measure : newMeasures){
                for (int j = 0; j < toGroup.size(); j++) {
                    Object[] oldLine = toGroup.get(j);
                    if (oldLine[mlInd].equals(measure))
                        newLine[index++] = oldLine[mvInd];
                }
            }
            //System.out.println(Arrays.toString(output.getHeader()));
            //System.out.println(Arrays.toString(newLine));
            output.pushLine(newLine);
        }

        return output;
    }

    /**
     * Extrapolates columns descriptors from the header trees of the cell set
     * @param cs The cell set
     * @return first part of the header trees
     */
    private static List<Pair<String, Datatype>> getDescriptors(CellSet cs){
        List<String> descriptors = new ArrayList<>(cs.getNbOfColumns() + cs.getNbOfRows());
        descriptors.addAll(getLevelDescriptors(HeaderTree.getLeaves(cs.getHeaderTree(1)).get(0)));
        descriptors.addAll(getLevelDescriptors(HeaderTree.getLeaves(cs.getHeaderTree(0)).get(0)));
        return descriptors.stream().map(d -> new Pair<>(d, Datatype.STRING)).collect(Collectors.toList());
    }

    private static DataSet cellSetToDataSet(CellSet cs){
        Double[][] data = cs.getData();
        List<HeaderTree> rows = HeaderTree.getLeaves(cs.getHeaderTree(1));
        List<HeaderTree> cols = HeaderTree.getLeaves(cs.getHeaderTree(0));

        var colDescription = getDescriptors(cs);
        colDescription.add(new Pair<>("MeasureValue", Datatype.REAL));
        DataSet ds = new DataSet(colDescription, cs.getNbOfCells());

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                List<String> coords = getCrossTabPos(rows.get(i));
                coords.addAll(getCrossTabPos(cols.get(j)));
                Object[] line = new Object[coords.size()+1];
                System.arraycopy(coords.toArray(), 0, line, 0, coords.size());
                line[coords.size()] = data[i][j];
                ds.pushLine(line);
            }
        }

        return ds;
    }

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
