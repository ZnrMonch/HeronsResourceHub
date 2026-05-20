package auth;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.*;
import components.*;
import utils.*;

/**
 * Forgotpassword — modal dialog that lets a student reset their account
 * password.
 *
 * Flow: 1. User enters Student ID, UMak e-mail, a security question, and the
 * answer. 2. STEP 1 — The dialog checks if the Student ID exists and is not
 * archived. 3. STEP 2 — The dialog verifies the e-mail matches the found
 * account. 4. STEP 3 — The dialog checks the answer against the `SECURITY`
 * table (up to 3 Q&A pairs). 5. STEP 4 — On success, a second dialog collects
 * and confirms the new password, encrypts it, then persists it to the database.
 */
public class Forgotpassword extends JDialog {
	private static final long serialVersionUID = 1L;
	/** Label used to display validation / error messages to the user. */
	private CustomLabel forgotError;
	// ── Database connection constants ────────────────────────────────────────
	private static final String DB_URL = "jdbc:mysql://localhost:3306/heronsresourcehub";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "";

	// =========================================================================
	// INNER CLASS — UserRecord
	// =========================================================================
	/**
	 * Lightweight data holder returned by {@link #fetchUser(String)}. Carries only
	 * the fields needed for the forgot-password flow.
	 */
	private static class UserRecord {
		final int userId;
		final String email;

		UserRecord(int userId, String email) {
			this.userId = userId;
			this.email = email;
		}
	}

