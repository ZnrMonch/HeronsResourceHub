package enums;

public enum ReputationLogAction {
    REPUTATION_INCREASE("User reputation increased."),
    REPUTATION_DECREASE("User reputation decreased."),
    REPUTATION_UPDATE("User reputation updated.");

    private final String description;

    ReputationLogAction(String description) {
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