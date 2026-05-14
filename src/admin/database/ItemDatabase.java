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
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			System.out.println("countActiveItems() failed: " + e.getMessage());
		}
		return 0;
	}

	public List<AdminItems> getAllItems() {
		List<AdminItems> list = new ArrayList<>();
		String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "item_condition, category, price, availability_status, action FROM items";
		try (Connection conn = getConn();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next())
				list.add(mapRow(rs, false));
		} catch (SQLException e) {
			System.out.println("getAllItems() failed: " + e.getMessage());
		}
		return list;
	}

	public List<AdminItems> getArchivedItems() {
		List<AdminItems> list = new ArrayList<>();
		String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "item_condition, category, price, availability_status FROM items_archive";
		try (Connection conn = getConn();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next())
				list.add(mapRowArchived(rs));
		} catch (SQLException e) {
			System.out.println("getArchivedItems() failed: " + e.getMessage());
		}
		return list;
	}

	public List<AdminItems> searchItems(String filter, String keyword) {
		List<AdminItems> list = new ArrayList<>();

		String column;
		switch (filter) {
		case "ID":
			column = "item_id";
			break;
		case "Item Name":
			column = "item_name";
			break;
		case "Condition":
			column = "item_condition";
			break;
		case "Category":
			column = "category";
			break;
		case "Stock":
			column = "item_quantity";
			break;
		case "Price":
			column = "price";
			break;
		case "Status":
			column = "availability_status";
			break;
		default:
			column = "item_name";
			break;
		}

		String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "item_condition, category, price, availability_status, action FROM items " + "WHERE " + column
				+ " LIKE ?";

		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, "%" + keyword + "%");
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				list.add(mapRow(rs, false));
		} catch (SQLException e) {
			System.out.println("searchItems() failed: " + e.getMessage());
		}
		return list;
	}

	public List<AdminItems> searchArchivedItems(String filter, String keyword) {
		List<AdminItems> list = new ArrayList<>();

		String column;
		switch (filter) {
		case "ID":
			column = "item_id";
			break;
		case "Item Name":
			column = "item_name";
			break;
		case "Condition":
			column = "item_condition";
			break;
		case "Category":
			column = "category";
			break;
		default:
			column = "item_name";
			break;
		}

		String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "item_condition, category, price, availability_status FROM items_archive " + "WHERE " + column
				+ " LIKE ?";

		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, "%" + keyword + "%");
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				list.add(mapRowArchived(rs));
		} catch (SQLException e) {
			System.out.println("searchArchivedItems() failed: " + e.getMessage());
		}
		return list;
	}

	public AdminItems getItemById(int itemId) {
		String sql = "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "item_condition, category, price, availability_status, action FROM items " + "WHERE item_id = ?";
		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, itemId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return mapRow(rs, false);
		} catch (SQLException e) {
			System.out.println("getItemById() active lookup failed: " + e.getMessage());
		}

		String archiveSql = "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "item_condition, category, price, availability_status FROM items_archive " + "WHERE item_id = ?";
		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(archiveSql)) {
			stmt.setInt(1, itemId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return mapRowArchived(rs);
		} catch (SQLException e) {
			System.out.println("getItemById() archive lookup failed: " + e.getMessage());
		}

		return null;
	}

	public boolean updateItem(AdminItems item) {
		String sql = "UPDATE items SET item_name = ?, category = ?, item_condition = ?, "
				+ "item_quantity = ?, price = ?, availability_status = ? " + "WHERE item_id = ?";
		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
		String nullifyTransLog = "UPDATE transaction_log SET item_id = NULL WHERE item_id = ?";
		String nullifyItemsLog = "UPDATE items_log SET item_id = NULL WHERE item_id = ?";

		String insert = "INSERT INTO items_archive " + "(item_id, owner_id, item_name, item_quantity, description, "
				+ " category, item_condition, price, availability_status, "
				+ " maximum_borrow_days, desired_item, pickup_area, pickup_time) "
				+ "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "       category, item_condition, price, availability_status, "
				+ "       maximum_borrow_days, desired_item, pickup_area, pickup_time "
				+ "FROM items WHERE item_id = ?";

		String delete = "DELETE FROM items WHERE item_id = ?";

		try (Connection conn = getConn()) {
			conn.setAutoCommit(false);
			try (PreparedStatement nt = conn.prepareStatement(nullifyTransLog);
					PreparedStatement ni = conn.prepareStatement(nullifyItemsLog);
					PreparedStatement ins = conn.prepareStatement(insert);
					PreparedStatement del = conn.prepareStatement(delete)) {
				nt.setInt(1, itemId);
				nt.executeUpdate();
				ni.setInt(1, itemId);
				ni.executeUpdate();
				ins.setInt(1, itemId);
				ins.executeUpdate();
				del.setInt(1, itemId);
				del.executeUpdate();
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
		String insert = "INSERT INTO items " + "(item_id, owner_id, item_name, item_quantity, description, "
				+ " category, item_condition, price, availability_status, "
				+ " maximum_borrow_days, desired_item, pickup_area, pickup_time, action) "
				+ "SELECT item_id, owner_id, item_name, item_quantity, description, "
				+ "       category, item_condition, price, availability_status, "
				+ "       maximum_borrow_days, desired_item, pickup_area, pickup_time, " + "       'Sharing' "
				+ "FROM items_archive WHERE item_id = ?";
		String delete = "DELETE FROM items_archive WHERE item_id = ?";

		try (Connection conn = getConn()) {
			conn.setAutoCommit(false);
			try (PreparedStatement ins = conn.prepareStatement(insert);
					PreparedStatement del = conn.prepareStatement(delete)) {
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

	private Connection getConn() throws SQLException {
		return DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(),
				DatabaseManager.getPassword());
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

	private AdminItems mapRowArchived(ResultSet rs) throws SQLException {
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
		item.setAction("—");
		item.setArchived(true);
		return item;
	}
}
