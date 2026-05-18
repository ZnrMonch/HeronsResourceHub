package enums;


public enum ReputationLogAction {

    REPUTATION_EARNED("Earned",    "Karma points earned"),
    REPUTATION_DEDUCTED("Deducted","Karma points deducted"),
    @Deprecated
    REPUTATION_UPDATE("Earned", "Karma points updated");

    private final String dbValue;
    private final String description;

    ReputationLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

   
    public String getDbValue() { return dbValue; }

    public String getDescription() { return description; }
}