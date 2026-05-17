package enums;

/**
 * Role.java
 * ---------
 * Defines the two roles in the system and what each one is ALLOWED to do.
 *
 * WHY: Having all permission rules in ONE place means you only need to
 * update this file if rules ever change — no hunting through every screen.
 *
 * USAGE:
 *   Role role = Role.from(SessionManager.get().getCurrentUserRole());
 *   if (role.canCreate()) { ... }
 */
public enum Role {

    // ── Role definitions ─────────────────────────────────────────────────────

    SUPER_ADMIN(true,  true,  true,  true),   // full access
    ADMIN      (false, true,  false, false),   // read-only
    UNKNOWN    (false, false, false, false);   // not logged in / bad role string

    // ── Permission flags ─────────────────────────────────────────────────────

    private final boolean canCreate;   // add new records
    private final boolean canRead;     // view records
    private final boolean canUpdate;   // edit / update records
    private final boolean canDelete;   // archive or permanently delete records

    // ── Constructor ───────────────────────────────────────────────────────────

    Role(boolean canCreate, boolean canRead, boolean canUpdate, boolean canDelete) {
        this.canCreate = canCreate;
        this.canRead   = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean canCreate() { return canCreate; }
    public boolean canRead()   { return canRead;   }
    public boolean canUpdate() { return canUpdate; }
    public boolean canDelete() { return canDelete; }

    // ── Convenience helpers ───────────────────────────────────────────────────

    /** True only for SUPER_ADMIN — use this as your main admin gate. */
    public boolean isSuperAdmin() { return this == SUPER_ADMIN; }

    /** True for both ADMIN and SUPER_ADMIN. */
    public boolean isAnyAdmin()   { return this == SUPER_ADMIN || this == ADMIN; }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Converts the raw role string stored in the session into a Role enum value.
     * Returns UNKNOWN for null or unrecognised strings — never crashes.
     *
     * Example:
     *   Role role = Role.from("super_admin");  // → Role.SUPER_ADMIN
     *   Role role = Role.from(null);           // → Role.UNKNOWN
     */
    public static Role from(String roleString) {
        if (roleString == null) return UNKNOWN;
        switch (roleString.trim().toLowerCase()) {
            case "super_admin": return SUPER_ADMIN;
            case "admin":       return ADMIN;
            default:            return UNKNOWN;
        }
    }
}