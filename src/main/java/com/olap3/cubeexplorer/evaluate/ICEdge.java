package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.infocolectors.InfoCollector;
import org.jgrapht.graph.DefaultEdge;

class ICEdge extends DefaultEdge {

    public InfoCollector getU(){
        return (InfoCollector) this.getSource();
    }

    public InfoCollector getV(){
        return (InfoCollector) this.getTarget();
    }
}
