package components;

import javax.swing.*;
import javax.swing.table.*;

import utils.IconLoader;

import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CustomTable extends JTable {
	private static final long serialVersionUID = 1L;
	
	private Object[][] fullData = new Object[0][0];
    private Object[] columnNames = new Object[0];
    private int pageSize = 10;
    private int currentPage = 1;
    private JLabel infoLabel;

    public CustomTable() {
        super();
        setupDefaultAppearance();
    }
    
    public CustomTable(Object[][] data, Object[] columnNames) {
        super();
        setupDefaultAppearance();
        setFullData(data, columnNames);
    }
    
    public CustomTable(ResultSet rs) throws SQLException {
        super(buildTableModel(rs));
        setupDefaultAppearance();
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

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
                return false;
            }
        };
    }

    private void setupDefaultAppearance() {
        setFillsViewportHeight(true);
        if (getTableHeader() != null) {
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setResizingAllowed(false);
        }
        
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Remove the default table border
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void setFullData(Object[][] data, Object[] columnNames) {
        this.fullData = data != null ? data : new Object[0][0];
        this.columnNames = columnNames != null ? columnNames : new Object[0];
        this.currentPage = 1;
        
        DefaultTableModel fullModel = new DefaultTableModel(fullData, this.columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        setModel(fullModel);
        setupDefaultAppearance(); 
        
        updateTableModel();
    }

    private void updateTableModel() {
        int totalItems = fullData.length;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;
        
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, totalItems);
        
        Object[][] pageData = new Object[end - start][columnNames.length];
        
        for (int i = start; i < end; i++) {
            pageData[i - start] = fullData[i];
        }
        
        DefaultTableModel newModel = new DefaultTableModel(pageData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
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
            infoLabel.setText("Showing " + (totalItems == 0 ? 0 : start + 1) + " to " + end + " out of " + totalItems + " items");
        }
    }

    public JPanel createPaginationPanel() {
        CustomPanel panel = new CustomPanel(new BorderLayout());
        panel.setPadding(5);
        
        infoLabel = new JLabel();
        updateTableModel();
        
        CustomButton btnFirst = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-left.png", 20, 20, Color.WHITE), 5);
        CustomButton btnPrev = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-left.png", 20, 20, Color.WHITE), 5);
        CustomButton btnNext = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-right.png", 20, 20, Color.WHITE), 5);
        CustomButton btnLast = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-right.png", 20, 20, Color.WHITE), 5);
        
        btnFirst.addActionListener(e -> { 
            if (currentPage > 1) { 
                currentPage = 1; 
                updateTableModel(); 
                revalidate();
                repaint();
            } 
        });
        
        btnPrev.addActionListener(e -> { 
            if (currentPage > 1) { 
                currentPage--; 
                updateTableModel(); 
                revalidate();
                repaint();
            } 
        });
        
        btnNext.addActionListener(e -> { 
            int totalPages = (int) Math.ceil((double) fullData.length / pageSize);
            if (currentPage < totalPages) { 
                currentPage++; 
                updateTableModel(); 
                revalidate();
                repaint();
            }
        });
        
        btnLast.addActionListener(e -> { 
            int totalPages = (int) Math.ceil((double) fullData.length / pageSize);
            if (totalPages > 0 && currentPage != totalPages) { 
                currentPage = totalPages; 
                updateTableModel(); 
                revalidate();
                repaint();
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
        panel.add(wrapper, BorderLayout.EAST);
                
        return panel;
    }

    public void setGridLines(boolean horizontal, boolean vertical) {
        setShowHorizontalLines(horizontal);
        setShowVerticalLines(vertical);
    }

    public void setHeaderCustomization(Font font, Color foregroundColor, Color backgroundColor) {
        JTableHeader header = getTableHeader();
        if (header != null) {
            if (font != null) header.setFont(font);
            if (foregroundColor != null) header.setForeground(foregroundColor);
            if (backgroundColor != null) header.setBackground(backgroundColor);
        }
    }

    public void setBodyCustomization(Font font, Color foregroundColor, Color backgroundColor) {
        if (font != null) setFont(font);
        if (foregroundColor != null) setForeground(foregroundColor);
        if (backgroundColor != null) setBackground(backgroundColor);
    }
}