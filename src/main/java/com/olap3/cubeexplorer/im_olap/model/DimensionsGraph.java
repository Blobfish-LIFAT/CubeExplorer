package com.olap3.cubeexplorer.im_olap.model;

import com.google.common.graph.MutableValueGraph;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DimensionsGraph {
    static boolean compatibilityMode = false;

    public static MutableValueGraph<QueryPart, Double> injectSchema(MutableValueGraph<QueryPart, Double> base, CubeUtils cube){
        for (Hierarchy h : cube.getHierarchies()){
            Level[] levels = h.getLevels();
            for (int i = 0; i < levels.length - 1; i++) {
                QueryPart p1 = QueryPart.newDimension(levels[i].toString());
                QueryPart p2 = QueryPart.newDimension(levels[i+1].toString());
                base.putEdgeValue(p1, p2, 1.0);
                base.putEdgeValue(p2, p1, 1.0);
                //System.out.printf("Linking %s | %s%n", p1, p2);
            }
        }
        return base;
    }

    /**
     * This will inject edges in the graph based on a XML mondrian (v3) Schema, this is probably not compatible with complex schemas
     * will have to check for that
     * @param base
     * @param mondrianFile
     * @return
     */
    //FIXME avoid putting all dimensions from all cubes in there maybe ?
    public static MutableValueGraph<QueryPart, Double> injectSchema(MutableValueGraph<QueryPart, Double> base, String mondrianFile){
        SAXReader reader = new SAXReader();
        try {
            Document schema = reader.read(Paths.get(mondrianFile).toFile());
            List<Node> nodes = schema.selectNodes("//Dimension"); //We select all dimensions with XPath

            //For each dimensions we must add father/child links in the graph
            for (Node dimension : nodes){
                String dimName = ((Element) dimension).attributeValue("name");
                List<Node> hierarchies = dimension.selectNodes("Hierarchy");
                //System.out.printf("[DEBUG] Found %d hierarchies in %s%n",hierarchies.size(), dimName);
                for (Node h : hierarchies){
                    Element hierarchy = (Element) h; String prefix;
                    if (compatibilityMode)
                        prefix = "[" + dimName + "].[" + hierarchy.attributeValue("name") + "]";
                    else
                        prefix = "[" + dimName + "." + hierarchy.attributeValue("name") + "]";

                    List<String> levels = new ArrayList<>();
                    levels.add(hierarchy.attributeValue("allLevelName"));
                    h.selectNodes("./Level").stream().map(o -> (Element) o).forEach(level -> levels.add(((Element) level).attributeValue("name")));

                    for (int i = 0; i < levels.size() - 1; i++) {
                        QueryPart p1 = QueryPart.newDimension(prefix + ".[" + levels.get(i) + "]");
                        QueryPart p2 = QueryPart.newDimension(prefix + ".[" + levels.get(i+1) + "]");
                        base.putEdgeValue(p1, p2, 1.0);
                        base.putEdgeValue(p2, p1, 1.0);
                        //System.out.printf("Linking %s | %s%n", p1, p2);
                    }
                }


            }

            return base;

        } catch (Exception e) {
            System.err.printf("Could not parse schema in '%s', verify file permission/format.", mondrianFile);
            return base;
        }
    }
}
