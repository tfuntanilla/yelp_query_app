package hw3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.json.*;

public class Populate {

	
	private static final int DATASETS = 4;
	private static final String HOST = "localhost";
	private static final String SERVICE_NAME = "orcl";
	private static final int PORT = 1521;
	private static final String[] TABLES = {
			"business",
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
	private static final String[] CATEGORIES = {
			"Active Life",
			"Arts & Entertainment",
			"Automotive",
			"Car Rental",
			"Cafes",
			"Beauty & Spas",
			"Convenience Stores",
			"Dentists",
			"Doctors",
			"Drugstores",
			"Department Stores",
			"Education",
			"Event Planning & Services",
			"Flowers & Gifts",
			"Food",
			"Health & Medical",
			"Home Services",
			"Home & Garden",
			"Hospitals",
			"Hotels & Travel",
			"Hardware Stores",
			"Grocery",
			"Medical Centers",
			"Nurseries & Gardening",
			"Nightlife",
			"Restaurants",
			"Shopping",
			"Transportation"
	};
	private static Set<String> SUBCATEGORIES = new TreeSet<String>();
	private static Set<String> ATTRIBUTES = new TreeSet<String>();

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
				String query = "TRUNCATE TABLE " + tName;
				stmt.executeUpdate(query);
			}
		} catch (SQLException e) {
			System.out.println("Exception on TRUNCATE: " + e.getMessage());
		}

		// TODO
		// 6. Parse data and create insert statements		
		for (int i = 0; i < DATASETS; i++) {

			System.out.println("Parsing and generating INSERT statements for " + args[i]);
			URL path = Populate.class.getResource(args[i]);
			File json = new File(path.getFile());
			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new FileReader(json));
				String line = null;
				int totalBusiness = 0;
				while ((line = reader.readLine()) != null) {
					switch(i) {
					case(0): 
						insertIntoBusiness(connection, line, totalBusiness);
						totalBusiness += 1;
					break;
					case(1):
						insertIntoReview(connection, line);
					break;
					case(2):
						insertIntoCheckin(line);
					case(3):
						insertIntoUser(line);
					default:
						break;
					}
				}
				insertIntoCategory();
				insertIntoSubCategory();
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

	private static String insertIntoCategory() {
		
		System.out.println("Generating INSERT statements for table CATEGORY");
		String insert = "INSERT ALL\n";
		for (int i = 1; i <= CATEGORIES.length; i++) {
			insert += "INTO category VALUES(";
			insert += i + ",'" + CATEGORIES[i-1] + "'";
			insert += ")\n";
		}
		insert += "SELECT * FROM dual;";
		// System.out.println(insert);
		return insert;
	}
	
	private static String insertIntoSubCategory() {
		
		System.out.println("Generating INSERT statements for table SUBCATEGORY");
		String insert = "INSERT ALL\n";
		Iterator<String> it = SUBCATEGORIES.iterator();
		int c = 0;
		while (it.hasNext()) {
			insert += "INTO subcategory VALUES(";
			insert += (c+=1) + ",'" + it.next() + "')\n";
		}
		insert += "SELECT * FROM dual;";
		// System.out.println(insert);
		return insert;
		
	}

	private static String insertIntoBusiness(Connection connection, String line, int count) {

		JSONObject json = new JSONObject(line);
		String id = json.getString("business_id");
		String fullAddress = json.getString("full_address");
		boolean openBool = json.getBoolean("open");
		String open = (openBool ? "T" : "F");
		JSONArray buCategories = json.getJSONArray("categories");
		String city = json.getString("city");
		String state = json.getString("state");
		float latitude = json.getFloat("latitude");
		float longitude = json.getFloat("longitude");
		int reviewCount = json.getInt("review_count");
		String name = json.getString("name");
		double stars = json.getDouble("stars");
		JSONObject attributes = json.getJSONObject("attributes");
		String type = json.getString("type");

		String insertStmt = "INSERT INTO business VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement preparedStmt = connection.prepareStatement(insertStmt);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, name);
			preparedStmt.setString(3, fullAddress);
			preparedStmt.setString(4, city);
			preparedStmt.setString(5, state);
			preparedStmt.setFloat(6, latitude);
			preparedStmt.setFloat(7, longitude);
			preparedStmt.setString(8, open);
			preparedStmt.setInt(9, reviewCount);
			preparedStmt.setDouble(10, stars);
			preparedStmt.setString(11, type);
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO business: " + e.getMessage());
		}
		
		String insert = "INSERT INTO business VALUES";
		insert += "('" + id + "','" + name + "','" + fullAddress + "','" + city + "','" + state + "'," + latitude + 
				"," + longitude + ",'" + open + "'," + reviewCount + "," + stars + ",'" + type + "');\n";
		
		for (int i = 0; i < buCategories.length(); i++) {
			boolean isACategory = false;
			String cat = buCategories.get(i).toString();
			for (int j = 0; j < CATEGORIES.length; j++) {
				if (cat.equals(CATEGORIES[j])) {
					insert += "INSERT INTO bu_category VALUES";
					insert += "(" + count + ",'" + id + "','" + cat + "');\n";
					isACategory = true;
					break;
				}
			}
			if (!isACategory) {
				SUBCATEGORIES.add(cat);
				insert += "INSERT INTO bu_subcategory VALUES";
				insert += "(" + count + ",'" + id + "','" + buCategories.get(i).toString() + "');\n";
			}
		}
		
		//System.out.println(insert);
		return insert;
		
	}

	private static void insertIntoReview(Connection connection, String line) {
		
		JSONObject json = new JSONObject(line);
		
		String id = json.getString("review_id");
		String buId = json.getString("business_id");
		String userId = json.getString("user_id");
		int stars = json.getInt("stars");
		String reviewDate = json.getString("date");
		Date date = null;
		try {
			date = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(reviewDate).getTime());
		} catch (ParseException e1) {
			System.out.println("Exception while parsing review date: " + e1.getMessage());
		}
		String text = json.getString("text");
		JSONObject votes = json.getJSONObject("votes");
		int useful = votes.getInt("useful");
		int funny = votes.getInt("funny");
		int cool = votes.getInt("cool");
		String type = json.getString("type");
		
		String insertStmt = "INSERT INTO review VALUES(?,?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement preparedStmt = connection.prepareStatement(insertStmt);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, buId);
			preparedStmt.setString(3, userId);
			preparedStmt.setInt(4, stars);
			preparedStmt.setDate(5, date);
			preparedStmt.setString(6, text);
			preparedStmt.setInt(7, useful);
			preparedStmt.setInt(8, funny);
			preparedStmt.setInt(9, cool);
			preparedStmt.setString(10, type);
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO review: " + e.getMessage());
		}
		
		// TODO execute
	}

	private static String insertIntoCheckin(String line) {
		JSONObject json = new JSONObject(line);
		String buId = json.getString("business_id");
		String checkin_info = json.getJSONObject("checkin_info").toString();
		String insert = "";
		return insert;

	}

	private static String insertIntoUser(String line) {
		JSONObject json = new JSONObject(line);
		String insert = "";
		return insert;

	}

}
