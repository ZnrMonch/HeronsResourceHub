package admin;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;
import items.*;

public class AdminTable extends CustomPanel {
	private static final long serialVersionUID = 1L;

	
	private TableType type;
	private JCheckBox archiveMode = new JCheckBox("Archive Mode");
	private CustomTable table;
	private CustomButton btnPrimaryAction;
	private CustomButton btnSecondaryAction;

	public AdminTable(TableType type) {
		this.type = type;
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
		else if (type == TableType.LOGS) titleText = "Logs Management";
		
		header.add(new CustomLabel(titleText, 18f, FontStyle.BOLD));
		header.add(Box.createHorizontalGlue());
		
		CustomSearchField searchField = new CustomSearchField("Search...", 25, 10);
		searchField.setCustomSize(200, 35);
		header.add(searchField);
		header.add(Box.createHorizontalStrut(10));
		
		header.add(new CustomLabel("Search by:"));
		header.add(Box.createHorizontalStrut(10));
		CustomComboBox<String> filterBox = new CustomComboBox<>(new String[]{"ID", "Column 1", "Column 2"});
		filterBox.setCustomSize(150, 35);
		header.add(filterBox);
		header.add(Box.createHorizontalStrut(10));
		
		if (type != TableType.LOGS) {
			archiveMode.setOpaque(false);
			if (FontLib.POPPINS_REGULAR != null) {
				archiveMode.setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
			}
			archiveMode.addItemListener(e -> updateActionUI());
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
			btnPrimaryAction.setDefaultColor(Color.decode("#ffc107")); // Yellow
			btnSecondaryAction.setText("Delete Data");
			btnSecondaryAction.setDefaultColor(Color.decode("#dc3545")); // Red
		} else {
			btnPrimaryAction.setText("Update Data");
			btnPrimaryAction.setDefaultColor(Color.decode("#28a745")); // Green
			btnSecondaryAction.setText("Archive Data");
			btnSecondaryAction.setDefaultColor(Color.decode("#ffc107")); // Yellow
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
			btnPrimaryAction = new CustomButton("Update Data", 8);
			btnSecondaryAction = new CustomButton("Archive Data", 8);
			btnPrimaryAction.setFontSize(12f);
			btnSecondaryAction.setFontSize(12f);
			btnPrimaryAction.setPadding(6, 12, 6, 12);
			btnSecondaryAction.setPadding(6, 12, 6, 12);
			updateActionUI();
			
			btnPrimaryAction.addActionListener(e -> {
				if (archiveMode.isSelected()) {
					handleRetrieve();
				} else {
					handleUpdate();
				}
			});

			btnSecondaryAction.addActionListener(e -> {
				if (archiveMode.isSelected()) {
					handleDelete();
				} else {
					handleArchive();
				}
			});
			
			table.getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
					updateActionUI();
				}
			});
		}
		
		Object[][] sampleData = new Object[25][columnNames.length];
		for (int i = 0; i < 25; i++) {
			if (type == TableType.USERS) {
				sampleData[i] = new Object[]{
					String.valueOf(i + 1), "K" + (1000000 + i), "Student" + i, "Last" + i, "CCIS", "3rd", String.valueOf(150 + (i * 10))
				};
			} else if (type == TableType.ITEMS) {
				sampleData[i] = new Object[]{
					String.valueOf(i + 1), "Item " + i, "Electronics", "10", "$50", "Available"
				};
			} else {
				sampleData[i] = new Object[]{
					String.valueOf(i + 1), "ACTION", "Log details " + i, "2026-05-11", "User " + i
				};
			}
		}
		
		table.setFullData(sampleData, columnNames);
		table.setRowHeight(30);
		table.setGridLines(true, false);
		
		table.setHeaderCustomization(FontLib.POPPINS_BOLD.deriveFont(14f), Color.BLACK, new Color(240, 240, 240));
		table.setBodyCustomization(FontLib.POPPINS_REGULAR.deriveFont(12f), Color.BLACK, Color.WHITE);
		
		table.setColumnWidth(0, 50);
		table.setColumnAlignment(0, SwingConstants.CENTER); // Center the ID column
		
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

	private void handleUpdate() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) return;
		
		int modelRow = table.convertRowIndexToModel(selectedRow);
		String id = table.getModel().getValueAt(modelRow, 0).toString();
		
		// Create form components
		CustomPanel formPanel = new CustomPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		
		if (type == TableType.USERS) {
			formPanel.add(createFieldPanel("First Name:"));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldPanel("Last Name:"));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldPanel("College:"));
		} else if (type == TableType.ITEMS) {
			formPanel.add(createFieldPanel("Item Name:"));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldPanel("Category:"));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldPanel("Stock:"));
			formPanel.add(Box.createVerticalStrut(10));
			formPanel.add(createFieldPanel("Price:"));
		}

		Window owner = SwingUtilities.getWindowAncestor(this);
		String typeStr = type == TableType.USERS ? "User" : "Item";
		AdminDialog dialog = new AdminDialog(owner, "Update " + typeStr + ": " + id, formPanel, "Save Changes", e -> {
			System.out.println("Update saved for ID: " + id);
			// Process inner fields logic here
		});
		dialog.setVisible(true);
	}

	private JPanel createFieldPanel(String label) {
		JPanel panel = new JPanel(new BorderLayout(10, 0));
		panel.setOpaque(false);
		panel.add(new CustomLabel(label, 14f, FontStyle.REGULAR), BorderLayout.WEST);
		panel.add(new JTextField(), BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		return panel;
	}

	private void handleArchive() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) return;
		
		int modelRow = table.convertRowIndexToModel(selectedRow);
		String id = table.getModel().getValueAt(modelRow, 0).toString();
		
		String typeStr = type == TableType.USERS ? "user" : "item";
		int confirm = JOptionPane.showConfirmDialog(this, 
			"Are you sure you want to archive " + typeStr + " " + id + "?", 
			"Confirm Archive", JOptionPane.YES_NO_OPTION);
			
		if (confirm == JOptionPane.YES_OPTION) {
			System.out.println("Archived " + typeStr + " ID: " + id);
		}
	}

	private void handleRetrieve() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) return;
		
		int modelRow = table.convertRowIndexToModel(selectedRow);
		String id = table.getModel().getValueAt(modelRow, 0).toString();
		
		String typeStr = type == TableType.USERS ? "user" : "item";
		int confirm = JOptionPane.showConfirmDialog(this, 
			"Are you sure you want to retrieve " + typeStr + " " + id + "?", 
			"Confirm Retrieve", JOptionPane.YES_NO_OPTION);
			
		if (confirm == JOptionPane.YES_OPTION) {
			System.out.println("Retrieved " + typeStr + " ID: " + id);
		}
	}

	private void handleDelete() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) return;
		
		int modelRow = table.convertRowIndexToModel(selectedRow);
		String id = table.getModel().getValueAt(modelRow, 0).toString();
		
		String typeStr = type == TableType.USERS ? "user" : "item";
		int confirm = JOptionPane.showConfirmDialog(this, 
			"Are you sure you want to permanently delete " + typeStr + " " + id + "?", 
			"Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			
		if (confirm == JOptionPane.YES_OPTION) {
			System.out.println("Deleted " + typeStr + " ID: " + id);
		}
	}
}