package auth;

import java.awt.*;
import javax.swing.*;

import components.*;
import utils.*;

public class Forgotpassword extends JDialog {

	private CustomLabel forgotError;

	public Forgotpassword(Auth authFrame) {

		super(authFrame, "Forgot Password", true);

		setUndecorated(true);
		setSize(420, 650);
		setLocationRelativeTo(authFrame);
		setBackground(new Color(0, 0, 0, 0));

		// =========================
		// MAIN PANEL
		// =========================
		JPanel mainPanel = new RoundedPanel(25);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.WHITE);

		// =========================
		// TOP PANEL
		// =========================
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

		JButton backButton = new JButton("← Back");
		backButton.setFocusPainted(false);
		backButton.setBorderPainted(false);
		backButton.setContentAreaFilled(false);
		backButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
		backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		backButton.addActionListener(e -> dispose());

		topPanel.add(backButton, BorderLayout.WEST);

		// =========================
		// CONTENT PANEL
		// =========================
		JPanel contentPanel = new JPanel();
		contentPanel.setOpaque(false);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 35, 25, 35));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		JLabel iconLabel = new JLabel("ⓘ");
		iconLabel.setFont(new Font("SansSerif", Font.BOLD, 70));
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel description = new JLabel(
				"<html><center>Please enter the required information<br>below to update your password.</center></html>");

		description.setAlignmentX(Component.CENTER_ALIGNMENT);
		description.setHorizontalAlignment(SwingConstants.CENTER);
		description.setForeground(Color.GRAY);

		contentPanel.add(iconLabel);
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(description);
		contentPanel.add(Box.createVerticalStrut(30));

		// =========================
		// FIELDS
		// =========================
		CustomTextField idField = new CustomTextField("", 20, 15);
		CustomTextField emailField = new CustomTextField("", 20, 15);
		CustomPasswordField newPass = new CustomPasswordField(20, 15);
		CustomPasswordField confirmPass = new CustomPasswordField(20, 15);

		contentPanel.add(createField("Enter Student ID *", idField));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Enter UMak Email Address *", emailField));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Enter New Password *", newPass));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Confirm New Password *", confirmPass));

		// =========================
		// ERROR LABEL
		// =========================
		forgotError = new CustomLabel("");
		forgotError.setForeground(Color.RED);
		forgotError.setAlignmentX(Component.CENTER_ALIGNMENT);

		contentPanel.add(Box.createVerticalStrut(15));
		contentPanel.add(forgotError);

		// =========================
		// CONFIRM BUTTON
		// =========================
		CustomButton confirmButton = new CustomButton("Confirm", 20);
		confirmButton.setPreferredSize(new Dimension(300, 45));
		confirmButton.setMaximumSize(new Dimension(300, 45));

		confirmButton.addActionListener(e -> {

			String id = idField.getText().trim();
			String email = emailField.getText().trim();
			String newPassStr = String.valueOf(newPass.getPassword()).trim();
			String confirmPassStr = String.valueOf(confirmPass.getPassword()).trim();

			forgotError.setText("");

			if (id.isEmpty() || email.isEmpty()
					|| newPassStr.isEmpty() || confirmPassStr.isEmpty()) {

				forgotError.setText("All fields are required.");
				return;
			}

			if (!id.matches("[AaKk][0-9]{8}")) {
				forgotError.setText("Invalid Student ID.");
				return;
			}

			if (!email.endsWith("@umak.edu.ph")) {
				forgotError.setText("Invalid UMak Email Address.");
				return;
			}

			if (!newPassStr.equals(confirmPassStr)) {
				forgotError.setText("Password does not match.");
				return;
			}

			SecurityDialog security =
					new SecurityDialog(authFrame, false);

			boolean securityPassed = security.isVisible();

			if (!securityPassed) {
				forgotError.setText("Security verification failed.");
				return;
			}

			JOptionPane.showMessageDialog(
					this,
					"Password updated successfully!",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);

			dispose();
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setOpaque(false);
		buttonPanel.add(confirmButton);

		contentPanel.add(Box.createVerticalStrut(20));
		contentPanel.add(buttonPanel);

		// =========================
		// FINAL LAYOUT
		// =========================
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		setContentPane(mainPanel);
	}

	// =========================
	// FIELD UI (BOLD LABEL FIXED)
	// =========================
	private JPanel createField(String label, JComponent field) {

		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setOpaque(false);

		CustomLabel lbl = new CustomLabel(
			"<html><b>" + label.replace("*", "<span style='color:red;'>*</span>") + "</b></html>"
		); // ✅ BOLD + RED ASTERISK

		lbl.setFontStyle(FontStyle.BOLD);
		lbl.setFontSize(Brand.STANDARD_TEXT_SIZE);

		panel.add(lbl, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

		return panel;
	}

	public void showDialog() {
		setVisible(true);
	}

	// =========================
	// ROUNDED PANEL
	// =========================
	class RoundedPanel extends JPanel {

		private int radius;
		private int borderThickness = 1;

		public RoundedPanel(int radius) {
			this.radius = radius;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(Color.BLACK);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

			g2.setColor(getBackground());
			g2.fillRoundRect(
					borderThickness,
					borderThickness,
					getWidth() - (borderThickness * 2),
					getHeight() - (borderThickness * 2),
					radius,
					radius
			);

			g2.dispose();
			super.paintComponent(g);
		}
	}
}