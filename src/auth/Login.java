package auth;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class Login extends JPanel {
	private static final long serialVersionUID = 1L;
	private CustomTextField studentIdField;
	private CustomPasswordField passwordField;
	private CustomButton loginButton;
	private Auth authFrame;
	
	public Login(Auth authFrame) {
		this.authFrame = authFrame;
		CustomPanel formPanel = new CustomPanel(20, Color.WHITE, Color.BLACK, 1) {
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				return new Dimension(500, size.height);
			}
		};
		formPanel.setPadding(30);
		formPanel.setLayout(new BorderLayout(0, 20));
		
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
		headerPanel.setOpaque(false);
		
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setOpaque(false);
		
		CustomLabel headerTitle = new CustomLabel("Welcome Back");
		headerTitle.setFontStyle(FontStyle.BOLD);
		headerTitle.setFontSize(Brand.HEADER1_TEXT_SIZE);
		
		CustomLabel headerSubTitle = new CustomLabel("Login to your account");
		headerSubTitle.setFontStyle(FontStyle.REGULAR);
		headerSubTitle.setFontSize(Brand.SUBHEADER_TEXT_SIZE);

		titlePanel.add(headerTitle);
		titlePanel.add(headerSubTitle);
		headerPanel.add(titlePanel, BorderLayout.CENTER);
		
		formPanel.add(headerPanel, BorderLayout.NORTH);
		
		JPanel regisFormPanel = new JPanel();
		regisFormPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
		regisFormPanel.setLayout(new BoxLayout(regisFormPanel, BoxLayout.Y_AXIS));
		regisFormPanel.setOpaque(false);
		
		studentIdField = new CustomTextField("K12345678", 20, 15);
		studentIdField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		regisFormPanel.add(createFieldGroup("Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));
		regisFormPanel.add(Box.createVerticalStrut(10));
				
		passwordField = new CustomPasswordField(20, 15);
		passwordField.setFontSize(Brand.STANDARD_TEXT_SIZE);
		regisFormPanel.add(createFieldGroup("Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));
		
		formPanel.add(regisFormPanel, BorderLayout.CENTER);
		
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.setOpaque(false);
		loginButton = new CustomButton("Login", 20);
		footerPanel.add(loginButton, BorderLayout.CENTER);
		
		JPanel toLoginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
		toLoginPanel.setOpaque(false);
		toLoginPanel.add(new CustomLabel("Don't have an account?"));
		toLoginPanel.add(new CustomLink("Register here!", () -> {
			this.authFrame.showRegistration();
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