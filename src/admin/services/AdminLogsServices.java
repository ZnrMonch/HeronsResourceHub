package admin.services;

import admin.database.LogsDatabase;
import admin.models.AdminLogs;
import java.util.List;

public class AdminLogsServices {

	private final LogsDatabase logDB = new LogsDatabase();

	
	public Object[][] getUserLogsForTable() {
		return filtered("USER", "", "", null, null);
	}

	public Object[][] getItemLogsForTable() {
		return filtered("ITEM", "", "", null, null);
	}

	public Object[][] getTransactionLogsForTable() {
		return filtered("TRANSACTION", "", "", null, null);
	}

	public Object[][] getReputationLogsForTable() {
		return filtered("REPUTATION", "", "", null, null);
	}

	// Filtered table data by keyword, column filter, and date range
	public Object[][] getUserLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return filtered("USER", filter, keyword, dateFrom, dateTo);
	}

	public Object[][] getItemLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return filtered("ITEM", filter, keyword, dateFrom, dateTo);
	}

	public Object[][] getTransactionLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return filtered("TRANSACTION", filter, keyword, dateFrom, dateTo);
	}

	public Object[][] getReputationLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return filtered("REPUTATION", filter, keyword, dateFrom, dateTo);
	}

	public int getTotalLogs() {
		return logDB.countAllLogs();
	}

	public int getTotalTransactions() {
		return logDB.countTransactions();
	}

	private Object[][] filtered(String logType, String filter, String keyword, String dateFrom, String dateTo) {
		return buildTableData(logDB.getLogsByTypeFiltered(logType, filter, keyword, dateFrom, dateTo));
	}

	private Object[][] buildTableData(List<AdminLogs> logs) {
		Object[][] data = new Object[logs.size()][5];
		for (int i = 0; i < logs.size(); i++) {
			AdminLogs log = logs.get(i);
			data[i][0] = log.getId();
			data[i][1] = log.getUserId();
			data[i][2] = log.getLogType();
			data[i][3] = log.getDescription();
			data[i][4] = log.getCreatedAt();
		}
		return data;
	}
}