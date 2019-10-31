package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.model.*;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.server.Statement;

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
        String testQuery = "SELECT NON EMPTY {Hierarchize({{[Measures].[Distance trajet domicile - travail (moyenne)], [Measures].[Nombre total d\u0027individus], [Measures].[Distance trajet domicile - travail (max)]}})} ON COLUMNS,\nNON EMPTY Hierarchize(Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Salariés].[Salariés en formation].[En contrat d\u0027apprentissage]}), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Salariés].[Salariés en formation].[Stagiaires rémunérés en entreprise]}), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Salariés].[Salariés non précaires].[Emplois sans limite de durée, CDI, titulaire de la fonction publique]}), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Salariés].[Salariés précaires].[Autres emplois à durée limitée, CDD, contrat court, vacataire...]}), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Salariés].[Salariés précaires].[Emplois-jeunes, CES, contrats de qualification]}), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Salariés].[Salariés précaires].[Placés par une agence d\u0027intérim]}), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Statut inconnu].[Statut inconnu].[Statut  inconnu]}), CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, {[Statut d\u0027emploi.STATEMPL_Hierarchie].[Sans objet].[Sans objet].[Sans objet]}))))))))) ON ROWS\nFROM [Cube1MobProInd]";

        mondrian.olap.QueryPart qp = olap.parseStatement(testQuery);

        System.out.println(qp);

        mondrian.olap.Query olapQuery = olap.parseQuery(testQuery);

        System.out.println(olapQuery);
        olapQuery.explain(new PrintWriter(System.out));


        System.out.println(Arrays.toString(olapQuery.getFormulas()));

        Statement st = olapQuery.getStatement();

        System.out.println(st);


        var res = olap.execute(olapQuery);



        Qfset test = new Qfset();
        test.add(ProjectionFragment.newInstance(utils.getLevel("Commune de residence", "Commune de residence")));
        test.add(MeasureFragment.newInstance(utils.getMeasure("Duree trajet domicile - travail (moyenne)")));
        test.add(MeasureFragment.newInstance(utils.getMeasure("Nombre total d'individus")));
        test.add(SelectionFragment.newInstance(utils.fetchMember("Sexe", "Sexe", "Hommes")));
        test.add(SelectionFragment.newInstance(utils.fetchMember("Sexe", "Sexe", "Femmes")));

        System.out.println("\n--- BEGIN Star Join test ---");

        SQLFactory factory = new SQLFactory(utils);
        String SQLquery = factory.getStarJoin(test);
        System.out.println(SQLquery);

        System.out.println("--- Begin conversion test ---");
        System.out.println(testQuery);
        Qfset converted = new Qfset(olapQuery);
        System.out.println(converted);
        MdxToTripletConverter converter = new MdxToTripletConverter(utils);
        converter.extractMeasures(testQuery, converted);
        System.out.println(converted);
    }

}
