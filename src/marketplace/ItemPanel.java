package marketplace;

import java.awt.*;
import java.sql.*;
<<<<<<< HEAD
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
import javax.swing.*;
import javax.swing.event.ChangeListener;
import components.*;
import database.DatabaseManager;
import enums.MarketplaceTabMode;
import enums.View;
import utils.*;
import database.*;

<<<<<<< HEAD
=======
// Custom Interface to replace Consumer<Boolean>
interface ViewStateListener {
	void onViewStateChanged(boolean isItemView);
}

// Custom Interface to replace Consumer<ItemRecord>
interface ItemActionListener {
	void onItemAction(ItemRecord record);
}

>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
public class ItemPanel extends CustomPanel {
	private static final long serialVersionUID = 1L;
	private View view = View.GRID;
	private MarketplaceTabMode tabMode = MarketplaceTabMode.MARKETPLACE;
	private CustomButton backButton;

	private final List<ItemRecord> marketplaceItems = new ArrayList<>();
	private final List<ItemRecord> sharingItems = new ArrayList<>();
	private final List<ItemRecord> tradingItems = new ArrayList<>();

	private int currentPage = 0;
	private static final int GRID_COLUMNS = 4;
	private static final int GRID_ROWS = 2;
	private static final int GRID_PAGE_SIZE = GRID_COLUMNS * GRID_ROWS; 
	private static final int LIST_PAGE_SIZE = 8;

	private ItemRecord selectedItem;
<<<<<<< HEAD
	private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM d, yyyy");
=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	
	private CustomSpinner quantitySpinner = new CustomSpinner(new SpinnerNumberModel(1, 1, 10, 1));
	private ChangeListener quantityChangeListener = e -> updateCheckoutButtonLabel();
	
	private CustomComboBox<String> paymentMethodComboBox;
	private CustomButton checkoutButton;
	private CustomButton primaryActionButton;
	private CustomButton secondaryActionButton;
<<<<<<< HEAD
	private Consumer<Boolean> itemViewStateListener;
=======
	private ViewStateListener itemViewStateListener; // Using custom interface
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	private boolean checkoutView = false;
	
	private final String DB_URL = DatabaseManager.getURL();
	private final String USER = DatabaseManager.getUser();
	private final String PASSWORD = DatabaseManager.getPassword();
	
	public ItemPanel() {
		this(MarketplaceTabMode.MARKETPLACE);
	}

	public ItemPanel(MarketplaceTabMode tabMode) {
		setBackground(Color.WHITE);
		setPadding(20);
		setLayout(new BorderLayout(20, 20));
		if (tabMode != null) {
			this.tabMode = tabMode;
		}

		fetchItemData();
		restoreView();
	}

	private void restoreView() {
		checkoutView = false;
		removeAll();
		if (View.GRID.equals(view)) {
			initGridView();
		} else {
			initListView();
		}
		initNav();
		if (itemViewStateListener != null) {
<<<<<<< HEAD
			itemViewStateListener.accept(false);
=======
			itemViewStateListener.onViewStateChanged(false); // Call interface method
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		}
		revalidate();
		repaint();
	}
	
