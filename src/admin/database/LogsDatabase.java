package admin.database;

import admin.models.AdminLogs;
import enums.ItemLogAction;
import enums.ReputationLogAction;
import enums.TransactionLogAction;
import enums.UserLogAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Handles all read and write operations across the four log tables
public class LogsDatabase extends BaseDatabase {

 // ── Read ──

 public List<AdminLogs> getLogsByType(String logType) {
     return getLogsByTypeFiltered(logType, "", "", null, null);
 }

 // Builds the full query dynamically: table selection, WHERE clauses, and date range
 public List<AdminLogs> getLogsByTypeFiltered(
         String logType, String filter, String keyword,
         String dateFrom, String dateTo) {

     List<AdminLogs> list = new ArrayList<>();

     String selectSql;
     // Each log table uses a different alias for its timestamp column
     String timestampCol;

     switch (logType) {
         case "USER":
             selectSql =
                 "SELECT ul.log_id AS id, "
                 + "ul.user_id, "
                 + "0 AS item_id, "
                 + "ul.action AS log_type, "
                 + "CONCAT('\"', "
                 +   "COALESCE(u.first_name, 'Unknown'), ' ', "
                 +   "COALESCE(u.last_name, ''), '\": ', "
                 +   "CASE ul.action "
                 +     "WHEN 'Create'   THEN 'User account created' "
                 +     "WHEN 'Update'   THEN 'User account updated' "
                 +     "WHEN 'Archive'  THEN 'User archived' "
                 +     "WHEN 'Retrieve' THEN 'User restored from archive' "
                 +     "WHEN 'Delete'   THEN 'User permanently deleted' "
                 +     "ELSE ul.action "
                 +   "END"
                 + ") AS description, "
                 + "ul.timestamp AS created_at "
                 + "FROM users_log ul "
                 // LEFT JOIN so logs for deleted users still appear as Unknown
                 + "LEFT JOIN users u ON ul.user_id = u.user_id";
             timestampCol = "ul.timestamp";
             break;

         case "ITEM":
             selectSql =
                 "SELECT il.log_id AS id, "
                 + "0 AS user_id, "
                 + "il.item_id, "
                 + "il.action AS log_type, "
                 // Prefer stored reason over a generated description when available
                 + "CASE "
                 +   "WHEN il.reason IS NOT NULL AND il.reason != '' "
                 +     "THEN il.reason "
                 +   "ELSE CONCAT('\"', COALESCE(it.item_name, 'Unknown Item'), '\": ', "
                 +     "CASE il.action "
                 +       "WHEN 'Create'   THEN 'Item created' "
                 +       "WHEN 'Update'   THEN 'Item updated' "
                 +       "WHEN 'Archive'  THEN 'Item archived' "
                 +       "WHEN 'Retrieve' THEN 'Item restored from archive' "
                 +       "WHEN 'Delete'   THEN 'Item permanently deleted' "
                 +       "ELSE il.action "
                 +     "END) "
                 + "END AS description, "
                 + "il.timestamp AS created_at "
                 + "FROM items_log il "
                 + "LEFT JOIN items it ON il.item_id = it.item_id";
             timestampCol = "il.timestamp";
             break;

         case "TRANSACTION":
             selectSql =
                 "SELECT t.transaction_id AS id, "
                 + "t.borrower_id AS user_id, "
                 + "t.item_id, "
                 + "t.action AS log_type, "
                 + "CONCAT('\"', COALESCE(it.item_name, 'Unknown Item'), '\": ', "
                 +   "COALESCE(u.first_name, 'Unknown'), ' ', "
                 +   "COALESCE(u.last_name, ''), ' \u2014 ', "
                 +   "t.action"
                 + ") AS description, "
                 + "t.timestamp AS created_at "
                 + "FROM transactions t "
                 + "LEFT JOIN items it ON t.item_id = it.item_id "
                 + "LEFT JOIN users u  ON t.borrower_id = u.user_id";
             timestampCol = "t.timestamp";
             break;

         case "REPUTATION":
             selectSql =
                 "SELECT rl.reputation_id AS id, "
                 + "rl.user_id, "
                 + "0 AS item_id, "
                 + "CONCAT('+', COALESCE(rl.points_earned, 0), ' pts') AS log_type, "
                 + "CONCAT('\"', COALESCE(u.first_name, 'Unknown'), ' ', "
                 +   "COALESCE(u.last_name, ''), '\": ', "
                 +   "'+', COALESCE(rl.points_earned, 0), ' karma pts earned \u2014 ', "
                 +   "COALESCE(t.action, 'No transaction')"
                 + ") AS description, "
                 + "rl.timestamp AS created_at "
                 + "FROM reputation_log rl "
                 + "LEFT JOIN users u        ON rl.user_id = u.user_id "
                 + "LEFT JOIN transactions t ON rl.transaction_id = t.transaction_id";
             timestampCol = "rl.timestamp";
             break;

         default:
             System.err.println("getLogsByTypeFiltered(): unknown logType '" + logType + "'");
             return list;
     }

     List<String> conditions = new ArrayList<>();
     List<Object> params     = new ArrayList<>();

     if (keyword != null && !keyword.trim().isEmpty()) {
         String trimmedKeyword = keyword.trim();
         if ("All".equals(filter)) {
             String orExpr = buildAllColumnsOR(logType);
             conditions.add(orExpr);
             // Bind one param per ? in the generated OR expression
             int placeholderCount = countPlaceholders(orExpr);
             for (int i = 0; i < placeholderCount; i++)
                 params.add("%" + trimmedKeyword + "%");
         } else {
             conditions.add(resolveSearchColumn(filter, logType) + " LIKE ?");
             params.add("%" + trimmedKeyword + "%");
         }
     }

     // Timestamp bounds use the aliased column to avoid ambiguity across joined tables
     if (dateFrom != null && !dateFrom.isEmpty()) {
         conditions.add(timestampCol + " >= ?");
         params.add(dateFrom + " 00:00:00");
     }
     if (dateTo != null && !dateTo.isEmpty()) {
         conditions.add(timestampCol + " <= ?");
         params.add(dateTo + " 23:59:59");
     }

     StringBuilder sql = new StringBuilder(selectSql);
     if (!conditions.isEmpty())
         sql.append(" WHERE ").append(String.join(" AND ", conditions));
     sql.append(" ORDER BY ").append(timestampCol).append(" DESC");

     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
         for (int i = 0; i < params.size(); i++)
             stmt.setObject(i + 1, params.get(i));
         ResultSet rs = stmt.executeQuery();
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.err.println("getLogsByTypeFiltered() error [" + logType + "]: " + e.getMessage());
     }

