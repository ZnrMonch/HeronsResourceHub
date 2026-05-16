package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;
import profile.EditProfilePicture;

public class Profile extends CustomPanel {
	private static final long serialVersionUID = 1L;
	
	private CustomToggleButton editProfileButton = new CustomToggleButton(
		IconLoader.loadAndScaleIcon("/resources/icons/edit.png", 25, 25),
		IconLoader.loadAndScaleIcon("/resources/icons/close.png", 25, 25)
	);
	
	private boolean isEditing = false;
	private CustomPanel profileContentPanel;
	
	// Track the current profile picture path so we can change it dynamically
	private String currentProfilePicturePath = "/resources/defaultpictures/renzjan.jpg";
	
	// Custom interactive panel for handling the avatar painting and hover effects
	private AvatarPanel avatarPanel;
	
	private String studentId = "K12360080";
	private String lastName = "Moncinilla";
	private String firstName = "Renzjan";
	private String contactNumber = "095118391870";
	private String email = "renzjan.moncinilla@umak.edu.ph";
	private String memberSince = "January 01, 2001";
	private String password = "*";
	private String storedPassword = "";
	
	private String college = "CCIS";
	private String course = "BS in Information Technology";
	private String yearLevel = "1st Year";

	private String gcash = "0913456789";
	private String maya = "0913456789";
	private String mastercard= "1234567891234567";
	private String visa = "1234567891234567";

	private String gcashName = "Renzjan Moncinilla";
	private String mayaName = "Renzjan Moncinilla";
	private String mastercardName = "Renzjan Moncinilla";
	private String visaName = "Renzjan Moncinilla";
	
	private CustomTextField studentIdField = new CustomTextField("");
	private CustomTextField lastNameField = new CustomTextField("");
	private CustomTextField firstNameField = new CustomTextField("");
	private CustomTextField contactNumberField = new CustomTextField("");
	
	private CustomTextField collegeField = new CustomTextField("");
	private CustomTextField courseField = new CustomTextField("");
	private CustomTextField yearLevelField = new CustomTextField("");

	private CustomComboBox<String> collegeCombo = null;
	private CustomComboBox<String> yearCombo = null;
	
	private CustomTextField gcashField = new CustomTextField("");
	private CustomTextField mayaField = new CustomTextField("");
	private CustomTextField mastercardField = new CustomTextField("");
	private CustomTextField visaField = new CustomTextField("");

	private CustomTextField gcashNameField = new CustomTextField("");
	private CustomTextField mayaNameField = new CustomTextField("");
	private CustomTextField mastercardNameField = new CustomTextField("");
	private CustomTextField visaNameField = new CustomTextField("");
	
	public Profile() {
		setLayout(new BorderLayout(20, 20));
		add(initWest(), BorderLayout.WEST);
		add(initCenter(), BorderLayout.CENTER);
	}
	
