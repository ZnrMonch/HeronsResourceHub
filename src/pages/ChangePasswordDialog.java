package pages;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public class ChangePasswordDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private String storedPassword;

	public ChangePasswordDialog(Window parent, String storedPassword, Consumer<String> onSuccess) {
		super(parent, "Change Password", ModalityType.APPLICATION_MODAL);
		this.storedPassword = storedPassword;
		
		// Use default JDialog settings (decorated with OS title bar)
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		// Get default content pane
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(10, 10));
		((JComponent) cp).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Center - input fields using standard JPanel
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

		JPasswordField curField = new JPasswordField(20);
		JPasswordField newField = new JPasswordField(20);
		JPasswordField confField = new JPasswordField(20);

		center.add(createLabeledField("Current Password", curField));
		center.add(Box.createVerticalStrut(15));
		center.add(createLabeledField("New Password", newField));
		center.add(Box.createVerticalStrut(15));
		center.add(createLabeledField("Confirm New Password", confField));
		center.add(Box.createVerticalStrut(10));

		JLabel errorLabel = new JLabel(" ");
		errorLabel.setForeground(Color.RED);
		errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		center.add(errorLabel);

		cp.add(center, BorderLayout.CENTER);

		// Buttons using standard JPanel
		JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		JButton cancelBtn = new JButton("Cancel");
		JButton changeBtn = new JButton("Change Password");
		
		cancelBtn.addActionListener(e -> dispose());
		changeBtn.addActionListener(e -> {
			String cur = new String(curField.getPassword());
			String n = new String(newField.getPassword());
			String c = new String(confField.getPassword());
			
			if (cur == null) cur = "";
			if (n == null) n = "";
			if (c == null) c = "";
			
			if ((this.storedPassword != null && !this.storedPassword.isEmpty()) && cur.isEmpty()) {
				errorLabel.setText("Current password is required.");
				return;
			}
			if (n.isEmpty() || c.isEmpty()) {
				errorLabel.setText("Invalid Parameters.");
				return;
			}
			if (this.storedPassword != null && !this.storedPassword.isEmpty() && !this.storedPassword.equals(cur)) {
				errorLabel.setText("Current password is incorrect.");
				return;
			}
			if (!n.equals(c)) {
				errorLabel.setText("New password and confirmation do not match.");
				return;
			}
			
			onSuccess.accept(n);
			dispose();
		});

		south.add(cancelBtn);
		south.add(changeBtn);
		cp.add(south, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private JPanel createLabeledField(String labelText, JPasswordField field) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Create standard label with red asterisk
		JLabel label = new JLabel("<html>" + labelText + " <font color='red'>*</font></html>");
		panel.add(label, BorderLayout.NORTH);
		
		// Add padding to standard text field
		field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.GRAY), 
			BorderFactory.createEmptyBorder(8, 10, 8, 10)
		));
		panel.add(field, BorderLayout.CENTER);
		
		return panel;
	}
}