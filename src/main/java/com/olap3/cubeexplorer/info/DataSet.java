package com.olap3.cubeexplorer.info;

import com.alexscode.utilities.collection.Pair;
import com.olap3.cubeexplorer.QueryPart;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSet {

    List<List> data


    public DataSet(List<Pair<String, Datatype>> columnsDef, int size) {
        //TODO
    }


    public Object[] getLine(int i){
        return null; //TODO
    }


    public int[] getIntColumn(int column_index) {}
    public double[] getDoubleColumn(int column_index) {}
    // ...

    public int[] getIntColumn(String col_name) {}
    // ...

    public String getColName(int column_index) {}

    public Datatype getColDatatype(int column_index) {}
    public Datatype getColDatatype(String column_name) {}

    public SemanticDatatype getColSemanticDatatype(int column_index) {}
    public SemanticDatatype getColSemanticDatatype(String column_name) {}

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


