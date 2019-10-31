package com.olap3.cubeexplorer.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olap3.cubeexplorer.data.castor.response.CastorJsonResponse;
import com.olap3.cubeexplorer.data.castor.response.CastorTable;
import com.olap3.cubeexplorer.data.castor.session.QueryRequest;
import com.olap3.cubeexplorer.data.castor.session.CrSession;
import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.parser.TokenMgrError;
import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FrontEndFormatter {
    public static String buildJson(List<ECube> inputSession){
        return "{}";//TODO
    }

    public static String buildJson(CrSession session) throws SQLException{
        Connection connection = DriverManager.getConnection(MondrianConfig.getURL());
        OlapWrapper wrapper = (OlapWrapper) connection;
        OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
        OlapStatement statement = olapConnection.createStatement();


        CastorJsonResponse response = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Gson gson = new GsonBuilder().setDateFormat(dateFormat.toPattern()).setPrettyPrinting().serializeNulls().create();

            List<CastorTable> castorTableList = new ArrayList<>();


            for (QueryRequest request : session.getQueries()) {
                CellSet cellSet = new CellSet(statement.executeOlapQuery(request.getQuery()));
                CastorTable castorTable = new CastorTable(request.getQuery(), cellSet.getData(), cellSet.getHeaderTree(Axis.ROWS.axisOrdinal()), cellSet.getHeaderTree(Axis.COLUMNS.axisOrdinal()), null, null, null);
                castorTable.setExplanation(request.getComments());
                castorTableList.add(castorTable);
            }
            response = new CastorJsonResponse(castorTableList);

            return gson.toJson(response);

    }

    /**
     * Test code
     */
    public static void main(String[] args) throws Exception{
        List<CrSession> sessions = StudentParser.loadDir("data/studentSessions");
        sessions.sort(Comparator.comparing(session -> session.getTitle()));

        for (int i = 0; i < sessions.size(); i++) {
            try {
                System.out.printf("Parsing file '%s' session is %d queries.%n", sessions.get(i).getTitle(), sessions.get(i).getQueries().size());
                String testJson = buildJson(sessions.get(i));
                Files.write(Paths.get("data/test/K"+(char)(65+i)+"H.json"), testJson.getBytes());
            } catch (Exception e){
                System.err.printf("Error in file '%s' : %s%n", sessions.get(i).getTitle(), e.getMessage());
            } catch (TokenMgrError err){
                System.err.printf("Error in file '%s' : %s%n", sessions.get(i).getTitle(), err.getMessage());
            }

        }

    }
}
