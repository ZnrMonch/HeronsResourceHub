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
    private final LogType logType;

    private int hoveredRow = -1;

    private CustomTable table;
    private CustomButton btnAddAction;
    private CustomButton btnUpdateOrRetrieve;   // renamed from btnPrimaryAction
    private CustomButton btnArchiveOrDelete;    // renamed from btnSecondaryAction
    private CustomSearchField searchField;
    private CustomComboBox<String> filterBox;
    private JCheckBox archiveMode = new JCheckBox("Archive Mode");

    private javax.swing.Timer logsRefreshTimer;
    private DateFilterPanel dateFilterPanel;

    // Services
    private final AdminUsersServices userService = new AdminUsersServices();
    private final AdminItemsServices itemService = new AdminItemsServices();
    private final AdminLogsServices  logService  = new AdminLogsServices();


    // CONSTRUCTORS
    public AdminTable(TableType type) {
        this(type, LogType.NONE);
    }

    public AdminTable(TableType type, LogType logType) {
        this.type    = type;
        this.logType = logType;
        setLayout(new BorderLayout());
        add(initHeader(), BorderLayout.NORTH);
        add(initTable(),  BorderLayout.CENTER);

        // Auto-refresh every 30 seconds for logs
        if (type == TableType.LOGS) {
            logsRefreshTimer = new javax.swing.Timer(30_000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadTableData();
                }
            });
            logsRefreshTimer.start();
        }
    }


    // HEADER
    private CustomPanel initHeader() {
        CustomPanel header = new CustomPanel();
        header.setPadding(20, 15);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        // Title label
        String title;
        switch (type) {
            case USERS: title = "User Management"; break;
            case ITEMS: title = "Item Management"; break;
            case LOGS:  title = "Logs Management"; break;
            default:    title = "Management";      break;
        }

        header.add(new CustomLabel(title, 18f, FontStyle.BOLD));
        header.add(Box.createHorizontalGlue());

        // Search field
        searchField = new CustomSearchField("Search...", 20, 10);
        searchField.setCustomSize(180, 35);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadTableData();
            }
        });
        header.add(Box.createHorizontalStrut(20));
        header.add(searchField);
        header.add(Box.createHorizontalStrut(10));

        // Filter dropdown
        header.add(new CustomLabel("Search by:"));
        header.add(Box.createHorizontalStrut(10));
        filterBox = new CustomComboBox<>(getFilterOptions());
        filterBox.setCustomSize(140, 35);
        filterBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadTableData();
            }
        });
        header.add(filterBox);
        header.add(Box.createHorizontalStrut(10));

        // Archive mode checkbox (users and items only)
        if (type != TableType.LOGS) {
            archiveMode.setOpaque(false);
            if (FontLib.POPPINS_REGULAR != null) {
                archiveMode.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
            }
            archiveMode.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    updateActionUI();
                    loadTableData();
                }
            });
            header.add(archiveMode);
            header.add(Box.createHorizontalStrut(10));
        }

        // Refresh button and date filter panel (logs only)
        if (type == TableType.LOGS) {
            header.add(Box.createHorizontalGlue());
            header.add(Box.createHorizontalStrut(10));

            CustomButton btnRefresh = makeHeaderButton("Refresh", Brand.PRIMARY_COLOR, Brand.GREEN);
            btnRefresh.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadTableData();
                }
            });
            header.add(btnRefresh);
            header.add(Box.createHorizontalStrut(10));

            dateFilterPanel = new DateFilterPanel(new Runnable() {
                public void run() {
                    loadTableData();
                }
            });
            header.add(dateFilterPanel);
        }

        return header;
    }

    // Returns the correct filter options depending on the table type
    private String[] getFilterOptions() {
        if (type == TableType.USERS) {
            return new String[] { "ID", "Student ID", "First Name", "Last Name",
                                  "College", "Year Level", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new String[] { "ID", "Item Name", "Condition", "Category",
                                  "Stock", "Price", "Status" };
        } else {
            if (logType == LogType.ITEM_LOGS || logType == LogType.TRANSACTION_LOGS) {
                return new String[] { "ID", "User ID", "Item ID", "Action" };
            } else {
                return new String[] { "ID", "User ID", "Action" };
            }
        }
    }

    // Creates a small styled button for the header area
    private CustomButton makeHeaderButton(String text, Color defaultColor, Color hoverColor) {
        CustomButton btn = new CustomButton(text, 8);
        btn.setFontSize(12f);
        btn.setPadding(6, 14, 6, 14);
        btn.setDefaultColor(defaultColor);
        btn.setTextColor(Color.WHITE);
        btn.setHoverColor(hoverColor);
        return btn;
    }


    // TABLE
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

        // Highlight row when mouse hovers over it
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        // Custom header appearance
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setText(value != null ? value.toString() : "");
                setBackground(Color.LIGHT_GRAY);
                setForeground(Color.DARK_GRAY);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(80, 85, 90)),
                    BorderFactory.createEmptyBorder(0, 0, 0, 10)));
                setHorizontalAlignment(SwingConstants.LEFT);
                if (FontLib.POPPINS_BOLD != null) {
                    setFont(FontLib.POPPINS_BOLD.deriveFont(13f));
                }
                return this;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Action buttons for users and items (not logs)
        if (type != TableType.LOGS) {
            btnUpdateOrRetrieve = new CustomButton("Update Data",  8);
            btnArchiveOrDelete  = new CustomButton("Archive Data", 8);
            btnUpdateOrRetrieve.setFontSize(12f);
            btnArchiveOrDelete.setFontSize(12f);
            btnUpdateOrRetrieve.setPadding(6, 14, 6, 14);
            btnArchiveOrDelete.setPadding(6, 14, 6, 14);
            updateActionUI();

            btnUpdateOrRetrieve.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (archiveMode.isSelected()) {
                        handleRetrieve();
                    } else {
                        handleUpdate();
                    }
                }
            });
            btnArchiveOrDelete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (archiveMode.isSelected()) {
                        handleDelete();
                    } else {
                        handleArchive();
                    }
                }
            });
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateActionUI();
            });
        }

        btnAddAction = new CustomButton("Add " + (type == TableType.USERS ? "User" : "Item"), 8);
        btnAddAction.setFontSize(12f);
        btnAddAction.setPadding(6, 14, 6, 14);
        btnAddAction.setDefaultColor(Color.decode("#007bff"));
        btnAddAction.setTextColor(Color.WHITE);
        btnAddAction.setHoverColor(Color.decode("#0056b3"));
        btnAddAction.setEnabled(true);

        btnAddAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleAdd();
            }
        });

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

        if (type != TableType.LOGS) {
            CustomPanel actionsPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            actionsPanel.add(btnAddAction);
            actionsPanel.add(btnUpdateOrRetrieve);
            actionsPanel.add(btnArchiveOrDelete);
            bottomPanel.add(actionsPanel, BorderLayout.EAST);
        }

        tableWrapper.add(scrollWrapper, BorderLayout.CENTER);
        tableWrapper.add(bottomPanel,   BorderLayout.SOUTH);
        return tableWrapper;
    }

    // Returns the column headers for the table based on type
    private Object[] getColumnNames() {
        if (type == TableType.USERS) {
            return new Object[] { "ID", "Student ID", "First Name", "Last Name",
                                  "College", "Year", "Karma Points", "Contact Number",
                                  "Gcash Number", "Maya Number", "Mastercard Card",
                                  "Visa Number", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new Object[] { "ID", "Item Name", "Condition", "Category",
                                  "Stock", "Price", "Status" };
        } else {
            return new Object[] { "ID", "User ID", "Log Type", "Description", "Date" };
        }
    }

    // Sets column widths and text alignment for each table type
    private void applyColumnWidths() {
        table.setColumnWidth(0, 55);
        table.setColumnAlignment(0, SwingConstants.LEFT);

        if (type == TableType.USERS) {
            table.setColumnWidth(0,  40);  table.setColumnAlignment(0,  SwingConstants.LEFT);
            table.setColumnWidth(1,  90);  table.setColumnAlignment(1,  SwingConstants.LEFT);
            table.setColumnWidth(4,  70);
            table.setColumnWidth(5,  70);  table.setColumnAlignment(5,  SwingConstants.LEFT);
            table.setColumnWidth(6,  110); table.setColumnAlignment(6,  SwingConstants.LEFT);
            table.setColumnWidth(7,  130); table.setColumnAlignment(7,  SwingConstants.LEFT);
            table.setColumnWidth(8,  120); table.setColumnAlignment(8,  SwingConstants.LEFT);
            table.setColumnWidth(9,  110); table.setColumnAlignment(9,  SwingConstants.LEFT);
            table.setColumnWidth(10, 130); table.setColumnAlignment(10, SwingConstants.LEFT);
            table.setColumnWidth(11, 110); table.setColumnAlignment(11, SwingConstants.LEFT);
            table.setColumnWidth(12, 100); table.setColumnAlignment(12, SwingConstants.LEFT);

        } else if (type == TableType.ITEMS) {
            table.setColumnWidth(2, 110); table.setColumnAlignment(2, SwingConstants.LEFT);
            table.setColumnWidth(3, 140); table.setColumnAlignment(3, SwingConstants.LEFT);
            table.setColumnWidth(4, 75);  table.setColumnAlignment(4, SwingConstants.LEFT);
            table.setColumnWidth(5, 80);  table.setColumnAlignment(5, SwingConstants.LEFT);
            table.setColumnWidth(6, 115); table.setColumnAlignment(6, SwingConstants.LEFT);

        } else {
            table.setColumnWidth(1, 90);  table.setColumnAlignment(1, SwingConstants.LEFT);
            table.setColumnWidth(2, 150); table.setColumnAlignment(2, SwingConstants.LEFT);
            table.setColumnWidth(4, 160); table.setColumnAlignment(4, SwingConstants.LEFT);
            TableColumn descCol = table.getColumnModel().getColumn(3);
            descCol.setPreferredWidth(260);
            descCol.setMinWidth(150);
        }
    }


    // DATA LOADING
    private void loadTableData() {
        if (table == null) return;

        String  keyword   = searchField != null ? searchField.getText().trim() : "";
        String  filter    = filterBox   != null ? (String) filterBox.getSelectedItem() : "";
        boolean searching = !keyword.isEmpty();
        boolean archived  = archiveMode.isSelected();

        Object[][] data;

        if (type == TableType.USERS) {
            if (archived && searching) {
                data = userService.searchArchivedUsers(filter, keyword);
            } else if (archived) {
                data = userService.getArchivedUsersForTable();
            } else if (searching) {
                data = userService.searchActiveUsers(filter, keyword);
            } else {
                data = userService.getActiveUsersForTable();
            }

        } else if (type == TableType.ITEMS) {
            if (archived && searching) {
                data = itemService.searchArchivedItems(filter, keyword);
            } else if (archived) {
                data = itemService.getArchivedItemsForTable();
            } else if (searching) {
                data = itemService.searchActiveItems(filter, keyword);
            } else {
                data = itemService.getActiveItemsForTable();
            }

        } else {
            // Logs — delegate date range entirely to DateFilterPanel
            String[] range  = dateFilterPanel != null
                                  ? dateFilterPanel.getEffectiveDateRange()
                                  : new String[] { null, null };
            String dateFrom = range[0];
            String dateTo   = range[1];

            if (logType == LogType.USER_LOGS) {
                data = logService.getUserLogsFiltered(filter, keyword, dateFrom, dateTo);
            } else if (logType == LogType.ITEM_LOGS) {
                data = logService.getItemLogsFiltered(filter, keyword, dateFrom, dateTo);
            } else if (logType == LogType.TRANSACTION_LOGS) {
                data = logService.getTransactionLogsFiltered(filter, keyword, dateFrom, dateTo);
            } else if (logType == LogType.REPUTATION_LOGS) {
                data = logService.getReputationLogsFiltered(filter, keyword, dateFrom, dateTo);
            } else {
                data = new Object[0][5];
            }
        }

        table.setFullData(data, getColumnNames());
    }


    // ACTION BUTTONS UI
    private void updateActionUI() {
        if (btnUpdateOrRetrieve == null || btnArchiveOrDelete == null) return;

        boolean hasSelection = table != null && table.getSelectedRow() != -1;
        btnUpdateOrRetrieve.setEnabled(hasSelection);
        btnArchiveOrDelete.setEnabled(hasSelection);

        if (archiveMode.isSelected()) {
            applyButtonStyle(btnUpdateOrRetrieve, "Retrieve Data", "#ffc107");
            applyButtonStyle(btnArchiveOrDelete,  "Delete Data",   "#dc3545");
            // Hide Add button in archive mode — adding to archive makes no sense
            if (btnAddAction != null) btnAddAction.setVisible(false);
        } else {
            applyButtonStyle(btnUpdateOrRetrieve, "Update Data",  "#28a745");
            applyButtonStyle(btnArchiveOrDelete,  "Archive Data", "#ffc107");
            if (btnAddAction != null) btnAddAction.setVisible(true);
        }
    }

    private void handleAdd() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS) {
            UserFormDialog.showAdd(owner, userService, new Runnable() {
                public void run() { loadTableData(); }
            });
        } else if (type == TableType.ITEMS) {
            ItemFormDialog.showAdd(owner, itemService, new Runnable() {
                public void run() { loadTableData(); }
            });
        }
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS) {
            UserFormDialog.showUpdate(owner, id, userService, new Runnable() {
                public void run() { loadTableData(); }
            });
        } else if (type == TableType.ITEMS) {
            ItemFormDialog.showUpdate(owner, id, itemService, new Runnable() {
                public void run() { loadTableData(); }
            });
        }
    }

    private void applyButtonStyle(CustomButton btn, String text, String hexColor) {
        btn.setText(text);
        btn.setDefaultColor(Color.decode(hexColor));
        btn.setHoverColor(btn.getBackground().darker());
    }

    private void handleArchive() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int id = getSelectedId(row);

        if (type == TableType.USERS) {
            int currentUserId = SessionManager.get().getCurrentUserId();
            if (id == currentUserId) {
                JOptionPane.showMessageDialog(this,
                    "You cannot archive your own account.",
                    "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String entityType = type == TableType.USERS ? "user" : "item";
        int choice = JOptionPane.showConfirmDialog(this,
            "Archive " + entityType + " ID " + id + "?",
            "Confirm Archive", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String successMsg = entityType + " ID " + id + " archived successfully.";
        String failureMsg = "Archive failed for " + entityType + " ID " + id + ".\n\n"
            + "Possible reasons:\n"
            + "  • Account has admin or super_admin role\n"
            + "  • Database error or FK constraint\n"
            + "  • Archive table not found";

        RecordAction action = type == TableType.USERS
            ? RecordAction.ARCHIVE_USER
            : RecordAction.ARCHIVE_ITEM;
        runAsync(new ArchiveTask(id, action), successMsg, failureMsg);
    }

    private void handleRetrieve() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        String entityType = type == TableType.USERS ? "user" : "item";
        int choice = JOptionPane.showConfirmDialog(this,
            "Restore " + entityType + " ID " + id + "?",
            "Confirm Restore", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String successMsg = entityType + " ID " + id + " restored successfully.";
        String failureMsg = "Restore failed for " + entityType + " ID " + id
            + ".\nThe record may not exist in the archive.";

        RecordAction action = type == TableType.USERS
            ? RecordAction.RESTORE_USER
            : RecordAction.RESTORE_ITEM;
        runAsync(new ArchiveTask(id, action), successMsg, failureMsg);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        String entityType = type == TableType.USERS ? "user" : "item";
        int choice = JOptionPane.showConfirmDialog(this,
            "PERMANENTLY delete " + entityType + " ID " + id + "?\n\n"
            + "This will remove the record from the archive forever.\n"
            + "This action CANNOT be undone.",
            "Confirm Permanent Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        String successMsg = entityType + " ID " + id + " permanently deleted.";
        String failureMsg = "Permanent delete failed for " + entityType + " ID " + id
            + ".\nThe record may not exist in the archive.";

        RecordAction action = type == TableType.USERS
            ? RecordAction.DELETE_USER
            : RecordAction.DELETE_ITEM;
        runAsync(new ArchiveTask(id, action), successMsg, failureMsg);
    }

    private void runAsync(ArchiveTask task, String successMsg, String failureMsg) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                return task.run();
            }
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        loadTableData();
                        JOptionPane.showMessageDialog(AdminTable.this,
                            successMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminTable.this,
                            failureMsg, "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Uses ArchiveAction enum instead of raw strings — typos now cause compile errors
    private class ArchiveTask {
        private final int           id;
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

    // Gets the ID from column 0 of the selected row
    private int getSelectedId(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        return Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
    }
}