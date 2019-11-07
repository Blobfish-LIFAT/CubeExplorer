package com.olap3.cubeexplorer;

import com.google.common.base.Stopwatch;
import com.olap3.cubeexplorer.evaluate.QueryStats;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.model.Compatibility;
import com.olap3.cubeexplorer.model.Qfset;
import com.olap3.cubeexplorer.model.Query;
import com.olap3.cubeexplorer.model.Session;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeCallibration {


    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());
    static final String testData = "./data/ssb_converted/";
    static final String outCSV = "./data/stats/timed_queries_ssb.csv";
    private static String[] cubeloadProfiles = new String[]{"explorative", "goal_oriented", "slice_all", "slice_and_drill"};
    static CubeUtils utils;
    static Connection olap;

    public static void main(String[] args) throws IOException {
        MondrianConfig.defaultConfigFile = args[0];
        olap = MondrianConfig.getMondrianConnection();
        if (olap == null)
            System.exit(1); //Crash the app can't do anything w/o mondrian
        utils = new CubeUtils(olap, "SSB");
        CubeUtils.setDefault(utils);
        MondrianConfig.setMondrianConnection(olap);
        LOGGER.info("Server Connection established");

        List<Session> learning = new ArrayList<>();
        Map<String, List<Session>> profiles = new HashMap<>();
        for (String cubeloadProfile : cubeloadProfiles) {
            List<Session> profile = CubeLoad.loadCubeloadXML(testData + cubeloadProfile + ".xml", olap, "SSB").subList(0,10);
            profiles.put(cubeloadProfile, profile);
            learning.addAll(profile);
        }
        LOGGER.info("Loaded Cubeload Sessions");

        //Open csv
        PrintWriter out = new PrintWriter(new FileOutputStream(new File(outCSV)));
        //out.println("id,ascii_len,projNb,selNb,tableNb,estTuples,estTuples_proper,aggNb,predicted,actual");
        out.println("id,ascii_len,projNb,selNb,tableNb,aggNb,rows,actual");

        SQLFactory sje = new SQLFactory(utils);
        //SQLEstimateEngine estimateEngine = new SQLEstimateEngine();
        java.sql.Connection con = MondrianConfig.getJdbcConnection();

        LOGGER.info("Beginning query evaluation");
        for (Session s : learning){
            System.out.println("Doing session " + s.getFilename());
            int i = 0;
            for (Query q : s.getQueries()){
                String id = "\"" + s.getFilename() + "|" + i++ + "\"";

                Qfset qfset = Compatibility.QPsToQfset(q, utils);
                String sql = sje.getStarJoin(qfset);

                //XMLPlan plan = estimateEngine.estimates(sql);
                //double t_est = plan.total_cost;
                double t_real = timeQuery(sql, con);

                QueryStats sq = sje.getLastStarStats();
                if (!(t_real < 0))
                    //out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", id, sql.length(), sq.getProjNb(), sq.getSelNb(), sq.getTableNb(), plan.estimated_tuples, plan.full_row_cost, sq.getAggNb(), t_est, t_real);
                    out.printf("%s,%s,%s,%s,%s,%s,%s,%s%n", id, sql.length(), sq.getProjNb(), sq.getSelNb(), sq.getTableNb(), sq.getAggNb(), explainQueryRows(sql, con), t_real);

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
            //planON.execute("set statistics time on;");

            Stopwatch sw = Stopwatch.createStarted();
            ResultSet rs = planON.executeQuery(query);

            double sum = 0.0;
            while (rs.next()){
                sum = sum + 0.9; //Dummy loop don't need the results but go through the rs to be realist
            }

            //double t = parseWarning(planON);
            double t = sw.stop().elapsed(TimeUnit.MILLISECONDS);

            planON.close();
            return t;

        } catch (SQLException e){
            System.err.printf("Offending query : [%s]%n", query);
            //e.printStackTrace();
        }
        return -1;
    }

    public static int explainQueryRows(String query, java.sql.Connection con){
        try {
            Statement planON = con.createStatement();
            ResultSet rs = planON.executeQuery("EXPLAIN " + query);

            int sum = 1;
            while (rs.next()){
                sum += rs.getInt("rows");
            }

            planON.close();
            return sum;

        } catch (SQLException e){
            System.err.printf("Offending query : [%s]%n", query);
            //e.printStackTrace();
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

    //['ascii_len','selNb', 'projNb', 'tableNb', 'aggNb']
    //[  -0.64920196 -243.99479906   39.2955434   221.55470518   39.2955434 ]
    //-288.5450654145311
    static SQLFactory queryFactoryCBL = null;
    public static long approximateCubeload(Qfset qfset, java.sql.Connection con){
        if (queryFactoryCBL == null) {
            queryFactoryCBL = new SQLFactory(CubeUtils.getDefault());
        }
        String q = queryFactoryCBL.getStarJoin(qfset);
        QueryStats qs = queryFactoryCBL.getLastStarStats();
        qfset.setStats(qs);
        qfset.setSql(q);

        double estRaw = q.length() * -0.64920196 + qs.getProjNb() * 39.2955434 + qs.getSelNb() * -243.99479906 + qs.getTableNb() * 221.55470518 + qs.getAggNb() * 39.2955434 ;
        estRaw = estRaw -288.5450654145311;
        //System.out.println(estRaw);
        return Math.round(estRaw);
    }

}
