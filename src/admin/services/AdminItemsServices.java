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
        return itemDB.updateItem(item);
    }

  
    public boolean archiveItem(int itemId) {
        AdminItems item = itemDB.getItemById(itemId);
        if (item == null) {
            System.out.println("Archive failed: item not found.");
            return false;
        }
        return itemDB.archiveItem(itemId);
    }

    public boolean unarchiveItem(int itemId) {
        return itemDB.unarchiveItem(itemId);
    }

    private Object[][] buildTableData(List<AdminItems> items) {
        Object[][] data = new Object[items.size()][6];
        for (int i = 0; i < items.size(); i++) {
            AdminItems item = items.get(i);
            data[i][0] = item.getItemId();
            data[i][1] = item.getItemName();
            data[i][2] = item.getCategory();
            data[i][3] = item.getItemQuantity();
            data[i][4] = item.getPrice();
            data[i][5] = item.getAvailabilityStatus();
        }
        return data;
    }
}