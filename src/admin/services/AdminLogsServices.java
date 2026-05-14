package admin.services;

import admin.database.LogsDatabase;
import admin.models.AdminLogs;
import java.util.List;

public class AdminLogsServices {

	private LogsDatabase logDB = new LogsDatabase();

	public Object[][] getUserLogsForTable() {
		return buildTableData(logDB.getLogsByType("USER"));
	}

	public Object[][] getItemLogsForTable() {
		return buildTableData(logDB.getLogsByType("ITEM"));
	}

	public Object[][] getTransactionLogsForTable() {
		return buildTableData(logDB.getLogsByType("TRANSACTION"));
	}

	public Object[][] getReputationLogsForTable() {
		return buildTableData(logDB.getLogsByType("REPUTATION"));
	}

	public Object[][] getUserLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return buildTableData(logDB.getLogsByTypeFiltered("USER", filter, keyword, dateFrom, dateTo));
	}

	public Object[][] getItemLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return buildTableData(logDB.getLogsByTypeFiltered("ITEM", filter, keyword, dateFrom, dateTo));
	}

	public Object[][] getTransactionLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return buildTableData(logDB.getLogsByTypeFiltered("TRANSACTION", filter, keyword, dateFrom, dateTo));
	}

	public Object[][] getReputationLogsFiltered(String filter, String keyword, String dateFrom, String dateTo) {
		return buildTableData(logDB.getLogsByTypeFiltered("REPUTATION", filter, keyword, dateFrom, dateTo));
	}

	public int getTotalLogs() {
		return logDB.countAllLogs();
	}

	public int getTotalTransactions() {
		return logDB.countTransactions();
	}

	private Object[][] buildTableData(List<AdminLogs> logs) {
		Object[][] data = new Object[logs.size()][5];
		for (int i = 0; i < logs.size(); i++) {
			AdminLogs log = logs.get(i);
			data[i][0] = log.getId();
			data[i][1] = log.getLogType();
			data[i][2] = log.getDescription();
			data[i][3] = log.getCreatedAt();
			data[i][4] = log.getUserId();
		}
		return data;
	}
}