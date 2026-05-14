package admin.database;

import admin.models.*;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This class is the ONLY place that runs SQL for the items table.

public class ItemsDatabase {

    // Returns all active (non-archived) items
    public List<AdminItems> getAllItems() {
        List<AdminItems> list = new ArrayList<>();
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
            System.out.println("ItemDatabase.getAllItems() error: " + e.getMessage());
        }

        return list;
    }

    // Returns all archived items
    public List<AdminItems> getArchivedItems() {
        List<AdminItems> list = new ArrayList<>();
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
            System.out.println("ItemDatabase.getArchivedItems() error: " + e.getMessage());
        }

        return list;
    }

    // Returns one item by its ID, or null if not found
    public AdminItems getItemById(int itemId) {
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
            System.out.println("ItemDatabase.getItemById() error: " + e.getMessage());
        }

        return null;
    }

    // Returns total count of active items (used for the status card)
    public int countActiveItems() {
        String sql = "SELECT COUNT(*) FROM items WHERE archived = 0";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("ItemDatabase.countActiveItems() error: " + e.getMessage());
        }

        return 0;
    }

    // Sets archived = 1 for an item
    public boolean archiveItem(int itemId) {
        String sql = "UPDATE items SET archived = 1 WHERE item_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ItemDatabase.archiveItem() error: " + e.getMessage());
        }

        return false;
    }

    // Sets archived = 0 — restores an item
    public boolean unarchiveItem(int itemId) {
        String sql = "UPDATE items SET archived = 0 WHERE item_id = ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASS);
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ItemDatabase.unarchiveItem() error: " + e.getMessage());
        }

        return false;
    }

    // Converts one database row into an Item object
    private AdminItems mapRow(ResultSet rs) throws SQLException {
    	AdminItems item = new Item();
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