package pages;

import java.awt.*;
import utils.*;

import javax.swing.*;

import components.*;

public class AdminPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public AdminPanel() {
		setOpaque(false);
		setLayout(new BorderLayout(10, 10));
		
		CustomPanel contentWrapper = new CustomPanel(new BorderLayout(20, 20));
		
		CustomPanel statusWrapper = new CustomPanel(new GridLayout(1, 0, 20, 0));
		statusWrapper.setPreferredSize(new Dimension(0, 150));
		statusWrapper.add(generateStatusCard("/resources/icons/user-group.png", "Users", "Total Users", 100));
		statusWrapper.add(generateStatusCard("/resources/icons/user-group.png", "Users", "Total Users", 100));
		statusWrapper.add(generateStatusCard("/resources/icons/user-group.png", "Users", "Total Users", 100));
		statusWrapper.add(generateStatusCard("/resources/icons/user-group.png", "Users", "Total Users", 100));
		
		contentWrapper.add(statusWrapper, BorderLayout.NORTH);
		contentWrapper.add(initTableManagement(), BorderLayout.CENTER);
		
		add(contentWrapper, BorderLayout.CENTER);
	}
	
	public CustomPanel generateStatusCard(String icon, String title, String keyLabel, int value) {
		CustomPanel statusWrapper = new CustomPanel(Color.WHITE);
		statusWrapper.setRadius(20);
		statusWrapper.addPadding(20);
		statusWrapper.setLayout(new GridLayout(2, 2, 10, 10));
		
		// STATUS ICON
		CustomPanel iconWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
		iconWrapper.add(new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/user-group.png", 50, 50)));
		
		// STATUS LABEL
		CustomPanel statusLabelWrapper = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		CustomPanel wrapper = new CustomPanel(new Color(230, 245, 230)); 
		wrapper.setRadius(10);
		wrapper.addPadding(5);
		wrapper.setLayout(new GridBagLayout());
		CustomLabel statusLabel = new CustomLabel("Users", 16f, FontStyle.BOLD);
		statusLabel.setForeground(new Color(34, 139, 34));
		wrapper.add(statusLabel);
		statusLabelWrapper.add(wrapper);
		
		// KEY LABEL
		CustomPanel totalWrapper = new CustomPanel();
		totalWrapper.setLayout(new BoxLayout(totalWrapper, BoxLayout.Y_AXIS));
		totalWrapper.add(Box.createVerticalGlue());
		CustomLabel totalLabel = new CustomLabel("Total Users", 16f, FontStyle.REGULAR);
		totalLabel.setForeground(Color.GRAY);
		totalWrapper.add(totalLabel);
		
		// VALUE LABEL
		CustomPanel numberWrapper = new CustomPanel();
		numberWrapper.setOpaque(false);
		numberWrapper.setLayout(new BoxLayout(numberWrapper, BoxLayout.Y_AXIS));
		numberWrapper.add(Box.createVerticalGlue());
		CustomLabel numberLabel = new CustomLabel("100", 28f, FontStyle.BOLD);
		numberLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		numberWrapper.add(numberLabel);

		statusWrapper.add(iconWrapper);
		statusWrapper.add(statusLabelWrapper);
		statusWrapper.add(totalWrapper);
		statusWrapper.add(numberWrapper);
		
		return statusWrapper;
	}
	
	public CustomPanel initTableManagement() {
		CustomPanel tableManagementWrapper = new CustomPanel(new BorderLayout());
		
		CustomTabbedPane tabbedPane = new CustomTabbedPane();
		tabbedPane.setRadius(20);
		
		tabbedPane.addTab("USERS", "/resources/icons/home.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("ITEMS", "/resources/icons/items.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("USER LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("TRANSACTION LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("REPUTATION LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));

		tableManagementWrapper.add(tabbedPane, BorderLayout.CENTER);
		
		return tableManagementWrapper;
	}
}