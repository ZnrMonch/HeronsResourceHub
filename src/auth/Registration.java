package auth;

import java.awt.*;
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
    private CustomTextField studentIdField;
    private CustomTextField umakEmailField;
    private CustomTextField firstNameField;
    private CustomTextField lastNameField;
    private CustomTextField courseField;
    private CustomComboBox<String> collegeBox;
    private CustomComboBox<String> yearLevelBox;
    private CustomPasswordField passwordField;
    private CustomButton registerButton;
    private CustomLabel passwordStrengthLabel;
    private Auth authFrame;

    // =========================
    // DATABASE CONFIG
    // =========================
    private static final String DB_URL = "jdbc:mysql://localhost:3306/heronsresourcehub";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private enum PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }

    // =========================
    // CONSTRUCTOR
    // =========================
    public Registration(Auth authFrame) {
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

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        CustomLabel headerTitle = new CustomLabel("Create Account");
        headerTitle.setFontStyle(FontStyle.BOLD);
        headerTitle.setFontSize(Brand.HEADER1_TEXT_SIZE);
        
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
        courseField    = new CustomTextField("e.g. BS in Information Technology", 20, 15);
        
        collegeBox = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES, 15);
        yearLevelBox = new CustomComboBox<>(new String[] { "Select Year", "1st Year", "2nd Year", "3rd Year",
                "4th Year", "5th Year", "GRADUATE", "ALUMNI" }, 15);
        
        passwordField = new CustomPasswordField("e.g. [A-Z], [a-z], [0-9], [!@#$%^&*()]", 20, 15);
        passwordStrengthLabel = new CustomLabel(" ");
        passwordStrengthLabel.setFontSize(12);
        passwordStrengthLabel.setFontStyle(FontStyle.BOLD);
        
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePasswordStrength(); }
            public void removeUpdate(DocumentEvent e) { updatePasswordStrength(); }
            public void changedUpdate(DocumentEvent e) { updatePasswordStrength(); }
        });
        
        regisFormPanel.add(createFieldGroup("Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));
        regisFormPanel.add(Box.createVerticalStrut(10));
        regisFormPanel.add(createFieldGroup("UMak Email Address: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, umakEmailField));
        regisFormPanel.add(Box.createVerticalStrut(10));
        
        JPanel namePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        namePanel.setOpaque(false);
        namePanel.add(createFieldGroup("First Name: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, firstNameField));
        namePanel.add(createFieldGroup("Last Name: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, lastNameField));
        regisFormPanel.add(namePanel);
        regisFormPanel.add(Box.createVerticalStrut(10));
        
        JPanel collegePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        collegePanel.setOpaque(false);
        collegePanel.add(createFieldGroup("College: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, collegeBox));
        collegePanel.add(createFieldGroup("Year Level: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, yearLevelBox));
        regisFormPanel.add(collegePanel);
        regisFormPanel.add(Box.createVerticalStrut(10));
        
        regisFormPanel.add(createFieldGroup("Course: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, courseField));
        regisFormPanel.add(Box.createVerticalStrut(10));
        
        regisFormPanel.add(createFieldGroup("Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));
        regisFormPanel.add(Box.createVerticalStrut(5));
        
        JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        strengthPanel.setOpaque(false);
        strengthPanel.add(passwordStrengthLabel);
        regisFormPanel.add(strengthPanel);
        
        formPanel.add(regisFormPanel, BorderLayout.CENTER);

        // FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        
        registerButton = new CustomButton("Register", 20);
        registerButton.setPreferredSize(new Dimension(0, 50)); 
        
        JPanel toLoginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
        toLoginPanel.setOpaque(false);
        toLoginPanel.add(new CustomLabel("Already have an account?"));
        toLoginPanel.add(new CustomLink("Login here!", () -> authFrame.showLogin()));
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.add(toLoginPanel);
        
        footerPanel.add(registerButton, BorderLayout.CENTER);
        footerPanel.add(bottomPanel, BorderLayout.SOUTH);
        formPanel.add(footerPanel, BorderLayout.SOUTH);
        
        registerButton.addActionListener(e -> handleRegister());
        setOpaque(false);
        add(formPanel);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(authFrame, message, "Registration Error", JOptionPane.ERROR_MESSAGE);
    }

    // =========================
    // HANDLE REGISTER
    // =========================
    private void handleRegister() {
        String studentId = studentIdField.getText().trim();
        String email = umakEmailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String course = courseField.getText().trim();
        String college = (String) collegeBox.getSelectedItem();
        String yearLevel = (String) yearLevelBox.getSelectedItem();
        String password = String.valueOf(passwordField.getPassword());
        
        // ── STEP 1: Empty field check ──
        if (studentId.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || course.isEmpty()
                || "Select College".equals(college) || "Select Year".equals(yearLevel) || password.isEmpty()) {
            showError("Please complete all fields.");
            return;
        }
        
        // ── STEP 2: Student ID format validation ──
        if (!studentId.matches("(?i)^[KkAa][0-9]{8}$")) {
            showError("Invalid Student ID format.\nMust start with K or A followed by 8 digits.");
            return;
        }
        
        // ── STEP 3: UMak email validation ──
        if (!email.endsWith("@umak.edu.ph")) {
            showError("Invalid Email.\nPlease use a valid @umak.edu.ph email address.");
            return;
        }
        
        // ── STEP 4: Password strength check ──
        PasswordStrength strength = getPasswordStrength(password);
        if (strength == PasswordStrength.WEAK) {
            showError("Weak password!\nPlease add uppercase letters, numbers, and symbols.");
            return;
        }
        if (strength == PasswordStrength.MEDIUM) {
            showError("Medium password!\nPlease improve password security before continuing.");
            return;
        }
        if (strength == PasswordStrength.STRONG) {
            JOptionPane.showMessageDialog(authFrame,
                    "Your password is STRONG.\nRecommendation:\n" + "- Add more symbols\n- Increase password length\n"
                            + "- Mix more uppercase letters",
                    "Password Recommendation", JOptionPane.INFORMATION_MESSAGE);
        }
        
        // ── STEP 5: Duplicate checks ──
        if (studentIdExists(studentId)) {
            showError("Student ID is already registered.");
            return;
        }
        if (emailExists(email)) {
            showError("Email is already registered.");
            return;
        }
        
        // ── STEP 6: Save to DB ──
        int newUserId = saveUserToDatabase(studentId, email, password, college, yearLevel, course, firstName, lastName);
        if (newUserId == -1) return;
        
        // ── STEP 7: Security questions ──
        SecurityDialog securityDialog = new SecurityDialog(authFrame, newUserId);
        if (!securityDialog.isSubmitted()) {
            deleteUser(newUserId);
            showError("Registration cancelled — security questions are required.");
            return;
        }
        
        // ── STEP 8: Log and finish ──
        try {
            LogsDatabase logDB = new LogsDatabase();
            logDB.insertLog(UserLogAction.USER_CREATE, newUserId, "");
        } catch (Exception ex) {
            System.err.println("Registration log failed (non-fatal): " + ex.getMessage());
        }
        JOptionPane.showMessageDialog(authFrame, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
        authFrame.showLogin();
    }

    // =========================
    // DUPLICATE CHECKS
    // =========================
    private boolean studentIdExists(String studentId) {
        return fieldExists("student_id", studentId);
    }

    private boolean emailExists(String email) {
        return fieldExists("umak_email_address", email);
    }

    private boolean fieldExists(String column, String value) {
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
    // SAVE USER
    // =========================
    private int saveUserToDatabase(String studentId, String email, String password, String college, String yearLevel,
            String course, String firstName, String lastName) {
        String sql = "INSERT INTO users " + "(student_id, system_role, umak_email_address, password, "
                + " college, year_level, course_program, first_name, last_name, " + " karma_score, profile_image) "
                + "VALUES (?, 'end_user', ?, ?, ?, ?, ?, ?, ?, 0, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            String encryptedPassword = Encryption.encryptPassword(password);
            if (encryptedPassword == null) {
                showError("Encryption error — please try again.");
                return -1;
            }
            ps.setString(1, studentId);
            ps.setString(2, email);
            ps.setString(3, encryptedPassword);
            ps.setString(4, college);
            ps.setString(5, yearLevel);
            ps.setString(6, course);
            ps.setString(7, firstName);
            ps.setString(8, lastName);
            ps.setString(9, "/resources/defaultpictures/axolotl.jpg");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error — please try again.");
        }
        return -1;
    }

    // =========================
    // ROLLBACK
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
        case WEAK -> {
            passwordField.setForeground(Color.RED);
            passwordStrengthLabel.setForeground(Color.RED);
            passwordStrengthLabel.setText("Weak Password");
        }
        case MEDIUM -> {
            passwordField.setForeground(Color.ORANGE);
            passwordStrengthLabel.setForeground(Color.ORANGE);
            passwordStrengthLabel.setText("Medium Password");
        }
        case STRONG -> {
            passwordField.setForeground(Color.BLUE);
            passwordStrengthLabel.setForeground(Color.BLUE);
            passwordStrengthLabel.setText("Strong Password");
        }
        case VERY_STRONG -> {
            passwordField.setForeground(new Color(0, 150, 0));
            passwordStrengthLabel.setForeground(new Color(0, 150, 0));
            passwordStrengthLabel.setText("Very Strong Password");
        }
        }
    }

    private PasswordStrength getPasswordStrength(String password) {
        if (password.length() <= 5) return PasswordStrength.WEAK;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNum = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");
        int score = 0;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasNum) score++;
        if (hasSpecial) score++;
        if (password.length() >= 10) score++;
        
        if (score <= 2) return PasswordStrength.WEAK;
        if (score == 3) return PasswordStrength.MEDIUM;
        if (score == 4) return PasswordStrength.STRONG;
        return PasswordStrength.VERY_STRONG;
    }

    // =========================
    // FIELD GROUP BUILDER
    // =========================
    private JPanel createFieldGroup(String labelText, FontStyle style, float size, JComponent field) {
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
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        return panel;
    }
}