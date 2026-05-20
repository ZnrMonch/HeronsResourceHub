package auth;

import java.awt.*;
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import components.*;
import marketplace.*;
import pages.Page;
import utils.*;
import database.UserRecord;
import admin.database.LogsDatabase;
import enums.UserLogAction;

public class Login extends JPanel {
    private static final long serialVersionUID = 1L;

    // ─── Form Fields ───────────────────────────────────────────────────────────
    /** Text field where the user types their Student ID (e.g., "A12345678") */
    private CustomTextField studentIdField;
    /** Password field that masks the user's password input */
    private CustomPasswordField passwordField;
    /** The submit/login button that triggers authentication */
    private CustomButton loginButton;
    // ─── Parent Frame Reference ────────────────────────────────────────────────
    /**
     * Reference to the parent Auth JFrame. Used to switch views (e.g., navigate to
     * Registration or the main Page).
     */
    private Auth authFrame;
    // ─── Database Configuration ────────────────────────────────────────────────
    /** JDBC connection URL pointing to the local MySQL database "dbnorm2" */
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/heronsresourcehub";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    // ──────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ──────────────────────────────────────────────────────────────────────────
    public Login(Auth authFrame) {
		this.authFrame = authFrame;

		// ── Form Panel ──────────────────────────────────────────────────────
		CustomPanel formPanel = new CustomPanel(20, Color.WHITE, Color.BLACK, 1) {
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				return new Dimension(510, size.height);
			}
		};
		formPanel.setPadding(30);                          
		formPanel.setLayout(new BorderLayout(0, 15));      

