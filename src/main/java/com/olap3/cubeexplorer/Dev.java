package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.evaluate.MDXtoSQLvisitor;
import com.olap3.cubeexplorer.evaluate.SQLEstimateEngine;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.julien.SelectionFragment;
import com.olap3.cubeexplorer.mdxparser.MDXExpLexer;
import com.olap3.cubeexplorer.mdxparser.MDXExpParser;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Level;
import mondrian.olap.Result;
import mondrian.rolap.sql.*;
import mondrian.server.Execution;
import mondrian.server.Statement;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Class for test code and such
 * Class model for this is on google drive (link in the emails)
 */
public class Dev {
    public static void main(String[] args) {

        // Init connection to our test cube
        Connection olap = MondrianConfig.getMondrianConnection();
        CubeUtils utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
/*
        var line = "([Measures].[Nombre total d'individus]*[Measures].[Duree trajet domicile - travail (total)])";
        CharStream lineStream = CharStreams.fromString(line);
        Lexer lexer = new MDXExpLexer(lineStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MDXExpParser parser = new MDXExpParser(tokens);
        ParseTree tree = parser.start();

        MDXtoSQLvisitor calculator = new MDXtoSQLvisitor(utils);

        String sqlExp = calculator.visit(tree);
        System.out.println("Final expresion: " + sqlExp);
*/
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

        var res = olap.execute(olapQuery);



        Qfset test = new Qfset();
        test.add(new ProjectionFragment(utils.getLevel("Commune de residence", "Commune de residence")));
        test.add(new MeasureFragment(utils.getMeasure("Duree trajet domicile - travail (moyenne)")));
        test.add(new MeasureFragment(utils.getMeasure("Nombre total d'individus")));
        test.add(new SelectionFragment(utils.fetchMember("Sexe", "Sexe", "Hommes")));
        test.add(new SelectionFragment(utils.fetchMember("Sexe", "Sexe", "Femmes")));

        System.out.println("\n--- BEGIN Star Join test ---");
        SQLFactory factory = new SQLFactory(utils);
        String SQLquery = factory.getStarJoin(test);
        System.out.println(SQLquery);

        SQLEstimateEngine see = new SQLEstimateEngine();
        var plan = see.estimates(SQLquery);

        System.out.println(plan);
    }

}
