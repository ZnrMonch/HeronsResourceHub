package marketplace;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import components.*;
import utils.*;
import enums.Category;
import enums.Condition;
import database.ItemRecord;
import database.DatabaseManager;

public class ItemForm extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private CustomTextField itemNameField;
	private CustomTextArea descriptionField;
	private CustomComboBox<String> categoryField;
	private CustomComboBox<String> conditionField;
	private CustomTextField pickupLocationField;
	
	private CustomPanel pickupDateField;
	private JCheckBox[] pickupDayBoxes;
	
	private CustomSpinner pickupTimeField;
	
	private CustomTextField priceField;
	private CustomSpinner maximumBorrowDaysField;
	private CustomTextField desiredItemField;
	
	private ItemRecord currentItem;
	private final String DB_URL = DatabaseManager.getURL();
	private final String USER = DatabaseManager.getUser();
	private final String PASSWORD = DatabaseManager.getPassword();
	
	public ItemForm(Window parent, String title, ItemRecord itemToUpdate) {
		super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
		this.currentItem = itemToUpdate;
		setResizable(false);
		
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);
		((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		init();
		
		if (currentItem != null) {
			preFillData();
		}
		
		pack();
		setSize(500, getPreferredSize().height);
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private void init() {
		add(initHeader(), BorderLayout.NORTH);
		add(initForm(), BorderLayout.CENTER);
		add(initAction(), BorderLayout.SOUTH);
	}
	
	private CustomPanel initHeader() {
		CustomPanel wrapper = new CustomPanel(new GridBagLayout());
		wrapper.add(new CustomLabel(getTitle() != null ? getTitle().toUpperCase() : "ITEM FORM", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
		return wrapper;
	}
	
	private CustomPanel createLabel(String text, boolean isRequired) {
		CustomPanel panel = new CustomPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(new CustomLabel(text, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD));
		if (isRequired) {
			panel.add(Box.createHorizontalStrut(3));
			panel.add(new CustomLabel("*", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD, Brand.RED));
		}
		return panel;
	}
	
	private CustomPanel initForm() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setPadding(0, 20);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		
		itemNameField = new CustomTextField("Enter item name...");
		itemNameField.setCustomSize(Integer.MAX_VALUE, 35);
		
		descriptionField = new CustomTextArea("Provide a detailed description...", 5, 20);
		descriptionField.setCustomSize(Integer.MAX_VALUE, 140);
		
		String[] categories = new String[Category.values().length];
		for (int i = 0; i < Category.values().length; i++) {
			categories[i] = Category.values()[i].name().replace("_", " ");
		}
		categoryField = new CustomComboBox<>(categories);
		categoryField.setCustomSize(Integer.MAX_VALUE, 35);
		
		String[] conditions = new String[Condition.values().length];
		for (int i = 0; i < Condition.values().length; i++) {
			conditions[i] = Condition.values()[i].name();
		}
		conditionField = new CustomComboBox<>(conditions);
		conditionField.setCustomSize(Integer.MAX_VALUE, 35);
		
		pickupLocationField = new CustomTextField("e.g., CCIS Lobby");
		pickupLocationField.setCustomSize(Integer.MAX_VALUE, 35);
		
		pickupDateField = new CustomPanel(new GridLayout(2, 4, 0, 0));
		pickupDateField.setOpaque(false);
		String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "All"};
		pickupDayBoxes = new JCheckBox[days.length];
		
		for (int i = 0; i < days.length; i++) {
			pickupDayBoxes[i] = new JCheckBox(days[i]);
			pickupDayBoxes[i].setOpaque(false);
			pickupDayBoxes[i].setFocusPainted(false);
			pickupDayBoxes[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			if (FontLib.POPPINS_REGULAR != null) {
				pickupDayBoxes[i].setFont(FontLib.POPPINS_REGULAR.deriveFont(12f));
			}
			pickupDateField.add(pickupDayBoxes[i]);
		}
		
		pickupDayBoxes[7].addActionListener(e -> {
			boolean isSelected = pickupDayBoxes[7].isSelected();
			for (int i = 0; i < 7; i++) {
				pickupDayBoxes[i].setSelected(isSelected);
			}
		});
		
		for (int i = 0; i < 7; i++) {
			pickupDayBoxes[i].addActionListener(e -> {
				boolean allChecked = true;
				for (int j = 0; j < 7; j++) {
					if (!pickupDayBoxes[j].isSelected()) {
						allChecked = false;
						break;
					}
				}
				pickupDayBoxes[7].setSelected(allChecked);
			});
		}
		
		String[] hours = new String[] {
			"12:00 AM", "01:00 AM", "02:00 AM", "03:00 AM", "04:00 AM", "05:00 AM", 
			"06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", 
			"12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", 
			"06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM", "11:00 PM"
		};
		pickupTimeField = new CustomSpinner(new SpinnerListModel(hours));
		pickupTimeField.setCustomSize(Integer.MAX_VALUE, 35);
		
		wrapper.add(createLabel("Item Name:", true));
		wrapper.add(itemNameField);
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(createLabel("Description:", true));
		wrapper.add(descriptionField);
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(createRow("Category:", categoryField, true, "Condition:", conditionField, true));
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(createLabel("Pickup Location:", true));
		wrapper.add(pickupLocationField);
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(createRow("Pickup Date:", pickupDateField, true, "Pickup Time:", pickupTimeField, true));
		wrapper.add(Box.createVerticalStrut(10));
		
		String lowerTitle = getTitle() != null ? getTitle().toLowerCase() : "";
		boolean isMarketplace = currentItem != null ? (currentItem.action.toLowerCase().contains("marketplace")) : lowerTitle.contains("sell");
		boolean isSharing = currentItem != null ? (currentItem.action.toLowerCase().contains("sharing")) : lowerTitle.contains("lend");
		boolean isTrade = currentItem != null ? (currentItem.action.toLowerCase().contains("trade")) : (lowerTitle.contains("offer") || lowerTitle.contains("trade"));
		
		if (isMarketplace) {
			priceField = new CustomTextField("e.g., 500");
			priceField.setCustomSize(Integer.MAX_VALUE, 35);
			wrapper.add(createLabel("Price:", true));
			wrapper.add(priceField);
			wrapper.add(Box.createVerticalStrut(10));
		} else if (isSharing) {
			maximumBorrowDaysField = new CustomSpinner(new SpinnerNumberModel(1, 1, 365, 1));
			maximumBorrowDaysField.setCustomSize(Integer.MAX_VALUE, 35);
			wrapper.add(createLabel("Maximum Borrow Days:", true));
			wrapper.add(maximumBorrowDaysField);
			wrapper.add(Box.createVerticalStrut(10));
		} else if (isTrade) {
			desiredItemField = new CustomTextField("e.g., Scientific Calculator");
			desiredItemField.setCustomSize(Integer.MAX_VALUE, 35);
			wrapper.add(createLabel("Desired Item in Return:", true));
			wrapper.add(desiredItemField);
			wrapper.add(Box.createVerticalStrut(10));
		}

		for (Component c : wrapper.getComponents()) {
			if (c instanceof JComponent) {
				((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
			}
		}

		return wrapper;
	}
	
	private void preFillData() {
		itemNameField.setText(currentItem.itemName);
		descriptionField.setText(currentItem.description);
		categoryField.setSelectedItem(currentItem.category.replace("_", " "));
		conditionField.setSelectedItem(currentItem.condition);
		pickupLocationField.setText(currentItem.pickupArea);
		pickupTimeField.setValue(currentItem.pickupTime);
		
		if (currentItem.pickupDays != null) {
			String[] daysSelected = currentItem.pickupDays.split(",");
			for (String d : daysSelected) {
				String prefix = d.trim().substring(0, 3);
				for (int i = 0; i < 7; i++) {
					if (pickupDayBoxes[i].getText().equalsIgnoreCase(prefix)) {
						pickupDayBoxes[i].setSelected(true);
					}
				}
			}
		}
		
		if (priceField != null && currentItem.price > 0) priceField.setText(String.valueOf(currentItem.price));
		if (maximumBorrowDaysField != null && currentItem.maximumBorrowDays > 0) maximumBorrowDaysField.setValue(currentItem.maximumBorrowDays);
		if (desiredItemField != null && currentItem.desiredItem != null) desiredItemField.setText(currentItem.desiredItem);
	}
	
	private CustomPanel createRow(String label1, JComponent field1, boolean req1, String label2, JComponent field2, boolean req2) {
		CustomPanel row = new CustomPanel(new GridLayout(1, 2, 10, 0));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); 
		
		CustomPanel col1 = new CustomPanel();
		col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
		col1.add(createLabel(label1, req1));
		field1.setAlignmentX(Component.LEFT_ALIGNMENT);
		col1.add(field1);
		
		CustomPanel col2 = new CustomPanel();
		col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
		col2.add(createLabel(label2, req2));
		field2.setAlignmentX(Component.LEFT_ALIGNMENT);
		col2.add(field2);
		
		row.add(col1);
		row.add(col2);
		
		return row;
	}
	
	private CustomPanel initAction() {
		CustomPanel wrapper = new CustomPanel(new FlowLayout(FlowLayout.CENTER));
		
		CustomButton cancelButton = new CustomButton("Cancel");
		cancelButton.setRadius(10);
		cancelButton.setPadding(5, 10, 5, 10);
		cancelButton.addActionListener(e -> dispose());
		
		CustomButton submitButton = new CustomButton(currentItem != null ? "Update" : "Submit");
		submitButton.setRadius(10);
		submitButton.setPadding(5, 10, 5, 10);
		submitButton.addActionListener(e -> {
			if (validateForm()) {
				if (currentItem != null) {
					updateItemInDatabase();
				}
				JOptionPane.showMessageDialog(this, "Item successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
				dispose();
			}
		});
		
		wrapper.add(cancelButton);
		wrapper.add(submitButton);
		return wrapper;
	}

	private boolean validateForm() {
		if (itemNameField.getText().trim().isEmpty() || itemNameField.getText().equals("Enter item name...")) {
			showError("Item Name is required.");
			return false;
		}
		if (descriptionField.getText().trim().isEmpty() || descriptionField.getText().equals("Provide a detailed description...")) {
			showError("Description is required.");
			return false;
		}
		if (pickupLocationField.getText().trim().isEmpty() || pickupLocationField.getText().equals("e.g., CCIS Lobby")) {
			showError("Pickup Location is required.");
			return false;
		}

		boolean daySelected = false;
		for (int i = 0; i < 7; i++) {
			if (pickupDayBoxes[i].isSelected()) {
				daySelected = true;
				break;
			}
		}
		if (!daySelected) {
			showError("Please select at least one pickup day.");
			return false;
		}

		if (priceField != null) {
			String priceText = priceField.getText().trim();
			if (priceText.isEmpty() || priceText.equals("e.g., 500")) {
				showError("Price is required.");
				return false;
			}
			try {
				if (Double.parseDouble(priceText) <= 0) {
					showError("Price must be greater than zero.");
					return false;
				}
			} catch (NumberFormatException ex) {
				showError("Invalid price format.");
				return false;
			}
		} else if (desiredItemField != null) {
			if (desiredItemField.getText().trim().isEmpty() || desiredItemField.getText().equals("e.g., Scientific Calculator")) {
				showError("Desired Item in Return is required.");
				return false;
			}
		}
		return true;
	}

	private void updateItemInDatabase() {
		StringBuilder days = new StringBuilder();
		String[] fullDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
		for (int i = 0; i < 7; i++) {
			if (pickupDayBoxes[i].isSelected()) {
				if (days.length() > 0) days.append(",");
				days.append(fullDays[i]);
			}
		}
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			 PreparedStatement pstmt = conn.prepareStatement(
				 "UPDATE items SET item_name=?, description=?, category=?, condition=?, pickup_area=?, pickup_days=?, pickup_time=?, price=?, maximum_borrow_days=?, desired_item=? WHERE item_id=?")) {
			
			pstmt.setString(1, itemNameField.getText().trim());
			pstmt.setString(2, descriptionField.getText().trim());
			pstmt.setString(3, categoryField.getSelectedItem().toString().replace(" ", "_"));
			pstmt.setString(4, conditionField.getSelectedItem().toString());
			pstmt.setString(5, pickupLocationField.getText().trim());
			pstmt.setString(6, days.toString());
			pstmt.setString(7, pickupTimeField.getValue().toString());
			
			if (priceField != null) {
				pstmt.setInt(8, Integer.parseInt(priceField.getText().trim()));
			} else {
				pstmt.setNull(8, Types.INTEGER);
			}
			
			if (maximumBorrowDaysField != null) {
				pstmt.setInt(9, (Integer) maximumBorrowDaysField.getValue());
			} else {
				pstmt.setNull(9, Types.INTEGER);
			}
			
			if (desiredItemField != null) {
				pstmt.setString(10, desiredItemField.getText().trim());
			} else {
				pstmt.setNull(10, Types.VARCHAR);
			}
			
			pstmt.setInt(11, currentItem.itemId);
			pstmt.executeUpdate();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			showError("Database Error: " + ex.getMessage());
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
	}
}