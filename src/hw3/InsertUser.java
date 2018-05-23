package hw3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

public class InsertUser {
	
	private static final String HOST = "localhost";
	private static final String SERVICE_NAME = "orcl";
	private static final String USERNAME = "hr";
	private static final String PASSWORD = "oracle";
	private static final int PORT = 1521;
	private static final String[] TABLES = {
			"yelp_user",
			"friend",
			"elite_years"
	};
	
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
						
			String sql1 = "INSERT INTO yelp_user VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String sql2 = "INSERT INTO friend VALUES(?,?,?)";
			String sql3 = "INSERT INTO elite_years VALUES(?,?,?)";
			PreparedStatement insertIntoYelpUser = connection.prepareStatement(sql1);
			PreparedStatement insertIntoFriend = connection.prepareStatement(sql2);
			PreparedStatement insertIntoEliteYears = connection.prepareStatement(sql3);
			int friendId = 1;
			int eyId = 1;
			while ((line = reader.readLine()) != null) {
				prepareInsertIntoYelpUser(insertIntoYelpUser, line);
				friendId = prepareInsertIntoFriend(insertIntoFriend, line, friendId);
				eyId = prepareInsertIntoEliteYears(insertIntoEliteYears, line, eyId);
			}
			
			int[] rs1 = insertIntoYelpUser.executeBatch();
			insertIntoYelpUser.close();
			System.out.println("Total rows yelp_user: " + rs1.length);
			
			int[] rs2 = insertIntoFriend.executeBatch();
			insertIntoFriend.close();
			System.out.println("Total rows friend: " + rs2.length);
			
			int[] rs3 = insertIntoEliteYears.executeBatch();
			insertIntoEliteYears.close();
			System.out.println("Total rows elite_years: " + rs3.length);

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
	
	private static void prepareInsertIntoYelpUser(PreparedStatement stmt, String line) {
		
		JSONObject json = new JSONObject(line);

		String id = json.getString("user_id");
		String name = json.getString("name");
		
		String yelpingSince = json.getString("yelping_since");
		Date date = null;
		try {
			date = new Date(new SimpleDateFormat("yyyy-MM").parse(yelpingSince).getTime());
		} catch (ParseException e1) {
			System.out.println("Exception while parsing review date: " + e1.getMessage());
		}
		
		int reviewCount = json.getInt("review_count");
		
		JSONObject votes = json.getJSONObject("votes");
		int useful = votes.getInt("useful");
		int funny = votes.getInt("funny");
		int cool = votes.getInt("cool");
		
		int fans = json.getInt("fans");	
		float averageStars = json.getFloat("average_stars");
		
		JSONObject compliments = json.getJSONObject("compliments");
		String type = json.getString("type");
		
		try {
			stmt.clearParameters();
			stmt.setString(1, id);
			stmt.setString(2, name);
			stmt.setDate(3, date);
			stmt.setInt(4, reviewCount);
			stmt.setInt(5, useful);
			stmt.setInt(6, funny);
			stmt.setInt(7, cool);
			stmt.setInt(8, fans);
			stmt.setFloat(9, averageStars);
			if (compliments.has("hot")) {
				stmt.setInt(10, compliments.getInt("hot"));
			} else {
				stmt.setNull(10, java.sql.Types.INTEGER);
			}
			if (compliments.has("more")) {
				stmt.setInt(11, compliments.getInt("more"));
			} else {
				stmt.setNull(11, java.sql.Types.INTEGER);
			}			
			if (compliments.has("profile")) {
				stmt.setInt(12, compliments.getInt("profile"));
			} else {
				stmt.setNull(12, java.sql.Types.INTEGER);
			}			
			if (compliments.has("cute")) {
				stmt.setInt(13, compliments.getInt("cute"));
			} else {
				stmt.setNull(13, java.sql.Types.INTEGER);
			}
			if (compliments.has("list")) {
				stmt.setInt(14, compliments.getInt("list"));
			} else {
				stmt.setNull(14, java.sql.Types.INTEGER);
			}
			if (compliments.has("note")) {
				stmt.setInt(15, compliments.getInt("note"));
			} else {
				stmt.setNull(15, java.sql.Types.INTEGER);
			}
			if (compliments.has("plain")) {
				stmt.setInt(16, compliments.getInt("plain"));
			} else {
				stmt.setNull(16, java.sql.Types.INTEGER);
			}
			if (compliments.has("cool")) {
				stmt.setInt(17, compliments.getInt("cool"));
			} else {
				stmt.setNull(17, java.sql.Types.INTEGER);
			}
			if (compliments.has("funny")) {
				stmt.setInt(18, compliments.getInt("funny"));
			} else {
				stmt.setNull(18, java.sql.Types.INTEGER);
			}
			if (compliments.has("writer")) {
				stmt.setInt(19, compliments.getInt("writer"));
			} else {
				stmt.setNull(19, java.sql.Types.INTEGER);
			}
			if (compliments.has("photos")) {
				stmt.setInt(20, compliments.getInt("photos"));
			} else {
				stmt.setNull(20, java.sql.Types.INTEGER);
			}
			stmt.setString(21, type);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO yelp_user: " + e.getMessage());
		}
		
	}
	
	private static int prepareInsertIntoFriend(PreparedStatement stmt, String line, int id) {
		
		int nextId = id;
		
		JSONObject json = new JSONObject(line);
		String userId = json.getString("user_id");
		JSONArray friends = json.getJSONArray("friends");
		
		for (int i = 0; i < friends.length(); i++) {
			try {
				stmt.clearParameters();
				stmt.setInt(1, nextId);
				stmt.setString(2, userId);
				stmt.setString(3, friends.getString(i));
				stmt.addBatch();
			} catch (SQLException e) {
				System.out.println("Exception while creating prepared statement for INSERT INTO friend: " + e.getMessage());
			}
			nextId += 1;
		}
		
		return nextId;
	}
	
	private static int prepareInsertIntoEliteYears(PreparedStatement stmt, String line, int id) {
		
		int nextId = id;
		
		JSONObject json = new JSONObject(line);
		String userId = json.getString("user_id");
		JSONArray elite = json.getJSONArray("elite");
		
		for (int i = 0; i < elite.length(); i++) {
			try {
				stmt.clearParameters();
				stmt.setInt(1, nextId);
				stmt.setString(2, userId);
				stmt.setString(3, String.valueOf(elite.getInt(i)));
				stmt.addBatch();
			} catch (SQLException e) {
				System.out.println("Exception while creating prepared statement for INSERT INTO elite_years: " + e.getMessage());
			}
			nextId += 1;
		}
		
		return nextId;

	}

}
