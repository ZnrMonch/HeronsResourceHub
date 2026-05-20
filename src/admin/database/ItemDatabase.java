package admin.database;

import admin.models.AdminItems;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Handles all database operations for the items table
public class ItemDatabase extends BaseDatabase {

 private static final String SELECT_ITEMS_COLS =
     "item_id, owner_id, item_name, item_quantity, description, item_image, "
     + "`condition`, category, price, availability_status, action, "
     + "items_is_archived, items_archived_at";

 // ── Read ──

 // Excludes archived items from the count
 public int countActiveItems() {
     String sql = "SELECT COUNT(*) FROM items WHERE items_is_archived = 0";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         if (rs.next()) return rs.getInt(1);
     } catch (SQLException e) {
         System.out.println("countActiveItems() failed: " + e.getMessage());
     }
     return 0;
 }

 public List<AdminItems> getAllItems() {
     List<AdminItems> list = new ArrayList<>();
     String sql = "SELECT " + SELECT_ITEMS_COLS
                + " FROM items WHERE items_is_archived = 0";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.out.println("getAllItems() failed: " + e.getMessage());
     }
     return list;
 }

 public List<AdminItems> getArchivedItems() {
     List<AdminItems> list = new ArrayList<>();
     String sql = "SELECT " + SELECT_ITEMS_COLS
                + " FROM items WHERE items_is_archived = 1 ORDER BY item_id ASC";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.out.println("getArchivedItems() failed: " + e.getMessage());
     }
     return list;
 }

 public AdminItems getItemById(int itemId) {
     String sql = "SELECT " + SELECT_ITEMS_COLS + " FROM items WHERE item_id = ?";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1, itemId);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) return mapRow(rs);
     } catch (SQLException e) {
         System.out.println("getItemById() failed: " + e.getMessage());
     }
     return null;
 }

 // Searches active or archived items across all columns when filter is "All"
 public List<AdminItems> searchItems(String filter, String keyword) {
     return search(filter, keyword, false);
 }

 public List<AdminItems> searchArchivedItems(String filter, String keyword) {
     return search(filter, keyword, true);
 }

 // ── Write ──

 // Returns the generated item_id, or -1 on failure
 public int insertItem(int ownerId,
         String name, String category, String condition,
         int quantity, int price, String status, String action,
         String description, String imagePath) {
     String sql =
         "INSERT INTO items "
         + "(owner_id, item_name, category, `condition`, "
         + " item_quantity, price, availability_status, description, "
         + " item_image, date_listed, action) "
         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         stmt.setInt(1,     ownerId);
         stmt.setString(2,  name);
         stmt.setString(3,  category);
         stmt.setString(4,  condition);
         stmt.setInt(5,     quantity);
         stmt.setInt(6,     price);
         stmt.setString(7,  status);
         stmt.setString(8,  description);
         stmt.setString(9,  imagePath);
         stmt.setString(10, action);
         int rows = stmt.executeUpdate();
         if (rows > 0) {
             ResultSet keys = stmt.getGeneratedKeys();
             if (keys.next()) return keys.getInt(1);
         }
     } catch (SQLException e) {
         System.out.println("insertItem() failed: " + e.getMessage());
     }
     return -1;
 }

 public boolean updateItem(AdminItems item) {
     String sql = "UPDATE items SET item_name = ?, category = ?, `condition` = ?, "
                + "item_quantity = ?, price = ?, availability_status = ?, action = ? "
                + "WHERE item_id = ?";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setString(1, item.getItemName());
         stmt.setString(2, item.getCategory());
         stmt.setString(3, item.getItemCondition());
         stmt.setInt(4,    item.getItemQuantity());
         stmt.setInt(5,    item.getPrice());
         stmt.setString(6, item.getAvailabilityStatus());
         stmt.setString(7, item.getAction());
         stmt.setInt(8,    item.getItemId());
         return stmt.executeUpdate() > 0;
     } catch (SQLException e) {
         System.out.println("updateItem() failed: " + e.getMessage());
     }
     return false;
 }

 // ── Archive ──

 public boolean archiveItem(int itemId) {
     String sql = "UPDATE items SET items_is_archived = 1, items_archived_at = NOW() WHERE item_id = ?";
     return executeArchiveToggle(itemId, sql, "archiveItem()");
 }

 // Clears items_archived_at so the record looks fully active again
 public boolean unarchiveItem(int itemId) {
     String sql = "UPDATE items SET items_is_archived = 0, items_archived_at = NULL WHERE item_id = ?";
     return executeArchiveToggle(itemId, sql, "unarchiveItem()");
 }

 // Guard: only rows already archived can be permanently deleted
 public boolean permanentDeleteItem(int itemId) {
     String sql = "DELETE FROM items WHERE item_id = ? AND items_is_archived = 1";
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1, itemId);
         int rows = stmt.executeUpdate();
         if (rows > 0) {
             System.out.println("permanentDeleteItem() success: item " + itemId + " permanently deleted.");
             return true;
         } else {
             System.out.println("permanentDeleteItem() failed: item " + itemId + " not found or not archived.");
         }
     } catch (SQLException e) {
         System.out.println("permanentDeleteItem() failed: " + e.getMessage());
     }
     return false;
 }

 // ── Helpers ──

 // Shared search path for both active and archived; avoids duplicating two near-identical methods
 private List<AdminItems> search(String filter, String keyword, boolean archived) {
     List<AdminItems> list = new ArrayList<>();
     int archiveFlag = archived ? 1 : 0;
     String label = archived ? "searchArchivedItems" : "searchItems";

     if ("All".equals(filter)) {
         String sql = "SELECT " + SELECT_ITEMS_COLS
                    + " FROM items WHERE ("
                    + "  CAST(item_id       AS CHAR) LIKE ? OR"
                    + "  item_name                   LIKE ? OR"
                    + "  `condition`                 LIKE ? OR"
                    + "  category                    LIKE ? OR"
                    + "  CAST(item_quantity AS CHAR) LIKE ? OR"
                    + "  CAST(price         AS CHAR) LIKE ? OR"
                    + "  availability_status         LIKE ?"
                    + ") AND items_is_archived = " + archiveFlag;
         try (Connection conn = getConn();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             String like = "%" + keyword + "%";
             for (int i = 1; i <= 7; i++) stmt.setString(i, like);
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) list.add(mapRow(rs));
         } catch (SQLException e) {
             System.out.println(label + "(All) failed: " + e.getMessage());
         }
         return list;
     }

     String column = resolveItemColumn(filter);
     String sql = "SELECT " + SELECT_ITEMS_COLS
                + " FROM items WHERE " + column + " LIKE ? AND items_is_archived = " + archiveFlag;
     try (Connection conn = getConn();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setString(1, "%" + keyword + "%");
         ResultSet rs = stmt.executeQuery();
         while (rs.next()) list.add(mapRow(rs));
     } catch (SQLException e) {
         System.out.println(label + "() failed: " + e.getMessage());
     }
     return list;
 }

 // Wraps archive/unarchive in a transaction so a partial update is never left committed
 private boolean executeArchiveToggle(int itemId, String sql, String label) {
     Connection conn = null;
     try {
         conn = getConn();
         conn.setAutoCommit(false);
         int rowsAffected;
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setInt(1, itemId);
             rowsAffected = ps.executeUpdate();
         }
         conn.commit();
         return rowsAffected >= 1;
     } catch (SQLException e) {
         System.out.println(label + " rolled back: " + e.getMessage());
         if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
         return false;
     } finally {
         if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
     }
 }

 // Defaults unknown filter values to item_name rather than failing silently
 private String resolveItemColumn(String filter) {
     if (filter == null) return "item_name";
     switch (filter) {
         case "ID":        return "item_id";
         case "Item Name": return "item_name";
         case "Condition": return "`condition`";
         case "Category":  return "category";
         case "Stock":     return "item_quantity";
         case "Price":     return "price";
         case "Status":    return "availability_status";
         default:          return "item_name";
     }
 }

 private AdminItems mapRow(ResultSet rs) throws SQLException {
     AdminItems item = new AdminItems();
     item.setItemId(rs.getInt("item_id"));
     item.setOwnerId(rs.getInt("owner_id"));
     item.setItemName(rs.getString("item_name"));
     item.setItemQuantity(rs.getInt("item_quantity"));
     item.setDescription(rs.getString("description"));
     item.setImagePath(rs.getString("item_image"));
     item.setCategory(rs.getString("category"));
     item.setItemCondition(rs.getString("condition"));
     item.setPrice(rs.getInt("price"));
     item.setAvailabilityStatus(rs.getString("availability_status"));
     item.setAction(rs.getString("action"));
     item.setArchived(rs.getInt("items_is_archived") == 1);
     item.setArchivedAt(rs.getTimestamp("items_archived_at"));
     return item;
 }
}