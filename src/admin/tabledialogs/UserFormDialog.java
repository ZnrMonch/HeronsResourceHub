package admin.tabledialogs;

import admin.AdminDialog;
import admin.models.AdminUsers;
import admin.services.AdminUsersServices;
import utils.*;
import components.*;
import database.UMak;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UserFormDialog {

    // Opens the Add New User dialog
    public static void showAdd(Window owner, AdminUsersServices userService, Runnable onSuccess) {
        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField studentIdField = new CustomTextField("");
        CustomTextField firstNameField = new CustomTextField("");
        CustomTextField lastNameField  = new CustomTextField("");
        CustomTextField emailField     = new CustomTextField("");
        CustomTextField passwordField  = new CustomTextField("");

        CustomComboBox<String> yearLevelBox = new CustomComboBox<>(new String[] {
            "First", "Second", "Third", "Fourth", "Fifth", "Graduate"
        });
        CustomComboBox<String> collegeBox = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES);
        CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(
            new String[] { "Standard User", "Admin" });
        CustomComboBox<String> roleSubBox = new CustomComboBox<>(
            new String[] { "admin", "super_admin" });
        roleSubBox.setCustomSize(130, 30);
        roleSubBox.setVisible(false);

        roleCategoryBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
                roleSubBox.setVisible(isAdmin);
                roleSubBox.getParent().revalidate();
                roleSubBox.getParent().repaint();
            }
        });

        JPanel roleComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleComboPanel.setOpaque(false);
        roleCategoryBox.setPreferredSize(new Dimension(140, 32));
        roleSubBox.setPreferredSize(new Dimension(130, 32));
        roleComboPanel.add(roleCategoryBox);
        roleComboPanel.add(roleSubBox);

        JPanel roleRow = new JPanel(new BorderLayout(10, 0));
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        CustomLabel roleLabel = new CustomLabel("System Role:", 14f, FontStyle.REGULAR);
        roleLabel.setPreferredSize(new Dimension(100, 30));
        roleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        roleRow.add(roleLabel,      BorderLayout.WEST);
        roleRow.add(roleComboPanel, BorderLayout.CENTER);

        final String[] profileImageHolder = { "" };

        CustomTextField profileImageField = new CustomTextField("No image selected");
        profileImageField.setEditable(false);
        profileImageField.setPreferredSize(new Dimension(170, 32));
        profileImageField.setMinimumSize(new Dimension(170, 32));

        CustomButton profileBrowseBtn = new CustomButton("Browse", 6);
        profileBrowseBtn.setFontSize(11f);
        profileBrowseBtn.setPadding(4, 10, 4, 10);
        profileBrowseBtn.setDefaultColor(Color.decode("#6c757d"));
        profileBrowseBtn.setTextColor(Color.WHITE);
        profileBrowseBtn.setHoverColor(Color.decode("#5a6268"));
        profileBrowseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = ImageBrowserHelper.browse(owner);
                if (!path.isEmpty()) {
                    profileImageHolder[0] = path;
                    profileImageField.setText(
                        path.substring(path.lastIndexOf(java.io.File.separator) + 1));
                }
            }
        });

        JPanel profileImageInputPanel = new JPanel(new BorderLayout(6, 0));
        profileImageInputPanel.setOpaque(false);
        profileImageInputPanel.add(profileImageField, BorderLayout.CENTER);
        profileImageInputPanel.add(profileBrowseBtn,  BorderLayout.EAST);

        JPanel profileImageRow = new JPanel(new BorderLayout(10, 0));
        profileImageRow.setOpaque(false);
        profileImageRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        profileImageRow.setPreferredSize(new Dimension(0, 35));
        CustomLabel profileImageLabel = new CustomLabel("Profile Image:", 14f, FontStyle.REGULAR);
        profileImageLabel.setPreferredSize(new Dimension(100, 30));
        profileImageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel profileImageWrap = new JPanel(new BorderLayout());
        profileImageWrap.setOpaque(false);
        profileImageWrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        profileImageWrap.add(profileImageInputPanel, BorderLayout.CENTER);
        profileImageRow.add(profileImageLabel, BorderLayout.WEST);
        profileImageRow.add(profileImageWrap,  BorderLayout.CENTER);

        formPanel.add(createFieldPanel("Student ID:", studentIdField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("First Name:", firstNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",  lastNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Email:",      emailField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Password:",   passwordField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Year Level:", yearLevelBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",    collegeBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(roleRow);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(profileImageRow);

        new AdminDialog(owner, "Add New User", formPanel, "Add User",
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                            "Student ID, First Name, Last Name, Email, Password, and College are required.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!email.toLowerCase().endsWith("@umak.edu.ph")) {
                        JOptionPane.showMessageDialog(null,
                            "Email must be a valid UMak address ending in @umak.edu.ph\n"
                            + "Example: firstname.lastname@umak.edu.ph",
                            "Invalid Email", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String resolvedRole;
                    if ("Admin".equals(roleCategoryBox.getSelectedItem())) {
                        resolvedRole = (roleSubBox.getSelectedItem() != null)
                            ? roleSubBox.getSelectedItem().toString() : "admin";
                    } else {
                        resolvedRole = "end_user";
                    }

                    boolean success = userService.addUser(
                        studentId, firstName, lastName, email, password,
                        yearLevel, college, resolvedRole, imagePath
                    );

                    if (success) {
                        onSuccess.run();
                        JOptionPane.showMessageDialog(null, "User added successfully.");
                        SwingUtilities.getWindowAncestor((Component) e.getSource()).dispose();
                    } else {
                        String reason = userService.getLastAddError();
                        JOptionPane.showMessageDialog(null,
                            reason != null ? reason : "Failed to add user. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }).setVisible(true);
    }

    // Opens the Update User dialog pre-filled with existing data
    public static void showUpdate(Window owner, int userId,
            AdminUsersServices userService, Runnable onSuccess) {

        AdminUsers user = userService.getUserById(userId);

        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField firstNameField = new CustomTextField(user != null ? user.getFirstName() : "");
        CustomTextField lastNameField  = new CustomTextField(user != null ? user.getLastName()  : "");

        CustomComboBox<String> collegeBox = new CustomComboBox<>(UMak.COLLEGES_INSTITUTES);
        if (user != null) {
            collegeBox.setSelectedItem(user.getCollege());
        }

        String currentRole = (user != null && user.getSystemRole() != null)
                                 ? user.getSystemRole() : "end_user";
        CustomComboBox<String> roleCategoryBox = new CustomComboBox<>(
            new String[] { "Standard User", "Admin" });
        CustomComboBox<String> roleSubBox = new CustomComboBox<>(
            new String[] { "admin", "super_admin" });
        roleSubBox.setCustomSize(130, 30);

        if (currentRole.equals("end_user")) {
            roleCategoryBox.setSelectedItem("Standard User");
            roleSubBox.setVisible(false);
        } else {
            roleCategoryBox.setSelectedItem("Admin");
            roleSubBox.setSelectedItem(currentRole);
        }

        roleCategoryBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isAdmin = "Admin".equals(roleCategoryBox.getSelectedItem());
                roleSubBox.setVisible(isAdmin);
                roleSubBox.getParent().revalidate();
                roleSubBox.getParent().repaint();
            }
        });

        JPanel roleComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleComboPanel.setOpaque(false);
        roleCategoryBox.setPreferredSize(new Dimension(140, 32));
        roleSubBox.setPreferredSize(new Dimension(130, 32));
        roleComboPanel.add(roleCategoryBox);
        roleComboPanel.add(roleSubBox);

        JPanel roleRow = new JPanel(new BorderLayout(10, 0));
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        CustomLabel roleLabel = new CustomLabel("System Role:", 14f, FontStyle.REGULAR);
        roleLabel.setPreferredSize(new Dimension(100, 30));
        roleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        roleRow.add(roleLabel,      BorderLayout.WEST);
        roleRow.add(roleComboPanel, BorderLayout.CENTER);

        formPanel.add(createFieldPanel("First Name:", firstNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Last Name:",  lastNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("College:",    collegeBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(roleRow);

        new AdminDialog(owner, "Update User: " + userId, formPanel, "Save Changes",
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (user == null) return;

                    // Use existing value if the field was left empty
                    String resolvedFirst = firstNameField.getText().trim().isEmpty()
                        ? user.getFirstName() : firstNameField.getText().trim();
                    String resolvedLast = lastNameField.getText().trim().isEmpty()
                        ? user.getLastName() : lastNameField.getText().trim();
                    String resolvedCollege = (collegeBox.getSelectedItem() == null)
                        ? user.getCollege() : collegeBox.getSelectedItem().toString().trim();

                    // Determine final role string
                    String resolvedRole;
                    if ("Admin".equals(roleCategoryBox.getSelectedItem())) {
                        resolvedRole = (roleSubBox.getSelectedItem() != null)
                            ? roleSubBox.getSelectedItem().toString() : "admin";
                    } else {
                        resolvedRole = "end_user";
                    }

                    if (resolvedFirst.isEmpty() || resolvedLast.isEmpty() || resolvedCollege.isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                            "First Name, Last Name, and College cannot be empty.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    user.setFirstName(resolvedFirst);
                    user.setLastName(resolvedLast);
                    user.setCollege(resolvedCollege);
                    user.setSystemRole(resolvedRole);

                    boolean success = userService.updateUser(user);
                    if (success) {
                        onSuccess.run();
                        JOptionPane.showMessageDialog(null, "User updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Update failed. Please try again.");
                    }
                }
            }).setVisible(true);
    }

    // Creates a label + input field row used inside dialogs
    private static JPanel createFieldPanel(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setPreferredSize(new Dimension(0, 35));

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(100, 30));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl, BorderLayout.WEST);

        component.setPreferredSize(new Dimension(250, 32));
        component.setMinimumSize(new Dimension(250, 32));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(component, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);

        return panel;
    }
}