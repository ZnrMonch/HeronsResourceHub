package admin;

import java.awt.*;
import utils.*;

import javax.swing.*;

import components.*;

public class AdminPanel extends CustomPanel {
	private static final long serialVersionUID = 1L;

	public AdminPanel() {
		setLayout(new BorderLayout(10, 10));
		
		CustomPanel contentWrapper = new CustomPanel(new BorderLayout(20, 20));
		
		CustomPanel statusWrapper = new CustomPanel(new GridLayout(1, 0, 20, 0));
		statusWrapper.setPreferredSize(new Dimension(0, 170));
		statusWrapper.add(generateStatusCard("/resources/icons/user-group.png", "Users", "Total Users", 100, Color.decode("#33277d")));
		statusWrapper.add(generateStatusCard("/resources/icons/items.png", "Items", "Total Items", 100, Color.decode("#f9cb0f")));
		statusWrapper.add(generateStatusCard("/resources/icons/request.png", "Logs", "Total Transactions", 100, Color.decode("#37ad00")));
		statusWrapper.add(generateStatusCard("/resources/icons/logs.png", "Logs", "Total Logs", 100, Color.decode("#37ad00")));
		
		contentWrapper.add(statusWrapper, BorderLayout.NORTH);
		contentWrapper.add(initTableManagement(), BorderLayout.CENTER);
		
		add(contentWrapper, BorderLayout.CENTER);
	}
	
	public CustomPanel generateStatusCard(String icon, String title, String keyLabel, int value, Color bgColor) {
		CustomPanel statusWrapper = new CustomPanel(Color.WHITE);
		statusWrapper.setRadius(20);
		statusWrapper.setPadding(20);
		statusWrapper.setLayout(new GridLayout(2, 2, 10, 10));
		
		// STATUS ICON
		CustomPanel iconWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
		iconWrapper.add(new JLabel(IconLoader.loadAndScaleIcon(icon, 60, 60)));
		
		// STATUS LABEL
		CustomPanel statusLabelWrapper = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		CustomPanel wrapper = new CustomPanel(bgColor); 
		wrapper.setRadius(10);
		wrapper.setPadding(15, 5);
		wrapper.setLayout(new GridBagLayout());
		CustomLabel statusLabel = new CustomLabel(title, 16f, FontStyle.BOLD);
		statusLabel.setForeground(Color.WHITE);
		wrapper.add(statusLabel);
		statusLabelWrapper.add(wrapper);
		
		// KEY LABEL
		CustomPanel totalWrapper = new CustomPanel();
		totalWrapper.setLayout(new BoxLayout(totalWrapper, BoxLayout.Y_AXIS));
		totalWrapper.add(Box.createVerticalGlue());
		CustomLabel totalLabel = new CustomLabel(keyLabel, 16f, FontStyle.REGULAR);
		totalLabel.setForeground(Color.GRAY);
		totalWrapper.add(totalLabel);
		
		// VALUE LABEL
		CustomPanel numberWrapper = new CustomPanel();
		numberWrapper.setOpaque(false);
		numberWrapper.setLayout(new BoxLayout(numberWrapper, BoxLayout.Y_AXIS));
		numberWrapper.add(Box.createVerticalGlue());
		CustomLabel numberLabel = new CustomLabel(String.valueOf(value), 28f, FontStyle.BOLD);
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
		
		tabbedPane.addTab("USERS", "/resources/icons/home.png", new AdminTable());
		tabbedPane.addTab("ITEMS", "/resources/icons/items.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("USER LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("ITEM LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("TRANSACTION LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));
		tabbedPane.addTab("REPUTATION LOGS", "/resources/icons/logs.png", new CustomPanel(Color.WHITE));

		tableManagementWrapper.add(tabbedPane, BorderLayout.CENTER);
		
		return tableManagementWrapper;
	}
}