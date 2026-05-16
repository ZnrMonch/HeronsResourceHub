package admin.database;

import admin.models.AdminItems;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDatabase extends BaseDatabase {

    public int countActiveItems() {
        String sql = "SELECT COUNT(*) FROM items";
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
        String sql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                   + "item_name, item_quantity, description, items_image, "
                   + "`condition`, category, price, availability_status, action FROM items";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.out.println("getAllItems() failed: " + e.getMessage());
        }
        return list;
    }

    public List<AdminItems> getArchivedItems() {
        List<AdminItems> list = new ArrayList<>();
        String sql = "SELECT ia.item_id, ia.owner_id, ia.initiator_firstname, ia.initiator_lastname, "
                   + "ia.item_name, ia.item_quantity, ia.description, ia.items_image, "
                   + "ia.`condition`, ia.category, ia.price, ia.availability_status "
                   + "FROM items_archive ia "
                   + "INNER JOIN ("
                   + "  SELECT item_id, MAX(item_archive_id) AS max_aid FROM items_archive GROUP BY item_id"
                   + ") latest ON ia.item_archive_id = latest.max_aid "
                   + "ORDER BY ia.item_id ASC";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRowArchived(rs));
        } catch (SQLException e) {
            System.out.println("getArchivedItems() failed: " + e.getMessage());
        }
        return list;
    }

    public List<AdminItems> searchItems(String filter, String keyword) {
        List<AdminItems> list = new ArrayList<>();
        String column;
        switch (filter) {
            case "ID":        column = "item_id";             break;
            case "Item Name": column = "item_name";           break;
            case "Condition": column = "`condition`";         break;
            case "Category":  column = "category";            break;
            case "Stock":     column = "item_quantity";       break;
            case "Price":     column = "price";               break;
            case "Status":    column = "availability_status"; break;
            default:          column = "item_name";           break;
        }
        String sql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                   + "item_name, item_quantity, description, items_image, "
                   + "`condition`, category, price, availability_status, action FROM items "
                   + "WHERE " + column + " LIKE ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.out.println("searchItems() failed: " + e.getMessage());
        }
        return list;
    }

    public List<AdminItems> searchArchivedItems(String filter, String keyword) {
        List<AdminItems> list = new ArrayList<>();
        String column;
        switch (filter) {
            case "ID":        column = "item_id";       break;
            case "Item Name": column = "item_name";     break;
            case "Condition": column = "`condition`";   break;
            case "Category":  column = "category";      break;
            default:          column = "item_name";     break;
        }
        String sql = "SELECT ia.item_id, ia.owner_id, ia.initiator_firstname, ia.initiator_lastname, "
                   + "ia.item_name, ia.item_quantity, ia.description, ia.items_image, "
                   + "ia.`condition`, ia.category, ia.price, ia.availability_status "
                   + "FROM items_archive ia "
                   + "INNER JOIN ("
                   + "  SELECT item_id, MAX(item_archive_id) AS max_aid FROM items_archive GROUP BY item_id"
                   + ") latest ON ia.item_archive_id = latest.max_aid "
                   + "WHERE ia." + column + " LIKE ? "
                   + "ORDER BY ia.item_id ASC";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRowArchived(rs));
        } catch (SQLException e) {
            System.out.println("searchArchivedItems() failed: " + e.getMessage());
        }
        return list;
    }

    public AdminItems getItemById(int itemId) {
        String sql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                   + "item_name, item_quantity, description, items_image, "
                   + "`condition`, category, price, availability_status, action FROM items "
                   + "WHERE item_id = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, false);
        } catch (SQLException e) {
            System.out.println("getItemById() active lookup failed: " + e.getMessage());
        }

        String archiveSql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                          + "item_name, item_quantity, description, items_image, "
                          + "`condition`, category, price, availability_status "
                          + "FROM items_archive WHERE item_id = ? "
                          + "ORDER BY item_archive_id DESC LIMIT 1";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(archiveSql)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRowArchived(rs);
        } catch (SQLException e) {
            System.out.println("getItemById() archive lookup failed: " + e.getMessage());
        }
        return null;
    }


    public int insertItem(int ownerId, String initiatorFirstName, String initiatorLastName,
            String name, String category, String condition,
            int quantity, int price, String status, String action,
            String description, String imagePath) {
        String sql =
            "INSERT INTO items "
            + "(owner_id, initiator_firstname, initiator_lastname, item_name, category, `condition`, "
            + " item_quantity, price, availability_status, description, "
            + " items_image, date_listed, action) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1,     ownerId);
            stmt.setString(2,  initiatorFirstName);
            stmt.setString(3,  initiatorLastName);
            stmt.setString(4,  name);
            stmt.setString(5,  category);
            stmt.setString(6,  condition);
            stmt.setInt(7,     quantity);
            stmt.setInt(8,     price);
            stmt.setString(9,  status);
            stmt.setString(10, description);
            stmt.setString(11, imagePath);
            stmt.setString(12, action);
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

    public boolean archiveItem(int itemId) {
        // Step 1 — reputation_log rows tied to this item's transactions
        String deleteRepLog =
            "DELETE FROM reputation_log WHERE transaction_id IN "
            + "(SELECT transaction_id FROM transaction_log WHERE item_id = ?)";

        // Step 2 — transaction_log rows for this item
        String deleteTransLog = "DELETE FROM transaction_log WHERE item_id = ?";

        // Step 3 — items_log rows for this item
        String deleteItemsLog = "DELETE FROM items_log WHERE item_id = ?";

        // Step 4a — copy item to archive
        String archiveItem =
            "INSERT IGNORE INTO items_archive "
            + "(item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + " description, items_image, category, `condition`, price, availability_status, "
            + " maximum_borrow_days, desired_item, pickup_area, pickup_time, pickup_days, action) "
            + "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + "       description, items_image, category, `condition`, price, availability_status, "
            + "       maximum_borrow_days, desired_item, pickup_area, pickup_time, pickup_days, action "
            + "FROM items WHERE item_id = ?";

        // Step 4b — delete item row
        String deleteItem = "DELETE FROM items WHERE item_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                exec(conn, deleteRepLog,   itemId); // 1
                exec(conn, deleteTransLog, itemId); // 2
                exec(conn, deleteItemsLog, itemId); // 3

                try (PreparedStatement ps = conn.prepareStatement(archiveItem)) {
                    ps.setInt(1, itemId);
                    int rows = ps.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("Item " + itemId + " not found in items table.");
                }                                   // 4a

                exec(conn, deleteItem, itemId);     // 4b

                conn.commit();
                System.out.println("archiveItem() success: item " + itemId + " archived.");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("archiveItem() rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("archiveItem() connection failed: " + e.getMessage());
        }
        return false;
    }

    public boolean unarchiveItem(int itemId) {
        String insert =
            "INSERT INTO items "
            + "(item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + " description, items_image, category, `condition`, price, availability_status, "
            + " maximum_borrow_days, desired_item, pickup_area, pickup_time, pickup_days, action) "
            + "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + "       description, items_image, category, `condition`, price, availability_status, "
            + "       maximum_borrow_days, desired_item, pickup_area, pickup_time, pickup_days, action "
            + "FROM items_archive WHERE item_id = ? "
            + "ORDER BY item_archive_id DESC LIMIT 1";
        String delete = "DELETE FROM items_archive WHERE item_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setInt(1, itemId);
                    int rows = ins.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("Item " + itemId + " not found in items_archive.");
                }
                exec(conn, delete, itemId);
                conn.commit();
                System.out.println("unarchiveItem() success: item " + itemId + " restored.");
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("unarchiveItem() rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("unarchiveItem() connection failed: " + e.getMessage());
        }
        return false;
    }

    public boolean permanentDeleteItem(int itemId) {
        String sql = "DELETE FROM items_archive WHERE item_id = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("permanentDeleteItem() success: item " + itemId + " permanently deleted.");
                return true;
            } else {
                System.out.println("permanentDeleteItem() failed: item " + itemId + " not found in archive.");
            }
        } catch (SQLException e) {
            System.out.println("permanentDeleteItem() failed: " + e.getMessage());
        }
        return false;
    }

    private AdminItems mapRow(ResultSet rs, boolean isArchived) throws SQLException {
        AdminItems item = new AdminItems();
        item.setItemId(rs.getInt("item_id"));
        item.setOwnerId(rs.getInt("owner_id"));
        item.setItemName(rs.getString("item_name"));
        item.setItemQuantity(rs.getInt("item_quantity"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setItemCondition(rs.getString("condition"));
        item.setPrice(rs.getInt("price"));
        item.setAvailabilityStatus(rs.getString("availability_status"));
        item.setAction(rs.getString("action"));
        item.setArchived(isArchived);
        return item;
    }
  
    private AdminItems mapRowArchived(ResultSet rs) throws SQLException {
        AdminItems item = new AdminItems();
        item.setItemId(rs.getInt("item_id"));
        item.setOwnerId(rs.getInt("owner_id"));
        item.setItemName(rs.getString("item_name"));
        item.setItemQuantity(rs.getInt("item_quantity"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setItemCondition(rs.getString("condition"));
        item.setPrice(rs.getInt("price"));
        item.setAvailabilityStatus(rs.getString("availability_status"));
        item.setAction("—");
        item.setArchived(true);
        return item;
    }
}