package hw3;

import java.io.*;
import java.sql.*;

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
            clearTable(connection);

            String currFile = "", file1 = "", file2 = "", file3 = "";
            File jsonFile;
            BufferedReader reader;
            try {

                // need to read and insert these in order because of constraints
                for (String arg : args) {
                    if (arg.equals("yelp_business.json")) {
                        file1 = arg;
                    }
                    if (arg.equals("yelp_user.json")) {
                        file2 = arg;
                    }
                    if (arg.equals("yelp_review.json")) {
                        file3 = arg;
                    }
                }

                if (file1.equals("") || file2.equals("") || file3.equals("")) {
                    System.out.println("You are missing a required file from the program arguments!");
                    System.exit(-1);
                }

                currFile = file1;
                jsonFile = new File(file1);
                reader = new BufferedReader(new FileReader(jsonFile));
                System.out.println("Parsing yelp_business.json...");
                BusinessParser.parse(connection, reader);
                System.out.println("Done.\n");
                reader.close();

                currFile = file2;
                jsonFile = new File(file2);
                reader = new BufferedReader(new FileReader(jsonFile));
                System.out.println("Parsing yelp_user.json...");
                UserParser.parse(connection, reader);
                System.out.println("Done.\n");
                reader.close();

                currFile = file3;
                jsonFile = new File(file3);
                reader = new BufferedReader(new FileReader(jsonFile));
                System.out.println("Parsing yelp_review.json... this will take a few seconds. Please wait...");
                ReviewParser.parse(connection, reader);
                System.out.println("Done.\n");
                reader.close();

                // Not parsing yelp_checkin.json as data from it is not needed
                System.out.println("Not parsing yelp_checkin.json - nothing needed from this file.\n");

            } catch (FileNotFoundException e) {
                System.out.println("File '" + currFile + "' not found.");
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

    private static void clearTable(Connection connection) {

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            for (String table : Constants.TABLES) {
                String query = "DELETE FROM " + table.toUpperCase();
                System.out.println("Clearing table..." + query);
                rs = stmt.executeQuery(query);
            }
            System.out.println("Done.\n");
        } catch (SQLException e) {
            System.out.println("Exception on DELETE: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                System.out.println("Exception while closing clearTable statement: " + e.getMessage());
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
