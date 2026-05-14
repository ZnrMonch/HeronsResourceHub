package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

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
	
	private CustomSpinner priceField;
	
	private CustomSpinner maximumBorrowDaysField;
	
	private CustomTextField desiredItemField;
	
	public ItemForm(JFrame parent, String title) {
		super(parent, title, true);
		setResizable(false);
		
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);
		((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		init();
		
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
	
	private CustomPanel initForm() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setPadding(0, 20);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		
		itemNameField = new CustomTextField();
		itemNameField.setCustomSize(Integer.MAX_VALUE, 35);
		descriptionField = new CustomTextArea(5, 20);
		descriptionField.setCustomSize(Integer.MAX_VALUE, 140);
		categoryField = new CustomComboBox<>(new String[]{"Electronics", "Books", "Clothing", "Home & Garden", "Toys", "Sports", "Other"});
		categoryField.setCustomSize(Integer.MAX_VALUE, 35);
		conditionField = new CustomComboBox<>(new String[]{"New", "Like New", "Good", "Fair", "Poor"});
		conditionField.setCustomSize(Integer.MAX_VALUE, 35);
		pickupLocationField = new CustomTextField();
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
		
		String[] hours = new String[] {
			"12:00 AM", "01:00 AM", "02:00 AM", "03:00 AM", "04:00 AM", "05:00 AM", 
			"06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", 
			"12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", 
			"06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM", "11:00 PM"
		};
		pickupTimeField = new CustomSpinner(new SpinnerListModel(hours));
		pickupTimeField.setCustomSize(Integer.MAX_VALUE, 35);
		
		wrapper.add(new CustomLabel("Item Name:"));
		wrapper.add(itemNameField);
		wrapper.add(Box.createVerticalStrut(10));
		wrapper.add(new CustomLabel("Description:"));
		wrapper.add(descriptionField);
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(createRow("Category:", categoryField, "Condition:", conditionField));
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(new CustomLabel("Pickup Location:"));
		wrapper.add(pickupLocationField);
		wrapper.add(Box.createVerticalStrut(10));
		
		wrapper.add(createRow("Pickup Date:", pickupDateField, "Pickup Time:", pickupTimeField));
		wrapper.add(Box.createVerticalStrut(10));
		
		String lowerTitle = getTitle() != null ? getTitle().toLowerCase() : "";
		
		if (lowerTitle.contains("sell")) {
			priceField = new CustomSpinner();
			priceField.setCustomSize(Integer.MAX_VALUE, 35);
			
			wrapper.add(new CustomLabel("Price:"));
			wrapper.add(priceField);
			wrapper.add(Box.createVerticalStrut(10));
		} else if (lowerTitle.contains("lend")) {
			maximumBorrowDaysField = new CustomSpinner();
			maximumBorrowDaysField.setCustomSize(Integer.MAX_VALUE, 35);
			
			wrapper.add(new CustomLabel("Maximum Borrow Days:"));
			wrapper.add(maximumBorrowDaysField);
			wrapper.add(Box.createVerticalStrut(10));
		} else if (lowerTitle.contains("offer") || lowerTitle.contains("trade")) {
			desiredItemField = new CustomTextField();
			desiredItemField.setCustomSize(Integer.MAX_VALUE, 35);
			
			wrapper.add(new CustomLabel("Desired Item in Return:"));
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
	
	private CustomPanel createRow(String label1, JComponent field1, String label2, JComponent field2) {
		CustomPanel row = new CustomPanel(new GridLayout(1, 2, 10, 0));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); 
		
		CustomPanel col1 = new CustomPanel();
		col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
		CustomLabel l1 = new CustomLabel(label1);
		l1.setAlignmentX(Component.LEFT_ALIGNMENT);
		field1.setAlignmentX(Component.LEFT_ALIGNMENT);
		col1.add(l1);
		col1.add(field1);
		
		CustomPanel col2 = new CustomPanel();
		col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
		CustomLabel l2 = new CustomLabel(label2);
		l2.setAlignmentX(Component.LEFT_ALIGNMENT);
		field2.setAlignmentX(Component.LEFT_ALIGNMENT);
		col2.add(l2);
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
		
		CustomButton submitButton = new CustomButton("Submit");
		submitButton.setRadius(10);
		submitButton.setPadding(5, 10, 5, 10);
		
		wrapper.add(cancelButton);
		wrapper.add(submitButton);
		return wrapper;
	}

}