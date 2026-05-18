package admin.services;

import admin.database.LogsDatabase;
import admin.database.UsersDatabase;
import admin.models.AdminUsers;
import enums.UserLogAction;
import utils.HashUtil;
import utils.Permission;
import utils.SessionManager;

import java.util.List;

public class AdminUsersServices {

    private final UsersDatabase usersDB = new UsersDatabase();
    private final LogsDatabase  logDB   = new LogsDatabase();

    private String lastAddError = null;

   

    public int getTotalUsers() {
        return usersDB.countActiveUsers();
    }

    public Object[][] getActiveUsersForTable() {
        return buildTableData(usersDB.getAllUsers());
    }

    public Object[][] getArchivedUsersForTable() {
        return buildTableData(usersDB.getArchivedUsers());
    }

    public Object[][] searchActiveUsers(String filter, String keyword) {
        return buildTableData(usersDB.searchUsers(filter, keyword));
    }

    public Object[][] searchArchivedUsers(String filter, String keyword) {
        return buildTableData(usersDB.searchArchivedUsers(filter, keyword));
    }

    public AdminUsers getUserById(int userId) {
        return usersDB.getUserById(userId);
    }

    public String getLastAddError() {
        return lastAddError;
    }


    public boolean addUser(String studentId, String firstName, String lastName,
            String email, String password, String yearLevel,
            String college, String role, String profileImagePath) {


        Permission.require(Permission.canCreate(), "create users");

        lastAddError = null;

        String dup = usersDB.checkDuplicateUser(studentId, email);
        if ("student_id".equals(dup)) {
            lastAddError = "A user with student ID \"" + studentId + "\" already exists.";
            return false;
        }
        if ("email".equals(dup)) {
            lastAddError = "A user with email \"" + email + "\" already exists.";
            return false;
        }

        String hashedPassword = HashUtil.sha256(password);
        int newUserId = usersDB.insertUser(
                studentId, firstName, lastName, email, hashedPassword,
                yearLevel, college, role, profileImagePath);

        if (newUserId > 0) {
            logDB.insertLog(UserLogAction.USER_CREATE,
                    SessionManager.get().getCurrentUserId(),
                    "\"" + firstName + " " + lastName + "\"");
            return true;
        }
        lastAddError = "Database error — the user could not be saved. Please try again.";
        return false;
    }

 
    public boolean updateUser(AdminUsers user) {
        Permission.require(Permission.canUpdate(), "update users");

        boolean success = usersDB.updateUser(user);
        if (success)
            logDB.insertLog(UserLogAction.USER_UPDATE,
                    user.getUserId(),
                    "\"" + user.getFirstName() + " " + user.getLastName() + "\"");
        return success;
    }

    	public boolean archiveUser(int userId) {
    	    Permission.require(Permission.canDelete(), "archive users");

    	    AdminUsers target = usersDB.getUserById(userId);
    	    if (target != null) {
    	        String role = target.getSystemRole();
    	        if ("admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role)) {
    	        	lastAddError = "Error: Accounts cannot be archived.";
    	            return false;
    	        }
    	    }

    	    boolean success = usersDB.archiveUser(userId);
        if (success)
            logDB.insertLog(UserLogAction.USER_ARCHIVE,
                    SessionManager.get().getCurrentUserId(),
                    "User ID " + userId);
        return success;
    }

    public boolean unarchiveUser(int userId) {
        Permission.require(Permission.canUpdate(), "restore users");

        boolean success = usersDB.unarchiveUser(userId);
        if (success)
            logDB.insertLog(UserLogAction.USER_RESTORE,
                    SessionManager.get().getCurrentUserId(),
                    "User ID " + userId);
        return success;
    }


    public boolean permanentDeleteUser(int userId) {
        Permission.require(Permission.canDelete(), "permanently delete users");

        boolean success = usersDB.permanentDeleteUser(userId);
        if (success)
            logDB.insertLog(UserLogAction.USER_DELETE,
                    SessionManager.get().getCurrentUserId(),
                    "User ID " + userId);
        return success;
    }

    private Object[][] buildTableData(List<AdminUsers> users) {
        Object[][] data = new Object[users.size()][14];
        for (int i = 0; i < users.size(); i++) {
            AdminUsers u = users.get(i);
            data[i][0]  = Boolean.FALSE;
            data[i][1]  = u.getUserId();
            data[i][2]  = u.getStudentId();
            data[i][3]  = u.getFirstName();
            data[i][4]  = u.getLastName();
            data[i][5]  = u.getCollege();
            data[i][6]  = u.getYearLevel();
            data[i][7]  = u.getKarmaScore();
            data[i][8]  = u.getContactNumber();
            data[i][9]  = HashUtil.mask(u.getGcashNum());
            data[i][10] = HashUtil.mask(u.getMayaNum());
            data[i][11] = HashUtil.mask(u.getMastercardNum());
            data[i][12] = HashUtil.mask(u.getVisaNum());
            data[i][13] = u.getSystemRole();
        }
        return data;
    }
}