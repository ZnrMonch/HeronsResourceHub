package pages;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

import components.*;
import utils.*;
import profile.EditProfilePicture;
import database.UserRecord;
import database.DatabaseManager;
import database.UMak;

public class Profile extends CustomPanel {
    
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_PROFILE_PIC = "/resources/defaultpictures/axolotl.jpg";
    private static final String[] COLLEGES = UMak.COLLEGES_INSTITUTES;
    private static final String[] YEARS = {"1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "GRADUATE", "ALUMNI"};

    private final UserRecord user;
    
    private CustomToggleButton editProfileButton;
    private CustomPanel profileContentPanel;
    private AvatarPanel avatarPanel;
    
    private CustomTextField studentIdField = new CustomTextField("");
    private CustomTextField lastNameField = new CustomTextField("");
    private CustomTextField firstNameField = new CustomTextField("");
    private CustomTextField contactNumberField = new CustomTextField("");
    private CustomTextField umakEmailAddressField = new CustomTextField("");
    private CustomTextField collegeField = new CustomTextField("");
    private CustomTextField courseField = new CustomTextField("");
    private CustomTextField yearLevelField = new CustomTextField("");
    private CustomTextField gcashField = new CustomTextField("");
    private CustomTextField mayaField = new CustomTextField("");
    private CustomTextField bdoField = new CustomTextField("");
    private CustomTextField bpiField = new CustomTextField("");

    private CustomComboBox<String> collegeCombo;
    private CustomComboBox<String> yearCombo;

    private boolean isEditing = false;
    private String storedPassword = ""; 
    private String gcashNum = "", mayaNum = "", bdoNum = "", bpiNum = "";
    private Map<String, String> originalValues = new HashMap<>();

    public Profile(UserRecord user) {
        if (user == null) throw new IllegalArgumentException("UserRecord is required");
        this.user = user;
        
        refreshUserData(); 

        editProfileButton = new CustomToggleButton(
            IconLoader.loadAndScaleIcon("/resources/icons/edit.png", 25, 25),
            IconLoader.loadAndScaleIcon("/resources/icons/close.png", 25, 25)
        );

        loadPaymentMethods();

        setLayout(new BorderLayout(20, 20));
        add(initWest(), BorderLayout.WEST);
        add(initCenter(), BorderLayout.CENTER);
    }
    
    private void refreshUserData() {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.user_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.first_name = rs.getString("first_name");
                    user.last_name = rs.getString("last_name");
                    user.contact_num = rs.getString("contact_num");
                    user.college = rs.getString("college");
                    user.course_program = rs.getString("course_program");
                    user.year_level = rs.getString("year_level");
                    user.password = rs.getString("password");
                    user.profile_image = rs.getString("profile_image");
                    user.karma_score = rs.getInt("karma_score");
                    user.umak_email_address = rs.getString("umak_email_address");
                    
                    String decrypted = Encryption.decryptPassword(user.password);
                    this.storedPassword = (decrypted != null) ? decrypted : user.password;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error refreshing profile data: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
    }

    /**
     * SMART AVATAR LOADER: Bypasses IconLoader to handle newly uploaded physical files AND Classpath files perfectly.
     */
    private Icon createCircularIcon(String path, int width, int height) {
        if (path == null || path.trim().isEmpty()) path = DEFAULT_PROFILE_PIC;
        Image img = null;
        try {
            File physicalFile = new File(System.getProperty("user.dir") + "/src" + path);
            if (physicalFile.exists()) {
                img = ImageIO.read(physicalFile);
            } else {
                java.net.URL url = getClass().getResource(path);
                if (url != null) img = ImageIO.read(url);
            }
        } catch (Exception e) {}

        if (img == null) {
            try {
                java.net.URL fallback = getClass().getResource(DEFAULT_PROFILE_PIC);
                if (fallback != null) img = ImageIO.read(fallback);
            } catch (Exception e) {}
        }
        
        if (img == null) return new ImageIcon();

        BufferedImage circleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillOval(0, 0, width, height);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(img, 0, 0, width, height, null);
        g2.dispose();

        return new ImageIcon(circleBuffer);
    }

    private String getKarmaTitle(int score) {
        if (score <= 49) return "New User";
        if (score <= 99) return "Verified Beginner";
        if (score <= 199) return "Trusted Member";
        if (score <= 349) return "Active Member";
        if (score <= 499) return "Reliable Trader";
        if (score <= 649) return "Skilled Trader";
        if (score <= 799) return "Advanced Trader";
        if (score <= 949) return "Expert Trader";
        if (score <= 999) return "Elite Trader";
        return "Apex Trader"; 
    }

    private CustomPanel initWest() {
        CustomPanel wrapper = new CustomPanel(new BorderLayout(0, 20));
        wrapper.setPreferredSize(new Dimension(300, Integer.MAX_VALUE));

        CustomPanel profilePictureWrapper = new CustomPanel(new GridBagLayout());
        profilePictureWrapper.setPreferredSize(new Dimension(300, 300));
        avatarPanel = new AvatarPanel();
        profilePictureWrapper.add(avatarPanel);

        CustomPanel karmaWrapper = new CustomPanel(new BorderLayout());
        karmaWrapper.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        karmaWrapper.setRadius(20);
        karmaWrapper.setBackground(Color.WHITE);

        CustomPanel headerContainer = new CustomPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        
        CustomLabel karmaLabel = new CustomLabel("KARMA POINTS", 34f, FontStyle.BOLD);
        karmaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        int score = (user.karma_score != 0) ? user.karma_score : 67;
        CustomLabel karmaTitleLabel = new CustomLabel(getKarmaTitle(score).toUpperCase(), 16f, FontStyle.REGULAR, Color.GRAY);
        karmaTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerContainer.add(karmaLabel);
        headerContainer.add(Box.createVerticalStrut(5));
        headerContainer.add(karmaTitleLabel);

        CustomLabel karmaPointsLabel = new CustomLabel(String.valueOf(score), 80f, FontStyle.BOLD, Brand.SECONDARY_COLOR);
        karmaPointsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        CustomLabel karmaDescLabel = new CustomLabel("Your Community Standing", 18f, FontStyle.REGULAR, Color.GRAY);
        karmaDescLabel.setHorizontalAlignment(SwingConstants.CENTER);

        karmaWrapper.add(headerContainer, BorderLayout.NORTH);
        karmaWrapper.add(karmaPointsLabel, BorderLayout.CENTER);
        karmaWrapper.add(karmaDescLabel, BorderLayout.SOUTH);

        wrapper.add(profilePictureWrapper, BorderLayout.NORTH);
        wrapper.add(karmaWrapper, BorderLayout.CENTER);
        return wrapper;
    }

    private class AvatarPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Icon editIcon;

        public AvatarPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(250, 250));
            editIcon = IconLoader.loadAndScaleColorizedIcon("/resources/icons/edit.png", 100, 100, Color.WHITE);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (isEditing) {
                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(Profile.this);
                        EditProfilePicture dialog = new EditProfilePicture(parentFrame, user.profile_image);
                        String updatedPath = dialog.getSavedImagePath();
                        if (updatedPath != null) {
                            user.profile_image = updatedPath;
                            AvatarPanel.this.repaint();
                        }
                    }
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setCursor(new Cursor(isEditing ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    if (isEditing) repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Icon mainAvatar = createCircularIcon(user.profile_image, 250, 250);
            if (mainAvatar != null) mainAvatar.paintIcon(this, g2, 0, 0);

            if (isEditing) {
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillOval(0, 0, 250, 250);
                if (editIcon != null) {
                    editIcon.paintIcon(this, g2, (250 - editIcon.getIconWidth()) / 2, (250 - editIcon.getIconHeight()) / 2);
                }
            }
            g2.dispose();
        }
    }

