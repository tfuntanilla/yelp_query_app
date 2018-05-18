package hw3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.logging.Logger;

public class Populate {
	
	private static final int DATASETS = 4;
	private static final String HOST = "localhost";
	private static final String SERVICE_NAME = "orcl";
	private static final int PORT = 1521;
	private static final String[] TABLES = {
			"business",
			"bu_hours",
			"category",
			"bu_category",
			"subcategory",
			"bu_subcategory",
			"bu_attribute",
			"yelp_user",
			"review",
			"friend",
			"elite_years",
			"checkin"
	}; 
	
	public static void main(String[] args) {

		if (args.length != 4) {
			System.out.println("usage: populate yelp_business.json yelp_review.json yelp_checkin.json yelp_user.json");
			System.exit(-1);
		}

		// 1. Load the driver
		try { 
			Class.forName("oracle.jdbc.driver.OracleDriver"); 
		} catch(ClassNotFoundException e) {
			System.out.println("Exception while loading oracle jdbc driver: " + e.getMessage());
		}

		// TODO Determine what to do with hardcoded variables
		// 2. Define the connection URL
		String oracleURL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE_NAME;

		// 3. Establish the Connection
		String username = "hr";
		String password = "oracle";
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(oracleURL, username, password);
		} catch (SQLException e) {
			System.out.println("Exception while establishing connection: " + e.getMessage());
		}
		
		// 4. Check that required tables exist...
		boolean pass = true;
		for (String tableName : TABLES) {
			if (!isTableExists(connection, tableName)) {
				System.out.println("Required database table '" + tableName + "' does not exist!");
				pass = false;
			}
		}

		if (!pass) {
			System.out.println("Please create required tables.");
			System.exit(-1);
		}
		
		// 5. Delete all rows from all tables before populating
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			for (String tName : TABLES) {
			    String query = "TRUNCATE " + tName;
			    stmt.executeUpdate(query);
			}
		} catch (SQLException e) {
			System.out.println("Exception on TRUNCATE: " + e.getMessage());
		}
		
		// TODO
		// 6. Parse data
		for (int i = 0; i < DATASETS; i++) {

			URL path = Populate.class.getResource(args[i]);
			File json = new File(path.getFile());
			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new FileReader(json));
				String line = null;
				while ((line = reader.readLine()) != null) {
					switch(i) {
						case(0): 
							populateBusiness(line); 
							break;
						case(1):
							populateReview(line);
							break;
						case(2):
							populateCheckin(line);
						case(3):
							populateUser(line);
						default:
							break;
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println("File '" + path.getFile() + "' not found.");
				System.exit(-1);
			} catch (IOException e) {
				System.out.println("Exception while trying to parse datasets: " + e.getMessage());
				System.exit(-1);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					System.out.println("Exception while trying to close reader: " + e.getMessage());
					System.exit(-1);
				}
			}
		}
	
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("Exception while closing db connection: " + e.getMessage());
		}

		System.exit(0);

	}

	public static boolean isTableExists(Connection conn, String tableName) {
		
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
		} catch(SQLException e) {
			System.out.println("Exception while checking if table '" + tableName + "' exists: " + e.getMessage());
		}

		return isExists;
	}

	private static void populateBusiness(String line) {
		
	}

	private static void populateReview(String line) {

	}

	private static void populateCheckin(String line) {

	}

	private static void populateUser(String line) {

	}

}
