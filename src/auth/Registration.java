package auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import components.*;
import utils.*;

import database.UserRecord;

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

	private CustomLabel errorLabel;

	private CustomLabel passwordStrengthLabel;

	private final List<UserRecord> users = new ArrayList<>();

	private static final String DB_URL =
			"jdbc:mysql://localhost:3306/your_database_name";

	private static final String DB_USER = "root";

	private static final String DB_PASSWORD = "";

	// =========================================================
	// PASSWORD STRENGTH ENUM
	// =========================================================
	private enum PasswordStrength {

		WEAK,
		MEDIUM,
		STRONG,
		VERY_STRONG
	}

	public Registration(Auth authFrame) {

		this.authFrame = authFrame;

		CustomPanel formPanel =
				new CustomPanel(
						20,
						Color.WHITE,
						Color.BLACK,
						1);

		formPanel.setPadding(20);

		formPanel.setPreferredSize(
				new Dimension(500, 680));

		formPanel.setLayout(
				new BorderLayout(0, 20));

		// =====================================================
		// HEADER
		// =====================================================
		JPanel headerPanel =
				new JPanel(new BorderLayout());

		headerPanel.setOpaque(false);

		JPanel titlePanel =
				new JPanel();

		titlePanel.setLayout(
				new BoxLayout(
						titlePanel,
						BoxLayout.Y_AXIS));

		titlePanel.setOpaque(false);

		CustomLabel headerTitle =
				new CustomLabel("Create Account");

		headerTitle.setFontStyle(
				FontStyle.BOLD);

		headerTitle.setFontSize(
				Brand.HEADER2_TEXT_SIZE);

		CustomLabel headerSubTitle =
				new CustomLabel(
						"Register to join the community");

		headerSubTitle.setFontStyle(
				FontStyle.REGULAR);

		headerSubTitle.setFontSize(
				Brand.SUBHEADER_TEXT_SIZE);

		titlePanel.add(headerTitle);

		titlePanel.add(headerSubTitle);

		headerPanel.add(
				titlePanel,
				BorderLayout.CENTER);

		formPanel.add(
				headerPanel,
				BorderLayout.NORTH);

		// =====================================================
		// FORM
		// =====================================================
		JPanel regisFormPanel =
				new JPanel();

		regisFormPanel.setLayout(
				new BoxLayout(
						regisFormPanel,
						BoxLayout.Y_AXIS));

		regisFormPanel.setOpaque(false);

		// =====================================================
		// FIELDS
		// =====================================================
		studentIdField =
				new CustomTextField(
						"K12345678",
						20,
						15);

		umakEmailField =
				new CustomTextField(
						"juan.delacruz@umak.edu.ph",
						20,
						15);

		firstNameField =
				new CustomTextField(
						"Juan",
						20,
						15);

		lastNameField =
				new CustomTextField(
						"Dela Cruz",
						20,
						15);

		courseField =
				new CustomTextField(
						"e.g. BSIT",
						20,
						15);

		collegeBox =
				new CustomComboBox<>(
						new String[]{
								"Select College",
								"CCIS",
								"CBA",
								"COE",
								"CAS",
								"CON"
						},
						15);

		yearLevelBox =
				new CustomComboBox<>(
						new String[]{
								"Select Year",
								"1st Year",
								"2nd Year",
								"3rd Year",
								"4th Year",
								"5th Year",
								"GRADUATE",
						},
						15);

		passwordField =
		        new CustomPasswordField(
		        		"e.g. [A-Z], [a-z], [0-9], [!@#$%^&*()]", 20, 15);

		// =====================================================
		// PASSWORD STRENGTH LABEL
		// =====================================================
		passwordStrengthLabel =
				new CustomLabel(" ");

		passwordStrengthLabel.setFontSize(12);

		passwordStrengthLabel.setFontStyle(
				FontStyle.BOLD);

		// =====================================================
		// LIVE PASSWORD VALIDATION
		// =====================================================
		passwordField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void insertUpdate(
							DocumentEvent e) {

						updatePasswordStrength();
					}

					@Override
					public void removeUpdate(
							DocumentEvent e) {

						updatePasswordStrength();
					}

					@Override
					public void changedUpdate(
							DocumentEvent e) {

						updatePasswordStrength();
					}
				});

		// =====================================================
		// STUDENT ID
		// =====================================================
		regisFormPanel.add(
				createFieldGroup(
						"Student ID: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						studentIdField));

		regisFormPanel.add(
				Box.createVerticalStrut(10));

		// =====================================================
		// EMAIL
		// =====================================================
		regisFormPanel.add(
				createFieldGroup(
						"UMak Email Address: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						umakEmailField));

		regisFormPanel.add(
				Box.createVerticalStrut(10));

		// =====================================================
		// NAME PANEL
		// =====================================================
		JPanel namePanel =
				new JPanel(
						new GridLayout(
								1,
								2,
								10,
								0));

		namePanel.setOpaque(false);

		namePanel.add(
				createFieldGroup(
						"First Name: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						firstNameField));

		namePanel.add(
				createFieldGroup(
						"Last Name: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						lastNameField));

		regisFormPanel.add(namePanel);

		regisFormPanel.add(
				Box.createVerticalStrut(10));

		// =====================================================
		// COLLEGE + YEAR
		// =====================================================
		JPanel collegePanel =
				new JPanel(
						new GridLayout(
								1,
								2,
								10,
								0));

		collegePanel.setOpaque(false);

		collegePanel.add(
				createFieldGroup(
						"College: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						collegeBox));

		collegePanel.add(
				createFieldGroup(
						"Year Level: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						yearLevelBox));

		regisFormPanel.add(collegePanel);

		regisFormPanel.add(
				Box.createVerticalStrut(10));

		// =====================================================
		// COURSE
		// =====================================================
		regisFormPanel.add(
				createFieldGroup(
						"Course: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						courseField));

		regisFormPanel.add(
				Box.createVerticalStrut(10));

		// =====================================================
		// PASSWORD
		// =====================================================
		regisFormPanel.add(
				createFieldGroup(
						"Password: *",
						FontStyle.BOLD,
						Brand.STANDARD_TEXT_SIZE,
						passwordField));

		regisFormPanel.add(
				Box.createVerticalStrut(5));

		regisFormPanel.add(passwordStrengthLabel);

		regisFormPanel.add(
				Box.createVerticalStrut(15));

		formPanel.add(
				regisFormPanel,
				BorderLayout.CENTER);

		// =====================================================
		// FOOTER
		// =====================================================
		JPanel footerPanel =
				new JPanel(new BorderLayout());

		footerPanel.setOpaque(false);

		registerButton =
				new CustomButton(
						"Register",
						20);

		registerButton.setPreferredSize(
				new Dimension(80, 50));

		footerPanel.add(
				registerButton,
				BorderLayout.CENTER);

		// =====================================================
		// LOGIN PANEL
		// =====================================================
		JPanel toLoginPanel =
				new JPanel(
						new FlowLayout(
								FlowLayout.CENTER,
								5,
								5));

		toLoginPanel.setOpaque(false);

		toLoginPanel.add(
				new CustomLabel(
						"Already have an account?")
		);

		toLoginPanel.add(
				new CustomLink(
						"Login here!",
						() -> {
							this.authFrame.showLogin();
						})
		);

		// =====================================================
		// ERROR LABEL
		// =====================================================
		errorLabel =
				new CustomLabel(" ");

		errorLabel.setForeground(Color.RED);

		errorLabel.setFontSize(12);

		errorLabel.setHorizontalAlignment(
				SwingConstants.CENTER);

		errorLabel.setAlignmentX(
				Component.CENTER_ALIGNMENT);

		errorLabel.setPreferredSize(
				new Dimension(400, 20));

		JPanel bottomPanel =
				new JPanel();

		bottomPanel.setLayout(
				new BoxLayout(
						bottomPanel,
						BoxLayout.Y_AXIS));

		bottomPanel.setOpaque(false);

		toLoginPanel.setAlignmentX(
				Component.CENTER_ALIGNMENT);

		errorLabel.setAlignmentX(
				Component.CENTER_ALIGNMENT);

		bottomPanel.add(toLoginPanel);

		bottomPanel.add(
				Box.createVerticalStrut(5));

		bottomPanel.add(errorLabel);

		footerPanel.add(
				bottomPanel,
				BorderLayout.SOUTH);

		// =====================================================
		// REGISTER BUTTON ACTION
		// =====================================================
		registerButton.addActionListener(e -> {

			String studentId =
					studentIdField.getText().trim();

			String email =
					umakEmailField.getText().trim();

			String firstName =
					firstNameField.getText().trim();

			String lastName =
					lastNameField.getText().trim();

			String course =
					courseField.getText().trim();

			String college =
					(String) collegeBox.getSelectedItem();

			String yearLevel =
					(String) yearLevelBox.getSelectedItem();

			String password =
					String.valueOf(
							passwordField.getPassword());

			// =================================================
			// EMPTY FIELDS
			// =================================================
			if (studentId.isEmpty()
					|| email.isEmpty()
					|| firstName.isEmpty()
					|| lastName.isEmpty()
					|| course.isEmpty()
					|| college.equals("Select College")
					|| yearLevel.equals("Select Year")
					|| password.isEmpty()) {

				errorLabel.setText(
						"Please complete all fields.");

				return;
			}

			// =================================================
			// STUDENT ID VALIDATION
			// =================================================
			if (!studentId.matches(
					"(?i)^[KA][0-9]{8}$")) {

				errorLabel.setText(
						"Invalid Student ID!");

				return;
			}

			// =================================================
			// EMAIL VALIDATION
			// =================================================
			if (!email.endsWith("@umak.edu.ph")) {

				errorLabel.setText(
						"Invalid UMak Email!");

				return;
			}

			// =================================================
			// PASSWORD STRENGTH VALIDATION
			// =================================================
			PasswordStrength strength =
					getPasswordStrength(password);

			if (strength == PasswordStrength.WEAK) {

				errorLabel.setText(
						"Weak password! Add uppercase, numbers, and symbols.");

				return;
			}

			if (strength == PasswordStrength.MEDIUM) {

				errorLabel.setText(
						"Medium password! Improve password security.");

				return;
			}

			if (strength == PasswordStrength.STRONG) {

				JOptionPane.showMessageDialog(
						authFrame,
						"Your password is STRONG.\n"
								+ "Recommendation:\n"
								+ "- Add more symbols\n"
								+ "- Increase password length\n"
								+ "- Mix more uppercase letters",
						"Password Recommendation",
						JOptionPane.INFORMATION_MESSAGE);
			}

			errorLabel.setText(" ");

			// =================================================
			// OPEN SECURITY DIALOG
			// =================================================
			SecurityDialog securityDialog =
					new SecurityDialog(authFrame);
			
			// =================================================
			// CHECK IF CONFIRMED
			// =================================================
			if (!securityDialog.isSubmitted()) {

				return;
			}

			// =================================================
			// SAVE TO DATABASE
			// =================================================
			saveUserData(
					studentId,
					email,
					password,
					college,
					yearLevel,
					course,
					firstName,
					lastName
			);

			// =================================================
			// SAVE LOCAL USER
			// =================================================
			UserRecord user =
					new UserRecord();


			users.add(user);

			JOptionPane.showMessageDialog(
					authFrame,
					"Registration Successful!");
		});

		formPanel.add(
				footerPanel,
				BorderLayout.SOUTH);

		setOpaque(false);

		add(formPanel);
	}

	// =========================================================
	// UPDATE PASSWORD STRENGTH
	// =========================================================
	private void updatePasswordStrength() {

		String password =
				String.valueOf(
						passwordField.getPassword());

		PasswordStrength strength =
				getPasswordStrength(password);

		switch (strength) {

			case WEAK:

				passwordField.setForeground(Color.RED);

				passwordStrengthLabel.setForeground(
						Color.RED);

				passwordStrengthLabel.setText(
						"Weak Password");

				break;

			case MEDIUM:

				passwordField.setForeground(Color.ORANGE);

				passwordStrengthLabel.setForeground(
						Color.ORANGE);

				passwordStrengthLabel.setText(
						"Medium Password");

				break;

			case STRONG:

				passwordField.setForeground(Color.BLUE);

				passwordStrengthLabel.setForeground(
						Color.BLUE);

				passwordStrengthLabel.setText(
						"Strong Password");

				break;

			case VERY_STRONG:

				passwordField.setForeground(
						new Color(0, 150, 0));

				passwordStrengthLabel.setForeground(
						new Color(0, 150, 0));

				passwordStrengthLabel.setText(
						"Very Strong Password");

				break;
		}
	}

	// =========================================================
	// PASSWORD STRENGTH LOGIC
	// =========================================================
	private PasswordStrength getPasswordStrength(
			String password) {

		if (password.length() <= 5) {

			return PasswordStrength.WEAK;
		}

		boolean hasUpper =
				password.matches(".*[A-Z].*");

		boolean hasLower =
				password.matches(".*[a-z].*");

		boolean hasNum =
				password.matches(".*[0-9].*");

		boolean hasSpecial =
				password.matches(
						".*[!@#$%^&*()].*");

		int score = 0;

		if (hasUpper) score++;

		if (hasLower) score++;

		if (hasNum) score++;

		if (hasSpecial) score++;

		if (password.length() >= 10) score++;

		if (score <= 2) {

			return PasswordStrength.WEAK;
		}

		if (score == 3) {

			return PasswordStrength.MEDIUM;
		}

		if (score == 4) {

			return PasswordStrength.STRONG;
		}

		return PasswordStrength.VERY_STRONG;
	}

	// =========================================================
	// FIELD GROUP
	// =========================================================
	private JPanel createFieldGroup(
			String labelText,
			FontStyle style,
			float size,
			JComponent field) {

		JPanel panel =
				new JPanel(
						new BorderLayout(0, 5));

		panel.setOpaque(false);

		JPanel labelPanel =
				new JPanel();

		labelPanel.setLayout(
				new BoxLayout(
						labelPanel,
						BoxLayout.X_AXIS));

		labelPanel.setOpaque(false);

		if (labelText.endsWith("*")) {

			String text =
					labelText.substring(
							0,
							labelText.length() - 1);

			CustomLabel l1 =
					new CustomLabel(text);

			l1.setFontStyle(style);

			l1.setFontSize(size);

			CustomLabel star =
					new CustomLabel("*");

			star.setForeground(Color.RED);

			star.setFontSize(size);

			labelPanel.add(l1);

			labelPanel.add(star);

		} else {

			CustomLabel l1 =
					new CustomLabel(labelText);

			l1.setFontStyle(style);

			l1.setFontSize(size);

			labelPanel.add(l1);
		}

		panel.add(
				labelPanel,
				BorderLayout.NORTH);

		panel.add(
				field,
				BorderLayout.CENTER);

		panel.setMaximumSize(
				new Dimension(
						Integer.MAX_VALUE,
						72));

		return panel;
	}

	// =========================================================
	// SAVE USER DATA
	// =========================================================
	private void saveUserData(
			String studentId,
			String email,
			String password,
			String college,
			String yearLevel,
			String course,
			String firstName,
			String lastName) {

		try (

				Connection conn =
						DriverManager.getConnection(
								DB_URL,
								DB_USER,
								DB_PASSWORD
						);

				PreparedStatement ps =
						conn.prepareStatement(

								"INSERT INTO users("
										+ "systemRole,"
										+ "studentId,"
										+ "umakEmailAddress,"
										+ "password,"
										+ "college,"
										+ "yearLevel,"
										+ "courseProgram,"
										+ "firstName,"
										+ "lastName,"
										+ "karmaScore,"
										+ "profileImage,"
										+ "contactNum,"
										+ "homeAddress,"
										+ "gcashNum,"
										+ "mayaNum,"
										+ "mastercardNum,"
										+ "visaNum)"
										+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")

		) {

			ps.setString(1, "Student");
			ps.setString(2, studentId);
			ps.setString(3, email);
			ps.setString(4, password);
			ps.setString(5, college);
			ps.setString(6, yearLevel);
			ps.setString(7, course);
			ps.setString(8, firstName);
			ps.setString(9, lastName);
			ps.setInt(10, 0);
			ps.setString(11, "");
			ps.setObject(12, null);
			ps.setString(13, "");
			ps.setObject(14, null);
			ps.setObject(15, null);
			ps.setString(16, "");
			ps.setString(17, "");

			ps.executeUpdate();

		} catch (SQLException e) {

			e.printStackTrace();

			errorLabel.setText(
					"Database Error!");
		}
	}
}