package com.olap3.cubeexplorer.data;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.olap4j.metadata.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Getter
    private String name;

    /**
     * Header span which is how many rows (for column header) or columns (for row header) should the header
     * cover. Used by <i>CASTOR</i> client to display table headers
     */
    @Getter
    private int span;

    /**
     * True if the member is a measure.
     */
    private boolean isMeasure;

    /**
     * List of sub header
     */
    @Getter
    private List<HeaderTree> children;

    @Getter @Setter
    private HeaderTree parent;

    @Getter @Setter
    private Level memberDescriptor;



    public HeaderTree(String name, boolean isMeasure, Level memberDescriptor) {
        //System.out.println(name);
        this.name = name;
        this.isMeasure = isMeasure;
        this.children = new ArrayList<>();
        this.memberDescriptor = memberDescriptor;
    }

    /**
     * Remove empty children list by replacing them with null and compute span values
     */
    public static int updateSpanAndTrimChildren(HeaderTree ht) {
        if (ht.children == null || ht.children.isEmpty()) {
            ht.span = 1;
            ht.children = null;
            return 1;
        } else {
            ht.span = 0;
            for (HeaderTree child : ht.children) {
                ht.span += updateSpanAndTrimChildren(child);
            }
            return ht.span;
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


    public boolean isMeasure() {
        return isMeasure;
    }

    public boolean isRoot(){
        return this.name.equals("root");
    }

    public boolean  isLeaf(){
        return this.children == null || this.children.size() == 0;
    }

    public static List<HeaderTree> getLeaves(HeaderTree treeNode){
        HeaderTree root = getRoot(treeNode);
        return recGetLeaves(root);
    }

    private static List<HeaderTree> recGetLeaves(HeaderTree node){
        if (node.isLeaf())
            return Lists.newArrayList(node);

        return node.children.stream().map(HeaderTree::recGetLeaves).flatMap(List::stream).collect(Collectors.toList());
    }

    public static HeaderTree getRoot(HeaderTree node){
        if (node.isRoot())
            return node;
        else
            return getRoot(node.getParent());
    }

    public static List<String> getCrossTabPos(HeaderTree node){
        if (node.isRoot())
            return Lists.newArrayList();
        var l = getCrossTabPos(node.getParent());
        l.add(node.getName());
        return l;
    }

    public static List<String> getLevelDescriptors(HeaderTree node){
        if (node.isRoot())
            return Lists.newArrayList();
        var l = getLevelDescriptors(node.getParent());
        l.add(node.getMemberDescriptor().getName());
        return l;
    }

    public void addChild(HeaderTree child){
        this.children.add(child);
        child.setParent(this);
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
