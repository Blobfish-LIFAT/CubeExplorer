package com.olap3.cubeexplorer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olap3.cubeexplorer.castor.response.CastorJsonResponse;
import com.olap3.cubeexplorer.castor.response.CastorTable;
import com.olap3.cubeexplorer.castor.session.QueryRequest;
import com.olap3.cubeexplorer.castor.session.Session;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.olap.CellSet;
import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FrontEndFormatter {
    public String buildJson(List<ECube> inputSession){
        return "{}";//TODO
    }

    public String buildJson(Session session) throws SQLException{
        Connection connection = DriverManager.getConnection(MondrianConfig.getURL());
        OlapWrapper wrapper = (OlapWrapper) connection;
        OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
        OlapStatement statement = olapConnection.createStatement();


        CastorJsonResponse response = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Gson gson = new GsonBuilder().setDateFormat(dateFormat.toPattern()).setPrettyPrinting().serializeNulls().create();

            List<CastorTable> castorTableList = new ArrayList<>();


            CellSet oldCellset = null;
            for (QueryRequest request : session.getQueries()) {
                CellSet cellSet = new CellSet(statement.executeOlapQuery(request.getQuery()));
                CastorTable castorTable = new CastorTable(request.getQuery(), cellSet.getData(), cellSet.getHeaderTree(Axis.ROWS.axisOrdinal()), cellSet.getHeaderTree(Axis.COLUMNS.axisOrdinal()), null, null, null);

                castorTableList.add(castorTable);
                oldCellset = cellSet;
            }
            response = new CastorJsonResponse(castorTableList);

            return gson.toJson(response);

    }
}
