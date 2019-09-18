package com.olap3.cubeexplorer.evaluate;


import com.alexscode.utilities.collection.Pair;
import com.olap3.cubeexplorer.Plan;
import com.olap3.cubeexplorer.xmlutil.PlanParser;
import org.dom4j.DocumentException;

import java.sql.*;

/**
 * Holds a parallel JDBC connection to mondrian on the DBMS hosting the cube to allow
 * running classic SQL queries notably EXPLAIN for time estimations
 */
public class SQLEstimateEngine {
    Connection con;

    private int timeout = 500;


    public SQLEstimateEngine(String jdbcURL) {

        try {
            con = DriverManager.getConnection(jdbcURL);
            PreparedStatement planON = con.prepareStatement("SET SHOWPLAN_XML ON;");
            planON.execute();
            planON.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

    public Plan estimates(String query) throws SQLException, DocumentException {

        Statement statement = con.createStatement();

        ResultSet rs = statement.executeQuery(query);

        Plan plan = null;

        if (rs.next()) {
            String xml_plan = rs.getString(0);
            plan = PlanParser.xml_to_plan(xml_plan);
        }

        statement.close();

        return plan;
    }
}
