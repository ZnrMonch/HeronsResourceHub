package pages;

import java.awt.*;
import javax.swing.*;
import components.*;
import items.*;
import utils.*;

public class Marketplace extends CustomTabbedPane {
	private static final long serialVersionUID = 1L;
	
	private CustomSearchField marketplaceSearchField;
	private CustomComboBox<String> marketplaceFilterByCategoryBox;
	private CustomComboBox<String> marketplaceFilterByStatusBox;
	private CustomToggleButton marketplaceViewToggle;
	private CustomButton marketplaceSellItemButton;
	
	private CustomSearchField sharingSearchField;
	private CustomComboBox<String> sharingFilterByCategoryBox;
	private CustomComboBox<String> sharingFilterByStatusBox;
	private CustomToggleButton sharingViewToggle;
	private CustomButton sharingLendItemButton;
	
	private CustomSearchField barterSearchField;
	private CustomComboBox<String> barterFilterByCategoryBox;
	private CustomComboBox<String> barterFilterByStatusBox;
	private CustomToggleButton barterViewToggle;
	private CustomButton barterLendItemButton;
	
	public Marketplace() {
		setBackground(Color.WHITE);
		setRadius(20);	
		
		addTab("MARKETPLACE", "/resources/icons/marketplace.png", initMarketplace());		
		addTab("SHARING", "/resources/icons/sharing.png", initSharing());
		addTab("BARTER TRADING", "/resources/icons/barter.png",  initTrading());
	}
	
	private CustomPanel initMarketplace() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BorderLayout());
		wrapper.setPadding(10);
		
		marketplaceSearchField = new CustomSearchField("Search in Marketplace...");
		marketplaceSearchField.setCustomSize(300, 35);
		marketplaceFilterByCategoryBox = new CustomComboBox<>(new String[]{"All Categories", "Category 1", "Category 2"});
		marketplaceFilterByCategoryBox.setCustomSize(200, 35);
		marketplaceFilterByStatusBox = new CustomComboBox<>(new String[]{"All Statuses", "Available", "Borrowed"});
		marketplaceFilterByStatusBox.setCustomSize(200, 35);
		marketplaceViewToggle = new CustomToggleButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-grid.png", 35, 35, Brand.PRIMARY_COLOR),
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-list.png", 35, 35, Brand.PRIMARY_COLOR));
		marketplaceViewToggle.setTransparent();
		marketplaceSellItemButton = new CustomButton("Sell Item");
		marketplaceSellItemButton.setCustomSize(120, 35);
		marketplaceSellItemButton.setRadius(10);
		
		CustomPanel header = new CustomPanel();
		header.setPadding(20, 10);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.add(marketplaceSearchField);
		header.add(Box.createHorizontalStrut(10));
		header.add(marketplaceFilterByCategoryBox);
		header.add(Box.createHorizontalStrut(10));
		header.add(marketplaceFilterByStatusBox);
		header.add(Box.createHorizontalGlue());
		header.add(marketplaceViewToggle);
		header.add(Box.createHorizontalStrut(5));
		header.add(marketplaceSellItemButton);
		
		marketplaceSellItemButton.addActionListener(e -> {
			new ItemForm((JFrame) SwingUtilities.getWindowAncestor(this), "Sell an Item");
		});
		
		ItemPanel content = new ItemPanel();
		marketplaceViewToggle.addToggleListener(isList -> {
			content.setView(isList ? View.LIST : View.GRID);
		});
		wrapper.add(content, BorderLayout.CENTER);
		
		wrapper.add(header, BorderLayout.NORTH);
		return wrapper;
	}
	
	private CustomPanel initSharing() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BorderLayout());
		wrapper.setPadding(10);
		
		sharingSearchField = new CustomSearchField("Search in Borrowing...");
		sharingSearchField.setCustomSize(300, 35);
		sharingFilterByCategoryBox = new CustomComboBox<>(new String[]{"All Categories", "Category 1", "Category 2"});
		sharingFilterByCategoryBox.setCustomSize(200, 35);
		sharingFilterByStatusBox = new CustomComboBox<>(new String[]{"All Statuses", "Available", "Borrowed"});
		sharingFilterByStatusBox.setCustomSize(200, 35);
		sharingViewToggle = new CustomToggleButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-grid.png", 35, 35, Brand.PRIMARY_COLOR),
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-list.png", 35, 35, Brand.PRIMARY_COLOR));
		sharingViewToggle.setTransparent();
		sharingLendItemButton = new CustomButton("Lend an Item");
		sharingLendItemButton.setCustomSize(120, 35);
		sharingLendItemButton.setRadius(10);
		
		CustomPanel header = new CustomPanel();
		header.setPadding(20, 10);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.add(sharingSearchField);
		header.add(Box.createHorizontalStrut(10));
		header.add(sharingFilterByCategoryBox);
		header.add(Box.createHorizontalStrut(10));
		header.add(sharingFilterByStatusBox);
		header.add(Box.createHorizontalGlue());
		header.add(sharingViewToggle);
		header.add(Box.createHorizontalStrut(5));
		header.add(sharingLendItemButton);
		
		sharingLendItemButton.addActionListener(e -> {
			new ItemForm((JFrame) SwingUtilities.getWindowAncestor(this), "Lend an Item");
		});
		
		ItemPanel content = new ItemPanel();
		sharingViewToggle.addToggleListener(isList -> {
			content.setView(isList ? View.LIST : View.GRID);
		});
		wrapper.add(content, BorderLayout.CENTER);
		
		wrapper.add(header, BorderLayout.NORTH);
		
		return wrapper;
	}
	
	private CustomPanel initTrading() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BorderLayout());
		wrapper.setPadding(10);
		
		barterSearchField = new CustomSearchField("Search in Trading...");
		barterSearchField.setCustomSize(300, 35);
		barterFilterByCategoryBox = new CustomComboBox<>(new String[]{"All Categories", "Category 1", "Category 2"});
		barterFilterByCategoryBox.setCustomSize(200, 35);
		barterFilterByStatusBox = new CustomComboBox<>(new String[]{"All Statuses", "Available", "Borrowed"});
		barterFilterByStatusBox.setCustomSize(200, 35);
		barterViewToggle = new CustomToggleButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-grid.png", 35, 35, Brand.PRIMARY_COLOR),
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/view-list.png", 35, 35, Brand.PRIMARY_COLOR));
		barterViewToggle.setTransparent();
		barterLendItemButton = new CustomButton("Offer an Item");
		barterLendItemButton.setCustomSize(120, 35);
		barterLendItemButton.setRadius(10);
		
		CustomPanel header = new CustomPanel();
		header.setPadding(20, 10);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.add(barterSearchField);
		header.add(Box.createHorizontalStrut(10));
		header.add(barterFilterByCategoryBox);
		header.add(Box.createHorizontalStrut(10));
		header.add(barterFilterByStatusBox);
		header.add(Box.createHorizontalGlue());
		header.add(barterViewToggle);
		header.add(Box.createHorizontalStrut(5));
		header.add(barterLendItemButton);
		
		barterLendItemButton.addActionListener(e -> {
			new ItemForm((JFrame) SwingUtilities.getWindowAncestor(this), "Offer an Item");
		});
		
		ItemPanel content = new ItemPanel();
		barterViewToggle.addToggleListener(isList -> {
			content.setView(isList ? View.LIST : View.GRID);
		});
		wrapper.add(content, BorderLayout.CENTER);
		
		wrapper.add(header, BorderLayout.NORTH);
		
		return wrapper;
	}
}