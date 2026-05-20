package marketplace;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import components.*;
import database.DatabaseManager;
import enums.MarketplaceTabMode;
import enums.View;
import utils.*;
import database.*;

interface ViewStateListener {
	void onViewStateChanged(boolean isItemView);
}

interface ItemActionListener {
	void onItemAction(ItemRecord record);
}

public class ItemPanel extends CustomPanel {
	
	private static final long serialVersionUID = 1L;
	private View view = View.GRID;
	private MarketplaceTabMode tabMode = MarketplaceTabMode.MARKETPLACE;
	private CustomButton backButton;

	private UserRecord user;

	private final List<ItemRecord> marketplaceItems = new ArrayList<>();
	private final List<ItemRecord> marketplaceWithdrawnItems = new ArrayList<>();

	private final List<ItemRecord> sharingItems = new ArrayList<>();
	private final List<ItemRecord> sharingWithdrawnItems = new ArrayList<>();
	private final List<ItemRecord> sharingApprovalItems = new ArrayList<>();
	private final List<ItemRecord> sharingReturnItems = new ArrayList<>();

	private final List<ItemRecord> tradeItems = new ArrayList<>();
	private final List<ItemRecord> tradeWithdrawnItems = new ArrayList<>();
	private final List<ItemRecord> tradeApprovalItems = new ArrayList<>();

	private int currentPage = 0;
	private static final int GRID_COLUMNS = 4;
	private static final int GRID_ROWS = 2;
	private static final int GRID_PAGE_SIZE = GRID_COLUMNS * GRID_ROWS;
	private static final int LIST_PAGE_SIZE = 8;

	private ItemRecord selectedItem;

	private CustomSpinner quantitySpinner = new CustomSpinner(new SpinnerNumberModel(1, 1, 10, 1));
	private ChangeListener quantityChangeListener = e -> updateCheckoutButtonLabel();

	private CustomComboBox<String> paymentMethodComboBox;
	private CustomButton checkoutButton;
	private CustomButton primaryActionButton;
	private CustomButton secondaryActionButton;
	private ViewStateListener itemViewStateListener;
	private boolean checkoutView = false;
	private boolean isItemViewActive = false; 

	private final String DB_URL = DatabaseManager.getURL();
	private final String USER = DatabaseManager.getUser();
	private final String PASSWORD = DatabaseManager.getPassword();

	private String currentSearchTarget = "All";
	private String currentSearchText = "";	
	private String currentCategoryFilter = "All Categories";
	private String currentConditionFilter = "All Conditions";
	private String currentPriceFilter = "All Prices";

	public ItemPanel(UserRecord user) {
		this(user, MarketplaceTabMode.MARKETPLACE);
	}

	public ItemPanel(UserRecord user, MarketplaceTabMode tabMode) {
		this.user = user;
		setBackground(Color.WHITE);
		setPadding(20);
		setLayout(new BorderLayout(20, 20));
		
		if (tabMode != null) {
			this.tabMode = tabMode;
		}

		fetchItemData();
		restoreView();
	}

	public void refreshPanel() {
		SwingUtilities.invokeLater(() -> {
			fetchItemData();
			
			if (isItemViewActive && selectedItem != null) {
				boolean found = false;
				for (ItemRecord item : getItemsForCurrentTab()) {
					if (item.itemId == selectedItem.itemId) {
						selectedItem = item; 
						found = true;
						break;
					}
				}
				
				if (found) {
					refreshItemView(); 
				} else {
					restoreView(); 
				}
			} else {
				restoreView();
			}
			
			Window window = SwingUtilities.getWindowAncestor(this);
			if (window != null) {
				window.revalidate();
				window.repaint();
			}
		});
	}

