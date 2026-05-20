package pages;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import components.*;
import utils.*;

// Popup window to change user password
public class ChangePasswordDialog extends JDialog {

	// FIELDS

	// Version tracker ID
	private static final long serialVersionUID = 1L;
	// Saves the old password
	private String storedPassword;

	// CONSTRUCTORS

	// Builds the password change window UI
	public ChangePasswordDialog(Window parent, String storedPassword, Consumer<String> onSuccess) {
		// Set title and block parent view
		super(parent, "Change Password", ModalityType.APPLICATION_MODAL);
		
		// Save old password
		this.storedPassword = storedPassword;

		// Lock window size
		setResizable(false);
		// Close when X is clicked
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// Get main window box
		Container cp = getContentPane();
		// Set outer spacing
		cp.setLayout(new BorderLayout(10, 10));
		((JComponent) cp).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Make center panel
		JPanel center = new JPanel();
		// Stack items down
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

		// Make input boxes
		RoundedPasswordField curField = new RoundedPasswordField(20);
		RoundedPasswordField newField = new RoundedPasswordField(20);
		RoundedPasswordField confField = new RoundedPasswordField(20);

		// Add old password box
		center.add(createLabeledField("Current Password", curField));
		center.add(Box.createVerticalStrut(15));
		// Add new password box
		center.add(createLabeledField("New Password", newField));
		center.add(Box.createVerticalStrut(15));
		// Add confirm password box
		center.add(createLabeledField("Confirm New Password", confField));
		center.add(Box.createVerticalStrut(10));

		// Make error text label
		JLabel errorLabel = new JLabel(" ");
		// Set font design
		if (FontLib.POPPINS_REGULAR != null) errorLabel.setFont(FontLib.POPPINS_REGULAR.deriveFont(Brand.STANDARD_TEXT_SIZE));
		// Make text red
		errorLabel.setForeground(Color.RED);
		// Move to left
		errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		// Add to view
		center.add(errorLabel);

		// Mount center panel
		cp.add(center, BorderLayout.CENTER);

		// Make bottom button panel
		JPanel south = new JPanel(new BorderLayout());

		// Make cancel button
		CustomButton cancelBtn = new CustomButton("Cancel", 12);
		cancelBtn.setDefaultColor(Color.decode("#D9534F"));
		cancelBtn.setTextColor(Color.WHITE);
		cancelBtn.setPadding(8);

		// Make save button
		CustomButton changeBtn = new CustomButton("Change Password", 12);
		changeBtn.setDefaultColor(Color.decode("#080B51"));
		changeBtn.setTextColor(Color.WHITE);
		changeBtn.setPadding(8);

		// Close window on cancel
		cancelBtn.addActionListener(e -> dispose());

		// Check errors on save
		changeBtn.addActionListener(e -> {
			// Read text inputs
			String cur = new String(curField.getPassword());
			String n = new String(newField.getPassword());
			String c = new String(confField.getPassword());

			// Fix null values
			if (cur == null) cur = "";
			if (n == null) n = "";
			if (c == null) c = "";

			// Check if old password missing
			if ((this.storedPassword != null && !this.storedPassword.isEmpty()) && cur.isEmpty()) {
				errorLabel.setText("Current password is required.");
				return;
			}
			// Check if new password missing
			if (n.isEmpty() || c.isEmpty()) {
				errorLabel.setText("Please fill out all required fields.");
				return;
			}
			// Check if old password wrong
			if (this.storedPassword != null && !this.storedPassword.isEmpty() && !this.storedPassword.equals(cur)) {
				errorLabel.setText("Current password is incorrect.");
				return;
			}
			// Check if new passwords mismatch
			if (!n.equals(c)) {
				errorLabel.setText("New password and confirmation do not match.");
				return;
			}

			// Send new password back
			onSuccess.accept(n);
			// Close window
			dispose();
		});

		// Wrap cancel button left
		JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		leftWrap.setOpaque(false);
		leftWrap.add(cancelBtn);

		// Wrap save button right
		JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightWrap.setOpaque(false);
		rightWrap.add(changeBtn);

		// Mount buttons
		south.add(leftWrap, BorderLayout.WEST);
		south.add(rightWrap, BorderLayout.EAST);
		cp.add(south, BorderLayout.SOUTH);

		// Resize to fit items
		pack();
		// Move to center
		setLocationRelativeTo(parent);
		// Show window
		setVisible(true);
	}

	// METHODS

