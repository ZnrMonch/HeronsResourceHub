package components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import utils.*;

public class CustomComboBox<E> extends JComboBox<E> {
    private static final long serialVersionUID = 1L;
    
    private int radius;
    private boolean isHovered = false;
    private Dimension customSize = null;
    
    public CustomComboBox(E[] items) {
        this(items, 10);
    }
    
    public CustomComboBox(E[] items, int radius) {
        super(items);
        init(radius);
    }
    
    public void setRadius(int radius) {
		this.radius = radius;
		repaint();
    }

    public void setFontSize(float size) {
        if (FontLib.POPPINS_REGULAR != null) {
            setFont(FontLib.POPPINS_REGULAR.deriveFont(size));
        } else {
            setFont(getFont().deriveFont(size));
        }
    }

    public void setCustomSize(int width, int height) {
        this.customSize = new Dimension(width, height);
        setPreferredSize(customSize);
        setMinimumSize(customSize);
        setMaximumSize(customSize);
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        if (customSize != null) {
            return customSize;
        }
        return super.getPreferredSize();
    }

    private void init(int radius) {
        this.radius = radius;
        
        setOpaque(false); 
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); 
        setBackground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (FontLib.POPPINS_REGULAR != null) {
            setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
        }
        
        setUI(new CustomComboBoxUI());
        
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
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background 
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        g2.dispose();
        
        super.paintComponent(g); 
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Determine border color
        g2.setColor(hasFocus() || isHovered ? Color.BLACK : Color.LIGHT_GRAY);
        
        // Draw the border stroke 
        g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        
        g2.dispose();
    }
    
    private class CustomComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            CustomButton button = new CustomButton(IconLoader.loadAndScaleIcon("/resources/icons/arrow-dropdown.png", 12, 12));
            button.setDefaultColor(new Color(0, 0, 0, 0));
            button.setHoverColor(new Color(0, 0, 0, 0));
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }
        
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Do not paint default background to allow CustomComboBox to handle rounded background
        }
    }
}