	private void fetchItemData() {
		marketplaceItems.clear();
		sharingItems.clear();
		tradingItems.clear();

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
				Statement stmt = conn.createStatement();
<<<<<<< HEAD
				ResultSet rs = stmt.executeQuery("SELECT * FROM ITEMS")) {
=======
				ResultSet rs = stmt.executeQuery("SELECT * FROM items")) {
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876

			while (rs.next()) {
				ItemRecord item = new ItemRecord();
				item.itemId = rs.getInt("item_id");
				item.ownerId = rs.getInt("owner_id");
<<<<<<< HEAD
				item.itemName = rs.getString("item_name");
				item.itemQuantity = rs.getInt("item_quantity");
				item.description = rs.getString("description");
				item.category = rs.getString("category");
				item.itemCondition = rs.getString("item_condition");
				item.price = rs.getInt("price");
=======
				item.initiatorFirstName = rs.getString("initiator_firstname");
				item.initiatorLastName = rs.getString("initiator_lastname");
				item.itemName = rs.getString("item_name");
				item.itemQuantity = rs.getInt("item_quantity");
				item.description = rs.getString("description");
				item.itemsImage = rs.getString("items_image");
				item.category = rs.getString("category");
				item.condition = rs.getString("condition");
				item.price = rs.getInt("price"); // Assuming int for simplicity, or getDouble
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
				item.availabilityStatus = rs.getString("availability_status");
				item.maximumBorrowDays = rs.getInt("maximum_borrow_days");
				item.desiredItem = rs.getString("desired_item");
				item.dateListed = rs.getTimestamp("date_listed");
				item.pickupArea = rs.getString("pickup_area");
				item.pickupTime = rs.getString("pickup_time");
<<<<<<< HEAD
				item.pickupDate = rs.getDate("pickup_date");
=======
				item.pickupDays = rs.getString("pickup_days"); // Fetches the SET string
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
				item.action = rs.getString("action");

				String normalizedAction = item.action == null ? "" : item.action.trim().toLowerCase();
				if ("sharing".equals(normalizedAction)) {
					sharingItems.add(item);
<<<<<<< HEAD
				} else if ("barter trading".equals(normalizedAction)) {
=======
				} else if ("barter-trading".equals(normalizedAction) || "barter trading".equals(normalizedAction)) {
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
					tradingItems.add(item);
				} else {
					marketplaceItems.add(item);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getPrimaryActionText() {
		switch (tabMode) {
		case SHARING: return "Borrow Item";
		case TRADING: return "Trade Item";
		default: return "Buy Item";
		}
	}

<<<<<<< HEAD
	private String formatDisplayDate(Date date) {
		return date == null ? "" : displayDateFormat.format(date);
	}

=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	private int getSelectedQuantity() {
		Object value = quantitySpinner.getValue();
		return value instanceof Number ? ((Number) value).intValue() : 1;
	}

	private String getSelectedPaymentMethod() {
		if (paymentMethodComboBox == null || paymentMethodComboBox.getSelectedItem() == null) return "";
		String s = paymentMethodComboBox.getSelectedItem().toString();
		return "Select a Payment Method".equalsIgnoreCase(s) ? "" : s;
	}

<<<<<<< HEAD
	private int getSelectedItemTotal() {
=======
	private double getSelectedItemTotal() {
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		return (selectedItem != null ? selectedItem.price : 0) * getSelectedQuantity();
	}

	private void updateCheckoutButtonLabel() {
		if (checkoutButton != null) {
<<<<<<< HEAD
			int total = getSelectedItemTotal();
			checkoutButton.setText("Checkout " + (total > 0 ? ("P" + total) : "Free"));
=======
			double total = getSelectedItemTotal();
			checkoutButton.setText("Checkout " + (total > 0 ? ("P" + String.format("%.0f", total)) : "Free"));
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		}
	}

	private List<ItemRecord> getItemsForCurrentTab() {
		switch (tabMode) {
		case SHARING: return sharingItems;
		case TRADING: return tradingItems;
		default: return marketplaceItems;
		}
	}

	private void addEmptyState(CustomPanel parent) {
		CustomPanel empty = new CustomPanel();
		empty.setBackground(Color.WHITE);
		empty.setLayout(new GridBagLayout());
		CustomLabel label = new CustomLabel("No items found.", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		label.setForeground(Color.GRAY);
		empty.add(label);
		parent.add(empty);
	}

	private void showItemView(ItemRecord record) {
		selectedItem = record;
		checkoutView = false;
		removeAll();
		viewItem();
		if (itemViewStateListener != null) {
<<<<<<< HEAD
			itemViewStateListener.accept(true);
=======
			itemViewStateListener.onViewStateChanged(true); // Call interface method
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		}
		revalidate();
		repaint();
	}

	private void initGridView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new GridLayout(GRID_ROWS, GRID_COLUMNS, 10, 10));

		List<ItemRecord> items = getItemsForCurrentTab();
		boolean showPrice = MarketplaceTabMode.MARKETPLACE.equals(tabMode);
		
		if (items.isEmpty()) {
			addEmptyState(wrapper);
		} else {
			int start = currentPage * GRID_PAGE_SIZE;
			if (start >= items.size()) {
				currentPage = 0;
				start = 0;
			}
			int endExclusive = Math.min(start + GRID_PAGE_SIZE, items.size());
			int added = 0;
<<<<<<< HEAD
			for (int i = start; i < endExclusive; i++) {
				ItemCard card = new ItemCard(View.GRID, getPrimaryActionText(), items.get(i), showPrice, this::showItemView);
=======
			
			// Use an anonymous class for our custom interface listener
			ItemActionListener listener = new ItemActionListener() {
				@Override
				public void onItemAction(ItemRecord record) {
					showItemView(record);
				}
			};

			for (int i = start; i < endExclusive; i++) {
				ItemCard card = new ItemCard(View.GRID, getPrimaryActionText(), items.get(i), showPrice, listener);
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
				wrapper.add(card);
				added++;
			}
			for (int i = added; i < GRID_PAGE_SIZE; i++) wrapper.add(new CustomPanel());
		}
		add(wrapper, BorderLayout.CENTER);
	}
	
	private void initListView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBackground(Color.WHITE);
		wrapper.setPadding(10, 20);

		List<ItemRecord> items = getItemsForCurrentTab();
		boolean showPrice = MarketplaceTabMode.MARKETPLACE.equals(tabMode);
		
		if (items.isEmpty()) {
			addEmptyState(wrapper);
		} else {
			int start = currentPage * LIST_PAGE_SIZE;
			if (start >= items.size()) {
				currentPage = 0;
				start = 0;
			}
			int endExclusive = Math.min(start + LIST_PAGE_SIZE, items.size());
<<<<<<< HEAD
			for (int i = start; i < endExclusive; i++) {
				ItemCard card = new ItemCard(View.LIST, getPrimaryActionText(), items.get(i), showPrice, this::showItemView);
=======
			
			ItemActionListener listener = new ItemActionListener() {
				@Override
				public void onItemAction(ItemRecord record) {
					showItemView(record);
				}
			};

			for (int i = start; i < endExclusive; i++) {
				ItemCard card = new ItemCard(View.LIST, getPrimaryActionText(), items.get(i), showPrice, listener);
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
				card.setPreferredSize(new Dimension(0, 100));
				card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
				wrapper.add(card);
				wrapper.add(Box.createVerticalStrut(10));
			}
		}
		
		JScrollPane scrollPane = new JScrollPane(wrapper);
		scrollPane.setBorder(null);
		scrollPane.setBackground(Color.WHITE);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	private void initNav() {
		CustomPanel navWrapper = new CustomPanel(new BorderLayout());
<<<<<<< HEAD
=======
		
		// Calculate item range for the label
		List<ItemRecord> items = getItemsForCurrentTab();
		int totalItems = items.size();
		int pageSize = View.GRID.equals(view) ? GRID_PAGE_SIZE : LIST_PAGE_SIZE;
		int startItem = totalItems == 0 ? 0 : (currentPage * pageSize) + 1;
		int endItem = Math.min((currentPage + 1) * pageSize, totalItems);
		
		CustomLabel showingLabel = new CustomLabel(
			String.format("Showing %d - %d of %d items", startItem, endItem, totalItems), 
			Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY
		);
		navWrapper.add(showingLabel, BorderLayout.WEST);

>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		CustomButton btnFirst = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-left.png", 20, 20, Color.WHITE), 5);
		CustomButton btnPrev = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-left.png", 20, 20, Color.WHITE), 5);
		CustomButton btnNext = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-right.png", 20, 20, Color.WHITE), 5);
		CustomButton btnLast = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-right.png", 20, 20, Color.WHITE), 5);

		btnFirst.addActionListener(e -> goToPage(0));
		btnPrev.addActionListener(e -> goToPage(currentPage - 1));
		btnNext.addActionListener(e -> goToPage(currentPage + 1));
		btnLast.addActionListener(e -> goToPage(getLastPageIndex()));
		
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
		wrapper.add(btnFirst);
		wrapper.add(Box.createHorizontalStrut(5));
		wrapper.add(btnPrev);
		wrapper.add(Box.createHorizontalStrut(5));
		wrapper.add(btnNext);
		wrapper.add(Box.createHorizontalStrut(5));
		wrapper.add(btnLast);
		
		navWrapper.add(wrapper, BorderLayout.EAST);
		add(navWrapper, BorderLayout.SOUTH);
	}
<<<<<<< HEAD

=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	private int getLastPageIndex() {
		List<ItemRecord> items = getItemsForCurrentTab();
		return items.isEmpty() ? 0 : Math.max(0, (items.size() - 1) / (View.GRID.equals(view) ? GRID_PAGE_SIZE : LIST_PAGE_SIZE));
	}

	private void goToPage(int page) {
		int last = getLastPageIndex();
		page = Math.max(0, Math.min(page, last));
		if (page != currentPage) {
			currentPage = page;
			restoreView();
		}
	}
	
	private void viewItem() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout(30, 20));
		CustomPanel header = new CustomPanel(new BorderLayout());
		backButton = new CustomButton(IconLoader.loadAndScaleIcon("/resources/icons/arrow-left.png", 40, 40), 0);
		backButton.setTransparent();
		
		if (checkoutView) {
			backButton.setToolTipText("Return to Item Information");
		} else {
			String rawTab = tabMode.toString();
			backButton.setToolTipText("Return to " + rawTab.substring(0, 1).toUpperCase() + rawTab.substring(1).toLowerCase());
		}
		
		backButton.addActionListener(e -> {
			if (checkoutView) {
				checkoutView = false;
				refreshItemView();
			} else {
				restoreView();
			}
		});
		
		header.add(backButton, BorderLayout.WEST);
		wrapper.add(header, BorderLayout.NORTH);
		
		CustomPanel westWrapper = new CustomPanel();
<<<<<<< HEAD
		JLabel imgLabel = new JLabel(IconLoader.loadAndScaleIcon("/resources/images/umak_img.jpg", 400, 500)); 
=======
		
		// Map dynamic image path if it exists
		String imagePath = (selectedItem != null && selectedItem.itemsImage != null && !selectedItem.itemsImage.isEmpty()) 
							? selectedItem.itemsImage 
							: "/resources/images/umak_img.jpg";
							
		JLabel imgLabel = new JLabel(IconLoader.loadAndScaleIcon(imagePath, 400, 500)); 
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		imgLabel.setPreferredSize(new Dimension(400, 500));
		imgLabel.setMaximumSize(new Dimension(400, 500));
		imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		westWrapper.add(imgLabel);
		
		wrapper.add(westWrapper, BorderLayout.WEST);
		wrapper.add(checkoutView ? viewItemCheckout() : viewItemInfo(), BorderLayout.CENTER);
		add(wrapper, BorderLayout.CENTER);
	}

	private void refreshItemView() {
		removeAll();
		viewItem();
<<<<<<< HEAD
		if (itemViewStateListener != null) itemViewStateListener.accept(true);
=======
		if (itemViewStateListener != null) itemViewStateListener.onViewStateChanged(true);
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		revalidate();
		repaint();
	}
	
	private CustomPanel viewItemInfo() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout());
		CustomPanel topContent = new CustomPanel();
		topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));
		
