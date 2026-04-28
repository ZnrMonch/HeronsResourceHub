package components;

import javax.swing.*;
import utils.*;
import java.awt.*;

public class CustomLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    private float fontSize;
    private FontStyle fontStyle;

    public CustomLabel(String text) {
    	this(text, Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
        setForeground(Color.BLACK);
        updateFont();
    }
    
    public CustomLabel(String text, float fontSize) {
        this(text, fontSize, FontStyle.REGULAR);
        setForeground(Color.BLACK);
        updateFont();
    }
    
    public CustomLabel(String text, float fontSize, FontStyle fontStyle) {
        super(text);
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        setForeground(Color.BLACK);
        updateFont();
    }
    
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        updateFont();
    }
    
    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
        updateFont();
    }

    public void updateFont() {
        if (FontLib.POPPINS_REGULAR == null) return;
        
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
    
    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth(getText());
        return new Dimension(width, (int)fontSize + 3);
    }
    
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}