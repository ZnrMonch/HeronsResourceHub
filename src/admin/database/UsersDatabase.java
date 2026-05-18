package admin.database;

import admin.models.AdminUsers;
import utils.Encryption;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Handles all database operations for the users table, including payment methods
public class UsersDatabase extends BaseDatabase {

 private static final String SELECT_USERS_COLS =
     "user_id, system_role, student_id, first_name, last_name, "
     + "college, year_level, course_program, karma_score, "
     + "contact_num, home_address, "
     + "users_is_archived, users_archived_at";

 // ── Read ──

 // Excludes archived users from the count
 public int countActiveUsers() {
     String sql = "SELECT COUNT(*) FROM users WHERE users_is_archived = 0";
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
     String sql = "SELECT " + SELECT_USERS_COLS
                + " FROM users WHERE users_is_archived = 0 ORDER BY user_id ASC";
     List<AdminUsers> list = new ArrayList<>();
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.err.println("getAllUsers() failed: " + e.getMessage());
     }
     return list;
 }

 public List<AdminUsers> getArchivedUsers() {
     String sql = "SELECT " + SELECT_USERS_COLS
                + " FROM users WHERE users_is_archived = 1 ORDER BY user_id ASC";
     List<AdminUsers> list = new ArrayList<>();
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.err.println("getArchivedUsers() failed: " + e.getMessage());
     }
     return list;
 }

 public AdminUsers getUserById(int userId) {
     String sql = "SELECT " + SELECT_USERS_COLS + " FROM users WHERE user_id = ?";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1, userId);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) return mapRow(rs);
     } catch (SQLException e) {
         System.err.println("getUserById() failed: " + e.getMessage());
     }
     return null;
 }

 // Shared search path for both active and archived; avoids duplicating two near-identical methods
 public List<AdminUsers> searchUsers(String filter, String keyword) {
     return search(filter, keyword, false);
 }

 public List<AdminUsers> searchArchivedUsers(String filter, String keyword) {
     return search(filter, keyword, true);
 }

 // Returns the payment_number decrypted; falls back to raw value for legacy unencrypted rows
 public Map<String, String> getPaymentMethods(int userId) {
     Map<String, String> methods = new HashMap<>();
     String sql = "SELECT payment_type, payment_number "
                + "FROM payment_methods WHERE user_id = ? AND is_active = 1";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1, userId);
         ResultSet rs = stmt.executeQuery();
         while (rs.next()) {
             String raw       = rs.getString("payment_number");
             String decrypted = Encryption.decrypt(raw);
             methods.put(rs.getString("payment_type"),
                         decrypted != null ? decrypted : raw);
         }
     } catch (SQLException e) {
         System.err.println("getPaymentMethods() failed: " + e.getMessage());
     }
     return methods;
 }

 // ── Write ──

 // Returns "student_id" or "email" if a duplicate exists, null if clear
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

 // Returns the generated user_id, or -1 on failure
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

 // AES-encrypts the payment number before storage; upserts on (user_id, payment_type)
 public boolean savePaymentMethod(int userId, String paymentType, String paymentNumber) {
     String sql = "INSERT INTO payment_methods (user_id, payment_type, payment_number, is_active) "
                + "VALUES (?, ?, ?, 1) "
                + "ON DUPLICATE KEY UPDATE payment_number = VALUES(payment_number), is_active = 1";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1,    userId);
         stmt.setString(2, paymentType);
         stmt.setString(3, Encryption.encrypt(paymentNumber));
         return stmt.executeUpdate() > 0;
     } catch (SQLException e) {
         System.err.println("savePaymentMethod() failed: " + e.getMessage());
     }
     return false;
 }

 // ── Archive ──

 // Cascades to the user's items so archived owners don't leave active listings
 public boolean archiveUser(int userId) {
     String archiveUser  = "UPDATE users SET users_is_archived = 1, users_archived_at = NOW() WHERE user_id = ?";
     String archiveItems = "UPDATE items SET items_is_archived = 1, items_archived_at = NOW() WHERE owner_id = ?";
     return executeUserArchiveToggle(userId, archiveUser, archiveItems, "archiveUser()");
 }

 // Cascades to the user's items so restored owners get their listings back
 public boolean unarchiveUser(int userId) {
     String unarchiveUser  = "UPDATE users SET users_is_archived = 0, users_archived_at = NULL WHERE user_id = ?";
     String unarchiveItems = "UPDATE items SET items_is_archived = 0, items_archived_at = NULL WHERE owner_id = ?";
     return executeUserArchiveToggle(userId, unarchiveUser, unarchiveItems, "unarchiveUser()");
 }

 // Guard: only rows already archived can be permanently deleted
 public boolean permanentDeleteUser(int userId) {
     String sql = "DELETE FROM users WHERE user_id = ? AND users_is_archived = 1";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1, userId);
         int rows = stmt.executeUpdate();
         if (rows > 0) {
             System.out.println("permanentDeleteUser() success: user " + userId + " permanently deleted.");
             return true;
         } else {
             System.out.println("permanentDeleteUser() failed: user " + userId + " not found or not archived.");
         }
     } catch (SQLException e) {
         System.err.println("permanentDeleteUser() failed: " + e.getMessage());
     }
     return false;
 }

 // ── Helpers ──

 // Shared search path for active/archived; avoids duplicating two near-identical methods
 private List<AdminUsers> search(String filter, String keyword, boolean archived) {
     List<AdminUsers> list = new ArrayList<>();
     int archiveFlag = archived ? 1 : 0;
     String label    = archived ? "searchArchivedUsers" : "searchUsers";

     if ("All".equals(filter)) {
         String sql = "SELECT " + SELECT_USERS_COLS
                 + " FROM users WHERE ("
                 + "  CAST(user_id AS CHAR) LIKE ? OR"
                 + "  student_id            LIKE ? OR"
                 + "  first_name            LIKE ? OR"
                 + "  last_name             LIKE ? OR"
                 + "  college               LIKE ? OR"
                 + "  year_level            LIKE ? OR"
                 + "  system_role           LIKE ? OR"
                 + "  contact_num           LIKE ?"
                 + ") AND users_is_archived = " + archiveFlag;
         try (Connection conn = getConn();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             String like = "%" + keyword + "%";
             for (int i = 1; i <= 8; i++) stmt.setString(i, like);
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) list.add(mapRow(rs));
         } catch (SQLException e) {
             System.err.println(label + "(All) failed: " + e.getMessage());
         }
         return list;
     }

     String column = resolveUserColumn(filter);
     String sql = "SELECT " + SELECT_USERS_COLS
                + " FROM users WHERE " + column + " LIKE ? AND users_is_archived = " + archiveFlag;
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setString(1, "%" + keyword + "%");
         ResultSet rs = stmt.executeQuery();
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.err.println(label + "() failed: " + e.getMessage());
     }
     return list;
 }

 // Wraps archive/unarchive in a transaction so user and items are always toggled together
 private boolean executeUserArchiveToggle(int userId, String userSql, String itemsSql, String label) {
     Connection conn = null;
     try {
         conn = getConn();
         conn.setAutoCommit(false);
         int userRowsAffected;
         try (PreparedStatement ps = conn.prepareStatement(userSql)) {
             ps.setInt(1, userId);
             userRowsAffected = ps.executeUpdate();
         }
         try (PreparedStatement ps = conn.prepareStatement(itemsSql)) {
             ps.setInt(1, userId);
             ps.executeUpdate();
         }
         conn.commit();
         return userRowsAffected >= 1;
     } catch (SQLException e) {
         System.err.println(label + " rolled back: " + e.getMessage());
         if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
         return false;
     } finally {
         if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
     }
 }

 // Defaults unknown filter values to first_name rather than failing silently
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

 private AdminUsers mapRow(ResultSet rs) throws SQLException {
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
     user.setArchived(rs.getInt("users_is_archived") == 1);
     user.setArchivedAt(rs.getTimestamp("users_archived_at"));
     user.setContactNumber(rs.getString("contact_num"));
     user.setHomeAddress(rs.getString("home_address"));
     return user;
 }
}