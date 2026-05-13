package marketplace;

import java.awt.*;
import java.sql.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import components.*;
import database.DatabaseManager;
import utils.*;

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

	private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM d, yyyy");
	
	private CustomSpinner quantitySpinner = new CustomSpinner(new SpinnerNumberModel(1, 1, 10, 1));
	private ChangeListener quantityChangeListener = e -> updateCheckoutButtonLabel();
	
	private CustomComboBox<String> paymentMethodComboBox;
	private CustomButton checkoutButton;
	private CustomButton primaryActionButton;
	private CustomButton secondaryActionButton;
	private Consumer<Boolean> itemViewStateListener;
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
			itemViewStateListener.accept(false);
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
				ResultSet rs = stmt.executeQuery("SELECT * FROM ITEMS")) {

			while (rs.next()) {
				ItemRecord item = new ItemRecord();
				item.itemId = rs.getInt("item_id");
				item.ownerId = rs.getInt("owner_id");
				item.itemName = rs.getString("item_name");
				item.itemQuantity = rs.getInt("item_quantity");
				item.description = rs.getString("description");
				item.category = rs.getString("category");
				item.itemCondition = rs.getString("item_condition");
				item.price = rs.getInt("price");
				item.availabilityStatus = rs.getString("availability_status");
				item.maximumBorrowDays = rs.getInt("maximum_borrow_days");
				item.desiredItem = rs.getString("desired_item");
				item.dateListed = rs.getTimestamp("date_listed");
				item.pickupArea = rs.getString("pickup_area");
				item.pickupTime = rs.getString("pickup_time");
				item.pickupDate = rs.getDate("pickup_date");
				item.action = rs.getString("action");

				String normalizedAction = item.action == null ? "" : item.action.trim().toLowerCase();
				if ("sharing".equals(normalizedAction)) {
					sharingItems.add(item);
				} else if ("marketplace".equals(normalizedAction)) {
					marketplaceItems.add(item);
				} else if ("barter trading".equals(normalizedAction)) {
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
		case SHARING:
			return "Borrow Item";
		case TRADING:
			return "Trade Item";
		case MARKETPLACE:
		default:
			return "Buy Item";
		}
	}

	private String formatDisplayDate(Date date) {
		if (date == null) {
			return "";
		}
		return displayDateFormat.format(date);
	}

	private int getSelectedQuantity() {
		Object value = quantitySpinner.getValue();
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return 1;
	}

	private String getSelectedPaymentMethod() {
		if (paymentMethodComboBox == null) {
			return "";
		}
		Object selected = paymentMethodComboBox.getSelectedItem();
		if (selected == null) {
			return "";
		}
		String s = selected.toString();
		if ("Select a Payment Method".equalsIgnoreCase(s)) {
			return "";
		}
		return s;
	}

	private int getSelectedItemTotal() {
		int unitPrice = selectedItem != null ? selectedItem.price : 0;
		return unitPrice * getSelectedQuantity();
	}

	private void updateCheckoutButtonLabel() {
		if (checkoutButton == null) {
			return;
		}
		int total = getSelectedItemTotal();
		checkoutButton.setText("Checkout " + (total > 0 ? ("P" + total) : "Free"));
	}

	private List<ItemRecord> getItemsForCurrentTab() {
		switch (tabMode) {
		case SHARING:
			return sharingItems;
		case TRADING:
			return tradingItems;
		case MARKETPLACE:
		default:
			return marketplaceItems;
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

	private void showItemView() {
		checkoutView = false;
		removeAll();
		viewItem();
		if (itemViewStateListener != null) {
			itemViewStateListener.accept(true);
		}
		revalidate();
		repaint();
	}

	private void initGridView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new GridLayout(GRID_ROWS, GRID_COLUMNS, 10, 10));

		List<ItemRecord> items = getItemsForCurrentTab();
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
			for (int i = start; i < endExclusive; i++) {
				ItemRecord r = items.get(i);
				Item card = new Item(View.GRID);
				card.bind(r);
				wrapper.add(card);
				added++;
			}
			for (int i = added; i < GRID_PAGE_SIZE; i++) {
				wrapper.add(new CustomPanel());
			}
		}
		
		add(wrapper, BorderLayout.CENTER);
	}
	
	private void initListView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBackground(Color.WHITE);
		wrapper.setPadding(10, 20);

		List<ItemRecord> items = getItemsForCurrentTab();
		if (items.isEmpty()) {
			addEmptyState(wrapper);
		} else {
			int start = currentPage * LIST_PAGE_SIZE;
			if (start >= items.size()) {
				currentPage = 0;
				start = 0;
			}
			int endExclusive = Math.min(start + LIST_PAGE_SIZE, items.size());
			for (int i = start; i < endExclusive; i++) {
				ItemRecord r = items.get(i);
				Item card = new Item(View.LIST);
				card.bind(r);
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

	private int getPageSize() {
		return View.GRID.equals(view) ? GRID_PAGE_SIZE : LIST_PAGE_SIZE;
	}

	private int getLastPageIndex() {
		List<ItemRecord> items = getItemsForCurrentTab();
		int pageSize = getPageSize();
		if (items.isEmpty()) {
			return 0;
		}
		return Math.max(0, (items.size() - 1) / pageSize);
	}

	private void goToPage(int page) {
		int last = getLastPageIndex();
		if (page < 0) {
			page = 0;
		} else if (page > last) {
			page = last;
		}
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
		JLabel imgLabel = new JLabel(IconLoader.loadAndScaleIcon("/resources/images/umak_img.jpg", 400, 500)); 
		imgLabel.setPreferredSize(new Dimension(400, 500));
		imgLabel.setMaximumSize(new Dimension(400, 500));
		imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		westWrapper.add(imgLabel);
		
		CustomPanel centerWrapper = checkoutView ? viewItemCheckout() : viewItemInfo();
		
		wrapper.add(westWrapper, BorderLayout.WEST);
		wrapper.add(centerWrapper, BorderLayout.CENTER);
		
		add(wrapper, BorderLayout.CENTER);
	}

	private void refreshItemView() {
		removeAll();
		viewItem();
		if (itemViewStateListener != null) {
			itemViewStateListener.accept(true);
		}
		revalidate();
		repaint();
	}
	
	private CustomPanel viewItemInfo() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout());
		
		CustomPanel topContent = new CustomPanel();
		topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));
		
		String itemName = selectedItem != null && selectedItem.itemName != null ? selectedItem.itemName : "Item";
		String itemCategory = selectedItem != null && selectedItem.category != null ? selectedItem.category : "";
		int itemPrice = selectedItem != null ? selectedItem.price : 0;
		String itemDesc = selectedItem != null && selectedItem.description != null ? selectedItem.description : "";
		int stock = selectedItem != null ? selectedItem.itemQuantity : 0;
		String pickupArea = selectedItem != null && selectedItem.pickupArea != null ? selectedItem.pickupArea : "";
		String pickupTime = selectedItem != null && selectedItem.pickupTime != null ? selectedItem.pickupTime : "";
		Date pickupDate = selectedItem != null ? selectedItem.pickupDate : null;
		
		CustomPanel itemInfoWrapper = new CustomPanel(new BorderLayout(10, 0));
		itemInfoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		CustomLabel nameLabel = new CustomLabel(itemName, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD);
		CustomLabel categoryLabel = new CustomLabel(itemCategory, Brand.HEADER4_TEXT_SIZE, FontStyle.REGULAR);
		categoryLabel.setForeground(Color.GRAY);
		itemInfoWrapper.add(nameLabel, BorderLayout.CENTER);
		itemInfoWrapper.add(categoryLabel, BorderLayout.EAST);
		
		CustomLabel priceLabel = new CustomLabel(itemPrice > 0 ? ("P" + itemPrice) : "Free", Brand.HEADER1_TEXT_SIZE, FontStyle.BOLD);
		priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		priceLabel.setForeground(Brand.PRIMARY_COLOR);

		CustomLabel initiatorLabel = new CustomLabel("LINDSAY MARY BALABIS", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		initiatorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		CustomPanel locationWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		locationWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/location.png", 15, 15, Color.GRAY)));
		locationWrapper.add(new CustomLabel(pickupArea, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(formatDisplayDate(pickupDate), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(pickupTime, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		
		CustomPanel quantityWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		quantityWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

		int maxQty = Math.max(1, stock);
		SpinnerNumberModel spinnerModel = (SpinnerNumberModel) quantitySpinner.getModel();
		spinnerModel.setMaximum(maxQty);
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
		topContent.add(priceLabel);
		topContent.add(Box.createVerticalStrut(15));
		topContent.add(initiatorLabel);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(locationWrapper);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(quantityWrapper);
		topContent.add(Box.createVerticalStrut(10));
		if (actionRow != null) {
			topContent.add(actionRow);
		}
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
		primaryActionButton = null;
		secondaryActionButton = null;

		switch (tabMode) {
		case MARKETPLACE: {
			paymentMethodComboBox = new CustomComboBox<>(
					new String[] { "Select a Payment Method", "Cash", "GCash", "Pay Maya", "Visa", "MasterCard" });
			paymentMethodComboBox.setCustomSize(250, 35);
			paymentMethodComboBox.addActionListener(e -> updateCheckoutButtonLabel());
			row.add(paymentMethodComboBox);
			row.add(Box.createHorizontalGlue());

			checkoutButton = new CustomButton("Checkout", 10);
			checkoutButton.setDefaultColor(Brand.GREEN);
			checkoutButton.setHoverColor(Brand.GREEN.darker());
			checkoutButton.setPadding(20, 5);
			checkoutButton.addActionListener(e -> {
				checkoutView = true;
				refreshItemView();
			});
			row.add(checkoutButton);
			updateCheckoutButtonLabel();
			break;
		}
		case SHARING: {
			primaryActionButton = new CustomButton("Request to Borrow", 10);
			primaryActionButton.setDefaultColor(Brand.PRIMARY_COLOR);
			primaryActionButton.setHoverColor(Brand.PRIMARY_COLOR.darker());
			primaryActionButton.setPadding(20, 5);

			secondaryActionButton = new CustomButton("Return Item", 10);
			secondaryActionButton.setDefaultColor(Color.DARK_GRAY);
			secondaryActionButton.setHoverColor(Color.DARK_GRAY.darker());
			secondaryActionButton.setPadding(20, 5);

			row.add(primaryActionButton);
			row.add(Box.createHorizontalStrut(10));
			row.add(secondaryActionButton);
			break;
		}
		case TRADING: {
			primaryActionButton = new CustomButton("Propose Trade", 10);
			primaryActionButton.setDefaultColor(Brand.PRIMARY_COLOR);
			primaryActionButton.setHoverColor(Brand.PRIMARY_COLOR.darker());
			primaryActionButton.setPadding(20, 5);

			row.add(primaryActionButton);
			row.add(Box.createHorizontalStrut(10));
			row.add(secondaryActionButton);
			row.add(Box.createHorizontalStrut(10));
			break;
		}
		default:
			return null;
		}

		return row;
	}
	
	private CustomPanel viewItemCheckout() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout());
		
		CustomPanel topContent = new CustomPanel();
		topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));
		
		String itemName = selectedItem != null && selectedItem.itemName != null ? selectedItem.itemName : "Item";
		String itemCategory = selectedItem != null && selectedItem.category != null ? selectedItem.category : "";
		int unitPrice = selectedItem != null ? selectedItem.price : 0;
		int qty = getSelectedQuantity();
		String payment = getSelectedPaymentMethod();
		String pickupArea = selectedItem != null && selectedItem.pickupArea != null ? selectedItem.pickupArea : "";
		String pickupTime = selectedItem != null && selectedItem.pickupTime != null ? selectedItem.pickupTime : "";
		Date pickupDate = selectedItem != null ? selectedItem.pickupDate : null;
		int total = getSelectedItemTotal();
		
		CustomPanel itemInfoWrapper = new CustomPanel(new BorderLayout(10, 0));
		itemInfoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		CustomLabel nameLabel = new CustomLabel(itemName, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD);
		CustomLabel categoryLabel = new CustomLabel(itemCategory, Brand.HEADER4_TEXT_SIZE, FontStyle.REGULAR);
		categoryLabel.setForeground(Color.GRAY);
		itemInfoWrapper.add(nameLabel, BorderLayout.CENTER);
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

			details.add(new CustomLabel("Quantity: " + qty, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Payment Method: " + (payment.isBlank() ? "(not selected)" : payment),
					Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(10));

			details.add(new CustomLabel("Pickup Location: " + pickupArea, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Pickup Date: " + formatDisplayDate(pickupDate), Brand.SUBHEADER_TEXT_SIZE,
					FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Pickup Time: " + pickupTime, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(15));

			details.add(new CustomLabel("Unit Price: " + (unitPrice > 0 ? ("P" + unitPrice) : "Free"),
					Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(Box.createVerticalStrut(5));
			details.add(new CustomLabel("Total: " + (total > 0 ? ("P" + total) : "Free"),
					Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD));
			
			details.add(Box.createVerticalStrut(20));
			
			CustomButton payButton = new CustomButton(total > 0 ? "Pay P" + total : "Confirm Order", 10);
			payButton.setDefaultColor(Brand.GREEN);
			payButton.setHoverColor(Brand.GREEN.darker());
			payButton.setPadding(15, 20, 15, 20);
			payButton.addActionListener(e -> {
				restoreView();
			});
			
			details.add(payButton);
			topContent.add(details);
		}
		
		wrapper.add(topContent, BorderLayout.NORTH);
		return wrapper;
	}
	
	public View getView() {
		return view;
	}

	public void setView(View view) {
		if (this.view != view) {
			this.view = view;
			restoreView();
		}
	}

	public void setItemViewStateListener(Consumer<Boolean> listener) {
		this.itemViewStateListener = listener;
	}
	
	private class Item extends CustomPanel {
		private static final long serialVersionUID = 1L;
		private String image = "/resources/images/umak_img.jpg";
		private String name;
		private String description;
		private String initiator;
		private Category category;
		private Condition condition;
		private Availability availability;
		private int price;
		private CustomButton actionButton;
		private ItemRecord record;
		
		private CustomLabel nameLabel;
		private CustomLabel priceLabel;
		private CustomLabel categoryLabel;
		private CustomLabel conditionLabel;
		
		public Item(View view) {
			if (View.GRID.equals(view)) {
				initCardLayout();
			} else {
				initListLayout();
			}
		}
		
		private void initCardLayout() {
			setBackground(Color.WHITE);
			setLayout(new BorderLayout(0, 10));
			setBorder(1, Color.LIGHT_GRAY);
			setPadding(10);
			setRadius(20);
			
			CustomPanel imgPanel = new CustomPanel() {
				private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : null;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (imgId != null) {
						g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
						int h = (int)(getWidth() * ((double)imgId.getHeight(null) / imgId.getWidth(null)));
						int y = (getHeight() - h) / 2;
						g.drawImage(imgId, 0, y, getWidth(), h, this);
					}
				}
			};
			imgPanel.setPreferredSize(new Dimension(200, 150));
			imgPanel.setOpaque(false);
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			
			CustomPanel infoWrapper = new CustomPanel();
			infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
			nameLabel = new CustomLabel(name != null ? name : "Item Name", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
			CustomLabel initiatorLabel = new CustomLabel(initiator != null ? "Initiator: " + initiator : "N/A");
			priceLabel = new CustomLabel(price > 0 ? "P" + price : "Free", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
			initiatorLabel.setForeground(Color.GRAY);
			infoWrapper.add(nameLabel);
			infoWrapper.add(initiatorLabel);
			
			CustomPanel actionWrapper = new CustomPanel(new BorderLayout());
			actionWrapper.add(priceLabel, BorderLayout.WEST);
			actionButton = new CustomButton(getPrimaryActionText());
			actionButton.setPadding(5, 10, 5, 10);
			actionButton.setRadius(15);
			actionButton.addActionListener(e -> {
				selectedItem = record;
				showItemView();
			});
			actionWrapper.add(actionButton, BorderLayout.EAST);
			
			contentPanel.add(infoWrapper, BorderLayout.NORTH);
			contentPanel.add(actionWrapper, BorderLayout.SOUTH);
			
			add(imgPanel, BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);
		}
		
		private void initListLayout() {
			setBackground(Color.WHITE);
			setLayout(new BorderLayout(10, 0));
			setBorder(1, Color.LIGHT_GRAY);
			setPadding(10);
			setRadius(20);
			
			CustomPanel imgPanel = new CustomPanel() {
				private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : null;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (imgId != null) {
						g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
						g.drawImage(imgId, 0, 0, getWidth(), getHeight(), this);
					}
				}
			};
			imgPanel.setPreferredSize(new Dimension(75, 75));
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel centerWrapper = new CustomPanel();
			CustomPanel textWrapper = new CustomPanel();
			CustomPanel eastWrapper = new CustomPanel(new GridBagLayout());

			centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
			nameLabel = new CustomLabel(name != null ? name : "Item Name", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
			CustomLabel descriptionLabel = new CustomLabel(description != null ? description : "Item Description", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setForeground(Color.GRAY);
			
			textWrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			textWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			categoryLabel = new CustomLabel("Category", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
			conditionLabel = new CustomLabel("Condition", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
			textWrapper.add(categoryLabel);
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(conditionLabel);
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(initiator != null ? new CustomLabel("by " + initiator, Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR) : new CustomLabel("by Someone", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR));
			
			centerWrapper.add(Box.createVerticalGlue());
			centerWrapper.add(nameLabel);
			centerWrapper.add(descriptionLabel);
			centerWrapper.add(textWrapper);
			centerWrapper.add(Box.createVerticalGlue());
			
			eastWrapper.setLayout(new BoxLayout(eastWrapper, BoxLayout.Y_AXIS));
			eastWrapper.add(Box.createVerticalGlue());
			actionButton = new CustomButton(getPrimaryActionText());
			actionButton.setPadding(5, 10, 5, 10);
			actionButton.setRadius(15);
			actionButton.addActionListener(e -> {
				selectedItem = record;
				showItemView();
			});
			eastWrapper.add(actionButton);
			eastWrapper.add(Box.createVerticalGlue());
			
			contentPanel.add(centerWrapper, BorderLayout.CENTER);
			contentPanel.add(eastWrapper, BorderLayout.EAST);
			add(imgPanel, BorderLayout.WEST);
			add(contentPanel, BorderLayout.CENTER);
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public Category getCategory() {
			return category;
		}

		public Condition getCondition() {
			return condition;
		}

		public Availability getAvailability() {
			return availability;
		}

		public void setName(String name) {
			this.name = name;
			if (nameLabel != null) {
				nameLabel.setText(name);
			}
			revalidate();
			repaint();
		}

		public void setDescription(String description) {
			this.description = description;
			revalidate();
			repaint();
		}

		public void setCategory(Category category) {
			this.category = category;
			revalidate();
			repaint();
		}

		public void setCondition(Condition condition) {
			this.condition = condition;
			revalidate();
			repaint();
		}

		public void setAvailability(Availability availability) {
			this.availability = availability;
			revalidate();
			repaint();
		}

		public void setPrice(int price) {
			this.price = price;
			if (priceLabel != null) {
				priceLabel.setText(price > 0 ? "P" + price : "Free");
			}
			revalidate();
			repaint();
		}

		public void setCategoryText(String categoryText) {
			if (categoryLabel != null) {
				categoryLabel.setText(categoryText != null ? categoryText : "");
			}
			revalidate();
			repaint();
		}

		public void setConditionText(String conditionText) {
			if (conditionLabel != null) {
				conditionLabel.setText(conditionText != null ? conditionText : "");
			}
			revalidate();
			repaint();
		}

		public void bind(ItemRecord record) {
			this.record = record;
			if (record == null) {
				return;
			}
			setName(record.itemName);
			setDescription(record.description);
			setPrice(record.price);
			setCategoryText(record.category);
			setConditionText(record.itemCondition);
		}
	}

	private static class ItemRecord {
		int itemId;
		int ownerId;
		String itemName;
		int itemQuantity;
		String description;
		String category;
		String itemCondition;
		int price;
		String availabilityStatus;
		int maximumBorrowDays;
		String desiredItem;
		Timestamp dateListed;
		String pickupArea;
		String pickupTime;
		Date pickupDate;
		String action;
	}

}