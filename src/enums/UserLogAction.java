package enums;



public enum UserLogAction {

    USER_REGISTER("Create", "New user registered"),
    USER_CREATE  ("Create", "User created by admin"),  
    USER_UPDATE         ("Update", "User data updated"),
    USER_UPDATE_PROFILE ("Update", "Profile updated"),
    USER_CHANGE_PASSWORD("Update", "Password changed"),
    USER_UPLOAD_PROFILE ("Update", "Profile image uploaded"),
    USER_ROLE_UPDATE    ("Update", "System role updated"),
    USER_ARCHIVE("Archive", "User archived"),
    USER_RESTORE("Retrieve", "User restored from archive"),
    USER_DELETE("Delete", "User permanently deleted");


    private final String dbValue;
    private final String description;

    UserLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

   
    public String getDbValue()     { return dbValue;     }
    public String getDescription() { return description; }
}