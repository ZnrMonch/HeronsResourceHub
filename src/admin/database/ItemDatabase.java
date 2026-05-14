package admin.database;

import admin.models.AdminItems;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDatabase {

   
    public int countActiveItems() {
        String sql = "SELECT COUNT(*) FROM items";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countActiveItems() failed: " + e.getMessage());
        }
        return 0;
    }

    public List<AdminItems> getAllItems() {
        List<AdminItems> list = new ArrayList<>();
        String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, " +
                     "category, item_condition, price, availability_status, action FROM items";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) list.add(mapRow(rs, false));
        } catch (SQLException e) {
            System.out.println("getAllItems() failed: " + e.getMessage());
        }
        return list;
    }

  

    public List<AdminItems> getArchivedItems() {
        List<AdminItems> list = new ArrayList<>();
        String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, " +
                     "category, item_condition, price, availability_status, action FROM items_archive";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) list.add(mapRow(rs, true));
        } catch (SQLException e) {
            System.out.println("getArchivedItems() failed: " + e.getMessage());
        }
        return list;
    }

   
    // ISN'T WORKING
    public List<AdminItems> searchItems(String filter, String keyword) {
        List<AdminItems> list = new ArrayList<>();

       
        String column;
        switch (filter) {
            case "ID":        column = "item_id";   break;
            case "Item Name": column = "item_name"; break;
            case "Category":  column = "category";  break;
            case "Action":    column = "action";    break;
            default:          column = "item_name"; break;
        }

        String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, " +
                     "category, item_condition, price, availability_status, action FROM items " +
                     "WHERE " + column + " LIKE ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
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
            case "Category":  column = "category";  break;
            case "Action":    column = "action";    break;
            default:          column = "item_name"; break;
        }

        String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, " +
                     "category, item_condition, price, availability_status, action FROM items_archive " +
                     "WHERE " + column + " LIKE ?";

        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, true));
        } catch (SQLException e) {
            System.out.println("searchArchivedItems() failed: " + e.getMessage());
        }
        return list;
    }


    public AdminItems getItemById(int itemId) {
        String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, " +
                     "category, item_condition, price, availability_status, action FROM items " +
                     "WHERE item_id = ?";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs, false);
        } catch (SQLException e) {
            System.out.println("getItemById() failed: " + e.getMessage());
        }
        return null;
    }

  
    public boolean updateItem(AdminItems item) {
        String sql = "UPDATE items SET item_name = ?, category = ?, item_quantity = ?, " +
                     "price = ?, availability_status = ? WHERE item_id = ?";
        try (
            Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getCategory());
            stmt.setInt(3, item.getItemQuantity());
            stmt.setInt(4, item.getPrice());
            stmt.setString(5, item.getAvailabilityStatus());
            stmt.setInt(6, item.getItemId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateItem() failed: " + e.getMessage());
        }
        return false;
    }

    
    public boolean archiveItem(int itemId) {
        String insert = "INSERT INTO items_archive (item_id, owner_id, item_name, item_quantity, " +
                        "description, category, item_condition, price, availability_status, action) " +
                        "SELECT item_id, owner_id, item_name, item_quantity, description, category, " +
                        "item_condition, price, availability_status, action FROM items WHERE item_id = ?";
        String delete = "DELETE FROM items WHERE item_id = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement ins = conn.prepareStatement(insert);
                PreparedStatement del = conn.prepareStatement(delete)
            ) {
                ins.setInt(1, itemId);
                ins.executeUpdate(); // step 1: copy to archive

                del.setInt(1, itemId);
                del.executeUpdate(); // step 2: delete from active

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("archiveItem() failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("archiveItem() connection failed: " + e.getMessage());
        }
        return false;
    }

    public boolean unarchiveItem(int itemId) {
        String insert = "INSERT INTO items (item_id, owner_id, item_name, item_quantity, " +
                        "description, category, item_condition, price, availability_status, action) " +
                        "SELECT item_id, owner_id, item_name, item_quantity, description, category, " +
                        "item_condition, price, availability_status, action FROM items_archive WHERE item_id = ?";
        String delete = "DELETE FROM items_archive WHERE item_id = ?";

        try (Connection conn =  DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement ins = conn.prepareStatement(insert);
                PreparedStatement del = conn.prepareStatement(delete)
            ) {
                ins.setInt(1, itemId);
                ins.executeUpdate();

                del.setInt(1, itemId);
                del.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("unarchiveItem() failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("unarchiveItem() connection failed: " + e.getMessage());
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
        item.setItemCondition(rs.getString("item_condition"));
        item.setPrice(rs.getInt("price"));
        item.setAvailabilityStatus(rs.getString("availability_status"));
        item.setAction(rs.getString("action"));
        item.setArchived(isArchived);
        return item;
    }
}