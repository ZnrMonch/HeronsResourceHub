package admin.services;

import admin.database.LogsDatabase;
import admin.database.UsersDatabase;
import admin.models.AdminUsers;
import java.util.List;

public class AdminUsersServices {

    private String lastAddError = null;

    public String getLastAddError() {
        return lastAddError;
    }

    private final UsersDatabase userDB = new UsersDatabase();
    private final LogsDatabase  logDB  = new LogsDatabase();

    public int getTotalUsers() {
        return userDB.countActiveUsers();
    }

    public Object[][] getActiveUsersForTable() {
        return buildTableData(userDB.getAllUsers());
    }

    public Object[][] getArchivedUsersForTable() {
        return buildTableData(userDB.getArchivedUsers());
    }

    public Object[][] searchActiveUsers(String filter, String keyword) {
        return buildTableData(userDB.searchUsers(filter, keyword));
    }

    public Object[][] searchArchivedUsers(String filter, String keyword) {
        return buildTableData(userDB.searchArchivedUsers(filter, keyword));
    }

    public AdminUsers getUserById(int userId) {
        return userDB.getUserById(userId);
    }

    public boolean addUser(String studentId, String firstName, String lastName,
            String email, String password, String yearLevel,
            String college, String role, String profileImagePath) {

        String duplicate = userDB.checkDuplicateUser(studentId, email);
        if (duplicate != null) {
            lastAddError = duplicate.equals("student_id")
                ? "Student ID \"" + studentId + "\" is already registered."
                : "Email \"" + email + "\" is already registered.";
            return false;
        }

        int newUserId = userDB.insertUser(studentId, firstName, lastName,
                                          email, password, yearLevel, college, role,
                                          profileImagePath);
        if (newUserId > 0) {
            lastAddError = null;
            logDB.insertLog("USER", newUserId, 0,
                "New user " + firstName + " " + lastName
                + " (" + studentId + ") added by admin.");
            return true;
        } else {
            lastAddError = "Database error. Please try again.";
            return false;
        }
    }

    public boolean updateUser(AdminUsers user) {
        boolean success = userDB.updateUser(user);
        if (success)
            logDB.insertLog("USER", user.getUserId(), 0,
                "User " + user.getFullName() + " (ID " + user.getUserId() + ") was updated by admin.");
        return success;
    }

    public boolean archiveUser(int userId) {
        AdminUsers user = userDB.getUserById(userId);
        if (user == null) {
            System.out.println("archiveUser() failed: user " + userId + " not found.");
            return false;
        }
        String role = user.getSystemRole();
        if ("admin".equals(role) || "super_admin".equals(role)) {
            System.out.println("archiveUser() blocked: cannot archive admin account (role=" + role + ").");
            return false;
        }
        // Snapshot name before the row is deleted
        String fullName = user.getFullName();
        boolean success = userDB.archiveUser(userId);
        if (success)
        
            logDB.insertLog("USER", 0, 0,
                "User " + fullName + " (ID " + userId + ") archived by admin.");
        return success;
    }

    public boolean unarchiveUser(int userId) {
        AdminUsers user = userDB.getUserFromArchiveById(userId);
        if (user == null) {
            System.out.println("unarchiveUser() failed: user " + userId + " not found in archive.");
            return false;
        }
        String fullName = user.getFullName();
        boolean success = userDB.unarchiveUser(userId);
        if (success)
            // userId is safe to pass — user row is restored before this runs
            logDB.insertLog("USER", userId, 0,
                "User " + fullName + " (ID " + userId + ") restored by admin.");
        return success;
    }

    public boolean permanentDeleteUser(int userId) {
        boolean success = userDB.permanentDeleteUser(userId);
        if (success)
            logDB.insertLog("USER", 0, 0,
                "User ID " + userId + " permanently deleted from archive by admin.");
        return success;
    }

    private Object[][] buildTableData(List<AdminUsers> users) {
        Object[][] data = new Object[users.size()][13];
        for (int i = 0; i < users.size(); i++) {
            AdminUsers u = users.get(i);
            data[i][0]  = u.getUserId();
            data[i][1]  = u.getStudentId();
            data[i][2]  = u.getFirstName();
            data[i][3]  = u.getLastName();
            data[i][4]  = u.getCollege();
            data[i][5]  = u.getYearLevel();
            data[i][6]  = u.getKarmaScore();
            data[i][7]  = u.getContactNumber();
            data[i][8]  = u.getGcashNum();
            data[i][9]  = u.getMayaNum();
            data[i][10] = u.getMastercardNum();
            data[i][11] = u.getVisaNum();
            data[i][12] = u.getSystemRole();
        }
        return data;
    }
}