		String itemName = selectedItem != null && selectedItem.itemName != null ? selectedItem.itemName : "Item";
		String itemCategory = selectedItem != null && selectedItem.category != null ? selectedItem.category : "";
<<<<<<< HEAD
		int itemPrice = selectedItem != null ? selectedItem.price : 0;
=======
		double itemPrice = selectedItem != null ? selectedItem.price : 0;
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		String itemDesc = selectedItem != null && selectedItem.description != null ? selectedItem.description : "";
		int stock = selectedItem != null ? selectedItem.itemQuantity : 0;
		String pickupArea = selectedItem != null && selectedItem.pickupArea != null ? selectedItem.pickupArea : "";
		String pickupTime = selectedItem != null && selectedItem.pickupTime != null ? selectedItem.pickupTime : "";
<<<<<<< HEAD
		Date pickupDate = selectedItem != null ? selectedItem.pickupDate : null;
=======
		String pickupDays = selectedItem != null && selectedItem.pickupDays != null ? selectedItem.pickupDays : "";
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		
		CustomPanel itemInfoWrapper = new CustomPanel(new BorderLayout(10, 0));
		itemInfoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		CustomLabel nameLabel = new CustomLabel(itemName, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD);
		CustomLabel categoryLabel = new CustomLabel(itemCategory, Brand.HEADER4_TEXT_SIZE, FontStyle.REGULAR);
		categoryLabel.setForeground(Color.GRAY);
		itemInfoWrapper.add(nameLabel, BorderLayout.CENTER);
		itemInfoWrapper.add(categoryLabel, BorderLayout.EAST);
		
<<<<<<< HEAD
		CustomLabel priceLabel = new CustomLabel(itemPrice > 0 ? ("P" + itemPrice) : "Free", Brand.HEADER1_TEXT_SIZE, FontStyle.BOLD);
		priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		priceLabel.setForeground(Brand.PRIMARY_COLOR);

