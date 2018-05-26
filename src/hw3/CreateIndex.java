package hw3;

import java.sql.*;

public class CreateIndex {

    public static void createIndex(Connection connection) {

        try {

            Statement stmt = connection.createStatement();

            String sql1 = "CREATE INDEX bu_cat_idx ON bu_category(bu_cat_id, bu_id, category)";
            stmt.executeUpdate(sql1);
            System.out.println("Created bu_cat_idx on bu_category.");

            String sql2 = "CREATE INDEX bu_subcat_idx ON bu_subcategory (bu_subcat_id, bu_id, subcategory)";
            stmt.executeUpdate(sql2);
            System.out.println("Created bu_subcat_idx on bu_subcategory.");

            String sql3 = "CREATE INDEX bu_attr_idx ON bu_attribute (bu_attr_id, bu_id, attr_name, attr_value)";
            stmt.executeUpdate(sql3);
            System.out.println("Created bu_attr_idx on bu_attribute.");

            String sql4 = "CREATE INDEX yelp_user_idx ON yelp_user " +
                    "(user_id, user_name, yelping_since, review_count, average_stars)";
            stmt.executeUpdate(sql4);
            System.out.println("Created yelp_user_idx on yelp_user.");

            String sql5 = "CREATE INDEX review_idx ON review " +
                    "(rev_id, bu_id, user_id, stars, review_date)";
            stmt.executeUpdate(sql5);
            System.out.println("Created review_idx on review.");

            stmt.close();

            System.out.println("Done indexing.");
        } catch(SQLException e) {
            e.printStackTrace();
        }

    }

}
