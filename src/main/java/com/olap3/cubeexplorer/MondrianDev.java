package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.data.CellSet;
import com.olap3.cubeexplorer.data.HeaderTree;
import com.olap3.cubeexplorer.model.Compatibility;
import com.olap3.cubeexplorer.model.columnStore.DataSet;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import java.sql.DriverManager;
import java.util.List;

import static com.olap3.cubeexplorer.data.HeaderTree.getLevelDescriptors;

public class MondrianDev {
    public static void main(String[] args) throws Exception{
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null)
            System.exit(1); //Crash the app can't do anything w/o mondrian
        CubeUtils utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
        MondrianConfig.setMondrianConnection(olap);

        String query = "SELECT NON EMPTY {Hierarchize({{[Measures].[Distance trajet domicile - travail (max)], [Measures].[Distance trajet domicile - travail (min)], [Measures].[Distance trajet domicile - travail (moyenne)]}})} ON COLUMNS, NON EMPTY Hierarchize(Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays des Châteaux].[AGGLOPOLYS]}, CrossJoin({[Statut d'emploi.STATEMPL_Hierarchie].[Salariés]}, [Sexe.Sexe_Hierarchie].[Sexe].Members)), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[PAYS INCONNU].[CC INCONNUE]}, CrossJoin({[Statut d'emploi.STATEMPL_Hierarchie].[Salariés]}, [Sexe.Sexe_Hierarchie].[Sexe].Members)), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays Vendômois].[COLLINES DU PERCHE]}, CrossJoin({[Statut d'emploi.STATEMPL_Hierarchie].[Salariés]}, [Sexe.Sexe_Hierarchie].[Sexe].Members)), Union(CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays Vendômois].[PAYS DE VENDÔME]}, CrossJoin({[Statut d'emploi.STATEMPL_Hierarchie].[Salariés]}, [Sexe.Sexe_Hierarchie].[Sexe].Members)), CrossJoin({[Commune de residence.CNERES_Hierarchie_intercommunale].[CENTRE].[Pays Vendômois].[VALLÉES LOIR ET BRAYE]}, CrossJoin({[Statut d'emploi.STATEMPL_Hierarchie].[Salariés]}, [Sexe.Sexe_Hierarchie].[Sexe].Members))))))) ON ROWS FROM [Cube1MobProInd]";
        String query2 = "SELECT NON EMPTY {Hierarchize({[Mode de transport.MODTRANS_Hierarchie].[Categorie].Members})} ON COLUMNS, NON EMPTY {Hierarchize({{[Measures].[Distance trajet domicile - travail (max)], [Measures].[Distance trajet domicile - travail (min)]}})} ON ROWS FROM [Cube1MobProInd]";

        java.sql.Connection connection = DriverManager.getConnection(MondrianConfig.getURL());
        OlapWrapper wrapper = (OlapWrapper) connection;
        OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
        OlapStatement statement = olapConnection.createStatement();

        CellSet cs = new CellSet(statement.executeOlapQuery(query2));

        Double[][] data = cs.getData();
        String[] descriptors = new String[cs.getNbOfColumns() + cs.getNbOfRows()];
        List<HeaderTree> rows = HeaderTree.getLeaves(cs.getHeaderTree(1));
        List<HeaderTree> cols = HeaderTree.getLeaves(cs.getHeaderTree(0));

        System.out.println(rows);
        System.out.println(getLevelDescriptors(rows.get(0)));
        System.out.println(cols);
        System.out.println(getLevelDescriptors(cols.get(0)));
/*
        for (int i = 0; i < data.length; i++) {
            System.out.print(getCrossTabPos(rows.get(i)) + " ");
            for (int j = 0; j < data[i].length; j++) {
                double d = data[i][j];
                System.out.print(d + " ");
            }
            System.out.println();
        }
*/
        DataSet ds = Compatibility.cellSetToDataSet(cs, true);

        System.out.println(ds);


    }



}
