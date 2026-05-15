package admin.services;

import admin.database.ItemDatabase;
import admin.database.LogsDatabase;
import admin.models.AdminItems;
import java.util.List;

public class AdminItemsServices {

    private ItemDatabase itemDB = new ItemDatabase();
    private LogsDatabase logDB  = new LogsDatabase();

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

    public boolean updateItem(AdminItems item) {
        boolean success = itemDB.updateItem(item);
        if (success) {
            logDB.insertLog("ITEM", item.getOwnerId(), 0,
                "Item \"" + item.getItemName() + "\" (ID " + item.getItemId() + ") was updated by admin.");
        }
        return success;
    }

    
    public boolean archiveItem(int itemId) {
        AdminItems item = itemDB.getItemById(itemId);
        if (item == null) {
            System.out.println("archiveItem() failed: item " + itemId + " not found.");
            return false;
        }

        boolean success = itemDB.archiveItem(itemId);

        if (success) {
            logDB.insertLog("ITEM", item.getOwnerId(), 0,
                "Item \"" + item.getItemName() + "\" (ID " + itemId + ") was archived by admin.");
        }

        return success;
    }

    
    public boolean unarchiveItem(int itemId) {
        AdminItems item = itemDB.getItemById(itemId);
        if (item == null) {
            System.out.println("unarchiveItem() failed: item " + itemId + " not found.");
            return false;
        }

        boolean success = itemDB.unarchiveItem(itemId);

        if (success) {
            logDB.insertLog("ITEM", item.getOwnerId(), 0,
                "Item \"" + item.getItemName() + "\" (ID " + itemId + ") was restored from archive by admin.");
        }

        return success;
    }

  
    public boolean permanentDeleteItem(int itemId) {
        AdminItems item = itemDB.getItemById(itemId);
        String itemLabel = (item != null)
            ? "\"" + item.getItemName() + "\" (ID " + itemId + ")"
            : "ID " + itemId;

        boolean success = itemDB.permanentDeleteItem(itemId);

        if (success) {
            int ownerId = (item != null) ? item.getOwnerId() : 0;
            logDB.insertLog("ITEM", ownerId, 0,
                "Item " + itemLabel + " was permanently deleted from archive by admin.");
        }

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