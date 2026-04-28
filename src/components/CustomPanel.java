package components;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CustomPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Image backgroundImage;
    private int cornerRadius;
    private Color borderColor;
    private int borderWidth;
    private boolean hasShadow;
    private int shadowSize = 10;

    public CustomPanel() {
    	this(0, null, null, 0, false);
    }
    
    public CustomPanel(LayoutManager layout) {
		this(0, null, null, 0, false);
		setLayout(layout);
	}
    
    // 2. BACKGROUND IMAGE
    public CustomPanel(String resourcePath) {
        this(0, null, null, 0, false);
        try {
            java.io.InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) this.backgroundImage = ImageIO.read(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public CustomPanel(Color panelColor) {
    	this(0, panelColor, null, 0, false);
    }
    
    public CustomPanel(Color panelColor, Color borderColor) {
    	this(0, panelColor, borderColor, 0, false);
    }
    
    public CustomPanel(Color panelColor, Color borderColor, int borderWidth) {
    	this(0, panelColor, borderColor, borderWidth, false);
    }

    public CustomPanel(int radius, Color panelColor, Color borderColor, int borderWidth, boolean hasShadow) {
        this.cornerRadius = radius;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.hasShadow = hasShadow;
        
        setBackground(panelColor != null ? panelColor : new Color(0,0,0,0));
        setOpaque(false);
        
        if (hasShadow) {
            setBorder(BorderFactory.createEmptyBorder(0, 0, shadowSize, shadowSize));
        }
    }
    
    public void setRadius(int radius) {
    	this.cornerRadius = radius;
    	repaint();
    }

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - (hasShadow ? shadowSize : 1);
        int height = getHeight() - (hasShadow ? shadowSize : 1);

        // 1. DRAW SHADOW (If enabled)
        if (hasShadow) {
            for (int i = 0; i < shadowSize; i++) {
                int alpha = Math.max(0, 30 - (i * 5));
                g2.setColor(new Color(0, 0, 0, alpha)); // Fade effect
                g2.fillRoundRect(i, i, width, height, cornerRadius, cornerRadius);
            }
        }

        // Define the shape for background/clip
        Shape roundShape = new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius);

        // 2. DRAW BACKGROUND
        if (backgroundImage != null) {
            g2.setClip(roundShape);
            g2.drawImage(backgroundImage, 0, 0, width, height, this);
            g2.setClip(null);
        } else {
            g2.setColor(getBackground());
            g2.fill(roundShape);
        }

        // 3. DRAW BORDER
        if (borderWidth > 0 && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            float offset = borderWidth / 2f;
            g2.draw(new RoundRectangle2D.Float(offset, offset, width - borderWidth, height - borderWidth, cornerRadius, cornerRadius));
        }
    }
    
    public void addPadding(int padding) {
		setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		revalidate();
	    repaint();
    }
    
    @Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		return new Dimension(500, size.height); // Fixed width to 500, height is now automatic
	}
}