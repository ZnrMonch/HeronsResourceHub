package admin.services;

import admin.database.UserDatabase;
import admin.database.LogsDatabase;
import admin.models.AdminUsers;

import java.util.List;

public class AdminUsersServices {

    private UserDatabase userDB = new UserDatabase();
    private LogsDatabase logDB  = new LogsDatabase();

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
        return userDB.updateUser(user);
    }

    public boolean archiveUser(int userId) {
        AdminUsers user = userDB.getUserById(userId);

        if (user == null) {
            System.out.println("Archive failed: user not found.");
            return false;
        }

        String role = user.getSystemRole();
        if (role != null && (role.equals("admin") || role.equals("super_admin"))) {
            System.out.println("Archive failed: cannot archive an admin account.");
            return false;
        }

        return userDB.archiveUser(userId);
    }

    public boolean unarchiveUser(int userId) {
        return userDB.unarchiveUser(userId);
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