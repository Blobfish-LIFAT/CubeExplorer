package com.olap3.cubeexplorer.Similarity.ced;

public class ContextualFunction {

    public static FType type = FType.GAUSS;

    public static double getWeight(Operator op, int index, double sigma, int x){
        if (op instanceof Edit){
            return dynFunction(index, sigma ,x);
        }
        if(op instanceof Add){
            if (x >= index)
                return dynFunction(index, sigma ,x);
            return dynFunction(index, sigma ,x + 1);
        }
        if (op instanceof Del){
            if (index == x)
                return 0;
            if (x <= index - 1)
                return dynFunction(index, sigma ,x + 1);
            return dynFunction(index, sigma ,x -1);
        }

        System.err.println("Err");
        return 0;
    }

    private static double dynFunction(double k, double sigma, double x){
        switch (type){
            case GAUSS: return gaussian_pdf(k, sigma, x);
            default:
            case UNIT: return 1;
        }
    }

    private static double gaussian_pdf(double k, double sigma, double x){
        return Math.exp(-0.5*Math.pow((x-k)/sigma, 2));
    }
}
