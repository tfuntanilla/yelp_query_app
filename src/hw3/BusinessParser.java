package hw3;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class BusinessParser {

	public static void parse(Connection connection, BufferedReader reader) {

		try {
			
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

			String line = null;
			while ((line = reader.readLine()) != null) {
				prepareInsertIntoBusiness(insertIntoBusiness, line);
				categoryId = prepareInsertIntoBuCategory(insertIntoBuCategory, line, categoryId);
				subCategoryId = prepareInsertIntoBuSubcategory(insertIntoBuSubcategory, line, subCategoryId);
				attrId = prepareInsertIntoBuAttribute(insertIntoBuAttribute, line, attrId);
			}

			int[] rs1 = insertIntoBusiness.executeBatch();
			insertIntoBusiness.close();
			System.out.println("Total rows inserted in table business: " + rs1.length);

			int[] rs2 = insertIntoBuCategory.executeBatch();
			insertIntoBuCategory.close();
			System.out.println("Total rows inserted in table bu_category: " + rs2.length);

			int[] rs3 = insertIntoBuSubcategory.executeBatch();
			insertIntoBuSubcategory.close();
			System.out.println("Total rows inserted in table bu_subcategory: " + rs3.length);

			int[] rs4 = insertIntoBuAttribute.executeBatch();
			insertIntoBuAttribute.close();
			System.out.println("Total rows inserted in table bu_attribute: " + rs4.length);

			String sql5 = "INSERT INTO category VALUES(?,?)";
			PreparedStatement insertIntoCategory = connection.prepareStatement(sql5);
			for (int i = 0; i < Constants.CATEGORIES.length; i++) {
				prepareInsertIntoCategory(insertIntoCategory, i + 1, Constants.CATEGORIES[i]);
			}

			int[] rs5 = insertIntoCategory.executeBatch();
			insertIntoCategory.close();
			System.out.println("Total rows inserted in table category: " + rs5.length);
			
		} catch(SQLException e) {
			System.out.println("Exception while creating PreparedStatement in parsing yelp_business.json: " + e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Exception while parsing yelp_business.json: " + e.getMessage());
			System.exit(-1);
		}

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
			System.out.println("Exception while creating PreparedStatement for INSERT INTO business: " + e.getMessage());
		}

	}

	private static int prepareInsertIntoBuCategory(PreparedStatement stmt, String line, int id) {

		int nextId = id;

		JSONObject json = new JSONObject(line);
		String buId = json.getString("business_id");
		JSONArray buCategories = json.getJSONArray("categories");

		for (int i = 0; i < buCategories.length(); i++) {
			for (int j = 0; j < Constants.CATEGORIES.length; j++) {
				if (buCategories.getString(i).equals(Constants.CATEGORIES[j])) {
					try {
						stmt.clearParameters();
						stmt.setInt(1, nextId);
						stmt.setString(2, buId);
						stmt.setString(3, buCategories.getString(i));
						stmt.addBatch();
					} catch (SQLException e) {
						System.out.println("Exception while creating PreparedStatement for INSERT INTO bu_category: " + e.getMessage());
					}
					nextId += 1;
				} else {
//					SUBCATEGORIES.add(buCategories.getString(i));
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
			for (int j = 0; j < Constants.CATEGORIES.length; j++) {
				if (buCategories.getString(i).equals(Constants.CATEGORIES[j])) {
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
					System.out.println("Exception while creating PreparedStatement for INSERT INTO bu_subcategory: " + e.getMessage());
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
			Object value = attributes.get(key);
			String v = "";
			if (value instanceof String) {
				v = (String) value;
			} else if (value instanceof Boolean) {
				if (attributes.getBoolean(key)) {
					v = "true";
				} else {
					v = "false";
				}
			} else if (value instanceof Integer) {
				v = String.valueOf(value);
			} else {
				JSONObject innerAttr = json.getJSONObject("attributes").getJSONObject(key);
				Iterator<String> innerIt = innerAttr.keys();
				while (innerIt.hasNext()) {
					String innerKey = innerIt.next();
					String insertKey = key + ": " + innerKey;
					value = innerAttr.get(innerKey);
					v = "";
					if (value instanceof String) {
						v = (String) value;
					} else if (value instanceof Boolean) {
						if (innerAttr.getBoolean(innerKey)) {
							v = "true";
						} else {
							v = "false";
						}
					} else if (value instanceof Integer) {
						v = String.valueOf(value);
					}
					try {
						stmt.clearParameters();
						stmt.setInt(1, nextId);
						stmt.setString(2, buId);
						stmt.setString(3, insertKey);
						stmt.setString(4, v);
						stmt.addBatch();
					} catch (SQLException e) {
						System.out.println("Exception while creating PreparedStatement for INSERT INTO bu_attribute: " + e.getMessage());
					}
					nextId += 1;
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
				System.out.println("Exception while creating PreparedStatement for INSERT INTO bu_attribute: " + e.getMessage());
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
			System.out.println("Exception while creating PreparedStatement for INSERT INTO category: " + e.getMessage());
		}

	}

}
