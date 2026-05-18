

package admin.services;

import admin.database.LogsDatabase;
import admin.models.AdminLogs;

import java.util.List;

public class AdminLogsServices {

    private final LogsDatabase logDB = new LogsDatabase();



    public Object[][] getUserLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("USER", filter, keyword, dateFrom, dateTo));
    }

    
    public Object[][] getItemLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("ITEM", filter, keyword, dateFrom, dateTo));
    }

    public Object[][] getTransactionLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("TRANSACTION", filter, keyword, dateFrom, dateTo));
    }

    public Object[][] getReputationLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("REPUTATION", filter, keyword, dateFrom, dateTo));
    }


    public int getTotalLogs() {
        return logDB.countAllLogs();
    }

    public int getTotalTransactions() {
        return logDB.countTransactions();
    }


    private Object[][] buildTableData(List<AdminLogs> logs) {
        // Pre-size the array to avoid resizing — one row per log entry.
        Object[][] data = new Object[logs.size()][5];

        for (int i = 0; i < logs.size(); i++) {
            AdminLogs log = logs.get(i);
            data[i][0] = log.getId();
            data[i][1] = log.getUserId();
            data[i][2] = formatLogType(log.getLogType());
            data[i][3] = log.getDescription();
            data[i][4] = log.getCreatedAt();   
        }

        return data;
    }

    
    public String formatLogType(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        String[] words = raw.split("[-_]");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" "); 

            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1).toLowerCase());
        }

        return sb.toString();
    }
}