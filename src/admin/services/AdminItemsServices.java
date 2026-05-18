package admin.services;

import admin.database.ItemDatabase;
import admin.database.LogsDatabase;
import admin.models.AdminItems;
import utils.SessionManager;
import java.util.List;
import enums.*;

//Service layer for item management — delegates to ItemDatabase and logs every mutation
public class AdminItemsServices {

 private final ItemDatabase itemDB = new ItemDatabase();
 private final LogsDatabase logDB  = new LogsDatabase();

 // ── Read ──

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

 // ── Write ──

 // Falls back to adminId=1 when no session exists (e.g. seeding or system operations)
 public boolean addItem(String name, String category, String condition,
         int quantity, int price, String status, String action,
         String description, String imagePath) {

     int adminId = SessionManager.get().getCurrentUserId();
     if (adminId <= 0) adminId = 1;

     int newItemId = itemDB.insertItem(
             adminId, name, category, condition,
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

 // Fetches item first to capture name and ownerId for the log entry
 public boolean archiveItem(int itemId) {
     AdminItems item = itemDB.getItemById(itemId);
     if (item == null) {
         System.out.println("archiveItem() failed: item " + itemId + " not found.");
         return false;
     }
     boolean success = itemDB.archiveItem(itemId);
     if (success)
         logDB.insertLog(ItemLogAction.ITEM_ARCHIVE, item.getOwnerId(), itemId,
                 "\"" + item.getItemName() + "\"");
     return success;
 }

 // Fetches item first to capture name and ownerId for the log entry
 public boolean unarchiveItem(int itemId) {
     AdminItems item = itemDB.getItemById(itemId);
     if (item == null) {
         System.out.println("unarchiveItem() failed: item " + itemId + " not found in archive.");
         return false;
     }
     boolean success = itemDB.unarchiveItem(itemId);
     if (success)
         logDB.insertLog(ItemLogAction.ITEM_RESTORE, item.getOwnerId(), itemId,
                 "\"" + item.getItemName() + "\"");
     return success;
 }

 // userId passed as 0 — item no longer exists after deletion so owner cannot be resolved
 public boolean permanentDeleteItem(int itemId) {
     boolean success = itemDB.permanentDeleteItem(itemId);
     if (success)
         logDB.insertLog(ItemLogAction.ITEM_DELETE, 0, itemId, "ID " + itemId);
     return success;
 }

 // ── Helpers ──

 private Object[][] buildTableData(List<AdminItems> items) {
     Object[][] data = new Object[items.size()][8];
     for (int i = 0; i < items.size(); i++) {
         AdminItems item = items.get(i);
         data[i][0] = Boolean.FALSE;
         data[i][1] = item.getItemId();
         data[i][2] = item.getItemName();
         data[i][3] = item.getItemCondition();
         data[i][4] = item.getCategory();
         data[i][5] = item.getItemQuantity();
         data[i][6] = item.getPrice();
         data[i][7] = item.getAvailabilityStatus();
     }
     return data;
 }
}