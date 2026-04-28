package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import utils.*;

public class CustomButton extends JButton {
    private static final long serialVersionUID = 1L;
    
    private int radius;
    private Color defaultColor = Brand.PRIMARY_COLOR;
    private Color hoverColor = Brand.PRIMARY_COLOR.darker();
    private Color textColor = Color.WHITE;
    private float fontSize = Brand.STANDARD_TEXT_SIZE;
    private FontStyle fontStyle = FontStyle.BOLD;
    private boolean isHovered = false;

    public CustomButton(String text) {
        this(text, 0);
    }
    
    public CustomButton(String text, int radius) {
        super(text);
        this.radius = radius;
        init();
    }
    
    public void setRadius(int radius) {
		this.radius = radius;
		repaint();
	}
    
    public void setDefaultColor(Color color) {
		this.defaultColor = color;
		repaint();
	}
    
    public void setHoverColor(Color color) {
    	this.hoverColor = color;
    	repaint();
    }
    
    public void setTextColor(Color color) {
		this.textColor = color;
		setForeground(color);
		repaint();
	}
    
    public void setFontSize(float fontSize) {
    	this.fontSize = fontSize;
        updateFont();
    	repaint();
    }
    
    public void setFontStyle(FontStyle fontStyle) {
		this.fontStyle = fontStyle;
        updateFont();
		repaint();
	}
    
    private void updateFont() {
        if (FontLib.POPPINS_REGULAR != null) {
            switch (fontStyle) {
                case BOLD:
                    setFont(FontLib.POPPINS_BOLD.deriveFont(fontSize));
                    break;
                case ITALIC:
                    setFont(FontLib.POPPINS_ITALIC.deriveFont(fontSize));
                    break;
                case REGULAR:
                default:
                    setFont(FontLib.POPPINS_REGULAR.deriveFont(fontSize));
                    break;
            }
        }
    }

    private void init() {
    	setForeground(textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        updateFont();
        
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (!isEnabled()) {
            g2.setColor(Color.LIGHT_GRAY);
        } else {
            g2.setColor(isHovered ? hoverColor : defaultColor);
        }
        
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        g2.dispose();
        
        super.paintComponent(g);
    }
}