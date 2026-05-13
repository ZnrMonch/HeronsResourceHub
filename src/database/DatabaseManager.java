package database;

public class DatabaseManager {
<<<<<<< HEAD
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
=======
	private static final String DB_URL = "jdbc:mysql://localhost:3306/infoman_cpg";
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
>>>>>>> 0af9b3c (drafted changes for main system)
