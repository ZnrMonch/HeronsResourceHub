package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class Page extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private CustomPanel contentPane = new CustomPanel(Brand.BACKGROUND_COLOR);
	private CustomPanel bodyPanel = new CustomPanel();
	
	public Page(JPanel content) {
		setTitle("");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1100, 900));
		setPreferredSize(new Dimension(1100, 1000));
		
		contentPane.addPadding(20);
		setContentPane(contentPane);
		bodyPanel.setLayout(new BorderLayout(20, 20));
		bodyPanel.add(content, BorderLayout.CENTER);
		
		init();
		setVisible(true);
	}
	
	private void init() {
		contentPane.setLayout(new BorderLayout(20, 20));
		contentPane.add(initHeader(), BorderLayout.NORTH);
		contentPane.add(initSidebar(), BorderLayout.WEST);
		contentPane.add(bodyPanel, BorderLayout.CENTER);
		
		repaint();
		revalidate();
	}
	
	private CustomPanel initHeader() {
		CustomPanel header = new CustomPanel(Color.WHITE);
		header.setPreferredSize(new Dimension(0, 70));
		header.addPadding(10);
		header.setRadius(40);
		header.setLayout(new BorderLayout());
		 
		CustomPanel westWrapper = new CustomPanel();
		westWrapper.setOpaque(false);
		westWrapper.setLayout(new BoxLayout(westWrapper, BoxLayout.X_AXIS));
		westWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		JLabel brandingLogo = new JLabel(IconLoader.loadAndScaleIcon("/resources/images/umak-icon-50.png", 50, 50));
		brandingLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // adding spacing
		westWrapper.add(brandingLogo);
		
		CustomPanel textWrapper = new CustomPanel();
		textWrapper.setOpaque(false);
		textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
		textWrapper.add(Box.createVerticalGlue());
		textWrapper.add(new CustomLabel("ADMIN PANEL", Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD));
		textWrapper.add(new CustomLabel("System Management Dashboard", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR));
		textWrapper.add(Box.createVerticalGlue());
		
		westWrapper.add(textWrapper);
		
		CustomPanel eastWrapper = new CustomPanel();
		eastWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		eastWrapper.setOpaque(false);
		eastWrapper.setLayout(new GridBagLayout());
		JLabel notifBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/notifications.png", 30, 30));
		
		eastWrapper.add(notifBtn);
		
		header.add(westWrapper, BorderLayout.WEST);
		header.add(eastWrapper, BorderLayout.EAST);
		return header;
	}
	
	private CustomPanel initSidebar() {
		CustomPanel sidebar = new CustomPanel(Color.WHITE);
		sidebar.setPreferredSize(new Dimension(70, 0));
		sidebar.setRadius(40);
		sidebar.addPadding(20);
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		
		return sidebar;
	}
}