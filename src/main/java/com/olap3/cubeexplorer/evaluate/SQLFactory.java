package com.olap3.cubeexplorer.evaluate;

import com.alexscode.utilities.Reflect;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.julien.SelectionFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.MondrianDef;
import mondrian.rolap.RolapCalculatedMember;
import mondrian.rolap.RolapCubeDimension;
import mondrian.rolap.RolapMeasure;
import mondrian.rolap.RolapStoredMeasure;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SQLFactory {
    CubeUtils cube;

    public SQLFactory(CubeUtils cube) {
        this.cube = cube;
    }


    /**
     * Builds a star join SQL query using the mondrian 3 schema of a cube, this as the name implies will only work on star schemas
     * The method roughly follows this algorithm:
     *
     *   For each selection/projection in Query:
     *     Add join clause for matching dimension and fact table
     *   For each selection:
     *     Add where clause where dim_table.level = "my predicate"
     *   For each projection:
     *     Add group by clause : grouping by dim_table.level
     *     Add select clause: dim_table.level
     *   For each measure:
     *     Add select clause: aggregation(fact_table.measure)
     *
     * @param formalQuery A query expressed in a high level abstraction (ie Query parts)
     * @return the SQL string ready to be executed by a DBMS (Only tested on MSSQL for now)
     */
    public String getStarJoin(Qfset formalQuery){
        List<String> joins = new ArrayList<>();
        List<String> groupBys = new ArrayList<>();
        List<String> selects = new ArrayList<>();
        List<String> whereDis = new ArrayList<>();
        HashSet<String> tables = new HashSet<>();

        String query = "";
        String factTable = cube.getFactTableName();
        tables.add(factTable);


        // For each dimension (projection fragment) of the query
        for (ProjectionFragment pf : formalQuery.getAttributes()){
            String dimTable = cube.getTableName(pf.getHierarchy());
            tables.add(dimTable);

            String join = factTable + "." + cube.getForeignKey(pf.getHierarchy().getDimension()) + "=" + dimTable + "." + cube.getPrimaryKey(pf.getHierarchy());
            joins.add(join);

            String gb = dimTable + "." + cube.getColumn(pf.getLevel());
            groupBys.add(gb);
            selects.add(gb);
        }


        //For each measure
        for (MeasureFragment mf : formalQuery.getMeasures()){
            selects.add(buildAggregate(mf));
        }

        /*
            Selection handling
            This is the 'tricky' part
         */
        Map<Level, List<SelectionFragment>> selectionGroups = formalQuery.getSelectionPredicates().stream().collect(Collectors.groupingBy(SelectionFragment::getLevel));
        for (Map.Entry<Level, List<SelectionFragment>> entry : selectionGroups.entrySet()){
            List<String> temp = new ArrayList<>();

            //Add the table to the FROM list
            String table = cube.getTableName(entry.getKey().getHierarchy());
            tables.add(table);

            String col = cube.getColumn(entry.getKey());

            // Add join condition on the fact table
            String join = factTable + "." + cube.getForeignKey(entry.getKey().getHierarchy().getDimension()) + "=" + table + "." + cube.getPrimaryKey(entry.getKey().getHierarchy());
            joins.add(join);

            for (SelectionFragment sf : entry.getValue()){
                temp.add(table + "." + col + "=" + formatted(sf.getValue()));
            }

            String dis = "(" + join(temp, " OR ") + ")";
            whereDis.add(dis);

        }

        /* Now we build the SQL */

        Set<String> where = new HashSet<>(joins);
        where.addAll(whereDis);

        query = "SELECT " + join(selects, ", ")
                + " FROM  " + join(new ArrayList<>(tables), ", ")
                + " WHERE " + join(new ArrayList<>(where), " AND ")
                + " GROUP BY " + join(groupBys, ", ") + ";";

        return query;
    }

    /**
     * Basic formatting for the members for now only MSSQL and simple stuff (Numbers and Strings)
     * @param value the value to format
     * @return the formatted value (eg: 'WOMEN', 14.4, 34) ready to insert into the SQL
     */
    private static String formatted(Member value) {
        //TODO handle data types other than string/numbers
        System.out.println(Arrays.toString(value.getProperties()));
        Pattern number = Pattern.compile("^[0-9\\.]*$");
        Matcher m = number.matcher(value.getName());
        if (m.matches())
            return value.getName();
        else
            return "'" + value.getName() + "'";
    }

    /**
     * Build an aggregate function for the SELECT statement
     * @param mf a measure fragment
     * @return the aggregate func in this form: SUM(people)
     */
    public static String buildAggregate(MeasureFragment mf){
        String func, inner;
        try {
            RolapStoredMeasure actualMeas = (RolapStoredMeasure) mf.getAttribute();
            func = actualMeas.getAggregator().name.toUpperCase();
            inner = actualMeas.getMondrianDefExpression().getGenericExpression();
        } catch (ClassCastException e){
            RolapCalculatedMember calcMeasure = (RolapCalculatedMember) mf.getAttribute();

            //TODO handle calculated measure (from other measures)
            func = "COUNT";
            inner = "*";
            //This should do the trick as we mainly want the EXPLAIN to give us a time estimate
            //No need for the actual proper results
        }



        return func + "(" + inner + ")";
    }


    /**
     * Utility method to join a list of strings
     * @param strings the strings to join like a list of names
     * @param separator a separator (will not be added at the end) eg ", "
     * @return eg: JP, Alex, PAUL, Pierre
     */
    public static String join(List<String> strings, String separator){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i));
            if (i != strings.size() - 1)
                sb.append(separator);
        }
        return sb.toString();
    }

}
