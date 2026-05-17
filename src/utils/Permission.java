package utils;
import enums.*;

/**
 * Permission.java
 * ---------------
 * A simple helper class that reads the current user's role from the session
 * and exposes easy boolean checks.
 *
 * WHY: Instead of writing Role.from(SessionManager.get().getCurrentUserRole())
 * everywhere, you just call Permission.canUpdate() — much more readable.
 *
 * USAGE (anywhere in the project):
 *   if (Permission.canCreate()) { btnAdd.setVisible(true); }
 *   if (Permission.isSuperAdmin()) { showSettingsPanel(); }
 *   Permission.require(Permission.canDelete(), "delete records"); // throws if denied
 */
public class Permission {

    // ── Private constructor — this is a utility class, never instantiate it ──
    private Permission() {}

    // ── Current role ──────────────────────────────────────────────────────────

    /**
     * Gets the Role enum for whoever is currently logged in.
     * Always safe to call — returns Role.UNKNOWN if nobody is logged in.
     */
    public static Role currentRole() {
        return Role.from(SessionManager.get().getCurrentUserRole());
    }

    // ── Simple boolean checks ─────────────────────────────────────────────────
    // Use these directly in button visibility logic, service methods, etc.

    public static boolean canCreate() { return currentRole().canCreate(); }
    public static boolean canRead()   { return currentRole().canRead();   }
    public static boolean canUpdate() { return currentRole().canUpdate(); }
    public static boolean canDelete() { return currentRole().canDelete(); }

    public static boolean isSuperAdmin() { return currentRole().isSuperAdmin(); }
    public static boolean isAnyAdmin()   { return currentRole().isAnyAdmin();   }

    // ── Hard guard — use in service/database methods ──────────────────────────

    /**
     * Throws an exception if the current user does NOT have permission.
     * Use this at the TOP of service methods that should never be reached
     * by an ADMIN — it stops the operation before touching the database.
     *
     * Example:
     *   public boolean archiveUser(int userId) {
     *       Permission.require(Permission.canDelete(), "archive users");
     *       // ... rest of method
     *   }
     *
     * @param allowed     The result of a canXxx() check
     * @param actionName  Human-readable name shown in the error message
     * @throws SecurityException if allowed is false
     */
    public static void require(boolean allowed, String actionName) {
        if (!allowed) {
            throw new SecurityException(
                "Access denied: your role (" + currentRole().name() + ") "
                + "is not permitted to " + actionName + "."
            );
        }
    }
}