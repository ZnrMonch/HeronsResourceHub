package admin.database;

import admin.models.*;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This class is the ONLY place that runs SQL for the admin_logs table.

public class LogsDatabase {

    // Returns all logs of a specific type, newest first
    // logType can be: "USER", "ITEM", "TRANSACTION", "REPUTATION"
    public List<AdminLog> getLogsByType(String logType) {
        List<AdminLog> list = new ArrayList<>();
        String sql = "SELECT * FROM admin_logs WHERE log_type = ? AND archived = 0 ORDER BY created_at DESC";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, logType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("LogDatabase.getLogsByType() error: " + e.getMessage());
        }

        return list;
    }

    // Returns the total count of all logs (used for the status card)
    public int countAllLogs() {
        String sql = "SELECT COUNT(*) FROM admin_logs WHERE archived = 0";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("LogDatabase.countAllLogs() error: " + e.getMessage());
        }

        return 0;
    }

    // Returns the total count of all transactions (used for the status card)
    public int countTransactions() {
        String sql = "SELECT COUNT(*) FROM transaction_log";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("LogDatabase.countTransactions() error: " + e.getMessage());
        }

        return 0;
    }

    // Saves a new log entry into admin_logs
    // Pass 0 for userId or itemId if they are not relevant to this log
    public boolean insertLog(String logType, int userId, int itemId, String description) {
        String sql = "INSERT INTO admin_logs (log_type, user_id, item_id, description) VALUES (?, ?, ?, ?)";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, logType);

            // Store NULL in DB if userId is not applicable
            if (userId == 0) stmt.setNull(2, Types.INTEGER);
            else             stmt.setInt(2, userId);

            // Store NULL in DB if itemId is not applicable
            if (itemId == 0) stmt.setNull(3, Types.INTEGER);
            else             stmt.setInt(3, itemId);

            stmt.setString(4, description);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("LogDatabase.insertLog() error: " + e.getMessage());
        }

        return false;
    }

    // Converts one database row into an AdminLog object
    private AdminLog mapRow(ResultSet rs) throws SQLException {
        AdminLog log = new AdminLog();
        log.setId(rs.getInt("id"));
        log.setLogType(rs.getString("log_type"));
        log.setUserId(rs.getInt("user_id"));
        log.setItemId(rs.getInt("item_id"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getString("created_at"));
        log.setArchived(rs.getBoolean("archived"));
        return log;
    }
}