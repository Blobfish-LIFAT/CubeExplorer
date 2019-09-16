package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Result;
import mondrian.rolap.sql.*;
import mondrian.server.Execution;
import mondrian.server.Statement;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Class for test code and such
 * Class model for this is on google drive (link in the emails)
 */
public class Dev {
    public static void main(String[] args) {
        System.out.println("I compiled !");
        Connection olap = MondrianConfig.getMondrianConnection();

        String testQuery = "SELECT\n" +
                "NON EMPTY {Hierarchize({[Type de logement.TYPLOGT_Hierarchie].[Type regroupe].Members})} ON COLUMNS,\n" +
                "NON EMPTY {Hierarchize({[Measures].[Nombre total d'individus]})} ON ROWS\n" +
                "FROM [Cube1MobProInd]";

        mondrian.olap.QueryPart qp = olap.parseStatement(testQuery);

        System.out.println(qp);

        mondrian.olap.Query olapQuery = olap.parseQuery(testQuery);

        System.out.println(olapQuery);
        olapQuery.explain(new PrintWriter(System.out));


        System.out.println(Arrays.toString(olapQuery.getFormulas()));

        Statement st = olapQuery.getStatement();

        System.out.println(st);

        Execution exec = new Execution(st, 1000);

        //System.out.println(exec.);

        Result res = olap.execute(olapQuery);

    }
}
