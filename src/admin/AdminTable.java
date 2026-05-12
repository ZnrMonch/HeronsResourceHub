package admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import components.*;
import utils.*;
import java.util.List;

public class AdminTable extends CustomPanel {
	private static final long serialVersionUID = 1L;
	private AdminService service;
	private String logType = "USER";

	public enum TableType {
		USERS, ITEMS, LOGS
	}

	private TableType type;
	private JCheckBox archiveMode = new JCheckBox("Archive Mode");
	private CustomTable table;
	private CustomButton btnPrimaryAction;
	private CustomButton btnSecondaryAction;
	private CustomSearchField searchField;
	private CustomComboBox<String> filterBox;
	private Object[] columnNames;

	public AdminTable(TableType type, AdminService service) {
		this(type, service, "USER/ITEMS");
	}

	public AdminTable(TableType type, AdminService service, String logType) {
		this.type = type;
		this.service = service;
		this.logType = logType;
		setLayout(new BorderLayout());
		add(initHeader(), BorderLayout.NORTH);
		add(initTable(), BorderLayout.CENTER);
	}

	private Object[] getColumnNames(TableType type) {
		return switch (type) {
		case USERS -> new Object[] { "ID", "Student ID", "First Name", "Last Name", "College", "Year", "Karma Points" };
		case ITEMS -> new Object[] { "ID", "Item Name", "Category", "Stock", "Price", "Status" };
		case LOGS -> new Object[] { "ID", "Description", "Date", "User ID" };
		};
	}

	private Object[][] getRealData(String search, String filter) {
		List<Object[]> dataList = switch (type) {
		case USERS -> service.searchUsers(search, filter, 0, 25);
		case ITEMS -> service.searchItems(search, filter, 0, 25);
		default -> service.searchLogs(logType, search, 0, 25);
		};
		return dataList.toArray(new Object[0][]);
	}

	private CustomPanel initHeader() {
		CustomPanel header = new CustomPanel();
		header.setPadding(20, 15);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

		String titleText = switch (type) {
		case USERS -> "User Management";
		case ITEMS -> "Item Management";
		case LOGS -> "Logs Management";
		};

		header.add(new CustomLabel(titleText, 18f, FontStyle.BOLD));
		header.add(Box.createHorizontalGlue());

		searchField = new CustomSearchField("Search...", 25, 10);
		searchField.setCustomSize(200, 35);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				updateTableData();
			}

			public void removeUpdate(DocumentEvent e) {
				updateTableData();
			}

