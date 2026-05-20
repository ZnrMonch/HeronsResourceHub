package enums;


public enum ReputationLogAction {

    REPUTATION_EARNED(   "Earned",   "Karma points earned",   10),
    REPUTATION_DEDUCTED( "Deducted", "Karma points deducted", -10),
    @Deprecated
    REPUTATION_UPDATE(   "Earned",   "Karma points updated",   0);

    private final String dbValue;
    private final String description;
    private final int points;

    ReputationLogAction(String dbValue, String description, int points) {
        this.dbValue     = dbValue;
        this.description = description;
        this.points      = points;
    }

    public String getDbValue()     { return dbValue; }
    public String getDescription() { return description; }
    public int    getPoints()      { return points; }
}