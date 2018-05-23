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

import org.json.JSONObject;

public class InsertReview {
	
	private static final String HOST = "localhost";
	private static final String SERVICE_NAME = "orcl";
	private static final String USERNAME = "hr";
	private static final String PASSWORD = "oracle";
	private static final int PORT = 1521;
	private static final String[] TABLES = {
			"review"
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
						
			String sql1 = "INSERT INTO review VALUES(?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement insertIntoReview = connection.prepareStatement(sql1);

			int totalRows = 0; int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				prepareInsertIntoReview(insertIntoReview, line);
				lineNum += 1;
				if (lineNum % 50000 == 0) {
					int rs[] = insertIntoReview.executeBatch();
					totalRows += rs.length;
					insertIntoReview.clearBatch();
					lineNum = 0;
				}
			}
			
			if (lineNum > 0) {
				int rs[] = insertIntoReview.executeBatch();
				totalRows += rs.length;
			}
			
			insertIntoReview.close();
			System.out.println("Total rows review: " + totalRows);

		} catch (FileNotFoundException e) {
			System.out.println("File '" + path.getFile() + "' not found.");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Exception while trying to parse datasets: " + e.getMessage());
			System.exit(-1);
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	private static void prepareInsertIntoReview(PreparedStatement stmt, String line) {

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

		try {
			stmt.clearParameters();
			stmt.setString(1, id);
			stmt.setString(2, buId);
			stmt.setString(3, userId);
			stmt.setInt(4, stars);
			stmt.setDate(5, date);
			stmt.setString(6, text);
			stmt.setInt(7, useful);
			stmt.setInt(8, funny);
			stmt.setInt(9, cool);
			stmt.setString(10, type);
			stmt.addBatch();
		} catch (SQLException e) {
			System.out.println("Exception while creating prepared statement for INSERT INTO review: " + e.getMessage());
		}
				
	}

}
