package com.olap3.cubeexplorer.evaluate;

import com.google.common.base.Stopwatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


@Data
@AllArgsConstructor
@ToString
public class EXPRunStats {
        Stopwatch globalTime, optTime, execTime;
        int candidatesNb, reoptGood, reoptBad;
}
