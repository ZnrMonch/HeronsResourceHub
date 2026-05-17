package admin.database;

import admin.models.AdminUsers;
import database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.HashUtil;

public class UsersDatabase extends BaseDatabase {

    private static Boolean softDeleteColumnExists  = null;
    private static Boolean archiveTableExistsCache = null;

    private static final String SELECT_USERS_COLS =
        "user_id, system_role, student_id, first_name, last_name, "
        + "college, year_level, course_program, karma_score, "
        + "contact_num, gcash_num, maya_num, mastercard_num, visa_num";

    private static final String SELECT_ARCHIVE_COLS =
        "user_id, system_role, student_id, first_name, last_name, "
        + "college, year_level, course_program, karma_score, "
        + "contact AS contact_num, gcash_num, maya_num, mastercard_num, visa_num";

    public int countActiveUsers() {
        String sql = hasSoftDeleteColumn()
            ? "SELECT COUNT(*) FROM users WHERE deleted_at IS NULL"
            : "SELECT COUNT(*) FROM users";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("countActiveUsers() failed: " + e.getMessage());
        }
        return 0;
    }

    public List<AdminUsers> getAllUsers() {
        String condition = hasSoftDeleteColumn() ? " WHERE deleted_at IS NULL" : "";
        String sql = "SELECT " + SELECT_USERS_COLS
                   + " FROM users" + condition + " ORDER BY user_id ASC";
        List<AdminUsers> list = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.err.println("getAllUsers() failed: " + e.getMessage());
        }
        return list;
    }

    public List<AdminUsers> getArchivedUsers() {
        if (hasSoftDeleteColumn()) {
            List<AdminUsers> list = new ArrayList<>();
            String sql = "SELECT " + SELECT_USERS_COLS
                       + " FROM users WHERE deleted_at IS NOT NULL ORDER BY user_id ASC";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs, true));
                if (!list.isEmpty()) return list;
            } catch (SQLException e) {
                System.err.println("getArchivedUsers() soft-delete failed: " + e.getMessage());
            }
        }

        if (archiveTableExists()) {
            List<AdminUsers> list = new ArrayList<>();
            String sql = "SELECT " + SELECT_ARCHIVE_COLS
                       + " FROM users_archive ORDER BY user_id ASC";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs, true));
            } catch (SQLException e) {
                System.err.println("getArchivedUsers() archive table failed: " + e.getMessage());
            }
            return list;
        }
        return new ArrayList<>();
    }

    public List<AdminUsers> searchUsers(String filter, String keyword) {
        String column      = resolveUserColumn(filter);
        String activeWhere = hasSoftDeleteColumn() ? " AND deleted_at IS NULL" : "";
        String sql = "SELECT " + SELECT_USERS_COLS
                   + " FROM users WHERE " + column + " LIKE ?" + activeWhere;
        List<AdminUsers> list = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.err.println("searchUsers() failed: " + e.getMessage());
        }
        return list;
    }

    public List<AdminUsers> searchArchivedUsers(String filter, String keyword) {
        String column = resolveUserColumn(filter);
        List<AdminUsers> list = new ArrayList<>();

        if (hasSoftDeleteColumn()) {
            String sql = "SELECT " + SELECT_USERS_COLS
                       + " FROM users WHERE " + column + " LIKE ? AND deleted_at IS NOT NULL";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + keyword + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) list.add(mapRow(rs, true));
                if (!list.isEmpty()) return list;
            } catch (SQLException e) {
                System.err.println("searchArchivedUsers() soft-delete failed: " + e.getMessage());
            }
        }

        if (archiveTableExists()) {
            String archiveCol = "Contact Number".equals(filter) ? "contact" : column;
            String sql = "SELECT " + SELECT_ARCHIVE_COLS
                       + " FROM users_archive WHERE " + archiveCol + " LIKE ?";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + keyword + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) list.add(mapRow(rs, true));
            } catch (SQLException e) {
                System.err.println("searchArchivedUsers() archive table failed: " + e.getMessage());
            }
        }
        return list;
    }

    public AdminUsers getUserById(int userId) {
        String base      = "SELECT " + SELECT_USERS_COLS + " FROM users WHERE user_id = ?";
        String activeSql = hasSoftDeleteColumn() ? base + " AND deleted_at IS NULL" : base;

        // 1. Active users
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(activeSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, false);
        } catch (SQLException e) {
            System.err.println("getUserById() active lookup failed: " + e.getMessage());
        }

        // 2. Soft-deleted users (only if column exists)
        if (hasSoftDeleteColumn()) {
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(base + " AND deleted_at IS NOT NULL")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return mapRow(rs, true);
            } catch (SQLException e) {
                System.err.println("getUserById() soft-delete lookup failed: " + e.getMessage());
            }
        }

        // 3. Archived users
        String archiveSql = "SELECT " + SELECT_ARCHIVE_COLS
                + " FROM users_archive WHERE user_id = ? "
                + "ORDER BY user_archive_id DESC LIMIT 1";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(archiveSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, true);
        } catch (SQLException e) {
            System.err.println("getUserById() archive lookup failed: " + e.getMessage());
        }

        return null;
    }

    public AdminUsers getUserFromArchiveById(int userId) {
        if (!archiveTableExists()) return null;
        String sql = "SELECT " + SELECT_ARCHIVE_COLS
                   + " FROM users_archive WHERE user_id = ?"
                   + " ORDER BY user_archive_id DESC LIMIT 1";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, true);
        } catch (SQLException e) {
            System.err.println("getUserFromArchiveById() failed: " + e.getMessage());
        }
        return null;
    }

    public String checkDuplicateUser(String studentId, String email) {
        String checkStudentId = "SELECT COUNT(*) FROM users WHERE student_id = ?";
        String checkEmail     = "SELECT COUNT(*) FROM users WHERE umak_email_address = ?";
        try (Connection conn = getConn()) {
            try (PreparedStatement stmt = conn.prepareStatement(checkStudentId)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return "student_id";
            }
            try (PreparedStatement stmt = conn.prepareStatement(checkEmail)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return "email";
            }
        } catch (SQLException e) {
            System.err.println("checkDuplicateUser() failed: " + e.getMessage());
        }
        return null;
    }

    // Returns the new user_id on success, -1 on failure
    public int insertUser(String studentId, String firstName, String lastName,
            String email, String password, String yearLevel,
            String college, String role, String profileImagePath) {
        String sql =
            "INSERT INTO users "
            + "(student_id, first_name, last_name, umak_email_address, password, "
            + " year_level, college, system_role, course_program, profile_image) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, '', ?)";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, studentId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setString(5, password);
            stmt.setString(6, yearLevel);
            stmt.setString(7, college);
            stmt.setString(8, role);
            stmt.setString(9, profileImagePath.isEmpty() ? null : profileImagePath);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("insertUser() failed: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateUser(AdminUsers user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, college = ?, "
                   + "year_level = ?, karma_score = ?, system_role = ? WHERE user_id = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getCollege());
            stmt.setString(4, user.getYearLevel());
            stmt.setInt(5,    user.getKarmaScore());
            stmt.setString(6, user.getSystemRole());
            stmt.setInt(7,    user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateUser() failed: " + e.getMessage());
        }
        return false;
    }

    public boolean archiveUser(int userId) {
        if (!archiveTableExists()) {
            System.err.println("archiveUser() failed: users_archive table not found.");
            return false;
        }

        String deleteStaleArchive     = "DELETE FROM users_archive WHERE user_id = ?";
        String deleteRepByItem        = "DELETE FROM reputation_log WHERE transaction_id IN "
            + "(SELECT transaction_id FROM transaction_log "
            + " WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?))";
        String deleteRepByUser        = "DELETE FROM reputation_log WHERE transaction_id IN "
            + "(SELECT transaction_id FROM transaction_log WHERE user_id = ?)";
        String deleteItemsLogByItem   = "DELETE FROM items_log WHERE item_id IN "
            + "(SELECT item_id FROM items WHERE owner_id = ?)";
        String deleteItemsLogByUser   = "DELETE FROM items_log WHERE user_id = ?";
        String deleteTransByItem      = "DELETE FROM transaction_log WHERE item_id IN "
            + "(SELECT item_id FROM items WHERE owner_id = ?)";
        String deleteTransByUser      = "DELETE FROM transaction_log WHERE user_id = ?";
        String nullifyUsersLog        = "UPDATE users_log SET user_id = NULL WHERE user_id = ?";
        String deleteItemsArchive     = "DELETE FROM items_archive WHERE owner_id = ?";
        String archiveItems           =
            "INSERT INTO items_archive ("
            + "  item_id, owner_id, initiator_firstname, initiator_lastname,"
            + "  item_name, item_quantity, description, items_image,"
            + "  category, `condition`, price, availability_status,"
            + "  maximum_borrow_days, desired_item, date_listed,"
            + "  pickup_area, pickup_time, pickup_days, `action`"
            + ") SELECT"
            + "  item_id, owner_id, initiator_firstname, initiator_lastname,"
            + "  item_name, item_quantity, description, items_image,"
            + "  CASE category WHEN 'Consumable-Goods' THEN 'Consumable' ELSE category END,"
            + "  `condition`, price, availability_status,"
            + "  maximum_borrow_days, desired_item, date_listed,"
            + "  pickup_area, pickup_time, pickup_days, `action`"
            + " FROM items WHERE owner_id = ?";
        String deleteItems            = "DELETE FROM items WHERE owner_id = ?";
        String insertUserArchive      =
            "INSERT INTO users_archive "
            + "(user_id, system_role, student_id, umak_email_address, password, "
            + " college, year_level, course_program, first_name, last_name, "
            + " karma_score, profile_image, contact, home_address, "
            + " gcash_num, maya_num, mastercard_num, visa_num) "
            + "SELECT user_id, system_role, student_id, umak_email_address, password, "
            + "       college, year_level, course_program, first_name, last_name, "
            + "       karma_score, profile_image, contact_num, home_address, "
            + "       gcash_num, maya_num, mastercard_num, visa_num "
            + "FROM users WHERE user_id = ?";
        String deleteUser             = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                exec(conn, deleteStaleArchive,   userId);
                exec(conn, deleteRepByItem,       userId);
                exec(conn, deleteRepByUser,       userId);
                exec(conn, deleteItemsLogByItem,  userId);
                exec(conn, deleteItemsLogByUser,  userId);
                exec(conn, deleteTransByItem,     userId);
                exec(conn, deleteTransByUser,     userId);
                exec(conn, nullifyUsersLog,       userId);
                exec(conn, deleteItemsArchive,    userId);

                try (PreparedStatement ps = conn.prepareStatement(archiveItems)) {
                    ps.setInt(1, userId);
                    int n = ps.executeUpdate();
                    System.out.println("archiveUser() copied " + n + " item(s) to items_archive.");
                }

                exec(conn, deleteItems, userId);

                try (PreparedStatement ps = conn.prepareStatement(insertUserArchive)) {
                    ps.setInt(1, userId);
                    int rows = ps.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("User " + userId + " not found — cannot archive.");
                }

                exec(conn, deleteUser, userId);
                conn.commit();
                System.out.println("archiveUser() success: user " + userId + " archived.");

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("archiveUser() rolled back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("archiveUser() connection failed: " + e.getMessage());
            return false;
        }


        String insertAuditLog =
                "INSERT INTO users_log (user_id, initiator_firstname, initiator_lastname, action, reason, timestamp) ";
            try (Connection conn = getConn();
                 PreparedStatement ps = conn.prepareStatement(insertAuditLog)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("archiveUser() audit log skipped (non-fatal): " + e.getMessage());
            }

        return true;
    }

    public boolean unarchiveUser(int userId) {
        if (hasSoftDeleteColumn()) {
            String sql = "UPDATE users SET deleted_at = NULL "
                       + "WHERE user_id = ? AND deleted_at IS NOT NULL";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                if (stmt.executeUpdate() > 0) {
                    System.out.println("unarchiveUser() soft-delete: restored user " + userId);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("unarchiveUser() soft-delete failed: " + e.getMessage());
            }
        }

        if (!archiveTableExists()) return false;

        String insertUser =
            "INSERT INTO users "
            + "(user_id, system_role, student_id, umak_email_address, password, "
            + " college, year_level, course_program, first_name, last_name, "
            + " karma_score, profile_image, contact_num, home_address, "
            + " gcash_num, maya_num, mastercard_num, visa_num) "
            + "SELECT user_id, system_role, student_id, umak_email_address, password, "
            + "       college, year_level, course_program, first_name, last_name, "
            + "       karma_score, profile_image, contact, home_address, "
            + "       gcash_num, maya_num, mastercard_num, visa_num "
            + "FROM users_archive WHERE user_id = ? "
            + "ORDER BY user_archive_id DESC LIMIT 1";
        String deleteFromArchive  = "DELETE FROM users_archive WHERE user_id = ?";
        String restoreItems       =
            "INSERT IGNORE INTO items "
            + "(item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + " description, items_image, category, `condition`, price, availability_status, "
            + " maximum_borrow_days, desired_item, date_listed, pickup_area, pickup_time, pickup_days, action) "
            + "SELECT ia.item_id, ia.owner_id, ia.initiator_firstname, ia.initiator_lastname, "
            + "       ia.item_name, ia.item_quantity, ia.description, ia.items_image, "
            + "       ia.category, ia.`condition`, ia.price, ia.availability_status, "
            + "       ia.maximum_borrow_days, ia.desired_item, ia.date_listed, "
            + "       ia.pickup_area, ia.pickup_time, ia.pickup_days, ia.action "
            + "FROM items_archive ia "
            + "INNER JOIN ("
            + "  SELECT item_id, MAX(item_archive_id) AS max_aid "
            + "  FROM items_archive WHERE owner_id = ? GROUP BY item_id"
            + ") latest ON ia.item_archive_id = latest.max_aid";
        String deleteRestoredItems = "DELETE FROM items_archive WHERE owner_id = ?";

        String cleanRepByItem  = "DELETE FROM reputation_log WHERE transaction_id IN "
                               + "(SELECT transaction_id FROM transaction_log "
                               + " WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?))";
        String cleanRepByUser  = "DELETE FROM reputation_log WHERE transaction_id IN "
                               + "(SELECT transaction_id FROM transaction_log WHERE user_id = ?)";
        String cleanItemsLogIt = "DELETE FROM items_log WHERE item_id IN "
                               + "(SELECT item_id FROM items WHERE owner_id = ?)";
        String cleanItemsLog   = "DELETE FROM items_log WHERE user_id = ?";
        String cleanTransItem  = "DELETE FROM transaction_log WHERE item_id IN "
                               + "(SELECT item_id FROM items WHERE owner_id = ?)";
        String cleanTransUser  = "DELETE FROM transaction_log WHERE user_id = ?";
        String cleanUsersLog   = "UPDATE users_log SET user_id = NULL WHERE user_id = ?";
        String cleanItemsArch  = "DELETE FROM items_archive WHERE owner_id = ?";
        String cleanItems      = "DELETE FROM items WHERE owner_id = ?";
        String cleanUser       = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                boolean ghostExists = false;
                try (PreparedStatement chk = conn.prepareStatement(
                        "SELECT 1 FROM users WHERE user_id = ? LIMIT 1")) {
                    chk.setInt(1, userId);
                    ghostExists = chk.executeQuery().next();
                }
                if (ghostExists) {
                    System.out.println("unarchiveUser() purging ghost row for user " + userId);
                    exec(conn, cleanRepByItem,  userId);
                    exec(conn, cleanRepByUser,  userId);
                    exec(conn, cleanItemsLogIt, userId);
                    exec(conn, cleanItemsLog,   userId);
                    exec(conn, cleanTransItem,  userId);
                    exec(conn, cleanTransUser,  userId);
                    exec(conn, cleanUsersLog,   userId);
                    exec(conn, cleanItemsArch,  userId);
                    exec(conn, cleanItems,      userId);
                    exec(conn, cleanUser,       userId);
                }

                try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                    ps.setInt(1, userId);
                    int rows = ps.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("User " + userId + " not found in users_archive.");
                }

                exec(conn, deleteFromArchive, userId);

                try {
                    exec(conn, restoreItems,        userId);
                    exec(conn, deleteRestoredItems, userId);
                } catch (SQLException e) {
                    System.out.println("unarchiveUser() item restore skipped: " + e.getMessage());
                }

                conn.commit();
                System.out.println("unarchiveUser() success: restored user " + userId);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("unarchiveUser() rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("unarchiveUser() connection failed: " + e.getMessage());
        }
        return false;
    }

    public boolean permanentDeleteUser(int userId) {
        String sql = "DELETE FROM users_archive WHERE user_id = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("permanentDeleteUser() success: user " + userId + " permanently deleted.");
                return true;
            } else {
                System.out.println("permanentDeleteUser() failed: user " + userId + " not found in archive.");
            }
        } catch (SQLException e) {
            System.err.println("permanentDeleteUser() failed: " + e.getMessage());
        }
        return false;
    }

    private boolean hasSoftDeleteColumn() {
        if (softDeleteColumnExists != null) return softDeleteColumnExists;
        try (Connection conn = getConn()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "users", "deleted_at")) {
                softDeleteColumnExists = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("hasSoftDeleteColumn() failed: " + e.getMessage());
            softDeleteColumnExists = false;
        }
        System.out.println("Schema — users.deleted_at exists: " + softDeleteColumnExists);
        return softDeleteColumnExists;
    }

    private boolean archiveTableExists() {
        if (archiveTableExistsCache != null) return archiveTableExistsCache;
        try (Connection conn = getConn()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "users_archive", new String[]{"TABLE"})) {
                archiveTableExistsCache = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("archiveTableExists() failed: " + e.getMessage());
            archiveTableExistsCache = false;
        }
        System.out.println("Schema — users_archive exists: " + archiveTableExistsCache);
        return archiveTableExistsCache;
    }



    private String resolveUserColumn(String filter) {
        if (filter == null) return "first_name";
        switch (filter) {
            case "ID":             return "user_id";
            case "Student ID":     return "student_id";
            case "First Name":     return "first_name";
            case "Last Name":      return "last_name";
            case "College":        return "college";
            case "Year Level":     return "year_level";
            case "Contact Number": return "contact_num";
            case "System Role":    return "system_role";
            default:               return "first_name";
        }
    }


    private AdminUsers mapRow(ResultSet rs, boolean isArchived) throws SQLException {
        AdminUsers user = new AdminUsers();
        user.setUserId(rs.getInt("user_id"));
        user.setSystemRole(rs.getString("system_role"));
        user.setStudentId(rs.getString("student_id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setCollege(rs.getString("college"));
        user.setYearLevel(rs.getString("year_level"));
        user.setCourseProgram(rs.getString("course_program"));
        user.setKarmaScore(rs.getInt("karma_score"));
        user.setArchived(isArchived);
        user.setContactNumber(rs.getString("contact_num"));
        user.setGcashNum(rs.getString("gcash_num"));
        user.setMayaNum(rs.getString("maya_num"));
        user.setMastercardNum(rs.getString("mastercard_num"));
        user.setVisaNum(rs.getString("visa_num"));
        return user;
    }
}