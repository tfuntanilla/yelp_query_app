package hw3;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserParser {

	public static void parse(Connection connection, BufferedReader reader) {

		try {
			
			String sql1 = "INSERT INTO yelp_user VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String sql2 = "INSERT INTO friend VALUES(?,?,?)";
			String sql3 = "INSERT INTO elite_years VALUES(?,?,?)";
			
			PreparedStatement insertIntoYelpUser = connection.prepareStatement(sql1);
			PreparedStatement insertIntoFriend = connection.prepareStatement(sql2);
			PreparedStatement insertIntoEliteYears = connection.prepareStatement(sql3);
			
			int friendId = 1;
			int eyId = 1;
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				prepareInsertIntoYelpUser(insertIntoYelpUser, line);
				friendId = prepareInsertIntoFriend(insertIntoFriend, line, friendId);
				eyId = prepareInsertIntoEliteYears(insertIntoEliteYears, line, eyId);
			}

			int[] rs1 = insertIntoYelpUser.executeBatch();
			insertIntoYelpUser.close();
			System.out.println("Total rows inserted in table yelp_user: " + rs1.length);

			int[] rs2 = insertIntoFriend.executeBatch();
			insertIntoFriend.close();
			System.out.println("Total rows inserted in table friend: " + rs2.length);

			int[] rs3 = insertIntoEliteYears.executeBatch();
			insertIntoEliteYears.close();
			System.out.println("Total rows inserted in table elite_years: " + rs3.length);

		} catch(SQLException e) {
			System.out.println("Exception while creating PreparedStatement in parsing yelp_user.json: " + e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Exception while parsing yelp_user.json: " + e.getMessage());
			System.exit(-1);
		}

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
			System.out.println("Exception while creating PreparedStatement for INSERT INTO yelp_user: " + e.getMessage());
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
				System.out.println("Exception while creating PreparedStatement for INSERT INTO friend: " + e.getMessage());
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
				System.out.println("Exception while creating PreparedStatement for INSERT INTO elite_years: " + e.getMessage());
			}
			nextId += 1;
		}

		return nextId;

	}

}
