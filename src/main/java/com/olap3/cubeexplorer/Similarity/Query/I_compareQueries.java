/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olap3.cubeexplorer.Similarity.Query;

/**
 *
 * @author Salim IGUE
 */
public interface I_compareQueries {
    double compareTwoQueriesByJaccard();
    double compareTwoQueriesByJaccardAndStructureThresholdWithSeveralSelectionPerLevel();
}
