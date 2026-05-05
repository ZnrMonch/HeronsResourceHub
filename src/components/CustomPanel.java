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
    private int shadowSize = 10;

    public CustomPanel() {
    	this(0, null, null, 0);
    }
    
    public CustomPanel(LayoutManager layout) {
		this(0, null, null, 0);
		setLayout(layout);
	}
    
    public CustomPanel(String resourcePath) {
        this(0, null, null, 0);
        try {
            java.io.InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) this.backgroundImage = ImageIO.read(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public CustomPanel(Color panelColor) {
    	this(0, panelColor, null, 0);
    }
    
    public CustomPanel(Color panelColor, Color borderColor) {
    	this(0, panelColor, borderColor, 0);
    }
    
    public CustomPanel(Color panelColor, Color borderColor, int borderWidth) {
    	this(0, panelColor, borderColor, borderWidth);
    }

    public CustomPanel(int radius, Color panelColor, Color borderColor, int borderWidth) {
        this.cornerRadius = radius;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        
        setBackground(panelColor != null ? panelColor : new Color(0,0,0,0));
        setOpaque(false);
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
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        Shape roundShape = new RoundRectangle2D.Float(0, 0, getWidth(), height, cornerRadius, cornerRadius);

        if (backgroundImage != null) {
            g2.setClip(roundShape);
            g2.drawImage(backgroundImage, 0, 0, width, height, this);
            g2.setClip(null);
        } else {
            g2.setColor(getBackground());
            g2.fill(roundShape);
        }

        if (borderWidth > 0 && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            float offset = borderWidth / 2f;
            g2.draw(new RoundRectangle2D.Float(offset, offset, width - borderWidth, height - borderWidth, cornerRadius, cornerRadius));
        }
    }
    
    public void setPadding(int padding) {
		setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		revalidate();
	    repaint();
    }
    
    public void setPadding(int paddingX, int paddingY) {
		setBorder(BorderFactory.createEmptyBorder(paddingY, paddingX, paddingY, paddingX));
		revalidate();
	    repaint();
    }

    public void setBorder(int width, Color color) {
		this.borderWidth = width;
		this.borderColor = color;
		revalidate();
		repaint();
    }
    
    public void setBorder(int width) {
		this.borderWidth = width;
		this.borderColor = Color.BLACK;
		revalidate();
		repaint();
    }
}