package admin.database;

import admin.models.AdminLogs;
import database.DatabaseManager;
import enums.ItemLogAction;
import enums.ReputationLogAction;
import enums.TransactionLogAction;
import enums.UserLogAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogsDatabase extends BaseDatabase{

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


    public boolean insertLog(UserLogAction action, int userId, String userName) {
        String description = userName.isEmpty()
            ? action.getDescription()
            : "User " + userName + ": " + action.getDescription();
        return insertLog("USER", userId, 0, action.name(), description);
    }

    public boolean insertLog(ItemLogAction action, int userId, int itemId, String context) {
        String description = context.isEmpty()
            ? action.getDescription()
            : context + ": " + action.getDescription();
        return insertLog("ITEM", userId, itemId, action.name(), description);
    }

    public boolean insertLog(TransactionLogAction action, int userId, int itemId, String context) {
        String description = context.isEmpty()
            ? action.getDescription()
            : context + ": " + action.getDescription();
        return insertLog("TRANSACTION", userId, itemId, action.name(), description);
    }

    public boolean insertLog(ReputationLogAction action, int userId, String context) {
        String description = context.isEmpty()
            ? action.getDescription()
            : context + ": " + action.getDescription();
        return insertLog("REPUTATION", userId, 0, action.name(), description);
    }


   
    public boolean insertLog(String logType, int userId, int itemId, String description) {
        String action;
        if ("USER".equals(logType))       action = resolveUserAction(description);
        else if ("ITEM".equals(logType))  action = resolveItemAction(description);
        else                              action = description;
        return insertLog(logType, userId, itemId, action, description);
    }


   

    private boolean insertLog(String logType, int userId, int itemId,
            String action, String description) {

        if ("USER".equals(logType)) {
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
                stmt.setString(4, toUserDbAction(action));                     
                stmt.setString(5, truncate(description, 255));    
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("insertLog(USER) failed: " + e.getMessage());
            }

        } else if ("ITEM".equals(logType)) {
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
                stmt.setString(5, toItemDbAction(action));                     // enum name e.g. ITEM_CREATE
                stmt.setString(6, truncate(description, 255));
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("insertLog(ITEM) failed: " + e.getMessage());
            }

        } else if ("TRANSACTION".equals(logType)) {
            String sql =
                "INSERT INTO transaction_log "
                + "(user_id, item_id, action, reason) "
                + "VALUES (?, ?, ?, ?)";

            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (userId > 0) stmt.setInt(1, userId);
                else            stmt.setNull(1, Types.INTEGER);
                if (itemId > 0) stmt.setInt(2, itemId);
                else            stmt.setNull(2, Types.INTEGER);
                stmt.setString(3, action);                        // enum name e.g. TRANSACTION_BUY
                stmt.setString(4, truncate(description, 255));
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("insertLog(TRANSACTION) failed: " + e.getMessage());
            }

        } else if ("REPUTATION".equals(logType)) {
            String sql =
                "INSERT INTO reputation_log "
                + "(user_id, action, reason) "
                + "VALUES (?, ?, ?)";

            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (userId > 0) stmt.setInt(1, userId);
                else            stmt.setNull(1, Types.INTEGER);
                stmt.setString(2, action);                        // enum name e.g. REPUTATION_UPDATE
                stmt.setString(3, truncate(description, 255));
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("insertLog(REPUTATION) failed: " + e.getMessage());
            }

        } else {
            System.out.println("insertLog() skipped: unknown logType '" + logType + "'");
        }

        return false;
    }


    // ── Private helpers ──────────────────────────────────────────────────────

    private String resolveUserAction(String description) {
        if (description == null) return UserLogAction.USER_UPDATE_PROFILE.name();
        String lower = description.toLowerCase();
        if (lower.contains("permanently deleted") || lower.contains("permanent delete"))
            return UserLogAction.USER_DELETE.name();
        if (lower.contains("restored") || lower.contains("unarchive"))
            return UserLogAction.USER_RESTORE.name();
        if (lower.contains("archived") || lower.contains("archive"))
            return UserLogAction.USER_ARCHIVE.name();
        if (lower.contains("added") || lower.contains("registered") || lower.contains("new user"))
            return UserLogAction.USER_REGISTER.name();
        if (lower.contains("role"))
            return UserLogAction.USER_ROLE_UPDATE.name();
        return UserLogAction.USER_UPDATE_PROFILE.name();
    }

    private String resolveItemAction(String description) {
        if (description == null) return ItemLogAction.ITEM_UPDATE.name();
        String lower = description.toLowerCase();
        if (lower.contains("archived"))  return ItemLogAction.ITEM_ARCHIVE.name();
        if (lower.contains("restored"))  return ItemLogAction.ITEM_RESTORE.name();
        if (lower.contains("added") || lower.contains("new item"))
            return ItemLogAction.ITEM_CREATE.name();
        return ItemLogAction.ITEM_UPDATE.name();
    }

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


    private String toUserDbAction(String enumName) {
        if (enumName == null) return "Update";
        switch (enumName) {
            case "USER_REGISTER":         return "Create";
            case "USER_UPDATE_PROFILE":
            case "USER_CHANGE_PASSWORD":
            case "USER_UPLOAD_PROFILE":
            case "USER_ROLE_UPDATE":      return "Update";
            case "USER_ARCHIVE":          return "Archive";
            case "USER_UNARCHIVE":
            case "USER_RESTORE":          return "Unarchive";
            case "USER_DELETE":           return "Delete";
            default:                      return "Update";
        }
    }
    
    private String toItemDbAction(String enumName) {
        if (enumName == null) return "UPDATE_ITEM";
        switch (enumName) {
            case "ITEM_CREATE":
            case "ITEM_LIST":             return "LIST_ITEM";
            case "ITEM_UPDATE":
            case "ITEM_MARK_AVAILABLE":
            case "ITEM_MARK_UNAVAILABLE":
            case "ITEM_APPROVE":
            case "ITEM_REJECT":
            case "ITEM_UPLOAD_IMAGE":
            case "ITEM_DELETE_IMAGE":
            case "ITEM_UNLIST":           return "UPDATE_ITEM";
            case "ITEM_ARCHIVE":          return "ARCHIVE_ITEM";
            case "ITEM_UNARCHIVE":
            case "ITEM_RESTORE":          return "RESTORE_ITEM";
            case "ITEM_DELETE":           return "DELETE_ITEM";
            default:                      return "UPDATE_ITEM";
        }
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