package com.olap3.cubeexplorer.mondrian;

import mondrian.olap.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MondrianConfig {
    private static Connection mondrianConnection;
    private static java.sql.Connection jdbcConnection;
    public static String defaultConfigFile = "data/olap.properties";
    private static Properties config = new Properties();
    private static boolean initConf = false;

    public static String getURL(){
        if (!initConf) {
            try {
                loadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "jdbc:mondrian:" +
                "Jdbc="+config.getProperty("jdbcUrl")+";" +
                "JdbcDrivers="+config.getProperty("driver")+";" +
                "Catalog=file:" + config.getProperty("schemaFile")
                + ";JdbcUser=" + config.getProperty("jdbcUser")
                + ";JdbcPassword=" + config.getProperty("jdbcPassword");
    }

    private static void initConnection() throws ClassNotFoundException, SQLException {
        Class.forName(config.getProperty("driver"));
        Class.forName("mondrian.olap4j.MondrianOlap4jDriver");

        String mondrianString =
                "Provider=" + config.getProperty("Provider")
                        + ";Jdbc=" + config.getProperty("jdbcUrl")
                        + ";Catalog=" + config.getProperty("schemaFile")
                        + ";JdbcDrivers=" + config.getProperty("driver")
                        + ";JdbcUser=" + config.getProperty("jdbcUser")
                        + ";JdbcPassword=" + config.getProperty("jdbcPassword");

        //System.out.println(config.getProperty("jdbcUrl"));
        //jdbcConnection     = DriverManager.getConnection(config.getProperty("jdbcUrl"));
        jdbcConnection     = DriverManager.getConnection(config.getProperty("jdbcUrl"), config.getProperty("jdbcUser"), config.getProperty("jdbcPassword"));
        mondrianConnection = mondrian.olap.DriverManager.getConnection(mondrianString, null);

        //Cache disable
        //MondrianProperties.instance().DisableCaching.set(true);

    }

    public static Connection getMondrianConnection() {
        if (mondrianConnection == null) {
            try {
                loadConfig();
                initConnection();
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

        }
        return mondrianConnection;
    }

    public static Connection getSeparateConnection(String confFilePath) throws IOException, ClassNotFoundException, SQLException{
        Properties config = new Properties();
        config.load(new FileInputStream(new File(confFilePath)));
        Class.forName(config.getProperty("driver"));
        Class.forName("mondrian.olap4j.MondrianOlap4jDriver");

        String mondrianString =
                "Provider=" + config.getProperty("Provider")
                        + ";Jdbc=" + config.getProperty("jdbcUrl")
                        + ";Catalog=" + config.getProperty("schemaFile")
                        + ";JdbcDrivers=" + config.getProperty("driver")
                        + ";JdbcUser=" + config.getProperty("jdbcUser")
                        + ";JdbcPassword=" + config.getProperty("jdbcPassword");

        return mondrian.olap.DriverManager.getConnection(mondrianString, null);
    }

    private static void loadConfig() throws IOException {
        String path = System.getProperty("olapConfig");
        if (path == null) path = defaultConfigFile;

        config.load(new FileInputStream(new File(path)));
        initConf = true;
    }

    /**
     * Do not use this if you just need a database connection
     * @return the underlining dopan connection
     */
    public static java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }

    public static java.sql.Connection getNewJdbcConnection(){
        try {
            return DriverManager.getConnection(config.getProperty("jdbcUrl"), config.getProperty("jdbcUser"), config.getProperty("jdbcPassword"));
        } catch (SQLException e) {
            e.printStackTrace();
            return getJdbcConnection();
        }
    }

    public static void close(){
        mondrianConnection.close();
        try {
            jdbcConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jdbcConnection = null;
            mondrianConnection = null;
        }
    }

    public static void setMondrianConnection(Connection mondrianConnection) {
        MondrianConfig.mondrianConnection = mondrianConnection;
    }
}
