package auth;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.*;
import components.*;
import utils.Brand;
import utils.FontLib;
import utils.FontStyle;

public class SecurityDialog extends JDialog {

	private CustomComboBox<String> securityQuestionComboBox1;
	private CustomComboBox<String> securityQuestionComboBox2;
	private CustomComboBox<String> securityQuestionComboBox3;
	
	private CustomTextArea securityAnswerField1;
	private CustomTextArea securityAnswerField2;
	private CustomTextArea securityAnswerField3;
	
	private CustomButton submitButton;
	private boolean isRegistration;
	
	public SecurityDialog(JFrame parent, boolean isRegistration) {
		FontLib.loadFonts();
		this.isRegistration = isRegistration;
		super(parent, "Security Verification", true);
		setSize(500, isRegistration ? 600 : 350);
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
		
		String[] securityQuestions = {
				"Select a Security Question",
				"What was the nickname your family called you as a child?",
				"What was the name of your first pet?",
				"What is your mother's complete maiden name?",
				"What was your favorite childhood food?",
				"What is the name of the hospital where you were born?",
				"What was your favorite family tradition during holidays?",
				"What is your favorite childhood movie?"
		};
		
		securityQuestionComboBox1 = new CustomComboBox<String>(securityQuestions, 5);
		securityAnswerField1 = new CustomTextArea();
		securityQuestionComboBox1.setAlignmentX(Component.LEFT_ALIGNMENT);
		securityAnswerField1.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		CustomLabel label1 = new CustomLabel("Security Question 1", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
		label1.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrapper.add(label1);
		wrapper.add(securityQuestionComboBox1);
		wrapper.add(Box.createVerticalStrut(5));
		wrapper.add(securityAnswerField1);
		wrapper.add(Box.createVerticalStrut(20));
		
		if (isRegistration) {
			securityQuestionComboBox2 = new CustomComboBox<String>(securityQuestions, 5);
			securityQuestionComboBox3 = new CustomComboBox<String>(securityQuestions, 5);
			
			securityAnswerField2 = new CustomTextArea();
			securityAnswerField3 = new CustomTextArea();
			
			securityQuestionComboBox2.setAlignmentX(Component.LEFT_ALIGNMENT);
			securityQuestionComboBox3.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			securityAnswerField2.setAlignmentX(Component.LEFT_ALIGNMENT);
			securityAnswerField3.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			CustomLabel label2 = new CustomLabel("Security Question 2", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
			label2.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(label2);
			wrapper.add(securityQuestionComboBox2);
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(securityAnswerField2);
			wrapper.add(Box.createVerticalStrut(20));
			
			CustomLabel label3 = new CustomLabel("Security Question 3", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
			label3.setAlignmentX(Component.LEFT_ALIGNMENT);
			wrapper.add(label3);
			wrapper.add(securityQuestionComboBox3);
			wrapper.add(Box.createVerticalStrut(5));
			wrapper.add(securityAnswerField3);
		}
		
		wrapper.add(Box.createVerticalStrut(10));
		
		submitButton = new CustomButton("Submit", 10);
		submitButton.setPadding(30, 5);
		submitButton.setDefaultColor(Brand.PRIMARY_COLOR);
		submitButton.setHoverColor(Brand.PRIMARY_COLOR.darker());
		
		CustomPanel buttonWrapper = new CustomPanel(new FlowLayout(FlowLayout.CENTER));
		buttonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttonWrapper.add(submitButton);
		wrapper.add(buttonWrapper);
		
		add(wrapper);
	}
	
	public static void main(String[] args) {
		new SecurityDialog(null, false);
	}

}