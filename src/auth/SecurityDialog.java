package auth;

import java.awt.*;
import javax.swing.*;

import components.*;
import utils.Brand;
import utils.FontLib;
import utils.FontStyle;

public class SecurityDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private CustomComboBox<String> securityQuestionComboBox1;
	private CustomComboBox<String> securityQuestionComboBox2;
	private CustomComboBox<String> securityQuestionComboBox3;

	private CustomTextArea securityAnswerField1;
	private CustomTextArea securityAnswerField2;
	private CustomTextArea securityAnswerField3;

	private CustomButton submitButton;

	private boolean isRegistration;
	private boolean isForgotPassword;

	private boolean submitted = false;

	private final String[] baseQuestions = {
			"Select a Security Question",
			"What was the nickname your family called you as a child?",
			"What was the name of your first pet?",
			"What is your mother's complete maiden name?",
			"What was your favorite childhood food?",
			"What is the name of the hospital where you were born?",
			"What was your favorite family tradition during holidays?",
			"What is your favorite childhood movie?"
	};

	// =========================
	// FIXED CONSTRUCTOR
	// =========================
	public SecurityDialog(JFrame parent) {

		super(parent, "Security Verification", true);

		FontLib.loadFonts();

		this.isRegistration = true;
		this.isForgotPassword = false;

		setSize(500, 600);

		setLayout(new BorderLayout());
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		init();

		setVisible(true);
	}

	private void init() {

		CustomPanel wrapper = new CustomPanel();
		wrapper.setPadding(20);
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		// =========================
		// REGISTRATION
		// =========================
		if (isRegistration) {

			securityQuestionComboBox1 = new CustomComboBox<>(baseQuestions, 5);
			securityAnswerField1 = new CustomTextArea();

			securityQuestionComboBox2 = new CustomComboBox<>(baseQuestions, 5);
			securityAnswerField2 = new CustomTextArea();

			securityQuestionComboBox3 = new CustomComboBox<>(baseQuestions, 5);
			securityAnswerField3 = new CustomTextArea();

			securityAnswerField1.setLineWrap(true);
			securityAnswerField2.setLineWrap(true);
			securityAnswerField3.setLineWrap(true);

			securityAnswerField1.setWrapStyleWord(true);
			securityAnswerField2.setWrapStyleWord(true);
			securityAnswerField3.setWrapStyleWord(true);

			CustomLabel label1 = new CustomLabel(
					"Security Question 1",
					Brand.STANDARD_TEXT_SIZE,
					FontStyle.BOLD);

			label1.setAlignmentX(Component.CENTER_ALIGNMENT);
			label1.setHorizontalAlignment(SwingConstants.CENTER);

			wrapper.add(label1);
			wrapper.add(securityQuestionComboBox1);
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(securityAnswerField1);
			wrapper.add(Box.createVerticalStrut(20));

			CustomLabel label2 = new CustomLabel(
					"Security Question 2",
					Brand.STANDARD_TEXT_SIZE,
					FontStyle.BOLD);

			label2.setAlignmentX(Component.CENTER_ALIGNMENT);
			label2.setHorizontalAlignment(SwingConstants.CENTER);

			wrapper.add(label2);
			wrapper.add(securityQuestionComboBox2);
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(securityAnswerField2);
			wrapper.add(Box.createVerticalStrut(20));

			CustomLabel label3 = new CustomLabel(
					"Security Question 3",
					Brand.STANDARD_TEXT_SIZE,
					FontStyle.BOLD);

			label3.setAlignmentX(Component.CENTER_ALIGNMENT);
			label3.setHorizontalAlignment(SwingConstants.CENTER);

			wrapper.add(label3);
			wrapper.add(securityQuestionComboBox3);
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(securityAnswerField3);

			securityQuestionComboBox1.addActionListener(e -> refreshComboBoxes());
			securityQuestionComboBox2.addActionListener(e -> refreshComboBoxes());
			securityQuestionComboBox3.addActionListener(e -> refreshComboBoxes());
		}

		// =========================
		// FORGOT PASSWORD
		// =========================
		else if (isForgotPassword) {

			securityQuestionComboBox1 = new CustomComboBox<>(baseQuestions, 5);
			securityAnswerField1 = new CustomTextArea();

			securityAnswerField1.setLineWrap(true);
			securityAnswerField1.setWrapStyleWord(true);

			wrapper.add(new CustomLabel(
					"Security Verification",
					Brand.STANDARD_TEXT_SIZE,
					FontStyle.BOLD));

			wrapper.add(Box.createVerticalStrut(10));
			wrapper.add(securityQuestionComboBox1);
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(securityAnswerField1);
		}

		// =========================
		// BUTTON
		// =========================
		submitButton = new CustomButton("Confirm", 10);
		submitButton.setPadding(30, 5);
		submitButton.setDefaultColor(Brand.PRIMARY_COLOR);
		submitButton.setHoverColor(Brand.PRIMARY_COLOR.darker());

		submitButton.addActionListener(e -> handleSubmit());

		CustomPanel buttonWrapper =
				new CustomPanel(new FlowLayout(FlowLayout.CENTER));

		buttonWrapper.add(submitButton);

		wrapper.add(Box.createVerticalStrut(20));
		wrapper.add(buttonWrapper);

		add(wrapper);
	}

	// =========================
	// FIXED SAFETY REFRESH
	// =========================
	private void refreshComboBoxes() {

		if (!isRegistration) return;

		String q1 = (String) securityQuestionComboBox1.getSelectedItem();
		String q2 = (String) securityQuestionComboBox2.getSelectedItem();
		String q3 = (String) securityQuestionComboBox3.getSelectedItem();

		updateModel(securityQuestionComboBox1, q2, q3);
		updateModel(securityQuestionComboBox2, q1, q3);
		updateModel(securityQuestionComboBox3, q1, q2);
	}

	private void updateModel(CustomComboBox<String> box,
							 String exclude1,
							 String exclude2) {

		String current = (String) box.getSelectedItem();

		box.removeAllItems();

		for (String q : baseQuestions) {
			if (q.equals(exclude1) || q.equals(exclude2)) continue;
			box.addItem(q);
		}

		box.setSelectedItem(current);
	}

	// =========================
	// SUBMIT LOGIC (UNCHANGED)
	// =========================
	private void handleSubmit() {

		if (submitted) return;

		if (isRegistration) {

			String q1 = (String) securityQuestionComboBox1.getSelectedItem();
			String q2 = (String) securityQuestionComboBox2.getSelectedItem();
			String q3 = (String) securityQuestionComboBox3.getSelectedItem();

			String a1 = securityAnswerField1.getText().trim();
			String a2 = securityAnswerField2.getText().trim();
			String a3 = securityAnswerField3.getText().trim();

			if (securityQuestionComboBox1.getSelectedIndex() == 0 ||
				securityQuestionComboBox2.getSelectedIndex() == 0 ||
				securityQuestionComboBox3.getSelectedIndex() == 0) {

				JOptionPane.showMessageDialog(this,
						"Please select all security questions.");
				return;
			}

			if (a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Please answer all security questions.");
				return;
			}

			if (a1.length() < 8 || a2.length() < 8 || a3.length() < 8) {
				JOptionPane.showMessageDialog(this,
						"Each security answer must be at least 8 characters.");
				return;
			}

			if (a1.length() > 100 || a2.length() > 100 || a3.length() > 100) {
				JOptionPane.showMessageDialog(this,
						"Each security answer must not exceed 100 characters.");
				return;
			}

			if (q1.equals(q2) || q1.equals(q3) || q2.equals(q3)) {
				JOptionPane.showMessageDialog(this,
						"Security questions must be unique.");
				return;
			}

			submitted = true;
			submitButton.setEnabled(false);

			dispose();

			SwingUtilities.invokeLater(() -> {
				Window window = SwingUtilities.getWindowAncestor(this);
				if (window != null) window.dispose();

				JOptionPane.showMessageDialog(null,
						"Registration completed. Please login to continue.");

				new Auth();
			});
		}

		else if (isForgotPassword) {

			String answer = securityAnswerField1.getText().trim();

			if (securityQuestionComboBox1.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(this,
						"Please select a security question.");
				return;
			}

			if (answer.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Please enter your answer.");
				return;
			}

			if (answer.length() < 8) {
				JOptionPane.showMessageDialog(this,
						"Security answer must be at least 8 characters.");
				return;
			}

			if (answer.length() > 100) {
				JOptionPane.showMessageDialog(this,
						"Security answer must not exceed 100 characters.");
				return;
			}

			submitted = true;
			submitButton.setEnabled(false);

			JOptionPane.showMessageDialog(this,
					"Security verification successful. Please login again.");

			dispose();

			SwingUtilities.invokeLater(() -> {
				Window window = SwingUtilities.getWindowAncestor(this);
				if (window != null) window.dispose();

				new Auth();
			});
		}
	}

	public boolean isSubmitted() {
		return submitted;
	}
}