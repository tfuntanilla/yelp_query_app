package hw3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Populate {

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("Required program args: yelp_business.json yelp_review.json yelp_checkin.json yelp_user.json");
            System.exit(-1);
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Exception while loading oracle jdbc driver: " + e.getMessage());
            System.out.println("Terminated program.");
            System.exit(-1);
        }

        Connection connection = null;
        try {

            connection = DriverManager
                    .getConnection(Constants.ORACLE_URL, Constants.USERNAME, Constants.PASSWORD);


            // Check that required tables exist before populating
            boolean pass = true;
            for (String tableName : Constants.TABLES) {
                if (!isTableExists(connection, tableName)) {
                    System.out.println("Required database table '" + tableName + "' does not exist!");
                    pass = false;
                }
            }

            if (!pass) {
                System.out.println("Please create required tables and rerun program.");
                System.exit(-1);
            }

            // Clear rows before populating
            truncate(connection);

            URL path = null;
            File jsonFile;
            BufferedReader reader;
            try {

                path = Populate.class.getResource("yelp_business.json");
                jsonFile = new File(path.getFile());
                reader = new BufferedReader(new FileReader(jsonFile));
                System.out.println("Parsing yelp_business.json...");
                BusinessParser.parse(connection, reader);
                System.out.println("Done.\n");
                reader.close();

                path = Populate.class.getResource("yelp_user.json");
                jsonFile = new File(path.getFile());
                reader = new BufferedReader(new FileReader(jsonFile));
                System.out.println("Parsing yelp_user.json...");
                UserParser.parse(connection, reader);
                System.out.println("Done.\n");
                reader.close();

                path = Populate.class.getResource("yelp_review.json");
                jsonFile = new File(path.getFile());
                reader = new BufferedReader(new FileReader(jsonFile));
                System.out.println("Parsing yelp_review.json... this will take a few seconds. Please wait...");
                ReviewParser.parse(connection, reader);
                System.out.println("Done.\n");
                reader.close();

                // Not parsing yelp_checkin.json as data from it is not needed
                System.out.println("Not parsing yelp_checkin.json - nothing needed from this file.\n");

            } catch (FileNotFoundException e) {
                System.out.println("File '" + path.getFile() + "' not found.");
                System.exit(-1);
            } catch (IOException e) {
                System.out.println("Exception while closing reader: " + e.getMessage());
                System.exit(-1);
            }

            CreateIndex.createIndex(connection);
            connection.commit();

        } catch (SQLException e) {
            System.out.println("Exception while establishing connection: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Exception while closing connection: " + e.getMessage());
                System.exit(-1);
            }
        }

        System.exit(0);

    }

    private static boolean isTableExists(Connection conn, String tableName) {

        boolean isExists = false;
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName.toUpperCase(), null);
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equalsIgnoreCase(tableName)) {
                    isExists = true;
                    return isExists;
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception while checking if table '" + tableName + "' exists: " + e.getMessage());
        }

        return isExists;
    }

    private static void truncate(Connection connection) {

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            for (String table : Constants.TABLES) {
                String query = "TRUNCATE TABLE " + table;
                rs = stmt.executeQuery(query);
            }
        } catch (SQLException e) {
            System.out.println("Exception on TRUNCATE: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                System.out.println("Exception while closing truncate statement: " + e.getMessage());
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                System.out.println("Exception while closing result set: " + e.getMessage());
            }
        }

    }

}
