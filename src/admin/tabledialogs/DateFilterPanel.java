package admin.tabledialogs;

import components.*;
import utils.*;
import utils.FontLib;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.YearMonth;
import java.util.Calendar;

public class DateFilterPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private String rangeStartDate = null;   // renamed from activeDateFrom
    private String rangeEndDate   = null;   // renamed from activeDateTo

    private CustomComboBox<String> yearBox;
    private CustomComboBox<String> monthBox;
    private CustomComboBox<String> dayBox;
    private CustomButton btnDateRange;
    private CustomButton btnClearDates;

    private final Runnable loadTableDataCallback;

    public DateFilterPanel(Runnable loadTableDataCallback) {
        this.loadTableDataCallback = loadTableDataCallback;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buildDateFilterPanel();
    }

    // Returns the active date range: [dateFrom, dateTo], either from the
    // range dialog or from the year/month/day dropdowns (dialog takes priority)
    public String[] getEffectiveDateRange() {
        if (rangeStartDate != null && rangeEndDate != null) {
            return new String[] { rangeStartDate, rangeEndDate };
        }

        if (yearBox == null) {
            return new String[] { null, null };
        }

        String selYear  = (String) yearBox.getSelectedItem();
        String selMonth = (String) monthBox.getSelectedItem();
        String selDay   = (String) dayBox.getSelectedItem();

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

    // Builds and adds all date filter UI components into this panel
    private void buildDateFilterPanel() {
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

        yearBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String  selected   = (String) yearBox.getSelectedItem();
                boolean yearChosen = selected != null && !selected.equals("All Years");
                monthBox.setEnabled(yearChosen);
                if (!yearChosen) {
                    monthBox.setSelectedIndex(0);
                    dayBox.setEnabled(false);
                    dayBox.setSelectedIndex(0);
                }
                loadTableDataCallback.run();
            }
        });

        monthBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String  selectedMonth = (String) monthBox.getSelectedItem();
                String  selectedYear  = (String) yearBox.getSelectedItem();
                boolean monthChosen   = selectedMonth != null && !selectedMonth.equals("All Months");
                if (monthChosen && selectedYear != null && !selectedYear.equals("All Years")) {
                    updateDayCombo(getDaysInMonth(selectedYear, selectedMonth));
                    dayBox.setEnabled(true);
                } else {
                    dayBox.setEnabled(false);
                    dayBox.setSelectedIndex(0);
                }
                loadTableDataCallback.run();
            }
        });

        dayBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadTableDataCallback.run();
            }
        });

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

        btnClearDates = makeButton("Clear", Color.decode("#ffc107"), Color.decode("#ffc107").darker());
        btnClearDates.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllDateFilters();
            }
        });

        add(new CustomLabel("Year:"));
        add(yearBox);
        add(new CustomLabel("Month:"));
        add(monthBox);
        add(new CustomLabel("Day:"));
        add(dayBox);
        add(Box.createHorizontalStrut(4));
        add(btnDateRange);
        add(btnClearDates);
    }

    // Builds the day dropdown options from "All Days" up to maxDay
    private String[] buildDayOptions(int maxDay) {
        String[] days = new String[maxDay + 1];
        days[0] = "All Days";
        for (int d = 1; d <= maxDay; d++) {
            days[d] = String.valueOf(d);
        }
        return days;
    }

    // Refreshes the day dropdown when the selected month changes
    private void updateDayCombo(int maxDay) {
        String previousSelection = (String) dayBox.getSelectedItem();
        dayBox.removeAllItems();
        dayBox.addItem("All Days");
        for (int d = 1; d <= maxDay; d++) {
            dayBox.addItem(String.valueOf(d));
        }
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
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner,     "yyyy-MM-dd"));

        if (FontLib.POPPINS_REGULAR != null) {
            fromSpinner.setFont(FontLib.POPPINS_REGULAR.deriveFont(13f));
            toSpinner.setFont(FontLib.POPPINS_REGULAR.deriveFont(13f));
        }
        fromSpinner.setPreferredSize(new Dimension(200, 32));
        toSpinner.setPreferredSize(new Dimension(200, 32));

        try {
            if (rangeStartDate != null) fromModel.setValue(java.sql.Date.valueOf(rangeStartDate));
        } catch (Exception ignored) {}
        try {
            if (rangeEndDate != null) toModel.setValue(java.sql.Date.valueOf(rangeEndDate));
        } catch (Exception ignored) {}

        content.add(createSpinnerRow("From:", fromSpinner));
        content.add(Box.createVerticalStrut(12));
        content.add(createSpinnerRow("To:  ", toSpinner));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnRow.setOpaque(false);

        CustomButton clearBtn = new CustomButton("Clear Range", 8);
        clearBtn.setPadding(5, 12, 5, 12);
        clearBtn.setDefaultColor(Color.decode("#ffc107"));
        clearBtn.setTextColor(Color.WHITE);
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rangeStartDate = null;
                rangeEndDate   = null;
                updateDateRangeButtonLabel();
                loadTableDataCallback.run();
                dialog.dispose();
            }
        });

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
                        "'From' date cannot be after 'To' date.",
                        "Invalid Range", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                rangeStartDate = new java.sql.Date(from.getTime()).toString();
                rangeEndDate   = new java.sql.Date(to.getTime()).toString();
                updateDateRangeButtonLabel();
                loadTableDataCallback.run();
                dialog.dispose();
            }
        });

        btnRow.add(clearBtn);
        btnRow.add(applyBtn);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnRow,  BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Creates a label + spinner row for the date range dialog
    private JPanel createSpinnerRow(String label, JSpinner spinner) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(45, 30));
        row.add(lbl,     BorderLayout.WEST);
        row.add(spinner, BorderLayout.CENTER);
        return row;
    }

    // Updates the Date Range button label to show the selected range (or default text)
    private void updateDateRangeButtonLabel() {
        if (btnDateRange == null) return;
        if (rangeStartDate != null && rangeEndDate != null) {
            btnDateRange.setText(rangeStartDate + " — " + rangeEndDate);
        } else {
            btnDateRange.setText("Date Range");
        }
        btnDateRange.repaint();
    }

    // Resets all date filters: dropdowns and the date range dialog values
    private void clearAllDateFilters() {
        rangeStartDate = null;
        rangeEndDate   = null;
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
        loadTableDataCallback.run();
    }

    // Creates a small styled button
    private CustomButton makeButton(String text, Color defaultColor, Color hoverColor) {
        CustomButton btn = new CustomButton(text, 8);
        btn.setFontSize(12f);
        btn.setPadding(6, 14, 6, 14);
        btn.setDefaultColor(defaultColor);
        btn.setTextColor(Color.WHITE);
        btn.setHoverColor(hoverColor);
        return btn;
    }
}