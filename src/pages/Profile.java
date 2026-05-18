package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;
import profile.EditProfilePicture;
import database.UserRecord;

public class Profile extends CustomPanel {
    private static final long serialVersionUID = 1L;

    private final UserRecord user;

    private boolean isEditing = false;
    private CustomPanel profileContentPanel;
    private AvatarPanel avatarPanel;
    private CustomToggleButton editProfileButton = new CustomToggleButton(
            IconLoader.loadAndScaleIcon("/resources/icons/edit.png", 25, 25),
            IconLoader.loadAndScaleIcon("/resources/icons/close.png", 25, 25)
    );

    // Editable Swing fields
    private CustomTextField lastNameField = new CustomTextField("");
    private CustomTextField firstNameField = new CustomTextField("");
    private CustomTextField contactNumberField = new CustomTextField("");
    private CustomTextField courseField = new CustomTextField("");
    private CustomTextField collegeField = new CustomTextField("");
    private CustomTextField yearLevelField = new CustomTextField("");
    private CustomComboBox<String> collegeCombo = null;
    private CustomComboBox<String> yearCombo = null;

    private CustomTextField gcashField = new CustomTextField("");
    private CustomTextField mayaField = new CustomTextField("");
    private CustomTextField mastercardField = new CustomTextField("");
    private CustomTextField visaField = new CustomTextField("");
    private String storedPassword = ""; // for mask/display/hiding

    // ---- Constructors ----

    /** Preferred constructor (requires user info). */
    public Profile(UserRecord user) {
        if (user == null) throw new IllegalArgumentException("UserRecord is required");
        this.user = user;
        if (user.password != null) storedPassword = user.password;
        setLayout(new BorderLayout(20, 20));
        add(initWest(), BorderLayout.WEST);
        add(initCenter(), BorderLayout.CENTER);
    }

    /** Default constructor: uses a dummy/default user. */
    public Profile() {
        this(createDefaultUserRecord());
    }

    /** Static helper: returns a "dummy" UserRecord. */
    private static UserRecord createDefaultUserRecord() {
        UserRecord user = new UserRecord();
        user.systemRole = "user";
        user.studentId = "DUMMY-STUDENT-ID";
        user.umakEmailAddress = "dummy@umak.edu.ph";
        user.password = "defaultpassword";
        user.college = "CCIS";
        user.yearLevel = "1st Year";
        user.courseProgram = "BS in Information Technology";
        user.firstName = "Default";
        user.lastName = "User";
        user.karmaScore = 0;
        user.profileImage = "/resources/defaultpictures/renzjan.jpg";
        user.contactNum = 639000000000L;
        user.gcashNum = 639000000001L;
        user.mayaNum = 639000000002L;
        user.mastercardNum = "1111222233334444";
        user.visaNum = "4444333322221111";
        return user;
    }

    private String getProfilePicturePath() {
        return (user.profileImage != null && !user.profileImage.isEmpty())
                ? user.profileImage : "/resources/defaultpictures/renzjan.jpg";
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

        CustomLabel karmaLabel = new CustomLabel("KARMA POINTS", 34f, FontStyle.BOLD);
        karmaLabel.setHorizontalAlignment(SwingConstants.CENTER);

        CustomLabel karmaPointsLabel = new CustomLabel(
                String.valueOf(user.karmaScore), 120f, FontStyle.BOLD);
        karmaPointsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        CustomLabel karmaDescLabel = new CustomLabel(
                "Your Community Standing", 18f, FontStyle.REGULAR);
        karmaDescLabel.setHorizontalAlignment(SwingConstants.CENTER);

        karmaPointsLabel.setForeground(Brand.SECONDARY_COLOR);
        karmaDescLabel.setForeground(Color.GRAY);

        karmaWrapper.add(karmaLabel, BorderLayout.NORTH);
        karmaWrapper.add(karmaPointsLabel, BorderLayout.CENTER);
        karmaWrapper.add(karmaDescLabel, BorderLayout.SOUTH);

        wrapper.add(profilePictureWrapper, BorderLayout.NORTH);
        wrapper.add(karmaWrapper, BorderLayout.CENTER);
        return wrapper;
    }

    // --- AvatarPanel as before, but reads path from user ---
    private class AvatarPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private boolean isHovered = false;
        private Icon editIcon;

