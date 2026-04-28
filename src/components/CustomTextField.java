package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import utils.*;

public class CustomTextField extends JTextField {
    private static final long serialVersionUID = 1L;
    
    private int radius;
    private boolean isHovered = false;
    private String placeholder = "";
    private Dimension customSize = null;

    public CustomTextField() {
        this("", 20, 10);
    }
    
    public CustomTextField(int columns) {
        this("", columns, 10);
    }
    
    public CustomTextField(int columns, int radius) {
        this("", columns, radius);
    }
    
    // Constructors with placeholder
    public CustomTextField(String placeholder) {
        this(placeholder, 20, 10);
    }
    
    public CustomTextField(String placeholder, int columns) {
        this(placeholder, columns, 10);
    }

    // Master Constructor
    public CustomTextField(String placeholder, int columns, int radius) {
        super(columns);
        this.placeholder = placeholder != null ? placeholder : "";
        this.radius = radius;
        
        setOpaque(false); 
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); 
        
        // Apply Poppins font to the text field
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background (slightly inset to prevent clipping)
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        g2.dispose();
        
        // Draw actual text and caret
        super.paintComponent(g); 
        
        // Draw placeholder text if field is empty
        if (getText().isEmpty() && !placeholder.isEmpty()) {
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