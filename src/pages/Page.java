package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import database.UserRecord;
import marketplace.Marketplace;
import utils.*;
import auth.Auth;
import admin.AdminPanel;

public class Page extends JFrame {
	
	// FIELDS
	
	// Set class version
	private static final long serialVersionUID = 1L;
	
	// Main window wrapper
	private CustomPanel contentPane = new CustomPanel(Brand.BACKGROUND_COLOR);
	// Center content holder
	private CustomPanel bodyPanel = new CustomPanel();
	// Save user data
	private UserRecord currentUser;
	
	// CONSTRUCTORS
	
	// Build main app window
	public Page(JPanel content, UserRecord user) {
		// Save user data
		this.currentUser = user;
		// Set window text
		setTitle("University of Makati | Herons' Resource Hub");
		// Set taskbar icon
		setIconImage(IconLoader.loadAndScaleIcon("/resources/images/hrh-icon.jpg", 50, 50).getImage());
		// Quit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Center window
		setLocationRelativeTo(null);
		// Maximize size
		setExtendedState(MAXIMIZED_BOTH);
		// Lock minimum scale
		setMinimumSize(new Dimension(1100, 900));
		// Set default scale
		setPreferredSize(new Dimension(1100, 1000));
		
		// Add inner gap
		contentPane.setPadding(20);
		// Apply main panel
		setContentPane(contentPane);
		// Set spacing layout
		bodyPanel.setLayout(new BorderLayout(20, 20));
		// Insert child view
		bodyPanel.add(content, BorderLayout.CENTER);
		
		// Setup UI elements
		init();
		// Show window
		setVisible(true);
	}
	
	// METHODS
	
	// Arrange top and center UI parts
	private void init() {
		// Set main layout gap
		contentPane.setLayout(new BorderLayout(20, 20));
		// Mount top bar
		contentPane.add(initHeader(), BorderLayout.NORTH);
		// Mount center box
		contentPane.add(bodyPanel, BorderLayout.CENTER);
		
		// Draw to screen
		repaint();
		revalidate();
	}
	
