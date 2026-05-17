package enums;

/**
 * Actions recorded in the users_log table.
 * DB column type: enum('Create','Update','Archive','Retrieve','Delete')
 *
 * HOW TO READ THIS:
 *   Each constant has two values:
 *     dbValue     — the exact string stored in the database enum column
 *     description — the human-readable text stored in the "reason" column
 *
 * USAGE:
 *   logDB.insertLog(UserLogAction.USER_CREATE, userId, context);
 *   logDB.insertLog(UserLogAction.USER_ARCHIVE, userId, "User ID " + id);
 */
public enum UserLogAction {

    // ── Create ────────────────────────────────────────────────────────────────
    USER_REGISTER("Create", "New user registered"),
    USER_CREATE  ("Create", "User created by admin"),   // used in AdminUsersServices.addUser()

    // ── Update ────────────────────────────────────────────────────────────────
    USER_UPDATE         ("Update", "User data updated"),
    USER_UPDATE_PROFILE ("Update", "Profile updated"),
    USER_CHANGE_PASSWORD("Update", "Password changed"),
    USER_UPLOAD_PROFILE ("Update", "Profile image uploaded"),
    USER_ROLE_UPDATE    ("Update", "System role updated"),

    // ── Archive ───────────────────────────────────────────────────────────────
    USER_ARCHIVE("Archive", "User archived"),

    // ── Retrieve (restore from archive) ──────────────────────────────────────
    // NOTE: USER_UNARCHIVE and USER_RESTORE were duplicates — kept USER_RESTORE
    // because that is what AdminUsersServices references.
    USER_RESTORE("Retrieve", "User restored from archive"),

    // ── Delete ────────────────────────────────────────────────────────────────
    USER_DELETE("Delete", "User permanently deleted");

    // ── Fields ────────────────────────────────────────────────────────────────

    private final String dbValue;
    private final String description;

    UserLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

    /** The exact value stored in the DB enum column. */
    public String getDbValue()     { return dbValue;     }

    /** Human-readable log description written to the reason column. */
    public String getDescription() { return description; }
}