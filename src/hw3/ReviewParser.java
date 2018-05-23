package hw3;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

public class ReviewParser {

	public static void parse(Connection connection, BufferedReader reader) {

		try {

			String sql1 = "INSERT INTO review VALUES(?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement insertIntoReview = connection.prepareStatement(sql1);

			int totalRows = 0; int lineNum = 0;

			String line = null;
			while ((line = reader.readLine()) != null) {
				prepareInsertIntoReview(insertIntoReview, line);
				lineNum += 1;
				if (lineNum % 75000 == 0) {
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
			System.out.println("Total rows inserted in table review: " + totalRows);


		} catch(SQLException e) {
			System.out.println("Exception while creating PreparedStatement in parsing yelp_user.json: " + e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Exception while parsing yelp_user.json: " + e.getMessage());
			System.exit(-1);
		}

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
			System.out.println("Exception while creating PreparedStatement for INSERT INTO review: " + e.getMessage());
		}

	}

}
