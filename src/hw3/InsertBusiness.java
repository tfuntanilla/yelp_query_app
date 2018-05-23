package hw3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class InsertBusiness {
	
	private static final String HOST = "localhost";
	private static final String SERVICE_NAME = "orcl";
	private static final String USERNAME = "hr";
	private static final String PASSWORD = "oracle";
	private static final int PORT = 1521;
	private static final String[] TABLES = {
			"business",
			"category",
			"bu_category",
			"subcategory",
			"bu_subcategory",
			"attribute",
			"bu_attribute"
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

		try { 
			Class.forName("oracle.jdbc.driver.OracleDriver"); 
		} catch(ClassNotFoundException e) {
			System.out.println("Exception while loading oracle jdbc driver: " + e.getMessage());
		}

		String oracleURL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE_NAME;

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(oracleURL, USERNAME, PASSWORD);
		} catch (SQLException e) {
			System.out.println("Exception while establishing connection: " + e.getMessage());
		}

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

		System.out.println("Parsing " + args[0] + "... please wait.");
		
		URL path = Populate.class.getResource(args[0]);
		File json = new File(path.getFile());
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(json));
			String line = null;
			
			
			String sql1 = "INSERT INTO business VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			String sql2 = "INSERT INTO bu_category VALUES(?,?,?)";
			String sql3 = "INSERT INTO bu_subcategory VALUES(?,?,?)";
			String sql4 = "INSERT INTO bu_attribute VALUES(?,?,?,?)";
			PreparedStatement insertIntoBusiness = connection.prepareStatement(sql1);
			PreparedStatement insertIntoBuCategory = connection.prepareStatement(sql2);
			PreparedStatement insertIntoBuSubcategory = connection.prepareStatement(sql3);
			PreparedStatement insertIntoBuAttribute = connection.prepareStatement(sql4);

			int categoryId = 1;
			int subCategoryId = 1;
			int attrId = 1;
			while ((line = reader.readLine()) != null) {
				prepareInsertIntoBusiness(insertIntoBusiness, line);
				categoryId = prepareInsertIntoBuCategory(insertIntoBuCategory, line, categoryId);
				subCategoryId = prepareInsertIntoBuSubcategory(insertIntoBuSubcategory, line, subCategoryId);
				attrId = prepareInsertIntoBuAttribute(insertIntoBuAttribute, line, attrId);
			}
			
			int[] rs1 = insertIntoBusiness.executeBatch();
			insertIntoBusiness.close();
			System.out.println("Total rows business: " + rs1.length);
			
			int[] rs2 = insertIntoBuCategory.executeBatch();
			insertIntoBuCategory.close();
			System.out.println("Total rows bu_category: " + rs2.length);
			
			int[] rs3 = insertIntoBuSubcategory.executeBatch();
			insertIntoBuSubcategory.close();
			System.out.println("Total rows bu_subcategory: " + rs3.length);
			
			int[] rs4 = insertIntoBuAttribute.executeBatch();
			insertIntoBuAttribute.close();
			System.out.println("Total rows bu_attribute: " + rs4.length);
			
			String sql5 = "INSERT INTO category VALUES(?,?)";
			PreparedStatement insertIntoCategory = connection.prepareStatement(sql5);
			for (int i = 0; i < CATEGORIES.length; i++) {
				prepareInsertIntoCategory(insertIntoCategory, i + 1, CATEGORIES[i]);
			}

			int[] rs5 = insertIntoCategory.executeBatch();
			insertIntoCategory.close();
			System.out.println("Total rows category: " + rs5.length);
			
			String sql6 = "INSERT INTO subcategory VALUES(?,?)";
			PreparedStatement insertIntoSubcategory = connection.prepareStatement(sql6);
			Iterator<String> it = SUBCATEGORIES.iterator();
			int subcatId = 1;
			while (it.hasNext()) {			
				prepareInsertIntoSubcategory(insertIntoSubcategory, subcatId, it.next());
				subcatId += 1;
			}

			int[] rs6 = insertIntoSubcategory.executeBatch();
			insertIntoSubcategory.close();
			System.out.println("Total rows subcategory: " + rs6.length);
			
			String sql7 = "INSERT INTO attribute VALUES(?,?)";
			PreparedStatement insertIntoAttribute = connection.prepareStatement(sql7);
			it = ATTRIBUTES.iterator();
			int attId = 1;
			while (it.hasNext()) {			
				prepareInsertIntoAttribute(insertIntoAttribute, attId, it.next());
				attId += 1;
			}

			int[] rs7 = insertIntoAttribute.executeBatch();
			insertIntoAttribute.close();
			System.out.println("Total rows attribute: " + rs7.length);

		} catch (FileNotFoundException e) {
			System.out.println("File '" + path.getFile() + "' not found.");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Exception while trying to parse datasets: " + e.getMessage());
			System.exit(-1);
		} catch (SQLException e) {
			System.out.println("Exception while preparing statement: " + e.getMessage());
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
	
	private static void prepareInsertIntoBusiness(PreparedStatement stmt, String line) {

		JSONObject json = new JSONObject(line);
		
		String id = json.getString("business_id");
		String fullAddress = json.getString("full_address");
		boolean openBool = json.getBoolean("open");
		String open = (openBool ? "T" : "F");

		String city = json.getString("city");
		String state = json.getString("state");
		float latitude = json.getFloat("latitude");
		float longitude = json.getFloat("longitude");
		int reviewCount = json.getInt("review_count");
		String name = json.getString("name");
		double stars = json.getDouble("stars");

		String type = json.getString("type");

		try {
			stmt.clearParameters();
			stmt.setString(1, id);
			stmt.setString(2, name);
			stmt.setString(3, fullAddress);
			stmt.setString(4, city);
			stmt.setString(5, state);
			stmt.setFloat(6, latitude);
			stmt.setFloat(7, longitude);
			stmt.setString(8, open);
			stmt.setInt(9, reviewCount);
			stmt.setDouble(10, stars);
			stmt.setString(11, type);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO business: " + e.getMessage());
		}
				
	}
	
	private static int prepareInsertIntoBuCategory(PreparedStatement stmt, String line, int id) {
		
		int nextId = id;
		
		JSONObject json = new JSONObject(line);
		String buId = json.getString("business_id");
		JSONArray buCategories = json.getJSONArray("categories");
		
		for (int i = 0; i < buCategories.length(); i++) {
			for (int j = 0; j < CATEGORIES.length; j++) {
				if (buCategories.getString(i).equals(CATEGORIES[j])) {
					try {
						stmt.clearParameters();
						stmt.setInt(1, nextId);
						stmt.setString(2, buId);
						stmt.setString(3, buCategories.getString(i));
						stmt.addBatch();
					} catch (SQLException e) {
						System.out.println("Exception while creating prepared statement for INSERT INTO bu_category: " + e.getMessage());
					}
					nextId += 1;
				} else {
					SUBCATEGORIES.add(buCategories.getString(i));
				}
			}
		}
		
		return nextId;
	}
	
	private static int prepareInsertIntoBuSubcategory(PreparedStatement stmt, String line, int id) {
		
		int nextId = id;
		
		JSONObject json = new JSONObject(line);
		String buId = json.getString("business_id");
		JSONArray buCategories = json.getJSONArray("categories");
		
		for (int i = 0; i < buCategories.length(); i++) {
			boolean isACategory = false;
			for (int j = 0; j < CATEGORIES.length; j++) {
				if (buCategories.getString(i).equals(CATEGORIES[j])) {
					isACategory = true;
				}
			}
			if (!isACategory) {
				try {
					stmt.clearParameters();
					stmt.setInt(1, nextId);
					stmt.setString(2, buId);
					stmt.setString(3, buCategories.getString(i));
					stmt.addBatch();
				} catch (SQLException e) {
					System.out.println("Exception while creating prepared statement for INSERT INTO bu_subcategory: " + e.getMessage());
				}
				nextId += 1;
			}
		}
		
		return nextId;
	}
	
	private static int prepareInsertIntoBuAttribute(PreparedStatement stmt, String line, int id) {
		
		int nextId = id;
		
		JSONObject json = new JSONObject(line);
		String buId = json.getString("business_id");
		JSONObject attributes = json.getJSONObject("attributes");
		
		Iterator<String> iterator = attributes.keys();
		while (iterator.hasNext()) {
		    String key = iterator.next();
		    ATTRIBUTES.add(key);
		    Object value = attributes.get(key);
		    String v = "";
		    if (value instanceof String) {
		    		v = (String) value;
		    }
		    if (value instanceof Boolean) {
		    		if (attributes.getBoolean(key)) {
		    			v = "true";
		    		} else {
		    			v = "false";
		    		}
		    }
			try {
				stmt.clearParameters();
				stmt.setInt(1, nextId);
				stmt.setString(2, buId);
				stmt.setString(3, key);
				stmt.setString(4, v);
				stmt.addBatch();
			} catch (SQLException e) {
				System.out.println("Exception while creating prepared statement for INSERT INTO bu_attribute: " + e.getMessage());
			}
			nextId += 1;
		}
		
		return nextId;
	}
	
	private static void prepareInsertIntoCategory(PreparedStatement stmt, int id, String name) {
		
		try {
			stmt.clearParameters();
			stmt.setInt(1, id);
			stmt.setString(2, name);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO category: " + e.getMessage());
		}
		
	}
	
	private static void prepareInsertIntoSubcategory(PreparedStatement stmt, int id, String name) {
		
		try {
			stmt.clearParameters();
			stmt.setInt(1, id);
			stmt.setString(2, name);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO subcategory: " + e.getMessage());
		}
		
	}
	
	private static void prepareInsertIntoAttribute(PreparedStatement stmt, int id, String name) {
		
		try {
			stmt.clearParameters();
			stmt.setInt(1, id);
			stmt.setString(2, name);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO attribute: " + e.getMessage());
		}
		
	}

}
