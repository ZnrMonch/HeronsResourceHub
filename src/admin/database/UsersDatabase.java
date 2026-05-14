package admin.database;

import admin.models.AdminUsers;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDatabase {

   
    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countActiveUsers() failed: " + e.getMessage());
        }
        return 0;
    }

   
    public List<AdminUsers> getAllUsers() {
        List<AdminUsers> list = new ArrayList<>();
        String sql = "SELECT user_id, system_role, student_id, first_name, last_name, " +
                     "college, year_level, course_program, karma_score FROM users";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.out.println("getAllUsers() failed: " + e.getMessage());
        }
        return list;
    }

    
    public List<AdminUsers> getArchivedUsers() {
        List<AdminUsers> list = new ArrayList<>();
        String sql = "SELECT user_id, system_role, student_id, first_name, last_name, " +
                     "college, year_level, course_program, karma_score FROM users_archive";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) list.add(mapRow(rs, true));
        } catch (SQLException e) {
            System.out.println("getArchivedUsers() failed: " + e.getMessage());
        }
        return list;
    }

    
    // NOT YET WORKING
    public List<AdminUsers> searchUsers(String filter, String keyword) {
        List<AdminUsers> list = new ArrayList<>();


        String column;
        switch (filter) {
            case "ID":         column = "user_id";    break;
            case "Student ID": column = "student_id"; break;
            case "First Name": column = "first_name"; break;
            case "Last Name":  column = "last_name";  break;
            case "College":    column = "college";    break;
            default:           column = "first_name"; break;
        }

        // LIKE with % means partial match — "Jo" finds "John", "Jojo", etc.
        String sql = "SELECT user_id, system_role, student_id, first_name, last_name, " +
                     "college, year_level, course_program, karma_score FROM users " +
                     "WHERE " + column + " LIKE ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.out.println("searchUsers() failed: " + e.getMessage());
        }
        return list;
    }

    
    public List<AdminUsers> searchArchivedUsers(String filter, String keyword) {
        List<AdminUsers> list = new ArrayList<>();

        String column;
        switch (filter) {
            case "ID":         column = "user_id";    break;
            case "Student ID": column = "student_id"; break;
            case "First Name": column = "first_name"; break;
            case "Last Name":  column = "last_name";  break;
            case "College":    column = "college";    break;
            default:           column = "first_name"; break;
        }

        String sql = "SELECT user_id, system_role, student_id, first_name, last_name, " +
                     "college, year_level, course_program, karma_score FROM users_archive " +
                     "WHERE " + column + " LIKE ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, true));
        } catch (SQLException e) {
            System.out.println("searchArchivedUsers() failed: " + e.getMessage());
        }
        return list;
    }

   
    public AdminUsers getUserById(int userId) {
        String sql = "SELECT user_id, system_role, student_id, first_name, last_name, " +
                     "college, year_level, course_program, karma_score FROM users WHERE user_id = ?";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, false);
        } catch (SQLException e) {
            System.out.println("getUserById() failed: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUser(AdminUsers user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, college = ?, " +
                     "year_level = ?, karma_score = ? WHERE user_id = ?";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
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
            System.out.println("updateUser() failed: " + e.getMessage());
        }
        return false;
    }

  
    public boolean archiveUser(int userId) {
        String insert = "INSERT INTO users_archive (user_id, system_role, student_id, " +
                        "first_name, last_name, college, year_level, course_program, karma_score) " +
                        "SELECT user_id, system_role, student_id, first_name, last_name, " +
                        "college, year_level, course_program, karma_score " +
                        "FROM users WHERE user_id = ?";
        String delete = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false); // start transaction

            try (
                PreparedStatement ins = conn.prepareStatement(insert);
                PreparedStatement del = conn.prepareStatement(delete)
            ) {
                ins.setInt(1, userId);
                ins.executeUpdate(); 

                del.setInt(1, userId);
                del.executeUpdate(); 

                conn.commit(); 
                return true;
            } catch (SQLException e) {
                conn.rollback(); // one failed — undo both
                System.out.println("archiveUser() failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("archiveUser() connection failed: " + e.getMessage());
        }
        return false;
    }

   
    public boolean unarchiveUser(int userId) {
        String insert = "INSERT INTO users (user_id, system_role, student_id, " +
                        "first_name, last_name, college, year_level, course_program, karma_score) " +
                        "SELECT user_id, system_role, student_id, first_name, last_name, " +
                        "college, year_level, course_program, karma_score " +
                        "FROM users_archive WHERE user_id = ?";
        String delete = "DELETE FROM users_archive WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement ins = conn.prepareStatement(insert);
                PreparedStatement del = conn.prepareStatement(delete)
            ) {
                ins.setInt(1, userId);
                ins.executeUpdate();

                del.setInt(1, userId);
                del.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("unarchiveUser() failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("unarchiveUser() connection failed: " + e.getMessage());
        }
        return false;
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