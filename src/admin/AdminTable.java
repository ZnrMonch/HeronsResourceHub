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

// Admin table panel for Users, Items, and Logs — handles display, filtering, and CRUD actions
public class AdminTable extends CustomPanel {

    private static final long serialVersionUID = 1L;

    private final TableType type;
    private final LogType logType;
    private int hoveredRow = -1;

    private CustomTable table;
    private CustomButton btnAddAction;
    private CustomButton btnUpdateOrRetrieve;
    private CustomButton btnArchiveOrDelete;
    private CustomSearchField searchField;
    private CustomComboBox<String> filterBox;

    private JCheckBox archiveMode      = new JCheckBox("Archive Mode");
    private JCheckBox selectAllBox     = new JCheckBox();
    private CustomLabel selectedCountLabel = new CustomLabel("Selected: 0", 12f, FontStyle.REGULAR);

    private final CustomCheckBox chkRenderer = new CustomCheckBox();
    private final CustomCheckBox chkEditor   = new CustomCheckBox();
    private final CustomCheckBox chkHeader   = new CustomCheckBox();

    private javax.swing.Timer logsRefreshTimer;
    private DateFilterPanel dateFilterPanel;

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

        // Auto-refresh logs every 10 seconds without user intervention
        if (type == TableType.LOGS) {
            logsRefreshTimer = new javax.swing.Timer(10_000, e -> loadTableData());
            logsRefreshTimer.start();
        }
    }

    // Init header with title, search, filter, and action controls
    private CustomPanel initHeader() {
        CustomPanel header = new CustomPanel();
        header.setPadding(10, 15);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        // Resolve panel title based on table type
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
            public void keyReleased(KeyEvent e) { loadTableData(); }
        });
        header.add(Box.createHorizontalStrut(20));
        header.add(searchField);
        header.add(Box.createHorizontalStrut(10));

        // Filter combo box
        header.add(new CustomLabel("Search by:"));
        header.add(Box.createHorizontalStrut(10));
        filterBox = new CustomComboBox<>(getFilterOptions());
        filterBox.setCustomSize(140, 35);
        filterBox.addActionListener(e -> loadTableData());
        header.add(filterBox);
        header.add(Box.createHorizontalStrut(10));

        // Archive mode toggle — not shown for Logs
        if (type != TableType.LOGS) {
            archiveMode.setOpaque(false);
            if (FontLib.POPPINS_REGULAR != null)
                archiveMode.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
            archiveMode.addItemListener(e -> {
                updateActionUI();
                loadTableData();
            });
            header.add(archiveMode);
            header.add(Box.createHorizontalStrut(10));
        }

        // Logs-only: refresh button and date filter
        if (type == TableType.LOGS) {
            header.add(Box.createHorizontalGlue());
            header.add(Box.createHorizontalStrut(10));

            // Manual refresh only exposed to super admins
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

    // Init table with model, renderers, editors, scroll pane, and bottom actions
    private CustomPanel initTable() {
        CustomPanel tableWrapper = new CustomPanel();
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);

        Object[] columnNames = getColumnNames();

        // Table model — checkbox col only editable for Users/Items
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
                if (col == 0) return Boolean.class;
                return Object.class;
            }
        };

        // Apply column widths after each data update
        table.setPostUpdateHook(() -> applyColumnWidths());

        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(0, 0));

        // Track hovered row for highlight effect
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

        // Row click toggles checkbox — skip Logs and non-super-admins
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (type == TableType.LOGS) return;
                if (!Permission.isSuperAdmin()) return;

                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;

                int col = table.columnAtPoint(e.getPoint());
                if (col == 0) return;

                boolean current = Boolean.TRUE.equals(
                    table.getModel().getValueAt(table.convertRowIndexToModel(row), 0));
                table.getModel().setValueAt(!current, table.convertRowIndexToModel(row), 0);
                table.setRowSelectionInterval(row, row);
                updateSelectedCount();
            }
        });

        // Cell renderer — applies row color based on checked/hover/alternating state
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);

                if (FontLib.POPPINS_REGULAR != null)
                    c.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));

                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);

                // Show tooltip only for long cell values
                String text = value != null ? value.toString() : "";
                ((JLabel) c).setToolTipText(text.length() > 15 ? text : null);

                int modelRow = t.convertRowIndexToModel(row);
                boolean checked = false;

                // Only read col 0 as Boolean for Users/Items — not Logs
                if (type != TableType.LOGS) {
                    try {
                        Object val = t.getModel().getValueAt(modelRow, 0);
                        checked = Boolean.TRUE.equals(val);
                    } catch (Exception ignored) {}
                }

                if (checked) {
                    c.setBackground(new Color(210, 230, 255));
                    c.setForeground(Color.BLACK);
                } else if (row == hoveredRow) {
                    c.setBackground(new Color(235, 235, 235));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 252, 254));
                    c.setForeground(Color.BLACK);
                }

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Restore checkbox renderer/editor on col 0 — only for Users/Items
        if (type != TableType.LOGS) {
            table.getColumnModel().getColumn(0).setCellRenderer(table.getDefaultRenderer(Boolean.class));
            table.getColumnModel().getColumn(0).setCellEditor(table.getDefaultEditor(Boolean.class));
        }

        // Header renderer — styled label with bold font
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    t, value, sel, foc, row, col);
                lbl.setText(value != null ? value.toString() : "");
                lbl.setBackground(new Color(210, 210, 210));
                lbl.setForeground(Color.DARK_GRAY);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                if (FontLib.POPPINS_BOLD != null)
                    lbl.setFont(FontLib.POPPINS_BOLD.deriveFont(13f));
                return lbl;
            }
        });

        table.getTableHeader().setBorder(null);
        table.getTableHeader().setBackground(new Color(210, 210, 210));
        table.getTableHeader().setForeground(Color.DARK_GRAY);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 44));
        table.getTableHeader().setReorderingAllowed(false);

        // Update/Archive buttons — only for Users and Items
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
                else handleUpdate();
            });
            btnArchiveOrDelete.addActionListener(e -> {
                if (archiveMode.isSelected()) handleDelete();
                else handleArchive();
            });
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateActionUI();
            });

            // Checkbox column header and editor — only for super admins
            if (Permission.isSuperAdmin()) {
                chkHeader.setHeader(true);
                table.setCheckboxColumn(0, chkRenderer, chkEditor, (tbl, val, sel, foc, row, col) -> {
                    chkHeader.setChecked(selectAllBox.isSelected());
                    return chkHeader;
                });

                chkEditor.addCellEditorListener(new javax.swing.event.CellEditorListener() {
                    public void editingStopped(javax.swing.event.ChangeEvent e) {
                        SwingUtilities.invokeLater(AdminTable.this::updateSelectedCount);
                    }
                    public void editingCanceled(javax.swing.event.ChangeEvent e) {}
                });

                // Header click on col 0 toggles select-all
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
        }

        // Add button — label differs by type
        btnAddAction = new CustomButton("Create " + (type == TableType.USERS ? "User" : "Item"), 8);
        btnAddAction.setFontSize(12f);
        btnAddAction.setPadding(6, 14, 6, 14);
        btnAddAction.setDefaultColor(Color.decode("#0056b3"));
        btnAddAction.setTextColor(Color.WHITE);
        btnAddAction.setHoverColor(Brand.PRIMARY_COLOR);
        btnAddAction.addActionListener(e -> handleAdd());

        loadTableData();

        // Scroll pane — always-on horizontal scroll for Users
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        if (type == TableType.USERS) {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        } else {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        applyCustomScrollBar(scrollPane);

        // Bordered scroll wrapper
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setBackground(Color.WHITE);
        scrollWrapper.setBorder(BorderFactory.createLineBorder(Brand.COLOR_BORDER, 1, true));
        scrollWrapper.add(scrollPane, BorderLayout.CENTER);

        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // Bottom panel with pagination and selected count
        CustomPanel bottomPanel = new CustomPanel(new BorderLayout());
        bottomPanel.add(table.createPaginationPanel(), BorderLayout.CENTER);

        // Selected count label — only for super admins
        if (type != TableType.LOGS && Permission.isSuperAdmin()) {
            selectedCountLabel.setForeground(Color.GRAY);
            if (FontLib.POPPINS_REGULAR != null)
                selectedCountLabel.setFont(FontLib.POPPINS_REGULAR.deriveFont(11f));
            bottomPanel.add(selectedCountLabel, BorderLayout.WEST);
        }

        // Action buttons — visible based on permission
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

    // Open add dialog for Users or Items
    private void handleAdd() {
        Permission.require(Permission.canCreate(), "create records");
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS)
            UserFormDialog.showAdd(owner, userService, () -> loadTableData());
        else if (type == TableType.ITEMS)
            ItemFormDialog.showAdd(owner, itemService, () -> loadTableData());
    }

    // Open update dialog — enforces single-row selection
    private void handleUpdate() {
        Permission.require(Permission.canUpdate(), "update records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one row.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Update dialog only supports a single record at a time
        if (ids.size() > 1) {
            JOptionPane.showMessageDialog(this, "Please select only one row to update.",
                    "Multiple Rows Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = ids.get(0);
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (type == TableType.USERS)
            UserFormDialog.showUpdate(owner, id, userService, () -> loadTableData());
        else if (type == TableType.ITEMS)
            ItemFormDialog.showUpdate(owner, id, itemService, () -> loadTableData());
    }

    // Archive selected records with confirmation
    private void handleArchive() {
        Permission.require(Permission.canDelete(), "archive records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one row.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
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
            // Prevent the logged-in user from archiving their own account
            if (type == TableType.USERS && id == currentUserId) {
                failed++;
                continue;
            }
            boolean ok = type == TableType.USERS
                    ? userService.archiveUser(id)
                    : itemService.archiveItem(id);
            if (ok) succeeded++; else failed++;
        }

        loadTableData();
        resetCheckboxState();
        JOptionPane.showMessageDialog(this,
                succeeded + " " + entityType + (succeeded != 1 ? "s" : "") + " archived successfully. "
                        + failed + " failed.",
                succeeded > 0 ? "Archive Complete" : "Archive Failed",
                failed > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // Restore archived records with confirmation
    private void handleRetrieve() {
        Permission.require(Permission.canUpdate(), "restore records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one row.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
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
                    ? userService.unarchiveUser(id)
                    : itemService.unarchiveItem(id);
            if (ok) succeeded++; else failed++;
        }

        loadTableData();
        resetCheckboxState();
        JOptionPane.showMessageDialog(this,
                succeeded + " " + entityType + (succeeded != 1 ? "s" : "") + " restored successfully. "
                        + failed + " failed.",
                succeeded > 0 ? "Restore Complete" : "Restore Failed",
                failed > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // Permanently delete records with confirmation
    private void handleDelete() {
        Permission.require(Permission.canDelete(), "permanently delete records");

        java.util.List<Integer> ids = getCheckedIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one row.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
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
                    ? userService.permanentDeleteUser(id)
                    : itemService.permanentDeleteItem(id);
            if (ok) succeeded++; else failed++;
        }

        loadTableData();
        resetCheckboxState();
        JOptionPane.showMessageDialog(this,
                succeeded + " " + entityType + (succeeded != 1 ? "s" : "") + " permanently deleted. "
                        + failed + " failed.",
                succeeded > 0 ? "Delete Complete" : "Delete Failed",
                failed > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // Fetch and populate table rows based on type, mode, and search state
    private void loadTableData() {
        if (table == null) return;

        String keyword    = searchField != null ? searchField.getText().trim() : "";
        String filter     = filterBox   != null ? (String) filterBox.getSelectedItem() : "";
        boolean searching = !keyword.isEmpty();
        boolean archived  = archiveMode.isSelected();
        Object[][] data;

        if (type == TableType.USERS) {
            if (archived && searching) data = userService.searchArchivedUsers(filter, keyword);
            else if (archived)         data = userService.getArchivedUsersForTable();
            else if (searching)        data = userService.searchActiveUsers(filter, keyword);
            else                       data = userService.getActiveUsersForTable();

        } else if (type == TableType.ITEMS) {
            if (archived && searching) data = itemService.searchArchivedItems(filter, keyword);
            else if (archived)         data = itemService.getArchivedItemsForTable();
            else if (searching)        data = itemService.searchActiveItems(filter, keyword);
            else                       data = itemService.getActiveItemsForTable();

        } else {
            // Logs — apply date range from filter panel
            String[] range  = dateFilterPanel != null ? dateFilterPanel.getEffectiveDateRange() : new String[]{null, null};
            String dateFrom = range[0];
            String dateTo   = range[1];

            if      (logType == LogType.USER_LOGS)        data = logService.getUserLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.ITEM_LOGS)        data = logService.getItemLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.TRANSACTION_LOGS) data = logService.getTransactionLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.REPUTATION_LOGS)  data = logService.getReputationLogsFiltered(filter, keyword, dateFrom, dateTo);
            else                                          data = new Object[0][5];
        }

        // applyColumnWidths() is triggered automatically via postUpdateHook inside setFullData()
        table.setFullData(data, getColumnNames());
        table.clearSelection();

        if (selectAllBox      != null) selectAllBox.setSelected(false);
        if (selectedCountLabel != null) selectedCountLabel.setText("Selected: 0");
        if (type != TableType.LOGS) resetCheckboxState();
    }

    // Sync button labels and colors to current archive mode state
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

    // Apply label, color, and derived hover color to a button
    private void applyButtonStyle(CustomButton btn, String text, String hexColor) {
        btn.setText(text);
        btn.setDefaultColor(Color.decode(hexColor));
        btn.setHoverColor(btn.getBackground().darker());
    }

    // Count checked rows and refresh related UI
    private void updateSelectedCount() {
        int count = 0;
        for (int r = 0; r < table.getModel().getRowCount(); r++) {
            Object val = table.getModel().getValueAt(r, 0);
            if (Boolean.TRUE.equals(val)) count++;
        }
        selectedCountLabel.setText("Selected: " + count);
        updateActionUI();
        table.repaint();
    }

    // Collect IDs of checked rows — falls back to selected row if none checked
    private java.util.List<Integer> getCheckedIds() {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (int r = 0; r < table.getModel().getRowCount(); r++) {
            Object val = table.getModel().getValueAt(r, 0);
            if (Boolean.TRUE.equals(val))
                ids.add(Integer.parseInt(table.getModel().getValueAt(r, 1).toString()));
        }

        // Fall back to the highlighted row when no checkboxes are ticked
        if (ids.isEmpty() && table.getSelectedRow() != -1)
            ids.add(getSelectedId(table.getSelectedRow()));
        return ids;
    }

    // Clear all checkboxes and reset the selected count label
    private void resetCheckboxState() {
        selectAllBox.setSelected(false);
        for (int r = 0; r < table.getModel().getRowCount(); r++)
            table.getModel().setValueAt(false, r, 0);
        selectedCountLabel.setText("Selected: 0");
    }

    // ID column index differs — Logs has no checkbox col so ID sits at col 0
    private int getSelectedId(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        int idCol = (type == TableType.USERS || type == TableType.ITEMS) ? 1 : 0;
        return Integer.parseInt(table.getModel().getValueAt(modelRow, idCol).toString());
    }

    // Column names differ per table type
    private Object[] getColumnNames() {
        if (type == TableType.USERS) {
            return new Object[]{ "", "ID", "Student ID", "First Name", "Last Name",
                    "College", "Year", "Karma Points", "Contact Number",
                    "Gcash Number", "Maya Number", "Mastercard Card", "Visa Number", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new Object[]{ "", "ID", "Item Name", "Condition", "Category", "Stock", "Price", "Status" };
        } else {
            return new Object[]{ "ID", "User ID", "Log Type", "Description", "Date" };
        }
    }

    // Set fixed or flexible column widths per table type
    private void applyColumnWidths() {
        if (type == TableType.USERS) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            TableColumn chk = table.getColumnModel().getColumn(0);
            chk.setMinWidth(36); chk.setMaxWidth(36); chk.setPreferredWidth(36);

            TableColumn id = table.getColumnModel().getColumn(1);
            id.setPreferredWidth(55); id.setMaxWidth(55);

            table.getColumnModel().getColumn(2).setPreferredWidth(110);
            table.getColumnModel().getColumn(3).setPreferredWidth(160);
            table.getColumnModel().getColumn(4).setPreferredWidth(130);
            table.getColumnModel().getColumn(5).setPreferredWidth(80);
            table.getColumnModel().getColumn(6).setPreferredWidth(100);
            table.getColumnModel().getColumn(7).setPreferredWidth(120);
            table.getColumnModel().getColumn(8).setPreferredWidth(140);
            table.getColumnModel().getColumn(9).setPreferredWidth(130);
            table.getColumnModel().getColumn(10).setPreferredWidth(130);
            table.getColumnModel().getColumn(11).setPreferredWidth(165);
            table.getColumnModel().getColumn(12).setPreferredWidth(165);
            table.getColumnModel().getColumn(13).setPreferredWidth(130);

            table.setFillsViewportHeight(true);

            // Collapse checkbox column for non-super-admins
            if (!Permission.isSuperAdmin()) {
                chk.setMinWidth(0); chk.setMaxWidth(0); chk.setPreferredWidth(0);
                chk.setResizable(false);
            }

        } else if (type == TableType.ITEMS) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            TableColumn chk = table.getColumnModel().getColumn(0);
            chk.setMinWidth(36); chk.setMaxWidth(36); chk.setPreferredWidth(36);

            TableColumn id = table.getColumnModel().getColumn(1);
            id.setMinWidth(110); id.setMaxWidth(180); id.setPreferredWidth(50);

            table.getColumnModel().getColumn(2).setMinWidth(150);
            table.getColumnModel().getColumn(2).setPreferredWidth(280);
            table.getColumnModel().getColumn(3).setMinWidth(90);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setMinWidth(110);
            table.getColumnModel().getColumn(4).setPreferredWidth(160);

            TableColumn stock = table.getColumnModel().getColumn(5);
            stock.setMinWidth(110); stock.setMaxWidth(180); stock.setPreferredWidth(70);

            TableColumn price = table.getColumnModel().getColumn(6);
            price.setMinWidth(110); price.setMaxWidth(180); price.setPreferredWidth(80);

            table.getColumnModel().getColumn(7).setMinWidth(100);
            table.getColumnModel().getColumn(7).setPreferredWidth(140);

            table.setFillsViewportHeight(true);

            // Collapse checkbox column for non-super-admins
            if (!Permission.isSuperAdmin()) {
                chk.setMinWidth(0); chk.setMaxWidth(0); chk.setPreferredWidth(0);
                chk.setResizable(false);
            }

        } else {
            // Logs — description column fills remaining space
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            TableColumn id = table.getColumnModel().getColumn(0);
            id.setMinWidth(110); id.setMaxWidth(180); id.setPreferredWidth(55);

            TableColumn uid = table.getColumnModel().getColumn(1);
            uid.setMinWidth(110); uid.setMaxWidth(180); uid.setPreferredWidth(70);

            TableColumn logType = table.getColumnModel().getColumn(2);
            logType.setMinWidth(110); logType.setMaxWidth(180); logType.setPreferredWidth(150);

            TableColumn desc = table.getColumnModel().getColumn(3);
            desc.setMinWidth(300); desc.setPreferredWidth(480);

            TableColumn date = table.getColumnModel().getColumn(4);
            date.setMinWidth(300); date.setMaxWidth(175); date.setPreferredWidth(480);

            table.setFillsViewportHeight(true);
        }
    }

    // Apply custom slim scroll bar UI to vertical and horizontal bars
    private void applyCustomScrollBar(JScrollPane scrollPane) {
        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        JScrollBar hBar = scrollPane.getHorizontalScrollBar();

        vBar.setPreferredSize(new Dimension(8, 0));
        hBar.setPreferredSize(new Dimension(0, 8));
        vBar.setUnitIncrement(16);

        vBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected JButton createDecreaseButton(int o) { return makeZeroButton(); }
            protected JButton createIncreaseButton(int o) { return makeZeroButton(); }

            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 240, 240));
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2.dispose();
            }

            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isThumbRollover() ? new Color(150, 150, 160) : new Color(190, 190, 200));
                g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8);
                g2.dispose();
            }

            // Zero-size button removes default scroll arrows
            private JButton makeZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });

        hBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected JButton createDecreaseButton(int o) {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
            protected JButton createIncreaseButton(int o) {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }

            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(new Color(240, 240, 240));
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(190, 190, 200));
                g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8);
            }
        });

        vBar.setOpaque(false);
        hBar.setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    // Filter options differ by type — Logs expose extra Item ID filter for some log types
    private String[] getFilterOptions() {
        if (type == TableType.USERS) {
            return new String[]{ "All", "ID", "Student ID", "First Name", "Last Name",
                    "College", "Year Level", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new String[]{ "All", "ID", "Item Name", "Condition", "Category", "Stock", "Price", "Status" };
        } else {
            if (logType == LogType.ITEM_LOGS || logType == LogType.TRANSACTION_LOGS)
                return new String[]{ "All", "ID", "User ID", "Item ID", "Action" };
            else
                return new String[]{ "All", "ID", "User ID", "Action" };
        }
    }

    // Shared factory for styled header buttons
    private CustomButton makeHeaderButton(String text, Color defaultColor, Color hoverColor) {
        CustomButton btn = new CustomButton(text, 8);
        btn.setFontSize(12f);
        btn.setPadding(6, 14, 6, 14);
        btn.setDefaultColor(defaultColor);
        btn.setTextColor(Color.WHITE);
        btn.setHoverColor(hoverColor);
        return btn;
    }

    // runAsync overloads kept for future use — all CRUD currently runs synchronously
    @SuppressWarnings("unused")
    private void runAsync(ArchiveTask task, String successMsg, String failureMsg) {
        runAsync(task, successMsg, failureMsg, false);
    }

    private void runAsync(ArchiveTask task, String successMsg, String failureMsg, boolean checkUserServiceError) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception { return task.run(); }

            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        loadTableData();
                        JOptionPane.showMessageDialog(AdminTable.this, successMsg,
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        String msg = failureMsg;
                        // Surface the service-level error when available
                        if (checkUserServiceError) {
                            String svcError = userService.getLastAddError();
                            if (svcError != null && !svcError.isEmpty()) msg = svcError;
                        }
                        JOptionPane.showMessageDialog(AdminTable.this, msg,
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                            "Error: An unexpected error occurred! " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Task wrapper used by runAsync to dispatch archive/restore/delete operations
    private class ArchiveTask {
        private final int id;
        private final RecordAction action;

        public ArchiveTask(int id, RecordAction action) {
            this.id     = id;
            this.action = action;
        }

        public boolean run() {
            switch (action) {
                case ARCHIVE_USER:  return userService.archiveUser(id);
                case RESTORE_USER:  return userService.unarchiveUser(id);
                case DELETE_USER:   return userService.permanentDeleteUser(id);
                case ARCHIVE_ITEM:  return itemService.archiveItem(id);
                case RESTORE_ITEM:  return itemService.unarchiveItem(id);
                case DELETE_ITEM:   return itemService.permanentDeleteItem(id);
                default:            return false;
            }
        }
    }
}