     return list;
 }

 public int countAllLogs() {
     int total = 0;
     String[] tables = { "users_log", "items_log", "transactions", "reputation_log" };
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
     String sql = "SELECT COUNT(*) FROM transactions";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         if (rs.next()) return rs.getInt(1);
     } catch (SQLException e) {
         System.err.println("countTransactions() error: " + e.getMessage());
     }
     return 0;
 }

 // ── Write ──

 // context accepted for API symmetry but not stored — users_log has no reason column
 public boolean insertLog(UserLogAction action, int userId, String context) {
     return insertUserLog(userId, action.getDbValue());
 }

 // context is the item name; stored as: "ItemName": Action description
 public boolean insertLog(ItemLogAction action, int userId, int itemId, String context) {
     String reason;
     if (context == null || context.trim().isEmpty()) {
         reason = action.getDescription();
     } else {
         String trimmed = context.trim();
         // Avoid double-quoting if the caller already wrapped in quotes
         reason = trimmed.startsWith("\"")
             ? trimmed + ": " + action.getDescription()
             : "\"" + trimmed + "\": " + action.getDescription();
     }
     return insertItemLog(itemId, action.getDbValue(), reason);
 }

 // Transaction logs are written by the transaction flow, not here
 public boolean insertLog(TransactionLogAction action, int userId, int itemId, String context) {
     return insertTransactionLog();
 }

 public boolean insertLog(ReputationLogAction action, int userId, String context) {
     return insertReputationLog(userId, null, action.getPoints());
 }

 // Reputation entry linked to a specific transaction
 public boolean insertReputationLog(int userId, int transactionId, int pointsEarned) {
     return insertReputationLog(userId, Integer.valueOf(transactionId), pointsEarned);
 }

 // ── Helpers ──

 private boolean insertUserLog(int userId, String dbAction) {
     String sql = "INSERT INTO users_log (user_id, action) VALUES (?, ?)";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         if (userId > 0) stmt.setInt(1, userId);
         else            stmt.setNull(1, Types.INTEGER);
         stmt.setString(2, dbAction);
         return stmt.executeUpdate() > 0;
     } catch (SQLException e) {
         System.err.println("insertUserLog() failed: " + e.getMessage());
     }
     return false;
 }

 private boolean insertItemLog(int itemId, String dbAction, String reason) {
     String sql = "INSERT INTO items_log (item_id, action, reason) VALUES (?, ?, ?)";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         if (itemId > 0) stmt.setInt(1, itemId);
         else            stmt.setNull(1, Types.INTEGER);
         stmt.setString(2, dbAction);
         stmt.setString(3, truncate(reason, 255));
         return stmt.executeUpdate() > 0;
     } catch (SQLException e) {
         System.err.println("insertItemLog() failed: " + e.getMessage());
     }
     return false;
 }

 private boolean insertTransactionLog() {
     System.out.println("insertTransactionLog() skipped: use transaction flow instead.");
     return false;
 }

 // Null transactionId inserts without the FK column rather than inserting a NULL FK
 private boolean insertReputationLog(int userId, Integer transactionId, int pointsEarned) {
     String sql = transactionId != null
         ? "INSERT INTO reputation_log (user_id, transaction_id, points_earned) VALUES (?, ?, ?)"
         : "INSERT INTO reputation_log (user_id, points_earned) VALUES (?, ?)";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         if (userId > 0) stmt.setInt(1, userId);
         else            stmt.setNull(1, Types.INTEGER);
         if (transactionId != null) {
             stmt.setInt(2, transactionId);
             stmt.setInt(3, pointsEarned);
         } else {
             stmt.setInt(2, pointsEarned);
         }
         return stmt.executeUpdate() > 0;
     } catch (SQLException e) {
         System.err.println("insertReputationLog() failed: " + e.getMessage());
     }
     return false;
 }

 // All columns use table-aliased names to avoid ambiguity across the JOINs
 private String resolveSearchColumn(String filter, String logType) {
     if (filter == null || filter.isEmpty()) return defaultSearchColumn(logType);
     switch (logType) {
         case "USER":
             switch (filter) {
                 case "ID":      return "ul.log_id";
                 case "User ID": return "ul.user_id";
                 default:        return "ul.action";
             }
         case "ITEM":
             switch (filter) {
                 case "ID":      return "il.log_id";
                 case "Item ID": return "il.item_id";
                 case "Action":  return "il.action";
                 default:        return "il.reason";
             }
         case "TRANSACTION":
             switch (filter) {
                 case "ID":      return "t.transaction_id";
                 case "User ID": return "t.borrower_id";
                 case "Item ID": return "t.item_id";
                 default:        return "t.action";
             }
         case "REPUTATION":
             switch (filter) {
                 case "ID":      return "rl.reputation_id";
                 default:        return "rl.user_id";
             }
         default:
             return "action";
     }
 }

 private String defaultSearchColumn(String logType) {
     return "ITEM".equals(logType) ? "il.reason" : "action";
 }

 private String buildAllColumnsOR(String logType) {
     switch (logType) {
         case "USER":
             return "("
                  + "CAST(ul.log_id  AS CHAR) LIKE ? OR "
                  + "CAST(ul.user_id AS CHAR) LIKE ? OR "
                  + "ul.action               LIKE ?"
                  + ")";
         case "ITEM":
             return "("
                  + "CAST(il.log_id  AS CHAR) LIKE ? OR "
                  + "CAST(il.item_id AS CHAR) LIKE ? OR "
                  + "il.action               LIKE ? OR "
                  + "il.reason               LIKE ?"
                  + ")";
         case "TRANSACTION":
             return "("
                  + "CAST(t.transaction_id AS CHAR) LIKE ? OR "
                  + "CAST(t.borrower_id    AS CHAR) LIKE ? OR "
                  + "CAST(t.item_id        AS CHAR) LIKE ? OR "
                  + "t.action                       LIKE ?"
                  + ")";
         case "REPUTATION":
             return "("
                  + "CAST(rl.reputation_id AS CHAR) LIKE ? OR "
                  + "CAST(rl.user_id       AS CHAR) LIKE ?"
                  + ")";
         default:
             return "action LIKE ?";
     }
 }

 // Counts ? chars to know how many params to bind for a dynamically built OR expression
 private int countPlaceholders(String sql) {
     int count = 0;
     for (int i = 0; i < sql.length(); i++)
         if (sql.charAt(i) == '?') count++;
     return count;
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