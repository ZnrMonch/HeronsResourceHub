// ══════════════════════════════════════════════════════════════════════════════
// LogsDatabase.java
// ──────────────────────────────────────────────────────────────────────────────
// The only class allowed to touch the four log tables directly:
//   users_log, items_log, transaction_log, reputation_log
//
// WHAT THIS CLASS DOES:
//   1. insertLog() overloads  — write a new row to the correct log table
//   2. getLogsByTypeFiltered() — read rows back with optional search + date range
//   3. countAllLogs() / countTransactions() — dashboard stat counters
//
// SCHEMA NOTES (frozen — do not change):
//   users_log      → log_id, user_id, initiator_firstname, initiator_lastname,
//                    action ENUM, reason, timestamp
//   items_log      → log_id, user_id, item_id, initiator_firstname,
//                    initiator_lastname, action ENUM, reason, timestamp
//   transaction_log→ transaction_id, user_id, item_id, action ENUM, reason
//                    (NO timestamp column)
//   reputation_log → reputation_id, user_id, action ENUM, reason, timestamp
// ══════════════════════════════════════════════════════════════════════════════
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

    // ── Public read methods ───────────────────────────────────────────────────

    /**
     * Returns all logs of the given type with no filters applied.
     * Convenience wrapper around getLogsByTypeFiltered().
     *
     * @param logType  One of: "USER", "ITEM", "TRANSACTION", "REPUTATION"
     */
    public List<AdminLogs> getLogsByType(String logType) {
        return getLogsByTypeFiltered(logType, "", "", null, null);
    }

    /**
     * Returns logs of the given type, optionally narrowed by a keyword search
     * and/or a date range.
     *
     * <p>When filter is "All", the search expression spans every relevant column
     * in the table using OR (see {@link #buildAllColumnsOR}).  Because that
     * expression contains multiple {@code ?} placeholders — one per column —
     * the keyword is bound once for every placeholder in the expression rather
     * than just once.
     *
     * @param logType   One of: "USER", "ITEM", "TRANSACTION", "REPUTATION"
     * @param filter    Column to search in (e.g. "User ID", "Action", "All")
     * @param keyword   Text to search for — pass "" to skip keyword filtering
     * @param dateFrom  Start date as "YYYY-MM-DD", or null/empty to skip
     * @param dateTo    End date   as "YYYY-MM-DD", or null/empty to skip
     */
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
                timestampCol = null;   // transaction_log has NO timestamp column
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

        // ── Build the SELECT expressions ──────────────────────────────────────
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

        // ── Build WHERE clauses dynamically ──────────────────────────────────
        List<String> conditions = new ArrayList<>();
        List<Object> params     = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();

            if ("All".equals(filter)) {
                // Multi-column OR expression — contains one ? per column.
                // We resolve the expression string first, then count the
                // number of ? placeholders so we can bind the keyword that
                // many times.
                String orExpr = buildAllColumnsOR(logType, hasItemId);
                conditions.add(orExpr);

                // Count the number of ? placeholders inside the expression
                // and add the keyword once per placeholder.
                int placeholderCount = countPlaceholders(orExpr);
                for (int i = 0; i < placeholderCount; i++) {
                    params.add("%" + trimmedKeyword + "%");
                }
            } else {
                // Single-column search (original behaviour)
                String searchCol = resolveSearchColumn(filter, logType, hasItemId);
                conditions.add(searchCol + " LIKE ?");
                params.add("%" + trimmedKeyword + "%");
            }
        }

        // Date range — only valid for tables that have a timestamp column.
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

        // ── Assemble the final SQL string ─────────────────────────────────────
        StringBuilder sql = new StringBuilder(selectSql);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if (timestampCol != null) {
            sql.append(" ORDER BY ").append(timestampCol).append(" DESC");
        } else {
            sql.append(" ORDER BY ").append(idCol).append(" ASC");
        }

        // ── Execute and map results ───────────────────────────────────────────
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

    /**
     * Returns the total number of rows across all four log tables combined.
     */
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

    /**
     * Returns the total number of rows in transaction_log only.
     */
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


    // ── Public insertLog overloads ────────────────────────────────────────────

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


    // ── Private insert helpers ────────────────────────────────────────────────

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


    // ── Name lookup helpers ───────────────────────────────────────────────────

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


    // ── Private query-building helpers ────────────────────────────────────────

    /**
     * Maps the UI filter label to the actual database column name (or expression)
     * used in a LIKE search.
     *
     * <p>The special "All" value returns a parenthesised multi-column OR expression
     * built by {@link #buildAllColumnsOR}.  That expression contains multiple
     * {@code ?} placeholders, which the caller must bind individually — one for
     * each placeholder — using {@link #countPlaceholders}.
     *
     * @param filter    The label the UI sends (e.g. "User ID", "Action", "All")
     * @param logType   Needed to pick the right PK column name and OR expression
     * @param hasItemId Whether this table has an item_id column
     */
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

    /**
     * Builds a parenthesised multi-column OR expression for an "All" keyword
     * search.  Each column gets its own {@code ?} placeholder.
     *
     * <p>The caller is responsible for counting the placeholders via
     * {@link #countPlaceholders} and binding the keyword value once per
     * placeholder.
     *
     * <ul>
     *   <li><b>USER</b>  — id, user_id, action, reason  (4 placeholders)</li>
     *   <li><b>ITEM</b>  — id, user_id, item_id, action, reason  (5 placeholders)</li>
     *   <li><b>TRANSACTION</b> — id, user_id, item_id, action, reason  (5 placeholders)</li>
     *   <li><b>REPUTATION</b> — id, user_id, action, reason  (4 placeholders)</li>
     * </ul>
     */
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
                // Fallback: search reason only (single placeholder)
                return "reason LIKE ?";
        }
    }

    /**
     * Counts the number of {@code ?} characters in a SQL fragment.
     * Used to determine how many times to bind the keyword value when the
     * search expression is a multi-column OR produced by {@link #buildAllColumnsOR}.
     *
     * @param sql  Any SQL string or fragment
     * @return     The number of {@code ?} characters found
     */
    private int countPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') count++;
        }
        return count;
    }

    /**
     * Returns the correct primary-key column name for a given log type.
     */
    private String resolveIdColumn(String logType) {
        switch (logType) {
            case "TRANSACTION": return "transaction_id";
            case "REPUTATION":  return "reputation_id";
            default:            return "log_id";
        }
    }

    /**
     * Returns true if the table for this log type has a timestamp column.
     * transaction_log is the only one that does NOT.
     */
    private boolean hasTimestamp(String logType) {
        return !"TRANSACTION".equals(logType);
    }

    /**
     * Cuts a string down to maxLen characters to avoid DB column overflow errors.
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    /**
     * Converts one ResultSet row into an AdminLogs model object.
     */
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