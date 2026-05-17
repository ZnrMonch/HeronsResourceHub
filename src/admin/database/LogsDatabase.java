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
     * @param logType   One of: "USER", "ITEM", "TRANSACTION", "REPUTATION"
     * @param filter    Column to search in (e.g. "User ID", "Action", "Description")
     * @param keyword   Text to search for — pass "" to skip keyword filtering
     * @param dateFrom  Start date as "YYYY-MM-DD", or null/empty to skip
     * @param dateTo    End date   as "YYYY-MM-DD", or null/empty to skip
     */
    public List<AdminLogs> getLogsByTypeFiltered(
            String logType, String filter, String keyword,
            String dateFrom, String dateTo) {

        List<AdminLogs> list = new ArrayList<>();

        // ── Resolve table-specific settings ──────────────────────────────────
        // Each log type lives in a different table with slightly different columns.
        // We capture the differences here so the rest of the method stays generic.

        String  table;        // which table to query
        String  idCol;        // the primary-key column name (varies per table)
        String  timestampCol; // null if the table has no timestamp column
        boolean hasItemId;    // whether this table tracks an item_id

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
                // Unknown type — return empty list rather than crashing.
                System.err.println("getLogsByTypeFiltered(): unknown logType '" + logType + "'");
                return list;
        }

        // ── Build the SELECT expressions ──────────────────────────────────────
        // We alias everything to fixed names so mapRow() always works the same way.

        // Tables without item_id get a literal 0 aliased as item_id.
        String itemIdExpr = hasItemId ? "item_id" : "0 AS item_id";

        // transaction_log has no timestamp, so we produce a typed NULL placeholder
        // instead of leaving the column missing — mapRow() can then call getString("created_at").
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
        // We collect condition strings and their matching parameter values in
        // parallel lists so we can bind them safely with PreparedStatement.

        List<String> conditions = new ArrayList<>();
        List<Object> params     = new ArrayList<>();

        // Keyword search — only added when the caller supplies a non-blank keyword.
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchCol = resolveSearchColumn(filter, logType, hasItemId);
            conditions.add(searchCol + " LIKE ?");
            params.add("%" + keyword.trim() + "%");
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
            // Join multiple conditions with AND
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Sort newest-first when a timestamp exists; fall back to PK order otherwise.
        if (timestampCol != null) {
            sql.append(" ORDER BY ").append(timestampCol).append(" DESC");
        } else {
            sql.append(" ORDER BY ").append(idCol).append(" ASC");
        }

        // ── Execute and map results ───────────────────────────────────────────
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Bind parameters in the same order they were added to the list.
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
     * Used for the admin dashboard "total logs" stat.
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
     * Used for the admin dashboard "total transactions" stat.
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
    //
    // One overload per log table. All accept a typed ENUM so there is no
    // ambiguity about which DB value gets stored. No string-based fallbacks.

    /**
     * Records a user-related event (users_log).
     *
     * @param action   The specific event that occurred
     * @param userId   The user the event is about (0 = system/anonymous)
     * @param context  Optional extra detail appended to the reason column
     */
    public boolean insertLog(UserLogAction action, int userId, String context) {
        // Build the human-readable reason string.
        // If context is blank we just use the enum's built-in description.
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();

        return insertUserLog(userId, action.getDbValue(), reason);
    }

    /**
     * Records an item-related event (items_log).
     *
     * @param action   The specific event that occurred
     * @param userId   The user who triggered the event
     * @param itemId   The item the event is about
     * @param context  Optional extra detail
     */
    public boolean insertLog(ItemLogAction action, int userId, int itemId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();

        return insertItemLog(userId, itemId, action.getDbValue(), reason);
    }

    /**
     * Records a transaction event (transaction_log).
     *
     * @param action   The specific event that occurred
     * @param userId   The user who triggered the event
     * @param itemId   The item involved
     * @param context  Optional extra detail
     */
    public boolean insertLog(TransactionLogAction action, int userId, int itemId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();

        return insertTransactionLog(userId, itemId, action.getDbValue(), reason);
    }

    /**
     * Records a reputation event (reputation_log).
     *
     * @param action   The specific event that occurred
     * @param userId   The user whose reputation changed
     * @param context  Optional extra detail
     */
    public boolean insertLog(ReputationLogAction action, int userId, String context) {
        String reason = context == null || context.trim().isEmpty()
                ? action.getDescription()
                : context.trim() + ": " + action.getDescription();

        return insertReputationLog(userId, action.getDbValue(), reason);
    }


    // ── Private insert helpers ────────────────────────────────────────────────
    //
    // Each helper maps directly to one table. They are private because callers
    // must go through the typed insertLog() overloads above.

    /**
     * Inserts one row into users_log.
     * Looks up the user's name from the users table (falls back to users_archive).
     */
    private boolean insertUserLog(int userId, String dbAction, String reason) {
        // Fetch the name only if we have a real userId to look up.
        String firstName = userId > 0 ? lookupFirstName(userId) : "";
        String lastName  = userId > 0 ? lookupLastName(userId)  : "";

        String sql =
            "INSERT INTO users_log "
            + "(user_id, initiator_firstname, initiator_lastname, action, reason) "
            + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Store NULL in the FK column rather than a 0 that references nothing.
            if (userId > 0) stmt.setInt(1, userId);
            else            stmt.setNull(1, Types.INTEGER);

            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, dbAction);
            stmt.setString(5, truncate(reason, 255)); // ENUM columns have a length limit

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertUserLog() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserts one row into items_log.
     */
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

    /**
     * Inserts one row into transaction_log.
     * Note: this table has no timestamp column — MySQL will not auto-fill one.
     */
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

    /**
     * Inserts one row into reputation_log.
     */
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

    /** Convenience wrapper — looks up first_name. */
    private String lookupFirstName(int userId) {
        return lookupNameField(userId, "first_name");
    }

    /** Convenience wrapper — looks up last_name. */
    private String lookupLastName(int userId) {
        return lookupNameField(userId, "last_name");
    }

    /**
     * Looks up a single name field for a user.
     * Tries the active users table first; falls back to users_archive if not found.
     * This handles the case where a user was archived before the log was written.
     *
     * @param userId  The user to look up
     * @param field   Either "first_name" or "last_name"
     */
    private String lookupNameField(int userId, String field) {
        String fromUsers   = "SELECT " + field + " FROM users WHERE user_id = ? LIMIT 1";
        String fromArchive = "SELECT " + field + " FROM users_archive "
                           + "WHERE user_id = ? ORDER BY user_archive_id DESC LIMIT 1";

        // 1. Try active users first.
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(fromUsers)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String val = rs.getString(1);
                return val != null ? val : "";
            }
        } catch (SQLException ignored) {}

        // 2. Fall back to archive (user may have been deleted after acting).
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(fromArchive)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String val = rs.getString(1);
                return val != null ? val : "";
            }
        } catch (SQLException ignored) {}

        return ""; // Unknown user — return blank rather than crashing.
    }


    // ── Private query-building helpers ────────────────────────────────────────

    /**
     * Maps the UI filter label to the actual database column name used in LIKE searches.
     *
     * @param filter    The label the UI sends (e.g. "User ID", "Action")
     * @param logType   Needed to pick the right PK column name
     * @param hasItemId Whether this table has an item_id column
     */
    private String resolveSearchColumn(String filter, String logType, boolean hasItemId) {
        if (filter == null || filter.isEmpty()) return "reason"; // default to description

        switch (filter) {
            case "ID":          return resolveIdColumn(logType);
            case "User ID":     return "user_id";
            case "Item ID":     return hasItemId ? "item_id" : "user_id"; // graceful fallback
            case "Action":      return "action";
            case "Description": return "reason";
            case "Date":
                // Only tables with a timestamp support date searching.
                return hasTimestamp(logType) ? "DATE(timestamp)" : resolveIdColumn(logType);
            default:
                return "reason";
        }
    }

    /**
     * Returns the correct primary-key column name for a given log type.
     * Each table chose a different name for its PK — this keeps that detail in one place.
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
     * Returns an empty string if the input is null.
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    /**
     * Converts one ResultSet row into an AdminLogs model object.
     * Column aliases set in getLogsByTypeFiltered() make this work for all four tables.
     */
    private AdminLogs mapRow(ResultSet rs) throws SQLException {
        AdminLogs log = new AdminLogs();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setItemId(rs.getInt("item_id"));          // returns 0 for tables without item_id
        log.setLogType(rs.getString("log_type"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getString("created_at")); // null for transaction_log rows
        return log;
    }
}