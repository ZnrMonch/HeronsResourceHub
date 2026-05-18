package pages;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import components.*;
import utils.*;

public class ChangePasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private String storedPassword;

	public ChangePasswordDialog(Window parent, String storedPassword, Consumer<String> onSuccess) {

		super(parent, "Change Password", ModalityType.APPLICATION_MODAL);

		this.storedPassword = storedPassword;

// Use default JDialog settings (decorated with OS title bar)

		setResizable(false);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(10, 10));
		((JComponent) cp).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Center panel
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

		RoundedPasswordField curField = new RoundedPasswordField(20);
		RoundedPasswordField newField = new RoundedPasswordField(20);
		RoundedPasswordField confField = new RoundedPasswordField(20);

		center.add(createLabeledField("Current Password", curField));
		center.add(Box.createVerticalStrut(15));
		center.add(createLabeledField("New Password", newField));
		center.add(Box.createVerticalStrut(15));
		center.add(createLabeledField("Confirm New Password", confField));
		center.add(Box.createVerticalStrut(10));

		JLabel errorLabel = new JLabel(" ");
		if (FontLib.POPPINS_REGULAR != null)
			errorLabel.setFont(FontLib.POPPINS_REGULAR.deriveFont(Brand.STANDARD_TEXT_SIZE));
		errorLabel.setForeground(Color.RED);
		errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		center.add(errorLabel);

		cp.add(center, BorderLayout.CENTER);

		// Buttons: cancel left, change right (keeps button sizes/format)
		JPanel south = new JPanel(new BorderLayout());

		CustomButton cancelBtn = new CustomButton("Cancel", 12);
		cancelBtn.setDefaultColor(Color.decode("#D9534F"));
		cancelBtn.setTextColor(Color.WHITE);
		cancelBtn.setPadding(8);

		CustomButton changeBtn = new CustomButton("Change Password", 12);
		changeBtn.setDefaultColor(Color.decode("#080B51"));
		changeBtn.setTextColor(Color.WHITE);
		changeBtn.setPadding(8);

		cancelBtn.addActionListener(e -> dispose());

		changeBtn.addActionListener(e -> {
			String cur = new String(curField.getPassword());
			String n = new String(newField.getPassword());
			String c = new String(confField.getPassword());

			if (cur == null)
				cur = "";
			if (n == null)
				n = "";
			if (c == null)
				c = "";

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

		JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		leftWrap.setOpaque(false);
		leftWrap.add(cancelBtn);

		JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightWrap.setOpaque(false);
		rightWrap.add(changeBtn);

		south.add(leftWrap, BorderLayout.WEST);
		south.add(rightWrap, BorderLayout.EAST);
		cp.add(south, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private JPanel createLabeledField(String labelText, JPasswordField field) {
		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Use shared CustomLabel (Poppins + Brand sizes) and a separate red asterisk
		// label
		CustomLabel label = new CustomLabel(labelText, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		JLabel star = new JLabel("*");
		star.setForeground(Color.RED);
		// match the same font as CustomLabel
		star.setFont(label.getFont());
		JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		labelRow.setOpaque(false);
		labelRow.add(label);
		labelRow.add(star);
		panel.add(labelRow, BorderLayout.NORTH);

		field.setBorder(new EmptyBorder(6, 10, 6, 10));
		if (FontLib.POPPINS_REGULAR != null)
			field.setFont(FontLib.POPPINS_REGULAR.deriveFont(Brand.STANDARD_TEXT_SIZE));
		panel.add(field, BorderLayout.CENTER);
		return panel;
	}

	// Rounded password field styled similar to CustomTextField
	private static class RoundedPasswordField extends JPasswordField {
		private static final long serialVersionUID = 1L;
		private int radius = 12;
		private boolean isHovered = false;

		public RoundedPasswordField(int columns) {
			super(columns);
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
			if (FontLib.POPPINS_REGULAR != null)
				setFont(FontLib.POPPINS_REGULAR.deriveFont(Brand.STANDARD_TEXT_SIZE));

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					isHovered = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					isHovered = false;
					repaint();
				}
			});
			addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent e) {
					repaint();
				}

				public void focusLost(java.awt.event.FocusEvent e) {
					repaint();
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
			g2.dispose();
			super.paintComponent(g);
		}

		@Override
		protected void paintBorder(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(hasFocus() || isHovered ? Color.BLACK : Color.LIGHT_GRAY);
			g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
			g2.dispose();
		}
	}

}
