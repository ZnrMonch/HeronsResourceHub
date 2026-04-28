package components;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class CustomLink extends CustomLabel {
    private static final long serialVersionUID = 1L;
    
    private Color normalColor = Color.BLUE.darker();
    private Color hoverColor = Color.BLUE;
    private String url;
    private Runnable clickAction;

    public CustomLink(String text, String url) {
        super(text);
        this.url = url;
        setupListener();
    }

    public CustomLink(String text, Runnable clickAction) {
        super(text);
        this.clickAction = clickAction;
        setupListener();
    }

    public void setNormalColor(Color normalColor) {
        this.normalColor = normalColor;
        setForeground(normalColor);
    }
    
    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    private void setupListener() {
        setForeground(normalColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(normalColor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (url != null && !url.isEmpty()) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (clickAction != null) {
                    clickAction.run();
                }
            }
        });
    }
}