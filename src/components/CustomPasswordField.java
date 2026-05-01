package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import utils.*;

public class CustomPasswordField extends JPasswordField {
    private static final long serialVersionUID = 1L;
    
    private int radius;
    private boolean isHovered = false;
    private String placeholder = "";
    private Dimension customSize = null;
    private boolean isPasswordVisible = false;
    private char defaultEchoChar;

    public CustomPasswordField() {
        this("", 20, 10);
    }
    
    public CustomPasswordField(int columns) {
        this("", columns, 10);
    }
    
    public CustomPasswordField(int columns, int radius) {
        this("", columns, radius);
    }
    
    public CustomPasswordField(String placeholder) {
        this(placeholder, 20, 10);
    }
    
    public CustomPasswordField(String placeholder, int columns) {
        this(placeholder, columns, 10);
    }

    public CustomPasswordField(String placeholder, int columns, int radius) {
        super(columns);
        this.placeholder = placeholder != null ? placeholder : "";
        this.radius = radius;
        this.defaultEchoChar = '*';
        setEchoChar(this.defaultEchoChar);
        
        setOpaque(false); 
        // Give padding on the right for the eye icon
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 30)); 
        
        // Apply Poppins font
        if (FontLib.POPPINS_REGULAR != null) {
            setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
        }
        
        // Listen for hover
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
        
        // Listen for active/focus state
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
        
        // Listen for clicks on the eye/toggle icon
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getX() >= getWidth() - getInsets().right) {
                    isPasswordVisible = !isPasswordVisible;
                    setEchoChar(isPasswordVisible ? (char) 0 : defaultEchoChar);
                    repaint();
                }
            }
        });
        
        // Listen for pointer changing when hovering over the icon
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() >= getWidth() - getInsets().right) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.TEXT_CURSOR));
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background (slightly inset to prevent clipping)
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        g2.dispose();
        
        // Draw actual text dots and caret
        super.paintComponent(g); 
        
        // Draw placeholder text if field is empty
        if (getPassword().length == 0 && !placeholder.isEmpty()) {
            Graphics2D g2p = (Graphics2D) g.create();
            g2p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2p.setColor(Color.GRAY);
            g2p.setFont(getFont());
            
            FontMetrics fm = g2p.getFontMetrics();
            int x = getInsets().left; 
            // Vertically center the placeholder
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            g2p.drawString(placeholder, x, y);
            g2p.dispose();
        }
        
        // Draw show/hide eye icon manually
        Graphics2D gIcon = (Graphics2D) g.create();
        gIcon.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gIcon.setColor(Color.GRAY);
        
        int iconX = getWidth() - 25;
        int iconY = getHeight() / 2;
        
        // Create a simple custom eye shape using basic shapes
        if (isPasswordVisible) {
            // Draw a crossed-out eye
            gIcon.drawArc(iconX - 7, iconY - 4, 14, 8, 0, 360);
            gIcon.fillOval(iconX - 2, iconY - 2, 4, 4); 
            gIcon.drawLine(iconX - 8, iconY - 5, iconX + 8, iconY + 5); // Slash
        } else {
            // Draw a standard eye
            gIcon.drawArc(iconX - 7, iconY - 4, 14, 8, 0, 360);
            gIcon.fillOval(iconX - 2, iconY - 2, 4, 4);
        }
        
        gIcon.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Determine border color: Use Brand Primary if focused (active) OR hovered
        g2.setColor(hasFocus() || isHovered ? Color.BLACK : Color.LIGHT_GRAY);
        
        // Draw the border stroke (slightly inset to prevent clipping)
        g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        
        g2.dispose();
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
        FontMetrics fm = getFontMetrics(getFont());
        int height = fm.getHeight() + getInsets().top + getInsets().bottom + 4;
        int width = super.getPreferredSize().width;
        return new Dimension(width, height);
    }
}