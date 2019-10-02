package com.olap3.cubeexplorer.optimize.stats;

import lombok.Data;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.Frequency;

@Data
public class Statistics {
    int cellCount;
    Frequency distribution;
    DescriptiveStatistics stats;
}
