package com.olap3.cubeexplorer.olap;

import org.olap4j.*;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Member;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This class act like a wrapper for the {@link org.olap4j.CellSet CellSet} interface. The main purpose is to store
 * the cell set values into an <code>Double[][]</code> for a simpler accessing, and provide utility methods</p>
 * <p><b><i>"But why just not extends/implement CellSet ?"</i></b> you may ask.</p>
 * <p>When executing an MDX query, a {@link org.olap4j.CellSet CellSet} implementation is retrieved. The implementation
 * depends of which driver was used for the connection. Mondrian for example return an <code>MondrianOlap4jCellSet</code>
 * which is a private class ! That means we can't extends it (curse you Mondrian !).</p>
 * <p>This class is also named CellSet, that may be stupid. Feel free to change it if you find a better name. Maybe
 * "Dashboard", or something like "CubeBoard" ? ... "DashCube" ?</p>
 */
public class CellSet {
    /**
     * The {@link org.olap4j.CellSet CellSet} object to wrap
     */
    public org.olap4j.CellSet olap4JCellSet;

    /**
     * Cells values matrix. Empty cells are null
     */
    private Double[][] data;

    private List<Double[][]> dataList;

    /**
     * Data headers (like column or row names). First one is column headers and second is row header
     */
    private List<HeaderTree> headerTreeList;

    private int measureAxisOrdinal = -1;

    private int nbOfRows;
    private int nbOfColumns;
    private int nbOfCells;

    /**
     * <p>Constructor used for testing. You should not build your cell set with this constructor.
     * Instead use {@link CellSet#CellSet(org.olap4j.CellSet)}</p>
     */
    public CellSet() {
    }

    public CellSet(org.olap4j.CellSet olap4JCellSet) {
        //TODO: check if olap4JCellSet is 2D, else throw an exception
        this.olap4JCellSet = olap4JCellSet;
        nbOfRows = olap4JCellSet.getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount();
        nbOfColumns = olap4JCellSet.getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount();
        nbOfCells = nbOfColumns * nbOfRows;

        // Build data
        data = new Double[nbOfRows][nbOfColumns];
        for (int row = 0; row < nbOfRows; row++) {
            for (int col = 0; col < nbOfColumns; col++) {
                Cell cell = olap4JCellSet.getCell(Arrays.asList(col, row));
                if (cell != null && !cell.isError() && !cell.isNull() && !cell.isEmpty()) {
                    try {
                        data[row][col] = cell.getDoubleValue(); //TODO: find something prettier than replace method
                    } catch (OlapException e) {
                        e.printStackTrace();
                    }
                } else {
                    data[row][col] = null;
                }
            }
        }

        // Build headers for each axis
        headerTreeList = new ArrayList<>();
        for (CellSetAxis axis : olap4JCellSet.getAxes()) {
            headerTreeList.add(this.buildHeaders(axis));
        }

        // Find measures
        List<HeaderTree> measureHeaderList = new ArrayList<>();
        List<HeaderTree> headerList = new ArrayList<>();
        for (int axisOrdinal = 0; axisOrdinal < headerTreeList.size(); axisOrdinal++) {
            List<HeaderTree> childrens = this.headerTreeList.get(axisOrdinal).getChildren();
            if (childrens != null) {
                headerList.addAll(childrens);
                while (!headerList.isEmpty()) {
                    List<HeaderTree> childList = new ArrayList<>();
                    for (HeaderTree parent : headerList) {
                        if (parent.isMeasure()) {
                            measureHeaderList.add(parent);
                        } else if (parent.getChildren() != null) {
                            childList.addAll(parent.getChildren());
                        }
                    }
                    headerList = childList;
                }
            }
            if (!measureHeaderList.isEmpty()) {
                measureAxisOrdinal = axisOrdinal;
                break;
            }
        }

        // Build data set list
        dataList = new ArrayList<>();
        int offset = 0;
        if (measureAxisOrdinal == 0) { // Column
            for (HeaderTree header : measureHeaderList) {
                Double[][] dataset = new Double[nbOfRows][header.getSpan()];
                for (int row = 0; row < dataset.length; row++) {
                    System.arraycopy(data[row], offset, dataset[row], 0, header.getSpan());
                }
                dataList.add(dataset);
                offset += header.getSpan();
            }
        } else if (measureAxisOrdinal == 1) { // Row
            for (HeaderTree header : measureHeaderList) {
                Double[][] dataset = new Double[header.getSpan()][nbOfColumns];
                for (int row = 0; row < header.getSpan(); row++) {
                    System.arraycopy(data[offset + row], 0, dataset[row], 0, nbOfColumns);
                }
                dataList.add(dataset);
                offset += header.getSpan();
            }
        } else { // No measure in axis
            dataList.add(data);
        }
    }

    public Double[][] getData() {
        return data;
    }

    public void setData(Double[][] data) {
        this.data = data;
    }

    public List<Double[][]> getDatasetList() {
        return dataList;
    }

    public Double[] getFlatData() {
        Double[] res = new Double[nbOfCells];
        for (int ordinal = 0; ordinal < nbOfCells; ordinal++) {
            res[ordinal] = data[ordinal / nbOfColumns][ordinal % nbOfColumns];
        }
        return res;
    }


    private HeaderTree buildHeaders(CellSetAxis axis) {
        HeaderTree root = new HeaderTree("root");
        for (Position position : axis) {
            HeaderTree parent = root;
            for (Member member : position.getMembers()) {
                HeaderTree child = parent.getChildNamed(member.getName());
                if (child == null) {
                    boolean isMeasure = false;
                    try {
                        isMeasure = Dimension.Type.MEASURE.equals(member.getHierarchy().getDimension().getDimensionType());
                    } catch (OlapException e) {
                        e.printStackTrace();
                    }
                    child = new HeaderTree(member.getName(), isMeasure);
                    parent.getChildren().add(child);
                }
                parent = child;
            }
        }
        root.updateSpanAndTrimChildren();
        return root;
    }

    /**
     * Return Header tree. It begin with root node
     *
     * @param axisOrdinal
     * @return
     */
    public HeaderTree getHeaderTree(int axisOrdinal) {
        return headerTreeList.get(axisOrdinal);
    }

    public List<CellSetAxis> getAxes() {
        return this.olap4JCellSet.getAxes();
    }

    public Cell getCell(List<Integer> list) {
        return this.olap4JCellSet.getCell(list);
    }

    public Cell getCell(int ordinal) {
        return this.olap4JCellSet.getCell(ordinal);
    }

    public List<Integer> ordinalToCoordinates(int ordinal) {
        return olap4JCellSet.ordinalToCoordinates(ordinal);
    }

    public int getNbOfRows() {
        return nbOfRows;
    }

    public void setNbOfRows(int nbOfRows) {
        this.nbOfRows = nbOfRows;
    }

    public int getNbOfColumns() {
        return nbOfColumns;
    }

    public void setNbOfColumns(int nbOfColumns) {
        this.nbOfColumns = nbOfColumns;
    }

    public int getNbOfCells() {
        return nbOfCells;
    }

    public void setNbOfCells(int nbOfCells) {
        this.nbOfCells = nbOfCells;
    }

    public int getMeasureAxisOrdinal() {
        return measureAxisOrdinal;
    }
}
