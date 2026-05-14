package database;

public class DatabaseManager {
	public static final String URL = "jdbc:mysql://localhost:4306/HeronsResourceHub";
    public static final String USER = "root";
    public static final String PASS = "";
   
    
    public static String getUrl() {
    	return URL;
    }
    
    public static String getUsername() {
    	return USER;
    }
    
    public static String getPassword() {
    	return PASS;
    }
    
}