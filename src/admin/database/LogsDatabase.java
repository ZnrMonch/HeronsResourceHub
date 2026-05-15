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

    // ── Counts ────────────────────────────────────────────────────────────────

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

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Inserts an admin action log entry.
     *
     * <p>Pass {@code userId = 0} or {@code itemId = 0} to store NULL for that
     * FK column. This avoids FK constraint violations when the referenced user
     * or item no longer exists in its source table (e.g. after archiving).
     *
     * <p>Both {@code user_id} and {@code item_id} in the log tables <strong>must
     * be nullable</strong> for this to work. If they are currently NOT NULL, run:
     * <pre>
     *   ALTER TABLE users_log MODIFY user_id INT NULL;
     *   ALTER TABLE items_log MODIFY user_id INT NULL;
     *   ALTER TABLE items_log MODIFY item_id INT NULL;
     * </pre>
     *
     * @param logType     "USER" or "ITEM"
     * @param userId      FK to users.user_id  — pass 0 to store NULL
     * @param itemId      FK to items.item_id  — pass 0 to store NULL (ITEM logs only)
     * @param description human-readable description of the action
     * @return true if the row was inserted successfully
     */
    public boolean insertLog(String logType, int userId, int itemId, String description) {
        String table;
        String columns;
        String values;
        String actionValue;

        switch (logType) {
            case "USER":
                table       = "users_log";
                columns     = "(user_id, first_name, last_name, action, reason, timestamp)";
                values      = "(?, '', '', ?, ?, NOW())";
                actionValue = "archive";
                break;
            case "ITEM":
                table       = "items_log";
                columns     = "(user_id, item_id, first_name, last_name, action, reason, timestamp)";
                values      = "(?, ?, '', '', ?, ?, NOW())";
                actionValue = "ARCHIVE_ITEM";
                break;
            default:
                System.out.println("insertLog() skipped: unknown logType '" + logType + "'");
                return false;
        }

        String sql = "INSERT INTO " + table + " " + columns + " VALUES " + values;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int i = 1;

            // user_id — NULL when 0 so FK constraint is not violated
            if (userId > 0) stmt.setInt(i++, userId);
            else            stmt.setNull(i++, Types.INTEGER);

            // item_id (ITEM logs only) — NULL when 0
            if ("ITEM".equals(logType)) {
                if (itemId > 0) stmt.setInt(i++, itemId);
                else            stmt.setNull(i++, Types.INTEGER);
            }

            stmt.setString(i++, actionValue);
            stmt.setString(i,   description);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertLog() failed: " + e.getMessage());
            return false;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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