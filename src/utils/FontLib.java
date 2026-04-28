package utils;

import java.awt.*;

public class FontLib {
	public static Font POPPINS_REGULAR;
	public static Font POPPINS_BOLD;
	public static Font POPPINS_ITALIC;
	
	public static void loadFonts() {
		try {			
			POPPINS_REGULAR = Font.createFont(Font.TRUETYPE_FONT, FontLib.class.getResourceAsStream("/resources/fonts/Poppins-Regular.ttf"));
			POPPINS_BOLD = Font.createFont(Font.TRUETYPE_FONT, FontLib.class.getResourceAsStream("/resources/fonts/Poppins-Bold.ttf"));
			POPPINS_ITALIC = Font.createFont(Font.TRUETYPE_FONT, FontLib.class.getResourceAsStream("/resources/fonts/Poppins-Italic.ttf"));
		} catch (Exception e) {
			e.printStackTrace();
			
			POPPINS_REGULAR = new Font("SansSerif", Font.PLAIN, 14);
			POPPINS_BOLD = new Font("SansSerif", Font.BOLD, 14);
			POPPINS_ITALIC = new Font("SansSerif", Font.ITALIC, 14);
		}
	}
}
