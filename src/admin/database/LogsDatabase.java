
package admin.database;

import admin.models.AdminLogs;
import enums.ItemLogAction;
import enums.ReputationLogAction;
import enums.TransactionLogAction;
import enums.UserLogAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogsDatabase extends BaseDatabase {

    public List<AdminLogs> getLogsByType(String logType) {
        return getLogsByTypeFiltered(logType, "", "", null, null);
    }

    public List<AdminLogs> getLogsByTypeFiltered(
            String logType, String filter, String keyword,
            String dateFrom, String dateTo) {

        List<AdminLogs> list = new ArrayList<>();

        // ── Resolve table-specific settings ──────────────────────────────────
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
                System.err.println("getLogsByTypeFiltered(): unknown logType '" + logType + "'");
                return list;
        }

      
        String itemIdExpr = hasItemId ? "item_id" : "0 AS item_id";
        String createdExpr = (timestampCol != null)
                ? timestampCol + " AS created_at"
                : "CAST(NULL AS CHAR) AS created_at";

        String selectSql =
                "SELECT " + idCol + " AS id, "
                + "user_id, "
                + itemIdExpr + ", "
                + "action AS log_type, "
                + "reason AS description, "
                + createdExpr
                + " FROM " + table;

      
        List<String> conditions = new ArrayList<>();
        List<Object> params     = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();

            if ("All".equals(filter)) {
             
                String orExpr = buildAllColumnsOR(logType, hasItemId);
                conditions.add(orExpr);

                
                int placeholderCount = countPlaceholders(orExpr);
                for (int i = 0; i < placeholderCount; i++) {
                    params.add("%" + trimmedKeyword + "%");
                }
            } else {
                
                String searchCol = resolveSearchColumn(filter, logType, hasItemId);
                conditions.add(searchCol + " LIKE ?");
                params.add("%" + trimmedKeyword + "%");
            }
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

        if (timestampCol != null) {
            sql.append(" ORDER BY ").append(timestampCol).append(" DESC");
        } else {
            sql.append(" ORDER BY ").append(idCol).append(" ASC");
        }

     
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("getLogsByTypeFiltered() error [" + logType + "]: " + e.getMessage());
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
                System.err.println("countAllLogs() error on " + table + ": " + e.getMessage());
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
            System.err.println("countTransactions() error: " + e.getMessage());
        }
        return 0;
    }


   
    public boolean insertLog(UserLogAction action, int userId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();
        return insertUserLog(userId, action.getDbValue(), reason);
    }

    public boolean insertLog(ItemLogAction action, int userId, int itemId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();
        return insertItemLog(userId, itemId, action.getDbValue(), reason);
    }

    public boolean insertLog(TransactionLogAction action, int userId, int itemId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();
        return insertTransactionLog(userId, itemId, action.getDbValue(), reason);
    }

    public boolean insertLog(ReputationLogAction action, int userId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();
        return insertReputationLog(userId, action.getDbValue(), reason);
    }


   

    private boolean insertUserLog(int userId, String dbAction, String reason) {
        String firstName = userId > 0 ? lookupFirstName(userId) : "";
        String lastName  = userId > 0 ? lookupLastName(userId)  : "";

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
            stmt.setString(4, dbAction);
            stmt.setString(5, truncate(reason, 255));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertUserLog() failed: " + e.getMessage());
        }
        return false;
    }

    private boolean insertItemLog(int userId, int itemId, String dbAction, String reason) {
        String firstName = userId > 0 ? lookupFirstName(userId) : "";
        String lastName  = userId > 0 ? lookupLastName(userId)  : "";

        String sql =
            "INSERT INTO items_log "
            + "(user_id, item_id, initiator_firstname, initiator_lastname, action, reason) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId > 0) stmt.setInt(1, userId); else stmt.setNull(1, Types.INTEGER);
            if (itemId > 0) stmt.setInt(2, itemId); else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, dbAction);
            stmt.setString(6, truncate(reason, 255));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertItemLog() failed: " + e.getMessage());
        }
        return false;
    }

    private boolean insertTransactionLog(int userId, int itemId, String dbAction, String reason) {
        String sql =
            "INSERT INTO transaction_log "
            + "(user_id, item_id, action, reason) "
            + "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId > 0) stmt.setInt(1, userId); else stmt.setNull(1, Types.INTEGER);
            if (itemId > 0) stmt.setInt(2, itemId); else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, dbAction);
            stmt.setString(4, truncate(reason, 255));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertTransactionLog() failed: " + e.getMessage());
        }
        return false;
    }

    private boolean insertReputationLog(int userId, String dbAction, String reason) {
        String sql =
            "INSERT INTO reputation_log "
            + "(user_id, action, reason) "
            + "VALUES (?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId > 0) stmt.setInt(1, userId); else stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, dbAction);
            stmt.setString(3, truncate(reason, 255));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertReputationLog() failed: " + e.getMessage());
        }
        return false;
    }


  

    private String lookupFirstName(int userId) {
        return lookupNameField(userId, "first_name");
    }

    private String lookupLastName(int userId) {
        return lookupNameField(userId, "last_name");
    }

    private String lookupNameField(int userId, String field) {
        String fromUsers   = "SELECT " + field + " FROM users WHERE user_id = ? LIMIT 1";
        String fromArchive = "SELECT " + field + " FROM users_archive "
                           + "WHERE user_id = ? ORDER BY user_archive_id DESC LIMIT 1";

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


    private String resolveSearchColumn(String filter, String logType, boolean hasItemId) {
        if (filter == null || filter.isEmpty()) return "reason"; // default to description

        switch (filter) {
            case "All":         return buildAllColumnsOR(logType, hasItemId);
            case "ID":          return resolveIdColumn(logType);
            case "User ID":     return "user_id";
            case "Item ID":     return hasItemId ? "item_id" : "user_id"; // graceful fallback
            case "Action":      return "action";
            case "Description": return "reason";
            case "Date":
                return hasTimestamp(logType) ? "DATE(timestamp)" : resolveIdColumn(logType);
            default:
                return "reason";
        }
    }

   
    private String buildAllColumnsOR(String logType, boolean hasItemId) {
        switch (logType) {
            case "USER":
                return "("
                     + "CAST(log_id       AS CHAR) LIKE ? OR "
                     + "CAST(user_id      AS CHAR) LIKE ? OR "
                     + "action                     LIKE ? OR "
                     + "reason                     LIKE ?"
                     + ")";

            case "ITEM":
                return "("
                     + "CAST(log_id       AS CHAR) LIKE ? OR "
                     + "CAST(user_id      AS CHAR) LIKE ? OR "
                     + "CAST(item_id      AS CHAR) LIKE ? OR "
                     + "action                     LIKE ? OR "
                     + "reason                     LIKE ?"
                     + ")";

            case "TRANSACTION":
                return "("
                     + "CAST(transaction_id AS CHAR) LIKE ? OR "
                     + "CAST(user_id        AS CHAR) LIKE ? OR "
                     + "CAST(item_id        AS CHAR) LIKE ? OR "
                     + "action                       LIKE ? OR "
                     + "reason                       LIKE ?"
                     + ")";

            case "REPUTATION":
                return "("
                     + "CAST(reputation_id AS CHAR) LIKE ? OR "
                     + "CAST(user_id       AS CHAR) LIKE ? OR "
                     + "action                      LIKE ? OR "
                     + "reason                      LIKE ?"
                     + ")";

            default:
                
                return "reason LIKE ?";
        }
    }

    
    private int countPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') count++;
        }
        return count;
    }

   
    private String resolveIdColumn(String logType) {
        switch (logType) {
            case "TRANSACTION": return "transaction_id";
            case "REPUTATION":  return "reputation_id";
            default:            return "log_id";
        }
    }

    private boolean hasTimestamp(String logType) {
        return !"TRANSACTION".equals(logType);
    }

   
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

  
    private AdminLogs mapRow(ResultSet rs) throws SQLException {
        AdminLogs log = new AdminLogs();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setItemId(rs.getInt("item_id"));
        log.setLogType(rs.getString("log_type"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getString("created_at"));
        return log;
    }
}