package admin.database;

import admin.models.User;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This class is the ONLY place that runs SQL for the users table.
// It talks directly to the database using JDBC.
// Nothing else in the project should write SQL for users.

public class UsersDatabase {

    // Returns all users where archived = 0 (active users)
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE archived = 0";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("UserDatabase.getAllUsers() error: " + e.getMessage());
        }

        return list;
    }

    // Returns all users where archived = 1 (archived users)
    public List<User> getArchivedUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE archived = 1";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("UserDatabase.getArchivedUsers() error: " + e.getMessage());
        }

        return list;
    }

    // Returns one user by their ID, or null if not found
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.out.println("UserDatabase.getUserById() error: " + e.getMessage());
        }

        return null;
    }

    // Returns the total count of active users (used for the status card)
    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE archived = 0";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1); // getInt(1) = first column of the result
            }
        } catch (SQLException e) {
            System.out.println("UserDatabase.countActiveUsers() error: " + e.getMessage());
        }

        return 0;
    }

    // Sets archived = 1 for a user — does NOT delete from DB
    // Returns true if successful
    public boolean archiveUser(int userId) {
        String sql = "UPDATE users SET archived = 1 WHERE user_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDatabase.archiveUser() error: " + e.getMessage());
        }

        return false;
    }

    // Sets archived = 0 — restores a user from the archive
    public boolean unarchiveUser(int userId) {
        String sql = "UPDATE users SET archived = 0 WHERE user_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDatabase.unarchiveUser() error: " + e.getMessage());
        }

        return false;
    }

    // Updates editable fields for a user
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, college = ?, year_level = ?, karma_score = ? WHERE user_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getCollege());
            stmt.setString(4, user.getYearLevel());
            stmt.setInt(5, user.getKarmaScore());
            stmt.setInt(6, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDatabase.updateUser() error: " + e.getMessage());
        }

        return false;
    }

    // Converts one database row into a User object
    // Private because only this class needs it
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setSystemRole(rs.getString("system_role"));
        user.setStudentId(rs.getString("student_id"));
        user.setEmail(rs.getString("umak_email_address"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setCollege(rs.getString("college"));
        user.setYearLevel(rs.getString("year_level"));
        user.setCourseProgram(rs.getString("course_program"));
        user.setKarmaScore(rs.getInt("karma_score"));
        user.setArchived(rs.getBoolean("archived"));
        return user;
    }
}