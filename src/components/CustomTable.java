package components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CustomTable extends JTable {

    public CustomTable() {
        super();
        setupDefaultAppearance();
    }
    
    // Constructor for Object arrays
    public CustomTable(Object[][] data, Object[] columnNames) {
        super(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        setupDefaultAppearance();
    }
    
    // Constructor that takes a ResultSet to automatically populate the table
    public CustomTable(ResultSet rs) throws SQLException {
        super(buildTableModel(rs));
        setupDefaultAppearance();
    }

    // Method to build a DefaultTableModel from a ResultSet
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Column names
        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Typically, database tables are rendered read-only
            }
        };
    }

    private void setupDefaultAppearance() {
        setFillsViewportHeight(true);
    }

    // Customize borderlines / gridlines
    public void setGridLines(boolean horizontal, boolean vertical) {
        setShowHorizontalLines(horizontal);
        setShowVerticalLines(vertical);
        if (horizontal || vertical) {
            setShowGrid(true);
        } else {
            setShowGrid(false);
        }
    }

    // Customize the header (font and color)
    public void setHeaderCustomization(Font font, Color foregroundColor, Color backgroundColor) {
        JTableHeader header = getTableHeader();
        if (header != null) {
            if (font != null) header.setFont(font);
            if (foregroundColor != null) header.setForeground(foregroundColor);
            if (backgroundColor != null) header.setBackground(backgroundColor);
        }
    }

    // Customize the body details (font and color for default renderer)
    public void setBodyCustomization(Font font, Color foregroundColor, Color backgroundColor) {
        if (font != null) setFont(font);
        if (foregroundColor != null) setForeground(foregroundColor);
        if (backgroundColor != null) setBackground(backgroundColor);
    }
}