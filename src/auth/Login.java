package auth;

import java.awt.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import components.*;
import marketplace.*;
import pages.Page;
import utils.*;
import database.UserRecord;


// need to connect on the database first to have a validations on the log in is
// first of all on the student id if the users doesnt have an account, but they input student id
// it will appear a error message that the "Student ID doesn't exists" as well 
// on the password if the users input a password but the student ID is doesn't exixts 
// it will also appear a error message that "Password doesn't exists'. for the whole



public class Login extends JPanel {

	private static final long serialVersionUID = 1L;

	// =========================
	// UI COMPONENTS
	// =========================
	private CustomTextField studentIdField;
	private CustomPasswordField passwordField;
	private CustomButton loginButton;

	private Auth authFrame;

	private CustomLabel studentIdError;
	private CustomLabel passwordError;
	private CustomLabel loginEmptyError;

	// =========================
	// SESSION DATA
	// =========================
	private String currentStudentId;

	// =========================
	// DATABASE LOG GROUPS
	// =========================
	private final List<UserRecord> createLogs = new ArrayList<>();
	private final List<UserRecord> updateLogs = new ArrayList<>();
	private final List<UserRecord> archiveLogs = new ArrayList<>();
	private final List<UserRecord> unarchiveLogs = new ArrayList<>();
	private final List<UserRecord> deleteLogs = new ArrayList<>();

	// =========================
	// DATABASE CONFIG
	// =========================
	private static final String DB_URL =
			"jdbc:mysql://localhost:3306/your_database_name";

	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "";

	// =========================
	// CONSTRUCTOR
	// =========================
	public Login(Auth authFrame) {

		this.authFrame = authFrame;

		CustomPanel formPanel =
				new CustomPanel(20, Color.WHITE, Color.BLACK, 1) {

			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				return new Dimension(510, size.height);
			}
		};

		formPanel.setPadding(30);
		formPanel.setLayout(new BorderLayout(0, 15));

		// HEADER
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

		// FORM FIELDS
		JPanel formFieldsPanel = new JPanel();
		formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));
		formFieldsPanel.setOpaque(false);

		studentIdField = new CustomTextField("A12345678", 20, 15);
		passwordField = new CustomPasswordField(20, 15);

		formFieldsPanel.add(createFieldGroup("Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));

		studentIdError = new CustomLabel("");
		studentIdError.setForeground(Color.RED);
		formFieldsPanel.add(studentIdError);

		formFieldsPanel.add(createFieldGroup("Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));

		passwordError = new CustomLabel("");
		passwordError.setForeground(Color.RED);
		formFieldsPanel.add(passwordError);

		// FORGOT PASSWORD
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

		// FOOTER
		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.setOpaque(false);

		loginButton = new CustomButton("Login", 20);

		loginEmptyError = new CustomLabel("");
		loginEmptyError.setForeground(Color.RED);

		JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
		registerPanel.setOpaque(false);

		registerPanel.add(new CustomLabel("Don't have an account?"));
		registerPanel.add(new CustomLink("Register here!", () -> authFrame.showRegistration()));

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setOpaque(false);

		JPanel errorWrapper = new JPanel();
		errorWrapper.setOpaque(false);
		errorWrapper.add(loginEmptyError);

		bottomPanel.add(registerPanel);
		bottomPanel.add(errorWrapper);

		// =========================
		// LOGIN BUTTON LOGIC (UPDATED ONLY HERE)
		// =========================
		loginButton.addActionListener(e -> {

			String studentId = studentIdField.getText().trim();
			String password = String.valueOf(passwordField.getPassword()).trim();

			studentIdError.setText("");
			passwordError.setText("");
			loginEmptyError.setText("");

			if (studentId.isEmpty() && password.isEmpty()) {
				loginEmptyError.setText("Student ID and Password is empty");
				return;
			}

			if (studentId.isEmpty()) {
				studentIdError.setText("Student ID is required.");
				return;
			}

			if (password.isEmpty()) {
				passwordError.setText("Password is required.");
				return;
			}

			if (!(studentId.matches("[AaKk][0-9]{8}"))) {
				studentIdError.setText("Invalid Student ID.");
				return;
			}

			if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
				passwordError.setText("Password is incorrect.");
				return;
			}

			// =========================
			// FETCH + EXISTENCE VALIDATION
			// =========================
			this.currentStudentId = studentId;
			fetchItemData();

			// Student ID does not exist
			if (createLogs.isEmpty()) {
				studentIdError.setText("Student ID doesn't exist.");
				return;
			}

			UserRecord user = createLogs.get(0);

			// Account does not exist
			if (user.student_id == null || user.student_id.isEmpty()) {
				loginEmptyError.setText("The account doesn't exist.");
				return;
			}

			// Password mismatch
			if (!user.password.equals(password)) {
				passwordError.setText("Password doesn't exist.");
				return;
			}

			// SUCCESS LOGIN
//			this.authFrame.setContentPane(new Page(new Marketplace()));
			this.authFrame.revalidate();
			this.authFrame.repaint();
		});

		footerPanel.add(loginButton, BorderLayout.CENTER);
		footerPanel.add(bottomPanel, BorderLayout.SOUTH);

		formPanel.add(footerPanel, BorderLayout.SOUTH);

		setOpaque(false);
		add(formPanel);
	}

	// DATABASE FETCH
	private void fetchItemData() {

		createLogs.clear();
		updateLogs.clear();
		archiveLogs.clear();
		unarchiveLogs.clear();
		deleteLogs.clear();

		if (currentStudentId == null || currentStudentId.isEmpty()) {
			return;
		}

		try (
			Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM users WHERE studentId = '" + currentStudentId + "'")
		) {

			while (rs.next()) {

				UserRecord log = new UserRecord();


				createLogs.add(log);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		panel.add(field, BorderLayout.CENTER);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

		return panel;
	}
}