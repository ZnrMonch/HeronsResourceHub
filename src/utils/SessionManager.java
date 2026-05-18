package utils;

public class SessionManager {

    private static SessionManager instance = null;

    private int    userId;
    private String userRole;
    private String fullName;

    private SessionManager() {}

    public static SessionManager get() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public static void login(int userId, String userRole, String fullName) {
        SessionManager s = get();
        s.userId   = userId;
        s.userRole = userRole;
        s.fullName = fullName;
    }

    public static void logout() {
        instance = null;
    }
  
    public int    getCurrentUserId()   { return userId; }
    public String getCurrentUserRole() { return userRole; }
    public String getCurrentFullName() { return fullName; }

   
    public boolean isAdmin() {
        return "admin".equals(userRole) || "super_admin".equals(userRole);
    }
}