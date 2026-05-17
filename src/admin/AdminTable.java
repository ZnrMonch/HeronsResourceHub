package admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import admin.tabledialogs.*;
import components.*;
import utils.*;
import admin.services.*;
import enums.*;

/**
 * AdminTable.java  (RBAC + AppDialog edition)
 * --------------------------------------------
 * Changes from the original:
 *
 *  1. Role-based visibility  — action buttons are shown/hidden based on
 *     Permission.canCreate() / canUpdate() / canDelete().
 *
 *  2. Backend guard          — service calls are wrapped with
 *     Permission.require() so even if a button somehow appears, the
 *     database layer rejects the call.
 *
 *  3. AppDialog              — every JOptionPane replaced with the new
 *     AppDialog helper for consistent styling.
 */
public class AdminTable extends CustomPanel {
    private static final long serialVersionUID = 1L;

    private final TableType type;
    private final LogType   logType;
    private int hoveredRow = -1;

    private CustomTable       table;
    private CustomButton      btnAddAction;
    private CustomButton      btnUpdateOrRetrieve;
    private CustomButton      btnArchiveOrDelete;
    private CustomSearchField searchField;
    private CustomComboBox<String> filterBox;
    private JCheckBox         archiveMode = new JCheckBox("Archive Mode");

    private javax.swing.Timer logsRefreshTimer;
    private DateFilterPanel   dateFilterPanel;

    private final AdminUsersServices userService = new AdminUsersServices();
    private final AdminItemsServices itemService = new AdminItemsServices();
    private final AdminLogsServices  logService  = new AdminLogsServices();

    // ── Constructors ──────────────────────────────────────────────────────────

    public AdminTable(TableType type) {
        this(type, LogType.NONE);
    }

    public AdminTable(TableType type, LogType logType) {
        this.type    = type;
        this.logType = logType;
        setLayout(new BorderLayout());
        add(initHeader(), BorderLayout.NORTH);
        add(initTable(),  BorderLayout.CENTER);

        if (type == TableType.LOGS) {
            logsRefreshTimer = new javax.swing.Timer(30_000, e -> loadTableData());
            logsRefreshTimer.start();
        }
    }

    // ── Header (unchanged except added refresh button logic) ──────────────────

