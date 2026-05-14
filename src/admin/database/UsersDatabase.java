package admin.database;

import admin.models.AdminUsers;
import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDatabase {

	public int countActiveUsers() {
		if (hasSoftDeleteColumn()) {
			String sql = "SELECT COUNT(*) FROM users WHERE deleted_at IS NULL";
			try (Connection conn = getConn();
					PreparedStatement stmt = conn.prepareStatement(sql);
					ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			} catch (SQLException e) {
				System.out.println("countActiveUsers() soft-delete failed: " + e.getMessage());
			}
		}

		String sql = "SELECT COUNT(*) FROM users";
		try (Connection conn = getConn();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			System.out.println("countActiveUsers() fallback failed: " + e.getMessage());
		}
		return 0;
	}

	public List<AdminUsers> getAllUsers() {
		String sql = hasSoftDeleteColumn()
				? "SELECT user_id, system_role, student_id, first_name, last_name, "
						+ "college, year_level, course_program, karma_score FROM users "
						+ "WHERE deleted_at IS NULL ORDER BY user_id ASC"
				: "SELECT user_id, system_role, student_id, first_name, last_name, "
						+ "college, year_level, course_program, karma_score FROM users " + "ORDER BY user_id ASC";

		List<AdminUsers> list = new ArrayList<>();
		try (Connection conn = getConn();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next())
				list.add(mapRow(rs, false));
		} catch (SQLException e) {
			System.out.println("getAllUsers() failed: " + e.getMessage());
		}
		return list;
	}

	public List<AdminUsers> getArchivedUsers() {
		if (hasSoftDeleteColumn()) {
			List<AdminUsers> list = new ArrayList<>();
			String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
					+ "college, year_level, course_program, karma_score FROM users "
					+ "WHERE deleted_at IS NOT NULL ORDER BY user_id ASC";
			try (Connection conn = getConn();
					PreparedStatement stmt = conn.prepareStatement(sql);
					ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					list.add(mapRow(rs, true));
				if (!list.isEmpty())
					return list;
			} catch (SQLException e) {
				System.out.println("getArchivedUsers() soft-delete query failed: " + e.getMessage());
			}
		}

		if (archiveTableExists()) {
			List<AdminUsers> list = new ArrayList<>();
			String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
					+ "college, year_level, course_program, karma_score FROM users_archive " + "ORDER BY user_id ASC";
			try (Connection conn = getConn();
					PreparedStatement stmt = conn.prepareStatement(sql);
					ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					list.add(mapRow(rs, true));
			} catch (SQLException e) {
				System.out.println("getArchivedUsers() archive table query failed: " + e.getMessage());
			}
			return list;
		}

		return new ArrayList<>();
	}

	public List<AdminUsers> searchUsers(String filter, String keyword) {
		String column = resolveUserColumn(filter);
		String activeCondition = hasSoftDeleteColumn() ? " AND deleted_at IS NULL" : "";

		String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
				+ "college, year_level, course_program, karma_score FROM users " + "WHERE " + column + " LIKE ?"
				+ activeCondition;

		List<AdminUsers> list = new ArrayList<>();
		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, "%" + keyword + "%");
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				list.add(mapRow(rs, false));
		} catch (SQLException e) {
			System.out.println("searchUsers() failed: " + e.getMessage());
		}
		return list;
	}

	public List<AdminUsers> searchArchivedUsers(String filter, String keyword) {
		String column = resolveUserColumn(filter);
		List<AdminUsers> list = new ArrayList<>();

		if (hasSoftDeleteColumn()) {
			String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
					+ "college, year_level, course_program, karma_score FROM users " + "WHERE " + column
					+ " LIKE ? AND deleted_at IS NOT NULL";
			try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, "%" + keyword + "%");
				ResultSet rs = stmt.executeQuery();
				while (rs.next())
					list.add(mapRow(rs, true));
				if (!list.isEmpty())
					return list;
			} catch (SQLException e) {
				System.out.println("searchArchivedUsers() soft-delete failed: " + e.getMessage());
			}
		}

		if (archiveTableExists()) {
			String sql = "SELECT user_id, system_role, student_id, first_name, last_name, "
					+ "college, year_level, course_program, karma_score FROM users_archive " + "WHERE " + column
					+ " LIKE ?";
			try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, "%" + keyword + "%");
				ResultSet rs = stmt.executeQuery();
				while (rs.next())
					list.add(mapRow(rs, true));
			} catch (SQLException e) {
				System.out.println("searchArchivedUsers() archive table failed: " + e.getMessage());
			}
		}

		return list;
	}

	public AdminUsers getUserById(int userId) {
		String baseSql = "SELECT user_id, system_role, student_id, first_name, last_name, "
				+ "college, year_level, course_program, karma_score FROM users " + "WHERE user_id = ?";
		String activeSql = hasSoftDeleteColumn() ? baseSql + " AND deleted_at IS NULL" : baseSql;

		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(activeSql)) {
			stmt.setInt(1, userId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return mapRow(rs, false);
		} catch (SQLException e) {
			System.out.println("getUserById() active lookup failed: " + e.getMessage());
		}

		if (hasSoftDeleteColumn()) {
			String archivedSql = baseSql + " AND deleted_at IS NOT NULL";
			try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(archivedSql)) {
				stmt.setInt(1, userId);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					return mapRow(rs, true);
			} catch (SQLException e) {
				System.out.println("getUserById() soft-delete archived lookup failed: " + e.getMessage());
			}
		}

		if (archiveTableExists()) {
			String archiveSql = "SELECT user_id, system_role, student_id, first_name, last_name, "
					+ "college, year_level, course_program, karma_score FROM users_archive " + "WHERE user_id = ?";
			try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(archiveSql)) {
				stmt.setInt(1, userId);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					return mapRow(rs, true);
			} catch (SQLException e) {
				System.out.println("getUserById() archive table lookup failed: " + e.getMessage());
			}
		}

		return null;
	}

	public boolean updateUser(AdminUsers user) {
		String sql = "UPDATE users SET first_name = ?, last_name = ?, college = ?, "
				+ "year_level = ?, karma_score = ?, system_role = ? WHERE user_id = ?";
		try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.getFirstName());
			stmt.setString(2, user.getLastName());
			stmt.setString(3, user.getCollege());
			stmt.setString(4, user.getYearLevel());
			stmt.setInt(5, user.getKarmaScore());
			stmt.setString(6, user.getSystemRole());
			stmt.setInt(7, user.getUserId());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("updateUser() failed: " + e.getMessage());
		}
		return false;
	}

	public boolean archiveUser(int userId) {

		if (hasSoftDeleteColumn()) {
			String sql = "UPDATE users SET deleted_at = NOW() WHERE user_id = ? AND deleted_at IS NULL";
			try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				int rows = stmt.executeUpdate();
				System.out.println("archiveUser() soft-delete: " + rows + " rows affected for user " + userId);
				if (rows > 0)
					return true;
			} catch (SQLException e) {
				System.err.println("archiveUser() soft-delete failed: " + e.getMessage());
			}
		}

		if (archiveTableExists()) {

			String nullifyTransLog = "UPDATE transaction_log SET item_id = NULL "
					+ "WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?)";

			String nullifyItemsLog = "UPDATE items_log SET item_id = NULL "
					+ "WHERE item_id IN (SELECT item_id FROM items WHERE owner_id = ?)";

			String insert = "INSERT INTO users_archive "
					+ "(user_id, system_role, student_id, umak_email_address, password, "
					+ " college, year_level, course_program, first_name, last_name, "
					+ " karma_score, profile_image, bio, contact, home_address, gcash_num, bank_number) "
					+ "SELECT user_id, system_role, student_id, umak_email_address, password, "
					+ "       college, year_level, course_program, first_name, last_name, "
					+ "       karma_score, profile_image, bio, contact_num, home_address, gcash_num, bank_network "
					+ "FROM users WHERE user_id = ?";

			String delete = "DELETE FROM users WHERE user_id = ?";

			try (Connection conn = getConn()) {
				conn.setAutoCommit(false);
				try (PreparedStatement nt = conn.prepareStatement(nullifyTransLog);
						PreparedStatement ni = conn.prepareStatement(nullifyItemsLog);
						PreparedStatement ins = conn.prepareStatement(insert);
						PreparedStatement del = conn.prepareStatement(delete)) {
					nt.setInt(1, userId);
					nt.executeUpdate();
					ni.setInt(1, userId);
					ni.executeUpdate();
					ins.setInt(1, userId);
					ins.executeUpdate();
					del.setInt(1, userId);
					del.executeUpdate();
					conn.commit();
					System.out.println("archiveUser() archive table: moved user " + userId);
					return true;
				} catch (SQLException e) {
					conn.rollback();
					System.err.println("archiveUser() archive table failed: " + e.getMessage());
				}
			} catch (SQLException e) {
				System.err.println("archiveUser() connection failed: " + e.getMessage());
			}
		}

		return false;
	}

	public boolean unarchiveUser(int userId) {

		if (hasSoftDeleteColumn()) {
			String sql = "UPDATE users SET deleted_at = NULL WHERE user_id = ? AND deleted_at IS NOT NULL";
			try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				int rows = stmt.executeUpdate();
				System.out.println("unarchiveUser() soft-delete: " + rows + " rows affected for user " + userId);
				if (rows > 0)
					return true;
			} catch (SQLException e) {
				System.err.println("unarchiveUser() soft-delete failed: " + e.getMessage());
			}
		}

		if (archiveTableExists()) {
			String insert = "INSERT INTO users " + "(user_id, system_role, student_id, umak_email_address, password, "
					+ " college, year_level, course_program, first_name, last_name, "
					+ " karma_score, profile_image, bio, contact_num, home_address, gcash_num, bank_network) "
					+ "SELECT user_id, system_role, student_id, umak_email_address, password, "
					+ "       college, year_level, course_program, first_name, last_name, "
					+ "       karma_score, profile_image, bio, contact, home_address, gcash_num, bank_number "
					+ "FROM users_archive WHERE user_id = ?";
			String delete = "DELETE FROM users_archive WHERE user_id = ?";

			try (Connection conn = getConn()) {
				conn.setAutoCommit(false);
				try (PreparedStatement ins = conn.prepareStatement(insert);
						PreparedStatement del = conn.prepareStatement(delete)) {
					ins.setInt(1, userId);
					ins.executeUpdate();
					del.setInt(1, userId);
					del.executeUpdate();
					conn.commit();
					System.out.println("unarchiveUser() archive table: restored user " + userId);
					return true;
				} catch (SQLException e) {
					conn.rollback();
					System.err.println("unarchiveUser() archive table failed: " + e.getMessage());
				}
			} catch (SQLException e) {
				System.err.println("unarchiveUser() connection failed: " + e.getMessage());
			}
		}

		return false;
	}

	private static Boolean softDeleteColumnExists = null;
	private static Boolean archiveTableExistsCache = null;

	private boolean hasSoftDeleteColumn() {
		if (softDeleteColumnExists != null)
			return softDeleteColumnExists;
		try (Connection conn = getConn()) {
			DatabaseMetaData meta = conn.getMetaData();
			try (ResultSet rs = meta.getColumns(null, null, "users", "deleted_at")) {
				softDeleteColumnExists = rs.next();
			}
		} catch (SQLException e) {
			System.out.println("hasSoftDeleteColumn() check failed: " + e.getMessage());
			softDeleteColumnExists = false;
		}
		System.out.println("Schema detection — users.deleted_at exists: " + softDeleteColumnExists);
		return softDeleteColumnExists;
	}

	private boolean archiveTableExists() {
		if (archiveTableExistsCache != null)
			return archiveTableExistsCache;
		try (Connection conn = getConn()) {
			DatabaseMetaData meta = conn.getMetaData();
			try (ResultSet rs = meta.getTables(null, null, "users_archive", new String[] { "TABLE" })) {
				archiveTableExistsCache = rs.next();
			}
		} catch (SQLException e) {
			System.out.println("archiveTableExists() check failed: " + e.getMessage());
			archiveTableExistsCache = false;
		}
		System.out.println("Schema detection — users_archive table exists: " + archiveTableExistsCache);
		return archiveTableExistsCache;
	}

	private Connection getConn() throws SQLException {
		return DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(),
				DatabaseManager.getPassword());
	}

	private String resolveUserColumn(String filter) {
		if (filter == null)
			return "first_name";
		switch (filter) {
		case "ID":
			return "user_id";
		case "Student ID":
			return "student_id";
		case "First Name":
			return "first_name";
		case "Last Name":
			return "last_name";
		case "College":
			return "college";
		case "Year Level":
			return "year_level";
		case "System Role":
			return "system_role";
		default:
			return "first_name";
		}
	}

	private AdminUsers mapRow(ResultSet rs, boolean isArchived) throws SQLException {
		AdminUsers user = new AdminUsers();
		user.setUserId(rs.getInt("user_id"));
		user.setSystemRole(rs.getString("system_role"));
		user.setStudentId(rs.getString("student_id"));
		user.setFirstName(rs.getString("first_name"));
		user.setLastName(rs.getString("last_name"));
		user.setCollege(rs.getString("college"));
		user.setYearLevel(rs.getString("year_level"));
		user.setCourseProgram(rs.getString("course_program"));
		user.setKarmaScore(rs.getInt("karma_score"));
		user.setArchived(isArchived);
		return user;
	}
}