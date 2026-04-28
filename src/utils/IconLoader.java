package utils;

import java.awt.Image;
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
			Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			return new ImageIcon(scaledImg);
		} catch (Exception e) {
			System.err.println("Couldn't find file: " + path);
			e.printStackTrace();
			return null;
		}
	}
}