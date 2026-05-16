package enums;

public enum UserLogAction {
    USER_REGISTER("New user registered."),
    USER_LOGIN("User logged in successfully."),
    USER_LOGOUT("User logged out successfully."),
    USER_UPDATE_PROFILE("User profile updated."),
    USER_CHANGE_PASSWORD("User password changed."),
    USER_UPLOAD_PROFILE("User uploaded a profile picture."),
    USER_ARCHIVE("User archived by admin."),
    USER_UNARCHIVE("User unarchived by admin."),
    USER_RESTORE("User restored by admin."),
    USER_DELETE("User deleted by admin."),
    USER_ROLE_UPDATE("User role updated by admin.");

    private final String description;

    UserLogAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String format(String... replacements) {
        String result = description;
        String[] keys = { "{user_name}", "{item_name}", "{role}", "{field}" };
        for (int i = 0; i < replacements.length && i < keys.length; i++) {
            result = result.replace(keys[i], replacements[i]);
        }
        return result;
    }
}