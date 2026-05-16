package admin.database;

import admin.models.AdminLogs;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogsDatabase {

    public List<AdminLogs> getLogsByType(String logType) {
        return getLogsByTypeFiltered(logType, "", "", null, null);
    }

    public List<AdminLogs> getLogsByTypeFiltered(
            String logType, String filter, String keyword,
            String dateFrom, String dateTo) {

        List<AdminLogs> list = new ArrayList<>();

        String  table;
        String  idCol;
        String  timestampCol;
        boolean hasItemId;

        switch (logType) {
            case "USER":
                table        = "users_log";
                idCol        = "log_id";
                timestampCol = "timestamp";
                hasItemId    = false;
                break;
            case "ITEM":
                table        = "items_log";
                idCol        = "log_id";
                timestampCol = "timestamp";
                hasItemId    = true;
                break;
            case "TRANSACTION":
                table        = "transaction_log";
                idCol        = "transaction_id";
                timestampCol = null;
                hasItemId    = true;
                break;
            case "REPUTATION":
                table        = "reputation_log";
                idCol        = "reputation_id";
                timestampCol = "timestamp";
                hasItemId    = false;
                break;
            default:
                return list;
        }

        String itemIdExpr  = hasItemId ? "item_id" : "0 AS item_id";
        String createdExpr = (timestampCol != null)
                ? timestampCol + " AS created_at"
                : "CAST(NULL AS CHAR) AS created_at";

        String selectSql = "SELECT " + idCol + " AS id, user_id, " + itemIdExpr + ", "
                + "action AS log_type, reason AS description, " + createdExpr + " "
                + "FROM " + table;

        List<String> conditions = new ArrayList<>();
        List<Object> params     = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchCol = resolveLogColumn(filter, logType, hasItemId);
            conditions.add(searchCol + " LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }

        if (timestampCol != null) {
            if (dateFrom != null && !dateFrom.isEmpty()) {
                conditions.add(timestampCol + " >= ?");
                params.add(dateFrom + " 00:00:00");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                conditions.add(timestampCol + " <= ?");
                params.add(dateTo + " 23:59:59");
            }
        }

        StringBuilder sql = new StringBuilder(selectSql);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        sql.append(timestampCol != null
                ? " ORDER BY " + timestampCol + " DESC"
                : " ORDER BY " + idCol + " ASC");

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("getLogsByTypeFiltered() error [" + logType + "]: " + e.getMessage());
        }

        return list;
    }

    public int countAllLogs() {
        int total = 0;
        String[] tables = { "users_log", "items_log", "transaction_log", "reputation_log" };
        for (String table : tables) {
            String sql = "SELECT COUNT(*) FROM " + table;
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) total += rs.getInt(1);
            } catch (SQLException e) {
                System.out.println("countAllLogs() error on " + table + ": " + e.getMessage());
            }
        }
        return total;
    }

    public int countTransactions() {
        String sql = "SELECT COUNT(*) FROM transaction_log";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countTransactions() error: " + e.getMessage());
        }
        return 0;
    }

   
    public boolean insertLog(String logType, int userId, int itemId, String description) {

        if ("USER".equals(logType)) {
            // Resolve action enum from the description text
            String action = resolveUserAction(description);

            // Look up the user's name (try live table first, then archive)
            String firstName = "";
            String lastName  = "";
            if (userId > 0) {
                firstName = lookupFirstName(userId);
                lastName  = lookupLastName(userId);
            }

          
            String sql =
                "INSERT INTO users_log "
                + "(user_id, initiator_firstname, initiator_lastname, action, reason) "
                + "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (userId > 0) stmt.setInt(1, userId);
                else            stmt.setNull(1, Types.INTEGER);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, action);
                stmt.setString(5, truncate(description, 100));
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("insertLog(USER) failed: " + e.getMessage());
            }
            return false;

        } else if ("ITEM".equals(logType)) {
          
            String action = resolveItemAction(description);

            String firstName = "";
            String lastName  = "";
            if (userId > 0) {
                firstName = lookupFirstName(userId);
                lastName  = lookupLastName(userId);
            }

            String sql =
                "INSERT INTO items_log "
                + "(user_id, item_id, initiator_firstname, initiator_lastname, action, reason) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (userId > 0) stmt.setInt(1, userId);
                else            stmt.setNull(1, Types.INTEGER);
                if (itemId > 0) stmt.setInt(2, itemId);
                else            stmt.setNull(2, Types.INTEGER);
                stmt.setString(3, firstName);
                stmt.setString(4, lastName);
                stmt.setString(5, action);
                stmt.setString(6, truncate(description, 100));
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("insertLog(ITEM) failed: " + e.getMessage());
            }
            return false;

        } else {
            System.out.println("insertLog() skipped: unknown logType '" + logType + "'");
            return false;
        }
    }

   
    private String resolveUserAction(String description) {
        if (description == null) return "Update";
        String lower = description.toLowerCase();
        if (lower.contains("permanently deleted") || lower.contains("permanent delete")) return "Delete";
        if (lower.contains("restored")            || lower.contains("unarchive"))        return "Unarchive";
        if (lower.contains("archived")            || lower.contains("archive"))          return "Archive";
        if (lower.contains("added")               || lower.contains("registered")
                                                  || lower.contains("new user"))         return "Create";
        return "Update";
    }

    // Derives a readable action string for items_log (varchar — no strict enum)
    private String resolveItemAction(String description) {
        if (description == null) return "UPDATE_ITEM";
        String lower = description.toLowerCase();
        if (lower.contains("archived"))  return "ARCHIVE_ITEM";
        if (lower.contains("restored"))  return "RESTORE_ITEM";
        if (lower.contains("added")
                || lower.contains("new item")) return "LIST_ITEM";
        return "UPDATE_ITEM";
    }

    // Looks up first_name from users or users_archive
    private String lookupFirstName(int userId) {
        return lookupNameField(userId, "first_name");
    }

    private String lookupLastName(int userId) {
        return lookupNameField(userId, "last_name");
    }

    private String lookupNameField(int userId, String field) {
        String fromUsers   = "SELECT " + field + " FROM users WHERE user_id = ? LIMIT 1";
        String fromArchive = "SELECT " + field + " FROM users_archive WHERE user_id = ? "
                           + "ORDER BY user_archive_id DESC LIMIT 1";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(fromUsers)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String val = rs.getString(1);
                return val != null ? val : "";
            }
        } catch (SQLException ignored) {}
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(fromArchive)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String val = rs.getString(1);
                return val != null ? val : "";
            }
        } catch (SQLException ignored) {}
        return "";
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    private String resolveLogColumn(String filter, String logType, boolean hasItemId) {
        if (filter == null || filter.isEmpty()) return "reason";
        switch (filter) {
            case "ID":          return getIdColumn(logType);
            case "User ID":     return "user_id";
            case "Item ID":     return hasItemId ? "item_id" : "user_id";
            case "Action":      return "action";
            case "Description": return "reason";
            case "Date":        return hasTimestamp(logType) ? "DATE(timestamp)" : getIdColumn(logType);
            default:            return "reason";
        }
    }

    private String getIdColumn(String logType) {
        switch (logType) {
            case "TRANSACTION": return "transaction_id";
            case "REPUTATION":  return "reputation_id";
            default:            return "log_id";
        }
    }

    private boolean hasTimestamp(String logType) {
        return !"TRANSACTION".equals(logType);
    }

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(
                DatabaseManager.getURL(),
                DatabaseManager.getUser(),
                DatabaseManager.getPassword());
    }

    private AdminLogs mapRow(ResultSet rs) throws SQLException {
        AdminLogs log = new AdminLogs();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setLogType(rs.getString("log_type"));
        log.setItemId(rs.getInt("item_id"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getString("created_at"));
        return log;
    }
}