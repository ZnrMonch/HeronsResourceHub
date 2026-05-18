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
    private JCheckBox         archiveMode        = new JCheckBox("Archive Mode");
    private JCheckBox         selectAllBox       = new JCheckBox();
    private CustomLabel       selectedCountLabel  = new CustomLabel("Selected: 0", 12f, FontStyle.REGULAR);
    private final CustomCheckBox chkRenderer     = new CustomCheckBox();
    private final CustomCheckBox chkEditor       = new CustomCheckBox();
    private final CustomCheckBox chkHeader       = new CustomCheckBox();
    
    private javax.swing.Timer logsRefreshTimer;
    private DateFilterPanel   dateFilterPanel;

    private final AdminUsersServices userService = new AdminUsersServices();
    private final AdminItemsServices itemService = new AdminItemsServices();
    private final AdminLogsServices  logService  = new AdminLogsServices();

 
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

    private CustomPanel initHeader() {
        CustomPanel header = new CustomPanel();
        header.setPadding(10, 15);
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
            return new String[]{ "All", "ID", "Student ID", "First Name",
                                 "Last Name", "College", "Year Level", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new String[]{ "All", "ID", "Item Name", "Condition",
                                 "Category", "Stock", "Price", "Status" };
        } else {
            if (logType == LogType.ITEM_LOGS || logType == LogType.TRANSACTION_LOGS)
                return new String[]{ "All", "ID", "User ID", "Item ID", "Action" };
            else
                return new String[]{ "All", "ID", "User ID", "Action" };
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

      private CustomPanel initTable() {
        CustomPanel tableWrapper = new CustomPanel();
        tableWrapper.setLayout(new BorderLayout());

        Object[] columnNames = getColumnNames();
      
        javax.swing.table.DefaultTableModel initModel = new javax.swing.table.DefaultTableModel(
                new Object[0][columnNames.length], columnNames) {
            @Override
            public Class<?> getColumnClass(int col) {
                if ((type == TableType.USERS || type == TableType.ITEMS) && col == 0)
                    return Boolean.class;
                return Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return (type != TableType.LOGS && col == 0);
            }
        };
        table = new CustomTable(initModel) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (type == TableType.LOGS) return Object.class;
                if (col == 0 && type != TableType.LOGS) return Boolean.class;
                return Object.class;
            }
        };

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

       
            chkHeader.setHeader(true);
            table.setCheckboxColumn(0, chkRenderer, chkEditor,
                    (tbl, val, sel, foc, row, col) -> {
                        chkHeader.setChecked(selectAllBox.isSelected());
                        return chkHeader;
                    });
            
            chkEditor.addCellEditorListener(new javax.swing.event.CellEditorListener() {
                public void editingStopped(javax.swing.event.ChangeEvent e) {
                    SwingUtilities.invokeLater(AdminTable.this::updateSelectedCount);
                }
                public void editingCanceled(javax.swing.event.ChangeEvent e) {}
            });

            table.getTableHeader().addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int col = table.getTableHeader().columnAtPoint(e.getPoint());
                    if (col != 0) return;
                    selectAllBox.setSelected(!selectAllBox.isSelected());
                    boolean checked = selectAllBox.isSelected();
                    for (int r = 0; r < table.getModel().getRowCount(); r++)
                        table.getModel().setValueAt(checked, r, 0);
                    table.repaint();
                    updateSelectedCount();
                }
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

        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        CustomPanel bottomPanel = new CustomPanel(new BorderLayout());
        bottomPanel.add(table.createPaginationPanel(), BorderLayout.CENTER);

        if (type != TableType.LOGS) {
            selectedCountLabel.setForeground(Color.GRAY);
            if (FontLib.POPPINS_REGULAR != null)
                selectedCountLabel.setFont(FontLib.POPPINS_REGULAR.deriveFont(11f));
            bottomPanel.add(selectedCountLabel, BorderLayout.WEST);
        }

  
        if (type != TableType.LOGS) {
            CustomPanel actionsPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            if (Permission.canCreate()) actionsPanel.add(btnAddAction);
            if (Permission.canUpdate()) actionsPanel.add(btnUpdateOrRetrieve);
            if (Permission.canDelete()) actionsPanel.add(btnArchiveOrDelete);
            bottomPanel.add(actionsPanel, BorderLayout.EAST);
        }

        tableWrapper.add(scrollWrapper, BorderLayout.CENTER);
        tableWrapper.add(bottomPanel,   BorderLayout.SOUTH);
        return tableWrapper;
    }

    private Object[] getColumnNames() {
        if (type == TableType.USERS) {
            return new Object[]{ "","ID","Student ID","First Name","Last Name","College","Year",
                                 "Karma Points","Contact Number","Gcash Number","Maya Number",
                                 "Mastercard Card","Visa Number","System Role" };
        } else if (type == TableType.ITEMS) {
            return new Object[]{ "","ID","Item Name","Condition","Category","Stock","Price","Status" };
        } else {
            return new Object[]{ "ID","User ID","Log Type","Description","Date" };
        }
    }

    private void applyColumnWidths() {
        if (type == TableType.USERS) {
            table.setColumnWidth(0,  36);  // checkbox
            table.setColumnWidth(1,  40);  // ID
            table.setColumnWidth(2,  90);  // Student ID
            table.setColumnWidth(5,  70);  // College
            table.setColumnWidth(6,  70);  // Year
            table.setColumnWidth(7,  110); // Karma
            table.setColumnWidth(8,  130); // Contact
            table.setColumnWidth(9,  120); // GCash
            table.setColumnWidth(10, 120); // Maya
            table.setColumnWidth(11, 140); // Mastercard
            table.setColumnWidth(12, 120); // Visa
            table.setColumnWidth(13, 110); // Role
        } else if (type == TableType.ITEMS) {
            table.setColumnWidth(0, 36);   // checkbox
            table.setColumnWidth(1, 55);   // ID
            table.setColumnWidth(3, 110);  // Condition
            table.setColumnWidth(4, 140);  // Category
            table.setColumnWidth(5, 75);   // Stock
            table.setColumnWidth(6, 80);   // Price
            table.setColumnWidth(7, 115);  // Status
        } else {
            table.setColumnWidth(0, 55);
            table.setColumnAlignment(0, SwingConstants.LEFT);
            table.setColumnWidth(1, 90);  table.setColumnWidth(2, 150);
            table.setColumnWidth(4, 160);
            TableColumn descCol = table.getColumnModel().getColumn(3);
            descCol.setPreferredWidth(260); descCol.setMinWidth(150);
        }
    }

  
    private void loadTableData() {
        if (table == null) return;

        String  keyword   = searchField != null ? searchField.getText().trim() : "";
        String  filter    = filterBox   != null ? (String) filterBox.getSelectedItem() : "";
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
            String[] range  = dateFilterPanel != null
                    ? dateFilterPanel.getEffectiveDateRange() : new String[]{null, null};
            String dateFrom = range[0];
            String dateTo   = range[1];

            if      (logType == LogType.USER_LOGS)        data = logService.getUserLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.ITEM_LOGS)        data = logService.getItemLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.TRANSACTION_LOGS) data = logService.getTransactionLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.REPUTATION_LOGS)  data = logService.getReputationLogsFiltered(filter, keyword, dateFrom, dateTo);
            else                                          data = new Object[0][5];
        }
        table.setFullData(data, getColumnNames());
        if (type != TableType.LOGS) {
            resetCheckboxState();
        }
    }

   

    private void updateActionUI() {
        if (btnUpdateOrRetrieve == null || btnArchiveOrDelete == null) return;

        btnUpdateOrRetrieve.setEnabled(true);
        btnArchiveOrDelete.setEnabled(true);

        if (archiveMode.isSelected()) {
            applyButtonStyle(btnUpdateOrRetrieve, "Retrieve Data", "#ffc107");
            applyButtonStyle(btnArchiveOrDelete,  "Delete Data",   "#dc3545");
            if (btnAddAction != null) btnAddAction.setVisible(false);
        } else {
            applyButtonStyle(btnUpdateOrRetrieve, "Update Data",  "#28a745");
            applyButtonStyle(btnArchiveOrDelete,  "Archive Data", "#ffc107");
            if (btnAddAction != null) btnAddAction.setVisible(Permission.canCreate());
        }
    }

    private void applyButtonStyle(CustomButton btn, String text, String hexColor) {
        btn.setText(text);
        btn.setDefaultColor(Color.decode(hexColor));
        btn.setHoverColor(btn.getBackground().darker());
    }

    private void updateSelectedCount() {
        int count = 0;
        for (int r = 0; r < table.getModel().getRowCount(); r++) {
            Object val = table.getModel().getValueAt(r, 0);
            if (Boolean.TRUE.equals(val)) count++;
        }
        selectedCountLabel.setText("Selected: " + count);
        updateActionUI();
    }

    private java.util.List<Integer> getCheckedIds() {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (int r = 0; r < table.getModel().getRowCount(); r++) {
            Object val = table.getModel().getValueAt(r, 0);
            if (Boolean.TRUE.equals(val)) {
                ids.add(Integer.parseInt(table.getModel().getValueAt(r, 1).toString()));
            }
        }
        // Fallback: single-click row selection
        if (ids.isEmpty() && table.getSelectedRow() != -1) {
            ids.add(getSelectedId(table.getSelectedRow()));
        }
        return ids;
    }
    
    private void resetCheckboxState() {
        selectAllBox.setSelected(false);
        for (int r = 0; r < table.getModel().getRowCount(); r++) {
            table.getModel().setValueAt(false, r, 0);
        }
        selectedCountLabel.setText("Selected: 0");
    }

    private void handleAdd() {
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

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one row.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ids.size() > 1) {
            JOptionPane.showMessageDialog(this,
                    "Please select only one row to update.", "Multiple Rows Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = ids.get(0);
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS) {
            UserFormDialog.showUpdate(owner, id, userService, () -> loadTableData());
        } else if (type == TableType.ITEMS) {
            ItemFormDialog.showUpdate(owner, id, itemService, () -> loadTableData());
        }
    }

    private void handleArchive() {
        Permission.require(Permission.canDelete(), "archive records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one row.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String entityType = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to archive " + ids.size() + " " + entityType
                        + (ids.size() > 1 ? "s" : "") + "?",
                "Confirm Archive", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int succeeded = 0, failed = 0;
        int currentUserId = SessionManager.get().getCurrentUserId();

        for (int id : ids) {
            if (type == TableType.USERS && id == currentUserId) { failed++; continue; }
            boolean ok = type == TableType.USERS
                    ? userService.archiveUser(id) : itemService.archiveItem(id);
            if (ok) succeeded++; else failed++;
        }

        loadTableData();
        resetCheckboxState();
        JOptionPane.showMessageDialog(this,
                succeeded + " " + entityType + (succeeded != 1 ? "s" : "")
                        + " archived successfully. " + failed + " failed.",
                succeeded > 0 ? "Archive Complete" : "Archive Failed",
                failed > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleRetrieve() {
        Permission.require(Permission.canUpdate(), "restore records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one row.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String entityType = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore " + ids.size() + " " + entityType
                        + (ids.size() > 1 ? "s" : "") + "?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int succeeded = 0, failed = 0;
        for (int id : ids) {
            boolean ok = type == TableType.USERS
                    ? userService.unarchiveUser(id) : itemService.unarchiveItem(id);
            if (ok) succeeded++; else failed++;
        }

        loadTableData();
        resetCheckboxState();
        JOptionPane.showMessageDialog(this,
                succeeded + " " + entityType + (succeeded != 1 ? "s" : "")
                        + " restored successfully. " + failed + " failed.",
                succeeded > 0 ? "Restore Complete" : "Restore Failed",
                failed > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDelete() {
        Permission.require(Permission.canDelete(), "permanently delete records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one row.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String entityType = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete " + ids.size() + " " + entityType
                        + (ids.size() > 1 ? "s" : "") + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int succeeded = 0, failed = 0;
        for (int id : ids) {
            boolean ok = type == TableType.USERS
                    ? userService.permanentDeleteUser(id) : itemService.permanentDeleteItem(id);
            if (ok) succeeded++; else failed++;
        }

        loadTableData();
        resetCheckboxState();
        JOptionPane.showMessageDialog(this,
                succeeded + " " + entityType + (succeeded != 1 ? "s" : "")
                        + " permanently deleted. " + failed + " failed.",
                succeeded > 0 ? "Delete Complete" : "Delete Failed",
                failed > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
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
        int idCol = (type == TableType.USERS || type == TableType.ITEMS) ? 1 : 0;
        return Integer.parseInt(table.getModel().getValueAt(modelRow, idCol).toString());
    }


}