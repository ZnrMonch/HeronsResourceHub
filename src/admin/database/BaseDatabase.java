package admin.database;

import database.DatabaseManager;

import java.sql.*;

public class BaseDatabase {

    protected Connection getConn() throws SQLException {
        return DriverManager.getConnection(
            DatabaseManager.getURL(),
            DatabaseManager.getUser(),
            DatabaseManager.getPassword()
        );
    }

    protected void exec(Connection conn, String sql, int param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            ps.executeUpdate();
        }
    }
}