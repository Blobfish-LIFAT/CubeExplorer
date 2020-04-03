package com.olap3.cubeexplorer.evaluate;

import lombok.Data;

/**
 * Data about queries for analysis/features
 */
@Data
public class QueryStats {
    int projNb, selNb, tableNb, aggNb;
}
