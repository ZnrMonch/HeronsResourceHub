package admin;

import database.DatabaseManager;
import database.UMak;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

	private Connection getConnection() {
		return DatabaseManager.getConnection();
	}

	public int getTotalUsers() {
		return countRows("SELECT COUNT(*) FROM users");
	}

	public int getTotalItems() {
		return countRows("SELECT COUNT(*) FROM items");
	}

	public int getTotalTransactions() {
		return countRows("SELECT COUNT(*) FROM transaction_log");
	}
	

	public int getTotalLogs() {
		int userLogs = countRows("SELECT COUNT(*) FROM users_log");
		int itemLogs = countRows("SELECT COUNT(*) FROM items_log");
		int reputation = countRows("SELECT COUNT(*) FROM reputation_log");
		return userLogs + itemLogs + reputation;
	}

	// Get and searches active users with filters
	public List<Object[]> searchUsers(String search, String filter, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT user_id, student_id, first_name, last_name, college, year_level, karma_score "
				+ "FROM users WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			String column = getUserColumn(filter, false);
			sql += " AND " + column + " LIKE ?";
		}

		
		sql += " LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("user_id"), rs.getString("student_id"), rs.getString("first_name"),
						rs.getString("last_name"), rs.getString("college"), rs.getString("year_level"),
						rs.getInt("karma_score") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchUsers error: " + e.getMessage());
		}

		return results;
	}

	
	// Get and searches archived users with filters
	public List<Object[]> searchArchivedUsers(String search, String filter, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT user_archive_id, student_id, first_name, last_name, college, year_level, karma_score "
				+ "FROM users_archive WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			String column = getUserColumn(filter, true);
			sql += " AND " + column + " LIKE ?";
		}

		sql += " LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("user_archive_id"), rs.getString("student_id"), rs.getString("first_name"),
						rs.getString("last_name"), rs.getString("college"), rs.getString("year_level"),
						rs.getInt("karma_score") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchArchivedUsers error: " + e.getMessage());
		}

		return results;
	}

	// Get and searches active items with filters
	public List<Object[]> searchItems(String search, String filter, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT item_id, item_name, category, item_quantity, price, availability_status "
				+ "FROM items WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			String column = getItemColumn(filter);
			sql += " AND " + column + " LIKE ?";
		}

		sql += " LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("item_id"), rs.getString("item_name"), rs.getString("category"),
						rs.getInt("item_quantity"), rs.getInt("price"), rs.getString("availability_status") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchItems error: " + e.getMessage());
		}

		return results;
	}

	// Get and searches archived items
	public List<Object[]> searchArchivedItems(String search, String filter, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT item_archive_id, item_name, category, item_quantity, price, availability_status "
				+ "FROM items_archive WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			String column = getItemColumn(filter);
			sql += " AND " + column + " LIKE ?";
		}

		sql += " LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("item_archive_id"), rs.getString("item_name"), rs.getString("category"),
						rs.getInt("item_quantity"), rs.getInt("price"), rs.getString("availability_status") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchArchivedItems error: " + e.getMessage());
		}

		return results;
	}

	// Get and searches logs with filters
	public List<Object[]> searchLogs(String logType, String search, int page, int size) {
		if (logType.equals("USER"))
			return searchUserLogs(search, page, size);
		if (logType.equals("ITEM"))
			return searchItemLogs(search, page, size);
		if (logType.equals("TRANSACTION"))
			return searchTransactionLogs(search, page, size);
		if (logType.equals("REPUTATION"))
			return searchReputationLogs(search, page, size);

		return searchUserLogs(search, page, size);
	}

	// Get and searches user logs with filters
	private List<Object[]> searchUserLogs(String search, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT log_id, action, reason, timestamp, user_id " + "FROM users_log WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			sql += " AND (action LIKE ? OR reason LIKE ?)";
		}

		sql += " ORDER BY timestamp ASC LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String description = rs.getString("action") + " - " + rs.getString("reason");
				Object[] row = { rs.getInt("log_id"), "USER", description, rs.getString("timestamp"),
						rs.getInt("user_id") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchUserLogs error: " + e.getMessage());
		}

		return results;
	}

	// Get and searches item logs with filters
	private List<Object[]> searchItemLogs(String search, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT log_id, action, reason, timestamp, user_id " + "FROM items_log WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			sql += " AND (action LIKE ? OR reason LIKE ?)";
		}

		sql += " ORDER BY timestamp ASC LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String description = rs.getString("action") + " - " + rs.getString("reason");
				Object[] row = { rs.getInt("log_id"), "ITEM", description, rs.getString("timestamp"),
						rs.getInt("user_id") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchItemLogs error: " + e.getMessage());
		}

		return results;
	}

	// Get and searches transaction logs with filters
	private List<Object[]> searchTransactionLogs(String search, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT transaction_id, action, transaction_message, transaction_note, user_id "
				+ "FROM transaction_log WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			sql += " AND (action LIKE ? OR transaction_message LIKE ?)";
		}

		sql += " LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
				stmt.setString(i, "%" + search.trim() + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("transaction_id"), "TRANSACTION",
						rs.getString("action") + " - " + rs.getString("transaction_message"), null,
						rs.getInt("user_id") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchTransactionLogs error: " + e.getMessage());
		}

		return results;
	}
	
	// Get and searches reputation logs with filters
	private List<Object[]> searchReputationLogs(String search, int page, int size) {
		List<Object[]> results = new ArrayList<>();

		String sql = "SELECT reputation_id, user_id, action, reason, timestamp, " + "points_earned, points_deducted "
				+ "FROM reputation_log WHERE 1=1";

		if (search != null && !search.trim().isEmpty()) {
			sql += " AND (action LIKE ? OR reason LIKE ?)";
		}

		sql += " ORDER BY timestamp ASC LIMIT ? OFFSET ?";

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int i = 1;

			if (search != null && !search.trim().isEmpty()) {
				stmt.setString(i, "%" + search + "%");
				i++;
				stmt.setString(i, "%" + search + "%");
				i++;
			}

			stmt.setInt(i, size);
			i++;
			stmt.setInt(i, page * size);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String action = rs.getString("action");
				String reason = rs.getString("reason");

				String earned = rs.getString("points_earned");
				String deducted = rs.getString("points_deducted");

				String points;
				if (action.equals("Earned")) {
					points = "+" + earned + " pts";
				} else {
					points = "-" + deducted + " pts";
				}

				Object[] row = { rs.getInt("reputation_id"), "REPUTATION", points + " | " + action + " - " + reason,
						rs.getString("timestamp"), rs.getInt("user_id") };
				results.add(row);
			}

		} catch (SQLException e) {
			System.err.println("searchReputationLogs error: " + e.getMessage());
		}

		return results;
	}

	// Moves user from archive to active
	public boolean retrieveUser(int userArchiveId) {

		String copySQL = "INSERT INTO users " + "(user_id, system_role, student_id, umak_email_address, password, college, "
				+ "year_level, course_program, first_name, last_name, karma_score, profile_image) "
				+ "SELECT system_role, student_id, umak_email_address, password, college, "
				+ "year_level, course_program, first_name, last_name, karma_score, profile_image "
				+ "FROM users_archive WHERE user_archive_id = ?";

		String deleteSQL = "DELETE FROM users_archive WHERE user_archive_id = ?";

		return moveRecord(copySQL, deleteSQL, userArchiveId);
	}

	// Moves item from archive to active
	public boolean retrieveItem(int itemArchiveId) {
		String copySQL = "INSERT INTO items "
				+ "(owner_id, item_name, item_quantity, description, category, item_condition, "
				+ "price, availability_status, maximum_borrow_days, desired_item, "
				+ "pickup_area, pickup_time, action) "
				+ "SELECT owner_id, item_name, item_quantity, description, category, item_condition, "
				+ "price, availability_status, maximum_borrow_days, desired_item, "
				+ "pickup_area, pickup_time, 'Sharing' " + "FROM items_archive WHERE item_archive_id = ?";

		String deleteSQL = "DELETE FROM items_archive WHERE item_archive_id = ?";

		return moveRecord(copySQL, deleteSQL, itemArchiveId);
	}

	// Deletes user from archive permanently
	public boolean deleteUser(int userArchiveId) {
		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM users_archive WHERE user_archive_id = ?");
			stmt.setInt(1, userArchiveId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("deleteUser error: " + e.getMessage());
			return false;
		}
	}

	// Deletes item from archive permanently
	public boolean deleteItem(int itemArchiveId) {
		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM items_archive WHERE item_archive_id = ?");
			stmt.setInt(1, itemArchiveId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("deleteItem error: " + e.getMessage());
			return false;
		}
	}

	// Updates user details
	public boolean updateUser(int userId, String firstName, String lastName, String college) {
		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("UPDATE users SET first_name = ?, last_name = ?, college = ? WHERE user_id = ?");
			stmt.setString(1, firstName);
			stmt.setString(2, lastName);
			stmt.setString(3, college);
			stmt.setInt(4, userId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("updateUser error: " + e.getMessage());
			return false;
		}
	}

	// Updates item details
	public boolean updateItem(int itemId, String itemName, String category, int quantity, int price) {
		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"UPDATE items SET item_name = ?, category = ?, item_quantity = ?, price = ? WHERE item_id = ?");
			stmt.setString(1, itemName);
			stmt.setString(2, category);
			stmt.setInt(3, quantity);
			stmt.setInt(4, price);
			stmt.setInt(5, itemId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("updateItem error: " + e.getMessage());
			return false;
		}
	}

	// Helper method to count rows based on a SQL query
	private int countRows(String sql) {
		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
		}
		return 0;
	}

	// Helper method to move records between tables (used for retrieving from archive)
	private boolean moveRecord(String copySQL, String deleteSQL, int id) {
		Connection conn = getConnection();
		try {

			conn.setAutoCommit(false);

			PreparedStatement copyStmt = conn.prepareStatement(copySQL);
			copyStmt.setInt(1, id);
			copyStmt.executeUpdate();

			PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL);
			deleteStmt.setInt(1, id);
			deleteStmt.executeUpdate();

			conn.commit();
			return true;

		} catch (SQLException e) {
			System.err.println("moveRecord error: " + e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException ignored) {
			}
			return false;

		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException ignored) {
			}
		}
	}

	// Helper method to map user filter to database column
	private String getUserColumn(String filter, boolean isArchive) {
		if (filter.equals("ID"))
			return isArchive ? "user_archive_id" : "user_id";
		if (filter.equals("Student ID"))
			return "student_id";
		if (filter.equals("First Name"))
			return "first_name";
		if (filter.equals("Last Name"))
			return "last_name";
		if (filter.equals("College"))
			return "college";
		if (filter.equals("Year"))
			return "year_level";
		if (filter.equals("Karma Points"))
			return "karma_score";
		return "first_name";
	}

	// Helper method to map item filter to database column
	private String getItemColumn(String filter) {
		if (filter.equals("ID"))
			return "item_id";
		if (filter.equals("Item Name"))
			return "item_name";
		if (filter.equals("Category"))
			return "category";
		if (filter.equals("Stock"))
			return "item_quantity";
		if (filter.equals("Price"))
			return "price";
		if (filter.equals("Status"))
			return "availability_status";
		return "item_name";
	}

	// Helper method to get list of colleges for dropdowns
	public String[] getColleges() {
		return UMak.COLLEGES_INSTITUTES;
	}
}