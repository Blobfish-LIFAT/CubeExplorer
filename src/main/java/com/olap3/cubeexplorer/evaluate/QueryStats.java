package com.olap3.cubeexplorer.evaluate;

import lombok.Data;

@Data
public class QueryStats {
    int projNb, selNb, tableNb, aggNb;
}
