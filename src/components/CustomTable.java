package components;

import javax.swing.*;
import javax.swing.table.*;
import utils.IconLoader;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CustomTable extends JTable {
    private static final long serialVersionUID = 1L;

    private Object[][] fullData    = new Object[0][0];
    private Object[]   columnNames = new Object[0];
    private int pageSize    = 10;
    private int currentPage = 1;
    private JLabel infoLabel;

    private java.util.Set<Integer>          editableColumns  = new java.util.HashSet<>();
    private java.util.Map<Integer, Integer> columnWidths     = new java.util.HashMap<>();
    private java.util.Map<Integer, Integer> columnAlignments = new java.util.HashMap<>();

    private TableModel externalModel = null;

    private TableCellRenderer checkboxRenderer = null;
    private TableCellEditor   checkboxEditor   = null;
    private TableCellRenderer checkboxHeader   = null;
    private int               checkboxCol      = -1;

    // Called after every model rebuild so the owner can reapply column widths/renderers
    private Runnable postUpdateHook = null;

    public void setPostUpdateHook(Runnable hook) {
        this.postUpdateHook = hook;
    }

    public void setCheckboxColumn(int col,
            TableCellRenderer renderer,
            TableCellEditor   editor,
            TableCellRenderer headerRenderer) {
        this.checkboxCol      = col;
        this.checkboxRenderer = renderer;
        this.checkboxEditor   = editor;
        this.checkboxHeader   = headerRenderer;
    }

    public CustomTable() {
        super();
        setupDefaultAppearance();
    }

    public CustomTable(Object[][] data, Object[] columnNames) {
        super();
        setupDefaultAppearance();
        setFullData(data, columnNames);
    }

    public CustomTable(TableModel model) {
        super();
        this.externalModel = model;
        setupDefaultAppearance();
        setFullData(new Object[0][model.getColumnCount()],
                    buildColumnNamesFromModel(model));
    }

    public CustomTable(ResultSet rs) throws SQLException {
        super(buildTableModel(rs));
        setupDefaultAppearance();
    }

    private Object[] buildColumnNamesFromModel(TableModel model) {
        Object[] names = new Object[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++)
            names[i] = model.getColumnName(i);
        return names;
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++)
            columnNames.add(metaData.getColumnName(column));
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++)
                vector.add(rs.getObject(columnIndex));
            data.add(vector);
        }
        return new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (getParent() == null) return false;
        // For tables with AUTO_RESIZE_OFF, never track viewport —
        // always let horizontal scroll handle overflow
        if (getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) return false;
        // For AUTO_RESIZE_LAST_COLUMN tables, track viewport so last col stretches
        return getPreferredSize().width <= getParent().getWidth();
    }

    private void setupDefaultAppearance() {
        setFillsViewportHeight(true);
        if (getTableHeader() != null) {
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setResizingAllowed(false);
        }
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void setFullData(Object[][] data, Object[] columnNames) {
        this.fullData    = data        != null ? data        : new Object[0][0];
        this.columnNames = columnNames != null ? columnNames : new Object[0];
        this.currentPage = 1;
        updateTableModel();
    }

    private void updateTableModel() {
        int totalItems = fullData.length;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        int start = (currentPage - 1) * pageSize;
        int end   = Math.min(start + pageSize, totalItems);

        Object[][] pageData = new Object[end - start][columnNames.length];
        for (int i = start; i < end; i++)
            pageData[i - start] = fullData[i];

        DefaultTableModel newModel = new DefaultTableModel(pageData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int col) {
                if (externalModel != null)
                    return externalModel.isCellEditable(0, col);
                return editableColumns.contains(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (externalModel != null)
                    return externalModel.getColumnClass(col);
                return String.class;
            }
        };
        setModel(newModel);

        setFillsViewportHeight(true);
        if (getTableHeader() != null) {
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setResizingAllowed(false);
        }
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (infoLabel != null) {
            infoLabel.setText("Showing " + (totalItems == 0 ? 0 : start + 1)
                    + " to " + end + " out of " + totalItems + " items");
        }

        // Only apply the internal fixed-width map — does NOT set min/max,
        // so it won't override preferred widths set externally via getColumnModel()
        applyColumnWidths();
        applyColumnAlignments();

        if (checkboxCol >= 0 && checkboxCol < getColumnCount()) {
            TableColumn chk = getColumnModel().getColumn(checkboxCol);
            if (checkboxRenderer != null) chk.setCellRenderer(checkboxRenderer);
            if (checkboxEditor   != null) chk.setCellEditor(checkboxEditor);
            if (checkboxHeader   != null) chk.setHeaderRenderer(checkboxHeader);
        }

        // Let the owner (e.g. AdminTable) reapply its own column widths and renderers
        if (postUpdateHook != null) {
            postUpdateHook.run();
        }
    }

    public JPanel createPaginationPanel() {
        CustomPanel panel = new CustomPanel(new BorderLayout());
        panel.setPadding(5);

        infoLabel = new JLabel();
        updateTableModel();

        CustomButton btnFirst = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-left.png",  20, 20, Color.WHITE), 5);
        CustomButton btnPrev  = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-left.png",         20, 20, Color.WHITE), 5);
        CustomButton btnNext  = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-right.png",        20, 20, Color.WHITE), 5);
        CustomButton btnLast  = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-right.png", 20, 20, Color.WHITE), 5);

        btnFirst.addActionListener(e -> {
            if (currentPage > 1) { currentPage = 1; updateTableModel(); revalidate(); repaint(); }
        });
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; updateTableModel(); revalidate(); repaint(); }
        });
        btnNext.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) fullData.length / pageSize);
            if (currentPage < totalPages) { currentPage++; updateTableModel(); revalidate(); repaint(); }
        });
        btnLast.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) fullData.length / pageSize);
            if (totalPages > 0 && currentPage != totalPages) {
                currentPage = totalPages; updateTableModel(); revalidate(); repaint();
            }
        });

        CustomPanel wrapper = new CustomPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.add(btnFirst);
        wrapper.add(Box.createHorizontalStrut(5));
        wrapper.add(btnPrev);
        wrapper.add(Box.createHorizontalStrut(5));
        wrapper.add(btnNext);
        wrapper.add(Box.createHorizontalStrut(5));
        wrapper.add(btnLast);

        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(wrapper,   BorderLayout.EAST);
        return panel;
    }

    public void setGridLines(boolean horizontal, boolean vertical) {
        setShowHorizontalLines(horizontal);
        setShowVerticalLines(vertical);
    }

    public void setHeaderCustomization(Font font, Color foregroundColor, Color backgroundColor) {
        JTableHeader header = getTableHeader();
        if (header != null) {
            if (font != null)            header.setFont(font);
            if (foregroundColor != null) header.setForeground(foregroundColor);
            if (backgroundColor != null) header.setBackground(backgroundColor);
        }
    }

    public void setBodyCustomization(Font font, Color foregroundColor, Color backgroundColor) {
        if (font != null)            setFont(font);
        if (foregroundColor != null) setForeground(foregroundColor);
        if (backgroundColor != null) setBackground(backgroundColor);
    }

    public void setColumnEditable(int column, boolean editable) {
        if (editable) editableColumns.add(column);
        else          editableColumns.remove(column);
    }

    /**
     * Stores a fixed width for a column in the internal map.
     * NOTE: only sets preferredWidth — does NOT lock min/max —
     * so AUTO_RESIZE modes and external preferred widths still work.
     */
    public void setColumnWidth(int column, int width) {
        columnWidths.put(column, width);
        applyColumnWidths();
    }

    private void applyColumnWidths() {
        for (java.util.Map.Entry<Integer, Integer> entry : columnWidths.entrySet()) {
            if (entry.getKey() < getColumnCount()) {
                TableColumn col = getColumnModel().getColumn(entry.getKey());
                // Only preferred — never lock min/max here, that fights AUTO_RESIZE
                col.setPreferredWidth(entry.getValue());
            }
        }
    }

    public void setColumnAlignment(int column, int alignment) {
        columnAlignments.put(column, alignment);
        applyColumnAlignments();
    }

    private void applyColumnAlignments() {
        for (java.util.Map.Entry<Integer, Integer> entry : columnAlignments.entrySet()) {
            if (entry.getKey() < getColumnCount()) {
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setHorizontalAlignment(entry.getValue());
                getColumnModel().getColumn(entry.getKey()).setCellRenderer(renderer);
            }
        }
    }
}