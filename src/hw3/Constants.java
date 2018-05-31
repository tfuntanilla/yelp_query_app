package hw3;

public class Constants {
	
	public static final String HOST = "localhost";
	public static final String SERVICE_NAME = "orcl";
	public static final int PORT = 1521;
	
	public static final String USERNAME = "hr";
	public static final String PASSWORD = "oracle";
	public static final String ORACLE_URL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SERVICE_NAME;
	
	public static final String[] TABLES = {
			"review",
			"yelp_user",
			"bu_attribute",
			"bu_subcategory",
			"bu_category",
			"category",
			"business"

	};
	
	public static final String[] CATEGORIES = {
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
	
}