    private CustomTabbedPane initCenter() {
        CustomTabbedPane tabbedPane = new CustomTabbedPane();
        tabbedPane.setRadius(20);
        tabbedPane.addTab("Profile", "/resources/icons/profile.png", initProfileTab());
        tabbedPane.addTab("Transaction History", "/resources/icons/logs.png", new TransactionHistory(user));
        return tabbedPane;
    }

    private CustomPanel initProfileTab() {
        CustomPanel wrapper = new CustomPanel(new BorderLayout(0, 30));
        wrapper.setPadding(20);

        CustomPanel topWrapper = new CustomPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.X_AXIS));
        topWrapper.add(new CustomLabel("Personal Information", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
        
        editProfileButton.setDefaultColor(new Color(0, 0, 0, 0));
        editProfileButton.setHoverColor(new Color(0, 0, 0, 0));
        topWrapper.add(Box.createHorizontalStrut(10));
        topWrapper.add(editProfileButton);

        editProfileButton.addToggleListener(toggled -> {
            if (!toggled && isEditing) {
                // Check if anything at all changed (including picture/password)
                if (!hasUnsavedChanges()) {
                    exitEditingMode(true);
                    return;
                }
                
                Object[] options = {"Save Changes", "Discard"};
                int choice = JOptionPane.showOptionDialog(this, "Do you want to save the changes made to your profile?", "Confirm Changes",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (choice == JOptionPane.YES_OPTION) {
                    if (validateFields()) {
                        saveFields();
                        exitEditingMode(false); // DO NOT restore old values! (Saves the new picture)
                    } else {
                        SwingUtilities.invokeLater(() -> editProfileButton.setToggled(true));
                    }
                } else if (choice == JOptionPane.NO_OPTION) {
                    exitEditingMode(true); // Restores old picture if discarded
                } else {
                    SwingUtilities.invokeLater(() -> editProfileButton.setToggled(true));
                }
            } else if (toggled && !isEditing) {
                isEditing = true;
                avatarPanel.repaint();
                buildProfileFields(); 
                SwingUtilities.invokeLater(this::captureOriginalValues); 
            }
        });

        profileContentPanel = new CustomPanel(new BorderLayout());
        buildProfileFields(); 

        wrapper.add(topWrapper, BorderLayout.NORTH);
        wrapper.add(profileContentPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // Pass 'true' to revert unsaved picture/password changes, 'false' if we just saved to the database.
    private void exitEditingMode(boolean restoreOldValues) {
        isEditing = false;
        
        if (restoreOldValues) {
            if (originalValues.containsKey("profileImage")) user.profile_image = originalValues.get("profileImage");
            if (originalValues.containsKey("password")) storedPassword = originalValues.get("password");
        }
        
        avatarPanel.repaint();
        buildProfileFields();
        originalValues.clear();
    }

    private boolean validateFields() {
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name cannot be empty.");
            return false;
        }
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name cannot be empty.");
            return false;
        }
        if (courseField.getText().trim().isEmpty()) {
            showError("Course/Program cannot be empty.");
            return false;
        }
        
        String contact = normalizeDigits(contactNumberField.getText());
        if (!contact.isEmpty() && (contact.length() != 11 || !contact.startsWith("09"))) {
            showError("Contact number must be an 11-digit number starting with 09.");
            return false;
        }
        
        String gcash = normalizeDigits(gcashField.getText());
        if (!gcash.isEmpty() && (gcash.length() != 11 || !gcash.startsWith("09"))) {
            showError("GCash number must be an 11-digit number starting with 09.");
            return false;
        }

        String maya = normalizeDigits(mayaField.getText());
        if (!maya.isEmpty() && (maya.length() != 11 || !maya.startsWith("09"))) {
            showError("Maya number must be an 11-digit number starting with 09.");
            return false;
        }

        String bdo = normalizeDigits(bdoField.getText());
        if (!bdo.isEmpty() && bdo.length() != 16) {
            showError("BDO number must be a 16-digit number.");
            return false;
        }

        String bpi = normalizeDigits(bpiField.getText());
        if (!bpi.isEmpty() && bpi.length() != 16) {
            showError("BPI number must be a 16-digit number.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void saveFields() {
        user.last_name = lastNameField.getText();
        user.first_name = firstNameField.getText();
        
        String contactDigits = normalizeDigits(contactNumberField.getText());
        user.contact_num = contactDigits.isEmpty() ? null : contactDigits;
        
        user.college = (collegeCombo != null) ? (String) collegeCombo.getSelectedItem() : collegeField.getText();
        user.course_program = courseField.getText();
        user.year_level = (yearCombo != null) ? (String) yearCombo.getSelectedItem() : yearLevelField.getText();

        gcashNum = normalizeDigits(gcashField.getText());
        mayaNum = normalizeDigits(mayaField.getText());
        bdoNum = normalizeDigits(bdoField.getText());
        bpiNum = normalizeDigits(bpiField.getText());
        
        saveProfileToDatabase();
    }

    private boolean hasUnsavedChanges() {
        if (!isEditing || originalValues.isEmpty()) return false;

        // FIXED: Now checks if the profile picture or password was modified!
        if (!safeEquals(originalValues.get("profileImage"), user.profile_image)) return true;
        if (!safeEquals(originalValues.get("password"), storedPassword)) return true;
        
        if (!safeEquals(originalValues.get("lastName"), lastNameField.getText())) return true;
        if (!safeEquals(originalValues.get("firstName"), firstNameField.getText())) return true;
        if (!safeEquals(originalValues.get("courseProgram"), courseField.getText())) return true;
        if (!normalizeDigits(originalValues.get("contactNumber")).equals(normalizeDigits(contactNumberField.getText()))) return true;
        
        String curCollege = (collegeCombo != null && collegeCombo.getSelectedItem() != null) ? collegeCombo.getSelectedItem().toString() : collegeField.getText();
        if (!safeEquals(originalValues.get("college"), curCollege)) return true;

        String curYear = (yearCombo != null && yearCombo.getSelectedItem() != null) ? yearCombo.getSelectedItem().toString() : yearLevelField.getText();
        if (!safeEquals(originalValues.get("yearLevel"), curYear)) return true;

        if (!normalizeDigits(originalValues.get("gcashNum")).equals(normalizeDigits(gcashField.getText()))) return true;
        if (!normalizeDigits(originalValues.get("mayaNum")).equals(normalizeDigits(mayaField.getText()))) return true;
        if (!safeEquals(originalValues.get("bdoNum"), bdoField.getText())) return true;
        if (!safeEquals(originalValues.get("bpiNum"), bpiField.getText())) return true;

        return false;
    }

    private void captureOriginalValues() {
        originalValues.clear();
        
        // FIXED: Now safely records the starting state of the picture and password
        originalValues.put("profileImage", safeGet(user.profile_image));
        originalValues.put("password", safeGet(storedPassword));
        
        originalValues.put("lastName", safeGet(lastNameField.getText()));
        originalValues.put("firstName", safeGet(firstNameField.getText()));
        originalValues.put("contactNumber", safeGet(contactNumberField.getText()));
        originalValues.put("college", collegeCombo != null && collegeCombo.getSelectedItem() != null ? collegeCombo.getSelectedItem().toString() : safeGet(collegeField.getText()));
        originalValues.put("courseProgram", safeGet(courseField.getText()));
        originalValues.put("yearLevel", yearCombo != null && yearCombo.getSelectedItem() != null ? yearCombo.getSelectedItem().toString() : safeGet(yearLevelField.getText()));
        originalValues.put("gcashNum", safeGet(gcashField.getText()));
        originalValues.put("mayaNum", safeGet(mayaField.getText()));
        originalValues.put("bdoNum", safeGet(bdoField.getText()));
        originalValues.put("bpiNum", safeGet(bpiField.getText()));
    }

    private String safeGet(String value) {
        return value == null ? "" : value;
    }

    private boolean safeEquals(String a, String b) {
        return safeGet(a).equals(safeGet(b));
    }

    private String normalizeDigits(String s) {
        return safeGet(s).replaceAll("\\D", "");
    }

    private String censorNumber(String num) {
        if (num == null) return "";
        int len = num.length();
        if (len <= 2) return "*".repeat(len);
        if (len <= 5) return num.charAt(0) + "*".repeat(len - 2) + num.charAt(len - 1);
        
        int keepStart = Math.min(3, len - 2);
        return num.substring(0, keepStart) + "*".repeat(len - keepStart - 2) + num.substring(len - 2);
    }

    private String censorCardNetwork(String num) {
        if (num == null) return "";
        String digits = num.replaceAll("\\D", "");
        if (digits.length() <= 4) return digits;
        return "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
    }

    private void buildProfileFields() {
        profileContentPanel.removeAll();

        CustomPanel infoWrapper = new CustomPanel();
        CustomPanel leftCol = new CustomPanel();
        CustomPanel rightCol = new CustomPanel();

        infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.X_AXIS));
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        leftCol.setAlignmentY(Component.TOP_ALIGNMENT);
        rightCol.setAlignmentY(Component.TOP_ALIGNMENT);

        leftCol.add(infoWrapper("Student ID", user.student_id, studentIdField, false));
        leftCol.add(Box.createVerticalStrut(15));
        leftCol.add(infoWrapper("Last Name", user.last_name, lastNameField, true));
        leftCol.add(Box.createVerticalStrut(15));
        leftCol.add(infoWrapper("First Name", user.first_name, firstNameField, true));
        leftCol.add(Box.createVerticalStrut(15));
        leftCol.add(infoWrapper("Contact Number", user.contact_num, contactNumberField, true));
        leftCol.add(Box.createVerticalStrut(15));
        leftCol.add(infoWrapper("UMak Email Address", user.umak_email_address, umakEmailAddressField, false));
        leftCol.add(Box.createVerticalStrut(15));
        leftCol.add(infoWrapper("Member Since", "January 01, 2001", null, false));
        leftCol.add(Box.createVerticalStrut(15));
        leftCol.add(infoWrapper("Password", storedPassword, null, false));
        leftCol.add(Box.createVerticalStrut(15));

        collegeCombo = new CustomComboBox<>(COLLEGES);
        rightCol.add(comboInfoWrapper("College/Institute", user.college, collegeCombo, true));
        rightCol.add(Box.createVerticalStrut(15));
        rightCol.add(infoWrapper("Course/Program", user.course_program, courseField, true));
        rightCol.add(Box.createVerticalStrut(15));

        yearCombo = new CustomComboBox<>(YEARS);
        rightCol.add(comboInfoWrapper("Year Level", user.year_level, yearCombo, true));
        rightCol.add(Box.createVerticalStrut(30));
        
        rightCol.add(new CustomLabel("Payment Information", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD));
        rightCol.add(Box.createVerticalStrut(5));

        CustomPanel paymentInfoPanel = new CustomPanel(new GridLayout(0, 2));
        CustomPanel paymentLeft = new CustomPanel();
        CustomPanel paymentRight = new CustomPanel();
        paymentLeft.setLayout(new BoxLayout(paymentLeft, BoxLayout.Y_AXIS));
        paymentRight.setLayout(new BoxLayout(paymentRight, BoxLayout.Y_AXIS));

        paymentLeft.add(createLeftAlignedLabel("Online Payment"));
        paymentLeft.add(Box.createVerticalStrut(10));
        paymentLeft.add(paymentInfoWrapper("/resources/icons/gcash.png", "GCash", gcashNum, gcashField));
        paymentLeft.add(Box.createVerticalStrut(10));
        paymentLeft.add(paymentInfoWrapper("/resources/icons/maya.png", "Maya", mayaNum, mayaField));

        paymentRight.add(createLeftAlignedLabel("Payment Network"));
        paymentRight.add(Box.createVerticalStrut(10));
        paymentRight.add(paymentInfoWrapper("/resources/icons/bdo.jpg", "BDO", bdoNum, bdoField));
        paymentRight.add(Box.createVerticalStrut(10));
        paymentRight.add(paymentInfoWrapper("/resources/icons/bpi.jpg", "BPI", bpiNum, bpiField));

        paymentInfoPanel.add(paymentLeft);
        paymentInfoPanel.add(paymentRight);
        paymentInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCol.add(paymentInfoPanel);

        infoWrapper.add(leftCol);
        infoWrapper.add(rightCol);

        CustomPanel topAnchor = new CustomPanel(new BorderLayout());
        topAnchor.add(infoWrapper, BorderLayout.NORTH);

        profileContentPanel.add(topAnchor, BorderLayout.CENTER);
        profileContentPanel.revalidate();
        profileContentPanel.repaint();
    }

    private CustomLabel createLeftAlignedLabel(String text) {
        CustomLabel label = new CustomLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private CustomPanel createBaseFieldWrapper(String title, Icon icon) {
        CustomPanel wrapper = new CustomPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        CustomLabel infoLabel = new CustomLabel(title, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
        if (icon != null) infoLabel.setIcon(icon);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(infoLabel);
        wrapper.add(Box.createVerticalStrut(5));
        return wrapper;
    }

    private CustomPanel infoWrapper(String info, String value, CustomTextField field, boolean isEditable) {
        CustomPanel wrapper = createBaseFieldWrapper(info, null);

        if ("Password".equals(info)) {
            wrapper.add(createDisabledField(storedPassword == null ? "*" : "*".repeat(Math.max(1, storedPassword.length())), 300));
            if (isEditing) {
                CustomButton changePwd = new CustomButton("Change Password", 10);
                changePwd.setDefaultColor(Brand.RED);
                changePwd.setHoverColor(Brand.RED.darker());
                changePwd.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                changePwd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                changePwd.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        new ChangePasswordDialog(SwingUtilities.getWindowAncestor(Profile.this), storedPassword, newPassword -> {
                            storedPassword = newPassword;
                            buildProfileFields();
                        });
                    }
                });
                CustomPanel changeWrap = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                changeWrap.setOpaque(false);
                changeWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                changeWrap.add(changePwd);
                wrapper.add(Box.createVerticalStrut(10));
                wrapper.add(changeWrap);
            }
            return wrapper;
        }

        if (isEditing && isEditable && field != null) {
            field.setText(safeGet(value));
            field.setCustomSize(300, 35);
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(field);
        } else {
            wrapper.add(createDisabledField(safeGet(value), 300));
        }
        return wrapper;
    }

    private CustomPanel comboInfoWrapper(String info, String value, JComboBox<String> combo, boolean isEditable) {
        CustomPanel wrapper = createBaseFieldWrapper(info, null);

        if (isEditing && isEditable && combo != null) {
            combo.setSelectedItem(value);
            combo.setPreferredSize(new Dimension(300, 35));
            combo.setMaximumSize(new Dimension(300, 35));
            combo.setAlignmentX(Component.LEFT_ALIGNMENT);
            combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
            combo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (c instanceof JComponent) ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
                    return c;
                }
            });
            wrapper.add(combo);
        } else {
            wrapper.add(createDisabledField(safeGet(value), 300));
        }
        return wrapper;
    }

    private CustomPanel paymentInfoWrapper(String iconPath, String info, String value, CustomTextField field) {
        CustomPanel wrapper = createBaseFieldWrapper(info, IconLoader.loadAndScaleIcon(iconPath, 20, 20));

        if (isEditing && field != null) {
            field.setText(safeGet(value));
            field.setCustomSize(200, 35);
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(field);
        } else {
            String display = ("BDO".equalsIgnoreCase(info) || "BPI".equalsIgnoreCase(info)) 
                             ? censorCardNetwork(value) 
                             : censorNumber(value);
            wrapper.add(createDisabledField(display, 200));
        }
        wrapper.add(Box.createVerticalStrut(6));
        return wrapper;
    }

    private CustomTextField createDisabledField(String value, int width) {
        CustomTextField displayField = new CustomTextField(value);
        displayField.setCustomSize(width, 35);
        displayField.setEditable(false);
        displayField.setEnabled(false);
        displayField.setFocusable(false);
        displayField.setAlignmentX(Component.LEFT_ALIGNMENT);
        displayField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return displayField;
    }

    private void loadPaymentMethods() {
        String sql = "SELECT payment_type, payment_number FROM payment_methods WHERE user_id = ? AND is_active = TRUE";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.user_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("payment_type");
                    String encryptedNumber = rs.getString("payment_number");
                    
                    String decryptedNumber = Encryption.decrypt(encryptedNumber);
                    if (decryptedNumber == null) decryptedNumber = encryptedNumber; 
                    
                    if ("gcash".equalsIgnoreCase(type)) gcashNum = decryptedNumber;
                    else if ("maya".equalsIgnoreCase(type)) mayaNum = decryptedNumber;
                    else if ("bdo".equalsIgnoreCase(type)) bdoNum = decryptedNumber;
                    else if ("bpi".equalsIgnoreCase(type)) bpiNum = decryptedNumber;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching payment methods: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProfileToDatabase() {
        String updateUsersSql = "UPDATE users SET first_name=?, last_name=?, contact_num=?, college=?, course_program=?, year_level=?, password=?, profile_image=? WHERE user_id=?";
        String upsertPaymentSql = "INSERT INTO payment_methods (user_id, payment_type, payment_number, is_active) VALUES (?, ?, ?, TRUE) ON DUPLICATE KEY UPDATE payment_number=?, is_active=TRUE";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement psUser = conn.prepareStatement(updateUsersSql)) {
                psUser.setString(1, user.first_name);
                psUser.setString(2, user.last_name);
                
                if (user.contact_num != null && !user.contact_num.trim().isEmpty()) {
                    psUser.setString(3, user.contact_num);
                } else {
                    psUser.setNull(3, java.sql.Types.VARCHAR);
                }
                
                psUser.setString(4, user.college);
                psUser.setString(5, user.course_program);
                psUser.setString(6, user.year_level);
                
                String finalEncryptedPass = Encryption.encryptPassword(storedPassword);
                psUser.setString(7, finalEncryptedPass);
                
                String safeImage = (user.profile_image != null && !user.profile_image.trim().isEmpty()) ? user.profile_image : DEFAULT_PROFILE_PIC;
                psUser.setString(8, safeImage);
                psUser.setInt(9, user.user_id);
                psUser.executeUpdate();
                
                user.password = finalEncryptedPass;
                user.profile_image = safeImage;
            }
            
            upsertPaymentHelper(conn, upsertPaymentSql, "gcash", gcashNum);
            upsertPaymentHelper(conn, upsertPaymentSql, "maya", mayaNum);
            upsertPaymentHelper(conn, upsertPaymentSql, "BDO", bdoNum);
            upsertPaymentHelper(conn, upsertPaymentSql, "BPI", bpiNum);
            
            conn.commit();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to persist profile changes: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void upsertPaymentHelper(Connection conn, String sql, String type, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) return;
        try (PreparedStatement psPay = conn.prepareStatement(sql)) {
            String encryptedValue = Encryption.encrypt(value.trim());
            
            psPay.setInt(1, user.user_id);
            psPay.setString(2, type);
            psPay.setString(3, encryptedValue);
            psPay.setString(4, encryptedValue);
            psPay.executeUpdate();
        }
    }
}