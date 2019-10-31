/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olap3.cubeexplorer.measures;

import com.olap3.cubeexplorer.model.MeasureFragment;
import com.olap3.cubeexplorer.model.ProjectionFragment;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.SelectionFragment;
import mondrian.olap.Hierarchy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Elisa
 */
public class Jaccard {
    Qfset query1, query2;

    private Jaccard(Qfset q1, Qfset q2) {
        query1 = q1;
        query2 = q2;
    }

    public static double similarity(Qfset q1, Qfset q2){
        var jc = new Jaccard(q1, q2);
        return (jc.computeGroupBySimilarity()
                + jc.computeMeasureSimilarity()
                + jc.computeSelPredSimilarity())/3.0d;
    }


    private double computeFragmentSimilarity() {
        double intersection = 0;
        for (ProjectionFragment p1 : query1.getAttributes()) {
            for (ProjectionFragment p2 : query2.getAttributes()) {
                if (p1 == p2) {
                    intersection++;
                }
            }
        }

        for (SelectionFragment s1 : query1.getSelectionPredicates()) {
            for (SelectionFragment s2 : query2.getSelectionPredicates()) {
                if (s1 == s2) {
                    intersection++;

                }
            }
        }

        for (MeasureFragment m1 : query1.getMeasures()) {
            for (MeasureFragment m2 : query2.getMeasures()) {
                if (m1 == m2) {
                    intersection++;
                }
            }
        }
        
        return (intersection/(query1.size()+query2.size()-intersection));
    }

    private double computeSelPredSimilarity() {

        double commonSelection = 0;
        for (ProjectionFragment attribute : this.query1.getAttributes()) {
            Hierarchy h = attribute.getHierarchy();
            SelectionFragment s1 = query1.getSelectionFromHierarchy(h);
            SelectionFragment s2 = query2.getSelectionFromHierarchy(h);


            if (s1 == null && s2 == null) {
                commonSelection++;
            } else if (s1 == null || s2 == null) {
                commonSelection += 0;
            } else if (s1.getLevel().getName().equals(s2.getLevel().getName()) && s1.getValue().getName().equals(s2.getValue().getName())) {
                commonSelection++;
            }
        }

        if (commonSelection != 0) {
            //System.out.println("Selection:"+commonSelection / (2 * this.query1.getAttributes().size() - commonSelection));
            return commonSelection / (double) (2 * this.query1.getAttributes().size() - commonSelection);
        } else {

            double similarity = 0;

            //alpha is used when two levels are identical to avoid that the similarity is the max (=1 but normalized by the lower possible similarity in the jaccard method) wheras the values are not the same
            double alpha1 = ((double) (2 * this.query1.getAttributes().size() - 2) / (double) ((double) (2 * this.query1.getAttributes().size() - 1) * 2));

            //System.out.println("alpha1"+alpha1);

            //System.out.println((2 * this.query1.getAttributes().size() - 2));
            //System.out.println((2 * this.query1.getAttributes().size() - 1) * 2);
            for (ProjectionFragment attribute : this.query1.getAttributes()) {
                Hierarchy h = attribute.getHierarchy();
                SelectionFragment s1 = query1.getSelectionFromHierarchy(h);
                SelectionFragment s2 = query2.getSelectionFromHierarchy(h);
                if ((s1 == null && s2 != null) || (s1 != null && s2 == null)) {
                    similarity += 0;
                } else {
                    similarity += s1.computeSimilarity(s2);
                }
            }

            //System.out.println("1/2n-1: "+1 / (double) (2 * this.query1.getAttributes().size() - 1));
            //System.out.println("without alpha1: "+(similarity / (double) this.query1.getAttributes().size()) * (1 / (double) (2 * this.query1.getAttributes().size() - 1)));
            //System.out.println("Selection: " + (similarity / (double) this.query1.getAttributes().size()) * (1 / (double) (2 * this.query1.getAttributes().size() - 1)) * alpha1);
            return (double) ((similarity / (double) this.query1.getAttributes().size()) * (1 / (double) (2 * this.query1.getAttributes().size() - 1)) * alpha1);
        }
    }

    private double computeMeasureSimilarity() {
        HashSet<MeasureFragment> M1 = query1.getMeasures();
        HashSet<MeasureFragment> M2 = query2.getMeasures();
        if (M1.isEmpty() || M2.isEmpty()) {
            return 0;
        } else {
            return (double) computeCardinalityIntersaction(M1, M2) / computeCardinalityUnion(M1, M2);
        }
    }

    private double computeGroupBySimilarity() {
        //compute the average of the contribute on each dimension
        if (this.query1.getAttributes().isEmpty() || this.query2.getAttributes().isEmpty()) {
            return 0;
        } else {
            double similarity = 0;
            HashSet<ProjectionFragment> attributes = query1.getAttributes();
            for (ProjectionFragment p1 : attributes) //compute the attribute of each dimension
            {
                ProjectionFragment p2 = query2.getAttributeFromHierarchy(p1.getHierarchy());
                similarity += p1.computeSimilarity(p2);
            }
            return (double) similarity / attributes.size();
        }
    }

    private int computeCardinalityIntersaction(HashSet<MeasureFragment> M1, HashSet<MeasureFragment> M2) {
        int count = 0;
        for (MeasureFragment m1 : M1) {
            for (MeasureFragment m2 : M2) {
                if (m1.isEqual(m2)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private boolean containsMeasure(List<MeasureFragment> result, MeasureFragment m) {

        for (MeasureFragment m2 : result) {
            if (m2.isEqual(m)) {
                return true;
            }
        }
        return false;
    }

    private int computeCardinalityUnion(HashSet<MeasureFragment> M1, HashSet<MeasureFragment> M2) {
        ArrayList<MeasureFragment> result = new ArrayList<MeasureFragment>();
        result.addAll(M1);

        for (MeasureFragment m : M2) {
            if (!containsMeasure(result, m)) {
                result.add(m);
            }
        }

        return result.size();
    }
}
