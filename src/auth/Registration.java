package auth;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class Registration extends JPanel {
	private static final long serialVersionUID = 1L;
	private CustomTextField studentIdField;
	private CustomTextField umakEmailField;
	private CustomTextField firstNameField;
	private CustomTextField lastNameField;
	private CustomTextField courseField;
	private CustomComboBox<String> collegeBox;
	private CustomComboBox<String> yearLevelBox;
	private CustomPasswordField passwordField;
	private CustomButton registerButton;
	private Auth authFrame;
	
	public Registration(Auth authFrame) {		
		this.authFrame = authFrame;
		CustomPanel formPanel = new CustomPanel(20, Color.WHITE, Color.BLACK, 1, true);
		formPanel.addPadding(30);
		formPanel.setPreferredSize(new Dimension(500, 670));
		formPanel.setLayout(new BorderLayout(0, 20));
		
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);
		
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setOpaque(false);
		
		CustomLabel headerTitle = new CustomLabel("Create Account");
		headerTitle.setFontStyle(FontStyle.BOLD);
		headerTitle.setFontSize(Brand.HEADER2_TEXT_SIZE);
		
		CustomLabel headerSubTitle = new CustomLabel("Register to join the community");
		headerSubTitle.setFontStyle(FontStyle.REGULAR);
		headerSubTitle.setFontSize(Brand.SUBHEADER_TEXT_SIZE);

		titlePanel.add(headerTitle);
		titlePanel.add(headerSubTitle);
		headerPanel.add(titlePanel, BorderLayout.CENTER);
		
		formPanel.add(headerPanel, BorderLayout.NORTH);
		
		JPanel regisFormPanel = new JPanel();
		regisFormPanel.setLayout(new BoxLayout(regisFormPanel, BoxLayout.Y_AXIS));
		regisFormPanel.setOpaque(false);
		
		studentIdField = new CustomTextField("K12345678", 20, 15);
		studentIdField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		regisFormPanel.add(createFieldGroup("Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));
		regisFormPanel.add(Box.createVerticalStrut(10));
		
		umakEmailField = new CustomTextField("juan.delacruz@umak.edu.ph", 20, 15);
		umakEmailField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		regisFormPanel.add(createFieldGroup("UMak Email Address: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, umakEmailField));
		regisFormPanel.add(Box.createVerticalStrut(10));
		
		JPanel nameFormPanel = new JPanel(new GridLayout(0, 2, 10, 0));
		nameFormPanel.setOpaque(false);
		nameFormPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameFormPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 
		
		firstNameField = new CustomTextField("Juan", 20, 15);
		firstNameField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		lastNameField = new CustomTextField("Dela Cruz", 20, 15);
		lastNameField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		nameFormPanel.add(createFieldGroup("First Name: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, firstNameField));
		nameFormPanel.add(createFieldGroup("Last Name: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, lastNameField));
		
		regisFormPanel.add(nameFormPanel);
		regisFormPanel.add(Box.createVerticalStrut(10));
		
		JPanel collegeFormPanel = new JPanel(new GridLayout(0, 2, 10, 0));
		collegeFormPanel.setOpaque(false);
		collegeFormPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		collegeFormPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
		
		String[] colleges = {"Select College","CCIS", "CBA", "COE", "CAS", "CON"};
		collegeBox = new CustomComboBox<>(colleges, 15);
		collegeBox.setFontSize(Brand.STANDARD_TEXT_SIZE);
		collegeBox.setBackground(Color.WHITE);
		collegeFormPanel.add(createFieldGroup("College: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, collegeBox));
		
		String[] yearLevels = {"Select Year", "1st Year", "2nd Year", "3rd Year", "4th Year"};
		yearLevelBox = new CustomComboBox<>(yearLevels, 15);
		yearLevelBox.setFontSize(Brand.STANDARD_TEXT_SIZE);
		yearLevelBox.setBackground(Color.WHITE);
		collegeFormPanel.add(createFieldGroup("Year Level: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, yearLevelBox));
		
		regisFormPanel.add(collegeFormPanel);
		regisFormPanel.add(Box.createVerticalStrut(10));
		
		courseField = new CustomTextField("e.g. BS in Information Technology", 20, 15);
		courseField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		regisFormPanel.add(createFieldGroup("Course: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, courseField));
		regisFormPanel.add(Box.createVerticalStrut(10));
		
		passwordField = new CustomPasswordField(20, 15);
		passwordField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		regisFormPanel.add(createFieldGroup("Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));
		
		formPanel.add(regisFormPanel, BorderLayout.CENTER);
		
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.setOpaque(false);
		registerButton = new CustomButton("Register", 20);
		registerButton.setPreferredSize(new Dimension(80, 40)); 
		footerPanel.add(registerButton, BorderLayout.CENTER);
		
		JPanel toLoginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		toLoginPanel.setOpaque(false);
		toLoginPanel.add(new CustomLabel("Already have an account?"));
		toLoginPanel.add(new CustomLink("Login here!", () -> {
			this.authFrame.showLogin();
		}));
		footerPanel.add(toLoginPanel, BorderLayout.SOUTH);
		
		formPanel.add(footerPanel, BorderLayout.SOUTH);
		
		setOpaque(false);
		add(formPanel);
	}
	
	private JPanel createFieldGroup(String labelText, FontStyle style, float size, JComponent field) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setOpaque(false);
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.setOpaque(false);
		
		if (labelText.endsWith("*")) {
			String textPart = labelText.substring(0, labelText.length() - 1);
			CustomLabel textLabel = new CustomLabel(textPart);
			textLabel.setFontStyle(style);
			textLabel.setFontSize(size);
			
			CustomLabel asteriskLabel = new CustomLabel("*");
			asteriskLabel.setFontStyle(style);
			asteriskLabel.setFontSize(size);
			asteriskLabel.setForeground(Color.RED);
			
			labelPanel.add(textLabel);
			labelPanel.add(asteriskLabel);
		} else {
			CustomLabel textLabel = new CustomLabel(labelText);
			textLabel.setFontStyle(style);
			textLabel.setFontSize(size);
			
			labelPanel.add(textLabel);
		}
		
		panel.add(labelPanel, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
		return panel;
	}
}