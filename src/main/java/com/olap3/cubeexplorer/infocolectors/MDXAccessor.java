package com.olap3.cubeexplorer.infocolectors;

import com.alexscode.utilities.collection.Pair;
import com.olap3.cubeexplorer.LinearTimeEstimator;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SQL datatype : https://www.tutorialspoint.com/java-resultsetmetadata-getcolumntype-method-with-example
 */
public class MDXAccessor extends DataAccessor {
    DataSet cached = null;
    @Setter
    long mesuredTime;

    static Connection connection;
    static SQLFactory sqlFactory;


    public MDXAccessor(Qfset query) {
        this.internal = query;
    }

    @Override
    public DataSet execute() {
        if (cached != null)
            return cached;

        init();

        try {

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sqlFactory.getStarJoin(internal));
            ResultSetMetaData rsmd = rs.getMetaData();

            List<Pair<String, DataSet.Datatype>> cols = new ArrayList<>();
            for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
                var name = rsmd.getColumnName(i);
                var typeRaw = rsmd.getColumnType(i);
                cols.add(new Pair<>(name, getProperType(typeRaw)));
            }

            rs.last();
            int count = rs.getRow();
            rs.beforeFirst();

            cached = new DataSet(cols, count);

            while (rs.next()){
                Object[] line = new Object[cols.size()];
                for (int i = 0; i < cols.size(); i++) {
                    switch (cols.get(i).right){
                        case INTEGER : line[i] = rs.getInt(cols.get(i).left); break;
                        case REAL : line[i] = rs.getDouble(cols.get(i).left); break;
                        case STRING : line[i] = rs.getString(cols.get(i).left); break;
                    }
                }
                cached.pushLine(line);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cached;
    }

    private static HashMap<Integer, DataSet.Datatype> typeMap;
    static {
        typeMap = new HashMap<>();
        typeMap.put(12, DataSet.Datatype.STRING);
        typeMap.put(6, DataSet.Datatype.REAL);
        typeMap.put(8, DataSet.Datatype.REAL);
        typeMap.put(4, DataSet.Datatype.INTEGER);
    }
    private static DataSet.Datatype getProperType(int typeRaw) {
        return typeMap.get(typeRaw);
    }

    static void init(){
        if (connection == null){
            try {
                connection = DriverManager.getConnection(MondrianConfig.getURL());
                sqlFactory = new SQLFactory(CubeUtils.getDefault());
            } catch (SQLException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public int aprioriTuples() {
        return 0;
    }

    @Override
    public int aposterioriTuples() {
        return 0;
    }

    @Override
    public long aprioriTime() {
        //return TimeCallibration.approximateCubeload(this.internal, MondrianConfig.getJdbcConnection());
        return LinearTimeEstimator.estimateQfsetMs(this.internal);
    }

    @Override
    public long aposterioriTime() {
        return mesuredTime;
    }

    @Override
    public double aprioriInterest() {
        return 0;
    }

    @Override
    public double aposterioriInterest() {
        return 0;
    }
}
