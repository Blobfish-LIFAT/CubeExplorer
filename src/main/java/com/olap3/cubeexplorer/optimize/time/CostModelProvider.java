package com.olap3.cubeexplorer.optimize.time;

import com.olap3.cubeexplorer.optimize.TimeableOp;

public class CostModelProvider {
    static CostModel defaultModel;

    public static CostModel getModelFor(TimeableOp op){
        if (false){
            //TODO check cube of the query to find server ?
            return null;
        }
        else {
            if (defaultModel == null){
                throw new IllegalStateException("No default cost model could be found ! Use supported operations or initialize default model.");
            }
            return defaultModel;
        }
    }

    public static CostModel getDefaultModel() {
        return defaultModel;
    }

    public static void setDefaultModel(CostModel defaultModel) {
        CostModelProvider.defaultModel = defaultModel;
    }
}
