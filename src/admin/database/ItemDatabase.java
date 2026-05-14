package admin.database;

import admin.models.AdminItem;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This class is the ONLY place that talks to the items table.

public class ItemDatabase {


    // Returns all active (non-archived) items
    public List<AdminItem> getAllItems() {
        List<AdminItem> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE archived = 0";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("getAllItems() failed: " + e.getMessage());
        }

        return list;
    }

    // Returns all archived items
    public List<AdminItem> getArchivedItems() {
        List<AdminItem> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE archived = 1";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("getArchivedItems() failed: " + e.getMessage());
        }

        return list;
    }

    // Returns one item by its ID
    public AdminItem getItemById(int itemId) {
        String sql = "SELECT * FROM items WHERE item_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.out.println("getItemById() failed: " + e.getMessage());
        }

        return null;
    }

   

    // Soft-deletes an item (sets archived = 1)
    public boolean archiveItem(int itemId) {
        String sql = "UPDATE items SET archived = 1 WHERE item_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("archiveItem() failed: " + e.getMessage());
        }

        return false;
    }

    // Restores an archived item
    public boolean unarchiveItem(int itemId) {
        String sql = "UPDATE items SET archived = 0 WHERE item_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("unarchiveItem() failed: " + e.getMessage());
        }

        return false;
    }

    // Updates the availability status of an item (Available / Unavailable)
    public boolean updateAvailability(int itemId, String status) {
        String sql = "UPDATE items SET availability_status = ? WHERE item_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, status);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("updateAvailability() failed: " + e.getMessage());
        }

        return false;
    }

   
    private AdminItem mapRow(ResultSet rs) throws SQLException {
    	AdminItem item = new AdminItem();
        item.setItemId(rs.getInt("item_id"));
        item.setOwnerId(rs.getInt("owner_id"));
        item.setItemName(rs.getString("item_name"));
        item.setItemQuantity(rs.getInt("item_quantity"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setItemCondition(rs.getString("item_condition"));
        item.setPrice(rs.getInt("price"));
        item.setAvailabilityStatus(rs.getString("availability_status"));
        item.setAction(rs.getString("action"));
        item.setArchived(rs.getBoolean("archived"));
        return item;
    }
}