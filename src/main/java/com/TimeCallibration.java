package com;

import com.olap3.cubeexplorer.DOLAP;
import com.olap3.cubeexplorer.data.Compatibility;
import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.evaluate.QueryStats;
import com.olap3.cubeexplorer.evaluate.SQLEstimateEngine;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.im_olap.model.Query;
import com.olap3.cubeexplorer.im_olap.model.Session;
import com.olap3.cubeexplorer.julien.Qfset;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import com.olap3.cubeexplorer.xmlutil.XMLPlan;
import lombok.Data;
import mondrian.olap.Connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeCallibration {


    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());
    static final String testData = "./data/import_ideb";
    static final String outCSV = "./data/timed_queries.csv";

    public static void main(String[] args) throws IOException {
        LOGGER.info("Initializing Mondrian"); //Initializer Block
        Connection olap = MondrianConfig.getMondrianConnection();
        if (olap == null) {
            System.err.println("Couldn't initialize db/mondrian connection check stack trace for details");
            System.exit(1); //Crash the app can't do anything w/o mondrian
        }
        CubeUtils utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);
        LOGGER.info("Mondrian connection init complete");

        LOGGER.info("Loading test data from " + testData);
        var sessions = DopanLoader.loadDir(testData);
        LOGGER.info("Test data loaded");

        //Open csv
        PrintWriter out = new PrintWriter(new FileOutputStream(new File(outCSV)));
        out.println("id,projNb,selNb,tableNb,estTuples,aggNb,predicted,actual");

        SQLFactory sje = new SQLFactory(utils);
        SQLEstimateEngine estimateEngine = new SQLEstimateEngine();
        java.sql.Connection con = MondrianConfig.getJdbcConnection();

        for (Session s : sessions){
            int i = 0;
            for (Query q : s.getQueries()){
                Qfset qfset = Compatibility.QPsToQfset(q, utils);
                String sql = sje.getStarJoin(qfset);

                XMLPlan plan = estimateEngine.estimates(sql);
                double t_est = plan.total_cost;
                double t_real = timeQuery(sql, con);
                String id = "\"" + s.getFilename() + "|" + i + "\"";
                QueryStats sq = sje.getLastStarStats();
                out.printf("%s,%s,%s,%s,%s,%s,%s,%s%n", id, sq.getProjNb(), sq.getSelNb(), sq.getTableNb(), plan.estimated_tuples, sq.getAggNb(), t_est, t_real);

                i++;
            }
            out.flush();
        }


        //Close file
        out.flush();
        out.close();
    }

    public static double timeQuery(String query, java.sql.Connection con){
        try {
            Statement planON = con.createStatement();
            planON.execute("set statistics time on;");

            ResultSet rs = planON.executeQuery(query);

            double t = parseWarning(planON);

            planON.close();
            return t;

        } catch (SQLException e){
            System.err.printf("Offending query : [%s]%n", query);
            e.printStackTrace();
        }
        return -1;
    }

    static Pattern timePattern = Pattern.compile(".*elapsed time = (\\d*) ms\\.", Pattern.MULTILINE|Pattern.DOTALL);
    public static double parseWarning(Statement st) throws SQLException {
        List<SQLWarning> warnings = new ArrayList<>();
        SQLWarning w = st.getWarnings();
        warnings.add(w);
        while ((w = w.getNextWarning()) != null){
            warnings.add(w);
        }

        for (SQLWarning warning : warnings){
            Matcher m = timePattern.matcher(warning.getMessage());
            boolean status = m.matches();
            if (m.matches()){
                return Double.parseDouble(m.group(1))/1000;
            }
        }
        return -1;
    }



}
