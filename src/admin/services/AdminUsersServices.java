package admin.services;

import admin.database.UsersDatabase;
import admin.database.LogsDatabase;
import admin.models.AdminUsers;
import java.util.List;

public class AdminUsersServices {

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

  

    public boolean updateUser(AdminUsers user) {
        boolean success = userDB.updateUser(user);
        if (success) {
            logDB.insertLog("USER", user.getUserId(), 0,
                "User " + user.getFullName() + " (ID " + user.getUserId() + ") was updated by admin.");
        }
        return success;
    }

   
    public boolean archiveUser(int userId) {
        AdminUsers user = userDB.getUserById(userId);

        if (user == null) {
            System.out.println("archiveUser() failed: user " + userId + " not found.");
            return false;
        }

        String role = user.getSystemRole();
        if (role != null && (role.equals("admin") || role.equals("super_admin"))) {
            System.out.println("archiveUser() blocked: cannot archive admin account (role=" + role + ").");
            return false;
        }

        return userDB.archiveUser(userId);
    }

   
    public boolean unarchiveUser(int userId) {
        AdminUsers user = userDB.getUserById(userId);

        if (user == null) {
            System.out.println("unarchiveUser() failed: user " + userId + " not found.");
            return false;
        }

        boolean success = userDB.unarchiveUser(userId);

        if (success) {
            logDB.insertLog("USER", userId, 0,
                "User " + user.getFullName() + " (ID " + userId + ") was restored from archive by admin.");
        }

        return success;
    }

    public boolean permanentDeleteUser(int userId) {
        // Fetch before deleting so we can include their name in the log
        AdminUsers user = userDB.getUserById(userId);
        String userLabel = (user != null)
            ? user.getFullName() + " (ID " + userId + ")"
            : "ID " + userId;

        boolean success = userDB.permanentDeleteUser(userId);

        if (success) {
            logDB.insertLog("USER", 0, 0,
                "User " + userLabel + " was permanently deleted from archive by admin.");
        }

        return success;
    }



    private Object[][] buildTableData(List<AdminUsers> users) {
        Object[][] data = new Object[users.size()][8];
        for (int i = 0; i < users.size(); i++) {
            AdminUsers u = users.get(i);
            data[i][0] = u.getUserId();
            data[i][1] = u.getStudentId();
            data[i][2] = u.getFirstName();
            data[i][3] = u.getLastName();
            data[i][4] = u.getCollege();
            data[i][5] = u.getYearLevel();
            data[i][6] = u.getKarmaScore();
            data[i][7] = u.getSystemRole();
        }
        return data;
    }
}