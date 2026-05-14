package admin;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;
import admin.models.*;
import admin.database.*;
import admin.services.*;
import items.*;

public class AdminTable extends CustomPanel {
    private static final long serialVersionUID = 1L;


    private TableType type;
    private LogType logType; 
    private JCheckBox archiveMode = new JCheckBox("Archive Mode");
    private CustomTable table;
    private CustomButton btnPrimaryAction;
    private CustomButton btnSecondaryAction;

    // ADDED: our service objects — one per table type
    private AdminUsersServices userService = new AdminUsersServices();
    private AdminItemsServices itemService = new AdminItemsServices();
    private AdminLogsServices  logService  = new AdminLogsServices();

    public AdminTable(TableType type) {
        this(type, LogType.NONE);
    }
    public AdminTable(TableType type, LogType logType) {
        this.type    = type;
        this.logType = logType;
        setLayout(new BorderLayout());
        add(initHeader(), BorderLayout.NORTH);
        add(initTable(), BorderLayout.CENTER);
    }

    private CustomPanel initHeader() {
        CustomPanel header = new CustomPanel();
        header.setPadding(20, 15);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        String titleText = "Management";
        if (type == TableType.USERS) titleText = "User Management";
        else if (type == TableType.ITEMS) titleText = "Item Management";
        else if (type == TableType.LOGS)  titleText = "Logs Management";

        header.add(new CustomLabel(titleText, 18f, FontStyle.BOLD));
        header.add(Box.createHorizontalGlue());

        CustomSearchField searchField = new CustomSearchField("Search...", 25, 10);
        searchField.setCustomSize(200, 35);
        header.add(searchField);
        header.add(Box.createHorizontalStrut(10));

        header.add(new CustomLabel("Search by:"));
        header.add(Box.createHorizontalStrut(10));
        CustomComboBox<String> filterBox = new CustomComboBox<>(new String[]{"ID", "First Name", "Last Name"});
        filterBox.setCustomSize(150, 35);
        header.add(filterBox);
        header.add(Box.createHorizontalStrut(10));

        if (type != TableType.LOGS) {
            archiveMode.setOpaque(false);
            if (FontLib.POPPINS_REGULAR != null) {
                archiveMode.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
            }
          
            archiveMode.addItemListener(e -> {
                updateActionUI();
                loadTableData();  
            });
            header.add(archiveMode);
        }

        return header;
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

    private CustomPanel initTable() {
        CustomPanel tableWrapper = new CustomPanel();
        tableWrapper.setLayout(new BorderLayout());

        Object[] columnNames;
        if (type == TableType.USERS) {
            columnNames = new Object[]{"ID", "Student ID", "First Name", "Last Name", "College", "Year", "Karma Points"};
        } else if (type == TableType.ITEMS) {
            columnNames = new Object[]{"ID", "Item Name", "Category", "Stock", "Price", "Status"};
        } else {
            columnNames = new Object[]{"ID", "Log Type", "Description", "Date", "User ID"};
        }

        Object[][] emptyData = new Object[0][columnNames.length];
        table = new CustomTable(emptyData, columnNames);

        if (type != TableType.LOGS) {
            btnPrimaryAction   = new CustomButton("Update Data", 8);
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
        table.setHeaderCustomization(FontLib.POPPINS_BOLD.deriveFont(14f), Color.BLACK, new Color(240, 240, 240));
        table.setBodyCustomization(FontLib.POPPINS_REGULAR.deriveFont(12f), Color.BLACK, Color.WHITE);
        table.setColumnWidth(0, 50);
        table.setColumnAlignment(0, SwingConstants.CENTER);

        if (type == TableType.USERS) {
            table.setColumnWidth(1, 200);
            table.setColumnWidth(4, 200);
            table.setColumnWidth(5, 200);
            table.setColumnWidth(6, 200);
            table.setColumnAlignment(1, SwingConstants.CENTER);
            table.setColumnAlignment(4, SwingConstants.CENTER);
            table.setColumnAlignment(5, SwingConstants.CENTER);
            table.setColumnAlignment(6, SwingConstants.CENTER);
        } if (type == TableType.ITEMS) {
            table.setColumnWidth(2, 200);
            table.setColumnWidth(3, 200);
            table.setColumnWidth(4, 200);
            table.setColumnWidth(5, 200);
        } else if (type == TableType.LOGS) {
            table.setColumnWidth(1, 200);
            table.setColumnWidth(3, 200);
            table.setColumnWidth(4, 200);
        }

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

        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        tableWrapper.add(bottomPanel, BorderLayout.SOUTH);

        return tableWrapper;
    }

 
    private void loadTableData() {
        if (table == null) return;

        Object[] columnNames;
        Object[][] data;

        if (type == TableType.USERS) {
            columnNames = new Object[]{"ID", "Student ID", "First Name", "Last Name", "College", "Year", "Karma Points"};
           
            if (archiveMode.isSelected()) {
                data = userService.getArchivedUsersForTable();
            } else {
                data = userService.getActiveUsersForTable();
            }

        } else if (type == TableType.ITEMS) {
            columnNames = new Object[]{"ID", "Item Name", "Category", "Stock", "Price", "Status"};
            if (archiveMode.isSelected()) {
                data = itemService.getArchivedItemsForTable();
            } else {
                data = itemService.getActiveItemsForTable();
            }

        } else {
            
            columnNames = new Object[]{"ID", "Log Type", "Description", "Date", "User ID"};
            if      (logType == LogType.USER_LOGS)        data = logService.getUserLogsForTable();
            else if (logType == LogType.ITEM_LOGS)        data = logService.getItemLogsForTable();
            else if (logType == LogType.TRANSACTION_LOGS) data = logService.getTransactionLogsForTable();
            else if (logType == LogType.REPUTATION_LOGS)  data = logService.getReputationLogsForTable();
            else                                          data = new Object[0][5];
        }

        table.setFullData(data, columnNames);
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

            JTextField firstNameField = new JTextField(user != null ? user.getFirstName() : "");
            JTextField lastNameField  = new JTextField(user != null ? user.getLastName()  : "");
            JTextField collegeField   = new JTextField(user != null ? user.getCollege()   : "");

            formPanel.add(createFieldPanel("First Name:", firstNameField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Last Name:",  lastNameField));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("College:",    collegeField));

            Window owner = SwingUtilities.getWindowAncestor(this);
            AdminDialog dialog = new AdminDialog(owner, "Update User: " + id, formPanel, "Save Changes", e -> {
          
                if (user != null) {
                    user.setFirstName(firstNameField.getText().trim());
                    user.setLastName(lastNameField.getText().trim());
                    user.setCollege(collegeField.getText().trim());

                    boolean success = userService.updateUser(user);
                    if (success) {
                        loadTableData(); 
                        JOptionPane.showMessageDialog(this, "User updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Update failed. Please try again.");
                    }
                }
            });
            dialog.setVisible(true);

        } else if (type == TableType.ITEMS) {
            formPanel.add(createFieldPanel("Item Name:", new JTextField()));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Category:",  new JTextField()));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Stock:",     new JTextField()));
            formPanel.add(Box.createVerticalStrut(10));
            formPanel.add(createFieldPanel("Price:",     new JTextField()));

            Window owner = SwingUtilities.getWindowAncestor(this);
            AdminDialog dialog = new AdminDialog(owner, "Update Item: " + id, formPanel, "Save Changes", e -> {
                System.out.println("Item update saved for ID: " + id);
                
            });
            dialog.setVisible(true);
        }
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.add(new CustomLabel(label, 14f, FontStyle.REGULAR), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
    }

   
    private JPanel createFieldPanel(String label) {
        return createFieldPanel(label, new JTextField());
    }

    private void handleArchive() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

        String typeStr = type == TableType.USERS ? "user" : "item";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to archive " + typeStr + " " + id + "?",
            "Confirm Archive", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = false;

          
            if (type == TableType.USERS) {
                success = userService.archiveUser(id);
            } else if (type == TableType.ITEMS) {
                success = itemService.archiveItem(id);
            }

            if (success) {
                loadTableData(); 
                JOptionPane.showMessageDialog(this, typeStr + " archived successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Archive failed. This may be an admin account.");
            }
        }
    }

    private void handleRetrieve() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

        String typeStr = type == TableType.USERS ? "user" : "item";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to retrieve " + typeStr + " " + id + "?",
            "Confirm Retrieve", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = false;


            if (type == TableType.USERS) {
                success = userService.unarchiveUser(id);
            } else if (type == TableType.ITEMS) {
                success = itemService.unarchiveItem(id);
            }

            if (success) {
                loadTableData();
                JOptionPane.showMessageDialog(this, typeStr + " restored successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Restore failed. Please try again.");
            }
        }
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

        String typeStr = type == TableType.USERS ? "user" : "item";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete " + typeStr + " " + id + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
           
            System.out.println("Permanent delete requested for " + typeStr + " ID: " + id);
        }
    }
}