	private CustomPanel initWest() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout(0, 20));
		wrapper.setPreferredSize(new Dimension(300, Integer.MAX_VALUE));
		
		CustomPanel profilePictureWrapper = new CustomPanel(new GridBagLayout());
		profilePictureWrapper.setPreferredSize(new Dimension(300, 300));
		
		// Use our custom panel instead of a standard JLabel
		avatarPanel = new AvatarPanel();
		profilePictureWrapper.add(avatarPanel);
		
		CustomPanel karmaWrapper = new CustomPanel(new BorderLayout());
		karmaWrapper.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20)); 
		karmaWrapper.setRadius(20);
		karmaWrapper.setBackground(Color.WHITE);
		
		CustomLabel karmaLabel = new CustomLabel("KARMA POINTS", 34f, FontStyle.BOLD);
		karmaLabel.setHorizontalAlignment(SwingConstants.CENTER);
		CustomLabel karmaPointsLabel = new CustomLabel("67", 120f, FontStyle.BOLD);
		karmaPointsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		CustomLabel karmaDescLabel = new CustomLabel("Your Community Standing", 18f, FontStyle.REGULAR);
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

	// --- CUSTOM AVATAR COMPONENT WITH HOVER PROFILE/EDIT ICON OVERLAY ---
	private class AvatarPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private boolean isHovered = false;
		private Icon editIcon;

		public AvatarPanel() {
			setOpaque(false);
			setPreferredSize(new Dimension(250, 250));
			
			// Load the edit/profile indicator icon overlay 
			// (Uses your edit icon scaled down cleanly to fit over the avatar)
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
							currentProfilePicturePath = updatedPath;
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
					} else {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
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

			// 1. Draw Circular Profile Picture
			Icon mainAvatar = IconLoader.loadAndScaleCircularIcon(currentProfilePicturePath, 250, 250);
			if (mainAvatar != null) {
				mainAvatar.paintIcon(this, g2, 0, 0);
			}

			// 2. Overlay an Indicator Icon ONLY if currently Hovered and in Editing Mode
			if (isEditing && isHovered) {
				// Create a dark transparent circular mask over the picture
				g2.setColor(new Color(0, 0, 0, 110));
				g2.fillOval(0, 0, 250, 250);
				
				// Center the edit/profile indicator icon exactly in the middle of the circle
				if (editIcon != null) {
					int iconX = (250 - editIcon.getIconWidth()) / 2;
					int iconY = (250 - editIcon.getIconHeight()) / 2;
					editIcon.paintIcon(this, g2, iconX, iconY);
				}
			}
			g2.dispose();
		}
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
	
	private CustomTabbedPane initCenter() {
		CustomTabbedPane tabbedPane = new CustomTabbedPane();
		tabbedPane.setRadius(20);
		
		tabbedPane.addTab("Profile", "/resources/icons/profile.png", initProfileTab());
		tabbedPane.addTab("Transaction History","/resources/icons/logs.png", new CustomPanel());
		
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
	
	private void saveFields() {
		lastName = lastNameField.getText();
		firstName = firstNameField.getText();
		contactNumber = contactNumberField.getText();
		if (collegeCombo != null) college = (String) collegeCombo.getSelectedItem();
		else college = collegeField.getText();
		course = courseField.getText();
		if (yearCombo != null) yearLevel = (String) yearCombo.getSelectedItem();
		else yearLevel = yearLevelField.getText();
		gcash = gcashField.getText();
		maya = mayaField.getText();
		mastercard = mastercardField.getText();
		visa = visaField.getText();

		gcash = censorNumber(gcashField.getText());
		maya = censorNumber(mayaField.getText());
		mastercard = censorNumber(mastercardField.getText());
		visa = censorNumber(visaField.getText());
	}

	private boolean hasUnsavedChanges() {
		try {
			if (!safeEquals(lastNameField.getText(), lastName)) return true;
			if (!safeEquals(firstNameField.getText(), firstName)) return true;
			if (!safeEquals(contactNumberField.getText(), contactNumber)) return true;
			if (collegeCombo != null) {
				String sel = (String) collegeCombo.getSelectedItem();
				if (!safeEquals(sel, college)) return true;
			} else {
				if (!safeEquals(collegeField.getText(), college)) return true;
			}
			if (!safeEquals(courseField.getText(), course)) return true;
			if (yearCombo != null) {
				String ysel = (String) yearCombo.getSelectedItem();
				if (!safeEquals(ysel, yearLevel)) return true;
			} else {
				if (!safeEquals(yearLevelField.getText(), yearLevel)) return true;
			}
			if (!safeEquals(gcashField.getText(), gcash)) return true;
			if (!safeEquals(mayaField.getText(), maya)) return true;
			if (!safeEquals(mastercardField.getText(), mastercard)) return true;
			if (!safeEquals(visaField.getText(), visa)) return true;
		} catch (Exception e) {
			return true;
		}
		return false;
	}

	private boolean safeEquals(String a, String b) {
		if (a == null) a = "";
		if (b == null) b = "";
		return a.equals(b);
	}

	private String maskName(String name) {
		if (name == null) return "";
		int len = name.length();
		int first = -1, last = -1;
		for (int i = 0; i < len; i++) if (!Character.isWhitespace(name.charAt(i))) { first = i; break; }
		for (int i = len - 1; i >= 0; i--) if (!Character.isWhitespace(name.charAt(i))) { last = i; break; }
		if (first == -1 || last == -1 || first == last) return name;
		StringBuilder sb = new StringBuilder(name);
		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			if (Character.isWhitespace(c)) continue;
			if (i == first || i == last) continue;
			sb.setCharAt(i, '*');
		}
		return sb.toString();
	}

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
		
		infoWrapper1.add(infoWrapper("Student ID", studentId, studentIdField, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Last Name", lastName, lastNameField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("First Name", firstName, firstNameField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Contact Number", contactNumber, contactNumberField, true));
		infoWrapper1.add(Box.createVerticalStrut(15));
		
		infoWrapper1.add(infoWrapper("UMak Email Address", email, null, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Member Since", memberSince, null, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		infoWrapper1.add(infoWrapper("Password", password, null, false));
		infoWrapper1.add(Box.createVerticalStrut(15));
		
		String[] colleges = new String[]{"CBFS","CCIS","CCAPS","CCSE","CET","CGPP","CHK","CITE","CITE-HSU","CTHM","IAD","IOA","IOP","ION","IIHS","IOPsy","ISW","IDEM"};
		collegeCombo = new CustomComboBox<>(colleges);
		infoWrapper2.add(comboInfoWrapper("College/Institute", college, collegeCombo, true));
		infoWrapper2.add(Box.createVerticalStrut(15));
		infoWrapper2.add(infoWrapper("Course/Program", course, courseField, true));
		infoWrapper2.add(Box.createVerticalStrut(15));

		String[] years = new String[]{"1st Year","2nd Year","3rd Year","4th Year","5th Year","Alumni"};
		yearCombo = new CustomComboBox<>(years);
		infoWrapper2.add(comboInfoWrapper("Year Level", yearLevel, yearCombo, true));
		infoWrapper2.add(Box.createVerticalStrut(30));
		infoWrapper2.add(new CustomLabel("Payment Information", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD));
		infoWrapper2.add(Box.createVerticalStrut(5));
		
		CustomPanel paymentInfoPanel = new CustomPanel(new GridLayout(0, 2));
		CustomPanel paymentInfoPanel1 = new CustomPanel();
		CustomPanel paymentInfoPanel2 = new CustomPanel();
		
		paymentInfoPanel1.setLayout(new BoxLayout(paymentInfoPanel1, BoxLayout.Y_AXIS));
		paymentInfoPanel2.setLayout(new BoxLayout(paymentInfoPanel2, BoxLayout.Y_AXIS));
		
		CustomLabel onlinePaymentTitle = new CustomLabel("Online Payment");
		onlinePaymentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		paymentInfoPanel1.add(onlinePaymentTitle);
		paymentInfoPanel1.add(Box.createVerticalStrut(10));
		paymentInfoPanel1.add(paymentInfoWrapper("/resources/icons/gcash.png", "GCash", gcash, gcashField, true, gcashName, gcashNameField));
		paymentInfoPanel1.add(Box.createVerticalStrut(10));
		paymentInfoPanel1.add(paymentInfoWrapper("/resources/icons/maya.png", "Maya", maya, mayaField, true, mayaName, mayaNameField));
		
		CustomLabel paymentNetworkTitle = new CustomLabel("Payment Network");
		paymentNetworkTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		paymentInfoPanel2.add(paymentNetworkTitle);
		paymentInfoPanel2.add(Box.createVerticalStrut(10));
		paymentInfoPanel2.add(paymentInfoWrapper("/resources/icons/mastercard.png", "Mastercard", mastercard, mastercardField, true, mastercardName, mastercardNameField));
		paymentInfoPanel2.add(Box.createVerticalStrut(10));
		paymentInfoPanel2.add(paymentInfoWrapper("/resources/icons/visa.png", "Visa", visa, visaField, true, visaName, visaNameField));
		
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
	
	private CustomPanel infoWrapper(String info, String value, CustomTextField field, boolean isEditable) {
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
							password = maskPasswordDisplay(storedPassword);
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
			field.setText(value);
			field.setCustomSize(300, 35);
			field.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(field);
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
	
	private CustomPanel paymentInfoWrapper(String path, String info, String value, CustomTextField field, boolean isEditable, String name, CustomTextField nameField) {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		CustomLabel infoLabel = new CustomLabel(info, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		infoLabel.setIcon(IconLoader.loadAndScaleIcon(path, 20, 20));
		infoLabel.setForeground(Color.GRAY);
		infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		wrapper.add(infoLabel);
		wrapper.add(Box.createVerticalStrut(5));
		if (isEditing && isEditable && field != null) {
			field.setText(value);
			field.setCustomSize(200, 35);
			field.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(field);
			wrapper.add(Box.createVerticalStrut(5));
			String displayName = name != null ? name : "";
			CustomLabel nameLabel = new CustomLabel(displayName, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
			nameLabel.setForeground(Color.GRAY);
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(nameLabel);
		} else {
			String display = censorNumber(value);
			CustomTextField displayNum = new CustomTextField(display);
			displayNum.setCustomSize(200, 35);
			displayNum.setEditable(false);
			displayNum.setEnabled(false);
			displayNum.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(displayNum);
			wrapper.add(Box.createVerticalStrut(5));
			CustomLabel nameLabel = new CustomLabel(maskName(name), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
			nameLabel.setForeground(Color.GRAY);
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(nameLabel);
		}
		
		return wrapper;
	}
}