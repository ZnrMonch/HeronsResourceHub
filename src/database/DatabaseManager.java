package database;

public class DatabaseManager {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 13a4392 (add admin services and database integration for user and item management)
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
    
<<<<<<< HEAD
}
=======
	private static final String DB_URL = "jdbc:mysql://localhost:3306/infoman_cpg";
=======
	private static final String DB_URL = "jdbc:mysql://localhost:4306/heronsresourcehub";
>>>>>>> c724ee5 (refactor database connection methods to use getter methods for URL,)
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
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 0af9b3c (drafted changes for main system)
=======
	
>>>>>>> 556f0dc (sa log in and registration may changes ulit yan later)
=======
}
>>>>>>> 13a4392 (add admin services and database integration for user and item management)
=======
	
>>>>>>> c724ee5 (refactor database connection methods to use getter methods for URL,)
