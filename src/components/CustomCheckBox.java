package components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

public class CustomCheckBox extends JComponent
        implements TableCellRenderer, TableCellEditor {

    private static final long serialVersionUID = 1L;

    private static final int   SIZE       = 16;
    private static final int   ARC        = 4;
    private static final Color BORDER_CLR = new Color(0xBBBBBB);
    private static final Color CHECK_CLR  = new Color(0x0056B3);
    private static final Color HOVER_CLR  = new Color(0xDDEAFF);
    private static final Color BG_NORMAL  = Color.WHITE;

    private boolean checked = false;
    private boolean hovered = false;
    private boolean isHeader = false;

    // Header background to match table header
    private static final Color HEADER_BG = Color.LIGHT_GRAY;

    private final java.util.List<javax.swing.event.CellEditorListener> listeners
            = new java.util.ArrayList<>();

    public CustomCheckBox() {
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            // Single click: toggle immediately on press, not release
            public void mousePressed(MouseEvent e) {
                checked = !checked;
                repaint();
                fireEditingStopped();
            }
        });
    }

    public void setChecked(boolean b) {
        this.checked = b;
        repaint();
    }

    public void setHeader(boolean b) {
        this.isHeader = b;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background to match context
        g2.setColor(isHeader ? HEADER_BG : BG_NORMAL);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int x = (getWidth()  - SIZE) / 2;
        int y = (getHeight() - SIZE) / 2;

        if (checked) {
            // Filled blue box
            g2.setColor(CHECK_CLR);
            g2.fillRoundRect(x, y, SIZE, SIZE, ARC, ARC);
            // White checkmark
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int pad = SIZE / 4;
            g2.drawLine(x + pad,          y + SIZE / 2,
                        x + SIZE / 2 - 1, y + SIZE - pad - 1);
            g2.drawLine(x + SIZE / 2 - 1, y + SIZE - pad - 1,
                        x + SIZE - pad,   y + pad);
        } else {
            // Hover highlight behind the box
            if (hovered) {
                g2.setColor(HOVER_CLR);
                g2.fillRoundRect(x - 3, y - 3, SIZE + 6, SIZE + 6, ARC + 2, ARC + 2);
            }
            // Empty box
            g2.setColor(BG_NORMAL);
            g2.fillRoundRect(x, y, SIZE, SIZE, ARC, ARC);
            // Border
            g2.setColor(hovered ? CHECK_CLR : BORDER_CLR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, SIZE, SIZE, ARC, ARC);
        }

        g2.dispose();
    }

    @Override public Dimension getPreferredSize() { return new Dimension(20, 20); }

    // ── TableCellRenderer ─────────────────────────────────────────────────────

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        checked = Boolean.TRUE.equals(value);
        hovered = false;
        isHeader = false;
        setBackground(table.getBackground());
        return this;
    }

    // ── TableCellEditor ───────────────────────────────────────────────────────

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        checked = Boolean.TRUE.equals(value);
        hovered = false;
        isHeader = false;
        return this;
    }

    @Override public Object  getCellEditorValue()            { return checked; }
    @Override public boolean isCellEditable(EventObject e)   { return true; }
    @Override public boolean shouldSelectCell(EventObject e) { return false; }
    @Override public boolean stopCellEditing()               { fireEditingStopped(); return true; }
    @Override public void    cancelCellEditing()             { fireEditingCanceled(); }

    @Override
    public void addCellEditorListener(javax.swing.event.CellEditorListener l) {
        listeners.add(l);
    }
    @Override
    public void removeCellEditorListener(javax.swing.event.CellEditorListener l) {
        listeners.remove(l);
    }

    private void fireEditingStopped() {
        javax.swing.event.ChangeEvent evt = new javax.swing.event.ChangeEvent(this);
        for (int i = listeners.size() - 1; i >= 0; i--)
            listeners.get(i).editingStopped(evt);
    }
    private void fireEditingCanceled() {
        javax.swing.event.ChangeEvent evt = new javax.swing.event.ChangeEvent(this);
        for (int i = listeners.size() - 1; i >= 0; i--)
            listeners.get(i).editingCanceled(evt);
    }
}