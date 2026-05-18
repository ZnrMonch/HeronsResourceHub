package utils;
import enums.*;

public class Permission {

   
    private Permission() {}

    public static Role currentRole() {
        return Role.from(SessionManager.get().getCurrentUserRole());
    }

    public static boolean canCreate() { return currentRole().canCreate(); }
    public static boolean canRead()   { return currentRole().canRead();   }
    public static boolean canUpdate() { return currentRole().canUpdate(); }
    public static boolean canDelete() { return currentRole().canDelete(); }

    public static boolean isSuperAdmin() { return currentRole().isSuperAdmin(); }
    public static boolean isAnyAdmin()   { return currentRole().isAnyAdmin();   }

    public static void require(boolean allowed, String actionName) {
        if (!allowed) {
            throw new SecurityException(
                "Access denied: your role (" + currentRole().name() + ") "
                + "is not permitted to " + actionName + "."
            );
        }
    }
}