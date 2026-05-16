package auth;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.*;

import components.*;
import marketplace.Marketplace;
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

	// =========================================================
	// FIX DOUBLE CONFIRM / DOUBLE MARKETPLACE
	// =========================================================
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

	public SecurityDialog(JFrame parent, boolean isRegistration) {

		super(parent, "Security Verification", true);

		FontLib.loadFonts();

		this.isRegistration = isRegistration;

		setSize(500, isRegistration ? 600 : 350);

		setLayout(new BorderLayout());

		setLocationRelativeTo(parent);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		init();

		setVisible(true);
	}

	// =========================================================
	// UI BUILD
	// =========================================================
	private void init() {

		CustomPanel wrapper = new CustomPanel();

		wrapper.setPadding(20);

		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		// =====================================================
		// QUESTION 1
		// =====================================================
		securityQuestionComboBox1 =
				new CustomComboBox<>(baseQuestions, 5);

		securityAnswerField1 =
				new CustomTextArea();

		securityQuestionComboBox1
				.setAlignmentX(Component.LEFT_ALIGNMENT);

		securityAnswerField1
				.setAlignmentX(Component.LEFT_ALIGNMENT);

		wrapper.add(new CustomLabel(
				"Security Question 1",
				Brand.STANDARD_TEXT_SIZE,
				FontStyle.BOLD));

		wrapper.add(securityQuestionComboBox1);

		wrapper.add(Box.createVerticalStrut(5));

		wrapper.add(securityAnswerField1);

		wrapper.add(Box.createVerticalStrut(20));

		// =====================================================
		// QUESTION 2
		// =====================================================
		securityQuestionComboBox2 =
				new CustomComboBox<>(baseQuestions, 5);

		securityAnswerField2 =
				new CustomTextArea();

		securityQuestionComboBox2
				.setAlignmentX(Component.LEFT_ALIGNMENT);

		securityAnswerField2
				.setAlignmentX(Component.LEFT_ALIGNMENT);

		wrapper.add(new CustomLabel(
				"Security Question 2",
				Brand.STANDARD_TEXT_SIZE,
				FontStyle.BOLD));

		wrapper.add(securityQuestionComboBox2);

		wrapper.add(Box.createVerticalStrut(5));

		wrapper.add(securityAnswerField2);

		wrapper.add(Box.createVerticalStrut(20));

		// =====================================================
		// QUESTION 3
		// =====================================================
		securityQuestionComboBox3 =
				new CustomComboBox<>(baseQuestions, 5);

		securityAnswerField3 =
				new CustomTextArea();

		securityQuestionComboBox3
				.setAlignmentX(Component.LEFT_ALIGNMENT);

		securityAnswerField3
				.setAlignmentX(Component.LEFT_ALIGNMENT);

		wrapper.add(new CustomLabel(
				"Security Question 3",
				Brand.STANDARD_TEXT_SIZE,
				FontStyle.BOLD));

		wrapper.add(securityQuestionComboBox3);

		wrapper.add(Box.createVerticalStrut(5));

		wrapper.add(securityAnswerField3);

		// =====================================================
		// LISTENERS FOR DUPLICATES
		// =====================================================
		securityQuestionComboBox1
				.addActionListener(e -> refreshComboBoxes());

		securityQuestionComboBox2
				.addActionListener(e -> refreshComboBoxes());

		securityQuestionComboBox3
				.addActionListener(e -> refreshComboBoxes());

		// =====================================================
		// SUBMIT BUTTON
		// =====================================================
		submitButton = new CustomButton("Confirm", 10);

		submitButton.setPadding(30, 5);

		submitButton.setDefaultColor(
				Brand.PRIMARY_COLOR);

		submitButton.setHoverColor(
				Brand.PRIMARY_COLOR.darker());

		submitButton.addActionListener(e -> handleSubmit());

		CustomPanel buttonWrapper =
				new CustomPanel(
						new FlowLayout(FlowLayout.CENTER));

		buttonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

		buttonWrapper.add(submitButton);

		wrapper.add(Box.createVerticalStrut(15));

		wrapper.add(buttonWrapper);

		add(wrapper);
	}

	// =========================================================
	// REMOVE DUPLICATE QUESTIONS
	// =========================================================
	private void refreshComboBoxes() {

		String q1 =
				(String) securityQuestionComboBox1
						.getSelectedItem();

		String q2 =
				(String) securityQuestionComboBox2
						.getSelectedItem();

		String q3 =
				(String) securityQuestionComboBox3
						.getSelectedItem();

		updateModel(securityQuestionComboBox1, q2, q3);

		updateModel(securityQuestionComboBox2, q1, q3);

		updateModel(securityQuestionComboBox3, q1, q2);
	}

	private void updateModel(
			CustomComboBox<String> box,
			String exclude1,
			String exclude2) {

		String current =
				(String) box.getSelectedItem();

		box.removeAllItems();

		for (String q : baseQuestions) {

			if (q.equals(exclude1)
					|| q.equals(exclude2)) {

				continue;
			}

			box.addItem(q);
		}

		box.setSelectedItem(current);
	}

	// =========================================================
	// FINAL VALIDATION
	// =========================================================
	private void handleSubmit() {

		// =====================================================
		// PREVENT DOUBLE CLICK
		// =====================================================
		if (submitted) return;

		if (!isRegistration) return;

		String q1 =
				(String) securityQuestionComboBox1
						.getSelectedItem();

		String q2 =
				(String) securityQuestionComboBox2
						.getSelectedItem();

		String q3 =
				(String) securityQuestionComboBox3
						.getSelectedItem();

		String a1 =
				securityAnswerField1.getText().trim();

		String a2 =
				securityAnswerField2.getText().trim();

		String a3 =
				securityAnswerField3.getText().trim();

		// =====================================================
		// EMPTY QUESTION CHECK
		// =====================================================
		if (securityQuestionComboBox1.getSelectedIndex() == 0 ||
			securityQuestionComboBox2.getSelectedIndex() == 0 ||
			securityQuestionComboBox3.getSelectedIndex() == 0) {

			JOptionPane.showMessageDialog(
					this,
					"Please select all security questions.");

			return;
		}

		// =====================================================
		// EMPTY ANSWER CHECK
		// =====================================================
		if (a1.isEmpty() ||
			a2.isEmpty() ||
			a3.isEmpty()) {

			JOptionPane.showMessageDialog(
					this,
					"Please answer all security questions.");

			return;
		}

		// =====================================================
		// DUPLICATE CHECK
		// =====================================================
		if (q1.equals(q2) ||
			q1.equals(q3) ||
			q2.equals(q3)) {

			JOptionPane.showMessageDialog(
					this,
					"Security questions must be unique.");

			return;
		}

		// =====================================================
		// LOCK BUTTON
		// =====================================================
		submitted = true;

		submitButton.setEnabled(false);

		// =====================================================
		// CLOSE DIALOG
		// =====================================================
		dispose();

		// =====================================================
		// PROCEED DIRECTLY TO MARKETPLACE
		// =====================================================
		SwingUtilities.invokeLater(() -> {

			new Marketplace();

		});
	}

	// =========================================================
	// TEST
	// =========================================================
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {

			new SecurityDialog(null, true);

		});
	}
}