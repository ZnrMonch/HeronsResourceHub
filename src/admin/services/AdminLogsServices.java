// ══════════════════════════════════════════════════════════════════════════════
// AdminLogsServices.java
// ──────────────────────────────────────────────────────────────────────────────
// Service layer between LogsDatabase and the UI (Swing panels / controllers).
//
// WHY A SERVICE LAYER?
//   The UI should not talk directly to the database. This class translates raw
//   AdminLogs objects into Object[][] tables that JTable can display, and
//   exposes clean method names the UI can call without knowing SQL details.
//
// COLUMN ORDER in every returned Object[][]:
//   [0] id  [1] userId  [2] formatted action  [3] description  [4] createdAt
// ══════════════════════════════════════════════════════════════════════════════
package admin.services;

import admin.database.LogsDatabase;
import admin.models.AdminLogs;

import java.util.List;

public class AdminLogsServices {

    // ── Dependencies ──────────────────────────────────────────────────────────

    // Direct connection to the log tables.
    private final LogsDatabase logDB = new LogsDatabase();


    // ── Filtered table-data getters ───────────────────────────────────────────
    //
    // Each method fetches logs for one table type and returns them as a 2D array
    // ready for a JTable model. All parameters are passed straight to LogsDatabase.
    //
    // @param filter    Column to search in (e.g. "User ID", "Action", "Description")
    // @param keyword   Text to match — pass "" to skip keyword filtering
    // @param dateFrom  Start date "YYYY-MM-DD" or null/empty to skip
    // @param dateTo    End   date "YYYY-MM-DD" or null/empty to skip

    /** Returns user-log rows matching the given filters. */
    public Object[][] getUserLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("USER", filter, keyword, dateFrom, dateTo));
    }

    /** Returns item-log rows matching the given filters. */
    public Object[][] getItemLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("ITEM", filter, keyword, dateFrom, dateTo));
    }

    /** Returns transaction-log rows matching the given filters. */
    public Object[][] getTransactionLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("TRANSACTION", filter, keyword, dateFrom, dateTo));
    }

    /** Returns reputation-log rows matching the given filters. */
    public Object[][] getReputationLogsFiltered(
            String filter, String keyword, String dateFrom, String dateTo) {
        return buildTableData(
                logDB.getLogsByTypeFiltered("REPUTATION", filter, keyword, dateFrom, dateTo));
    }


    // ── Dashboard stat methods ────────────────────────────────────────────────

    /**
     * Returns the grand total of rows across all four log tables.
     * Displayed on the admin dashboard as "Total Logs".
     */
    public int getTotalLogs() {
        return logDB.countAllLogs();
    }

    /**
     * Returns the total number of rows in transaction_log.
     * Displayed on the admin dashboard as "Total Transactions".
     */
    public int getTotalTransactions() {
        return logDB.countTransactions();
    }


    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Converts a list of AdminLogs into the Object[][] format JTable expects.
     *
     * Column layout (index → meaning):
     *   0 → log id
     *   1 → user id
     *   2 → action label (formatted, e.g. "Borrow Approved")
     *   3 → description / reason text
     *   4 → timestamp string (may be null for transaction rows)
     *
     * @param logs  The raw list returned by LogsDatabase
     */
    private Object[][] buildTableData(List<AdminLogs> logs) {
        // Pre-size the array to avoid resizing — one row per log entry.
        Object[][] data = new Object[logs.size()][5];

        for (int i = 0; i < logs.size(); i++) {
            AdminLogs log = logs.get(i);
            data[i][0] = log.getId();
            data[i][1] = log.getUserId();
            data[i][2] = formatLogType(log.getLogType()); // make it readable for the UI
            data[i][3] = log.getDescription();
            data[i][4] = log.getCreatedAt();              // null-safe; JTable shows "" for null
        }

        return data;
    }

    /**
     * Converts a raw DB action value into a readable label for the UI.
     *
     * Examples:
     *   "Borrow-Approved"  →  "Borrow Approved"
     *   "Sell-Relisted"    →  "Sell Relisted"
     *   "Create"           →  "Create"
     *   null / ""          →  ""
     *
     * Strategy: split on hyphen or underscore, capitalise each word, rejoin with space.
     */
    public String formatLogType(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        // Split on hyphens (transaction actions) or underscores (future-proofing).
        String[] words = raw.split("[-_]");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" "); // space between words
            // Capitalise the first letter; lowercase the rest for consistency.
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1).toLowerCase());
        }

        return sb.toString();
    }
}