package marketplace;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import components.*;
<<<<<<< HEAD
import database.DatabaseManager;
import enums.Availability;
import enums.Category;
import enums.MarketplaceTabMode;
import enums.View;
=======
import enums.*;
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
import utils.*;

public abstract class BaseBrowsePanel extends CustomPanel {
	private static final long serialVersionUID = 1L;
	
	private CustomSearchField searchField;
	private CustomComboBox<String> filterByCategoryBox;
	private CustomComboBox<String> filterByStatusBox;
	private CustomToggleButton viewToggle;
	private CustomButton actionButton;
	private ItemPanel content;

	public BaseBrowsePanel(String searchPrompt, String actionText, MarketplaceTabMode tabMode) {
		setLayout(new BorderLayout());
		setPadding(10);

		searchField = new CustomSearchField(searchPrompt);
		searchField.setCustomSize(300, 35);
		
		List<String> categoryList = new ArrayList<>();
		categoryList.add("All Categories");
		for (Category c : Category.values()) {
			categoryList.add(c.toString());
		}
		
		filterByCategoryBox = new CustomComboBox<>(categoryList.toArray(new String[0]));
		filterByCategoryBox.setCustomSize(200, 35);
		
<<<<<<< HEAD
		List<String> statusList = new ArrayList<>();
		statusList.add("All Statuses");
		for (Availability a : Availability.values()) {
			statusList.add(a.toString());
		}
		
		filterByStatusBox = new CustomComboBox<>(statusList.toArray(new String[0]));
=======
		List<String> conditionList = new ArrayList<>();
		conditionList.add("All Conditions");
		for (Condition a : Condition.values()) {
			conditionList.add(a.toString());
		}
		
		filterByStatusBox = new CustomComboBox<>(conditionList.toArray(new String[0]));
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		filterByStatusBox.setCustomSize(200, 35);
		
		viewToggle = new CustomToggleButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-grid.png", 35, 35, Brand.PRIMARY_COLOR),
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-list.png", 35, 35, Brand.PRIMARY_COLOR));
		viewToggle.setTransparent();
		
		actionButton = new CustomButton(actionText);
		actionButton.setCustomSize(120, 35);
		actionButton.setRadius(10);
		actionButton.addActionListener(e -> onActionClicked());

		CustomPanel header = new CustomPanel();
		header.setPadding(20, 10);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.add(searchField);
		header.add(Box.createHorizontalStrut(10));
		header.add(filterByCategoryBox);
		header.add(Box.createHorizontalStrut(10));
		header.add(filterByStatusBox);
		header.add(Box.createHorizontalGlue());
		header.add(viewToggle);
		header.add(Box.createHorizontalStrut(5));
		header.add(actionButton);

		content = new ItemPanel(tabMode);
		viewToggle.addToggleListener(isList -> {
			content.setView(isList ? View.LIST : View.GRID);
		});
		
		content.setItemViewStateListener(inItemView -> header.setVisible(!inItemView));

		add(header, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}

	protected abstract void onActionClicked();
}