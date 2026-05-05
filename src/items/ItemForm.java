package items;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class ItemForm extends JDialog {
	private static final long serialVersionUID = 1L;
	
	// GENERAL FIELDS
	private CustomTextField itemNameField;
	private CustomTextArea descriptionField;
	private CustomComboBox categoryField;
	private CustomComboBox conditionField;
	private CustomTextField pickupLocationField;
	
	// SELLING FIELDS
	private CustomSpinner priceField;
	private CustomComboBox paymentMethodField;
	
	// LENDING FIELDS
	private CustomSpinner maximumBorrowDaysField;
	
	// TRADING FIELDS
	private CustomTextField desiredItemField;
	
	
	public ItemForm(JFrame parent, String title) {
		super(parent, title, true);
        setSize(500, 600);
        setResizable(false);
        setLocationRelativeTo(parent);
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
		
		init();
		setVisible(true);
	}
	
	private void init() {
		add(initHeader(), BorderLayout.NORTH);
		add(initForm(), BorderLayout.SOUTH);
	}
	
	private CustomPanel initHeader() {
		CustomPanel wrapper = new CustomPanel(new GridBagLayout());
		
		wrapper.setPadding(30);
		wrapper.add(new CustomLabel("MARKETPLACE | SELLING", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
	
		return wrapper;
	}
	
	private CustomPanel initForm() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
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
		
		wrapper.add(new CustomLabel("Item Name:"));
		wrapper.add(itemNameField);
		wrapper.add(Box.createVerticalStrut(10));
		wrapper.add(new CustomLabel("Description:"));
		wrapper.add(descriptionField);
		wrapper.add(Box.createVerticalStrut(10));
		wrapper.add(new CustomLabel("Category:"));
		wrapper.add(categoryField);
		wrapper.add(Box.createVerticalStrut(10));
		wrapper.add(new CustomLabel("Condition:"));
		wrapper.add(conditionField);
		wrapper.add(Box.createVerticalStrut(10));
		wrapper.add(new CustomLabel("Pickup Location:"));
		wrapper.add(pickupLocationField);

		for (Component c : wrapper.getComponents()) {
			if (c instanceof JComponent) {
				((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
			}
		}

		return wrapper;
	}

}