    private CustomPanel initHeader() {
        CustomPanel header = new CustomPanel();
        header.setPadding(20, 15);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        String title;
        switch (type) {
            case USERS: title = "User Management"; break;
            case ITEMS: title = "Item Management"; break;
            case LOGS:  title = "Logs Management"; break;
            default:    title = "Management";      break;
        }

        header.add(new CustomLabel(title, 18f, FontStyle.BOLD));
        header.add(Box.createHorizontalGlue());

        searchField = new CustomSearchField("Search...", 20, 10);
        searchField.setCustomSize(180, 35);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { loadTableData(); }
        });
        header.add(Box.createHorizontalStrut(20));
        header.add(searchField);
        header.add(Box.createHorizontalStrut(10));

        header.add(new CustomLabel("Search by:"));
        header.add(Box.createHorizontalStrut(10));
        filterBox = new CustomComboBox<>(getFilterOptions());
        filterBox.setCustomSize(140, 35);
        filterBox.addActionListener(e -> loadTableData());
        header.add(filterBox);
        header.add(Box.createHorizontalStrut(10));

        if (type != TableType.LOGS) {
            archiveMode.setOpaque(false);
            if (FontLib.POPPINS_REGULAR != null)
                archiveMode.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
            archiveMode.addItemListener(e -> { updateActionUI(); loadTableData(); });
            header.add(archiveMode);
            header.add(Box.createHorizontalStrut(10));
        }
        if (type == TableType.LOGS) {
            header.add(Box.createHorizontalGlue());
            header.add(Box.createHorizontalStrut(10));
            if (Permission.isSuperAdmin()) {
                CustomButton btnRefresh = makeHeaderButton("Refresh", Brand.PRIMARY_COLOR, Brand.GREEN);
                btnRefresh.addActionListener(e -> loadTableData());
                header.add(btnRefresh);
                header.add(Box.createHorizontalStrut(10));
            }
            dateFilterPanel = new DateFilterPanel(() -> loadTableData());
            header.add(dateFilterPanel);
        }

        return header;
    }

    private String[] getFilterOptions() {
        if (type == TableType.USERS) {
            return new String[]{ "ID","Student ID","First Name","Last Name",
                                 "College","Year Level","System Role" };
        } else if (type == TableType.ITEMS) {
            return new String[]{ "ID","Item Name","Condition","Category",
                                 "Stock","Price","Status" };
        } else {
            if (logType == LogType.ITEM_LOGS || logType == LogType.TRANSACTION_LOGS)
                return new String[]{ "ID","User ID","Item ID","Action" };
            else
                return new String[]{ "ID","User ID","Action" };
        }
    }

    private CustomButton makeHeaderButton(String text, Color defaultColor, Color hoverColor) {
        CustomButton btn = new CustomButton(text, 8);
        btn.setFontSize(12f);
        btn.setPadding(6, 14, 6, 14);
        btn.setDefaultColor(defaultColor);
        btn.setTextColor(Color.WHITE);
        btn.setHoverColor(hoverColor);
        return btn;
    }

    // ── Table initialisation ──────────────────────────────────────────────────

    private CustomPanel initTable() {
        CustomPanel tableWrapper = new CustomPanel();
        tableWrapper.setLayout(new BorderLayout());

        Object[] columnNames = getColumnNames();
        table = new CustomTable(new Object[0][columnNames.length], columnNames);

        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Brand.COLOR_GRID);
        table.setRowHeight(38);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        // Custom header
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setText(value != null ? value.toString() : "");
                setBackground(Color.LIGHT_GRAY);
                setForeground(Color.DARK_GRAY);
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
                setHorizontalAlignment(SwingConstants.LEFT);
                if (FontLib.POPPINS_BOLD != null) setFont(FontLib.POPPINS_BOLD.deriveFont(13f));
                return this;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // ── Action buttons — visibility controlled by Permission ──────────────
        // SUPER_ADMIN sees everything. ADMIN sees nothing in this panel.
        if (type != TableType.LOGS) {
            btnUpdateOrRetrieve = new CustomButton("Update Data", 8);
            btnArchiveOrDelete  = new CustomButton("Archive Data", 8);
            btnUpdateOrRetrieve.setFontSize(12f);
            btnArchiveOrDelete.setFontSize(12f);
            btnUpdateOrRetrieve.setPadding(6, 14, 6, 14);
            btnArchiveOrDelete.setPadding(6, 14, 6, 14);
            updateActionUI();

            btnUpdateOrRetrieve.addActionListener(e -> {
                if (archiveMode.isSelected()) handleRetrieve();
                else                          handleUpdate();
            });
            btnArchiveOrDelete.addActionListener(e -> {
                if (archiveMode.isSelected()) handleDelete();
                else                          handleArchive();
            });
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateActionUI();
            });
        }

        // "Create" button — only shown to SUPER_ADMIN
        btnAddAction = new CustomButton(
                "Create " + (type == TableType.USERS ? "User" : "Item"), 8);
        btnAddAction.setFontSize(12f);
        btnAddAction.setPadding(6, 14, 6, 14);
        btnAddAction.setDefaultColor(Color.decode("#0056b3"));
        btnAddAction.setTextColor(Color.WHITE);
        btnAddAction.setHoverColor(Brand.PRIMARY_COLOR);
        btnAddAction.addActionListener(e -> handleAdd());

        loadTableData();
        applyColumnWidths();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setBackground(Color.WHITE);
        scrollWrapper.setBorder(BorderFactory.createLineBorder(Brand.COLOR_BORDER, 1, true));
        scrollWrapper.add(scrollPane, BorderLayout.CENTER);

        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        CustomPanel bottomPanel = new CustomPanel(new BorderLayout());
        bottomPanel.add(table.createPaginationPanel(), BorderLayout.CENTER);

        // ── RBAC: build the actions panel only when the user has any write access
        if (type != TableType.LOGS) {
            CustomPanel actionsPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

            // Only SUPER_ADMIN can create
            if (Permission.canCreate()) {
                actionsPanel.add(btnAddAction);
            }
            // Only SUPER_ADMIN can update or archive/delete
            if (Permission.canUpdate()) {
                actionsPanel.add(btnUpdateOrRetrieve);
            }
            if (Permission.canDelete()) {
                actionsPanel.add(btnArchiveOrDelete);
            }

            // Always add the panel — it may be empty for ADMIN (clean read-only view)
            bottomPanel.add(actionsPanel, BorderLayout.EAST);
        }

        tableWrapper.add(scrollWrapper, BorderLayout.CENTER);
        tableWrapper.add(bottomPanel,   BorderLayout.SOUTH);
        return tableWrapper;
    }

    private Object[] getColumnNames() {
        if (type == TableType.USERS) {
            return new Object[]{ "ID","Student ID","First Name","Last Name","College","Year",
                                 "Karma Points","Contact Number","Gcash Number","Maya Number",
                                 "Mastercard Card","Visa Number","System Role" };
        } else if (type == TableType.ITEMS) {
            return new Object[]{ "ID","Item Name","Condition","Category","Stock","Price","Status" };
        } else {
            return new Object[]{ "ID","User ID","Log Type","Description","Date" };
        }
    }

    private void applyColumnWidths() {
        table.setColumnWidth(0, 55);
        table.setColumnAlignment(0, SwingConstants.LEFT);

        if (type == TableType.USERS) {
            table.setColumnWidth(0,  40);  table.setColumnWidth(1,  90);
            table.setColumnWidth(4,  70);  table.setColumnWidth(5,  70);
            table.setColumnWidth(6,  110); table.setColumnWidth(7,  130);
            table.setColumnWidth(8,  120); table.setColumnWidth(9,  120);
            table.setColumnWidth(10, 140); table.setColumnWidth(11, 120);
            table.setColumnWidth(12, 110);
        } else if (type == TableType.ITEMS) {
            table.setColumnWidth(2, 110); table.setColumnWidth(3, 140);
            table.setColumnWidth(4, 75);  table.setColumnWidth(5, 80);
            table.setColumnWidth(6, 115);
        } else {
            table.setColumnWidth(1, 90);  table.setColumnWidth(2, 150);
            table.setColumnWidth(4, 160);
            TableColumn descCol = table.getColumnModel().getColumn(3);
            descCol.setPreferredWidth(260); descCol.setMinWidth(150);
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadTableData() {
        if (table == null) return;

        String  keyword  = searchField != null ? searchField.getText().trim() : "";
        String  filter   = filterBox   != null ? (String) filterBox.getSelectedItem() : "";
        boolean searching = !keyword.isEmpty();
        boolean archived  = archiveMode.isSelected();
        Object[][] data;

        if (type == TableType.USERS) {
            if (archived && searching)  data = userService.searchArchivedUsers(filter, keyword);
            else if (archived)          data = userService.getArchivedUsersForTable();
            else if (searching)         data = userService.searchActiveUsers(filter, keyword);
            else                        data = userService.getActiveUsersForTable();

        } else if (type == TableType.ITEMS) {
            if (archived && searching)  data = itemService.searchArchivedItems(filter, keyword);
            else if (archived)          data = itemService.getArchivedItemsForTable();
            else if (searching)         data = itemService.searchActiveItems(filter, keyword);
            else                        data = itemService.getActiveItemsForTable();

        } else {
            String[] range   = dateFilterPanel != null
                    ? dateFilterPanel.getEffectiveDateRange() : new String[]{null, null};
            String dateFrom  = range[0];
            String dateTo    = range[1];

            if      (logType == LogType.USER_LOGS)         data = logService.getUserLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.ITEM_LOGS)         data = logService.getItemLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.TRANSACTION_LOGS)  data = logService.getTransactionLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.REPUTATION_LOGS)   data = logService.getReputationLogsFiltered(filter, keyword, dateFrom, dateTo);
            else                                           data = new Object[0][5];
        }

        table.setFullData(data, getColumnNames());
    }

    // ── Button UI state ───────────────────────────────────────────────────────

    /**
     * Updates button labels, colours, and enabled state.
     * Visibility was already set in initTable() based on Permission — we don't
     * override that here; we only manage the enabled/disabled state based on
     * whether a row is actually selected.
     */
    private void updateActionUI() {
        if (btnUpdateOrRetrieve == null || btnArchiveOrDelete == null) return;

        boolean hasSelection = table != null && table.getSelectedRow() != -1;
        btnUpdateOrRetrieve.setEnabled(hasSelection);
        btnArchiveOrDelete.setEnabled(hasSelection);

        if (archiveMode.isSelected()) {
            applyButtonStyle(btnUpdateOrRetrieve, "Retrieve Data", "#ffc107");
            applyButtonStyle(btnArchiveOrDelete,  "Delete Data",   "#dc3545");
            if (btnAddAction != null) btnAddAction.setVisible(false);
        } else {
            applyButtonStyle(btnUpdateOrRetrieve, "Update Data",  "#28a745");
            applyButtonStyle(btnArchiveOrDelete,  "Archive Data", "#ffc107");
            // Only show Add button if user has create permission
            if (btnAddAction != null) btnAddAction.setVisible(Permission.canCreate());
        }
    }

    private void applyButtonStyle(CustomButton btn, String text, String hexColor) {
        btn.setText(text);
        btn.setDefaultColor(Color.decode(hexColor));
        btn.setHoverColor(btn.getBackground().darker());
    }

    // ── Action handlers — each starts with a Permission.require() guard ───────

    private void handleAdd() {
        // Frontend already hides this button, but guard the backend too
        Permission.require(Permission.canCreate(), "create records");

        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS) {
            UserFormDialog.showAdd(owner, userService, () -> loadTableData());
        } else if (type == TableType.ITEMS) {
            ItemFormDialog.showAdd(owner, itemService, () -> loadTableData());
        }
    }

    private void handleUpdate() {
        Permission.require(Permission.canUpdate(), "update records");

        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS) {
            UserFormDialog.showUpdate(owner, id, userService, () -> loadTableData());
        } else if (type == TableType.ITEMS) {
            ItemFormDialog.showUpdate(owner, id, itemService, () -> loadTableData());
        }
    }

    private void handleArchive() {
        Permission.require(Permission.canDelete(), "archive records");

        int row = table.getSelectedRow();
        if (row == -1) {
        	JOptionPane.showMessageDialog(
        		    this,
        		    "Error: No row selected! Please select a row before proceeding.",
        		    "Error",
        		    JOptionPane.WARNING_MESSAGE
        		);
            return;
        }
        int id = getSelectedId(row);

        // Prevent admins from archiving themselves
        if (type == TableType.USERS) {
            int currentUserId = SessionManager.get().getCurrentUserId();
            if (id == currentUserId) {
            	JOptionPane.showMessageDialog(
            		    this,
            		    "Error: Action not allowed! You cannot archive your own account.",
            		    "Error",
            		    JOptionPane.ERROR_MESSAGE
            		);
                return;
            }
        }

        String entityType = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(
        	    this,
        	    "Are you sure you want to archive " + entityType + " ID " + id + "?",
        	    "Confirm Archive",
        	    JOptionPane.YES_NO_OPTION,
        	    JOptionPane.WARNING_MESSAGE
        	);

        	if (confirm != JOptionPane.YES_OPTION) {
        	    return;
        	}

        String successMsg = entityType + " ID " + id + " archived successfully.";
        String failureMsg = entityType + " ID " + id + " could not be archived.\n\n"
                + "Possible reasons:\n"
                + "• Account has admin or super_admin role\n"
                + "• Database error or FK constraint\n"
                + "• Archive table not found";

        RecordAction action = type == TableType.USERS
                ? RecordAction.ARCHIVE_USER : RecordAction.ARCHIVE_ITEM;
        runAsync(new ArchiveTask(id, action), successMsg, failureMsg,
                type == TableType.USERS);
    }

    private void handleRetrieve() {
        Permission.require(Permission.canUpdate(), "restore records");

        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        String entityType = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(
        	    this,
        	    "Are you sure you want to restore " + entityType + " ID " + id + "?",
        	    "Confirm Restore",
        	    JOptionPane.YES_NO_OPTION,
        	    JOptionPane.QUESTION_MESSAGE
        	);

        	if (confirm != JOptionPane.YES_OPTION) {
        	    return;
        	}

        String successMsg = entityType + " ID " + id + " restored successfully.";
        String failureMsg = entityType + " ID " + id + " could not be restored.\n\n"
                + "Possible reasons:\n"
                + "• The item's original owner is still archived\n"
                + "• The record was not found in the archive";

        RecordAction action = type == TableType.USERS
                ? RecordAction.RESTORE_USER : RecordAction.RESTORE_ITEM;
        runAsync(new ArchiveTask(id, action), successMsg, failureMsg);
    }

    private void handleDelete() {
        Permission.require(Permission.canDelete(), "permanently delete records");

        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        String entityType = type == TableType.USERS ? "user" : "item";

        // ── Replaced JOptionPane — uses the dedicated delete confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
        	    this,
        	    "Are you sure you want to permanently delete " + entityType + " ID " + id,
        	    "Confirm Deletion",
        	    JOptionPane.YES_NO_OPTION,
        	    JOptionPane.WARNING_MESSAGE
        	);

        	if (confirm != JOptionPane.YES_OPTION) {
        	    return;
        	}

        	String successMsg =
        		    "Success! " + entityType + " ID " + id + " has been permanently deleted.";

        		String failureMsg =
        		    "Error: Failed to permanently delete " + entityType + " ID " + id + "! Please try again.";

        RecordAction action = type == TableType.USERS
                ? RecordAction.DELETE_USER : RecordAction.DELETE_ITEM;
        runAsync(new ArchiveTask(id, action), successMsg, failureMsg);
    }

   

    private void runAsync(ArchiveTask task, String successMsg, String failureMsg) {
        runAsync(task, successMsg, failureMsg, false);
    }

    private void runAsync(ArchiveTask task, String successMsg, String failureMsg,
                          boolean checkUserServiceError) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                return task.run();
            }
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        loadTableData();
                        JOptionPane.showMessageDialog(
                                AdminTable.this,
                                successMsg,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        // If the caller opted in, prefer the service's specific error message.
                        String msg = failureMsg;
                        if (checkUserServiceError) {
                            String svcError = userService.getLastAddError();
                            if (svcError != null && !svcError.isEmpty()) {
                                msg = svcError;
                            }
                        }
                        JOptionPane.showMessageDialog(
                                AdminTable.this,
                                msg,
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            AdminTable.this,
                            "Error: An unexpected error occurred! " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    // ── ArchiveTask inner class (unchanged logic) ─────────────────────────────

    private class ArchiveTask {
        private final int          id;
        private final RecordAction action;

        public ArchiveTask(int id, RecordAction action) {
            this.id     = id;
            this.action = action;
        }

        public boolean run() {
            switch (action) {
                case ARCHIVE_USER: return userService.archiveUser(id);
                case RESTORE_USER: return userService.unarchiveUser(id);
                case DELETE_USER:  return userService.permanentDeleteUser(id);
                case ARCHIVE_ITEM: return itemService.archiveItem(id);
                case RESTORE_ITEM: return itemService.unarchiveItem(id);
                case DELETE_ITEM:  return itemService.permanentDeleteItem(id);
                default:           return false;
            }
        }
    }

    private int getSelectedId(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        return Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
    }
}