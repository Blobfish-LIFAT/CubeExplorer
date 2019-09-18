package com.olap3.cubeexplorer.info;

import com.alexscode.utilities.collection.Pair;
import com.olap3.cubeexplorer.QueryPart;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSet {

    List<List> data;


    public DataSet(List<Pair<String, Datatype>> columnsDef, int size) {
        //TODO
    }

    public Object[] getLine(int i){
        return null; //TODO
    }


    public int getNumberOfColumns(){
        return -1;
    }

    public int getNumberOfRows(){
        return -1;
    }

    public int[] getIntColumn(int column_index) {
        return new int[0];
    }
    public double[] getDoubleColumn(int column_index) {
        return new double[0];
    }
    public long[] getLongColumn(int column_index) {
        return new long[0];
    }
    // ...

    public int[] getIntColumn(String col_name) {
        return null;
    }
    // ...

    public String getColName(int column_index) {
        return null;
    }

    public Datatype getColDatatype(int column_index) {
        return null;
    }

    public Datatype getColDatatype(String column_name) {
        return null;
    }

    public SemanticDatatype getColSemanticDatatype(int column_index) {
        return null;
    }
    public SemanticDatatype getColSemanticDatatype(String column_name) {
        return null;
    }

    public enum SemanticDatatype {
        Date, Currency //...
    }


    public enum Datatype {
        STRING(0), INTEGER(1), REAL(2);

        private int value;
        private static Map map = new HashMap<>();

        private Datatype(int value) {
            this.value = value;
        }

        static {
            for (DataSet.Datatype pageType : DataSet.Datatype.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static DataSet.Datatype valueOf(int pageType) {
            return (DataSet.Datatype) map.get(pageType);
        }

        public int getValue() {
            return value;
        }

        public byte[] getBytes() {
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(value);
            return b.array();
        }
    }
}


