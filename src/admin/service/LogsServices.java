package admin.services;

import admin.database.LogDatabase;
import admin.models.AdminLog;

import java.util.List;

// AdminLogService is what AdminTable calls for the log tabs.
// Admins only VIEW logs — they never edit or delete them.
// Logs are written automatically by AdminUserService and AdminItemService.

public class AdminLogService {

    private LogDatabase logDB = new LogDatabase();

    // -------------------------------------------------------
    // READ — called when each log tab loads
    // -------------------------------------------------------

    // Returns USER logs as a 2D array ready for JTable
    // Each row = { ID, LogType, Description, Date, UserID }
    public Object[][] getUserLogsForTable() {
        return buildTableData(logDB.getLogsByType("USER"));
    }

    // Returns ITEM logs as a 2D array ready for JTable
    public Object[][] getItemLogsForTable() {
        return buildTableData(logDB.getLogsByType("ITEM"));
    }

    // Returns TRANSACTION logs as a 2D array ready for JTable
    public Object[][] getTransactionLogsForTable() {
        return buildTableData(logDB.getLogsByType("TRANSACTION"));
    }

    // Returns REPUTATION logs as a 2D array ready for JTable
    public Object[][] getReputationLogsForTable() {
        return buildTableData(logDB.getLogsByType("REPUTATION"));
    }

    // Returns total count of all logs (for the status card in AdminPanel)
    public int getTotalLogs() {
        return logDB.countAllLogs();
    }

    // Returns total count of all transactions (for the status card in AdminPanel)
    public int getTotalTransactions() {
        return logDB.countTransactions();
    }

    // -------------------------------------------------------
    // PRIVATE HELPER
    // -------------------------------------------------------

    // Converts a List<AdminLog> into a 2D array for JTable
    // Reused by all the methods above so we don't repeat the same loop
    private Object[][] buildTableData(List<AdminLog> logs) {
        Object[][] data = new Object[logs.size()][5];

        for (int i = 0; i < logs.size(); i++) {
            AdminLog log = logs.get(i);
            data[i][0] = log.getId();
            data[i][1] = log.getLogType();
            data[i][2] = log.getDescription();
            data[i][3] = log.getCreatedAt();
            data[i][4] = log.getUserId();
        }

        return data;
    }
}