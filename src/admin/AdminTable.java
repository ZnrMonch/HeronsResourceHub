package admin;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class AdminTable extends CustomPanel {
	private static final long serialVersionUID = 1L;

	public AdminTable() {
		setLayout(new BorderLayout());
		add(initHeader(), BorderLayout.NORTH);
		add(initTable(), BorderLayout.CENTER);
	}
	
	private CustomPanel initHeader() {
		CustomPanel header = new CustomPanel();
		header.addPadding(20, 15);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.add(new CustomLabel("User Management", 18f, FontStyle.BOLD));
		header.add(Box.createHorizontalGlue());
		
		CustomSearchField searchField = new CustomSearchField("Search users...", 25, 10);
		searchField.setCustomSize(200, 35);
		header.add(searchField);
		header.add(Box.createHorizontalStrut(10));
		
		header.add(new CustomLabel("Search by:"));
		header.add(Box.createHorizontalStrut(10));
		CustomComboBox<String> filterBox = new CustomComboBox<>(new String[]{"ID", "Column 1", "Column 2"});
		filterBox.setCustomSize(150, 35);
		header.add(filterBox);
		
		return header;
	}
	
	private CustomPanel initTable() {
		CustomPanel tableWrapper = new CustomPanel();
		tableWrapper.setLayout(new BorderLayout());
		
		Object[] columnNames = {"ID", "Student ID", "First Name", "Last Name", "College", "Year", "Karma Points"};
		Object[][] emptyData = new Object[0][7];
		
		CustomTable table = new CustomTable(emptyData, columnNames);
		
		/**
		 * SAMPLE DATA
		 */
		Object[][] sampleData = new Object[25][7];
		for (int i = 0; i < 25; i++) {
			sampleData[i] = new Object[]{
				String.valueOf(i + 1), "K" + (1000000 + i), "Student" + i, "Last" + i, "CCIS", "3rd", String.valueOf(150 + (i * 10))
			};
		}
		
		table.setFullData(sampleData, columnNames);
		table.setRowHeight(30);
		table.setGridLines(true, false);
		
		table.setHeaderCustomization(FontLib.POPPINS_BOLD.deriveFont(14f), Color.BLACK, new Color(240, 240, 240));
		table.setBodyCustomization(FontLib.POPPINS_REGULAR.deriveFont(12f), Color.BLACK, Color.WHITE);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(Color.WHITE);
		
		tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
		
		tableWrapper.add(scrollPane, BorderLayout.CENTER);
		tableWrapper.add(table.createPaginationPanel(), BorderLayout.SOUTH);
		
		return tableWrapper;
	}
}