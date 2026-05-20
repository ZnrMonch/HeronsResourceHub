package admin.tabledialogs;

import admin.AdminDialog;
import admin.models.AdminUsers;
import admin.services.AdminUsersServices;
import utils.*;
import components.*;
import database.UMak;
import enums.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import auth.*;

// Static factory for the add and update user dialogs
public class UserFormDialog {

    private static final int LABEL_WIDTH  = 120;
    private static final int FIELD_WIDTH  = 280;
    private static final int FIELD_HEIGHT = 35;
    private static final int ROW_HEIGHT   = 40;

    private enum PasswordStrength { WEAK, MEDIUM, STRONG, VERY_STRONG }

    // ── Init ──

    public static void showAdd(Window owner, AdminUsersServices userService, Runnable onSuccess) {
        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField studentIdField = new CustomTextField("");
        CustomTextField firstNameField = new CustomTextField("");
        CustomTextField lastNameField  = new CustomTextField("");
        CustomTextField emailField     = new CustomTextField("");

        JPasswordField passwordField = new JPasswordField();
        passwordField.setEchoChar((char) 0);
        passwordField.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        passwordField.setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));

        CustomLabel passwordStrengthLabel = new CustomLabel(
            "", Brand.STANDARD_TEXT_SIZE - 2f, FontStyle.ITALIC);

        // Spacer aligns strength label under the field column, not the label column
        JPanel strengthRow = new JPanel(new BorderLayout());
        strengthRow.setOpaque(false);
        strengthRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JPanel strengthSpacer = new JPanel();
        strengthSpacer.setOpaque(false);
        strengthSpacer.setPreferredSize(new Dimension(LABEL_WIDTH + 15, 18));
        strengthRow.add(strengthSpacer,        BorderLayout.WEST);
        strengthRow.add(passwordStrengthLabel, BorderLayout.CENTER);

        CustomComboBox<String> yearLevelBox = new CustomComboBox<>(new String[]{
            "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "Graduate", "Alumni"
        });
        CustomComboBox<String> collegeBox = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES);

        CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(
            new String[]{ "Standard User", "Admin" });
        CustomComboBox<String> roleSubBox = new CustomComboBox<>(
            new String[]{ "admin", "super_admin" });
        roleSubBox.setPreferredSize(new Dimension(110, FIELD_HEIGHT));
        roleSubBox.setVisible(false);

        roleCategoryBox.addActionListener(e -> {
            boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
            roleSubBox.setVisible(isAdmin);
            roleSubBox.getParent().revalidate();
            roleSubBox.getParent().repaint();
        });

        // BorderLayout so roleCategoryBox stretches to fill the row width
        JPanel roleComboPanel = new JPanel(new BorderLayout(6, 0));
        roleComboPanel.setOpaque(false);
        roleComboPanel.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        roleComboPanel.add(roleCategoryBox, BorderLayout.CENTER);
        roleComboPanel.add(roleSubBox,      BorderLayout.EAST);

        final String[] profileImageHolder = { "" };
        CustomTextField profileImageField = new CustomTextField("No image selected");
        profileImageField.setEditable(false);

        CustomButton profileBrowseBtn = new CustomButton("Browse", 6);
        profileBrowseBtn.setFontSize(11f);
        profileBrowseBtn.setPadding(4, 10, 4, 10);
        profileBrowseBtn.setDefaultColor(Color.decode("#6c757d"));
        profileBrowseBtn.setTextColor(Color.WHITE);
        profileBrowseBtn.setHoverColor(Color.decode("#5a6268"));
        profileBrowseBtn.addActionListener(e -> {
            String path = ImageBrowserHelper.browse(owner);
            if (!path.isEmpty()) {
                profileImageHolder[0] = path;
                profileImageField.setText(
                    path.substring(path.lastIndexOf(java.io.File.separator) + 1));
            }
        });

        // BorderLayout so profileImageField stretches and Browse button stays right
        JPanel profileImageInputPanel = new JPanel(new BorderLayout(6, 0));
        profileImageInputPanel.setOpaque(false);
        profileImageInputPanel.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        profileImageInputPanel.add(profileImageField, BorderLayout.CENTER);
        profileImageInputPanel.add(profileBrowseBtn,  BorderLayout.EAST);

        formPanel.add(createFieldPanel("Student ID:",  studentIdField,         true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("First Name:",  firstNameField,         true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",   lastNameField,          true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Email:",       emailField,             true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Password:",    passwordField,          true));
        formPanel.add(strengthRow);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(createFieldPanel("Year Level:",  yearLevelBox,           true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",     collegeBox,             true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buildManualRow("System Role:",   roleComboPanel,         true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buildManualRow("Profile Image:", profileImageInputPanel, true));

        // Live strength indicator updates on every keystroke
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String pw = new String(passwordField.getPassword());
                if (pw.isEmpty()) {
                    passwordField.setForeground(Color.BLACK);
                    passwordStrengthLabel.setText("");
                    return;
                }
                updatePasswordStrength(pw, passwordField, passwordStrengthLabel);
            }
            @Override public void insertUpdate(DocumentEvent e)  { update(); }
            @Override public void removeUpdate(DocumentEvent e)  { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        new AdminDialog(owner, "Create New User", formPanel, "Create", e -> {
            String studentId = studentIdField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName  = lastNameField.getText().trim();
            String email     = emailField.getText().trim();
            String password  = new String(passwordField.getPassword()).trim();
            String yearLevel = comboValue(yearLevelBox, "");
            String college   = comboValue(collegeBox,   "");
            String imagePath = profileImageHolder[0];

            // STEP 1: Required fields
            if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                    || email.isEmpty() || password.isEmpty() || college.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Error: Please complete all required fields.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // STEP 2: Student ID format — K or A followed by exactly 8 digits
            if (!studentId.toUpperCase().matches("^[KA][0-9]{8}$")) {
                JOptionPane.showMessageDialog(null,
                    "Error: Invalid Student ID! Expected format: K12345678 or A12345678.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // STEP 3: UMak institutional email
            if (!email.matches("^[\\w.\\-]+@umak\\.edu\\.ph$")) {
                JOptionPane.showMessageDialog(null,
                    "Error: Invalid UMak email! Must follow the pattern: name@umak.edu.ph",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // STEP 4: Password strength — WEAK and MEDIUM are hard blocks
            PasswordStrength strength = getPasswordStrength(password);
            if (strength == PasswordStrength.WEAK) {
                JOptionPane.showMessageDialog(null,
                    "Error: Weak password! Add uppercase letters, numbers, and symbols.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (strength == PasswordStrength.MEDIUM) {
                JOptionPane.showMessageDialog(null,
                    "Error: Medium password! Improve password security before continuing.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            

            // STEP 5: Profile image required
            if (imagePath.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Error: Please select a profile image for the user.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Encrypt the plain-text password before it reaches the database
            String encrypted = Encryption.encryptPassword(password);
            if (encrypted == null) {
                JOptionPane.showMessageDialog(null,
                    "Error: Failed to encrypt password! Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userService.addUser(
                studentId, firstName, lastName, email, encrypted,
                yearLevel, college, resolveRole(roleCategoryBox, roleSubBox), imagePath);

            if (success) {
                onSuccess.run();
                JOptionPane.showMessageDialog(null,
                    "Success! The user has been added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.getWindowAncestor((Component) e.getSource()).dispose();
            } else {
                String reason = userService.getLastAddError();
                JOptionPane.showMessageDialog(null,
                    reason != null
                        ? "Error: " + reason + "! Please try again."
                        : "Error: Failed to add user! Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).setVisible(true);
    }

    public static void showUpdate(Window owner, int userId,
            AdminUsersServices userService, Runnable onSuccess) {

        AdminUsers user = userService.getUserById(userId);

        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField firstNameField = new CustomTextField(user != null ? user.getFirstName() : "");
        CustomTextField lastNameField  = new CustomTextField(user != null ? user.getLastName()  : "");

        CustomComboBox<String> collegeBox = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES);
        if (user != null) collegeBox.setSelectedItem(user.getCollege());

        String currentRole = (user != null && user.getSystemRole() != null)
            ? user.getSystemRole() : "end_user";

        CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(
            new String[]{ "Standard User", "Admin" });
        CustomComboBox<String> roleSubBox = new CustomComboBox<>(
            new String[]{ "admin", "super_admin" });
        roleSubBox.setPreferredSize(new Dimension(110, FIELD_HEIGHT));

        // Pre-select the correct category and sub-role based on the existing record
        if ("end_user".equals(currentRole)) {
            roleCategoryBox.setSelectedItem("Standard User");
            roleSubBox.setVisible(false);
        } else {
            roleCategoryBox.setSelectedItem("Admin");
            roleSubBox.setSelectedItem(currentRole);
        }

        roleCategoryBox.addActionListener(e -> {
            boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
            roleSubBox.setVisible(isAdmin);
            roleSubBox.getParent().revalidate();
            roleSubBox.getParent().repaint();
        });

        JPanel roleComboPanel = new JPanel(new BorderLayout(6, 0));
        roleComboPanel.setOpaque(false);
        roleComboPanel.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        roleComboPanel.add(roleCategoryBox, BorderLayout.CENTER);
        roleComboPanel.add(roleSubBox,      BorderLayout.EAST);

        formPanel.add(createFieldPanel("First Name:", firstNameField, true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",  lastNameField,  true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",    collegeBox,     true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buildManualRow("System Role:",  roleComboPanel, true));

        AdminDialog[] dialog = { null };
        dialog[0] = new AdminDialog(owner, "Update User: " + userId, formPanel, "Save Changes", e -> {
            if (user == null) return;

            String resolvedFirst   = firstNameField.getText().trim().isEmpty()
                ? user.getFirstName() : firstNameField.getText().trim();
            String resolvedLast    = lastNameField.getText().trim().isEmpty()
                ? user.getLastName()  : lastNameField.getText().trim();
            String resolvedCollege = collegeBox.getSelectedItem() == null
                ? user.getCollege()   : collegeBox.getSelectedItem().toString().trim();
            String resolvedRole    = resolveRole(roleCategoryBox, roleSubBox);

            if (resolvedFirst.isEmpty() || resolvedLast.isEmpty() || resolvedCollege.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Error: Please complete all required fields.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Dismiss the form before showing the confirm dialog so they don't stack
            dialog[0].dispose();
            int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to update User ID " + userId + "?",
                "Confirm Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            user.setFirstName(resolvedFirst);
            user.setLastName(resolvedLast);
            user.setCollege(resolvedCollege);
            user.setSystemRole(resolvedRole);

            boolean success = userService.updateUser(user);
            if (success) {
                onSuccess.run();
                JOptionPane.showMessageDialog(null,
                    "Success! The user has been updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    "Error: Failed to update user information! Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog[0].setVisible(true);
    }

    // ── Helpers ──

    // Scores on four criteria: length ≥ 8, uppercase, digit, special character
    private static PasswordStrength getPasswordStrength(String pw) {
        if (pw == null || pw.length() < 8) return PasswordStrength.WEAK;

        int score = 0;
        if (pw.length() >= 8)                               score++;
        if (pw.chars().anyMatch(Character::isUpperCase))    score++;
        if (pw.chars().anyMatch(Character::isDigit))        score++;
        if (pw.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;

        return switch (score) {
            case 4  -> PasswordStrength.VERY_STRONG;
            case 3  -> PasswordStrength.STRONG;
            case 2  -> PasswordStrength.MEDIUM;
            default -> PasswordStrength.WEAK;
        };
    }

    // Updates field foreground and strength label text + color to match current strength
    private static void updatePasswordStrength(String pw,
                                               JPasswordField field,
                                               CustomLabel label) {
        PasswordStrength strength = getPasswordStrength(pw);
        switch (strength) {
            case WEAK -> {
                field.setForeground(Color.RED);
                label.setForeground(Color.RED);
           
            }
            case MEDIUM -> {
                field.setForeground(Color.ORANGE);
                label.setForeground(Color.ORANGE);
       
            }
            case STRONG -> {
                field.setForeground(Color.BLUE);
                label.setForeground(Color.BLUE);

            }
            case VERY_STRONG -> {
                field.setForeground(new Color(0, 150, 0));
                label.setForeground(new Color(0, 150, 0));
               
            }
        }
    }

    // Returns "end_user" for Standard User, or the sub-role selection for Admin
    private static String resolveRole(CustomComboBox<String> categoryBox,
                                      CustomComboBox<String> subBox) {
        if ("Admin".equals(categoryBox.getSelectedItem()))
            return subBox.getSelectedItem() != null ? subBox.getSelectedItem().toString() : "admin";
        return "end_user";
    }

    // Null-safe combo read; returns fallback when nothing is selected
    private static String comboValue(CustomComboBox<String> box, String fallback) {
        Object val = box.getSelectedItem();
        return val != null ? val.toString() : fallback;
    }

    private static JPanel createFieldPanel(String label, JComponent component, boolean required) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH + 20, ROW_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        labelRow.setOpaque(false);
        labelRow.setPreferredSize(new Dimension(LABEL_WIDTH, 30));

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        labelRow.add(lbl);

        if (required) {
            CustomLabel star = new CustomLabel("*");
            star.setForeground(Color.RED);
            star.setFont(lbl.getFont());
            labelRow.add(star);
        }
        panel.add(labelRow, BorderLayout.WEST);

        component.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        component.setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(component, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);
        return panel;
    }

    // Identical layout to createFieldPanel but skips the fixed FIELD_WIDTH constraint
    // so composite inputs (role combos, image field + browse button) can size themselves freely
    private static JPanel buildManualRow(String labelText, JComponent inputComponent, boolean required) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH + 20, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        labelRow.setOpaque(false);
        labelRow.setPreferredSize(new Dimension(LABEL_WIDTH, 30));

        CustomLabel lbl = new CustomLabel(labelText, 14f, FontStyle.REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        labelRow.add(lbl);

        if (required) {
            CustomLabel star = new CustomLabel("*");
            star.setForeground(Color.RED);
            star.setFont(lbl.getFont());
            labelRow.add(star);
        }
        row.add(labelRow, BorderLayout.WEST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(inputComponent, BorderLayout.CENTER);
        row.add(wrap, BorderLayout.CENTER);
        return row;
    }
}