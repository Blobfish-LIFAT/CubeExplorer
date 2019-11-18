package com.olap3.cubeexplorer.optimize.time;

import com.olap3.cubeexplorer.infocolectors.MDXAccessor;
import com.olap3.cubeexplorer.optimize.TimeableOp;

public class CostModelProvider {
    static CostModel defaultModel;

    public static CostModel getModelFor(TimeableOp op){
        if (op instanceof MDXAccessor){
            MDXAccessor accessor = ((MDXAccessor) op);
            String schema = accessor.getInternal().getMeasures().iterator().next().getAttribute().getHierarchy().getDimension().getSchema().getName();
            if (schema.equals("DOPAn DW3")){
                return new LinearTimeEstimatorDOPAN();
            }
            if (schema.equals("SCHEMA_SSB"))
                return new TimeCallibration();
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
