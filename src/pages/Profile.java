package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class Profile extends CustomPanel {
	private static final long serialVersionUID = 1L;
	
	private CustomToggleButton editProfileButton = new CustomToggleButton(
		IconLoader.loadAndScaleIcon("/resources/icons/edit.png", 25, 25),
		IconLoader.loadAndScaleIcon("/resources/icons/close.png", 25, 25)
	);
	
	private boolean isEditing = false;
	private CustomPanel profileContentPanel;
	
	private String studentId = "K12360080";
	private String lastName = "Moncinilla";
	private String firstName = "Renzjan";
	private String contactNumber = "095118391870";
	private String email = "renzjan.moncinilla@umak.edu.ph";
	private String memberSince = "January 01, 2000";
	private String password = "*";
	
	private String college = "CCIS";
	private String course = "BS in Information Technology";
	private String yearLevel = "1st Year";
	
	private String gcash = "09********0";
	private String maya = "09********0";
	private String mastercard = "09********0";
	private String visa = "09********0";
	
	private CustomTextField studentIdField = new CustomTextField("");
	private CustomTextField lastNameField = new CustomTextField("");
	private CustomTextField firstNameField = new CustomTextField("");
	private CustomTextField contactNumberField = new CustomTextField("");
	
	private CustomTextField collegeField = new CustomTextField("");
	private CustomTextField courseField = new CustomTextField("");
	private CustomTextField yearLevelField = new CustomTextField("");
	
	private CustomTextField gcashField = new CustomTextField("");
	private CustomTextField mayaField = new CustomTextField("");
	private CustomTextField mastercardField = new CustomTextField("");
	private CustomTextField visaField = new CustomTextField("");
	
	public Profile() {
		setLayout(new BorderLayout(20, 20));
		add(initWest(), BorderLayout.WEST);
		add(initCenter(), BorderLayout.CENTER);
	}
	
	private CustomPanel initWest() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout(0, 20));
		wrapper.setPreferredSize(new Dimension(300, Integer.MAX_VALUE));
		
		CustomPanel profilePictureWrapper = new CustomPanel(new GridBagLayout());
		profilePictureWrapper.setPreferredSize(new Dimension(300, 300));
		profilePictureWrapper.add(new JLabel(IconLoader.loadAndScaleCircularIcon("/resources/defaultpictures/renzjan.jpg", 250, 250)));
		
		CustomPanel karmaWrapper = new CustomPanel(new BorderLayout());
		karmaWrapper.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20)); 
		karmaWrapper.setRadius(20);
		karmaWrapper.setBackground(Color.WHITE);
		
		CustomLabel karmaLabel = new CustomLabel("KARMA POINTS", 34f, FontStyle.BOLD);
		karmaLabel.setHorizontalAlignment(SwingConstants.CENTER);
		CustomLabel karmaPointsLabel = new CustomLabel("67", 120f, FontStyle.BOLD);
		karmaPointsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		CustomLabel karmaDescLabel = new CustomLabel("Your Community Standing", 18f, FontStyle.REGULAR);
		karmaDescLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		karmaPointsLabel.setForeground(Brand.SECONDARY_COLOR);
		karmaDescLabel.setForeground(Color.GRAY);
		
		karmaWrapper.add(karmaLabel, BorderLayout.NORTH);
		karmaWrapper.add(karmaPointsLabel, BorderLayout.CENTER);
		karmaWrapper.add(karmaDescLabel, BorderLayout.SOUTH);
		
		wrapper.add(profilePictureWrapper, BorderLayout.NORTH);
		wrapper.add(karmaWrapper, BorderLayout.CENTER);
		return wrapper;
	}
	
	private CustomTabbedPane initCenter() {
		CustomTabbedPane tabbedPane = new CustomTabbedPane();
		tabbedPane.setRadius(20);
		
		tabbedPane.addTab("Profile", "/resources/icons/profile.png", initProfileTab());
		tabbedPane.addTab("Transaction History", new CustomPanel());
		
		return tabbedPane;
	}
	
	private CustomPanel initProfileTab() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout(0, 30));
		wrapper.setPadding(20);
		
		CustomPanel topWrapper = new CustomPanel();
		topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.X_AXIS));
		topWrapper.add(new CustomLabel("Personal Information", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
		editProfileButton.setDefaultColor(new Color(0, 0, 0, 0));
		editProfileButton.setHoverColor(new Color(0, 0, 0, 0));
		topWrapper.add(Box.createHorizontalStrut(10));
		topWrapper.add(editProfileButton);
		
		editProfileButton.addToggleListener(toggled -> {
			if (!toggled && isEditing) {
				// Define your custom buttons here. No "Cancel" button is included.
				Object[] options = {"Save Changes", "Discard"};
				
				int choice = JOptionPane.showOptionDialog(
					this,
					"Do you want to save the changes made to your profile?",
					"Confirm Changes",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options, // Applies the custom text array
					options[0] // Default button focus
				);
				
				if (choice == JOptionPane.YES_OPTION) { // Maps to index 0 ("Save Changes")
					saveFields();
					isEditing = false;
					buildProfileFields();
				} else if (choice == JOptionPane.NO_OPTION) { // Maps to index 1 ("Discard")
					isEditing = false;
					buildProfileFields();
				} else {
					// If the user clicks the 'X' to close the window (JOptionPane.CLOSED_OPTION)
					// It safely keeps them in the edit screen.
					SwingUtilities.invokeLater(() -> editProfileButton.setToggled(true));
				}
			} else if (toggled && !isEditing) {
				isEditing = true;
				buildProfileFields();
			}
		});
		
		profileContentPanel = new CustomPanel(new BorderLayout());
		buildProfileFields(); 
		
		wrapper.add(topWrapper, BorderLayout.NORTH);
		wrapper.add(profileContentPanel, BorderLayout.CENTER);
		
		return wrapper;
	}
	
	private void saveFields() {
		studentId = studentIdField.getText();
		lastName = lastNameField.getText();
		firstName = firstNameField.getText();
		contactNumber = contactNumberField.getText();
		college = collegeField.getText();
		course = courseField.getText();
		yearLevel = yearLevelField.getText();
		gcash = gcashField.getText();
		maya = mayaField.getText();
		mastercard = mastercardField.getText();
		visa = visaField.getText();
	}
	
	private void buildProfileFields() {
		profileContentPanel.removeAll();
		
		CustomPanel infoWrapper = new CustomPanel();
		CustomPanel infoWrapper1 = new CustomPanel();
		CustomPanel infoWrapper2 = new CustomPanel();
		
		infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.X_AXIS));
		infoWrapper1.setLayout(new BoxLayout(infoWrapper1, BoxLayout.Y_AXIS));
		infoWrapper2.setLayout(new BoxLayout(infoWrapper2, BoxLayout.Y_AXIS));		
		infoWrapper1.setAlignmentY(Component.TOP_ALIGNMENT);
		infoWrapper2.setAlignmentY(Component.TOP_ALIGNMENT);
		
		infoWrapper1.add(infoWrapper("Student ID", studentId, studentIdField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Last Name", lastName, lastNameField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("First Name", firstName, firstNameField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Contact Number", contactNumber, contactNumberField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		
		infoWrapper1.add(infoWrapper("UMak Email Address", email, null, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Member Since", memberSince, null, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Password", password, null, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		
		infoWrapper2.add(infoWrapper("College/Institute", college, collegeField, true));
		infoWrapper2.add(Box.createVerticalStrut(15));
		infoWrapper2.add(infoWrapper("Course/Program", course, courseField, true));
		infoWrapper2.add(Box.createVerticalStrut(15));
		infoWrapper2.add(infoWrapper("Year Level", yearLevel, yearLevelField, true));
		infoWrapper2.add(Box.createVerticalStrut(30));
		infoWrapper2.add(new CustomLabel("Payment Information", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD));
		infoWrapper2.add(Box.createVerticalStrut(5));
		
		CustomPanel paymentInfoPanel = new CustomPanel(new GridLayout(0, 2));
		CustomPanel paymentInfoPanel1 = new CustomPanel();
		CustomPanel paymentInfoPanel2 = new CustomPanel();
		
		paymentInfoPanel1.setLayout(new BoxLayout(paymentInfoPanel1, BoxLayout.Y_AXIS));
		paymentInfoPanel2.setLayout(new BoxLayout(paymentInfoPanel2, BoxLayout.Y_AXIS));
		
		CustomLabel onlinePaymentTitle = new CustomLabel("Online Payment");
		onlinePaymentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		paymentInfoPanel1.add(onlinePaymentTitle);
		paymentInfoPanel1.add(Box.createVerticalStrut(10));
		paymentInfoPanel1.add(paymentInfoWrapper("/resources/icons/gcash.png", "GCash", gcash, gcashField, true));
		paymentInfoPanel1.add(Box.createVerticalStrut(10));
		paymentInfoPanel1.add(paymentInfoWrapper("/resources/icons/maya.png", "Maya", maya, mayaField, true));
		
		CustomLabel paymentNetworkTitle = new CustomLabel("Payment Network");
		paymentNetworkTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		paymentInfoPanel2.add(paymentNetworkTitle);
		paymentInfoPanel2.add(Box.createVerticalStrut(10));
		paymentInfoPanel2.add(paymentInfoWrapper("/resources/icons/mastercard.png", "Mastercard", mastercard, mastercardField, true));
		paymentInfoPanel2.add(Box.createVerticalStrut(10));
		paymentInfoPanel2.add(paymentInfoWrapper("/resources/icons/visa.png", "Visa", visa, visaField, true));
		
		paymentInfoPanel.add(paymentInfoPanel1);
		paymentInfoPanel.add(paymentInfoPanel2);
		
		paymentInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoWrapper2.add(paymentInfoPanel);
		
		infoWrapper.add(infoWrapper1);
		infoWrapper.add(infoWrapper2);
		
		CustomPanel topAnchor = new CustomPanel(new BorderLayout());
		topAnchor.add(infoWrapper, BorderLayout.NORTH);
		
		profileContentPanel.add(topAnchor, BorderLayout.CENTER);
		
		profileContentPanel.revalidate();
		profileContentPanel.repaint();
	}
	
	private CustomPanel infoWrapper(String info, String value, CustomTextField field, boolean isEditable) {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		CustomLabel infoLabel = new CustomLabel(info, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		infoLabel.setForeground(Color.GRAY);
		infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		wrapper.add(infoLabel);
		wrapper.add(Box.createVerticalStrut(5));
		
		if (isEditing && isEditable && field != null) {
			field.setText(value); 
			field.setCustomSize(300, 35);
			field.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(field);
		} else {
			CustomLabel valueLabel = new CustomLabel(value, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
			valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(valueLabel);
		}
		
		return wrapper;
	}
	
	private CustomPanel paymentInfoWrapper(String path, String info, String value, CustomTextField field, boolean isEditable) {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		CustomLabel infoLabel = new CustomLabel(info, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		infoLabel.setIcon(IconLoader.loadAndScaleIcon(path, 20, 20));
		infoLabel.setForeground(Color.GRAY);
		infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		wrapper.add(infoLabel);
		wrapper.add(Box.createVerticalStrut(5));
		
		if (isEditing && isEditable && field != null) {
			field.setText(value); 
			field.setCustomSize(200, 35);
			field.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(field);
		} else {
			CustomLabel valueLabel = new CustomLabel(value, Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
			valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(valueLabel);
		}
		
		return wrapper;
	}
}