/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olap3.cubeexplorer.Similarity.Query;


import com.olap3.cubeexplorer.Similarity.QueryComparisonType;
import com.olap3.cubeexplorer.model.Qfset;

/**
 *
 * @author Julien
 */
public class QueryComparison {

    protected Qfset query1;
    protected Qfset query2;
    //Weight for the group by set
    protected double alpha;
    //Weight for the selection
    protected double beta;
    //Weight for the measure
    protected double gamma;
    

    public QueryComparison(Qfset query1, Qfset query2, double alpha, double beta, double gamma) {
        this.query1 = query1;
        this.query2 = query2;
        this.alpha=alpha;
        this.beta=beta;
        this.gamma=gamma;
        
    }

    public Qfset getQuery1() {
        return query1;
    }

    public void Qfset(Qfset query1) {
        this.query1 = query1;
    }

    public Qfset getQuery2() {
        return query2;
    }

    public void setQuery2(Qfset query2) {
        this.query2 = query2;
    }
    
    public Similarity computeSimilarity() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot computeSimilarity in the superclass");};

    public QueryComparison QueryComparisonFactory(QueryComparisonType queryComparisonType)
    {
        switch(queryComparisonType)
        {
            case BY_JACCARD_STRUCTURE:
                return new QueryCompJaccardStructureThresWithSeveralSelectionPerLevel(query1, query2, alpha, beta, gamma);
            default:
                return new QueryCompJaccardStructureThresWithSeveralSelectionPerLevel(query1, query2, alpha, beta, gamma);
        }
    }
}
