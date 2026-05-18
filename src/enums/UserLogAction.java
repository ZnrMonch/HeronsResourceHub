package enums;

public enum UserLogAction {

    USER_CREATE( "Create",   "User created"),
    USER_UPDATE( "Update",   "User updated"),
    USER_ARCHIVE("Archive",  "User archived"),
    USER_RESTORE("Retrieve", "User restored"),
    USER_DELETE( "Delete",   "User permanently deleted");

    private final String dbValue;
    private final String description;

    UserLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

    public String getDbValue()     { return dbValue; }
    public String getDescription() { return description; }
}