package admin;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import components.*;
import utils.*;

public class AdminDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public AdminDialog(Window owner, String titleText, JPanel contentPanel, String submitBtnText, ActionListener onSubmit) {
		super(owner, titleText, ModalityType.APPLICATION_MODAL);
		setSize(450, 400);
		setLocationRelativeTo(owner);
		setLayout(new BorderLayout());

		CustomPanel mainPanel = new CustomPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setPadding(20);

		// Header
		CustomLabel title = new CustomLabel(titleText, 18f, FontStyle.BOLD);
		CustomPanel headerPanel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		headerPanel.add(title);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// Content body provided by caller
		if (contentPanel != null) {
			mainPanel.add(contentPanel, BorderLayout.CENTER);
		}

		add(mainPanel, BorderLayout.CENTER);

		// Footer Buttons
		CustomPanel bottomPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		bottomPanel.setPadding(10, 20); // Fallback to 2-arg padding since 4-arg does not exist

		CustomButton cancelBtn = new CustomButton("Cancel", 8);
		cancelBtn.setDefaultColor(Color.GRAY);
		cancelBtn.setTextColor(Color.WHITE);
		cancelBtn.setPadding(5, 15, 5, 15);
		cancelBtn.addActionListener(e -> dispose());

		CustomButton submitBtn = new CustomButton(submitBtnText != null ? submitBtnText : "Submit", 8);
		submitBtn.setDefaultColor(Brand.PRIMARY_COLOR);
		submitBtn.setTextColor(Color.WHITE);
		submitBtn.setPadding(5, 15, 5, 15);
		submitBtn.addActionListener(e -> {
			if (onSubmit != null) {
				onSubmit.actionPerformed(e);
			}
			dispose();
		});

		bottomPanel.add(cancelBtn);
		bottomPanel.add(submitBtn);
		
		add(bottomPanel, BorderLayout.SOUTH);
	}
}