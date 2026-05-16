package admin.services;

import admin.database.ItemDatabase;
import admin.database.LogsDatabase;
import admin.database.UsersDatabase;
import admin.models.AdminItems;
import admin.models.AdminUsers;
import utils.SessionManager;
import java.util.List;
import enums.*;

public class AdminItemsServices {

    private final ItemDatabase itemDB = new ItemDatabase();
    private final LogsDatabase logDB  = new LogsDatabase();

    public int getTotalItems() {
        return itemDB.countActiveItems();
    }

    public Object[][] getActiveItemsForTable() {
        return buildTableData(itemDB.getAllItems());
    }

    public Object[][] getArchivedItemsForTable() {
        return buildTableData(itemDB.getArchivedItems());
    }

    public Object[][] searchActiveItems(String filter, String keyword) {
        return buildTableData(itemDB.searchItems(filter, keyword));
    }

    public Object[][] searchArchivedItems(String filter, String keyword) {
        return buildTableData(itemDB.searchArchivedItems(filter, keyword));
    }

    public AdminItems getItemById(int itemId) {
        return itemDB.getItemById(itemId);
    }

    public boolean addItem(String name, String category, String condition,
            int quantity, int price, String status, String action,
            String description, String imagePath) {

        int adminId = SessionManager.get().getCurrentUserId();
        if (adminId <= 0) adminId = 1;

        AdminUsers admin = new UsersDatabase().getUserById(adminId);
        String firstName = admin != null ? admin.getFirstName() : "";
        String lastName  = admin != null ? admin.getLastName()  : "";

        int newItemId = itemDB.insertItem(
            adminId, firstName, lastName,
            name, category, condition,
            quantity, price, status, action,
            description, imagePath);

        if (newItemId > 0) {
            logDB.insertLog(ItemLogAction.ITEM_CREATE, adminId, newItemId, "\"" + name + "\"");
            return true;
        }
        return false;
    }

    public boolean updateItem(AdminItems item) {
        boolean success = itemDB.updateItem(item);
        if (success)
            logDB.insertLog(ItemLogAction.ITEM_UPDATE, item.getOwnerId(), item.getItemId(),
                    "\"" + item.getItemName() + "\"");
        return success;
    }

    public boolean archiveItem(int itemId) {
        AdminItems item = itemDB.getItemById(itemId);
        if (item == null) {
            System.out.println("archiveItem() failed: item " + itemId + " not found.");
            return false;
        }

        String itemName = item.getItemName();
        int    ownerId  = item.getOwnerId();
        boolean success = itemDB.archiveItem(itemId);
        if (success)
            logDB.insertLog(ItemLogAction.ITEM_ARCHIVE, ownerId, 0, "\"" + itemName + "\"");
        return success;
    }

    public boolean unarchiveItem(int itemId) {
        AdminItems item = itemDB.getItemById(itemId);
        if (item == null) {
            System.out.println("unarchiveItem() failed: item " + itemId + " not found in archive.");
            return false;
        }
        String itemName = item.getItemName();
        int    ownerId  = item.getOwnerId();
        boolean success = itemDB.unarchiveItem(itemId);
        if (success)
            logDB.insertLog(ItemLogAction.ITEM_RESTORE, ownerId, itemId, "\"" + itemName + "\"");
        return success;
    }

    public boolean permanentDeleteItem(int itemId) {
        boolean success = itemDB.permanentDeleteItem(itemId);
        if (success)
            logDB.insertLog(ItemLogAction.ITEM_DELETE, 0, 0, "ID " + itemId);
        return success;
    }

    private Object[][] buildTableData(List<AdminItems> items) {
        Object[][] data = new Object[items.size()][7];
        for (int i = 0; i < items.size(); i++) {
            AdminItems item = items.get(i);
            data[i][0] = item.getItemId();
            data[i][1] = item.getItemName();
            data[i][2] = item.getItemCondition();
            data[i][3] = item.getCategory();
            data[i][4] = item.getItemQuantity();
            data[i][5] = item.getPrice();
            data[i][6] = item.getAvailabilityStatus();
        }
        return data;
    }
}