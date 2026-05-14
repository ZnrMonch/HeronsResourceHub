package admin.database;

import admin.models.AdminLogs;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogsDatabase {


    public List<AdminLogs> getLogsByType(String logType) {
        List<AdminLogs> list = new ArrayList<>();
        String sql;

        switch (logType) {
            case "USER":
                sql = "SELECT log_id AS id, user_id, 0 AS item_id, action AS log_type, " +
                      "reason AS description, timestamp AS created_at FROM users_log " +
                      "ORDER BY item_id ASC";
                break;
            case "ITEM":
                sql = "SELECT log_id AS id, user_id, item_id, action AS log_type, " +
                      "reason AS description, timestamp AS created_at FROM items_log " +
                      "ORDER BY timestamp DESC";
                break;
            case "TRANSACTION":
                sql = "SELECT transaction_id AS id, user_id, item_id, action AS log_type, " +
                      "reason AS description, NULL AS created_at FROM transaction_log " +
                      "ORDER BY transaction_id ASC";
                break;
            case "REPUTATION":
                sql = "SELECT reputation_id AS id, user_id, 0 AS item_id, action AS log_type, " +
                      "reason AS description, timestamp AS created_at FROM reputation_log " +
                      "ORDER BY timestamp DESC";
                break;
            default:
                return list; 
        }

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("LogDatabase.getLogsByType() error: " + e.getMessage());
        }

        return list;
    }

   
    public int countAllLogs() {
        int total = 0;
        String[] tables = {"users_log", "items_log", "transaction_log", "reputation_log"};

        for (String table : tables) {
            String sql = "SELECT COUNT(*) FROM " + table;
            try (
                Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
            ) {
                if (rs.next()) total += rs.getInt(1);
            } catch (SQLException e) {
                System.out.println("countAllLogs() error on " + table + ": " + e.getMessage());
            }
        }

        return total;
    }


    public int countTransactions() {
        String sql = "SELECT COUNT(*) FROM transaction_log";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countTransactions() error: " + e.getMessage());
        }
        return 0;
    }


    public boolean insertLog(String logType, int userId, int itemId, String description) {
        return true; 
    }

    private AdminLogs mapRow(ResultSet rs) throws SQLException {
        AdminLogs log = new AdminLogs();
        log.setId(rs.getInt("id"));
        log.setLogType(rs.getString("log_type"));
        log.setUserId(rs.getInt("user_id"));
        log.setItemId(rs.getInt("item_id"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getString("created_at"));
        return log;
    }
}