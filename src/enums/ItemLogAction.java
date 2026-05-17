package enums;

/**
 * Actions recorded in the items_log table.
 * DB column type: enum('Create','Update','Archive','Retrieve','Delete')
 */
public enum ItemLogAction {

    // Maps to DB value "Create"
    ITEM_CREATE("Create", "Item listed"),
    ITEM_LIST("Create",   "Item listed"),

    // Maps to DB value "Update"
    ITEM_UPDATE("Update",           "Item details updated"),
    ITEM_MARK_AVAILABLE("Update",   "Item marked available"),
    ITEM_MARK_UNAVAILABLE("Update", "Item marked unavailable"),
    ITEM_APPROVE("Update",          "Item approved"),
    ITEM_REJECT("Update",           "Item rejected"),
    ITEM_UPLOAD_IMAGE("Update",     "Item image uploaded"),
    ITEM_DELETE_IMAGE("Update",     "Item image removed"),
    ITEM_UNLIST("Update",           "Item unlisted"),

    // Maps to DB value "Archive"
    ITEM_ARCHIVE("Archive", "Item archived"),

    // Maps to DB value "Retrieve"
    ITEM_UNARCHIVE("Retrieve", "Item restored from archive"),
    ITEM_RESTORE("Retrieve",   "Item restored from archive"),

    // Maps to DB value "Delete"
    ITEM_DELETE("Delete", "Item permanently deleted");

    private final String dbValue;
    private final String description;

    ItemLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

    /** The exact value stored in the DB enum column. */
    public String getDbValue() { return dbValue; }

    /** Human-readable log description. */
    public String getDescription() { return description; }
}