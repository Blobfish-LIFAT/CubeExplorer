package com.olap3.cubeexplorer.info;

import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.olap.CellSet;
import mondrian.olap.Query;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MDXAccessor extends DataAccessor {
    DataSet cached = null;

    static Connection connection;
    static OlapWrapper wrapper;
    static OlapConnection olapConnection;


    public MDXAccessor(Qfset query) {
        this.internal = query;
    }

    //TODO finish query
    @Override
    public DataSet execute() {
        if (cached != null)
            return cached;

        init();

        try {
            OlapStatement statement = olapConnection.createStatement();
            String mdxQuery = internal.toMDXString();
            CellSet cellSet = new CellSet(statement.executeOlapQuery(mdxQuery));

        } catch (OlapException e) {
            e.printStackTrace();
        }
        return cached;
    }

    static void init(){
        if (connection == null){
            try {
                connection = DriverManager.getConnection(MondrianConfig.getURL());
                wrapper = (OlapWrapper) connection;
                olapConnection = wrapper.unwrap(OlapConnection.class);
            } catch (SQLException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public int aprioriTuples() {
        return 0;
    }

    @Override
    public int aposterioriTuples() {
        return 0;
    }

    @Override
    public int aprioriTime() {
        return 0;
    }

    @Override
    public int aposterioriTime() {
        return 0;
    }

    @Override
    public double aprioriInterest() {
        return 0;
    }

    @Override
    public double aposterioriInterest() {
        return 0;
    }
}
