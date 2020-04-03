/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olap3.cubeexplorer.measures;

/**
 * @author Julien
 */
abstract class Similarity {

    protected double similarity;

    public Similarity(double similarity) {
        this.similarity = similarity;
    }

    public Similarity(){
        this.similarity=-1;
    }
    public abstract double getSimilarity();
}