package com.olap3.cubeexplorer.optimize.time;


import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.optimize.time.xmlutil.PlanParser;
import com.olap3.cubeexplorer.optimize.time.xmlutil.XMLPlan;
import org.dom4j.DocumentException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Holds a parallel JDBC connection to mondrian on the DBMS hosting the cube to allow
 * running classic SQL queries notably EXPLAIN for time estimations
 */
public class SQLEstimateEngine {

    static SQLEstimateEngine def = null;
    static SQLFactory queryFactory = null;
    public static Stopwatch timer =Stopwatch.createUnstarted();

    Connection con;
    private int timeout = 500;


    public SQLEstimateEngine() {
        con = MondrianConfig.getNewJdbcConnection();
        try {
            Statement planON = con.createStatement();
            planON.execute("SET SHOWPLAN_XML ON");
            planON.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SQLEstimateEngine(Connection con) {
        this.con = con;
    }

    public static long estimateQfset(Qfset qfset) {
        if (def == null) {
            def = new SQLEstimateEngine();
            queryFactory = new SQLFactory(CubeUtils.getDefault());
        }

        //FIXME Not sure about this ratio see with ben
        double rawTime = def.estimates(queryFactory.getStarJoin(qfset)).total_cost * 1000;
        return Math.round(rawTime);
    }

    public boolean setTimeout(int timeout) {
        if (timeout > 0) {
            this.timeout = timeout;
            return true;
        }
        else {
            return false;
        }
    }

    public XMLPlan estimates(String query){
        try {
            Statement st = con.createStatement();
            //st.execute("SET SHOWPLAN_XML ON");

            ResultSet rs = st.executeQuery(query);

            XMLPlan plan = null;
            if (rs.next()) {
                String xml_plan = rs.getString(1);
                //System.out.println(xml_plan);

                plan = PlanParser.xml_to_plan(xml_plan);

            }
            st.close();

            return plan;
        } catch (SQLException | DocumentException e){
            System.err.printf("Offending query : [%s]%n", query);
            e.printStackTrace();
        }
        return new XMLPlan(-1, -1, -1, -1);
    }
}