			public void changedUpdate(DocumentEvent e) {
				updateTableData();
			}
		});
		header.add(searchField);
		header.add(Box.createHorizontalStrut(10));

		header.add(new CustomLabel("Search by:"));
		header.add(Box.createHorizontalStrut(10));
		String[] filterOptions = switch (type) {
		case USERS -> new String[] { "ID", "Student ID", "First Name", "Last Name", "College", "Year", "Karma Points" };
		case ITEMS -> new String[] { "ID", "Item Name", "Category", "Stock", "Price", "Status" };
		case LOGS -> new String[] { "ID", "Log Type", "Description", "Date", "User ID" };
		};

		filterBox = new CustomComboBox<>(filterOptions);
		filterBox.setCustomSize(150, 35);
		filterBox.addActionListener(e -> updateTableData());
		header.add(filterBox);
		header.add(Box.createHorizontalStrut(10));

		if (type != TableType.LOGS) {
			archiveMode.setOpaque(false);
			if (FontLib.POPPINS_REGULAR != null) {
				archiveMode.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
			}
			archiveMode.addItemListener(e -> {
				updateTableData();
				updateActionUI();
			});

			header.add(archiveMode);
		}
		return header;
	}

	private void updateTableData() {
		if (table == null || searchField == null || filterBox == null)
			return;

		String search = searchField.getText();
		String filter = filterBox.getSelectedItem().toString();
		List<Object[]> dataList;

		if (type == TableType.USERS) {
			if (archiveMode.isSelected()) {
				dataList = service.searchArchivedUsers(search, filter, 0, 25);
			} else {
				dataList = service.searchUsers(search, filter, 0, 25);
			}
		} else if (type == TableType.ITEMS) {
			if (archiveMode.isSelected()) {
				dataList = service.searchArchivedItems(search, filter, 0, 25);
			} else {
				dataList = service.searchItems(search, filter, 0, 25);
			}
		} else {
			dataList = service.searchLogs(logType, search, 0, 25);
		}

		table.setFullData(dataList.toArray(new Object[0][]), columnNames);
	}

	private void updateActionUI() {
		if (btnPrimaryAction == null || btnSecondaryAction == null || table == null)
			return;

		boolean hasSelection = table.getSelectedRow() != -1;
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

		table = new CustomTable();
		columnNames = getColumnNames(type);
		Object[][] realData = getRealData("", filterBox != null ? filterBox.getSelectedItem().toString() : "ID");
		table.setFullData(realData, columnNames);

		if (type != TableType.LOGS) {
			btnPrimaryAction = new CustomButton("Update Data", 8);
			btnSecondaryAction = new CustomButton("Archive Data", 8);
			btnPrimaryAction.setFontSize(12f);
			btnSecondaryAction.setFontSize(12f);
			btnPrimaryAction.setPadding(6, 12, 6, 12);
			btnSecondaryAction.setPadding(6, 12, 6, 12);
			updateActionUI();

			btnPrimaryAction.addActionListener(e -> {
				if (archiveMode.isSelected())
					handleRetrieve();
				else
					handleUpdate();
			});

			btnSecondaryAction.addActionListener(e -> {
				if (archiveMode.isSelected())
					handleDelete();
				else
					handleArchive();
			});

			table.getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting())
					updateActionUI();
			});
		}

		table.setRowHeight(30);
		table.setGridLines(true, false);
		table.setHeaderCustomization(FontLib.POPPINS_BOLD.deriveFont(14f), Color.BLACK, new Color(240, 240, 240));
		table.setBodyCustomization(FontLib.POPPINS_REGULAR.deriveFont(12f), Color.BLACK, Color.WHITE);

		// Column configuration
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
		} else if (type == TableType.ITEMS) {
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

	private void handleUpdate() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1)
			return;

		int modelRow = table.convertRowIndexToModel(selectedRow);
		int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

		CustomPanel formPanel = new CustomPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

		if (type == TableType.USERS) {
			String currentFirst = table.getModel().getValueAt(modelRow, 2).toString();
			String currentLast = table.getModel().getValueAt(modelRow, 3).toString();
			String currentCollege = table.getModel().getValueAt(modelRow, 4).toString();

			JTextField fnField = new CustomTextField(currentFirst);
			JTextField lnField = new CustomTextField(currentLast);
			JComboBox<String> collegeBox = new CustomComboBox<>(service.getColleges());
			collegeBox.setSelectedItem(currentCollege);

			formPanel.add(createFieldRow("First Name:", fnField));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldRow("Last Name:", lnField));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldRow("College:", collegeBox));

			Window owner = SwingUtilities.getWindowAncestor(this);
			AdminDialog dialog = new AdminDialog(owner, "Update User: " + id, formPanel, "Save Changes", e -> {
				String fn = fnField.getText().trim();
				String ln = lnField.getText().trim();
				String col = collegeBox.getSelectedItem().toString();

				boolean success = service.updateUser(id, fn, ln, col);
				if (success) {
					JOptionPane.showMessageDialog(this, "User updated successfully.");
					updateTableData();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to update user. Please try again.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			dialog.setVisible(true);

		} else if (type == TableType.ITEMS) {
			String currentName = table.getModel().getValueAt(modelRow, 1).toString();
			String currentCategory = table.getModel().getValueAt(modelRow, 2).toString();
			String currentStock = table.getModel().getValueAt(modelRow, 3).toString();
			String currentPrice = table.getModel().getValueAt(modelRow, 4).toString();

			JTextField nameField = new CustomTextField(currentName);
			JTextField categoryField = new CustomTextField(currentCategory);
			JTextField stockField = new CustomTextField(currentStock);
			JTextField priceField = new CustomTextField(currentPrice);

			formPanel.add(createFieldRow("Item Name:", nameField));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldRow("Category:", categoryField));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldRow("Stock:", stockField));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldRow("Price:", priceField));

			Window owner = SwingUtilities.getWindowAncestor(this);
			AdminDialog dialog = new AdminDialog(owner, "Update Item: " + id, formPanel, "Save Changes", e -> {
				String name = nameField.getText().trim();
				String category = categoryField.getText().trim();
				String stockStr = stockField.getText().trim();
				String priceStr = priceField.getText().trim();
				
				int stock, price;
				try {
					stock = Integer.parseInt(stockStr);
					price = Integer.parseInt(priceStr);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Stock and Price must be valid numbers.", "Validation Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				boolean success = service.updateItem(id, name, category, stock, price);
				if (success) {
					JOptionPane.showMessageDialog(this, "Item updated successfully.");
					updateTableData();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to update item. Please try again.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			dialog.setVisible(true);
		}
	}

	private JPanel createFieldRow(String label, JComponent input) {
		JPanel panel = new JPanel(new BorderLayout(10, 0));
		panel.setOpaque(false);
		panel.add(new CustomLabel(label, 14f, FontStyle.REGULAR), BorderLayout.WEST);
		panel.add(input, BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
		return panel;
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
	           // Not yet implemented.
	        } else if (type == TableType.ITEMS) {
	        	// Not yet implemented.
	        }

	        if (success) {
	            JOptionPane.showMessageDialog(this, typeStr + " archived successfully.");
	        } else {
	            JOptionPane.showMessageDialog(this,
	                "Failed to archive " + typeStr + ".", "Error", JOptionPane.ERROR_MESSAGE);
	        }
	        updateTableData();
	    }
	}

	private void handleRetrieve() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1)
			return;

		int modelRow = table.convertRowIndexToModel(selectedRow);
		int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

		String typeStr = type == TableType.USERS ? "user" : "item";
		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to retrieve " + typeStr + " " + id + "?", "Confirm Retrieve",
				JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			if (type == TableType.USERS) {
				service.retrieveUser(id);
			} else if (type == TableType.ITEMS) {
				service.retrieveItem(id);
			}
			updateTableData();
		}
	}

	private void handleDelete() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1)
			return;

		int modelRow = table.convertRowIndexToModel(selectedRow);
		int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());

		String typeStr = type == TableType.USERS ? "user" : "item";
		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to permanently delete " + typeStr + " " + id + "?", "Confirm Delete",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			if (type == TableType.USERS) {
				service.deleteUser(id);
			} else if (type == TableType.ITEMS) {
				service.deleteItem(id);
			}
			updateTableData();
		}
	}

}