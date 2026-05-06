package components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import utils.*;

public class CustomSpinner extends JSpinner {
    private static final long serialVersionUID = 1L;
    
    private int radius;
    private boolean isHovered = false;
    private Dimension customSize = null;

    public CustomSpinner() {
        this(new SpinnerNumberModel(), 10);
    }
    
    public CustomSpinner(SpinnerModel model) {
        this(model, 10);
    }
    
    public CustomSpinner(SpinnerModel model, int radius) {
        super(model);
        this.radius = radius;
        init();
    }
    
    private void init() {
        setOpaque(false); 
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8)); 
        setBackground(Color.WHITE);
        
        // Hide standard background from DefaultEditor panel wrapper
        JComponent editor = getEditor();
        if (editor instanceof DefaultEditor) {
            editor.setOpaque(false); // << Added to fix gray DefaultEditor wrapper container
            JFormattedTextField textField = ((DefaultEditor) editor).getTextField();
            textField.setOpaque(false);
            textField.setBackground(new Color(0,0,0,0));
            textField.setBorder(BorderFactory.createEmptyBorder());
            
            if (FontLib.POPPINS_REGULAR != null) {
                textField.setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
            }
            
            // Add listeners to the actual textfield to trigger container repaint
            textField.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }
                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
        
        // Remove standard borders from child UI (spinner arrows)
        setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                Component c = super.createNextButton();
                if (c instanceof JButton) {
                    ((JButton) c).setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    ((JButton) c).setContentAreaFilled(false);
                    ((JButton) c).setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                return c;
            }
            @Override
            protected Component createPreviousButton() {
                Component c = super.createPreviousButton();
                if (c instanceof JButton) {
                    ((JButton) c).setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    ((JButton) c).setContentAreaFilled(false);
                    ((JButton) c).setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                return c;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        g2.dispose();
        
        super.paintComponent(g); 
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Determine border color: Black if focused or hovered, else Light Gray
        boolean isFocused = false;
        if (getEditor() instanceof DefaultEditor) {
            isFocused = ((DefaultEditor) getEditor()).getTextField().hasFocus();
        }
        
        g2.setColor(isFocused || isHovered ? Color.BLACK : Color.LIGHT_GRAY);
        g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
        
        g2.dispose();
    }
    
    public void setFontSize(float size) {
        JComponent editor = getEditor();
        if (editor instanceof DefaultEditor) {
            JFormattedTextField textField = ((DefaultEditor) editor).getTextField();
            if (FontLib.POPPINS_REGULAR != null) {
                textField.setFont(FontLib.POPPINS_REGULAR.deriveFont(size));
            } else {
                textField.setFont(textField.getFont().deriveFont(size));
            }
        }
    }

    public void setCustomSize(int width, int height) {
        this.customSize = new Dimension(width, height);
        setPreferredSize(customSize);
        setMinimumSize(customSize);
        setMaximumSize(customSize);
        revalidate();
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (customSize != null) {
            return customSize;
        }
        return super.getPreferredSize();
    }
}