package com.olap3.cubeexplorer.evaluate;


import com.alexscode.utilities.collection.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Holds a parallel JDBC connection to mondrian on the DBMS hosting the cube to allow
 * running classic SQL queries notably EXPLAIN for time estimations
 */
public class SQLEstimateEngine {
    Connection con;


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

    public Pair<Integer, Integer> estimates(String query){
        //TODO


        return new Pair<>(1, 1);
    }
}
