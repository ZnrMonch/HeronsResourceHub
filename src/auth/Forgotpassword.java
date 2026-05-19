package auth;


import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.*;


import components.*;
import utils.*;


public class Forgotpassword extends JDialog {


	private static final long serialVersionUID = 1L;


	private CustomLabel forgotError;


	// =========================
	// DATABASE CONNECTIONS
	// =========================


	// USERS TABLE (login validation)
	private static final String DB_URL =
			"jdbc:mysql://localhost:4306/dbnorm2";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "";


	public Forgotpassword(Auth authFrame) {


		super(authFrame, "Forgot Password", true);


		setUndecorated(true);
		setSize(500, 700);
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
		description.setAlignmentY(Component.CENTER_ALIGNMENT);
		description.setForeground(Color.GRAY);


		contentPanel.add(iconLabel);
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(description);
		contentPanel.add(Box.createVerticalStrut(30));


		// =========================
		// INPUT FIELDS
		// =========================
		CustomTextField idField = new CustomTextField("", 20, 15);
		CustomTextField emailField = new CustomTextField("", 20, 15);


		CustomComboBox<String> securityQuestionComboBox =
				new CustomComboBox<>(new String[]{
						"Select a Security Question",
						"What was the nickname your family called you as a child?",
						"What was the name of your first pet?",
						"What is your mother's complete maiden name?",
						"What was your favorite childhood food?",
						"What is the name of the hospital where you were born?",
						"What was your favorite family tradition during holidays?",
						"What is your favorite childhood movie?"
				}, 5);


		CustomTextArea securityAnswerField = new CustomTextArea();
		securityAnswerField.setLineWrap(true);
		securityAnswerField.setWrapStyleWord(true);


		contentPanel.add(createField("Enter Student ID *", idField));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Enter UMak Email Address *", emailField));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Security Question *", securityQuestionComboBox));
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(createField("Security Answer *", securityAnswerField));


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


		confirmButton.addActionListener(e -> {


			String id = idField.getText().trim();
			String email = emailField.getText().trim();
			String answer = securityAnswerField.getText().trim();


			forgotError.setText("");


			if (id.isEmpty() || email.isEmpty() || answer.isEmpty()) {
				forgotError.setText("All fields are required.");
				return;
			}


			if (!email.endsWith("@umak.edu.ph")) {
				forgotError.setText("Invalid UMak Email Address.");
				return;
			}


			if (securityQuestionComboBox.getSelectedIndex() == 0) {
				forgotError.setText("Please select a security question.");
				return;
			}


			if (answer.length() < 3) {
				forgotError.setText("Security answer must be at least 8 characters.");
				return;
			}


			// =========================
			// 1. VERIFY USER (users table)
			// =========================
			int matchedUserId = -1;


			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			     PreparedStatement ps = conn.prepareStatement(
			         "SELECT user_id FROM users WHERE student_id = ? AND umak_email_address = ? AND users_is_archived = false")) {


				ps.setString(1, id);
				ps.setString(2, email);


				ResultSet rs = ps.executeQuery();


				if (rs.next()) {
					matchedUserId = rs.getInt("user_id");
				} else {
					forgotError.setText("Student ID or Email not found.");
					return;
				}


			} catch (SQLException ex) {
				ex.printStackTrace();
				forgotError.setText("Database error: " + ex.getMessage());
				return;
			}


			// =========================
			// 2. VERIFY SECURITY (security table)
			// =========================
			boolean securityVerified = false;
			String selectedQuestion = (String) securityQuestionComboBox.getSelectedItem();


			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			     PreparedStatement ps = conn.prepareStatement(
			         "SELECT * FROM SECURITY WHERE user_id = ?")) {


				ps.setInt(1, matchedUserId);


				ResultSet rs = ps.executeQuery();


				if (rs.next()) {


					String q1 = rs.getString("security_question_1");
					String a1 = rs.getString("security_answer_1");
					String q2 = rs.getString("security_question_2");
					String a2 = rs.getString("security_answer_2");
					String q3 = rs.getString("security_question_3");
					String a3 = rs.getString("security_answer_3");


					if ((selectedQuestion.equals(q1) && answer.equalsIgnoreCase(a1)) ||
					    (selectedQuestion.equals(q2) && answer.equalsIgnoreCase(a2)) ||
					    (selectedQuestion.equals(q3) && answer.equalsIgnoreCase(a3))) {


						securityVerified = true;
					}
				}


			} catch (SQLException ex) {
				ex.printStackTrace();
				forgotError.setText("Database error: " + ex.getMessage());
				return;
			}


			if (!securityVerified) {
				forgotError.setText("Security question or answer is incorrect.");
				return;
			}


			JOptionPane.showMessageDialog(this, "Security verification successful.");


			final int verifiedUserId = matchedUserId;


			// =========================
			// PASSWORD RESET DIALOG
			// =========================
			JDialog passwordDialog = new JDialog(authFrame, "Create New Password", true);
			passwordDialog.setSize(400, 350);
			passwordDialog.setLocationRelativeTo(authFrame);
			passwordDialog.setLayout(new BorderLayout());


			CustomPanel wrapper = new CustomPanel();
			wrapper.setPadding(20);
			wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));


			CustomPasswordField newPasswordField = new CustomPasswordField(20, 15);
			CustomPasswordField confirmPasswordField = new CustomPasswordField(20, 15);


			CustomLabel strengthLabel = new CustomLabel("");
			strengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


			wrapper.add(createField("Enter New Password *", newPasswordField));
			wrapper.add(Box.createVerticalStrut(10));
			wrapper.add(strengthLabel);
			wrapper.add(Box.createVerticalStrut(10));
			wrapper.add(createField("Confirm New Password *", confirmPasswordField));


			CustomButton reset = new CustomButton("Confirm", 10);


			reset.addActionListener(ev -> {


				String p1 = String.valueOf(newPasswordField.getPassword()).trim();
				String p2 = String.valueOf(confirmPasswordField.getPassword()).trim();


				if (p1.isEmpty() || p2.isEmpty()) {
					JOptionPane.showMessageDialog(passwordDialog, "All fields are required.");
					return;
				}


				if (!p1.equals(p2)) {
					JOptionPane.showMessageDialog(passwordDialog, "Password does not match.");
					return;
				}


				try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				     PreparedStatement ps = conn.prepareStatement(
				         "UPDATE users SET password = ? WHERE user_id = ?")) {


					ps.setString(1, p1);
					ps.setInt(2, verifiedUserId);


					ps.executeUpdate();


				} catch (SQLException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(passwordDialog, "Database error: " + ex.getMessage());
					return;
				}


				JOptionPane.showMessageDialog(passwordDialog, "Password updated successfully!");
				passwordDialog.dispose();
				dispose();
			});


			JPanel btn = new JPanel(new FlowLayout());
			btn.setOpaque(false);
			btn.add(reset);


			wrapper.add(Box.createVerticalStrut(20));
			wrapper.add(btn);


			passwordDialog.add(wrapper);
			passwordDialog.setVisible(true);
		});


		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setOpaque(false);
		buttonPanel.add(confirmButton);


		contentPanel.add(Box.createVerticalStrut(20));
		contentPanel.add(buttonPanel);


		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);


		setContentPane(mainPanel);
	}


	
	// =========================
	private JPanel createField(String label, JComponent field) {


		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setOpaque(false);


		CustomLabel lbl = new CustomLabel(
				"<html><b>" + label.replace("*", "<span style='color:red;'>*</span>") + "</b></html>");


		panel.add(lbl, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);


		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
		return panel;
	}


	class RoundedPanel extends JPanel {


		private int radius;


		public RoundedPanel(int r) {
			this.radius = r;
			setOpaque(false);
		}


		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


			g2.setColor(Color.BLACK);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);


			g2.setColor(getBackground());
			g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, radius, radius);


			g2.dispose();
			super.paintComponent(g);
		}
	}


	public void showDialog() {
		setVisible(true);
	}
}



