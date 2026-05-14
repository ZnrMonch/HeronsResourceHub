package admin.services;

import admin.database.UserDatabase;
import admin.database.LogDatabase;
import admin.models.User;

import java.util.List;

// AdminUserService is what AdminTable and AdminPanel call.
// It contains the business rules — things like:
//   "don't archive an admin account"
//   "always write a log after every action"
// It calls UserDatabase and LogDatabase to do the actual DB work.

public class AdminUserService {

    // These are the database classes we use
    private UserDatabase userDB = new UserDatabase();
    private LogDatabase logDB   = new LogDatabase();

    // -------------------------------------------------------
    // READ — called when the table loads
    // -------------------------------------------------------

    // Returns all active users as a 2D array ready for JTable
    // Each row = { ID, StudentID, FirstName, LastName, College, Year, Karma }
    public Object[][] getActiveUsersForTable() {
        List<User> users = userDB.getAllUsers();
        Object[][] data = new Object[users.size()][7];

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            data[i][0] = u.getUserId();
            data[i][1] = u.getStudentId();
            data[i][2] = u.getFirstName();
            data[i][3] = u.getLastName();
            data[i][4] = u.getCollege();
            data[i][5] = u.getYearLevel();
            data[i][6] = u.getKarmaScore();
        }

        return data;
    }

    // Returns all archived users as a 2D array ready for JTable
    public Object[][] getArchivedUsersForTable() {
        List<User> users = userDB.getArchivedUsers();
        Object[][] data = new Object[users.size()][7];

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            data[i][0] = u.getUserId();
            data[i][1] = u.getStudentId();
            data[i][2] = u.getFirstName();
            data[i][3] = u.getLastName();
            data[i][4] = u.getCollege();
            data[i][5] = u.getYearLevel();
            data[i][6] = u.getKarmaScore();
        }

        return data;
    }

    // Returns total count of active users (for the status card in AdminPanel)
    public int getTotalUsers() {
        return userDB.countActiveUsers();
    }

    // Returns one User object by ID (used to pre-fill the update dialog)
    public User getUserById(int userId) {
        return userDB.getUserById(userId);
    }

    // -------------------------------------------------------
    // ARCHIVE — called by handleArchive() in AdminTable
    // -------------------------------------------------------

    // Archives a user and writes a log entry
    // Returns true if successful, false if not (e.g. user is an admin)
    public boolean archiveUser(int userId) {
        User user = userDB.getUserById(userId);

        if (user == null) {
            System.out.println("Archive failed: user not found.");
            return false;
        }

        // Business rule: never allow archiving admin accounts
        String role = user.getSystemRole();
        if (role.equals("admin") || role.equals("super_admin")) {
            System.out.println("Archive failed: cannot archive an admin account.");
            return false;
        }

        boolean success = userDB.archiveUser(userId);

        if (success) {
            // Always log the action
            logDB.insertLog("USER", userId, 0,
                "User " + user.getFullName() + " was archived by admin.");
        }

        return success;
    }

    // -------------------------------------------------------
    // UNARCHIVE — called by handleRetrieve() in AdminTable
    // -------------------------------------------------------

    // Restores a user from archive and writes a log entry
    public boolean unarchiveUser(int userId) {
        User user = userDB.getUserById(userId);

        if (user == null) {
            System.out.println("Unarchive failed: user not found.");
            return false;
        }

        boolean success = userDB.unarchiveUser(userId);

        if (success) {
            logDB.insertLog("USER", userId, 0,
                "User " + user.getFullName() + " was restored by admin.");
        }

        return success;
    }

    // -------------------------------------------------------
    // UPDATE — called by handleUpdate() in AdminTable
    // -------------------------------------------------------

    // Updates a user's details and writes a log entry
    public boolean updateUser(User user) {
        boolean success = userDB.updateUser(user);

        if (success) {
            logDB.insertLog("USER", user.getUserId(), 0,
                "User " + user.getFullName() + " details were updated by admin.");
        }

        return success;
    }
}