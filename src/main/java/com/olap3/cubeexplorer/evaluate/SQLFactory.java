package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.exctractionmethod.ExtractionMethod;
import com.olap3.cubeexplorer.exctractionmethod.SqlQuery;
import com.olap3.cubeexplorer.julien.MeasureFragment;
import com.olap3.cubeexplorer.julien.ProjectionFragment;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SQLFactory {
    CubeUtils cube;

    public SQLFactory(CubeUtils cube) {
        this.cube = cube;
    }
/*
    public String getStarJoin(Qfset formalQuery){
        //TODO: God method, super long.
        //CONSIDER: refactoring to smaller parts, but do it without errors
        //ATTN: ordering of the steps is sensitive, do NOT change unless you know what you are doing.
        List<MeasureFragment> measure = new ArrayList(formalQuery.getMeasures());
        String aggregateFunction;
        ArrayList<String[]> gammaExpressions;
        ArrayList<String[]> sigmaExpressions;
        ExtractionMethod extractionMethod;

        aggregateFunction = getAggregateFunction(formalQuery, cube); //
        gammaExpressions = cubeQuery.getGammaExpressions(); // Projections ? will use projection fragments instead
        sigmaExpressions = cubeQuery.getSigmaExpressions(); // Selections ?
        extractionMethod = new SqlQuery(); // I just new()'ed it should work ?

        if(measure.get(0).getAttribute() !=null )
            extractionMethod.addReturnedFields(aggregateFunction,measure.get(0).getAttribute().getName()); // Not clear what's an attribute for me
        else
            extractionMethod.addReturnedFields(aggregateFunction,"");

        HashSet<String> FromTables=new HashSet<String>();

        //Create WhereClausse
        for(String[] sigmaExpr: sigmaExpressions){
            for(int i = 0;i < referCube.getListDimension().size(); i++) { // For each dimension in the schema ? I can do that with mondrian
                Dimension dimension = referCube.getListDimension().get(i);
                String[] tmp=sigmaExpr[0].split("\\.");
                if(dimension.hasSameName(tmp[0].trim())){
                    //FOR JOIN WITH Basic CUBE
                    String toaddJoin[]=new String[3];
                    toaddJoin[0] = referCube.getDimensionRefField().get(i);
                    toaddJoin[1] = "=";
                    toaddJoin[2] = dimension.getTableName()+"."+((LinearHierarchy)dimension.getHier().get(0)).getLevels().get(0).getAttributeName(0);
                    extractionMethod.addFilter(toaddJoin);

                    FromTables.add(dimension.getTableName());

                    //Add the Sigma Expression
                    ArrayList<Hierarchy> current_hierachy=dimension.getHier();
                    String toaddSigma[]=new String[3];
                    toaddSigma[0]=dimension.getTableName()+".";

                    for(int k=0;k<current_hierachy.size();k++){//for each hierarchy of dimension
                        List<Level> current_lvls=current_hierachy.get(k).getLevels();
                        for(int l=0;l<current_lvls.size();l++){
                            if(current_lvls.get(l).getName().equals(tmp[1].trim())){
                                toaddSigma[0]+=current_lvls.get(l).getAttributeName(0);
                            }
                        }
                    }
                    toaddSigma[1]=sigmaExpr[1];
                    toaddSigma[2]=sigmaExpr[2];
                    extractionMethod.addFilter(toaddSigma);
                }
            }
        } //end for of WhereClasue

        //Create From clause
        String[] tbl_tmp = new String[1];
        tbl_tmp[0] = "";
        if(cube != null)
            tbl_tmp[0] = "mobpro_ind_1";//TODO get fact table name
        extractionMethod.addSourceCube(tbl_tmp);

        for(int i=0;i<FromTables.size();i++){
            String[] toAdd=new String[1];
            toAdd[0]=(String) FromTables.toArray()[i];
            extractionMethod.addSourceCube(toAdd);
        }

        //Create groupClausse
        for(String[] gammaExpr: gammaExpressions){
            if(gammaExpr[0].length()==0) {
                String[] toadd=new String[1];
                toadd[0]=gammaExpr[1];
                extractionMethod.addGroupers(toadd);
            }
            else{
                for(int i=0;i<referCube.getListDimension().size();i++){
                    Dimension dimension= referCube.getListDimension().get(i);
                    if(dimension.hasSameName(gammaExpr[0])){
                        String[] toadd=new String[1];
                        toadd[0]=dimension.getTableName()+".";
                        ArrayList<Hierarchy> current_hierachy=dimension.getHier();
                        for(int k=0;k<current_hierachy.size();k++){//for each hierarchy of dimension
                            List<Level> current_lvls=current_hierachy.get(k).getLevels();
                            for(int l=0;l<current_lvls.size();l++){
                                if(current_lvls.get(l).getName().equals(gammaExpr[1])){
                                    // FOR JOIN WITH Basic CUBE
                                    String toaddJoin[]=new String[3];
                                    toaddJoin[0]=referCube.getDimensionRefField().get(i);
                                    toaddJoin[1]="=";
                                    toaddJoin[2]=dimension.getTableName()+"."+((LinearHierarchy)dimension.getHier().get(0)).getLevels().get(0).getAttributeName(0);
                                    extractionMethod.addFilter(toaddJoin);
                                    String[] toAddfrom=new String[1];
                                    toAddfrom[0]=dimension.getTableName();
                                    if(FromTables.contains(dimension.getTableName())==false)
                                        extractionMethod.addSourceCube(toAddfrom);

                                    toadd[0]+=current_lvls.get(l).getAttributeName(0);
                                }
                            }
                        }

                        extractionMethod.addGroupers(toadd);
                    }
                }
            }
        }

        return extractionMethod.toString();
    }
    */
}
