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

	private static Map<String, TreeSet<String>> CAT_TO_SUBCAT = new HashMap<String, TreeSet<String>>();
	private static Set<String> SUBCATEGORIES = new TreeSet<String>();
	private static Set<String> ATTRIBUTES = new TreeSet<String>();

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
			System.out.println("Total rows inserted in table subcategory: " + rs6.length);

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
			System.out.println("Total rows inserted in table attribute: " + rs7.length);

			String sql8 = "INSERT INTO cat_to_subcat VALUES(?,?,?)";
			PreparedStatement insertIntoCatToSubcat = connection.prepareStatement(sql8);
			int id = 1;
			id = prepareInsertIntoCatToSubcat(insertIntoCatToSubcat, line, id);
			id += 1;

			int[] rs8 = insertIntoCatToSubcat.executeBatch();
			insertIntoCatToSubcat.close();
			System.out.println("Total rows inserted in table cat_to_subcat: " + rs8.length);
			
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

		Set<String> categories = new TreeSet<String>();
		Set<String> subcategories = new TreeSet<String>();
		for (int i = 0; i < buCategories.length(); i++) {
			boolean isACategory = false;
			for (int j = 0; j < Constants.CATEGORIES.length; j++) {
				if (buCategories.getString(i).equals(Constants.CATEGORIES[j])) {
					categories.add(buCategories.getString(i));
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

		Iterator<String> it = categories.iterator();
		while (it.hasNext()) {
			CAT_TO_SUBCAT.get(it.next()).addAll(subcategories);
		}


		return nextId;
	}

	private static int prepareInsertIntoCatToSubcat(PreparedStatement stmt, String line, int id) {

		int nextId = id;
		System.out.println(CAT_TO_SUBCAT.size());
		for(Map.Entry<String, TreeSet<String>> entry : CAT_TO_SUBCAT.entrySet()) {
			String category = entry.getKey();
			Set<String> subcategories = entry.getValue();
			Iterator<String> s = subcategories.iterator();
			while(s.hasNext()) {
				String sub = s.next();
				try {
					stmt.clearParameters();
					stmt.setInt(1, nextId);
					stmt.setString(2, category);
					stmt.setString(3, sub);
					stmt.addBatch();
				} catch (SQLException e) {
					System.out.println("Exception while creating PreparedStatement for INSERT INTO cat_to_subcat: " + e.getMessage());
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

	private static void prepareInsertIntoSubcategory(PreparedStatement stmt, int id, String name) {

		try {
			stmt.clearParameters();
			stmt.setInt(1, id);
			stmt.setString(2, name);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating PreparedStatement for INSERT INTO subcategory: " + e.getMessage());
		}

	}

	private static void prepareInsertIntoAttribute(PreparedStatement stmt, int id, String name) {

		try {
			stmt.clearParameters();
			stmt.setInt(1, id);
			stmt.setString(2, name);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating PreparedStatement for INSERT INTO attribute: " + e.getMessage());
		}

	}

}
