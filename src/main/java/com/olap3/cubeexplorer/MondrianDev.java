package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.infocolectors.ICCorrelate;
import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.model.Compatibility;
import com.olap3.cubeexplorer.model.ECube;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.Query;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import java.sql.DriverManager;

import static com.olap3.cubeexplorer.dolap.DOLAPFig3.testData;

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

        var sessions = DopanLoader.loadDir(testData);

        Query q = sessions.stream().filter(s -> s.getFilename().equals("2-08.json")).findFirst().get().queries.get(3);
        Qfset qf = Compatibility.QPsToQfset(q, utils);

        ICCorrelate icc = new ICCorrelate(new MDXAccessor(qf),"Nombre de voitures des menages (moyenne)");
        //DataSet ds = Compatibility.cellSetToDataSet(cs, true);
        ECube ec = icc.execute();


        System.out.println(ec.getExplProperties().get("corr_matrix"));


    }



}
