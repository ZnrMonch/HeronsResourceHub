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
	private CustomComboBox<String> categoryField;
	private CustomComboBox<String> conditionField;
	private CustomTextField pickupLocationField;
	
	// SELLING FIELDS
	private CustomSpinner priceField;
	private CustomComboBox<String> paymentMethodField;
	
	// LENDING FIELDS
	private CustomSpinner maximumBorrowDaysField;
	
	// TRADING FIELDS
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
		wrapper.add(Box.createVerticalStrut(10));
		
		String lowerTitle = getTitle() != null ? getTitle().toLowerCase() : "";
		
		if (lowerTitle.contains("sell")) {
			priceField = new CustomSpinner();
			priceField.setCustomSize(Integer.MAX_VALUE, 35);
			paymentMethodField = new CustomComboBox<>(new String[]{"Cash", "GCash", "Bank Transfer", "Other"});
			paymentMethodField.setCustomSize(Integer.MAX_VALUE, 35);
			
			wrapper.add(new CustomLabel("Price:"));
			wrapper.add(priceField);
			wrapper.add(Box.createVerticalStrut(10));
			wrapper.add(new CustomLabel("Payment Method:"));
			wrapper.add(paymentMethodField);
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