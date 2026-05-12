package database;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:4306/HeronsResourceHub";
    private static final String USER = "root";
    private static final String PASS = "";
    private static Connection conn;
    
    public static Connection getConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection(URL, USER, PASS);
            } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return conn;
    }
    
}