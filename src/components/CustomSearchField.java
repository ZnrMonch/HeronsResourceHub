package components;

import java.awt.*;
import javax.swing.*;
import utils.*;

public class CustomSearchField extends CustomTextField {
    private static final long serialVersionUID = 1L;
    private Image searchIcon;
    private int iconPadding = 10;

    public CustomSearchField() {
        this("Search...", 20, 10);
    }

    public CustomSearchField(int columns) {
        this("Search...", columns, 10);
    }

    public CustomSearchField(int columns, int radius) {
        this("Search...", columns, radius);
    }

    public CustomSearchField(String placeholder) {
        this(placeholder, 20, 10);
    }

    public CustomSearchField(String placeholder, int columns) {
        this(placeholder, columns, 10);
    }

    public CustomSearchField(String placeholder, int columns, int radius) {
        super(placeholder, columns, radius);
        ImageIcon icon = IconLoader.loadAndScaleIcon("/resources/icons/search.png", 16, 16);
        if (icon != null) {
            this.searchIcon = icon.getImage();
        }
        setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (searchIcon != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int y = (getHeight() - searchIcon.getHeight(null)) / 2;
            int x = iconPadding;

            g2.drawImage(searchIcon, x, y, this);
            g2.dispose();
        }
    }
}