	public void fetchItemData() {
	    marketplaceItems.clear();
	    marketplaceWithdrawnItems.clear();
	    sharingItems.clear();
	    sharingWithdrawnItems.clear();
	    sharingApprovalItems.clear();
	    sharingReturnItems.clear();
	    tradeItems.clear();
	    tradeWithdrawnItems.clear();
	    tradeApprovalItems.clear();

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
	            Statement stmt = conn.createStatement();
	            ResultSet rs = stmt.executeQuery(
	                    "SELECT i.*, " +
	                    "  CASE WHEN (i.action LIKE '%_Approval' OR i.action LIKE '%_Return') AND u_req.user_id IS NOT NULL THEN u_req.first_name ELSE u.first_name END AS initiator_firstname, " +
	                    "  CASE WHEN (i.action LIKE '%_Approval' OR i.action LIKE '%_Return') AND u_req.user_id IS NOT NULL THEN u_req.last_name ELSE u.last_name END AS initiator_lastname, " +
	                    "  CASE WHEN (i.action LIKE '%_Approval' OR i.action LIKE '%_Return') AND u_req.user_id IS NOT NULL THEN u_req.karma_score ELSE u.karma_score END AS initiator_karmascore " +
	                    "FROM items i " +
	                    "LEFT JOIN users u ON i.owner_id = u.user_id " +
	                    "LEFT JOIN (" +
	                    "  SELECT t1.item_id, t1.borrower_id " +
	                    "  FROM transactions t1 " +
	                    "  WHERE t1.transaction_id = (SELECT MAX(transaction_id) FROM transactions t2 WHERE t2.item_id = t1.item_id)" +
	                    ") latest_t ON latest_t.item_id = i.item_id " +
	                    "LEFT JOIN users u_req ON latest_t.borrower_id = u_req.user_id " +
	                    "WHERE i.items_is_archived = 0")) {

	        while (rs.next()) {
	            ItemRecord item = new ItemRecord();
	            item.itemId = rs.getInt("item_id");
	            item.ownerId = rs.getInt("owner_id");
	            item.itemName = rs.getString("item_name");
	            item.itemQuantity = rs.getInt("item_quantity");
	            item.description = rs.getString("description");

	            item.itemImage = rs.getString("item_image");
	            if (item.itemImage == null || item.itemImage.trim().isEmpty()
	                    || item.itemImage.contains("axolotl.jpg")) {
	                item.itemImage = "/resources/images/umak_img.jpg";
	            }

	            item.category = rs.getString("category");
	            item.condition = rs.getString("condition");
	            item.price = rs.getInt("price");
	            item.availabilityStatus = rs.getString("availability_status");
	            item.maximumBorrowDays = rs.getInt("maximum_borrow_days");
	            item.desiredItem = rs.getString("desired_item");
	            item.dateListed = rs.getTimestamp("date_listed");
	            item.pickupArea = rs.getString("pickup_area");
	            item.pickupTime = rs.getString("pickup_time");
	            item.pickupDays = rs.getString("pickup_days");
	            item.action = rs.getString("action");
	            item.itemsIsArchived = rs.getBoolean("items_is_archived");
	            item.itemsArchivedAt = rs.getTimestamp("items_archived_at");
	            item.initiatorFirstName = rs.getString("initiator_firstname");
	            item.initiatorLastName = rs.getString("initiator_lastname");
	            item.initiatorKarmaScore = rs.getInt("initiator_karmascore");

	            String normalizedAction = item.action == null ? "" : item.action.trim().toLowerCase();

	            if (normalizedAction.startsWith("sharing")) {
	                item.price = 0;
	            }

	            boolean isAvailable = "Available".equalsIgnoreCase(item.availabilityStatus) && item.itemQuantity > 0;

	            switch (normalizedAction) {
	            case "marketplace":
	                if (isAvailable) {
	                	marketplaceItems.add(item);
	                }
	            case "marketplace_withdrawn":
	                marketplaceWithdrawnItems.add(item);
	                break;
	            case "sharing":
	                if (isAvailable) {
	                	sharingItems.add(item);
	                }
	            case "sharing_withdrawn":
	                sharingWithdrawnItems.add(item);
	                break;
	            case "sharing_approval":
	                sharingApprovalItems.add(item);
	                sharingWithdrawnItems.add(item);
	                break;
	            case "sharing_return":
	                sharingReturnItems.add(item);
	                sharingWithdrawnItems.add(item);
	                break;
	            case "trade":
	                if (isAvailable) {
	                	tradeItems.add(item);
	                }
	            case "trade_withdrawn":
	                tradeWithdrawnItems.add(item);
	                break;
	            case "trade_approval":
	                tradeApprovalItems.add(item);
	                tradeWithdrawnItems.add(item);
	                break;
	            default:
	                break;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void restoreView() {
		checkoutView = false;
		isItemViewActive = false; 
		removeAll();
		if (View.GRID.equals(view)) {
			initGridView();
		} else {
			initListView();
		}
		initNav();
		if (itemViewStateListener != null) {
			itemViewStateListener.onViewStateChanged(false);
		}
		revalidate();
		repaint();
	}

	public void applyFilters(String target, String search, String category, String condition, String price) {
		this.currentSearchTarget = target == null ? "All" : target;
		this.currentSearchText = search == null ? "" : search.trim().toLowerCase();
		this.currentCategoryFilter = category == null ? "All Categories" : category;
		this.currentConditionFilter = condition == null ? "All Conditions" : condition;
		this.currentPriceFilter = price == null ? "All Prices" : price;
		this.currentPage = 0;
		restoreView();
	}

	private boolean updateItemStatusInDB(int itemId, String newAction, String newStatus) {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement("UPDATE items SET action = ?, availability_status = ? WHERE item_id = ?")) {
			pstmt.setString(1, newAction);
			pstmt.setString(2, newStatus);
			pstmt.setInt(3, itemId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	private String getPrimaryActionText(ItemRecord record) {
		if (tabMode == MarketplaceTabMode.MARKETPLACE_WITHDRAWN || tabMode == MarketplaceTabMode.SHARING_WITHDRAWN
				|| tabMode == MarketplaceTabMode.TRADE_WITHDRAWN) {

			String action = record.action != null ? record.action.trim().toLowerCase() : "";

			if (action.endsWith("_approval") || action.endsWith("_return")) {
				return "Pending Processing";
			} else {
				return "Manage Item";
			}
		}

		switch (tabMode) {
		case SHARING:
			return "Borrow Item";
		case TRADE:
			return "Trade Item";
		default:
			return "Buy Item";
		}
	}

	private int getSelectedQuantity() {
		Object value = quantitySpinner.getValue();
		return value instanceof Number ? ((Number) value).intValue() : 1;
	}

	private String getSelectedPaymentMethod() {
		if (paymentMethodComboBox == null || paymentMethodComboBox.getSelectedItem() == null)
			return "";
		String s = paymentMethodComboBox.getSelectedItem().toString();
		return "Select a Payment Method".equalsIgnoreCase(s) ? "" : s;
	}

	private double getSelectedItemTotal() {
		return (selectedItem != null ? selectedItem.price : 0) * getSelectedQuantity();
	}

	private void updateCheckoutButtonLabel() {
		if (checkoutButton != null) {
			double total = getSelectedItemTotal();
			checkoutButton.setText("Checkout " + (total > 0 ? ("P" + String.format("%.0f", total)) : "Free"));
		}
	}

	private List<ItemRecord> filterByUser(List<ItemRecord> list) {
		List<ItemRecord> filtered = new ArrayList<>();
		for (ItemRecord item : list) {
			if (user != null && item.ownerId == user.user_id) {
				filtered.add(item); 
			}
		}
		return filtered;
	}

	private List<ItemRecord> getItemsForCurrentTab() {
		List<ItemRecord> combined = new ArrayList<>();

		switch (tabMode) {
		case SHARING:
			combined.addAll(sharingItems);
			break;
		case TRADE:
			combined.addAll(tradeItems);
			break;
		case MARKETPLACE:
			combined.addAll(marketplaceItems);
			break;
		case MARKETPLACE_WITHDRAWN:
			combined.addAll(marketplaceWithdrawnItems);
			combined = filterByUser(combined); 
			break;
		case SHARING_WITHDRAWN:
			combined.addAll(sharingWithdrawnItems);
			combined = filterByUser(combined); 
			break;
		case TRADE_WITHDRAWN:
			combined.addAll(tradeWithdrawnItems);
			combined = filterByUser(combined); 
			break;
		default:
			return new ArrayList<>(); 
		}

		List<ItemRecord> filteredList = new ArrayList<>();
		
		for (ItemRecord item : combined) {
			boolean matchSearch = true;
			boolean matchCategory = true;
			boolean matchCondition = true;
			boolean matchPrice = true;

			if (!currentSearchText.isEmpty()) {
				String name = item.itemName != null ? item.itemName.toLowerCase() : "";
				String fName = item.initiatorFirstName != null ? item.initiatorFirstName.toLowerCase() : "";
				String lName = item.initiatorLastName != null ? item.initiatorLastName.toLowerCase() : "";

				if ("Item Name".equals(currentSearchTarget)) {
					if (!name.contains(currentSearchText))
						matchSearch = false; 
				} else if ("Initiator Name".equals(currentSearchTarget)) {
					if (!fName.contains(currentSearchText) && !lName.contains(currentSearchText))
						matchSearch = false; 
				} else {
					if (!name.contains(currentSearchText) && !fName.contains(currentSearchText)
							&& !lName.contains(currentSearchText)) {
						matchSearch = false; 
					}
				}
			}

			if (!"All Categories".equals(currentCategoryFilter)) {
				String itemCat = item.category != null ? item.category.replace("_", " ") : "";
				if (!currentCategoryFilter.equalsIgnoreCase(itemCat)) {
					matchCategory = false; 
				}
			}

			if (!"All Conditions".equals(currentConditionFilter)) {
				String itemCond = item.condition != null ? item.condition.replace("_", " ") : "";
				if (!currentConditionFilter.equalsIgnoreCase(itemCond)) {
					matchCondition = false; 
				}
			}

			if (!"All Prices".equals(currentPriceFilter)) {
				double p = item.price;
				if ("Free".equals(currentPriceFilter) && p != 0) {
					matchPrice = false;
				} else if ("Under P500".equals(currentPriceFilter) && (p <= 0 || p >= 500)) {
					matchPrice = false;
				} else if ("P500 - P1000".equals(currentPriceFilter) && (p < 500 || p > 1000)) {
					matchPrice = false;
				} else if ("Over P1000".equals(currentPriceFilter) && p <= 1000) {
					matchPrice = false;
				}
			}

			if (matchSearch && matchCategory && matchCondition && matchPrice) {
				filteredList.add(item);
			}
		}

		return filteredList;
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
		isItemViewActive = true; 
		removeAll();
		viewItem();
		if (itemViewStateListener != null) {
			itemViewStateListener.onViewStateChanged(true);
		}
		revalidate();
		repaint();
	}

	private void initGridView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new GridLayout(GRID_ROWS, GRID_COLUMNS, 10, 10));

		List<ItemRecord> items = getItemsForCurrentTab();
		boolean showPrice = (tabMode == MarketplaceTabMode.MARKETPLACE
				|| tabMode == MarketplaceTabMode.MARKETPLACE_WITHDRAWN);

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

			ItemActionListener listener = new ItemActionListener() {
				@Override
				public void onItemAction(ItemRecord record) {
					showItemView(record); 
				}
			};

			for (int i = start; i < endExclusive; i++) {
				ItemCard card = new ItemCard(View.GRID, getPrimaryActionText(items.get(i)), items.get(i), showPrice,
						listener);
				wrapper.add(card);
				added++; 
			}
			for (int i = added; i < GRID_PAGE_SIZE; i++)
				wrapper.add(new CustomPanel());
		}
		add(wrapper, BorderLayout.CENTER);
	}

	private void initListView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBackground(Color.WHITE);
		wrapper.setPadding(10, 20);

		List<ItemRecord> items = getItemsForCurrentTab();
		boolean showPrice = (tabMode == MarketplaceTabMode.MARKETPLACE
				|| tabMode == MarketplaceTabMode.MARKETPLACE_WITHDRAWN);

		if (items.isEmpty()) {
			addEmptyState(wrapper);
		} else {
			int start = currentPage * LIST_PAGE_SIZE;
			if (start >= items.size()) {
				currentPage = 0; 
				start = 0;
			}
			int endExclusive = Math.min(start + LIST_PAGE_SIZE, items.size());

			ItemActionListener listener = new ItemActionListener() {
				@Override
				public void onItemAction(ItemRecord record) {
					showItemView(record); 
				}
			};

			for (int i = start; i < endExclusive; i++) {
				ItemCard card = new ItemCard(View.LIST, getPrimaryActionText(items.get(i)), items.get(i), showPrice,
						listener);
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

		List<ItemRecord> items = getItemsForCurrentTab();
		int totalItems = items.size();
		int pageSize = View.GRID.equals(view) ? GRID_PAGE_SIZE : LIST_PAGE_SIZE;
		int startItem = totalItems == 0 ? 0 : (currentPage * pageSize) + 1;
		int endItem = Math.min((currentPage + 1) * pageSize, totalItems);

		CustomLabel showingLabel = new CustomLabel(
				String.format("Showing %d - %d of %d items", startItem, endItem, totalItems), Brand.STANDARD_TEXT_SIZE,
				FontStyle.REGULAR, Color.GRAY);
		navWrapper.add(showingLabel, BorderLayout.WEST);

		CustomButton btnFirst = new CustomButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-left.png", 20, 20, Color.WHITE), 5);
		CustomButton btnPrev = new CustomButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-left.png", 20, 20, Color.WHITE), 5);
		CustomButton btnNext = new CustomButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-right.png", 20, 20, Color.WHITE), 5);
		CustomButton btnLast = new CustomButton(
				IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-right.png", 20, 20, Color.WHITE),
				5);

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

	private int getLastPageIndex() {
		List<ItemRecord> items = getItemsForCurrentTab();
		return items.isEmpty() ? 0
				: Math.max(0, (items.size() - 1) / (View.GRID.equals(view) ? GRID_PAGE_SIZE : LIST_PAGE_SIZE));
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
		CustomPanel header = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		java.awt.event.ActionListener backAction = e -> {
			if (checkoutView) {
				checkoutView = false; 
				refreshItemView();
			} else {
				restoreView(); 
			}
		};

		backButton = new CustomButton(IconLoader.loadAndScaleIcon("/resources/icons/arrow-left.png", 40, 40), 0);
		backButton.setTransparent();
		backButton.addActionListener(backAction);

		CustomButton backLabelButton = new CustomButton("Back", 0);
		backLabelButton.setTransparent();
		if (FontLib.POPPINS_BOLD != null) {
			backLabelButton.setFont(FontLib.POPPINS_BOLD.deriveFont(Brand.HEADER4_TEXT_SIZE));
		} else {
			backLabelButton.setFont(new Font("SansSerif", Font.BOLD, (int) Brand.HEADER4_TEXT_SIZE));
		}
		backLabelButton.addActionListener(backAction);

		header.add(backButton);
		header.add(backLabelButton);

		wrapper.add(header, BorderLayout.NORTH);

		CustomPanel westWrapper = new CustomPanel();

		String imagePath = (selectedItem != null && selectedItem.itemImage != null
				&& !selectedItem.itemImage.trim().isEmpty() && !selectedItem.itemImage.contains("axolotl.jpg"))
						? selectedItem.itemImage
						: "/resources/images/umak_img.jpg";

		ImageIcon loadedIcon = IconLoader.loadAndScaleIcon(imagePath, 400, 500);

		if (loadedIcon == null) {
			loadedIcon = IconLoader.loadAndScaleIcon("/resources/images/umak_img.jpg", 400, 500);
		}

		JLabel imgLabel = new JLabel(loadedIcon != null ? loadedIcon : new ImageIcon());
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
		if (itemViewStateListener != null)
			itemViewStateListener.onViewStateChanged(true);
		revalidate();
		repaint();
	}
	
	private String formatPickupDays(String pickupDays) {
		if (pickupDays == null || pickupDays.isEmpty()) return "N/A";
		String days = pickupDays.replace(" ", "");
		switch (days) {
			case "Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday":
				return "Everyday"; 
			case "Monday,Tuesday,Wednesday,Thursday,Friday":
				return "Every Weekdays"; 
			case "Saturday,Sunday":
				return "Every Weekends"; 
			default:
				return "Every " + days.replace(",", ", ");
		}
	}

	private CustomPanel viewItemInfo() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout());
		CustomPanel topContent = new CustomPanel();
		topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));

		String itemName = selectedItem != null && selectedItem.itemName != null ? selectedItem.itemName : "Item";
		String itemCategory = selectedItem != null && selectedItem.category != null ? selectedItem.category : "";
		double itemPrice = selectedItem != null ? selectedItem.price : 0;
		String itemDesc = selectedItem != null && selectedItem.description != null ? selectedItem.description : "";
		int stock = selectedItem != null ? selectedItem.itemQuantity : 0;
		String pickupArea = selectedItem != null && selectedItem.pickupArea != null ? selectedItem.pickupArea : "";
		String pickupTime = selectedItem != null && selectedItem.pickupTime != null ? selectedItem.pickupTime : "";
		String formattedDays = formatPickupDays(selectedItem != null ? selectedItem.pickupDays : "");

		CustomPanel itemInfoWrapper = new CustomPanel(new BorderLayout(10, 0));
		itemInfoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		CustomLabel nameLabel = new CustomLabel(itemName, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD);
		CustomLabel categoryLabel = new CustomLabel(itemCategory, Brand.HEADER4_TEXT_SIZE, FontStyle.REGULAR);
		categoryLabel.setForeground(Color.GRAY);
		itemInfoWrapper.add(nameLabel, BorderLayout.CENTER);
		itemInfoWrapper.add(categoryLabel, BorderLayout.EAST);

		String priceText = itemPrice > 0 ? "P" + String.format("%.0f", itemPrice) : "Free";
		CustomLabel priceLabel = new CustomLabel(priceText, Brand.HEADER1_TEXT_SIZE, FontStyle.BOLD);
		priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		priceLabel.setForeground(Brand.PRIMARY_COLOR);

		String initiatorName = selectedItem != null && selectedItem.initiatorFirstName != null && selectedItem.initiatorLastName != null ? (selectedItem.initiatorFirstName + " " + selectedItem.initiatorLastName) : "Someone";
		int karmaScore = 0;
		if (selectedItem != null && selectedItem.initiatorKarmaScore != 0) {
			karmaScore = selectedItem.initiatorKarmaScore;
		}

		CustomPanel initiatorWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		initiatorWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		initiatorWrapper.add(new CustomLabel("by ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
		initiatorWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/karma.png", 25, 25, Brand.SECONDARY_COLOR)));
		initiatorWrapper.add(new CustomLabel(String.valueOf(karmaScore), Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Brand.SECONDARY_COLOR));
		initiatorWrapper.add(new CustomLabel(" " + initiatorName.toUpperCase(), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));

		CustomLabel explicitStockLabel = new CustomLabel("Stock: " + stock, Brand.STANDARD_TEXT_SIZE, stock <= 0 ? FontStyle.BOLD : FontStyle.REGULAR, stock <= 0 ? Brand.RED : Color.GRAY);
		explicitStockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		CustomPanel locationWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		locationWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationWrapper.add(
				new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/location.png", 15, 15, Color.GRAY)));
		locationWrapper.add(new CustomLabel(pickupArea, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(formattedDays, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(pickupTime, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));

		CustomPanel quantityWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		quantityWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

		SpinnerNumberModel spinnerModel = (SpinnerNumberModel) quantitySpinner.getModel();

		int maxLimit = Math.max(1, stock);
		spinnerModel.setMaximum(maxLimit);
		spinnerModel.setMinimum(1);
		spinnerModel.setValue(1); 

		quantitySpinner.removeChangeListener(quantityChangeListener);
		quantitySpinner.addChangeListener(quantityChangeListener);

		quantityWrapper.add(quantitySpinner);

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

		if (tabMode == MarketplaceTabMode.MARKETPLACE || tabMode == MarketplaceTabMode.MARKETPLACE_WITHDRAWN) {
			topContent.add(priceLabel);
			topContent.add(Box.createVerticalStrut(15));
		}

		topContent.add(initiatorWrapper);
		topContent.add(Box.createVerticalStrut(5));
		topContent.add(explicitStockLabel);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(locationWrapper);
		topContent.add(Box.createVerticalStrut(10));

		boolean isWithdrawnTab = (tabMode == MarketplaceTabMode.MARKETPLACE_WITHDRAWN || tabMode == MarketplaceTabMode.SHARING_WITHDRAWN || tabMode == MarketplaceTabMode.TRADE_WITHDRAWN);
		
		if (!isWithdrawnTab && tabMode == MarketplaceTabMode.MARKETPLACE) {
			topContent.add(quantityWrapper);
			topContent.add(Box.createVerticalStrut(10));
		}
		
		if (actionRow != null)
			topContent.add(actionRow);
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

		if (tabMode == MarketplaceTabMode.MARKETPLACE_WITHDRAWN || tabMode == MarketplaceTabMode.SHARING_WITHDRAWN
				|| tabMode == MarketplaceTabMode.TRADE_WITHDRAWN) {

			String currentAction = selectedItem.action != null ? selectedItem.action.trim() : "";
			boolean isWithdrawn = currentAction.toLowerCase().endsWith("_withdrawn");
			boolean pendingProcessing = currentAction.toLowerCase().endsWith("_approval")
					|| currentAction.toLowerCase().endsWith("_return");

			if (pendingProcessing) {
				CustomButton toggleStatusBtn = new CustomButton("Pending Processing", 10);
				toggleStatusBtn.setPadding(20, 5);
				toggleStatusBtn.setDefaultColor(Brand.RED);
				toggleStatusBtn.setHoverColor(Brand.RED.darker());
				toggleStatusBtn.setEnabled(false); 
				row.add(toggleStatusBtn);
			} else {
				CustomButton toggleStatusBtn = new CustomButton(isWithdrawn ? "Add to Listing" : "Remove from Listing",
						10);
				toggleStatusBtn.setPadding(20, 5);
				toggleStatusBtn.setDefaultColor(Brand.PRIMARY_COLOR);
				toggleStatusBtn.setHoverColor(Brand.PRIMARY_COLOR.darker());
				
				toggleStatusBtn.addActionListener(e -> {
					if (isWithdrawn && selectedItem.itemQuantity <= 0) {
						JOptionPane.showMessageDialog(this, "Cannot add to listing. Item stock is currently zero.", "Action Denied", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					String newAction = isWithdrawn ? currentAction.replace("_Withdrawn", "").replace("_withdrawn", "") : currentAction + "_Withdrawn";
					String newStatus = isWithdrawn ? "Available" : "Unavailable";
					
					boolean updated = updateItemStatusInDB(selectedItem.itemId, newAction, newStatus);
					if (updated) {
						JOptionPane.showMessageDialog(this, "Item status successfully updated.", "Success",
								JOptionPane.INFORMATION_MESSAGE);
						refreshPanel();
					} else {
						JOptionPane.showMessageDialog(this, "Item status was not updated in the database.", "Update Failed",
								JOptionPane.ERROR_MESSAGE);
					}
				});
				row.add(toggleStatusBtn);

				row.add(Box.createHorizontalStrut(10));

				CustomButton updateItemBtn = new CustomButton("Update Data", 10);
				updateItemBtn.setPadding(20, 5);
				updateItemBtn.setDefaultColor(Brand.GREEN);
				updateItemBtn.setHoverColor(Brand.GREEN.darker());
				updateItemBtn.addActionListener(e -> {
					Window parentWindow = SwingUtilities.getWindowAncestor(this);
					new ItemForm(parentWindow, "Update Item", selectedItem, user.user_id, () -> {
						refreshPanel(); 
					});
				});
				row.add(updateItemBtn);

				row.add(Box.createHorizontalStrut(10));

				CustomButton deleteItemBtn = new CustomButton("Remove Data", 10);
				deleteItemBtn.setPadding(20, 5);
				deleteItemBtn.setDefaultColor(Brand.RED);
				deleteItemBtn.setHoverColor(Brand.RED.darker());
				deleteItemBtn.addActionListener(e -> {
					int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently remove this item?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
					if (confirm == JOptionPane.YES_OPTION) {
						ItemActionManager.deleteArchivedItem(selectedItem.itemId, "User removed item");
						JOptionPane.showMessageDialog(this, "Item successfully removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
						refreshPanel(); 
					}
				});
				row.add(deleteItemBtn);
			}
			return row; 
		}

		switch (tabMode) {
		case MARKETPLACE:
			paymentMethodComboBox = new CustomComboBox<>(
					new String[] { "Select a Payment Method", "Cash", "GCash", "Maya", "BDO", "BPI" });
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
					JOptionPane.showMessageDialog(this, "Please select a payment method before proceeding to checkout.",
							"Payment Method Required", JOptionPane.WARNING_MESSAGE);
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
			primaryActionButton.addActionListener(e -> {
				boolean success = ItemActionManager.processRequestApproval(selectedItem.itemId, user.user_id, selectedItem.itemQuantity, null, "borrow request", "Borrow-Request", false, "Sharing_Approval", null);
				if (success) {
					JOptionPane.showMessageDialog(this, "Borrow Request sent to the owner!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					refreshPanel(); 
				} else {
					JOptionPane.showMessageDialog(this, "Borrow request failed due to a database error.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			row.add(primaryActionButton);
			break;
		case TRADE:
			CustomTextField proposedItemField = new CustomTextField("Item to propose...");
			proposedItemField.setCustomSize(200, 35);
			proposedItemField.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			row.add(proposedItemField);
			row.add(Box.createHorizontalStrut(10));
			
			primaryActionButton = new CustomButton("Propose Trade", 10);
			primaryActionButton.setDefaultColor(Brand.PRIMARY_COLOR);
			primaryActionButton.setHoverColor(Brand.PRIMARY_COLOR.darker());
			primaryActionButton.setPadding(20, 5);
			primaryActionButton.addActionListener(e -> {
				String proposedItem = proposedItemField.getText().trim();
				if (proposedItem.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Please enter the item you want to propose for the trade.", "Proposal Empty", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				boolean success = ItemActionManager.processRequestApproval(selectedItem.itemId, user.user_id, selectedItem.itemQuantity, null, "trade request", "Trade-Request", false, "Trade_Approval", proposedItem);
				if (success) {
					JOptionPane.showMessageDialog(this, "Trade Proposal sent to the owner!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					refreshPanel();
				} else {
					JOptionPane.showMessageDialog(this, "Trade proposal failed due to a database error.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});

			row.add(primaryActionButton);
			row.add(Box.createHorizontalStrut(10));
			break;
		default:
			break;
		}
		return row;
	}

	private CustomPanel viewItemCheckout() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout());
		CustomPanel topContent = new CustomPanel();
		topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));

		double unitPrice = selectedItem != null ? selectedItem.price : 0;
		double total = getSelectedItemTotal();
		
		String itemName = selectedItem != null && selectedItem.itemName != null ? selectedItem.itemName : "Item";
		String itemCategory = selectedItem != null && selectedItem.category != null ? selectedItem.category : "";
		String formattedDays = formatPickupDays(selectedItem != null ? selectedItem.pickupDays : "");
		String area = (selectedItem != null && selectedItem.pickupArea != null) ? selectedItem.pickupArea : "";
		String time = (selectedItem != null && selectedItem.pickupTime != null) ? selectedItem.pickupTime : "";
		int stock = selectedItem != null ? selectedItem.itemQuantity : 0;

		CustomPanel itemInfoWrapper = new CustomPanel(new BorderLayout(10, 0));
		itemInfoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		itemInfoWrapper.add(new CustomLabel(itemName, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD), BorderLayout.CENTER);

		CustomLabel categoryLabel = new CustomLabel(itemCategory, Brand.HEADER4_TEXT_SIZE, FontStyle.REGULAR);
		categoryLabel.setForeground(Color.GRAY);
		itemInfoWrapper.add(categoryLabel, BorderLayout.EAST);

		String priceText = unitPrice > 0 ? "P" + String.format("%.0f", unitPrice) : "Free";
		CustomLabel priceLabel = new CustomLabel(priceText, Brand.HEADER1_TEXT_SIZE, FontStyle.BOLD);
		priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		priceLabel.setForeground(Brand.PRIMARY_COLOR);

		String initiatorName = selectedItem != null && selectedItem.initiatorFirstName != null && selectedItem.initiatorLastName != null ? (selectedItem.initiatorFirstName + " " + selectedItem.initiatorLastName) : "Unknown";
		int karmaScore = 67;
		if (selectedItem != null && selectedItem.initiatorKarmaScore != 0) {
			karmaScore = selectedItem.initiatorKarmaScore;
		}
		
		CustomPanel initiatorWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		initiatorWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		initiatorWrapper.add(new CustomLabel("by ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
		initiatorWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/karma.png", 25, 25, Brand.SECONDARY_COLOR)));
		initiatorWrapper.add(new CustomLabel(String.valueOf(karmaScore), Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Brand.SECONDARY_COLOR));
		initiatorWrapper.add(new CustomLabel(" " + initiatorName.toUpperCase(), Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));

		CustomLabel explicitStockLabel = new CustomLabel("Stock: " + stock, Brand.STANDARD_TEXT_SIZE, stock <= 0 ? FontStyle.BOLD : FontStyle.REGULAR, stock <= 0 ? Brand.RED : Color.GRAY);
		explicitStockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		CustomPanel locationWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		locationWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/location.png", 15, 15, Color.GRAY)));
		locationWrapper.add(new CustomLabel(area, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(formattedDays, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		locationWrapper.add(new CustomLabel("\u2022", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.GRAY));
		locationWrapper.add(new CustomLabel(time, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));

		topContent.add(itemInfoWrapper);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(priceLabel);
		topContent.add(Box.createVerticalStrut(15));
		topContent.add(initiatorWrapper);
		topContent.add(Box.createVerticalStrut(5));
		topContent.add(explicitStockLabel);
		topContent.add(Box.createVerticalStrut(10));
		topContent.add(locationWrapper);
		topContent.add(Box.createVerticalStrut(20));

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

			CustomPanel pricePanel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			pricePanel.add(new CustomLabel("Unit Price: ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD));
			pricePanel.add(new CustomLabel(unitPrice > 0 ? ("P" + String.format("%.0f", unitPrice)) : "Free",
					Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			details.add(pricePanel);
			details.add(Box.createVerticalStrut(5));

			CustomPanel qtyPanel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			qtyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			qtyPanel.add(new CustomLabel("Quantity: ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD));
			qtyPanel.add(new CustomLabel(String.valueOf(getSelectedQuantity()), Brand.SUBHEADER_TEXT_SIZE,
					FontStyle.REGULAR));
			details.add(qtyPanel);
			details.add(Box.createVerticalStrut(5));

			String payment = getSelectedPaymentMethod();
			CustomPanel paymentPanel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			paymentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			paymentPanel.add(new CustomLabel("Payment Method: ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD));
			paymentPanel.add(new CustomLabel(payment.isBlank() ? "(not selected)" : payment, Brand.SUBHEADER_TEXT_SIZE,
					FontStyle.REGULAR));
			details.add(paymentPanel);
			details.add(Box.createVerticalStrut(50));

			CustomPanel summaryWrapper = new CustomPanel(new BorderLayout());
			summaryWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

			CustomPanel leftInfoPanel = new CustomPanel();
			leftInfoPanel.setLayout(new BoxLayout(leftInfoPanel, BoxLayout.Y_AXIS));
			leftInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			leftInfoPanel.add(new CustomLabel(initiatorName, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));

			CustomPanel claimPanel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			claimPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			claimPanel.add(new CustomLabel("Claim at ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			claimPanel.add(new CustomLabel(area, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Brand.GREEN));
			claimPanel.add(new CustomLabel(" by ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			claimPanel.add(new CustomLabel(formattedDays, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Brand.GREEN));
			claimPanel.add(new CustomLabel(" at ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			claimPanel.add(new CustomLabel(time, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Brand.GREEN));

			leftInfoPanel.add(claimPanel);

			CustomPanel rightInfoPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			rightInfoPanel.add(new CustomLabel("Total: " + (total > 0 ? ("P" + String.format("%.0f", total)) : "Free"),
					Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD));

			summaryWrapper.add(leftInfoPanel, BorderLayout.CENTER);
			summaryWrapper.add(rightInfoPanel, BorderLayout.EAST);

			details.add(summaryWrapper);
			details.add(Box.createVerticalStrut(20));

			CustomButton payButton = new CustomButton(
					total > 0 ? "Pay P" + String.format("%.0f", total) : "Confirm Order", 10);
			payButton.setDefaultColor(Brand.GREEN);
			payButton.setHoverColor(Brand.GREEN.darker());
			payButton.setPadding(20, 5);
			payButton.addActionListener(e -> {

				String refNo = null;
				if (!payment.equalsIgnoreCase("Cash")) {
					refNo = JOptionPane.showInputDialog(this,
							"Enter Payment Reference Number for " + payment + ":", "Payment Reference",
							JOptionPane.PLAIN_MESSAGE);

					if (refNo == null || !refNo.matches("\\d{8,}")) {
						JOptionPane.showMessageDialog(this,
								"Payment Reference must contain at least 8 digits and only numbers.", "Invalid Reference",
								JOptionPane.ERROR_MESSAGE);
						return; 
					}
				}

				Object[] options = { "Proceed", "Cancel" };
				int choice = JOptionPane.showOptionDialog(this, "Are you sure you want to finalize this transaction?",
						"Confirm Payment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);
						
				if (choice == JOptionPane.YES_OPTION) {
					int quantityToBuy = getSelectedQuantity();
					
					boolean success = ItemActionManager.buyItem(selectedItem.itemId, user.user_id, quantityToBuy, (int)total, payment, refNo);
					
					if (success) {
						JOptionPane.showMessageDialog(this,
								"Transaction completed successfully!\nPlease meet the seller at the designated time and location.",
								"Success", JOptionPane.INFORMATION_MESSAGE);
						
						refreshPanel();
					} else {
						JOptionPane.showMessageDialog(this,
								"Transaction failed due to a database error.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			CustomPanel buttonWrapper = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			buttonWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			buttonWrapper.add(payButton);

			details.add(buttonWrapper);
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

	public void setItemViewStateListener(ViewStateListener listener) {
		this.itemViewStateListener = listener;
	}
}