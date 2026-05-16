package admin.database;

import admin.models.AdminUsers;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDatabase {

    private static Boolean softDeleteColumnExists  = null;
    private static Boolean archiveTableExistsCache = null;


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
        String sql = hasSoftDeleteColumn()
            ? "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users WHERE deleted_at IS NULL ORDER BY user_id ASC"
            : "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users ORDER BY user_id ASC";

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
            String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users WHERE deleted_at IS NOT NULL ORDER BY user_id ASC";
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
            String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users_archive ORDER BY user_id ASC";
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
        String column          = resolveUserColumn(filter);
        String activeCondition = hasSoftDeleteColumn() ? " AND deleted_at IS NULL" : "";
        String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
            + "college, year_level, course_program, karma_score "
            + "FROM users WHERE " + column + " LIKE ?" + activeCondition;

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
            String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users WHERE " + column + " LIKE ? AND deleted_at IS NOT NULL";
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
            String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users_archive WHERE " + column + " LIKE ?";
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
        String base      = "SELECT user_id, system_role, student_id, first_name, last_name, "
            + "college, year_level, course_program, karma_score FROM users WHERE user_id = ?";
        String activeSql = hasSoftDeleteColumn() ? base + " AND deleted_at IS NULL" : base;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(activeSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, false);
        } catch (SQLException e) {
            System.err.println("getUserById() active lookup failed: " + e.getMessage());
        }

        if (hasSoftDeleteColumn()) {
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(base + " AND deleted_at IS NOT NULL")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return mapRow(rs, true);
            } catch (SQLException e) {
                System.err.println("getUserById() soft-delete archived lookup failed: " + e.getMessage());
            }
        }

        if (archiveTableExists()) {
            String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
                + "college, year_level, course_program, karma_score "
                + "FROM users_archive WHERE user_id = ?";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return mapRow(rs, true);
            } catch (SQLException e) {
                System.err.println("getUserById() archive table lookup failed: " + e.getMessage());
            }
        }
        return null;
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
            stmt.setInt(5, user.getKarmaScore());
            stmt.setString(6, user.getSystemRole());
            stmt.setInt(7, user.getUserId());
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

      
        String deleteRepLogByItem =
            "DELETE FROM reputation_log "
            + "WHERE transaction_id IN ("
            + "  SELECT transaction_id FROM transaction_log "
            + "  WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?)"
            + ")";

        
        String deleteRepLogByUser =
            "DELETE FROM reputation_log "
            + "WHERE transaction_id IN ("
            + "  SELECT transaction_id FROM transaction_log WHERE user_id = ?"
            + ")";

       
        String deleteItemsLogByItem =
            "DELETE FROM items_log "
            + "WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?)";

      
        String deleteItemsLogByUser =
            "DELETE FROM items_log WHERE user_id = ?";

        
        String deleteTransLogByItem =
            "DELETE FROM transaction_log "
            + "WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?)";

      
        String deleteTransLogByUser =
            "DELETE FROM transaction_log WHERE user_id = ?";

      
        String nullifyUsersLog =
            "UPDATE users_log SET user_id = NULL WHERE user_id = ?";

        
        String deleteItemsArchiveByOwner =
            "DELETE FROM items_archive WHERE owner_id = ?";

        
        String archiveItems =
            "INSERT INTO items_archive ("
            + "  item_id, owner_id, initiator_firstname, initiator_lastname,"
            + "  item_name, item_quantity, description, items_image,"
            + "  category, `condition`, price, availability_status,"
            + "  maximum_borrow_days, desired_item, date_listed,"
            + "  pickup_area, pickup_time, pickup_days, `action`"
            + ") "
            + "SELECT"
            + "  item_id, owner_id, initiator_firstname, initiator_lastname,"
            + "  item_name, item_quantity, description, items_image,"
            + "  CASE category WHEN 'Consumable-Goods' THEN 'Consumable' ELSE category END,"
            + "  `condition`, price, availability_status,"
            + "  maximum_borrow_days, desired_item, date_listed,"
            + "  pickup_area, pickup_time, pickup_days, `action`"
            + " FROM items WHERE owner_id = ?";

       
        String deleteItems =
            "DELETE FROM items WHERE owner_id = ?";


        String insertUserArchive =
            "INSERT INTO users_archive "
            + "(user_id, system_role, student_id, umak_email_address, password, "
            + " college, year_level, course_program, first_name, last_name, "
            + " karma_score, profile_image, contact, home_address, gcash_num, maya_num, mastercard_num, visa_num) "
            + "SELECT user_id, system_role, student_id, umak_email_address, password, "
            + "       college, year_level, course_program, first_name, last_name, "
            + "       karma_score, profile_image, contact_num, home_address, gcash_num, maya_num, mastercard_num, visa_num "
            + "FROM users WHERE user_id = ?";

        String deleteUser =
            "DELETE FROM users WHERE user_id = ?";

      
        String insertAuditLog =
            "INSERT INTO users_log (user_id, initiator_firstname, initiator_lastname, action, reason, timestamp) "
            + "VALUES (NULL, '', '', 'Archive', 'User archived by admin.', NOW())";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
             
                for (String sql : new String[]{ deleteRepLogByItem, deleteRepLogByUser }) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }

      
                for (String sql : new String[]{ deleteItemsLogByItem, deleteItemsLogByUser }) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }

                for (String sql : new String[]{ deleteTransLogByItem, deleteTransLogByUser }) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }

       
                try (PreparedStatement ps = conn.prepareStatement(nullifyUsersLog)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

    
                try (PreparedStatement ps = conn.prepareStatement(deleteItemsArchiveByOwner)) {
                    ps.setInt(1, userId);
                    int deleted = ps.executeUpdate();
                    System.out.println("archiveUser() removed " + deleted
                        + " row(s) from items_archive for user " + userId);
                }

       
                try (PreparedStatement ps = conn.prepareStatement(archiveItems)) {
                    ps.setInt(1, userId);
                    int archived = ps.executeUpdate();
                    System.out.println("archiveUser() copied " + archived
                        + " item(s) into items_archive for user " + userId);
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteItems)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertUserArchive)) {
                    ps.setInt(1, userId);
                    int rows = ps.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("User " + userId + " not found or already archived.");
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

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

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(insertAuditLog)) {
            ps.executeUpdate();
            System.out.println("archiveUser() audit log written for user " + userId + ".");
        } catch (SQLException e) {
            System.out.println("archiveUser() audit log skipped (non-fatal): " + e.getMessage());
        }

        return true;
    }

    public boolean unarchiveUser(int userId) {

        if (hasSoftDeleteColumn()) {
            String sql = "UPDATE users SET deleted_at = NULL WHERE user_id = ? AND deleted_at IS NOT NULL";
            try (Connection conn = getConn();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("unarchiveUser() soft-delete: restored user " + userId);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("unarchiveUser() soft-delete failed: " + e.getMessage());
            }
        }

        if (archiveTableExists()) {
            String insert =
                "INSERT INTO users "
                + "(user_id, system_role, student_id, umak_email_address, password, "
                + " college, year_level, course_program, first_name, last_name, "
                + " karma_score, profile_image, contact_num, home_address, gcash_num, maya_num, mastercard_num, visa_num) "
                + "SELECT user_id, system_role, student_id, umak_email_address, password, "
                + "       college, year_level, course_program, first_name, last_name, "
                + "       karma_score, profile_image, contact, home_address, gcash_num, maya_num, mastercard_num, visa_num "
                + "FROM users_archive WHERE user_id = ?";
            String delete = "DELETE FROM users_archive WHERE user_id = ?";

            try (Connection conn = getConn()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ins = conn.prepareStatement(insert);
                     PreparedStatement del = conn.prepareStatement(delete)) {
                    ins.setInt(1, userId);
                    int rows = ins.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("User " + userId + " not found in users_archive.");
                    del.setInt(1, userId);
                    del.executeUpdate();
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
            System.err.println("hasSoftDeleteColumn() check failed: " + e.getMessage());
            softDeleteColumnExists = false;
        }
        System.out.println("Schema detection — users.deleted_at exists: " + softDeleteColumnExists);
        return softDeleteColumnExists;
    }

    private boolean archiveTableExists() {
        if (archiveTableExistsCache != null) return archiveTableExistsCache;
        try (Connection conn = getConn()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "users_archive", new String[]{ "TABLE" })) {
                archiveTableExistsCache = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("archiveTableExists() check failed: " + e.getMessage());
            archiveTableExistsCache = false;
        }
        System.out.println("Schema detection — users_archive table exists: " + archiveTableExistsCache);
        return archiveTableExistsCache;
    }

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(
            DatabaseManager.getURL(),
            DatabaseManager.getUser(),
            DatabaseManager.getPassword()
        );
    }

    private String resolveUserColumn(String filter) {
        if (filter == null) return "first_name";
        switch (filter) {
            case "ID":          return "user_id";
            case "Student ID":  return "student_id";
            case "First Name":  return "first_name";
            case "Last Name":   return "last_name";
            case "College":     return "college";
            case "Year Level":  return "year_level";
            case "System Role": return "system_role";
            default:            return "first_name";
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
        return user;
    }
}