		// ── HEADER SECTION ──────────────────────────────────────────────────
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false); 
		
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setOpaque(false);
		
		CustomLabel title = new CustomLabel("Welcome Back");
		title.setFontStyle(FontStyle.BOLD);
		title.setFontSize(Brand.HEADER1_TEXT_SIZE);
		
		CustomLabel subtitle = new CustomLabel("Login to your account");
		subtitle.setFontStyle(FontStyle.REGULAR);
		subtitle.setFontSize(Brand.SUBHEADER_TEXT_SIZE);
		
		titlePanel.add(title);
		titlePanel.add(subtitle);
		headerPanel.add(titlePanel, BorderLayout.CENTER);
		formPanel.add(headerPanel, BorderLayout.NORTH); 

		// ── FORM FIELDS SECTION ─────────────────────────────────────────────
		JPanel formFieldsPanel = new JPanel();
		formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));
		formFieldsPanel.setOpaque(false);
		
		// Student ID text input with placeholder hint
		studentIdField = new CustomTextField("A12345678", 20, 15);
		// Password input (characters are hidden/masked)
		passwordField = new CustomPasswordField(20, 15);
		// Add "Student ID: *" label + input field as a grouped row
		formFieldsPanel
				.add(createFieldGroup("Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));
		
		formFieldsPanel.add(Box.createVerticalStrut(10));

		// Add "Password: *" label + input field as a grouped row
		formFieldsPanel.add(createFieldGroup("Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));
		
		formFieldsPanel.add(Box.createVerticalStrut(10));
		
		// ── Forgot Password Link ────────────────────────────────────────────
		// Right-aligned clickable link that opens the ForgotPassword dialog
		JPanel forgotPasswordPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		forgotPasswordPanel.setOpaque(false);
		CustomLink forgotPasswordLink = new CustomLink(
				"Forgot Password?",
				() -> new Forgotpassword(authFrame).showDialog()
		);
		forgotPasswordLink.setForeground(Color.RED);
		forgotPasswordPanel.add(forgotPasswordLink);
		formFieldsPanel.add(forgotPasswordPanel);
		
		formPanel.add(formFieldsPanel, BorderLayout.CENTER); 

		// ── FOOTER SECTION ──────────────────────────────────────────────────
		JPanel footerPanel = new JPanel(new BorderLayout(0, 10)); // 10px spacing
		footerPanel.setOpaque(false);
		loginButton = new CustomButton("Login", 20);
		// Force fixed smaller height for button to prevent it from stretching vertically
		loginButton.setPreferredSize(new Dimension(0, 40));
		
		// "Don't have an account? Register here!" row
		JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
		registerPanel.setOpaque(false);
		registerPanel.add(new CustomLabel("Don't have an account?"));
		// Clicking "Register here!" switches the Auth frame to the Registration panel
		registerPanel.add(new CustomLink("Register here!", () -> authFrame.showRegistration()));
		// Stack the register row vertically below the button
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setOpaque(false);

		bottomPanel.add(registerPanel);
		// ── Login Button — Action Listener (Authentication Logic) ────────────
		loginButton.addActionListener(e -> {
			// Step 1: Read and trim input values
			String studentId = studentIdField.getText().trim();
			String password = String.valueOf(passwordField.getPassword()).trim();
			
			// Step 2: Combined empty check — both fields are blank
			if (studentId.isEmpty() && password.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Student ID and Password is empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// Step 3: Individual field validation
			if (studentId.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Student ID is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (password.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Password is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// Step 4: Student ID format validation
			if (!studentId.matches("(?i)^[KkAa][0-9]{8}$")) {
				JOptionPane.showMessageDialog(this, "Invalid Student ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
				return; 
			}
			// Step 5: Database lookup
			UserRecord user = fetchUser(studentId);
			if (user == null) {
			    JOptionPane.showMessageDialog(this, "Student ID doesn't exist or account is archived.", "Validation Error", JOptionPane.ERROR_MESSAGE);
			    return;
			}

			// Step 6: Password verification
			String decryptedPassword = Encryption.decryptPassword(user.password);

			// --- ADD THESE DEBUG LINES TO SEE THE PROBLEM IN YOUR CONSOLE ---
			System.out.println("1. What the user typed: " + password);
			System.out.println("2. What is saved in the DB: " + user.password);
			System.out.println("3. What it decrypted to: " + decryptedPassword);
			// ----------------------------------------------------------------

			if (decryptedPassword == null || !decryptedPassword.equals(password)) {
			    JOptionPane.showMessageDialog(this, "Invalid  username or password.", "Validation Error", JOptionPane.ERROR_MESSAGE);
			    return;
			}
			// Step 7: Session initialization
			SessionManager.login(user.user_id, user.system_role, user.first_name + " " + user.last_name);
			// Step 8: Audit logging
			try {
				LogsDatabase logDB = new LogsDatabase();
				logDB.insertLog(UserLogAction.USER_LOGIN, user.user_id, "");
			} catch (Exception ex) {
				// Fallback generic catch
				System.err.println("Login log failed: " + ex.getMessage());
			}
			// Step 9: Navigate to the Marketplace page

			new Page(new Marketplace(user), user);
			this.authFrame.dispose();
		});
		
		footerPanel.add(loginButton,  BorderLayout.CENTER); 
		footerPanel.add(bottomPanel,  BorderLayout.SOUTH);  
		formPanel.add(footerPanel,    BorderLayout.SOUTH);  
		
		setOpaque(false);
		add(formPanel);
	}

    private UserRecord fetchUser(String studentId) {
        String sql = "SELECT user_id, student_id, password, system_role, first_name, last_name "
                   + "FROM users WHERE student_id = ? AND users_is_archived = false";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserRecord u     = new UserRecord();
                u.user_id        = rs.getInt("user_id");        
                u.system_role    = rs.getString("system_role"); 
                u.first_name     = rs.getString("first_name");  
                u.last_name      = rs.getString("last_name");   
                u.student_id     = rs.getString("student_id");  
                u.password       = rs.getString("password");    
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
            asteriskLabel.setForeground(Color.RED);
            asteriskLabel.setFontSize(size);
            
            labelPanel.add(textLabel);
            labelPanel.add(asteriskLabel);
        } else {
            CustomLabel label = new CustomLabel(labelText);
            label.setFontStyle(style);
            label.setFontSize(size);
            labelPanel.add(label);
        }
        
        panel.add(labelPanel, BorderLayout.NORTH);  
        panel.add(field,      BorderLayout.CENTER); 
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        return panel;
    }
}