        public AvatarPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(250, 250));
            editIcon = IconLoader.loadAndScaleIcon("/resources/icons/edit.png", 40, 40);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (isEditing) {
                        Window parentWindow = SwingUtilities.getWindowAncestor(Profile.this);
                        JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;
                        EditProfilePicture dialog = new EditProfilePicture(parentFrame);

                        String updatedPath = dialog.getSavedImagePath();
                        if (updatedPath != null) {
                            user.profileImage = updatedPath;
                            AvatarPanel.this.repaint();
                        }
                    }
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (isEditing) {
                        isHovered = true;
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        repaint();
                    } else setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
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

            Icon mainAvatar = IconLoader.loadAndScaleCircularIcon(getProfilePicturePath(), 250, 250);
            if (mainAvatar != null) mainAvatar.paintIcon(this, g2, 0, 0);

            if (isEditing && isHovered) {
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillOval(0, 0, 250, 250);
                if (editIcon != null) {
                    int iconX = (250 - editIcon.getIconWidth()) / 2;
                    int iconY = (250 - editIcon.getIconHeight()) / 2;
                    editIcon.paintIcon(this, g2, iconX, iconY);
                }
            }
            g2.dispose();
        }
    }

    private CustomTabbedPane initCenter() {
        CustomTabbedPane tabbedPane = new CustomTabbedPane();
        tabbedPane.setRadius(20);
        tabbedPane.addTab("Profile", "/resources/icons/profile.png", initProfileTab());
        tabbedPane.addTab("Transaction History", "/resources/icons/logs.png", new CustomPanel());
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
                if (!hasUnsavedChanges()) {
                    isEditing = false;
                    avatarPanel.repaint();
                    buildProfileFields();
                    return;
                }
                Object[] options = {"Save Changes", "Discard"};
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "Do you want to save the changes made to your profile?",
                        "Confirm Changes",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == JOptionPane.YES_OPTION) {
                    saveFields();
                    isEditing = false;
                    avatarPanel.repaint();
                    buildProfileFields();
                } else if (choice == JOptionPane.NO_OPTION) {
                    isEditing = false;
                    avatarPanel.repaint();
                    buildProfileFields();
                } else {
                    SwingUtilities.invokeLater(() -> editProfileButton.setToggled(true));
                }
            } else if (toggled && !isEditing) {
                isEditing = true;
                avatarPanel.repaint();
                buildProfileFields();
            }
        });

        profileContentPanel = new CustomPanel(new BorderLayout());
        buildProfileFields();
        wrapper.add(topWrapper, BorderLayout.NORTH);
        wrapper.add(profileContentPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private void buildProfileFields() {
        profileContentPanel.removeAll();

        CustomPanel infoWrapper = new CustomPanel();
        CustomPanel infoWrapper1 = new CustomPanel();
        CustomPanel infoWrapper2 = new CustomPanel();

        infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.X_AXIS));
        infoWrapper1.setLayout(new BoxLayout(infoWrapper1, BoxLayout.Y_AXIS));
        infoWrapper2.setLayout(new BoxLayout(infoWrapper2, BoxLayout.Y_AXIS));
        infoWrapper1.setAlignmentY(Component.TOP_ALIGNMENT);
        infoWrapper2.setAlignmentY(Component.TOP_ALIGNMENT);

        // Use user fields everywhere:
        infoWrapper1.add(infoDisplayWrapper("Student ID", user.studentId, null, false));
        infoWrapper1.add(Box.createVerticalStrut(15));
        infoWrapper1.add(infoDisplayWrapper("Last Name", user.lastName, lastNameField, true));
        infoWrapper1.add(Box.createVerticalStrut(15));
        infoWrapper1.add(infoDisplayWrapper("First Name", user.firstName, firstNameField, true));
        infoWrapper1.add(Box.createVerticalStrut(15));
        infoWrapper1.add(infoDisplayWrapper("Contact Number", numberToString(user.contactNum), contactNumberField, true));
        infoWrapper1.add(Box.createVerticalStrut(15));
        infoWrapper1.add(infoDisplayWrapper("UMak Email Address", user.umakEmailAddress, null, false));
        infoWrapper1.add(Box.createVerticalStrut(15));
        infoWrapper1.add(infoDisplayWrapper("Member Since", "January 01, 2001", null, false)); // Optional: add to UserRecord!
        infoWrapper1.add(Box.createVerticalStrut(15));
        infoWrapper1.add(infoDisplayWrapper("Password", storedPassword, null, false));
        infoWrapper1.add(Box.createVerticalStrut(15));

        String[] colleges = new String[]{"CBFS","CCIS","CCAPS","CCSE","CET","CGPP","CHK","CITE","CITE-HSU","CTHM","IAD","IOA","IOP","ION","IIHS","IOPsy","ISW","IDEM"};
        collegeCombo = new CustomComboBox<>(colleges);
        infoWrapper2.add(comboInfoWrapper("College/Institute", user.college, collegeCombo, true));
        infoWrapper2.add(Box.createVerticalStrut(15));
        infoWrapper2.add(infoDisplayWrapper("Course/Program", user.courseProgram, courseField, true));
        infoWrapper2.add(Box.createVerticalStrut(15));

        String[] years = new String[]{"1st Year","2nd Year","3rd Year","4th Year","5th Year","Alumni"};
        yearCombo = new CustomComboBox<>(years);
        infoWrapper2.add(comboInfoWrapper("Year Level", user.yearLevel, yearCombo, true));
        infoWrapper2.add(Box.createVerticalStrut(30));
        infoWrapper2.add(new CustomLabel("Payment Information", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD));
        infoWrapper2.add(Box.createVerticalStrut(5));

        CustomPanel paymentInfoPanel = new CustomPanel(new GridLayout(0, 2));
        CustomPanel paymentInfoPanel1 = new CustomPanel();
        CustomPanel paymentInfoPanel2 = new CustomPanel();

        paymentInfoPanel1.setLayout(new BoxLayout(paymentInfoPanel1, BoxLayout.Y_AXIS));
        paymentInfoPanel2.setLayout(new BoxLayout(paymentInfoPanel2, BoxLayout.Y_AXIS));

        paymentInfoPanel1.add(new CustomLabel("Online Payment"));
        paymentInfoPanel1.add(Box.createVerticalStrut(10));
        paymentInfoPanel1.add(paymentInfoWrapper("/resources/icons/gcash.png", "GCash", numberToString(user.gcashNum), gcashField));
        paymentInfoPanel1.add(Box.createVerticalStrut(10));
        paymentInfoPanel1.add(paymentInfoWrapper("/resources/icons/maya.png", "Maya", numberToString(user.mayaNum), mayaField));

        paymentInfoPanel2.add(new CustomLabel("Payment Network"));
        paymentInfoPanel2.add(Box.createVerticalStrut(10));
        paymentInfoPanel2.add(paymentInfoWrapper("/resources/icons/mastercard.png", "Mastercard", user.mastercardNum, mastercardField));
        paymentInfoPanel2.add(Box.createVerticalStrut(10));
        paymentInfoPanel2.add(paymentInfoWrapper("/resources/icons/visa.png", "Visa", user.visaNum, visaField));

        paymentInfoPanel.add(paymentInfoPanel1);
        paymentInfoPanel.add(paymentInfoPanel2);
        paymentInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoWrapper2.add(paymentInfoPanel);

        infoWrapper.add(infoWrapper1);
        infoWrapper.add(infoWrapper2);

        CustomPanel topAnchor = new CustomPanel(new BorderLayout());
        topAnchor.add(infoWrapper, BorderLayout.NORTH);

        profileContentPanel.add(topAnchor, BorderLayout.CENTER);

        profileContentPanel.revalidate();
        profileContentPanel.repaint();
    }

    private CustomPanel infoDisplayWrapper(String info, String value, CustomTextField field, boolean isEditable) {
        CustomPanel wrapper = new CustomPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        CustomLabel infoLabel = new CustomLabel(info, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(infoLabel);
        wrapper.add(Box.createVerticalStrut(5));

        if ("Password".equals(info)) {
            CustomTextField pwdDisplay = new CustomTextField(maskPasswordDisplay(storedPassword));
            pwdDisplay.setCustomSize(300, 35);
            pwdDisplay.setEditable(false);
            pwdDisplay.setEnabled(false);
            pwdDisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(pwdDisplay);

            if (isEditing) {
                CustomButton changePwd = new CustomButton("Change Password", 10);
                changePwd.setDefaultColor(Brand.RED);
                changePwd.setHoverColor(Brand.RED.darker());
                changePwd.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                changePwd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                changePwd.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        Window win = SwingUtilities.getWindowAncestor(Profile.this);
                        new ChangePasswordDialog(win, storedPassword, newPassword -> {
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
            field.setText(value != null ? value : "");
            field.setCustomSize(300, 35);
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(field);
        } else {
            CustomTextField displayField = new CustomTextField(value != null ? value : "");
            displayField.setCustomSize(300, 35);
            displayField.setEditable(false);
            displayField.setEnabled(false);
            displayField.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(displayField);
        }
        return wrapper;
    }

    private CustomPanel comboInfoWrapper(String info, String value, JComboBox<String> combo, boolean isEditable) {
        CustomPanel wrapper = new CustomPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        CustomLabel infoLabel = new CustomLabel(info, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(infoLabel);
        wrapper.add(Box.createVerticalStrut(5));

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
            CustomTextField displayField = new CustomTextField(value);
            displayField.setCustomSize(300, 35);
            displayField.setEditable(false);
            displayField.setEnabled(false);
            displayField.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(displayField);
        }
        return wrapper;
    }

    private CustomPanel paymentInfoWrapper(String path, String info, String value, CustomTextField field) {
        CustomPanel wrapper = new CustomPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        CustomLabel infoLabel = new CustomLabel(info, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
        infoLabel.setIcon(IconLoader.loadAndScaleIcon(path, 20, 20));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(infoLabel);
        wrapper.add(Box.createVerticalStrut(5));
        if (isEditing && field != null) {
            field.setText(value != null ? value : "");
            field.setCustomSize(200, 35);
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(field);
        } else {
            CustomTextField displayNum = new CustomTextField(censorNumber(value));
            displayNum.setCustomSize(200, 35);
            displayNum.setEditable(false);
            displayNum.setEnabled(false);
            displayNum.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(displayNum);
        }

        return wrapper;
    }

    private void saveFields() {
        user.lastName = lastNameField.getText();
        user.firstName = firstNameField.getText();
        // Parse to Long (or null if blank or invalid)
        String contactText = contactNumberField.getText();
        try { user.contactNum = (contactText != null && !contactText.trim().isEmpty()) ? Long.parseLong(contactText) : null; }
        catch (Exception e) { user.contactNum = null; }

        if (collegeCombo != null) user.college = (String) collegeCombo.getSelectedItem();
        else user.college = collegeField.getText();
        user.courseProgram = courseField.getText();
        if (yearCombo != null) user.yearLevel = (String) yearCombo.getSelectedItem();
        else user.yearLevel = yearLevelField.getText();

        try { user.gcashNum = tryParseLong(gcashField.getText()); } catch (Exception e) { user.gcashNum = null; }
        try { user.mayaNum = tryParseLong(mayaField.getText()); } catch (Exception e) { user.mayaNum = null; }
        user.mastercardNum = mastercardField.getText();
        user.visaNum = visaField.getText();

        user.password = storedPassword;
    }

    private boolean hasUnsavedChanges() {
        if (!safeEquals(lastNameField.getText(), user.lastName)) return true;
        if (!safeEquals(firstNameField.getText(), user.firstName)) return true;
        if (!safeEquals(contactNumberField.getText(), numberToString(user.contactNum))) return true;
        if (collegeCombo != null && !safeEquals((String)collegeCombo.getSelectedItem(), user.college)) return true;
        if (!safeEquals(courseField.getText(), user.courseProgram)) return true;
        if (yearCombo != null && !safeEquals((String)yearCombo.getSelectedItem(), user.yearLevel)) return true;
        if (!safeEquals(gcashField.getText(), numberToString(user.gcashNum))) return true;
        if (!safeEquals(mayaField.getText(), numberToString(user.mayaNum))) return true;
        if (!safeEquals(mastercardField.getText(), user.mastercardNum)) return true;
        if (!safeEquals(visaField.getText(), user.visaNum)) return true;
        return false;
    }

    private static boolean safeEquals(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        return a.equals(b);
    }
    private static String numberToString(Number n) { return n == null ? "" : String.valueOf(n); }
    private static Long tryParseLong(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }

    // Util methods for masking
    private String maskPasswordDisplay(String pwd) {
        if (pwd == null || pwd.isEmpty()) return "*";
        return "*".repeat(Math.max(1, pwd.length()));
    }
    private String censorNumber(String num) {
        if (num == null) return "";
        int len = num.length();
        if (len <= 2) return "*".repeat(len);
        if (len <= 5) {
            StringBuilder sb = new StringBuilder();
            sb.append(num.charAt(0));
            for (int i = 1; i < len - 1; i++) sb.append('*');
            sb.append(num.charAt(len - 1));
            return sb.toString();
        }
        int keepStart = Math.min(3, len - 2);
        int keepEnd = 2;
        StringBuilder sb = new StringBuilder();
        sb.append(num.substring(0, keepStart));
        for (int i = 0; i < len - keepStart - keepEnd; i++) sb.append('*');
        sb.append(num.substring(len - keepEnd));
        return sb.toString();
    }
}
