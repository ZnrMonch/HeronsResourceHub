package components;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import utils.*;

public class CustomTabbedPane extends CustomPanel {
    private static final long serialVersionUID = 1L;
    
    private CustomPanel headerPanel;
    private CustomPanel contentPanel;
    private CardLayout cardLayout;
    private List<TabItem> tabs;
    
    // Set your brand colors here
    private Color activeColor = Brand.PRIMARY_COLOR;
    private Color inactiveColor = Color.BLACK;

    public CustomTabbedPane() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        tabs = new ArrayList<>();

        // Header holds the tab buttons side-by-side
        headerPanel = new CustomPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        
        // Content holds the actual pages
        cardLayout = new CardLayout();
        contentPanel = new CustomPanel();
        contentPanel.setLayout(cardLayout);
        contentPanel.setOpaque(false); 

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    public void setActiveColor(Color activeColor) {
		this.activeColor = activeColor;
		for (TabItem tab : tabs) {
			tab.setActive(tab.label.getForeground().equals(activeColor));
		}
		repaint();
	}
    
    @Override
    public void setRadius(int radius) {
        super.setRadius(radius);
        repaint();
    }

    /**
     * Adds a new tab to the pane.
     * @param title The text for the tab
     * @param iconPath The resource path to the image icon
     * @param activeIconColor The color to shade the icon when active
     * @param contentPane The panel to display when selected
     */
    public void addTab(String title, String iconPath, JPanel contentPane) {
        ImageIcon defaultIcon = IconLoader.loadAndScaleIcon(iconPath, 20, 20);
        ImageIcon activeIcon = IconLoader.loadAndScaleColorizedIcon(iconPath, 20, 20, activeColor);
        
        String cardName = "TAB_" + tabs.size();
        contentPane.setOpaque(false); 
        contentPanel.add(contentPane, cardName);

        TabItem tab = new TabItem(title, defaultIcon, activeIcon, cardName);
        tabs.add(tab);
        headerPanel.add(tab);

        if (tabs.size() == 1) {
            selectTab(tab);
        }
    }

    private void selectTab(TabItem selectedTab) {
        for (TabItem tab : tabs) {
            tab.setActive(tab == selectedTab);
        }
        cardLayout.show(contentPanel, selectedTab.cardName);
    }

    private class TabItem extends CustomPanel {
        private static final long serialVersionUID = 1L;
        private CustomLabel label;
        private ImageIcon defaultIcon;
        private ImageIcon activeIcon;
        private String cardName;

        public TabItem(String title, ImageIcon defaultIcon, ImageIcon activeIcon, String cardName) {
            this.defaultIcon = defaultIcon;
            this.activeIcon = activeIcon;
            this.cardName = cardName;

            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            label = new CustomLabel(title);
            label.setIcon(defaultIcon);
            label.setFontSize(14f);
            label.setFontStyle(utils.FontStyle.BOLD);
            label.setForeground(inactiveColor);
            label.setIconTextGap(8);

            add(label, BorderLayout.CENTER);
            setActive(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectTab(TabItem.this);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(240, 240, 240));
                    setRadius(15);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                	setBackground(new Color(0, 0, 0, 0)); // Transparent
                    repaint();
                }
            });
        }

        public void setActive(boolean isActive) {
            label.setForeground(isActive ? activeColor : inactiveColor);
            label.setIcon(isActive ? activeIcon : defaultIcon);
            
            if (isActive) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, activeColor),
                    BorderFactory.createEmptyBorder(10, 20, 7, 20) 
                ));
            } else {
                setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            }
            
            revalidate();
            repaint();
        }
    }
}