	// Build top navigation bar
	private CustomPanel initHeader() {
		// Make white box
		CustomPanel header = new CustomPanel(Color.WHITE);
		// Set fixed height
		header.setPreferredSize(new Dimension(0, 70));
		// Add gap
		header.setPadding(10);
		// Curve corners
		header.setRadius(40);
		// Use border layout
		header.setLayout(new BorderLayout());
		 
		// Make left container
		CustomPanel westWrapper = new CustomPanel();
		// Hide background
		westWrapper.setOpaque(false);
		// Stack items right
		westWrapper.setLayout(new BoxLayout(westWrapper, BoxLayout.X_AXIS));
		// Add left gap
		westWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		// Make logo box
		CustomPanel logoWrapper = new CustomPanel();
		// Stack right
		logoWrapper.setLayout(new BoxLayout(logoWrapper, BoxLayout.X_AXIS));
		// Show pointer
		logoWrapper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		// Handle logo click
		logoWrapper.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				// Go to marketplace
				switchPage(new Marketplace(currentUser));
			}
		});
		
		// Load images
		JLabel umakLogo = new JLabel(IconLoader.loadAndScaleIcon("/resources/images/umak-icon-50.png", 50, 50));
		JLabel brandingLogo = new JLabel(IconLoader.loadAndScaleIcon("/resources/images/hrh-icon.jpg", 50, 50));
		// Add to wrapper
		logoWrapper.add(umakLogo);
		logoWrapper.add(Box.createHorizontalStrut(10));
		logoWrapper.add(brandingLogo);
		
		// Set outer gaps
		westWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		// Add logo box
		westWrapper.add(logoWrapper);
		// Add empty space
		westWrapper.add(Box.createHorizontalStrut(10));
		
		// Make title box
		CustomPanel textWrapper = new CustomPanel();
		// Hide background
		textWrapper.setOpaque(false);
		// Stack down
		textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
		// Push to middle
		textWrapper.add(Box.createVerticalGlue());
		// Add big text
		textWrapper.add(new CustomLabel("HERONS' RESOURCE HUB", Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD));
		// Add small text
		textWrapper.add(new CustomLabel("A Gamified Resource Management System", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR));
		// Push to middle
		textWrapper.add(Box.createVerticalGlue());
		
		// Add texts to left side
		westWrapper.add(textWrapper);
		
		// Make right container
		CustomPanel eastWrapper = new CustomPanel();
		// Add right gap
		eastWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		// Hide background
		eastWrapper.setOpaque(false);
		// Stack right
		eastWrapper.setLayout(new BoxLayout(eastWrapper, BoxLayout.X_AXIS));
		
		// Load icon buttons
		JLabel homeBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/home.png", 45, 45));
		JLabel contactBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/contact.png", 40, 40));
		JLabel adminBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/admin.png", 40, 40));
		JLabel profileBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/profile.png", 40, 40));
		
		// Show pointers
		homeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		contactBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		adminBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		profileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		// Bind home button click
		homeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				switchPage(new Marketplace(currentUser));
			}
		});

		// Bind contact button click
		contactBtn.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				showContactDialog();
			}
		});

		// Bind admin button click
		adminBtn.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				switchPage(new AdminPanel());
			}
		});

		// Make drop down
		JPopupMenu profileMenu = new JPopupMenu();
		// Add gray border
		profileMenu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		// Make menu links
		JMenuItem profileItem = createMenuItem("Profile");
		// Bind click actions
		profileItem.addActionListener(e -> handleProfile());
		
		// Make menu links
		JMenuItem logoutItem = createMenuItem("Log Out");
		// Bind click actions
		logoutItem.addActionListener(e -> handleLogout());
		
		// Add profile link
		profileMenu.add(profileItem);
		// Add line break
		profileMenu.addSeparator();
		// Add logout link
		profileMenu.add(logoutItem);
		
		// Bind profile icon click
		profileBtn.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				// Open menu list
				profileMenu.show(profileBtn, 
					profileBtn.getWidth() - profileMenu.getPreferredSize().width, 
					profileBtn.getHeight()); // Align under button
			}
		});

		// Mount Home button
		eastWrapper.add(homeBtn);
		eastWrapper.add(Box.createHorizontalStrut(15));
		
		// Mount Contact button
		eastWrapper.add(contactBtn);
		eastWrapper.add(Box.createHorizontalStrut(15));

		// Add admin icon if admin
		if (SessionManager.get().isAdmin()) {
			eastWrapper.add(adminBtn);
			// Add space
			eastWrapper.add(Box.createHorizontalStrut(15));
		}
		// Add profile icon
		eastWrapper.add(profileBtn);
		
		// Mount left block
		header.add(westWrapper, BorderLayout.WEST);
		// Mount right block
		header.add(eastWrapper, BorderLayout.EAST);
		// Return finished bar
		return header;
	}

	// Build styled drop down links
	private JMenuItem createMenuItem(String text) {
		// Make menu button
		JMenuItem item = new JMenuItem(text);
		// Set fixed scale
		item.setPreferredSize(new Dimension(120, 30));
		// Show pointer
		item.setCursor(new Cursor(Cursor.HAND_CURSOR));
		// Make it white
		item.setBackground(Color.WHITE);
		// Apply custom font
		if (FontLib.POPPINS_REGULAR != null) {
			item.setFont(FontLib.POPPINS_REGULAR.deriveFont(13f));
		}
		// Return link
		return item;
	}

	// Open user profile view
	private void handleProfile() {
		// Load profile page
		switchPage(new Profile(currentUser));
	}

	// Run safe logout check
	private void handleLogout() {
		// Show popup question
		int confirm = JOptionPane.showConfirmDialog(this, 
			"Are you sure you want to log out?", 
			"Confirm Logout", JOptionPane.YES_NO_OPTION);
				
		// Close app if yes
		if (confirm == JOptionPane.YES_OPTION) {
			SessionManager.logout();
			dispose();
			new Auth();
		}
	}
	
	// Show contact developer dialog
	private void showContactDialog() {
		// Create modal dialog
		JDialog dialog = new JDialog(this, "Contact Information", true);
		
		// Create main container
		CustomPanel panel = new CustomPanel();
		panel.setPadding(20);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

		// Developer Title
		CustomLabel title = new CustomLabel("Lead Developer", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.setForeground(Brand.PRIMARY_COLOR);

		// Developer Name
		CustomLabel name = new CustomLabel("Renzjan Moncinilla", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
		name.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Developer Email
		CustomLabel email = new CustomLabel("UMak Email Address: renzjan.moncinilla@umak.edu.ph", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
		email.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Developer Phone
		CustomLabel phone = new CustomLabel("Contact Number: 09624414717", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
		phone.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Close Button
		CustomButton closeBtn = new CustomButton("Close", 10);
		closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		closeBtn.addActionListener(e -> dialog.dispose());

		// Assemble layout
		panel.add(title);
		panel.add(Box.createVerticalStrut(15));
		panel.add(name);
		panel.add(Box.createVerticalStrut(5));
		panel.add(email);
		panel.add(Box.createVerticalStrut(5));
		panel.add(phone);
		panel.add(Box.createVerticalStrut(25));
		panel.add(closeBtn);

		// Finalize dialog
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(this); // Center on main screen
		dialog.setResizable(false);
		dialog.setVisible(true);
	}
	
	// Swap center view
	public void switchPage(JPanel newContent) {
		// Clear old page
		bodyPanel.removeAll();
		// Insert new page
		bodyPanel.add(newContent, BorderLayout.CENTER);
		// Redraw screen
		bodyPanel.revalidate();
		bodyPanel.repaint();
	}
}