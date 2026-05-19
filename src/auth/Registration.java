package auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import admin.database.LogsDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import components.*;
import database.UMak;
import enums.UserLogAction;
import utils.*;

public class Registration extends JPanel {
    private static final long serialVersionUID = 1L;

    // =========================
    // UI FIELDS
    // =========================
    private CustomTextField        studentIdField;
    private CustomTextField        umakEmailField;
    private CustomTextField        firstNameField;
    private CustomTextField        lastNameField;
    private CustomTextField        courseField;
    private CustomComboBox<String> collegeBox;
    private CustomComboBox<String> yearLevelBox;
    private CustomPasswordField    passwordField;
    private CustomButton           registerButton;
    private CustomLabel            errorLabel;
    private CustomLabel            passwordStrengthLabel;

    private Auth authFrame;

    // =========================
    // DATABASE CONFIG
    // FIX: constants were missing — copied from Login pattern
    // =========================
    private static final String DB_URL  = "jdbc:mysql://localhost:4306/dbnorm2";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private enum PasswordStrength { WEAK, MEDIUM, STRONG, VERY_STRONG }

    // =========================
    // CONSTRUCTOR
    // =========================
    public Registration(Auth authFrame) {
        this.authFrame = authFrame;

        CustomPanel formPanel = new CustomPanel(20, Color.WHITE, Color.BLACK, 1);
        formPanel.setPadding(20);
        formPanel.setPreferredSize(new Dimension(500, 680));
        formPanel.setLayout(new BorderLayout(0, 20));

        // HEADER
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

        // FORM BODY
        JPanel regisFormPanel = new JPanel();
        regisFormPanel.setLayout(new BoxLayout(regisFormPanel, BoxLayout.Y_AXIS));
        regisFormPanel.setOpaque(false);

        studentIdField = new CustomTextField("K12345678", 20, 15);
        umakEmailField = new CustomTextField("juan.delacruz@umak.edu.ph", 20, 15);
        firstNameField = new CustomTextField("Juan", 20, 15);
        lastNameField  = new CustomTextField("Dela Cruz", 20, 15);
        courseField    = new CustomTextField("e.g. BSIT", 20, 15);

        collegeBox   = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES, 15);
        yearLevelBox = new CustomComboBox<>(new String[]{
                "Select Year",
                "1st Year", "2nd Year", "3rd Year",
                "4th Year", "5th Year",
                "GRADUATE", "ALUMNI"
        }, 15);

        passwordField = new CustomPasswordField(
                "e.g. [A-Z], [a-z], [0-9], [!@#$%^&*()]", 20, 15);

