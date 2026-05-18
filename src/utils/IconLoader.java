package utils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconLoader {

	private static BufferedImage loadBufferedImage(String path) {
		URL url = IconLoader.class.getResource(path);
		if (url == null) {
			System.err.println("Resource not found: " + path);
			return null;
		}
		try {
			return ImageIO.read(url);
		} catch (Exception e) {
			System.err.println("Error reading file: " + path);
			e.printStackTrace();
			return null;
		}
	}

	public static ImageIcon loadIcon(String path) {
		BufferedImage img = loadBufferedImage(path);
		return (img != null) ? new ImageIcon(img) : null;
	}
	
	public static ImageIcon loadAndScaleIcon(String path, int width, int height) {
		BufferedImage img = loadBufferedImage(path);
		if (img == null) return null;
		Image scaledImg = getHighQualityScaledImage(img, width, height);
		return new ImageIcon(scaledImg);
	}
	
	public static ImageIcon loadAndScaleColorizedIcon(String path, int width, int height, Color color) {
		BufferedImage img = loadBufferedImage(path);
		if (img == null) return null;
		
		BufferedImage colorizedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = colorizedImg.createGraphics();
		
		g2d.drawImage(img, 0, 0, null);
		
		g2d.setComposite(AlphaComposite.SrcAtop);
		g2d.setColor(color);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
		g2d.dispose();

		Image scaledImg = getHighQualityScaledImage(colorizedImg, width, height);
		return new ImageIcon(scaledImg);
	}

	public static ImageIcon loadAndScaleCircularIcon(String path, int width, int height) {
		BufferedImage img = loadBufferedImage(path);
		if (img == null) return null;

		Image scaledImg = getHighQualityScaledImage(img, width, height);

		BufferedImage circleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = circleBuffer.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.fill(new Ellipse2D.Float(0, 0, width, height));

		g2d.setComposite(AlphaComposite.SrcIn);
		g2d.drawImage(scaledImg, 0, 0, null);
		g2d.dispose();

		return new ImageIcon(circleBuffer);
	}
	
	private static Image getHighQualityScaledImage(BufferedImage src, int width, int height) {
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = scaled.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(src, 0, 0, width, height, null);
		g2d.dispose();
		return scaled;
	}
}