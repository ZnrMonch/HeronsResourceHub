package enums;

/**
 * Actions recorded in the reputation_log table.
 * DB column type: enum('Earned','Deducted')
 */
public enum ReputationLogAction {

    REPUTATION_EARNED("Earned",    "Karma points earned"),
    REPUTATION_DEDUCTED("Deducted","Karma points deducted"),

    // Legacy aliases — kept so existing call sites compile without changes
    /** @deprecated use REPUTATION_EARNED */
    @Deprecated
    REPUTATION_UPDATE("Earned", "Karma points updated");

    private final String dbValue;
    private final String description;

    ReputationLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

    /** The exact value stored in the DB enum column. */
    public String getDbValue() { return dbValue; }

    /** Human-readable log description. */
    public String getDescription() { return description; }
}