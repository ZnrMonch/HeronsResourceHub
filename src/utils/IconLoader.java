package utils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconLoader {

	public static ImageIcon loadIcon(String path) {
		try {
			BufferedImage img = ImageIO.read(IconLoader.class.getResource(path));
			return new ImageIcon(img);
		} catch (Exception e) {
			System.err.println("Couldn't find file: " + path);
			e.printStackTrace();
			return null;
		}
	}
	
	public static ImageIcon loadAndScaleIcon(String path, int width, int height) {
		try {
			BufferedImage img = ImageIO.read(IconLoader.class.getResource(path));
			Image scaledImg = getHighQualityScaledImage(img, width, height);
			return new ImageIcon(scaledImg);
		} catch (Exception e) {
			System.err.println("Couldn't find file: " + path);
			e.printStackTrace();
			return null;
		}
	}
	
	public static ImageIcon loadAndScaleColorizedIcon(String path, int width, int height, Color color) {
		try {
			BufferedImage img = ImageIO.read(IconLoader.class.getResource(path));
			
			BufferedImage colorizedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = colorizedImg.createGraphics();
			
			g2d.drawImage(img, 0, 0, null);
			
			g2d.setComposite(AlphaComposite.SrcAtop);
			g2d.setColor(color);
			g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
			g2d.dispose();

			Image scaledImg = getHighQualityScaledImage(colorizedImg, width, height);
			return new ImageIcon(scaledImg);
		} catch (Exception e) {
			System.err.println("Couldn't find or colorize file: " + path);
			e.printStackTrace();
			return null;
		}
	}

	public static ImageIcon loadAndScaleCircularIcon(String path, int width, int height) {
		try {
			BufferedImage img = ImageIO.read(IconLoader.class.getResource(path));
			Image scaledImg = getHighQualityScaledImage(img, width, height);

			BufferedImage circleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = circleBuffer.createGraphics();

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.fill(new Ellipse2D.Float(0, 0, width, height));

			g2d.setComposite(AlphaComposite.SrcIn);
			g2d.drawImage(scaledImg, 0, 0, null);
			g2d.dispose();

			return new ImageIcon(circleBuffer);
		} catch (Exception e) {
			System.err.println("Couldn't find or crop file: " + path);
			e.printStackTrace();
			return null;
		}
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