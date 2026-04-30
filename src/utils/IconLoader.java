package utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconLoader {

	/**
	 * Loads an icon from the resources folder.
	 * @param path The path to the image resource (e.g., "resources/images/icon.png")
	 * @return ImageIcon if found, null otherwise
	 */
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
	
	/**
	 * Loads an icon and scales it to the specified width and height.
	 * @param path The path to the image resource
	 * @param width The target width
	 * @param height The target height
	 * @return Scaled ImageIcon if found, null otherwise
	 */
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
            
            // Create a new image with transparency support
            BufferedImage colorizedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = colorizedImg.createGraphics();
            
            // Draw the original image first
            g2d.drawImage(img, 0, 0, null);
            
            // Set the composite to SrcAtop (replaces existing pixels with the new color, ignores transparent areas)
            g2d.setComposite(AlphaComposite.SrcAtop);
            g2d.setColor(color);
            g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
            g2d.dispose();

            // Scale the newly colorized image
            Image scaledImg = getHighQualityScaledImage(colorizedImg, width, height);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            System.err.println("Couldn't find or colorize file: " + path);
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