        passwordStrengthLabel = new CustomLabel(" ");
        passwordStrengthLabel.setFontSize(12);
        passwordStrengthLabel.setFontStyle(FontStyle.BOLD);

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updatePasswordStrength(); }
            public void removeUpdate(DocumentEvent e)  { updatePasswordStrength(); }
            public void changedUpdate(DocumentEvent e) { updatePasswordStrength(); }
        });

        regisFormPanel.add(createFieldGroup(
                "Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));
        regisFormPanel.add(Box.createVerticalStrut(10));

        regisFormPanel.add(createFieldGroup(
                "UMak Email Address: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, umakEmailField));
        regisFormPanel.add(Box.createVerticalStrut(10));

        JPanel namePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        namePanel.setOpaque(false);
        namePanel.add(createFieldGroup(
                "First Name: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, firstNameField));
        namePanel.add(createFieldGroup(
                "Last Name: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, lastNameField));
        regisFormPanel.add(namePanel);
        regisFormPanel.add(Box.createVerticalStrut(10));

        JPanel collegePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        collegePanel.setOpaque(false);
        collegePanel.add(createFieldGroup(
                "College: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, collegeBox));
        collegePanel.add(createFieldGroup(
                "Year Level: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, yearLevelBox));
        regisFormPanel.add(collegePanel);
        regisFormPanel.add(Box.createVerticalStrut(10));

        regisFormPanel.add(createFieldGroup(
                "Course: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, courseField));
        regisFormPanel.add(Box.createVerticalStrut(10));

        regisFormPanel.add(createFieldGroup(
                "Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));
        regisFormPanel.add(Box.createVerticalStrut(5));
        regisFormPanel.add(passwordStrengthLabel);
        regisFormPanel.add(Box.createVerticalStrut(15));

        formPanel.add(regisFormPanel, BorderLayout.CENTER);

        // FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        registerButton = new CustomButton("Register", 20);
        registerButton.setPreferredSize(new Dimension(80, 50));
        footerPanel.add(registerButton, BorderLayout.CENTER);

        JPanel toLoginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        toLoginPanel.setOpaque(false);
        toLoginPanel.add(new CustomLabel("Already have an account?"));
        toLoginPanel.add(new CustomLink("Login here!", () -> authFrame.showLogin()));

        errorLabel = new CustomLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFontSize(12);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setPreferredSize(new Dimension(400, 20));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        toLoginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(toLoginPanel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(errorLabel);

        footerPanel.add(bottomPanel, BorderLayout.SOUTH);
        formPanel.add(footerPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> handleRegister());

        setOpaque(false);
        add(formPanel);
    }

    // =========================
    // HANDLE REGISTER
    // =========================
    private void handleRegister() {
        String studentId = studentIdField.getText().trim();
        String email     = umakEmailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String course    = courseField.getText().trim();
        String college   = (String) collegeBox.getSelectedItem();
        String yearLevel = (String) yearLevelBox.getSelectedItem();
        String password  = String.valueOf(passwordField.getPassword());

        // Empty check
        if (studentId.isEmpty() || email.isEmpty()
                || firstName.isEmpty() || lastName.isEmpty()
                || course.isEmpty()
                || "Select College".equals(college)
                || "Select Year".equals(yearLevel)
                || password.isEmpty()) {
            errorLabel.setText("Please complete all fields.");
            return;
        }

        // Student ID format
    

        // Email domain
        if (!email.endsWith("@umak.edu.ph")) {
            errorLabel.setText("Invalid UMak Email!");
            return;
        }

        // Password strength
        PasswordStrength strength = getPasswordStrength(password);
        if (strength == PasswordStrength.WEAK) {
            errorLabel.setText("Weak password! Add uppercase, numbers, and symbols.");
            return;
        }
        if (strength == PasswordStrength.MEDIUM) {
            errorLabel.setText("Medium password! Improve password security.");
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

        // Duplicate check
        if (studentIdExists(studentId)) {
            errorLabel.setText("Student ID is already registered.");
            return;
        }
        if (emailExists(email)) {
            errorLabel.setText("Email is already registered.");
            return;
        }

        errorLabel.setText(" ");

        // --- Save user first, then get the generated user_id ---
        // FIX: we need the auto-generated user_id to save security questions,
        //      so saveUserToDatabase now returns the new user_id (or -1 on failure).
        int newUserId = saveUserToDatabase(
                studentId, email, password,
                college, yearLevel, course, firstName, lastName);

        if (newUserId == -1) {
            // errorLabel is already set inside saveUserToDatabase
            return;
        }

        // --- Security dialog — pass the real user_id ---
        // FIX: SecurityDialog now receives the userId so it can INSERT into SECURITY table.
        SecurityDialog securityDialog = new SecurityDialog(authFrame, newUserId);
        if (!securityDialog.isSubmitted()) {
            deleteUser(newUserId);
            errorLabel.setText("Registration cancelled — security questions required.");
            return;
        }

        // Log after security questions confirmed — user row is now permanent
        try {
            LogsDatabase logDB = new LogsDatabase();
            logDB.insertLog(UserLogAction.USER_CREATE, newUserId, "");
        } catch (Exception ex) {
            System.err.println("Registration log failed (non-fatal): " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(authFrame, "Registration Successful!");
        authFrame.showLogin();
    }

    // =========================
    // DUPLICATE CHECKS
    // FIX: fieldExists() had a broken try-with-resources (missing "Connection conn =")
    // =========================
    private boolean studentIdExists(String studentId) {
        return fieldExists("student_id", studentId);
    }

    private boolean emailExists(String email) {
        return fieldExists("umak_email_address", email);
    }

    private boolean fieldExists(String column, String value) {
        // NOTE: column name is not user-input so string concat here is safe.
        String sql = "SELECT 1 FROM users WHERE " + column + " = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // SAVE USER TO DATABASE
    // FIX: now returns the auto-generated user_id via RETURN_GENERATED_KEYS,
    //      instead of a boolean, so SecurityDialog can link security questions.
    // =========================
    private int saveUserToDatabase(
            String studentId, String email, String password,
            String college, String yearLevel, String course,
            String firstName, String lastName) {

        String sql =
                "INSERT INTO users "
                + "(student_id, system_role, umak_email_address, password, "
                + " college, year_level, course_program, first_name, last_name, "
                + " karma_score, profile_image) "
                + "VALUES (?, 'end_user', ?, ?, ?, ?, ?, ?, ?, 0, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, studentId);
            ps.setString(2, email);
            ps.setString(3, password);   // TODO: hash with BCrypt before storing
            ps.setString(4, college);
            ps.setString(5, yearLevel);
            ps.setString(6, course);
            ps.setString(7, firstName);
            ps.setString(8, lastName);
            ps.setString(9, "/resources/defaultpictures/axolotl.jpg");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);   // return the new user_id
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error — please try again.");
        }

        return -1;
    }

    // =========================
    // ROLLBACK: delete user if security dialog is cancelled
    // =========================
    private void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // PASSWORD STRENGTH
    // =========================
    private void updatePasswordStrength() {
        String p = String.valueOf(passwordField.getPassword());
        PasswordStrength strength = getPasswordStrength(p);

        switch (strength) {
            case WEAK:
                passwordField.setForeground(Color.RED);
                passwordStrengthLabel.setForeground(Color.RED);
                passwordStrengthLabel.setText("Weak Password");
                break;
            case MEDIUM:
                passwordField.setForeground(Color.ORANGE);
                passwordStrengthLabel.setForeground(Color.ORANGE);
                passwordStrengthLabel.setText("Medium Password");
                break;
            case STRONG:
                passwordField.setForeground(Color.BLUE);
                passwordStrengthLabel.setForeground(Color.BLUE);
                passwordStrengthLabel.setText("Strong Password");
                break;
            case VERY_STRONG:
                passwordField.setForeground(new Color(0, 150, 0));
                passwordStrengthLabel.setForeground(new Color(0, 150, 0));
                passwordStrengthLabel.setText("Very Strong Password");
                break;
        }
    }

    private PasswordStrength getPasswordStrength(String password) {
        if (password.length() <= 5) return PasswordStrength.WEAK;

        boolean hasUpper   = password.matches(".*[A-Z].*");
        boolean hasLower   = password.matches(".*[a-z].*");
        boolean hasNum     = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");

        int score = 0;
        if (hasUpper)                score++;
        if (hasLower)                score++;
        if (hasNum)                  score++;
        if (hasSpecial)              score++;
        if (password.length() >= 10) score++;

        if (score <= 2) return PasswordStrength.WEAK;
        if (score == 3) return PasswordStrength.MEDIUM;
        if (score == 4) return PasswordStrength.STRONG;
        return PasswordStrength.VERY_STRONG;
    }

    // =========================
    // FIELD GROUP BUILDER
    // =========================
    private JPanel createFieldGroup(
            String labelText, FontStyle style, float size, JComponent field) {

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setOpaque(false);

        if (labelText.endsWith("*")) {
            String text = labelText.substring(0, labelText.length() - 1);
            CustomLabel l1 = new CustomLabel(text);
            l1.setFontStyle(style);
            l1.setFontSize(size);
            CustomLabel star = new CustomLabel("*");
            star.setForeground(Color.RED);
            star.setFontSize(size);
            labelPanel.add(l1);
            labelPanel.add(star);
        } else {
            CustomLabel l1 = new CustomLabel(labelText);
            l1.setFontStyle(style);
            l1.setFontSize(size);
            labelPanel.add(l1);
        }

        panel.add(labelPanel, BorderLayout.NORTH);
        panel.add(field,      BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        return panel;
    }
}