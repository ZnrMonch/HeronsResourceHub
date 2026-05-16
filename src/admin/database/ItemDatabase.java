package admin.database;

import admin.models.AdminItems;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDatabase {

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
        // CHANGED: item_condition -> condition; added initiator_firstname, initiator_lastname, items_image
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
        // CHANGED: item_condition -> condition; added initiator_firstname, initiator_lastname, items_image
        String sql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                + "item_name, item_quantity, description, items_image, "
                + "`condition`, category, price, availability_status FROM items_archive";
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
        // CHANGED: item_condition -> condition; added initiator_firstname, initiator_lastname, items_image
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
            case "ID":        column = "item_id";   break;
            case "Item Name": column = "item_name"; break;
            // CHANGED: "Condition" now maps to "condition" (was "item_condition")
            case "Condition": column = "`condition`"; break;
            case "Category":  column = "category";  break;
            default:          column = "item_name"; break;
        }
        // CHANGED: item_condition -> condition; added initiator_firstname, initiator_lastname, items_image
        String sql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                + "item_name, item_quantity, description, items_image, "
                + "`condition`, category, price, availability_status FROM items_archive "
                + "WHERE " + column + " LIKE ?";
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
        // CHANGED: item_condition -> condition; added initiator_firstname, initiator_lastname, items_image
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

        // CHANGED: item_condition -> condition; added initiator_firstname, initiator_lastname, items_image
        String archiveSql = "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, "
                + "item_name, item_quantity, description, items_image, "
                + "`condition`, category, price, availability_status FROM items_archive "
                + "WHERE item_id = ?";
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

    public boolean updateItem(AdminItems item) {
        // CHANGED: item_condition -> condition
        String sql = "UPDATE items SET item_name = ?, category = ?, `condition` = ?, "
                + "item_quantity = ?, price = ?, availability_status = ? "
                + "WHERE item_id = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getCategory());
            stmt.setString(3, item.getItemCondition());
            stmt.setInt(4, item.getItemQuantity());
            stmt.setInt(5, item.getPrice());
            stmt.setString(6, item.getAvailabilityStatus());
            stmt.setInt(7, item.getItemId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateItem() failed: " + e.getMessage());
        }
        return false;
    }

    public boolean archiveItem(int itemId) {
        String archiveItem =
            "INSERT IGNORE INTO items_archive "
            + "(item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + " description, items_image, category, `condition`, price, availability_status, "
            + " maximum_borrow_days, desired_item, pickup_area, pickup_time, pickup_days, action) "
            + "SELECT item_id, owner_id, initiator_firstname, initiator_lastname, item_name, item_quantity, "
            + "       description, items_image, category, `condition`, price, availability_status, "
            + "       maximum_borrow_days, desired_item, pickup_area, pickup_time, pickup_days, action "
            + "FROM items WHERE item_id = ?";

        String deleteItem =
            "DELETE FROM items WHERE item_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(archiveItem)) {
                    ps.setInt(1, itemId);
                    int rows = ps.executeUpdate();
                    if (rows == 0)
                        throw new SQLException("Item " + itemId + " not found in items table.");
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteItem)) {
                    ps.setInt(1, itemId);
                    ps.executeUpdate();
                }

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
            try (PreparedStatement ins = conn.prepareStatement(insert);
                 PreparedStatement del = conn.prepareStatement(delete)) {
                ins.setInt(1, itemId);
                int rows = ins.executeUpdate();
                if (rows == 0)
                    throw new SQLException("Item " + itemId + " not found in items_archive.");
                del.setInt(1, itemId);
                del.executeUpdate();
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

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(
            DatabaseManager.getURL(),
            DatabaseManager.getUser(),
            DatabaseManager.getPassword()
        );
    }

    private AdminItems mapRow(ResultSet rs, boolean isArchived) throws SQLException {
        AdminItems item = new AdminItems();
        item.setItemId(rs.getInt("item_id"));
        item.setOwnerId(rs.getInt("owner_id"));
        item.setItemName(rs.getString("item_name"));
        item.setItemQuantity(rs.getInt("item_quantity"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        // CHANGED: column is now "condition" instead of "item_condition"
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
        // CHANGED: column is now "condition" instead of "item_condition"
        item.setItemCondition(rs.getString("condition"));
        item.setPrice(rs.getInt("price"));
        item.setAvailabilityStatus(rs.getString("availability_status"));
        item.setAction("—");   // items_archive has no action column exposed in admin view
        item.setArchived(true);
        return item;
    }
}