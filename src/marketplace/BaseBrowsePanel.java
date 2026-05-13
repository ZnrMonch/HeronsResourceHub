package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import database.DatabaseManager;
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
        
        filterByCategoryBox = new CustomComboBox<>(new String[]{"All Categories", "Category 1", "Category 2"});
        filterByCategoryBox.setCustomSize(200, 35);
        
        filterByStatusBox = new CustomComboBox<>(new String[]{"All Statuses", "Available", "Borrowed"});
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