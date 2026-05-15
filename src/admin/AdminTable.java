package admin;

import java.awt.*;
import java.awt.event.*;
import java.time.YearMonth;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import components.*;
import database.UMak;
import utils.*;
import admin.models.*;
import admin.services.*;
import enums.*;


public class AdminTable extends CustomPanel {
    private static final long serialVersionUID = 1L;

    private final TableType type;
    private final LogType logType;

    private JCheckBox archiveMode = new JCheckBox("Archive Mode");
    private CustomTable table;
    private CustomButton btnPrimaryAction;
    private CustomButton btnSecondaryAction;
    private CustomSearchField searchField;
    private CustomComboBox<String> filterBox;

    private CustomComboBox<String> yearBox;
    private CustomComboBox<String> monthBox;
    private CustomComboBox<String> dayBox;
    private CustomButton btnDateRange;
    private CustomButton btnClearDates;

    private String activeDateFrom = null;
    private String activeDateTo   = null;

    private javax.swing.Timer logsRefreshTimer;

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
        add(initTable(), BorderLayout.CENTER);

      
        if (type == TableType.LOGS) {
            logsRefreshTimer = new javax.swing.Timer(30_000, e -> loadTableData());
            logsRefreshTimer.start();
        }
    }
    
    private CustomPanel initHeader() {
        CustomPanel header = new CustomPanel();
        header.setPadding(20, 15);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        String titleText = "Management";
        if      (type == TableType.USERS) titleText = "User Management";
        else if (type == TableType.ITEMS) titleText = "Item Management";
        else if (type == TableType.LOGS)  titleText = "Logs Management";
        header.add(new CustomLabel(titleText, 18f, FontStyle.BOLD));
        header.add(Box.createHorizontalGlue());

        searchField = new CustomSearchField("Search...", 25, 10);
        searchField.setCustomSize(200, 35);
        header.add(searchField);
        header.add(Box.createHorizontalStrut(10));

        header.add(new CustomLabel("Search by:"));
        header.add(Box.createHorizontalStrut(10));

        filterBox = new CustomComboBox<>(getFilterOptions());
        filterBox.setCustomSize(160, 35);
        header.add(filterBox);
        header.add(Box.createHorizontalStrut(10));

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadTableData();
            }
        });
        filterBox.addActionListener(e -> loadTableData());

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

        if (type == TableType.LOGS) {
            header.add(Box.createHorizontalGlue());
            header.add(Box.createHorizontalStrut(10));

          
            CustomButton btnRefresh = new CustomButton("Refresh", 8);
            btnRefresh.setFontSize(12f);
            btnRefresh.setPadding(6, 10, 6, 10);
            btnRefresh.setDefaultColor(Brand.PRIMARY_COLOR);
            btnRefresh.setTextColor(Color.WHITE);
            btnRefresh.setHoverColor(Brand.GREEN);
            btnRefresh.addActionListener(e -> loadTableData());
            header.add(btnRefresh);
            header.add(Box.createHorizontalStrut(10));

            header.add(buildDateFilterPanel());
        }

        return header;
    }

    private String[] getFilterOptions() {
        if (type == TableType.USERS) {
            return new String[]{ "ID", "Student ID", "First Name", "Last Name",
                                 "College", "Year Level", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new String[]{ "ID", "Item Name", "Condition", "Category",
                                 "Stock", "Price", "Status" };
        } else {
            if (logType == LogType.ITEM_LOGS || logType == LogType.TRANSACTION_LOGS)
                return new String[]{ "ID", "User ID", "Item ID", "Action" };
            return new String[]{ "ID", "User ID", "Action" };
        }
    }

    private JPanel buildDateFilterPanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        datePanel.setOpaque(false);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years  = new String[currentYear - 2019 + 2];
        years[0] = "All Years";
        for (int y = currentYear, i = 1; y >= 2020; y--, i++)
            years[i] = String.valueOf(y);
        yearBox = new CustomComboBox<>(years);
        yearBox.setCustomSize(110, 35);

        String[] months = { "All Months", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        monthBox = new CustomComboBox<>(months);
        monthBox.setCustomSize(115, 35);
        monthBox.setEnabled(false);

        String[] days = buildDayOptions(31);
        dayBox = new CustomComboBox<>(days);
        dayBox.setCustomSize(100, 35);
        dayBox.setEnabled(false);

        yearBox.addActionListener(e -> {
            String sel      = (String) yearBox.getSelectedItem();
            boolean chosen  = sel != null && !sel.equals("All Years");
            monthBox.setEnabled(chosen);
            if (!chosen) {
                monthBox.setSelectedIndex(0);
                dayBox.setEnabled(false);
                dayBox.setSelectedIndex(0);
            }
            loadTableData();
        });

        monthBox.addActionListener(e -> {
            String selMonth = (String) monthBox.getSelectedItem();
            String selYear  = (String) yearBox.getSelectedItem();
            boolean chosen  = selMonth != null && !selMonth.equals("All Months");
            if (chosen && selYear != null && !selYear.equals("All Years")) {
                int maxDay = getDaysInMonth(selYear, selMonth);
                updateDayCombo(maxDay);
                dayBox.setEnabled(true);
            } else {
                dayBox.setEnabled(false);
                dayBox.setSelectedIndex(0);
            }
            loadTableData();
        });

        dayBox.addActionListener(e -> loadTableData());

        btnDateRange = new CustomButton("Date Range", 8);
        btnDateRange.setFontSize(12f);
        btnDateRange.setPadding(6, 10, 6, 10);
        btnDateRange.setDefaultColor(Color.decode("#ffc107"));
        btnDateRange.setTextColor(Color.WHITE);
        btnDateRange.addActionListener(e -> openDateRangeDialog());

        btnClearDates = new CustomButton("Clear", 8);
        btnClearDates.setFontSize(12f);
        btnClearDates.setPadding(6, 10, 6, 10);
        btnClearDates.setDefaultColor(Color.GRAY);
        btnClearDates.setTextColor(Color.WHITE);
        btnClearDates.addActionListener(e -> clearAllDateFilters());

        datePanel.add(new CustomLabel("Year:"));
        datePanel.add(yearBox);
        datePanel.add(new CustomLabel("Month:"));
        datePanel.add(monthBox);
        datePanel.add(new CustomLabel("Day:"));
        datePanel.add(dayBox);
        datePanel.add(Box.createHorizontalStrut(4));
        datePanel.add(btnDateRange);
        datePanel.add(btnClearDates);

        return datePanel;
    }

    private String[] buildDayOptions(int maxDay) {
        String[] days = new String[maxDay + 1];
        days[0] = "All Days";
        for (int d = 1; d <= maxDay; d++)
            days[d] = String.valueOf(d);
        return days;
    }

    private void updateDayCombo(int maxDay) {
        String currentSel = (String) dayBox.getSelectedItem();
        dayBox.removeAllItems();
        dayBox.addItem("All Days");
        for (int d = 1; d <= maxDay; d++) dayBox.addItem(String.valueOf(d));
        if (currentSel != null && !currentSel.equals("All Days")) {
            try {
                int prev = Integer.parseInt(currentSel);
                if (prev <= maxDay) dayBox.setSelectedItem(currentSel);
                else                dayBox.setSelectedIndex(0);
            } catch (NumberFormatException ignored) {
                dayBox.setSelectedIndex(0);
            }
        }
    }

    private int getDaysInMonth(String yearStr, String monthStr) {
        try {
            int year  = Integer.parseInt(yearStr);
            int month = monthNameToNumber(monthStr);
            return YearMonth.of(year, month).lengthOfMonth();
        } catch (Exception e) {
            return 31;
        }
    }

    private int monthNameToNumber(String name) {
        String[] names = { "Jan","Feb","Mar","Apr","May","Jun",
                           "Jul","Aug","Sep","Oct","Nov","Dec" };
        for (int i = 0; i < names.length; i++)
            if (names[i].equals(name)) return i + 1;
        return 1;
    }

    private void openDateRangeDialog() {
        Window  owner  = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Select Date Range",
                                     Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout());

        CustomPanel content = new CustomPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 25, 10, 25));

        SpinnerDateModel fromModel = new SpinnerDateModel();
        CustomSpinner    fromSpinner = new CustomSpinner(fromModel);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        styleSpinner(fromSpinner);

        SpinnerDateModel toModel = new SpinnerDateModel();
        CustomSpinner    toSpinner = new CustomSpinner(toModel);
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));
        styleSpinner(toSpinner);

        if (activeDateFrom != null) {
            try { fromModel.setValue(java.sql.Date.valueOf(activeDateFrom)); }
            catch (Exception ignored) {}
        }
        if (activeDateTo != null) {
            try { toModel.setValue(java.sql.Date.valueOf(activeDateTo)); }
            catch (Exception ignored) {}
        }

        content.add(createSpinnerRow("From:", fromSpinner));
        content.add(Box.createVerticalStrut(12));
        content.add(createSpinnerRow("To:  ", toSpinner));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        btnRow.setOpaque(false);

        CustomButton clearBtn = new CustomButton("Clear Range", 8);
        clearBtn.setPadding(5, 12, 5, 12);
        clearBtn.setDefaultColor(Color.GRAY);
        clearBtn.setTextColor(Color.WHITE);
        clearBtn.addActionListener(e -> {
            activeDateFrom = null;
            activeDateTo   = null;
            updateDateRangeButtonLabel();
            loadTableData();
            dialog.dispose();
        });

        CustomButton applyBtn = new CustomButton("Apply", 8);
        applyBtn.setPadding(5, 12, 5, 12);
        applyBtn.setDefaultColor(Color.decode("#28a745"));
        applyBtn.setTextColor(Color.WHITE);
        applyBtn.addActionListener(e -> {
            java.util.Date from = (java.util.Date) fromSpinner.getValue();
            java.util.Date to   = (java.util.Date) toSpinner.getValue();
            if (from.after(to)) {
                JOptionPane.showMessageDialog(dialog,
                    "Error! 'From' date cannot be after 'To' date.",
                    "Invalid Range", JOptionPane.ERROR_MESSAGE);
                return;
            }
            activeDateFrom = new java.sql.Date(from.getTime()).toString();
            activeDateTo   = new java.sql.Date(to.getTime()).toString();
            updateDateRangeButtonLabel();
            loadTableData();
            dialog.dispose();
        });

        btnRow.add(clearBtn);
        btnRow.add(applyBtn);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnRow,  BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createSpinnerRow(String label, JSpinner spinner) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(45, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(spinner, BorderLayout.CENTER);
        return row;
    }

    private void styleSpinner(JSpinner spinner) {
        if (FontLib.POPPINS_REGULAR != null)
            spinner.setFont(FontLib.POPPINS_REGULAR.deriveFont(13f));
        spinner.setPreferredSize(new Dimension(200, 32));
    }

    private void updateDateRangeButtonLabel() {
        if (btnDateRange == null) return;
        if (activeDateFrom != null && activeDateTo != null) {
            btnDateRange.setText(activeDateFrom + " — " + activeDateTo);
            btnDateRange.setDefaultColor(Color.decode("#28a745"));
        } else {
            btnDateRange.setText("Date Range");
            btnDateRange.setDefaultColor(Color.decode("#ffc107"));
        }
        btnDateRange.repaint();
    }

    private void clearAllDateFilters() {
        activeDateFrom = null;
        activeDateTo   = null;
        if (yearBox  != null) yearBox.setSelectedIndex(0);
        if (monthBox != null) { monthBox.setSelectedIndex(0); monthBox.setEnabled(false); }
        if (dayBox   != null) { dayBox.setSelectedIndex(0);   dayBox.setEnabled(false);   }
        updateDateRangeButtonLabel();
        loadTableData();
    }

    private CustomPanel initTable() {
        CustomPanel tableWrapper = new CustomPanel();
        tableWrapper.setLayout(new BorderLayout());

        Object[] columnNames = getColumnNames();
        table = new CustomTable(new Object[0][columnNames.length], columnNames);

        if (type != TableType.LOGS) {
            btnPrimaryAction   = new CustomButton("Update Data",  8);
            btnSecondaryAction = new CustomButton("Archive Data", 8);
            btnPrimaryAction.setFontSize(12f);
            btnSecondaryAction.setFontSize(12f);
            btnPrimaryAction.setPadding(6, 12, 6, 12);
            btnSecondaryAction.setPadding(6, 12, 6, 12);
            updateActionUI();

            btnPrimaryAction.addActionListener(e -> {
                if (archiveMode.isSelected()) handleRetrieve();
                else                          handleUpdate();
            });
            btnSecondaryAction.addActionListener(e -> {
                if (archiveMode.isSelected()) handleDelete();
                else                          handleArchive();
            });
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateActionUI();
            });
        }

        loadTableData();

        table.setRowHeight(30);
        table.setGridLines(true, false);
        table.setHeaderCustomization(FontLib.POPPINS_BOLD.deriveFont(14f),
                                     Color.BLACK, new Color(240, 240, 240));
        table.setBodyCustomization(FontLib.POPPINS_REGULAR.deriveFont(12f),
                                   Color.BLACK, Color.WHITE);
        applyColumnWidths();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        CustomPanel bottomPanel = new CustomPanel(new BorderLayout());
        bottomPanel.add(table.createPaginationPanel(), BorderLayout.CENTER);

        if (type != TableType.LOGS) {
            CustomPanel actionsPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            actionsPanel.add(btnPrimaryAction);
            actionsPanel.add(btnSecondaryAction);
            bottomPanel.add(actionsPanel, BorderLayout.EAST);
        }

        tableWrapper.add(scrollPane,   BorderLayout.CENTER);
        tableWrapper.add(bottomPanel,  BorderLayout.SOUTH);

        return tableWrapper;
    }

    private Object[] getColumnNames() {
        if (type == TableType.USERS) {
            return new Object[]{ "ID", "Student ID", "First Name", "Last Name",
                                 "College", "Year", "Karma Points", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new Object[]{ "ID", "Item Name", "Condition", "Category",
                                 "Stock", "Price", "Status" };
        } else {
            return new Object[]{ "ID", "User ID", "Log Type", "Description", "Date" };
        }
    }

    private void applyColumnWidths() {
        table.setColumnWidth(0, 50);
        table.setColumnAlignment(0, SwingConstants.CENTER);

        if (type == TableType.USERS) {
            table.setColumnWidth(1, 180);
            table.setColumnAlignment(1, SwingConstants.CENTER);
            table.setColumnWidth(4, 180);
            table.setColumnAlignment(4, SwingConstants.CENTER);
            table.setColumnWidth(5, 80);
            table.setColumnAlignment(5, SwingConstants.CENTER);
            table.setColumnWidth(6, 120);
            table.setColumnAlignment(6, SwingConstants.CENTER);
            table.setColumnWidth(7, 140);
            table.setColumnAlignment(7, SwingConstants.CENTER);
        } else if (type == TableType.ITEMS) {
            table.setColumnWidth(2, 120);
            table.setColumnAlignment(2, SwingConstants.CENTER);
            table.setColumnWidth(3, 150);
            table.setColumnAlignment(3, SwingConstants.CENTER);
            table.setColumnWidth(4, 80);
            table.setColumnAlignment(4, SwingConstants.CENTER);
            table.setColumnWidth(5, 80);
            table.setColumnAlignment(5, SwingConstants.CENTER);
            table.setColumnWidth(6, 120);
            table.setColumnAlignment(6, SwingConstants.CENTER);
        } else {
            table.setColumnWidth(1, 140);
            table.setColumnAlignment(1, SwingConstants.CENTER);
            table.setColumnWidth(2, 180);
            table.setColumnAlignment(3, SwingConstants.CENTER);
            table.setColumnWidth(4, 180);
            table.setColumnAlignment(4, SwingConstants.CENTER);

            TableColumn descCol = table.getColumnModel().getColumn(3);
            descCol.setPreferredWidth(230);
            descCol.setMinWidth(150);
            descCol.setMaxWidth(400);
        }
    }

    private void loadTableData() {
        if (table == null) return;

        String  keyword   = (searchField != null) ? searchField.getText().trim() : "";
        String  filter    = (filterBox   != null) ? (String) filterBox.getSelectedItem() : "";
        boolean searching = !keyword.isEmpty();

        Object[] columnNames = getColumnNames();
        Object[][] data;

        if (type == TableType.USERS) {
            if (archiveMode.isSelected()) {
                data = searching
                    ? userService.searchArchivedUsers(filter, keyword)
                    : userService.getArchivedUsersForTable();
            } else {
                data = searching
                    ? userService.searchActiveUsers(filter, keyword)
                    : userService.getActiveUsersForTable();
            }

        } else if (type == TableType.ITEMS) {
            if (archiveMode.isSelected()) {
                data = searching
                    ? itemService.searchArchivedItems(filter, keyword)
                    : itemService.getArchivedItemsForTable();
            } else {
                data = searching
                    ? itemService.searchActiveItems(filter, keyword)
                    : itemService.getActiveItemsForTable();
            }

        } else {
            String[] range    = resolveEffectiveDateRange();
            String   dateFrom = range[0];
            String   dateTo   = range[1];

            if      (logType == LogType.USER_LOGS)        data = logService.getUserLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.ITEM_LOGS)        data = logService.getItemLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.TRANSACTION_LOGS) data = logService.getTransactionLogsFiltered(filter, keyword, dateFrom, dateTo);
            else if (logType == LogType.REPUTATION_LOGS)  data = logService.getReputationLogsFiltered(filter, keyword, dateFrom, dateTo);
            else                                          data = new Object[0][5];
        }

        table.setFullData(data, columnNames);
    }

    private String[] resolveEffectiveDateRange() {
        if (activeDateFrom != null && activeDateTo != null)
            return new String[]{ activeDateFrom, activeDateTo };

        if (yearBox == null) return new String[]{ null, null };

        String selYear  = (String) yearBox.getSelectedItem();
        String selMonth = (String) monthBox.getSelectedItem();
        String selDay   = (String) dayBox.getSelectedItem();

        boolean hasYear  = selYear  != null && !selYear.equals("All Years");
        boolean hasMonth = selMonth != null && !selMonth.equals("All Months");
        boolean hasDay   = selDay   != null && !selDay.equals("All Days");

        if (!hasYear) return new String[]{ null, null };

        int year  = Integer.parseInt(selYear);
        int month = hasMonth ? monthNameToNumber(selMonth) : 1;
        int day   = hasDay   ? Integer.parseInt(selDay) : 1;

        String dateFrom, dateTo;

        if (hasDay) {
            dateFrom = String.format("%04d-%02d-%02d", year, month, day);
            dateTo   = dateFrom;
        } else if (hasMonth) {
            YearMonth ym = YearMonth.of(year, month);
            dateFrom = String.format("%04d-%02d-01", year, month);
            dateTo   = String.format("%04d-%02d-%02d", year, month, ym.lengthOfMonth());
        } else {
            dateFrom = year + "-01-01";
            dateTo   = year + "-12-31";
        }

        return new String[]{ dateFrom, dateTo };
    }


    private void updateActionUI() {
        if (btnPrimaryAction == null || btnSecondaryAction == null) return;

        boolean hasSelection = table != null && table.getSelectedRow() != -1;
        btnPrimaryAction.setEnabled(hasSelection);
        btnSecondaryAction.setEnabled(hasSelection);

        if (archiveMode.isSelected()) {
            btnPrimaryAction.setText("Retrieve Data");
            btnPrimaryAction.setDefaultColor(Color.decode("#ffc107"));
            btnSecondaryAction.setText("Delete Data");
            btnSecondaryAction.setDefaultColor(Color.decode("#dc3545"));
        } else {
            btnPrimaryAction.setText("Update Data");
            btnPrimaryAction.setDefaultColor(Color.decode("#28a745"));
            btnSecondaryAction.setText("Archive Data");
            btnSecondaryAction.setDefaultColor(Color.decode("#ffc107"));
        }

        btnPrimaryAction.setHoverColor(btnPrimaryAction.getBackground().darker());
        btnSecondaryAction.setHoverColor(btnSecondaryAction.getBackground().darker());
    }


    private void handleUpdate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        if (type == TableType.USERS) {
            AdminUsers user = userService.getUserById(id);

            CustomTextField firstNameField = new CustomTextField(
                user != null ? user.getFirstName() : "");
            firstNameField.setPreferredSize(new Dimension(250, 32));

            CustomTextField lastNameField = new CustomTextField(
                user != null ? user.getLastName() : "");
            lastNameField.setPreferredSize(new Dimension(250, 32));

            CustomComboBox<String> collegeBox = new CustomComboBox<>(getColleges());
            collegeBox.setPreferredSize(new Dimension(250, 32));
            if (user != null) collegeBox.setSelectedItem(user.getCollege());

            String currentRole = (user != null && user.getSystemRole() != null)
                                 ? user.getSystemRole() : "end_user";

            String[] roleCategories = { "Standard User", "Admin" };
            CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(roleCategories);

            String[] adminSubRoles = { "admin", "super_admin" };
            CustomComboBox<String> roleSubBox = new CustomComboBox<>(adminSubRoles);
            roleSubBox.setCustomSize(130, 30);

            if (currentRole.equals("end_user")) {
                roleCategoryBox.setSelectedItem("Standard User");
                roleSubBox.setVisible(false);
            } else {
                roleCategoryBox.setSelectedItem("Admin");
                roleSubBox.setSelectedItem(currentRole);
                roleSubBox.setVisible(true);
            }

            roleCategoryBox.addActionListener(e -> {
                boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
                roleSubBox.setVisible(isAdmin);
                roleSubBox.getParent().revalidate();
                roleSubBox.getParent().repaint();
            });

            JPanel rolePanel = new JPanel(new BorderLayout(10, 0));
            rolePanel.setOpaque(false);
            rolePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            rolePanel.setPreferredSize(new Dimension(0, 35));

            CustomLabel roleLabel = new CustomLabel("System Role:", 14f, FontStyle.REGULAR);
            roleLabel.setPreferredSize(new Dimension(100, 30));
            roleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel roleComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            roleComboPanel.setOpaque(false);

            roleCategoryBox.setPreferredSize(new Dimension(140, 32));
            roleCategoryBox.setMinimumSize(new Dimension(140, 32));
            roleComboPanel.add(roleCategoryBox);

            roleSubBox.setCustomSize(130, 32);
            roleSubBox.setPreferredSize(new Dimension(130, 32));
            roleSubBox.setMinimumSize(new Dimension(130, 32));
            roleComboPanel.add(roleSubBox);

            rolePanel.add(roleLabel,      BorderLayout.WEST);
            rolePanel.add(roleComboPanel, BorderLayout.CENTER);

            formPanel.add(createFieldPanel("First Name:", firstNameField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Last Name:", lastNameField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("College:", collegeBox));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(rolePanel);

            Window owner = SwingUtilities.getWindowAncestor(this);
            AdminDialog dialog = new AdminDialog(owner, "Update User: " + id, formPanel,
                    "Save Changes", e -> {
                if (user == null) return;

                String firstName = firstNameField.getText().trim();
                String lastName  = lastNameField.getText().trim();
                String selectedCollege = (String) collegeBox.getSelectedItem();

                String resolvedFirst  = firstName.isEmpty()
                    ? user.getFirstName() : firstName;
                String resolvedLast   = lastName.isEmpty()
                    ? user.getLastName()  : lastName;
                String resolvedCollege = (selectedCollege == null || selectedCollege.trim().isEmpty())
                    ? user.getCollege() : selectedCollege.trim();

                String selectedCategory = (String) roleCategoryBox.getSelectedItem();
                String resolvedRole;
                if ("Admin".equals(selectedCategory)) {
                    resolvedRole = (String) roleSubBox.getSelectedItem();
                    if (resolvedRole == null) resolvedRole = "admin";
                } else {
                    resolvedRole = "end_user";
                }

                if (resolvedFirst.isEmpty() || resolvedLast.isEmpty() || resolvedCollege.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Error: First Name, Last Name, and College cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                user.setFirstName(resolvedFirst);
                user.setLastName(resolvedLast);
                user.setCollege(resolvedCollege);
                user.setSystemRole(resolvedRole);

                boolean success = userService.updateUser(user);
                if (success) {
                    loadTableData();
                    JOptionPane.showMessageDialog(this, "Success! User updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Update failed. Please try again.");
                }
            });
            dialog.setVisible(true);

        } else if (type == TableType.ITEMS) {
            AdminItems item = itemService.getItemById(id);

            CustomTextField nameField = new CustomTextField(
                item != null ? item.getItemName() : "");
            nameField.setPreferredSize(new Dimension(250, 32));

            CustomTextField stockField = new CustomTextField(
                item != null ? String.valueOf(item.getItemQuantity()) : "");
            stockField.setPreferredSize(new Dimension(250, 32));

            CustomTextField priceField = new CustomTextField(
                item != null ? String.valueOf(item.getPrice()) : "");
            priceField.setPreferredSize(new Dimension(250, 32));

            String[] categories = { "Textbooks", "Electronics", "Equipment",
                                    "Supplies", "Consumable Goods", "Other" };
            CustomComboBox<String> categoryBox = new CustomComboBox<>(categories);
            categoryBox.setSelectedItem(item != null ? item.getCategory() : "Textbooks");

            String[] conditions = { "Fair", "Good", "New" };
            CustomComboBox<String> conditionBox = new CustomComboBox<>(conditions);
            conditionBox.setSelectedItem(item != null ? item.getItemCondition() : "Good");

            String[] statuses = { "Available", "Unavailable" };
            CustomComboBox<String> statusBox = new CustomComboBox<>(statuses);
            statusBox.setSelectedItem(item != null ? item.getAvailabilityStatus() : "Available");

            categoryBox.setPreferredSize(new Dimension(250, 32));
            conditionBox.setPreferredSize(new Dimension(250, 32));
            statusBox.setPreferredSize(new Dimension(250, 32));

            formPanel.add(createFieldPanel("Item Name:", nameField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Category:", categoryBox));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Condition:", conditionBox));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Stock:", stockField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Price:", priceField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Status:", statusBox));

            Window owner = SwingUtilities.getWindowAncestor(this);
            AdminDialog dialog = new AdminDialog(owner, "Update Item: " + id, formPanel,
                    "Save Changes", e -> {
                if (item == null) return;

                String name      = nameField.getText().trim();
                String stock     = stockField.getText().trim();
                String price     = priceField.getText().trim();
                String category  = (String) categoryBox.getSelectedItem();
                String condition = (String) conditionBox.getSelectedItem();
                String status    = (String) statusBox.getSelectedItem();

                String resolvedName = name.isEmpty()     ? item.getItemName()          : name;
                String resolvedCat  = category == null   ? item.getCategory()          : category;
                String resolvedCond = condition == null  ? item.getItemCondition()      : condition;
                String resolvedStat = status == null     ? item.getAvailabilityStatus() : status;

                if (resolvedName.isEmpty() || resolvedCat.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Error: Item Name and Category cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int resolvedStock, resolvedPrice;
                try {
                    resolvedStock = stock.isEmpty() ? item.getItemQuantity() : Integer.parseInt(stock);
                    resolvedPrice = price.isEmpty() ? item.getPrice()        : Integer.parseInt(price);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error: Stock and Price must be valid whole numbers.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (resolvedStock < 0 || resolvedPrice < 0) {
                    JOptionPane.showMessageDialog(this,
                        "Error: Stock and Price cannot be negative.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                item.setItemName(resolvedName);
                item.setCategory(resolvedCat);
                item.setItemCondition(resolvedCond);
                item.setItemQuantity(resolvedStock);
                item.setPrice(resolvedPrice);
                item.setAvailabilityStatus(resolvedStat);

                boolean success = itemService.updateItem(item);
                if (success) {
                    loadTableData();
                    JOptionPane.showMessageDialog(this, "Success! Item updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Update failed. Please try again.");
                }
            });
            dialog.setVisible(true);
        }
    }

    private void handleArchive() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
        String typeStr = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to archive " + typeStr + " ID " + id + "?",
            "Confirm Archive", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return (type == TableType.USERS)
                    ? userService.archiveUser(id)
                    : itemService.archiveItem(id);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        loadTableData();
                        JOptionPane.showMessageDialog(AdminTable.this,
                            typeStr + " ID " + id + " archived successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // FIX 3 — meaningful error message instead of blaming role only
                        JOptionPane.showMessageDialog(AdminTable.this,
                            "Archive failed for " + typeStr + " ID " + id + ".\n\n"
                            + "Possible reasons:\n"
                            + "  • Account has admin or super_admin role\n"
                            + "  • Database error or FK constraint\n"
                            + "  • Archive table not found\n\n",
                            "Archive Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Unexpected error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void handleRetrieve() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
        String typeStr = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to restore " + typeStr + " ID " + id + "?",
            "Confirm Retrieve", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return (type == TableType.USERS)
                    ? userService.unarchiveUser(id)
                    : itemService.unarchiveItem(id);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        loadTableData();
                        JOptionPane.showMessageDialog(AdminTable.this,
                            "Success! " + typeStr + " ID " + id + " restored successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminTable.this,
                            "Restore failed for " + typeStr + " ID " + id + ".\n"
                            + "The record may not exist in the archive.\n"
                            + "Check server logs for details.",
                            "Restore Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Unexpected error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
        String typeStr = type == TableType.USERS ? "user" : "item";

        int confirm = JOptionPane.showConfirmDialog(this,
            "PERMANENTLY delete " + typeStr + " ID " + id + "?\n\n"
            + "This will remove the record from the archive forever.\n"
            + "This action CANNOT be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return (type == TableType.USERS)
                    ? userService.permanentDeleteUser(id)
                    : itemService.permanentDeleteItem(id);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        loadTableData();
                        JOptionPane.showMessageDialog(AdminTable.this,
                            typeStr + " ID " + id + " permanently deleted.",
                            "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminTable.this,
                            "Permanent delete failed for " + typeStr + " ID " + id + ".\n"
                            + "The record may not exist in the archive.\n"
                            + "Check server logs for details.",
                            "Delete Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Unexpected error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private JPanel createFieldPanel(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(100, 30));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl, BorderLayout.WEST);

        JPanel componentWrapper = new JPanel(new BorderLayout());
        componentWrapper.setOpaque(false);
        componentWrapper.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        if (component instanceof JTextField || component instanceof CustomTextField) {
            component.setPreferredSize(new Dimension(250, 32));
            component.setMinimumSize(new Dimension(250, 32));
        } else if (component instanceof JComboBox) {
            component.setPreferredSize(new Dimension(250, 32));
            component.setMinimumSize(new Dimension(250, 32));
        }

        componentWrapper.add(component, BorderLayout.CENTER);
        panel.add(componentWrapper, BorderLayout.CENTER);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setPreferredSize(new Dimension(0, 35));

        return panel;
    }

    private JPanel createFieldPanel(String label, CustomTextField textField) {
        return createFieldPanel(label, (JComponent) textField);
    }

    @SuppressWarnings("unused")
    private JPanel createFieldPanel(String label) {
        return createFieldPanel(label, (JComponent) new CustomTextField());
    }

    private String[] getColleges() {
        return UMak.COLLEGES_INSTITUTES;
    }
}