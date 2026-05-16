package enums;

public enum ItemLogAction {
    ITEM_CREATE("New item created."),
    ITEM_UPDATE("Item details updated."),
    ITEM_DELETE("Item deleted."),
    ITEM_ARCHIVE("Item archived by admin."),
    ITEM_UNARCHIVE("Item unarchived by admin."),
    ITEM_RESTORE("Item restored from archive."),
    ITEM_LIST("Item listed by user."),
    ITEM_UNLIST("Item removed from listing."),
    ITEM_APPROVE("Item approved by admin."),
    ITEM_REJECT("Item rejected by admin."),
    ITEM_UPLOAD_IMAGE("Item image uploaded."),
    ITEM_DELETE_IMAGE("Item image deleted."),
    ITEM_MARK_AVAILABLE("Item marked as available."),
    ITEM_MARK_UNAVAILABLE("Item marked as unavailable.");

    private final String description;

    ItemLogAction(String description) {
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