package com.olap3.cubeexplorer.evaluate;


import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.xmlutil.XMLPlan;
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


    public SQLEstimateEngine() {

            con = MondrianConfig.getJdbcConnection();



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
            Statement planON = con.createStatement();
            planON.execute("SET SHOWPLAN_XML ON");


            ResultSet rs = planON.executeQuery(query);

            XMLPlan plan = null;
            if (rs.next()) {
                String xml_plan = rs.getString(1);
                System.out.println(xml_plan);
                plan = PlanParser.xml_to_plan(xml_plan);
            }

            planON.close();


            return plan;
        } catch (SQLException | DocumentException e){
            e.printStackTrace();
        }
        return new XMLPlan(-1, -1, -1);
    }
}
