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

    private CustomTextField     studentIdField;
    private CustomPasswordField passwordField;
    private CustomButton        loginButton;
    private Auth                authFrame;
    private CustomLabel         studentIdError;
    private CustomLabel         passwordError;
    private CustomLabel         loginEmptyError;

    private static final String DB_URL      = "jdbc:mysql://localhost:4306/dbnorm2";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

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
        passwordField  = new CustomPasswordField(20, 15);

        formFieldsPanel.add(createFieldGroup(
                "Student ID: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, studentIdField));

        studentIdError = new CustomLabel("");
        studentIdError.setForeground(Color.RED);
        formFieldsPanel.add(studentIdError);

        formFieldsPanel.add(createFieldGroup(
                "Password: *", FontStyle.BOLD, Brand.STANDARD_TEXT_SIZE, passwordField));

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

        loginButton     = new CustomButton("Login", 20);
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

        loginButton.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String password  = String.valueOf(passwordField.getPassword()).trim();

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

          
            UserRecord user = fetchUser(studentId);
            if (user == null) {
                studentIdError.setText("Student ID doesn't exist or account is archived.");
                return;
            }

         
            SessionManager.login(
                user.user_id,
                user.system_role,
                user.first_name + " " + user.last_name
            );
            try {
                LogsDatabase logDB = new LogsDatabase();
                logDB.insertLog(UserLogAction.USER_LOGIN, user.user_id, "");
            } catch (Exception ex) {
                System.err.println("Login log failed (non-fatal): " + ex.getMessage());
            }

            this.authFrame.setContentPane(new Page(new Marketplace()));
            this.authFrame.revalidate();
            this.authFrame.repaint();
        });

        footerPanel.add(loginButton,  BorderLayout.CENTER);
        footerPanel.add(bottomPanel,  BorderLayout.SOUTH);
        formPanel.add(footerPanel,    BorderLayout.SOUTH);

        setOpaque(false);
        add(formPanel);
    }

   
    private UserRecord fetchUser(String studentId) {
        String sql = "SELECT * FROM users "
                   + "WHERE student_id = ? AND users_is_archived = false";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserRecord u      = new UserRecord();
                u.user_id         = rs.getInt("user_id");
                u.student_id      = rs.getString("student_id");
                u.system_role     = rs.getString("system_role");
                u.umak_email_address = rs.getString("umak_email_address");
                u.password        = rs.getString("password");
                u.college         = rs.getString("college");
                u.year_level      = rs.getString("year_level");
                u.course_program  = rs.getString("course_program");
                u.first_name      = rs.getString("first_name");
                u.last_name       = rs.getString("last_name");
                u.karma_score     = rs.getInt("karma_score");
                u.profile_image   = rs.getString("profile_image");
                u.contact_num     = rs.getLong("contact_num");
                u.home_address    = rs.getString("home_address");
                u.users_is_archived = rs.getBoolean("users_is_archived");
                return u;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JPanel createFieldGroup(
            String labelText, FontStyle style, float size, JComponent field) {

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