package database;

public class DatabaseManager {
	private static final String DB_URL = "jdbc:mysql://localhost:3306/heronsresourcehub";
	private static final String USER = "root";
	private static final String PASSWORD = "";
	
	public static String getURL() {
		return DB_URL;
	}
	
	public static String getUser() {
		return USER;
	}
	
	public static String getPassword() {
		return PASSWORD;
	}
}
	