	// Wraps a text box with a top label text
	private JPanel createLabeledField(String labelText, JPasswordField field) {
		// Make container box
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Make label text
		CustomLabel label = new CustomLabel(labelText, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		// Make red star
		JLabel star = new JLabel("*"); star.setForeground(Color.RED); star.setFont(label.getFont());
		// Group text and star
		JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		labelRow.setOpaque(false); labelRow.add(label); labelRow.add(star);
		// Add label to top
		panel.add(labelRow, BorderLayout.NORTH);

		// Add inner gap for icon
		field.setBorder(new EmptyBorder(6, 10, 6, 12));
		// Apply font style
		if (FontLib.POPPINS_REGULAR != null) field.setFont(FontLib.POPPINS_REGULAR.deriveFont(Brand.STANDARD_TEXT_SIZE));

		// Check if it is a password box
		if (field instanceof JPasswordField) {
			Icon openIcon = null, closedIcon = null;
			// Try loading image files
			try {
				openIcon = IconLoader.loadAndScaleIcon("/resources/icons/eye.png", 18, 18);
				closedIcon = IconLoader.loadAndScaleIcon("/resources/icons/eye-off.png", 18, 18);
			} catch (Exception ignored) {}
			
			// Use drawn icon if load fails
			final Icon resOpen = openIcon != null ? openIcon : new SimpleEyeIcon(true, 18, 18);
			final Icon resClosed = closedIcon != null ? closedIcon : new SimpleEyeIcon(false, 18, 18);

			// Make eye button
			JLabel eye = new JLabel(resClosed);
			eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			eye.setBorder(new EmptyBorder(0, 2, 0, 4));
			
			// Track if showing text
			final boolean[] showing = { false };
			
			// Click to toggle hidden text
			eye.addMouseListener(new MouseAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					JPasswordField pf = (JPasswordField) field;
					// Hide text
					if (showing[0]) { pf.setEchoChar('*'); eye.setIcon(resClosed); showing[0] = false; }
					// Show text
					else { pf.setEchoChar((char)0); eye.setIcon(resOpen); showing[0] = true; }
				}
			});

			// Put eye inside the text box
			try { field.setLayout(new BorderLayout()); field.add(eye, BorderLayout.EAST); }
			catch (Exception ex) { 
				// Fallback if inside fails
				JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(field, BorderLayout.CENTER); wrap.add(eye, BorderLayout.EAST); panel.add(wrap, BorderLayout.CENTER); return panel; 
			}
		}

		// Mount text box to center
		panel.add(field, BorderLayout.CENTER);
		return panel;
	}

	// INNER CLASSES

	// Custom input box with curved edges
	private static class RoundedPasswordField extends JPasswordField {
		
		// FIELDS
		
		// Version ID
		private static final long serialVersionUID = 1L;
		// Curve amount
		private int radius = 12;
		// Mouse state
		private boolean isHovered = false;

		// CONSTRUCTORS

		// Builds the styled text box
		public RoundedPasswordField(int columns) {
			super(columns);
			// Hide default box
			setOpaque(false);
			// Add inner text gap
			setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
			// Set font
			if (FontLib.POPPINS_REGULAR != null) setFont(FontLib.POPPINS_REGULAR.deriveFont(Brand.STANDARD_TEXT_SIZE));

			// Use star to hide text
			try { setEchoChar('*'); } catch (Exception ignore) {}

			// Check mouse enters box
			addMouseListener(new MouseAdapter() {
				@Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
				@Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
			});
			// Check if typing in box
			addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { repaint(); } public void focusLost(java.awt.event.FocusEvent e) { repaint(); } });
		}

		// METHODS

		// Fills inner box color
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			// Smooth edges
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			// Draw curved square
			g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
			g2.dispose();
			super.paintComponent(g);
		}

		// Draws outline border
		@Override
		protected void paintBorder(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			// Smooth edges
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Black if clicked or hovered, gray if not
			g2.setColor(hasFocus() || isHovered ? Color.BLACK : Color.LIGHT_GRAY);
			// Draw curved outline
			g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
			g2.dispose();
		}
	}

	// Fallback drawn eye icon if images fail to load
	private static class SimpleEyeIcon implements Icon {
		
		// FIELDS
		
		// Icon width
		private final int w;
		// Icon height
		private final int h;
		// Open or closed state
		private final boolean open;
		
		// CONSTRUCTORS

		// Saves icon details
		public SimpleEyeIcon(boolean open, int w, int h) { this.open = open; this.w = w; this.h = h; }
		
		// METHODS

		// Returns width
		@Override public int getIconWidth() { return w; }
		// Returns height
		@Override public int getIconHeight() { return h; }
		
		// Draws the lines for the eye shape
		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			// Smooth edges
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Move to correct spot
			g2.translate(x, y);
			
			// Setup variables
			int vw = w; int vh = h;
			int cx = vw/2; int cy = vh/2;
			int rx = vw/2 - 2; int ry = vh/4;
			
			// Draw outer eye shape
			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(1.5f));
			java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
			path.moveTo(2, cy);
			path.quadTo(cx, 2, vw-2, cy);
			path.quadTo(cx, vh-2, 2, cy);
			g2.draw(path);
			
			// Draw middle dot if open
			if (open) {
				int pr = Math.min(vw, vh) / 6;
				g2.fillOval(cx - pr, cy - pr, pr*2, pr*2);
			} else {
				// Draw dash line if closed
				g2.setStroke(new BasicStroke(2.0f));
				g2.drawLine(3, cy, vw-3, cy);
			}
			g2.dispose();
		}
	}

}