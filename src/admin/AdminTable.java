package admin;

import java.awt.*;
import java.awt.event.*;
import java.time.YearMonth;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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

    private String activeDateFrom = null;
    private String activeDateTo = null;
    private int hoveredRow = -1;

    private CustomTable table;
    private CustomButton btnPrimaryAction;
    private CustomButton btnSecondaryAction;
    private CustomSearchField searchField;
    private CustomComboBox<String> filterBox;
    private JCheckBox archiveMode = new JCheckBox("Archive Mode");

    // Date filter components (logs only)
    private CustomComboBox<String> yearBox;
    private CustomComboBox<String> monthBox;
    private CustomComboBox<String> dayBox;
    private CustomButton btnDateRange;
    private CustomButton btnClearDates;
    private javax.swing.Timer logsRefreshTimer;

    // Services
    private final AdminUsersServices userService = new AdminUsersServices();
    private final AdminItemsServices itemService = new AdminItemsServices();
    private final AdminLogsServices logService = new AdminLogsServices();

  
    // CONSTRUCTORS
    public AdminTable(TableType type) {
        this(type, LogType.NONE);
    }

    public AdminTable(TableType type, LogType logType) {
        this.type = type;
        this.logType = logType;
        setLayout(new BorderLayout());
        add(initHeader(), BorderLayout.NORTH);
        add(initTable(), BorderLayout.CENTER);

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
        String title = "Management";
        if (type == TableType.USERS) title = "User Management";
        else if (type == TableType.ITEMS) title = "Item Management";
        else if (type == TableType.LOGS)  title = "Logs Management";

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

        // Refresh button and date filter (logs only)
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
            header.add(buildDateFilterPanel());
        }

        return header;
    }

    // Returns the correct filter options depending on the table type
    private String[] getFilterOptions() {
        if (type == TableType.USERS) {
            return new String[] { "ID", "Student ID", "First Name", "Last Name", "College", "Year Level", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new String[] { "ID", "Item Name", "Condition", "Category", "Stock", "Price", "Status" };
        } else {
            // Logs
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

    
    // DATE FILTER PANEL (logs only)
    private JPanel buildDateFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        // Build year list from 2020 to current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[currentYear - 2019 + 2];
        years[0] = "All Years";
        int index = 1;
        for (int y = currentYear; y >= 2020; y--) {
            years[index] = String.valueOf(y);
            index++;
        }

        yearBox  = new CustomComboBox<>(years);
        monthBox = new CustomComboBox<>(new String[] {
            "All Months", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        });
        dayBox = new CustomComboBox<>(buildDayOptions(31));

        yearBox.setCustomSize(120, 35);
        monthBox.setCustomSize(125, 35);
        dayBox.setCustomSize(105, 35);
        monthBox.setEnabled(false);
        dayBox.setEnabled(false);

        // When year changes, enable/disable month
        yearBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) yearBox.getSelectedItem();
                boolean yearChosen = selected != null && !selected.equals("All Years");
                monthBox.setEnabled(yearChosen);
                if (!yearChosen) {
                    monthBox.setSelectedIndex(0);
                    dayBox.setEnabled(false);
                    dayBox.setSelectedIndex(0);
                }
                loadTableData();
            }
        });

        // When month changes, update day options
        monthBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedMonth = (String) monthBox.getSelectedItem();
                String selectedYear  = (String) yearBox.getSelectedItem();
                boolean monthChosen  = selectedMonth != null && !selectedMonth.equals("All Months");
                if (monthChosen && selectedYear != null && !selectedYear.equals("All Years")) {
                    updateDayCombo(getDaysInMonth(selectedYear, selectedMonth));
                    dayBox.setEnabled(true);
                } else {
                    dayBox.setEnabled(false);
                    dayBox.setSelectedIndex(0);
                }
                loadTableData();
            }
        });

        dayBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadTableData();
            }
        });

        // Date range picker button — has fixed size so the label fits when dates are shown
        btnDateRange = new CustomButton("Date Range", 8);
        btnDateRange.setFontSize(12f);
        btnDateRange.setPadding(6, 10, 6, 10);
        btnDateRange.setDefaultColor(Color.decode("#28a745"));
        btnDateRange.setTextColor(Color.WHITE);
        btnDateRange.setPreferredSize(new Dimension(150, 35));
        btnDateRange.setMinimumSize(new Dimension(150, 35));
        btnDateRange.setMaximumSize(new Dimension(150, 35));
        btnDateRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openDateRangeDialog();
            }
        });

        // Clear all date filters button
        btnClearDates = makeHeaderButton("Clear", Color.decode("#ffc107"), Color.decode("#ffc107").darker());
        btnClearDates.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllDateFilters();
            }
        });

        panel.add(new CustomLabel("Year:"));
        panel.add(yearBox);
        panel.add(new CustomLabel("Month:"));
        panel.add(monthBox);
        panel.add(new CustomLabel("Day:"));
        panel.add(dayBox);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(btnDateRange);
        panel.add(btnClearDates);

        return panel;
    }

    // Builds the day dropdown options up to maxDay
    private String[] buildDayOptions(int maxDay) {
        String[] days = new String[maxDay + 1];
        days[0] = "All Days";
        for (int d = 1; d <= maxDay; d++) {
            days[d] = String.valueOf(d);
        }
        return days;
    }

    // Refreshes the day dropdown when the month changes
    private void updateDayCombo(int maxDay) {
        String previousSelection = (String) dayBox.getSelectedItem();
        dayBox.removeAllItems();
        dayBox.addItem("All Days");
        for (int d = 1; d <= maxDay; d++) {
            dayBox.addItem(String.valueOf(d));
        }
        // Try to keep the previously selected day if it still fits
        if (previousSelection != null && !previousSelection.equals("All Days")) {
            try {
                int prev = Integer.parseInt(previousSelection);
                if (prev <= maxDay) {
                    dayBox.setSelectedItem(previousSelection);
                } else {
                    dayBox.setSelectedIndex(0);
                }
            } catch (NumberFormatException ignored) {
                dayBox.setSelectedIndex(0);
            }
        }
    }

    // Returns how many days are in the given month and year
    private int getDaysInMonth(String yearStr, String monthStr) {
        try {
            int year  = Integer.parseInt(yearStr);
            int month = monthNameToNumber(monthStr);
            return YearMonth.of(year, month).lengthOfMonth();
        } catch (Exception e) {
            return 31;
        }
    }

    // Converts a month name like "Jan" to its number like 1
    private int monthNameToNumber(String name) {
        String[] names = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return i + 1;
            }
        }
        return 1;
    }

    // Opens the date range dialog where the user picks From and To dates
    private void openDateRangeDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Select Date Range", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 260);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout());

        CustomPanel content = new CustomPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 25, 10, 25));

        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();
        CustomSpinner fromSpinner  = new CustomSpinner(fromModel);
        CustomSpinner toSpinner    = new CustomSpinner(toModel);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));

        if (FontLib.POPPINS_REGULAR != null) {
            fromSpinner.setFont(FontLib.POPPINS_REGULAR.deriveFont(13f));
            toSpinner.setFont(FontLib.POPPINS_REGULAR.deriveFont(13f));
        }
        fromSpinner.setPreferredSize(new Dimension(200, 32));
        toSpinner.setPreferredSize(new Dimension(200, 32));

        // Pre-fill spinners if a range was already set
        try {
            if (activeDateFrom != null) fromModel.setValue(java.sql.Date.valueOf(activeDateFrom));
        } catch (Exception ignored) {}
        try {
            if (activeDateTo != null) toModel.setValue(java.sql.Date.valueOf(activeDateTo));
        } catch (Exception ignored) {}

        content.add(createSpinnerRow("From:", fromSpinner));
        content.add(Box.createVerticalStrut(12));
        content.add(createSpinnerRow("To:  ", toSpinner));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnRow.setOpaque(false);

        // Clear button resets the active date range
        CustomButton clearBtn = new CustomButton("Clear Range", 8);
        clearBtn.setPadding(5, 12, 5, 12);
        clearBtn.setDefaultColor(Color.decode("#ffc107"));
        clearBtn.setTextColor(Color.WHITE);
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeDateFrom = null;
                activeDateTo   = null;
                updateDateRangeButtonLabel();
                loadTableData();
                dialog.dispose();
            }
        });

        // Apply button saves the selected range and reloads data
        CustomButton applyBtn = new CustomButton("Apply", 8);
        applyBtn.setPadding(5, 12, 5, 12);
        applyBtn.setDefaultColor(Color.decode("#28a745"));
        applyBtn.setTextColor(Color.WHITE);
        applyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                java.util.Date from = (java.util.Date) fromSpinner.getValue();
                java.util.Date to   = (java.util.Date) toSpinner.getValue();
                if (from.after(to)) {
                    JOptionPane.showMessageDialog(dialog,
                        "'From' date cannot be after 'To' date.", "Invalid Range", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                activeDateFrom = new java.sql.Date(from.getTime()).toString();
                activeDateTo   = new java.sql.Date(to.getTime()).toString();
                updateDateRangeButtonLabel();
                loadTableData();
                dialog.dispose();
            }
        });

        btnRow.add(clearBtn);
        btnRow.add(applyBtn);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnRow,  BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Creates a row with a label on the left and a spinner on the right
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

    // Updates the Date Range button label to show the selected range (or default text)
    private void updateDateRangeButtonLabel() {
        if (btnDateRange == null) return;
        if (activeDateFrom != null && activeDateTo != null) {
            btnDateRange.setText(activeDateFrom + " — " + activeDateTo);
        } else {
            btnDateRange.setText("Date Range");
        }
        btnDateRange.repaint();
    }

    // Resets all date filters (dropdowns + date range)
    private void clearAllDateFilters() {
        activeDateFrom = null;
        activeDateTo   = null;
        if (yearBox  != null) yearBox.setSelectedIndex(0);
        if (monthBox != null) {
            monthBox.setSelectedIndex(0);
            monthBox.setEnabled(false);
        }
        if (dayBox != null) {
            dayBox.setSelectedIndex(0);
            dayBox.setEnabled(false);
        }
        updateDateRangeButtonLabel();
        loadTableData();
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
                setBackground(Brand.PRIMARY_COLOR);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(80, 85, 90)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                setHorizontalAlignment(SwingConstants.LEFT);
                if (FontLib.POPPINS_BOLD != null) {
                    setFont(FontLib.POPPINS_BOLD.deriveFont(12f));
                }
                return this;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Action buttons for users and items (not logs)
        if (type != TableType.LOGS) {
            btnPrimaryAction   = new CustomButton("Update Data", 8);
            btnSecondaryAction = new CustomButton("Archive Data", 8);
            btnPrimaryAction.setFontSize(12f);
            btnSecondaryAction.setFontSize(12f);
            btnPrimaryAction.setPadding(6, 14, 6, 14);
            btnSecondaryAction.setPadding(6, 14, 6, 14);
            updateActionUI();

            btnPrimaryAction.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (archiveMode.isSelected()) {
                        handleRetrieve();
                    } else {
                        handleUpdate();
                    }
                }
            });
            btnSecondaryAction.addActionListener(new ActionListener() {
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
            actionsPanel.add(btnPrimaryAction);
            actionsPanel.add(btnSecondaryAction);
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
                                  "College", "Year", "Karma Points", "System Role" };
        } else if (type == TableType.ITEMS) {
            return new Object[] { "ID", "Item Name", "Condition", "Category", "Stock", "Price", "Status" };
        } else {
            return new Object[] { "ID", "User ID", "Log Type", "Description", "Date" };
        }
    }

    // Sets column widths and text alignment for each table type
    private void applyColumnWidths() {
        table.setColumnWidth(0, 55);
        table.setColumnAlignment(0, SwingConstants.CENTER);

        if (type == TableType.USERS) {
            table.setColumnWidth(1, 160); table.setColumnAlignment(1, SwingConstants.CENTER);
            table.setColumnWidth(4, 150);
            table.setColumnWidth(5, 70);  table.setColumnAlignment(5, SwingConstants.CENTER);
            table.setColumnWidth(6, 110); table.setColumnAlignment(6, SwingConstants.CENTER);
            table.setColumnWidth(7, 130); table.setColumnAlignment(7, SwingConstants.CENTER);

        } else if (type == TableType.ITEMS) {
            table.setColumnWidth(2, 110); table.setColumnAlignment(2, SwingConstants.CENTER);
            table.setColumnWidth(3, 140); table.setColumnAlignment(3, SwingConstants.CENTER);
            table.setColumnWidth(4, 75);  table.setColumnAlignment(4, SwingConstants.CENTER);
            table.setColumnWidth(5, 80);  table.setColumnAlignment(5, SwingConstants.CENTER);
            table.setColumnWidth(6, 115); table.setColumnAlignment(6, SwingConstants.CENTER);

        } else {
            table.setColumnWidth(1, 90);  table.setColumnAlignment(1, SwingConstants.CENTER);
            table.setColumnWidth(2, 150); table.setColumnAlignment(2, SwingConstants.CENTER);
            table.setColumnWidth(4, 160); table.setColumnAlignment(4, SwingConstants.CENTER);
            TableColumn descCol = table.getColumnModel().getColumn(3);
            descCol.setPreferredWidth(260);
            descCol.setMinWidth(150);
        }
    }


    // DATA LOADING
    // Loads the correct data into the table based on current filters
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
            // Logs
            String[] range = resolveEffectiveDateRange();
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

    // Figures out the date range to use based on dropdowns or the range dialog
    private String[] resolveEffectiveDateRange() {
        // Date range dialog takes priority over the dropdowns
        if (activeDateFrom != null && activeDateTo != null) {
            return new String[] { activeDateFrom, activeDateTo };
        }

        if (yearBox == null) {
            return new String[] { null, null };
        }

        String  selYear  = (String) yearBox.getSelectedItem();
        String  selMonth = (String) monthBox.getSelectedItem();
        String  selDay   = (String) dayBox.getSelectedItem();

        boolean hasYear  = selYear  != null && !selYear.equals("All Years");
        boolean hasMonth = selMonth != null && !selMonth.equals("All Months");
        boolean hasDay   = selDay   != null && !selDay.equals("All Days");

        if (!hasYear) {
            return new String[] { null, null };
        }

        int year  = Integer.parseInt(selYear);
        int month = hasMonth ? monthNameToNumber(selMonth) : 1;

        if (hasDay) {
            int    day  = Integer.parseInt(selDay);
            String date = String.format("%04d-%02d-%02d", year, month, day);
            return new String[] { date, date };

        } else if (hasMonth) {
            YearMonth ym   = YearMonth.of(year, month);
            String    from = String.format("%04d-%02d-01", year, month);
            String    to   = String.format("%04d-%02d-%02d", year, month, ym.lengthOfMonth());
            return new String[] { from, to };

        } else {
            return new String[] { year + "-01-01", year + "-12-31" };
        }
    }

    // ACTION BUTTONS UI
    // Updates the action button text, color, and enabled state
    private void updateActionUI() {
        if (btnPrimaryAction == null || btnSecondaryAction == null) return;

        boolean hasSelection = table != null && table.getSelectedRow() != -1;
        btnPrimaryAction.setEnabled(hasSelection);
        btnSecondaryAction.setEnabled(hasSelection);

        if (archiveMode.isSelected()) {
            applyButtonStyle(btnPrimaryAction,   "Retrieve Data", "#ffc107");
            applyButtonStyle(btnSecondaryAction, "Delete Data",   "#dc3545");
        } else {
            applyButtonStyle(btnPrimaryAction,   "Update Data",   "#28a745");
            applyButtonStyle(btnSecondaryAction, "Archive Data",  "#ffc107");
        }
    }

    // Sets the text, background color, and hover color on a button
    private void applyButtonStyle(CustomButton btn, String text, String hexColor) {
        btn.setText(text);
        btn.setDefaultColor(Color.decode(hexColor));
        btn.setHoverColor(btn.getBackground().darker());
    }


    // CRUD HANDLERS
    // Opens the correct update dialog depending on whether it's a user or item
    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        if (type == TableType.USERS) {
            buildUserUpdateDialog(id, formPanel);
        } else if (type == TableType.ITEMS) {
            buildItemUpdateDialog(id, formPanel);
        }
    }

    // Builds and shows the update dialog for a user
    private void buildUserUpdateDialog(int id, CustomPanel formPanel) {
        AdminUsers user = userService.getUserById(id);

        CustomTextField firstNameField = new CustomTextField(user != null ? user.getFirstName() : "");
        CustomTextField lastNameField  = new CustomTextField(user != null ? user.getLastName()  : "");

        CustomComboBox<String> collegeBox = new CustomComboBox<>(getColleges());
        if (user != null) {
            collegeBox.setSelectedItem(user.getCollege());
        }

        // Role selection: Standard User or Admin
        String currentRole = (user != null && user.getSystemRole() != null) ? user.getSystemRole() : "end_user";
        CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(new String[] { "Standard User", "Admin" });
        CustomComboBox<String> roleSubBox      = new CustomComboBox<>(new String[] { "admin", "super_admin" });
        roleSubBox.setCustomSize(130, 30);

        if (currentRole.equals("end_user")) {
            roleCategoryBox.setSelectedItem("Standard User");
            roleSubBox.setVisible(false);
        } else {
            roleCategoryBox.setSelectedItem("Admin");
            roleSubBox.setSelectedItem(currentRole);
        }

        // Show/hide sub-role dropdown based on category selection
        roleCategoryBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
                roleSubBox.setVisible(isAdmin);
                roleSubBox.getParent().revalidate();
                roleSubBox.getParent().repaint();
            }
        });

        JPanel roleComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleComboPanel.setOpaque(false);
        roleCategoryBox.setPreferredSize(new Dimension(140, 32));
        roleSubBox.setPreferredSize(new Dimension(130, 32));
        roleComboPanel.add(roleCategoryBox);
        roleComboPanel.add(roleSubBox);

        JPanel roleRow = new JPanel(new BorderLayout(10, 0));
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        CustomLabel roleLabel = new CustomLabel("System Role:", 14f, FontStyle.REGULAR);
        roleLabel.setPreferredSize(new Dimension(100, 30));
        roleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        roleRow.add(roleLabel,      BorderLayout.WEST);
        roleRow.add(roleComboPanel, BorderLayout.CENTER);

        formPanel.add(createFieldPanel("First Name:", firstNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",  lastNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",    collegeBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(roleRow);

        Window owner = SwingUtilities.getWindowAncestor(this);
        new AdminDialog(owner, "Update User: " + id, formPanel, "Save Changes", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (user == null) return;

                // Use existing value if the field was left empty
                String resolvedFirst = firstNameField.getText().trim().isEmpty()
                    ? user.getFirstName() : firstNameField.getText().trim();
                String resolvedLast = lastNameField.getText().trim().isEmpty()
                    ? user.getLastName() : lastNameField.getText().trim();
                String resolvedCollege = (collegeBox.getSelectedItem() == null)
                    ? user.getCollege() : collegeBox.getSelectedItem().toString().trim();

                // Determine final role string
                String resolvedRole;
                if ("Admin".equals(roleCategoryBox.getSelectedItem())) {
                    resolvedRole = (roleSubBox.getSelectedItem() != null)
                        ? roleSubBox.getSelectedItem().toString() : "admin";
                } else {
                    resolvedRole = "end_user";
                }

                if (resolvedFirst.isEmpty() || resolvedLast.isEmpty() || resolvedCollege.isEmpty()) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "First Name, Last Name, and College cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                user.setFirstName(resolvedFirst);
                user.setLastName(resolvedLast);
                user.setCollege(resolvedCollege);
                user.setSystemRole(resolvedRole);

                showUpdateResult(userService.updateUser(user), "User");
            }
        }).setVisible(true);
    }

    // Builds and shows the update dialog for an item
    private void buildItemUpdateDialog(int id, CustomPanel formPanel) {
        AdminItems item = itemService.getItemById(id);

        CustomTextField nameField  = new CustomTextField(item != null ? item.getItemName() : "");
        CustomTextField stockField = new CustomTextField(item != null ? String.valueOf(item.getItemQuantity()) : "");
        CustomTextField priceField = new CustomTextField(item != null ? String.valueOf(item.getPrice()) : "");

        CustomComboBox<String> categoryBox = new CustomComboBox<>(
            new String[] { "Textbooks", "Electronics", "Equipment", "Supplies", "Consumable Goods", "Other" });
        CustomComboBox<String> conditionBox = new CustomComboBox<>(new String[] { "Fair", "Good", "New" });
        CustomComboBox<String> statusBox    = new CustomComboBox<>(new String[] { "Available", "Unavailable" });

        if (item != null) {
            categoryBox.setSelectedItem(item.getCategory());
            conditionBox.setSelectedItem(item.getItemCondition());
            statusBox.setSelectedItem(item.getAvailabilityStatus());
        }

        formPanel.add(createFieldPanel("Item Name:", nameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Category:",  categoryBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Condition:", conditionBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Stock:",     stockField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Price:",     priceField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Status:",    statusBox));

        Window owner = SwingUtilities.getWindowAncestor(this);
        new AdminDialog(owner, "Update Item: " + id, formPanel, "Save Changes", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (item == null) return;

                // Use existing value if the field was left empty
                String resolvedName = nameField.getText().trim().isEmpty()
                    ? item.getItemName() : nameField.getText().trim();
                String resolvedCat = (categoryBox.getSelectedItem() != null)
                    ? categoryBox.getSelectedItem().toString() : item.getCategory();
                String resolvedCond = (conditionBox.getSelectedItem() != null)
                    ? conditionBox.getSelectedItem().toString() : item.getItemCondition();
                String resolvedStat = (statusBox.getSelectedItem() != null)
                    ? statusBox.getSelectedItem().toString() : item.getAvailabilityStatus();

                if (resolvedName.isEmpty() || resolvedCat.isEmpty()) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Item Name and Category cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int resolvedStock;
                int resolvedPrice;
                try {
                    resolvedStock = stockField.getText().trim().isEmpty()
                        ? item.getItemQuantity() : Integer.parseInt(stockField.getText().trim());
                    resolvedPrice = priceField.getText().trim().isEmpty()
                        ? item.getPrice() : Integer.parseInt(priceField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Stock and Price must be valid whole numbers.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (resolvedStock < 0 || resolvedPrice < 0) {
                    JOptionPane.showMessageDialog(AdminTable.this,
                        "Stock and Price cannot be negative.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                item.setItemName(resolvedName);
                item.setCategory(resolvedCat);
                item.setItemCondition(resolvedCond);
                item.setItemQuantity(resolvedStock);
                item.setPrice(resolvedPrice);
                item.setAvailabilityStatus(resolvedStat);

                showUpdateResult(itemService.updateItem(item), "Item");
            }
        }).setVisible(true);
    }

    // Shows a success or failure message after an update
    private void showUpdateResult(boolean success, String entityName) {
        if (success) {
            loadTableData();
            JOptionPane.showMessageDialog(this, entityName + " updated successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Update failed. Please try again.");
        }
    }

    // Asks the user to confirm, then archives the selected row
    private void handleArchive() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int id = getSelectedId(row);

        String entityType = type == TableType.USERS ? "user" : "item";
        int choice = JOptionPane.showConfirmDialog(this,
            "Archive " + entityType + " ID " + id + "?", "Confirm Archive", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String successMsg = entityType + " ID " + id + " archived successfully.";
        String failureMsg = "Archive failed for " + entityType + " ID " + id + ".\n\n"
            + "Possible reasons:\n"
            + "  • Account has admin or super_admin role\n"
            + "  • Database error or FK constraint\n"
            + "  • Archive table not found";

        if (type == TableType.USERS) {
            runAsync(new ArchiveTask(id, "archive_user"), successMsg, failureMsg);
        } else {
            runAsync(new ArchiveTask(id, "archive_item"), successMsg, failureMsg);
        }
    }

    // Asks the user to confirm, then restores the selected archived row
    private void handleRetrieve() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = getSelectedId(row);

        String entityType = type == TableType.USERS ? "user" : "item";
        int choice = JOptionPane.showConfirmDialog(this,
            "Restore " + entityType + " ID " + id + "?", "Confirm Restore", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String successMsg = entityType + " ID " + id + " restored successfully.";
        String failureMsg = "Restore failed for " + entityType + " ID " + id
            + ".\nThe record may not exist in the archive.";

        if (type == TableType.USERS) {
            runAsync(new ArchiveTask(id, "restore_user"), successMsg, failureMsg);
        } else {
            runAsync(new ArchiveTask(id, "restore_item"), successMsg, failureMsg);
        }
    }

    // Asks the user to confirm, then permanently deletes the selected archived row
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

        if (type == TableType.USERS) {
            runAsync(new ArchiveTask(id, "delete_user"), successMsg, failureMsg);
        } else {
            runAsync(new ArchiveTask(id, "delete_item"), successMsg, failureMsg);
        }
    }


    // Runs the given task in the background so the UI doesn't freeze
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

    // Simple helper class used to pass a DB task into runAsync without lambdas
    private class ArchiveTask {
        private final int    id;
        private final String action;

        public ArchiveTask(int id, String action) {
            this.id     = id;
            this.action = action;
        }

        public boolean run() {
            if (action.equals("archive_user"))  return userService.archiveUser(id);
            if (action.equals("restore_user"))  return userService.unarchiveUser(id);
            if (action.equals("delete_user"))   return userService.permanentDeleteUser(id);
            if (action.equals("archive_item"))  return itemService.archiveItem(id);
            if (action.equals("restore_item"))  return itemService.unarchiveItem(id);
            if (action.equals("delete_item"))   return itemService.permanentDeleteItem(id);
            return false;
        }
    }

    // Gets the ID from column 0 of the selected row
    private int getSelectedId(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        return Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
    }

    // Creates a label + input field row used inside update dialogs
    private JPanel createFieldPanel(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setPreferredSize(new Dimension(0, 35));

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(100, 30));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl, BorderLayout.WEST);

        component.setPreferredSize(new Dimension(250, 32));
        component.setMinimumSize(new Dimension(250, 32));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(component, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);

        return panel;
    }

    // Returns the list of colleges from UMak constants
    private String[] getColleges() {
        return UMak.COLLEGES_INSTITUTES;
    }
}