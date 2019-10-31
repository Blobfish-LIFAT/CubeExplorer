package com.olap3.cubeexplorer;

import com.olap3.cubeexplorer.data.DopanLoader;
import com.olap3.cubeexplorer.evaluate.QueryStats;
import com.olap3.cubeexplorer.evaluate.SQLEstimateEngine;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.evaluate.xmlutil.XMLPlan;
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
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeCallibration {


    private static final Logger LOGGER = Logger.getLogger(DOLAP.class.getName());
    static final String testData = "./data/import_ideb";
    static final String testData2 = "./data/dopan_converted";
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
        //sessions.addAll(DopanLoader.loadDir(testData2).stream().filter(s -> s.getCubeName().equals("Cube1MobProInd")).collect(Collectors.toSet()));
        LOGGER.info("Test data loaded");

        //Open csv
        PrintWriter out = new PrintWriter(new FileOutputStream(new File(outCSV)));
        out.println("id,ascii_len,projNb,selNb,tableNb,estTuples,estTuples_proper,aggNb,predicted,actual");

        SQLFactory sje = new SQLFactory(utils);
        SQLEstimateEngine estimateEngine = new SQLEstimateEngine();
        java.sql.Connection con = MondrianConfig.getJdbcConnection();

        LOGGER.info("Beginning query evaluation");
        for (Session s : sessions){
            int i = 0;
            for (Query q : s.getQueries()){
                String id = "\"" + s.getFilename() + "|" + i++ + "\"";

                Object mdx = q.getProperties().get("mdx");
                if (mdx != null && mdx.toString().contains("Cube2MobScoInd"))
                    continue;
                if (mdx != null && mdx.toString().contains("Cube4Chauffage"))
                    continue;
                Qfset qfset = Compatibility.QPsToQfset(q, utils);
                String sql = sje.getStarJoin(qfset);

                XMLPlan plan = estimateEngine.estimates(sql);
                double t_est = plan.total_cost;
                double t_real = timeQuery(sql, con);

                QueryStats sq = sje.getLastStarStats();
                if (!(t_real < 0))
                    out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", id, sql.length(), sq.getProjNb(), sq.getSelNb(), sq.getTableNb(), plan.estimated_tuples, plan.full_row_cost, sq.getAggNb(), t_est, t_real);

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
