package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class Page extends JFrame {
	private static final long serialVersionUID = 1L;
	private JLayeredPane layeredPane = new JLayeredPane();
	private CustomPanel contentPane = new CustomPanel(Brand.BACKGROUND_COLOR);
	private CustomPanel menuPane;
	
	public Page(JPanel content) {
		setTitle("");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1100, 900));
		setPreferredSize(new Dimension(1100, 1000));
		
		layeredPane.setLayout(new OverlayLayout(layeredPane));
		
		contentPane.addPadding(20);
		
		layeredPane.setLayer(contentPane, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(contentPane);
		
		menuPane = initMenu();
		menuPane.setVisible(false);
		layeredPane.setLayer(menuPane, JLayeredPane.PALETTE_LAYER);
		layeredPane.add(menuPane);
		
		setContentPane(layeredPane);
		
		init();
		setVisible(true);
	}
	
	private void init() {
		contentPane.setLayout(new BorderLayout());
		contentPane.add(initHeader(), BorderLayout.NORTH);
		
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
		eastWrapper.setOpaque(false);
		eastWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JLabel menuBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/round-menu.png", 40, 40));
		menuBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		menuBtn.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				menuPane.setVisible(true);
			}
		});
		eastWrapper.add(menuBtn);
		
		header.add(westWrapper, BorderLayout.WEST);
		header.add(eastWrapper, BorderLayout.EAST);
		return header;
	}
	
	private CustomPanel initMenu() {
		CustomPanel menuWrapper = new CustomPanel();
		menuWrapper.setBorder(BorderFactory.createEmptyBorder(110, 0, 20, 20));
		menuWrapper.setOpaque(false);
		menuWrapper.setLayout(new BorderLayout());
		
		CustomPanel sideMenu = new CustomPanel(Color.WHITE);
		sideMenu.setRadius(40);
		sideMenu.setPreferredSize(new Dimension(150, 0));
		sideMenu.setLayout(new BorderLayout());
		
		CustomPanel topPanel = new CustomPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
		JLabel closeBtn = new JLabel(IconLoader.loadAndScaleIcon("/resources/icons/round-close.png", 35, 35));
		closeBtn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 20));
		closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				menuWrapper.setVisible(false);
			}
		});
		CustomLabel menuTitle = new CustomLabel("MENU NAVIGATION", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
		menuTitle.setBorder(BorderFactory.createEmptyBorder(3, 20, 0, 0));
		topPanel.add(menuTitle, BorderLayout.CENTER); // Spacer
		topPanel.add(closeBtn, BorderLayout.EAST);
		
		sideMenu.add(topPanel, BorderLayout.NORTH);
		
		
		menuWrapper.add(sideMenu, BorderLayout.EAST);
		
		return menuWrapper;
	}
	
	private CustomPanel initNav() {
		CustomPanel nav = new CustomPanel();
		nav.setPreferredSize(new Dimension(0, 100));
		nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
		
		return nav;
	}
}