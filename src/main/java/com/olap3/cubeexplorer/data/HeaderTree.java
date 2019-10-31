package com.olap3.cubeexplorer.data;

import org.olap4j.CellSet;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Table header, used for building graphical table. It's modeled like a tree structure, with root node always named
 * "root"</p>
 *<p>Don't forget to call {@link HeaderTree#updateSpanAndTrimChildren()} after building the tree to compute span value
 * and remove empty children list.</p>
 */
public class HeaderTree {

    /**
     * Header name or value
     */
    private String name;

    /**
     * Header span which is how many rows (for column header) or columns (for row header) should the header
     * cover. Used by <i>CASTOR</i> client to display table headers
     */
    private int span;

    /**
     * True if the member is a measure. This value is set in the {@link olap.CellSet#CellSet(CellSet) CellSet} constructor
     */
    private boolean isMeasure;

    /**
     * List of sub header
     */
    private List<HeaderTree> children;

    public HeaderTree(String name) {
        this(name, false);
    }

    public HeaderTree(String name, boolean isMeasure) {
        this.name = name;
        this.isMeasure = isMeasure;
        this.children = new ArrayList<>();
    }

    /**
     * Remove empty children list by replacing them with null and compute span values
     */
    public void updateSpanAndTrimChildren() {
        if (children == null || children.isEmpty()) {
            span = 1;
            children = null;
        } else {
            span = 0;
            for (HeaderTree child : children) {
                child.updateSpanAndTrimChildren();
                span += child.span;
            }
        }
    }

    /**
     * Search for a direct child with the given name from th children list
     *
     * @param childrenName The child name
     * @return The child if found or null if not
     */
    public HeaderTree getChildNamed(String childrenName) {
        if (childrenName == null) return null;
        for (HeaderTree child : children) {
            if (childrenName.equals(child.name)) return child;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getSpan() {
        return span;
    }

    public boolean isMeasure() {
        return isMeasure;
    }

    public List<HeaderTree> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "HeaderTree{" +
                "name='" + name + '\'' +
                ", span=" + span +
                ", children=" + children +
                '}';
    }
}