		CustomLabel initiatorLabel = new CustomLabel("LINDSAY MARY BALABIS", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		initiatorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
=======
		String priceText = itemPrice > 0 ? "P" + String.format("%.0f", itemPrice) : "Free";
		CustomLabel priceLabel = new CustomLabel(priceText, Brand.HEADER1_TEXT_SIZE, FontStyle.BOLD);
		priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		priceLabel.setForeground(Brand.PRIMARY_COLOR);

		// Format Initiator Name
		String fullName = "by Someone";
		if (selectedItem != null && selectedItem.initiatorFirstName != null && selectedItem.initiatorLastName != null) {
			fullName = "by " + selectedItem.initiatorFirstName.toUpperCase() + " " + selectedItem.initiatorLastName.toUpperCase();
		}
		CustomLabel initiatorLabel = new CustomLabel(fullName, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		initiatorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Clean up pickup days formatting (convert "Monday,Tuesday" to "Monday, Tuesday")
		String displayDays = pickupDays.replace(",", ", ");
		
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		CustomPanel locationWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		locationWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/location.png", 15, 15, Color.GRAY)));
		locationWrapper.add(new CustomLabel(pickupArea, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
<<<<<<< HEAD
		locationWrapper.add(new CustomLabel(formatDisplayDate(pickupDate), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
=======
		locationWrapper.add(new CustomLabel(displayDays, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(pickupTime, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		
		CustomPanel quantityWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		quantityWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

		SpinnerNumberModel spinnerModel = (SpinnerNumberModel) quantitySpinner.getModel();
		spinnerModel.setMaximum(Math.max(1, stock));
		spinnerModel.setMinimum(1);
		spinnerModel.setValue(1);
		
		quantitySpinner.removeChangeListener(quantityChangeListener);
		quantitySpinner.addChangeListener(quantityChangeListener);
		
		quantityWrapper.add(quantitySpinner);
		quantityWrapper.add(new CustomLabel("Stock: " + stock, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));

		CustomPanel actionRow = buildTabActionRow();
		
		CustomPanel descriptionWrapper = new CustomPanel();
		descriptionWrapper.setLayout(new BoxLayout(descriptionWrapper, BoxLayout.Y_AXIS));
		descriptionWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionWrapper.add(new CustomLabel("Product Description:", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD));
		CustomTextArea descriptionLabel = new CustomTextArea(4, 0);
		descriptionLabel.setText(itemDesc);
		descriptionLabel.setDisplayOnly(true);
		descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionWrapper.add(descriptionLabel);
		
		topContent.add(itemInfoWrapper);
		topContent.add(Box.createVerticalStrut(10));
		
		// Hide price for Trading/Sharing views
		if (MarketplaceTabMode.MARKETPLACE.equals(tabMode)) {
			topContent.add(priceLabel);
			topContent.add(Box.createVerticalStrut(15));
		}
		
		topContent.add(initiatorLabel);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(locationWrapper);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(quantityWrapper);
		topContent.add(Box.createVerticalStrut(10));
		if (actionRow != null) topContent.add(actionRow);
		topContent.add(Box.createVerticalStrut(40));
		topContent.add(descriptionWrapper);
		
		wrapper.add(topContent, BorderLayout.NORTH);
		return wrapper;
	}

	private CustomPanel buildTabActionRow() {
		CustomPanel row = new CustomPanel();
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

		paymentMethodComboBox = null;
		checkoutButton = null;

		switch (tabMode) {
		case MARKETPLACE: 
			paymentMethodComboBox = new CustomComboBox<>(new String[] { "Select a Payment Method", "Cash", "GCash", "Maya", "Mastercard", "Visa" });
			paymentMethodComboBox.setCustomSize(250, 35);
			paymentMethodComboBox.addActionListener(e -> updateCheckoutButtonLabel());
			row.add(paymentMethodComboBox);
			row.add(Box.createHorizontalGlue());

			checkoutButton = new CustomButton("Checkout", 10);
			checkoutButton.setDefaultColor(Brand.GREEN);
			checkoutButton.setHoverColor(Brand.GREEN.darker());
			checkoutButton.setPadding(20, 5);
			checkoutButton.addActionListener(e -> {
				String payment = getSelectedPaymentMethod();
				if (payment.isBlank() || payment.equalsIgnoreCase("Select a Payment Method")) {
					JOptionPane.showMessageDialog(this, "Please select a payment method before proceeding to checkout.", "Payment Method Required", JOptionPane.WARNING_MESSAGE);
					return;
				}
				checkoutView = true;
				refreshItemView();
			});
			row.add(checkoutButton);
			updateCheckoutButtonLabel();
			break;
		case SHARING: 
			primaryActionButton = new CustomButton("Request to Borrow", 10);
			primaryActionButton.setDefaultColor(Brand.PRIMARY_COLOR);
			primaryActionButton.setHoverColor(Brand.PRIMARY_COLOR.darker());
			primaryActionButton.setPadding(20, 5);
			row.add(primaryActionButton);
			break;
		case TRADING: 
			primaryActionButton = new CustomButton("Propose Trade", 10);
			primaryActionButton.setDefaultColor(Brand.PRIMARY_COLOR);
			primaryActionButton.setHoverColor(Brand.PRIMARY_COLOR.darker());
			primaryActionButton.setPadding(20, 5);

			secondaryActionButton = new CustomButton("View Offers", 10);
			secondaryActionButton.setDefaultColor(Color.DARK_GRAY);
			secondaryActionButton.setHoverColor(Color.DARK_GRAY.darker());
			secondaryActionButton.setPadding(20, 5);

			row.add(primaryActionButton);
			row.add(Box.createHorizontalStrut(10));
			row.add(secondaryActionButton);
			break;
		}
		return row;
	}
	
	private CustomPanel viewItemCheckout() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout());
		CustomPanel topContent = new CustomPanel();
		topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));
		
<<<<<<< HEAD
		int unitPrice = selectedItem != null ? selectedItem.price : 0;
		int total = getSelectedItemTotal();
=======
		double unitPrice = selectedItem != null ? selectedItem.price : 0;
		double total = getSelectedItemTotal();
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		
		CustomPanel itemInfoWrapper = new CustomPanel(new BorderLayout(10, 0));
		itemInfoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		itemInfoWrapper.add(new CustomLabel(selectedItem != null && selectedItem.itemName != null ? selectedItem.itemName : "Item", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD), BorderLayout.CENTER);
		
		CustomLabel categoryLabel = new CustomLabel(selectedItem != null && selectedItem.category != null ? selectedItem.category : "", Brand.HEADER4_TEXT_SIZE, FontStyle.REGULAR);
		categoryLabel.setForeground(Color.GRAY);
		itemInfoWrapper.add(categoryLabel, BorderLayout.EAST);
		
		topContent.add(itemInfoWrapper);

		if (!MarketplaceTabMode.MARKETPLACE.equals(tabMode)) {
			CustomLabel msg = new CustomLabel("Checkout is not available in this tab.");
			msg.setForeground(Color.GRAY);
			topContent.add(Box.createVerticalStrut(10));
			topContent.add(msg);
		} else {
			topContent.add(Box.createVerticalStrut(15));
			topContent.add(new CustomLabel("Checkout Summary", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD));
			topContent.add(Box.createVerticalStrut(10));

			CustomPanel details = new CustomPanel();
			details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
			details.setAlignmentX(Component.LEFT_ALIGNMENT);

			details.add(new CustomLabel("Quantity: " + getSelectedQuantity(), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			String payment = getSelectedPaymentMethod();
			details.add(new CustomLabel("Payment Method: " + (payment.isBlank() ? "(not selected)" : payment), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(10));
<<<<<<< HEAD

			details.add(new CustomLabel("Pickup Location: " + (selectedItem != null && selectedItem.pickupArea != null ? selectedItem.pickupArea : ""), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Pickup Date: " + formatDisplayDate(selectedItem != null ? selectedItem.pickupDate : null), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
=======
			
			String pickupDays = selectedItem != null && selectedItem.pickupDays != null ? selectedItem.pickupDays.replace(",", ", ") : "";

			details.add(new CustomLabel("Pickup Location: " + (selectedItem != null && selectedItem.pickupArea != null ? selectedItem.pickupArea : ""), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Pickup Days: " + pickupDays, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Pickup Time: " + (selectedItem != null && selectedItem.pickupTime != null ? selectedItem.pickupTime : ""), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(15));

<<<<<<< HEAD
			details.add(new CustomLabel("Unit Price: " + (unitPrice > 0 ? ("P" + unitPrice) : "Free"), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Total: " + (total > 0 ? ("P" + total) : "Free"), Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD));
			
			details.add(Box.createVerticalStrut(20));
			
			CustomButton payButton = new CustomButton(total > 0 ? "Pay P" + total : "Confirm Order", 10);
=======
			details.add(new CustomLabel("Unit Price: " + (unitPrice > 0 ? ("P" + String.format("%.0f", unitPrice)) : "Free"), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Total: " + (total > 0 ? ("P" + String.format("%.0f", total)) : "Free"), Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD));
			
			details.add(Box.createVerticalStrut(20));
			
			CustomButton payButton = new CustomButton(total > 0 ? "Pay P" + String.format("%.0f", total) : "Confirm Order", 10);
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
			payButton.setDefaultColor(Brand.GREEN);
			payButton.setHoverColor(Brand.GREEN.darker());
			payButton.setPadding(15, 20, 15, 20);
			payButton.addActionListener(e -> {
				Object[] options = {"Proceed", "Cancel"};
				int choice = JOptionPane.showOptionDialog(this, "Are you sure you want to finalize this transaction?", "Confirm Payment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (choice == JOptionPane.YES_OPTION) {
					JOptionPane.showMessageDialog(this, "Transaction completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
					restoreView();
				}
			});
			
			details.add(payButton);
			topContent.add(details);
		}
		
		wrapper.add(topContent, BorderLayout.NORTH);
		return wrapper;
	}
	
	public View getView() { return view; }

	public void setView(View view) {
		if (this.view != view) {
			this.view = view;
			restoreView();
		}
	}

<<<<<<< HEAD
	public void setItemViewStateListener(Consumer<Boolean> listener) {
=======
	public void setItemViewStateListener(ViewStateListener listener) {
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		this.itemViewStateListener = listener;
	}
}