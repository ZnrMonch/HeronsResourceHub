package enums;

public enum ItemLogAction {

    ITEM_CREATE("Create", "Item listed"),
    ITEM_LIST("Create",   "Item listed"),


    ITEM_UPDATE("Update",           "Item details updated"),
    ITEM_MARK_AVAILABLE("Update",   "Item marked available"),
    ITEM_MARK_UNAVAILABLE("Update", "Item marked unavailable"),
    ITEM_APPROVE("Update",          "Item approved"),
    ITEM_REJECT("Update",           "Item rejected"),
    ITEM_UPLOAD_IMAGE("Update",     "Item image uploaded"),
    ITEM_DELETE_IMAGE("Update",     "Item image removed"),
    ITEM_UNLIST("Update",           "Item unlisted"),

  
    ITEM_ARCHIVE("Archive", "Item archived"),
    ITEM_UNARCHIVE("Retrieve", "Item restored from archive"),
    ITEM_RESTORE("Retrieve",   "Item restored from archive"),
    ITEM_DELETE("Delete", "Item permanently deleted");

    private final String dbValue;
    private final String description;

    ItemLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

    public String getDbValue() { return dbValue; }

    public String getDescription() { return description; }
}