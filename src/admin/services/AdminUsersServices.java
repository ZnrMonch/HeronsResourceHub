package admin.services;

import admin.database.LogsDatabase;
import admin.database.UsersDatabase;
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
        if (success)
            insertLog(user.getUserId(),
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
        return userDB.archiveUser(userId);
    }

    public boolean unarchiveUser(int userId) {
      
        AdminUsers user = userDB.getUserFromArchiveById(userId);
        if (user == null) {
            System.out.println("unarchiveUser() failed: user " + userId + " not found in archive.");
            return false;
        }
        return userDB.unarchiveUser(userId);
    }

    public boolean permanentDeleteUser(int userId) {
        return userDB.permanentDeleteUser(userId);
    }

    // Inserts a USER audit log entry
    private void insertLog(int userId, String message) {
        logDB.insertLog("USER", userId, 0, message);
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
            data[i][9] = u.getMayaNum();
            data[i][10] = u.getMastercardNum();
            data[i][11] = u.getVisaNum();
            data[i][12]  = u.getSystemRole();
        }
        return data;
    }
}