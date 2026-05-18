package admin.tabledialogs;

import admin.AdminDialog;
import admin.models.AdminUsers;
import admin.services.AdminUsersServices;
import utils.*;
import components.*;
import database.UMak;
import enums.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UserFormDialog {

    public static void showAdd(Window owner, AdminUsersServices userService, Runnable onSuccess) {
        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField studentIdField = new CustomTextField("");
        CustomTextField firstNameField = new CustomTextField("");
        CustomTextField lastNameField  = new CustomTextField("");
        CustomTextField emailField     = new CustomTextField("");
        CustomTextField passwordField  = new CustomTextField("");

        CustomComboBox<String> yearLevelBox = new CustomComboBox<>(new String[]{
            "First", "Second", "Third", "Fourth", "Fifth", "Graduate" });
        CustomComboBox<String> collegeBox = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES);
        CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(
            new String[]{ "Standard User", "Admin" });
        CustomComboBox<String> roleSubBox = new CustomComboBox<>(
            new String[]{ "admin", "super_admin" });
        roleSubBox.setCustomSize(110, 32);
        roleSubBox.setVisible(false);

        roleCategoryBox.addActionListener(e -> {
            boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
            roleSubBox.setVisible(isAdmin);
            roleSubBox.getParent().revalidate();
            roleSubBox.getParent().repaint();
        });

        JPanel roleComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleComboPanel.setOpaque(false);
        roleComboPanel.setPreferredSize(new Dimension(240, 32)); // STEP 3
        roleCategoryBox.setPreferredSize(new Dimension(120, 32));
        roleSubBox.setPreferredSize(new Dimension(110, 32));
        roleComboPanel.add(roleCategoryBox);
        roleComboPanel.add(roleSubBox);

        JPanel roleRow = buildManualRow("System Role:", roleComboPanel); // STEP 2

      
        final String[] profileImageHolder = { "" };
        CustomTextField profileImageField = new CustomTextField("No image selected");
        profileImageField.setEditable(false);
        profileImageField.setPreferredSize(new Dimension(150, 32));
        profileImageField.setMinimumSize(new Dimension(150, 32));

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

        JPanel profileImageInputPanel = new JPanel(new BorderLayout(6, 0));
        profileImageInputPanel.setOpaque(false);
        profileImageInputPanel.add(profileImageField, BorderLayout.CENTER);
        profileImageInputPanel.add(profileBrowseBtn,  BorderLayout.EAST);

        JPanel profileImageRow = makeImageRow("Profile Image:", profileImageInputPanel); // STEP 2

    
        formPanel.add(createFieldPanel("Student ID:",  studentIdField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("First Name:",  firstNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",   lastNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Email:",       emailField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Password:",    passwordField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Year Level:",  yearLevelBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",     collegeBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(roleRow);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(profileImageRow);

        new AdminDialog(owner, "Create New User", formPanel, "Create",
            e -> {
                String studentId = studentIdField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName  = lastNameField.getText().trim();
                String email     = emailField.getText().trim();
                String password  = passwordField.getText().trim();
                String yearLevel = yearLevelBox.getSelectedItem() != null
                                       ? yearLevelBox.getSelectedItem().toString() : "";
                String college   = collegeBox.getSelectedItem() != null
                                       ? collegeBox.getSelectedItem().toString() : "";
                String imagePath = profileImageHolder[0];

                if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                        || email.isEmpty() || password.isEmpty() || college.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        "Error: Please complete all required fields.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!email.toLowerCase().endsWith("@umak.edu.ph")) {
                    JOptionPane.showMessageDialog(null,
                        "Error: Invalid email address format!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String resolvedRole = "Admin".equals(roleCategoryBox.getSelectedItem())
                        ? (roleSubBox.getSelectedItem() != null
                               ? roleSubBox.getSelectedItem().toString() : "admin")
                        : "end_user";

                boolean success = userService.addUser(
                    studentId, firstName, lastName, email, password,
                    yearLevel, college, resolvedRole, imagePath);

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
        roleSubBox.setCustomSize(110, 32);

        if (currentRole.equals("end_user")) {
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

        JPanel roleComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleComboPanel.setOpaque(false);
        roleComboPanel.setPreferredSize(new Dimension(240, 32)); // STEP 3
        roleCategoryBox.setPreferredSize(new Dimension(120, 32));
        roleSubBox.setPreferredSize(new Dimension(110, 32));
        roleComboPanel.add(roleCategoryBox);
        roleComboPanel.add(roleSubBox);

        JPanel roleRow = buildManualRow("System Role:", roleComboPanel); // STEP 2

        formPanel.add(createFieldPanel("First Name:", firstNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",  lastNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",    collegeBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(roleRow);

        AdminDialog[] dialog = { null };
        dialog[0] = new AdminDialog(owner, "Update User: " + userId, formPanel, "Save Changes",
            e -> {
                if (user == null) return;

                String resolvedFirst = firstNameField.getText().trim().isEmpty()
                    ? user.getFirstName() : firstNameField.getText().trim();
                String resolvedLast  = lastNameField.getText().trim().isEmpty()
                    ? user.getLastName()  : lastNameField.getText().trim();
                String resolvedCollege = (collegeBox.getSelectedItem() == null)
                    ? user.getCollege()   : collegeBox.getSelectedItem().toString().trim();
                String resolvedRole = "Admin".equals(roleCategoryBox.getSelectedItem())
                        ? (roleSubBox.getSelectedItem() != null
                               ? roleSubBox.getSelectedItem().toString() : "admin")
                        : "end_user";

                if (resolvedFirst.isEmpty() || resolvedLast.isEmpty() || resolvedCollege.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        "Error: Please complete all required fields.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

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


    private static JPanel createFieldPanel(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setPreferredSize(new Dimension(0, 35));

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(120, 30));          
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);      

        panel.add(lbl, BorderLayout.WEST);

        component.setPreferredSize(new Dimension(240, 32));    
        component.setMinimumSize(new Dimension(240, 32));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(component, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);
        return panel;
    }

   
    private static JPanel buildManualRow(String labelText, JComponent inputComponent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.setPreferredSize(new Dimension(0, 35));

        CustomLabel lbl = new CustomLabel(labelText, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(120, 30));           // STEP 2: matches createFieldPanel
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(inputComponent, BorderLayout.CENTER);
        row.add(wrap, BorderLayout.CENTER);
        return row;
    }

   
    private static JPanel makeImageRow(String labelText, JPanel inputPanel) {
        return buildManualRow(labelText, inputPanel);
    }
}