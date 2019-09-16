package com.olap3.cubeexplorer.evaluate;

import java.sql.Connection;

/**
 * Holds a parallel JDBC connection to mondrian on the DBMS hosting the cube to allow
 * running classic SQL queries notably EXPLAIN for time estimations
 */
public class SQLEngine {
    Connection con;
    //TODO Singleton stuff

}
