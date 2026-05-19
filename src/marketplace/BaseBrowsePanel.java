package marketplace;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import components.*;
import database.UserRecord;
import enums.*;
import utils.*;

public abstract class BaseBrowsePanel extends CustomPanel {
	private static final long serialVersionUID = 1L;
	
	private CustomComboBox<String> searchTargetBox;
	private CustomSearchField searchField;
	private CustomComboBox<String> filterByCategoryBox;
	private CustomComboBox<String> filterByStatusBox;
	private CustomComboBox<String> filterByPriceBox;
	private CustomButton resetButton;
	private CustomToggleButton viewToggle;
	private CustomButton actionButton;
	private ItemPanel content;

	public BaseBrowsePanel(UserRecord user, String searchPrompt, String actionText, MarketplaceTabMode tabMode) {
		setLayout(new BorderLayout());
		setPadding(10);

		searchTargetBox = new CustomComboBox<>(new String[]{"All", "Item Name", "Initiator Name"});
		searchTargetBox.setCustomSize(130, 35);

		searchField = new CustomSearchField(searchPrompt);
		searchField.setCustomSize(250, 35);
		
		List<String> categoryList = new ArrayList<>();
		categoryList.add("All Categories");
		for (Category c : Category.values()) {
			categoryList.add(c.toString());
		}
		filterByCategoryBox = new CustomComboBox<>(categoryList.toArray(new String[0]));
		filterByCategoryBox.setCustomSize(140, 35);
		
		List<String> conditionList = new ArrayList<>();
		conditionList.add("All Conditions");
		for (Condition a : Condition.values()) {
			conditionList.add(a.toString());
		}
		filterByStatusBox = new CustomComboBox<>(conditionList.toArray(new String[0]));
		filterByStatusBox.setCustomSize(140, 35);

		filterByPriceBox = new CustomComboBox<>(new String[]{"All Prices", "Free", "Under P500", "P500 - P1000", "Over P1000"});
		filterByPriceBox.setCustomSize(130, 35);

		resetButton = new CustomButton("Reset");
		resetButton.setCustomSize(80, 35);
		resetButton.setRadius(10);
		resetButton.addActionListener(e -> resetFilters());
		
		viewToggle = new CustomToggleButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-grid.png", 35, 35, Brand.PRIMARY_COLOR),
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-list.png", 35, 35, Brand.PRIMARY_COLOR));
		viewToggle.setTransparent();
		
		CustomPanel header = new CustomPanel();
		header.setPadding(20, 10);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.add(searchTargetBox);
		header.add(Box.createHorizontalStrut(5));
		header.add(searchField);
		header.add(Box.createHorizontalStrut(5));
		header.add(filterByCategoryBox);
		header.add(Box.createHorizontalStrut(5));
		header.add(filterByStatusBox);
		header.add(Box.createHorizontalStrut(5));
		header.add(filterByPriceBox);
		header.add(Box.createHorizontalStrut(5));
		header.add(resetButton);
		header.add(Box.createHorizontalGlue());
		header.add(viewToggle);

		if (actionText != null && !actionText.trim().isEmpty()) {
			actionButton = new CustomButton(actionText);
			actionButton.setCustomSize(120, 35);
			actionButton.setRadius(10);
			actionButton.addActionListener(e -> onActionClicked());
			
			header.add(Box.createHorizontalStrut(5));
			header.add(actionButton);
		}

		content = new ItemPanel(user, tabMode);
		viewToggle.addToggleListener(isList -> {
			content.setView(isList ? View.LIST : View.GRID);
		});
		
		content.setItemViewStateListener(inItemView -> header.setVisible(!inItemView));

		searchTargetBox.addActionListener(e -> triggerFilter());
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { triggerFilter(); }
			public void removeUpdate(DocumentEvent e) { triggerFilter(); }
			public void changedUpdate(DocumentEvent e) { triggerFilter(); }
		});
		filterByCategoryBox.addActionListener(e -> triggerFilter());
		filterByStatusBox.addActionListener(e -> triggerFilter());
		filterByPriceBox.addActionListener(e -> triggerFilter());

		add(header, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}

    // NEW: Exposes a hard-refresh trigger to the parent tabs
	public void refreshData() {
		if (content != null) {
			content.refreshPanel();
		}
	}

	private void resetFilters() {
		searchTargetBox.setSelectedIndex(0);
		searchField.setText("");
		filterByCategoryBox.setSelectedIndex(0);
		filterByStatusBox.setSelectedIndex(0);
		filterByPriceBox.setSelectedIndex(0);
		triggerFilter();
	}

	private void triggerFilter() {
		String target = (String) searchTargetBox.getSelectedItem();
		String search = searchField.getText();
		String category = (String) filterByCategoryBox.getSelectedItem();
		String condition = (String) filterByStatusBox.getSelectedItem();
		String price = (String) filterByPriceBox.getSelectedItem();
		content.applyFilters(target, search, category, condition, price);
	}

	protected abstract void onActionClicked();
}