	/**
	 * Constructs and configures the Forgot Password dialog.
	 *
	 * @param authFrame the parent Auth window; used for centering and modality
	 */
	public Forgotpassword(Auth authFrame) {
		super(authFrame, "Forgot Password", true); // modal dialog
		setUndecorated(true); // remove OS window chrome
		setSize(500, 700);
		setLocationRelativeTo(authFrame);
		setBackground(new Color(0, 0, 0, 0)); // transparent background for rounded corners
		// ── Root panel with rounded corners ─────────────────────────────────
		JPanel mainPanel = new RoundedPanel(25);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.WHITE);
		// ── TOP PANEL — contains the back button ────────────────────────────
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
		// Back button closes this dialog and returns to the Auth screen
		JButton backButton = new JButton("← Back");
		backButton.setFocusPainted(false);
		backButton.setBorderPainted(false);
		backButton.setContentAreaFilled(false);
		backButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
		backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		backButton.addActionListener(e -> dispose()); // close dialog on click
		topPanel.add(backButton, BorderLayout.WEST);
		// ── CONTENT PANEL — vertically stacked form elements ────────────────
		JPanel contentPanel = new JPanel();
		contentPanel.setOpaque(false);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 35, 25, 35));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		// Informational icon at the top of the form
		JLabel iconLabel = new JLabel("ⓘ");
		iconLabel.setFont(new Font("SansSerif", Font.BOLD, 70));
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		// Brief description instructing the user what to do
		JLabel description = new JLabel("<html><div style='text-align: center;'>"
				+ "Please enter the required information<br>" + "below to update your password." + "</div></html>");
		description.setAlignmentX(Component.CENTER_ALIGNMENT);
		description.setHorizontalAlignment(SwingConstants.CENTER);
		description.setForeground(Color.GRAY);
		contentPanel.add(iconLabel);
		contentPanel.add(Box.createVerticalStrut(10)); // spacing
		contentPanel.add(description);
		contentPanel.add(Box.createVerticalStrut(30)); // spacing
		// ── INPUT FIELDS ─────────────────────────────────────────────────────
		CustomTextField idField = new CustomTextField("", 20, 15); // Student ID
		CustomTextField emailField = new CustomTextField("", 20, 15); // UMak e-mail
		// Dropdown listing the available security questions
		CustomComboBox<String> securityQuestionComboBox = new CustomComboBox<>(new String[] {
				"Select a Security Question", "What was the nickname your family called you as a child?",
				"What was the name of your first pet?", "What is your mother's complete maiden name?",
				"What was your favorite childhood food?", "What is the name of the hospital where you were born?",
				"What was your favorite family tradition during holidays?", "What is your favorite childhood movie?" },
				5);
		// Multi-line text area for the security answer
		CustomTextArea securityAnswerField = new CustomTextArea();
		securityAnswerField.setLineWrap(true);
		securityAnswerField.setWrapStyleWord(true);
		// Add labeled field wrappers to the content panel
		contentPanel.add(createField("Enter Student ID *", idField));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Enter UMak Email Address *", emailField));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Security Question *", securityQuestionComboBox));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Security Answer *", securityAnswerField));
		// ── ERROR LABEL ──────────────────────────────────────────────────────
		// Wrapped in HTML so long messages word-wrap instead of being clipped.
		// MaximumSize constrains the width so BoxLayout does not stretch it
		// beyond the usable content area (dialog 500px − 35px padding × 2 = 430px).
		forgotError = new CustomLabel("<html><div style='text-align:center;'></div></html>");
		forgotError.setForeground(Color.RED);
		forgotError.setAlignmentX(Component.CENTER_ALIGNMENT);
		forgotError.setHorizontalAlignment(SwingConstants.CENTER);
		forgotError.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));
		contentPanel.add(Box.createVerticalStrut(15));
		contentPanel.add(forgotError);
		// ── CONFIRM BUTTON — triggers verification and password reset flow ───
		CustomButton confirmButton = new CustomButton("Confirm", 20);
		confirmButton.setPreferredSize(new Dimension(300, 45));
		confirmButton.addActionListener(e -> {
			// Retrieve and trim user input
			String id = idField.getText().trim();
			String email = emailField.getText().trim();
			String answer = securityAnswerField.getText().trim();
			setError(""); // clear any previous error
			// ── CLIENT-SIDE VALIDATION ───────────────────────────────────
			// Ensure no required field is left blank before running any checks
			if (id.isEmpty() || email.isEmpty() || answer.isEmpty()) {
				setError("All fields are required.");
				return;
			}
			// Student ID must begin with 'K' or 'A' (case-insensitive),
			// followed by exactly 8 numeric digits — e.g. K12345678 or A87654321.
			// The (?i) flag makes the leading letter check case-insensitive so
			// both 'k' and 'K' (and 'a' / 'A') are accepted.
			if (!id.matches("(?i)^[KkAa][0-9]{8}$")) {
				setError("Invalid Student ID!");
				return;
			}
			// UMak institutional e-mail addresses must end with the official domain
			if (!email.endsWith("@umak.edu.ph")) {
				setError("Invalid UMak Email Address.");
				return;
			}
			if (securityQuestionComboBox.getSelectedIndex() == 0) {
				// Index 0 is the placeholder "Select a Security Question"
				setError("Please select a security question.");
				return;
			}
			if (answer.length() < 3) {
				setError("Security answer must be at least 3 characters.");
				return;
			}
			// ── STEP 1: CHECK STUDENT ID EXISTS AND IS NOT ARCHIVED ──────────
			UserRecord user = fetchUser(id);
			if (user == null) {
				// No active user was found with that Student ID
				setError("Student ID doesn't exist or account is archived.");
				return;
			}
			// ── STEP 2: VERIFY EMAIL MATCHES THE FOUND ACCOUNT ───────────────
			if (!user.email.equalsIgnoreCase(email)) {
				setError("Student ID or Email not found.");
				return;
			}
			int matchedUserId = user.userId; // carry forward for security check
			// ── STEP 3: VERIFY SECURITY QUESTION AND ANSWER ──────────────
			boolean securityVerified = false;
			String selectedQuestion = (String) securityQuestionComboBox.getSelectedItem();
			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
					PreparedStatement ps = conn.prepareStatement("SELECT * FROM SECURITY WHERE user_id = ?")) {
				ps.setInt(1, matchedUserId);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					// Retrieve up to three Q&A pairs stored for the user
					String q1 = rs.getString("security_question_1");
					String a1 = rs.getString("security_answer_1");
					String q2 = rs.getString("security_question_2");
					String a2 = rs.getString("security_answer_2");
					String q3 = rs.getString("security_question_3");
					String a3 = rs.getString("security_answer_3");
					// Case-insensitive answer match against any of the three pairs
					if ((selectedQuestion.equals(q1) && answer.equalsIgnoreCase(a1))
							|| (selectedQuestion.equals(q2) && answer.equalsIgnoreCase(a2))
							|| (selectedQuestion.equals(q3) && answer.equalsIgnoreCase(a3))) {
						securityVerified = true;
					}
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
				setError("Database error: " + ex.getMessage());
				return;
			}
			if (!securityVerified) {
				setError("Security question or answer is incorrect.");
				return;
			}
			JOptionPane.showMessageDialog(this, "Security verification successful.");
			// Capture the verified user ID in a final variable for use inside the lambda
			// below
			final int verifiedUserId = matchedUserId;
			// ── STEP 4: PASSWORD RESET DIALOG ────────────────────────────
			JDialog passwordDialog = new JDialog(authFrame, "Create New Password", true);
			passwordDialog.setSize(400, 350);
			passwordDialog.setLocationRelativeTo(authFrame);
			passwordDialog.setLayout(new BorderLayout());
			// Wrapper panel with padding, laid out vertically
			CustomPanel wrapper = new CustomPanel();
			wrapper.setPadding(20);
			wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
			CustomPasswordField newPasswordField = new CustomPasswordField(20, 15);
			CustomPasswordField confirmPasswordField = new CustomPasswordField(20, 15);
			// Label that reflects the current password strength in real time
			CustomLabel strengthLabel = new CustomLabel("");
			strengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			// ── PASSWORD STRENGTH LISTENER ───────────────────────────────
			// Fires on every keystroke to re-evaluate strength and update UI color + text
			newPasswordField.getDocument().addDocumentListener(new DocumentListener() {
				private void update() {
					String p = String.valueOf(newPasswordField.getPassword());
					PasswordStrength strength = getPasswordStrength(p);
					switch (strength) {
					case WEAK -> {
						newPasswordField.setForeground(Color.RED);
						strengthLabel.setForeground(Color.RED);
						strengthLabel.setText("Weak Password");
					}
					case MEDIUM -> {
						newPasswordField.setForeground(Color.ORANGE);
						strengthLabel.setForeground(Color.ORANGE);
						strengthLabel.setText("Medium Password");
					}
					case STRONG -> {
						newPasswordField.setForeground(Color.BLUE);
						strengthLabel.setForeground(Color.BLUE);
						strengthLabel.setText("Strong Password");
					}
					case VERY_STRONG -> {
						newPasswordField.setForeground(new Color(0, 150, 0));
						strengthLabel.setForeground(new Color(0, 150, 0));
						strengthLabel.setText("Very Strong Password");
					}
					}
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					update();
				}
			});
			// ─────────────────────────────────────────────────────────────
			wrapper.add(createField("Enter New Password *", newPasswordField));
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(strengthLabel); // strength indicator sits between the two fields
			wrapper.add(Box.createVerticalStrut(10));
			wrapper.add(createField("Confirm New Password *", confirmPasswordField));
			// ── RESET BUTTON — validates, encrypts, and saves new password ─
			CustomButton reset = new CustomButton("Confirm", 10);
			reset.addActionListener(ev -> {
				String p1 = String.valueOf(newPasswordField.getPassword()).trim();
				String p2 = String.valueOf(confirmPasswordField.getPassword()).trim();
				// Basic presence check
				if (p1.isEmpty() || p2.isEmpty()) {
					JOptionPane.showMessageDialog(passwordDialog, "All fields are required.");
					return;
				}
				// Ensure both entries match before proceeding
				if (!p1.equals(p2)) {
					JOptionPane.showMessageDialog(passwordDialog, "Passwords do not match.");
					return;
				}
				// Encrypt the new password before storing it
				String encryptedNew = Encryption.encryptPassword(p1);
				if (encryptedNew == null) {
					JOptionPane.showMessageDialog(passwordDialog, "Encryption error — please try again.");
					return;
				}
				// Persist the encrypted password to the database
				try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
						PreparedStatement ps = conn
								.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")) {
					ps.setString(1, encryptedNew); // store the encrypted value
					ps.setInt(2, verifiedUserId);
					ps.executeUpdate();
				} catch (SQLException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(passwordDialog, "Database error: " + ex.getMessage());
					return;
				}
				JOptionPane.showMessageDialog(passwordDialog, "Password updated successfully!");
				passwordDialog.dispose(); // close reset dialog
				dispose(); // close forgot-password dialog
			});
			// Button row at the bottom of the reset dialog
			JPanel btn = new JPanel(new FlowLayout());
			btn.setOpaque(false);
			btn.add(reset);
			wrapper.add(Box.createVerticalStrut(20));
			wrapper.add(btn);
			passwordDialog.add(wrapper);
			passwordDialog.setVisible(true); // show reset dialog (blocks until closed)
		});
		// Wrap the confirm button in a centered flow panel
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setOpaque(false);
		buttonPanel.add(confirmButton);
		contentPanel.add(Box.createVerticalStrut(20));
		contentPanel.add(buttonPanel);
		// Assemble main panel
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		setContentPane(mainPanel);
	}

	// =========================================================================
	// HELPER — setError
	// =========================================================================
	/**
	 * Sets the error label text, wrapping it in centred HTML so long messages
	 * word-wrap within the dialog instead of being clipped at the right edge.
	 *
	 * Pass an empty string to clear the label.
	 *
	 * @param message the plain-text error message to display (HTML-safe characters
	 *                only)
	 */
	private void setError(String message) {
		if (message == null || message.isEmpty()) {
			forgotError.setText("<html><div style='text-align:center;'></div></html>");
		} else {
			forgotError.setText("<html><div style='text-align:center;'>" + message + "</div></html>");
		}
	}

	// =========================================================================
	// HELPER — fetchUser
	// =========================================================================
	/**
	 * Looks up an active (non-archived) user by Student ID.
	 *
	 * @param studentId the raw Student ID string entered by the user
	 * @return a {@link UserRecord} if a matching active account exists, or
	 *         {@code null} if the student ID is unknown or the account is archived
	 */
	private UserRecord fetchUser(String studentId) {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				PreparedStatement ps = conn.prepareStatement("SELECT user_id, umak_email_address FROM users "
						+ "WHERE student_id = ? AND users_is_archived = false")) {
			ps.setString(1, studentId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return new UserRecord(rs.getInt("user_id"), rs.getString("umak_email_address"));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			setError("Database error: " + ex.getMessage());
		}
		return null;
	}

	// =========================================================================
	// PASSWORD STRENGTH EVALUATION
	// =========================================================================
	/** Categorical strength levels for a given password string. */
	private enum PasswordStrength {
		WEAK, MEDIUM, STRONG, VERY_STRONG
	}

	/**
	 * Rates the strength of {@code password} based on length and character variety.
	 *
	 * Scoring rules (each criterion adds 1 point): • Contains an uppercase letter •
	 * Contains a lowercase letter • Contains a digit • Contains a special character
	 * (!@#$%^&*()) • Length ≥ 10 characters
	 *
	 * Passwords of 5 characters or fewer are immediately rated WEAK regardless of
	 * content. Final rating: ≤ 2 pts → WEAK | 3 pts → MEDIUM | 4 pts → STRONG | 5
	 * pts → VERY_STRONG
	 *
	 * @param password the plain-text password to evaluate
	 * @return the corresponding {@link PasswordStrength} value
	 */
	private PasswordStrength getPasswordStrength(String password) {
		if (password.length() <= 5)
			return PasswordStrength.WEAK; // too short, no need to score
		boolean hasUpper = password.matches(".*[A-Z].*");
		boolean hasLower = password.matches(".*[a-z].*");
		boolean hasNum = password.matches(".*[0-9].*");
		boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");
		int score = 0;
		if (hasUpper)
			score++;
		if (hasLower)
			score++;
		if (hasNum)
			score++;
		if (hasSpecial)
			score++;
		if (password.length() >= 10)
			score++; // bonus point for longer passwords
		if (score <= 2)
			return PasswordStrength.WEAK;
		if (score == 3)
			return PasswordStrength.MEDIUM;
		if (score == 4)
			return PasswordStrength.STRONG;
		return PasswordStrength.VERY_STRONG;
	}

	// =========================================================================
	// HELPER — labeled field builder
	// =========================================================================
	/**
	 * Creates a small panel that pairs a bold label above a form component.
	 *
	 * Asterisks (*) in {@code label} are rendered in red to indicate required
	 * fields.
	 *
	 * @param label the display text for the field (use * to mark as required)
	 * @param field any Swing component to place below the label
	 * @return a configured JPanel ready to add to the form
	 */
	private JPanel createField(String label, JComponent field) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setOpaque(false);
		CustomLabel lbl = new CustomLabel(
				"<html><b>" + label.replace("*", "<span style='color:red;'>*</span>") + "</b></html>");
		panel.add(lbl, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90)); // prevent vertical stretching
		return panel;
	}

	// =========================================================================
	// INNER CLASS — RoundedPanel
	// =========================================================================
	/**
	 * A JPanel subclass that paints itself with rounded corners.
	 *
	 * It is rendered with a 1-pixel black border followed by the panel's own
	 * background color, creating a subtle outlined, rounded rectangle.
	 */
	class RoundedPanel extends JPanel {
		/** Corner arc radius in pixels. */
		private int radius;

		/**
		 * @param r arc radius for the rounded corners
		 */
		public RoundedPanel(int r) {
			this.radius = r;
			setOpaque(false); // let the custom paint handle the background
		}

		/**
		 * Paints a rounded rectangle: first a black outline, then the background color
		 * inset by 1 pixel on every side.
		 */
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Draw black border at full size
			g2.setColor(Color.BLACK);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
			// Draw background color inset by 1 px to reveal the border
			g2.setColor(getBackground());
			g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, radius, radius);
			g2.dispose();
			super.paintComponent(g); // paint child components on top
		}
	}

	/**
	 * Makes this dialog visible; call instead of {@code setVisible(true)} for
	 * clarity.
	 */
	public void showDialog() {
		setVisible(true);
	}
}
