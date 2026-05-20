package marketplace;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import components.*;
import database.*;
import enums.MarketplaceTabMode;
import enums.Category;
import enums.Condition;
import utils.*;

// Shows a split screen with a list on the left and details on the right
public class SplitRequestPanel extends CustomPanel {
	
	// FIELDS

	// Keeps track of version
	private static final long serialVersionUID = 1L;

	// Stores user and tab data
	private UserRecord user;
	private MarketplaceTabMode tabMode;
	
	// UI parts for lists and panels
	private List<ItemRecord> itemsList;
	private JList<ItemRecord> requestList;
	private CustomPanel centerPanel;
	
	// Database connection strings
	private final String DB_URL = DatabaseManager.getURL();
	private final String USER = DatabaseManager.getUser();
	private final String PASSWORD = DatabaseManager.getPassword();

	// CONSTRUCTORS

	// Sets up the main split layout and UI parts
	public SplitRequestPanel(UserRecord user, String listTitle, MarketplaceTabMode tabMode) {
		this.user = user;
		this.tabMode = tabMode;
		
		// Set basic layout styles
		setLayout(new BorderLayout(15, 0));
		setPadding(10);
		setBackground(Color.WHITE);

		// Build the left panel for the list
		CustomPanel westPanel = new CustomPanel();
		westPanel.setLayout(new BorderLayout());
		westPanel.setPreferredSize(new Dimension(300, 0));
		westPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

		// Add title to the left panel
		CustomLabel titleLabel = new CustomLabel(listTitle, 16f, FontStyle.BOLD);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		westPanel.add(titleLabel, BorderLayout.NORTH);

		// Setup the list UI
		itemsList = new ArrayList<>();
		requestList = new JList<>();
		requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
		// Set custom font for the list
		if (FontLib.POPPINS_REGULAR != null) {
			requestList.setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
		} else {
			requestList.setFont(new Font("SansSerif", Font.PLAIN, 14));
		}
		
		// Change how list items look
		requestList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
			JLabel label = new JLabel(value.itemName); 
			label.setOpaque(true);
			
			// Set label font
			if (FontLib.POPPINS_REGULAR != null) {
				label.setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
			} else {
				label.setFont(new Font("SansSerif", Font.PLAIN, 14));
			}
			
			label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
			
			// Change colors when clicked
			if (isSelected) {
				label.setBackground(Brand.PRIMARY_COLOR);
				label.setForeground(Color.WHITE);
				
				if (FontLib.POPPINS_BOLD != null) {
					label.setFont(FontLib.POPPINS_BOLD.deriveFont(14f));
				} else {
					label.setFont(new Font("SansSerif", Font.BOLD, 14));
				}
			} else {
				label.setBackground(Color.WHITE);
				label.setForeground(Color.DARK_GRAY);
			}
			return label;
		});

		// Add scroll bar to list
		JScrollPane listScroll = new JScrollPane(requestList);
		listScroll.setBorder(BorderFactory.createEmptyBorder());
		westPanel.add(listScroll, BorderLayout.CENTER);

		// Build the right panel for details
		centerPanel = new CustomPanel();
		centerPanel.setLayout(new BorderLayout());
		showPlaceholder();

		// Add click event to the list
		requestList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				ItemRecord selected = requestList.getSelectedValue();
				if (selected != null) {
					showItemDetails(selected);
				}
			}
		});

		// Add panels to the screen
		add(westPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		
		// Load data
		fetchRequests();
	}
	
	// METHODS

	// Gets data from the database and fills the list
	private void fetchRequests() {
		itemsList.clear();
		String targetAction = "";
		
		// Check what kind of items to get
		switch (tabMode) {
			case SHARING_APPROVAL: targetAction = "Sharing_Approval"; break;
			case SHARING_RETURN: targetAction = "Sharing_Return"; break;
			case TRADE_APPROVAL: targetAction = "Trade_Approval"; break;
			default: return;
		}

		// Connect to database and run query
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			 PreparedStatement pstmt = conn.prepareStatement(
				"SELECT i.*, " +
				"  u_req.first_name AS initiator_firstname, " +
				"  u_req.last_name AS initiator_lastname, " +
				"  u_req.karma_score AS initiator_karmascore, " +
				"  latest_t.proposed_item AS proposed_item_trans " +
				"FROM items i " +
				"LEFT JOIN (" +
				"  SELECT t1.item_id, t1.borrower_id, t1.proposed_item " +
				"  FROM transactions t1 " +
				"  WHERE t1.transaction_id = (SELECT MAX(transaction_id) FROM transactions t2 WHERE t2.item_id = t1.item_id)" +
				") latest_t ON latest_t.item_id = i.item_id " +
				"LEFT JOIN users u_req ON latest_t.borrower_id = u_req.user_id " +
				"WHERE i.action = ? AND i.owner_id = ? AND i.items_is_archived = 0")) {
			
			// Set query values
			pstmt.setString(1, targetAction);
			pstmt.setInt(2, user.user_id);
			
			// Read the results
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				ItemRecord item = new ItemRecord();
				item.itemId = rs.getInt("item_id");
				item.ownerId = rs.getInt("owner_id");
				item.itemName = rs.getString("item_name");
				item.itemQuantity = rs.getInt("item_quantity");
				item.description = rs.getString("description");
				item.itemImage = rs.getString("item_image");
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
				
				item.desiredItem = rs.getString("proposed_item_trans");

				// Add to list
				itemsList.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Update UI
		requestList.setListData(itemsList.toArray(new ItemRecord[0]));
		showPlaceholder();
		requestList.revalidate();
		requestList.repaint();
	}

	// Reloads the data list
	public void refreshData() {
		fetchRequests();
	}
	
	// Shows default text when no item is clicked
	private void showPlaceholder() {
		centerPanel.removeAll();
		CustomLabel placeholderLabel = new CustomLabel("Select a request from the list to view details", 14f, FontStyle.REGULAR, Color.GRAY);
		placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		centerPanel.add(placeholderLabel, BorderLayout.CENTER);
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	// Shows the selected item's info and buttons on the right side
	private void showItemDetails(ItemRecord item) {
		centerPanel.removeAll();
		
		// Main wrapper
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setPadding(20);
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Top header
		CustomPanel headerWrapper = new CustomPanel();
		headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.X_AXIS));
		headerWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		String headerTitle = tabMode == MarketplaceTabMode.SHARING_RETURN ? "Return Item" : "Request Approval";
		String statusText = tabMode == MarketplaceTabMode.SHARING_RETURN ? "Pending Return" : "Pending Request";
		
		headerWrapper.add(new CustomLabel(headerTitle, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
		headerWrapper.add(Box.createHorizontalGlue());
		headerWrapper.add(new CustomLabel(statusText, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		wrapper.add(headerWrapper);
		wrapper.add(Box.createVerticalStrut(20));
		
		// Item layout
		CustomPanel itemWrapper = new CustomPanel(new BorderLayout(20, 0));
		itemWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Load image
		String imagePath = (item.itemImage != null && !item.itemImage.isEmpty()) ? item.itemImage : "/resources/images/umak_img.jpg";
		
		ImageIcon itemIcon = IconLoader.loadAndScaleIcon(imagePath, 350, 350);
		if (itemIcon == null) {
			itemIcon = IconLoader.loadAndScaleIcon("/resources/images/umak_img.jpg", 350, 350);
		}
		
		JLabel imgLabel = new JLabel(itemIcon);
		imgLabel.setPreferredSize(new Dimension(350, 350));
		itemWrapper.add(imgLabel, BorderLayout.WEST); 
		
		// Details container
		CustomPanel detailsContainer = new CustomPanel(new BorderLayout());
		
		CustomPanel detailsWrapper = new CustomPanel();
		detailsWrapper.setLayout(new BoxLayout(detailsWrapper, BoxLayout.Y_AXIS));
		detailsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Get category color
		Color categoryColor = Color.GRAY;
		if (item.category != null) {
			try {
				Category catEnum = Category.valueOf(item.category.toUpperCase().replace(" ", "_"));
				Color c = Brand.getCategoryColor(catEnum);
				if (c != null) categoryColor = c;
			} catch (IllegalArgumentException e) {
				categoryColor = Color.GRAY;
			}
		}

		// Get condition color
		Color conditionColor = Color.GRAY;
		if (item.condition != null) {
			try {
				Condition condEnum = Condition.valueOf(item.condition.toUpperCase().replace(" ", "_"));
				Color c = Brand.getConditionColor(condEnum);
				if (c != null) conditionColor = c;
			} catch (IllegalArgumentException e) {
				conditionColor = Color.GRAY;
			}
		}

		// Show name and category
		CustomPanel nameWrapper = new CustomPanel();
		nameWrapper.setLayout(new BoxLayout(nameWrapper, BoxLayout.X_AXIS));
		nameWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameWrapper.add(new CustomLabel(item.itemName, Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
		nameWrapper.add(Box.createHorizontalStrut(15));
		nameWrapper.add(Box.createHorizontalGlue());

		CustomPanel categoryWrapper = new CustomPanel(new GridBagLayout());
		categoryWrapper.setBackground(categoryColor);
		categoryWrapper.setRadius(10);
		categoryWrapper.setPadding(20, 5); 
		categoryWrapper.add(new CustomLabel(item.category, Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Color.WHITE)); 
		categoryWrapper.setMaximumSize(new Dimension(200, 35));
		
		nameWrapper.add(categoryWrapper);
		detailsWrapper.add(nameWrapper);
		detailsWrapper.add(Box.createVerticalStrut(10));
		
		// Show condition
		CustomLabel conditionLabel = new CustomLabel(item.condition, Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
		conditionLabel.setForeground(conditionColor);
		conditionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		detailsWrapper.add(conditionLabel);
		detailsWrapper.add(Box.createVerticalStrut(10));
		
		// Show user who made request
		CustomPanel requesterWrapper = new CustomPanel();
		requesterWrapper.setLayout(new BoxLayout(requesterWrapper, BoxLayout.X_AXIS));
		requesterWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		requesterWrapper.add(new CustomLabel("Request from: ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD));
		requesterWrapper.add(Box.createHorizontalStrut(5));
		requesterWrapper.add(new CustomLabel("[" + item.initiatorKarmaScore + "] ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD, Brand.SECONDARY_COLOR));
		requesterWrapper.add(new CustomLabel(item.initiatorFirstName + " " + item.initiatorLastName, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
		detailsWrapper.add(requesterWrapper);
		detailsWrapper.add(Box.createVerticalStrut(10));
		
		// Show proposed trade item if trading
		if (tabMode == MarketplaceTabMode.TRADE_APPROVAL && item.desiredItem != null && !item.desiredItem.trim().isEmpty()) {
			CustomPanel proposedWrapper = new CustomPanel();
			proposedWrapper.setLayout(new BoxLayout(proposedWrapper, BoxLayout.X_AXIS));
			proposedWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			proposedWrapper.add(new CustomLabel("Proposed Item: ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD));
			proposedWrapper.add(Box.createHorizontalStrut(5));
			proposedWrapper.add(new CustomLabel(item.desiredItem, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
			detailsWrapper.add(proposedWrapper);
			detailsWrapper.add(Box.createVerticalStrut(10));
		}
		
		// Show location
		CustomPanel locationWrapper = new CustomPanel();
		locationWrapper.setLayout(new BoxLayout(locationWrapper, BoxLayout.X_AXIS));
		locationWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/location.png", 15, 15, Color.GRAY)));
		locationWrapper.add(Box.createHorizontalStrut(5));
		
		String locationText = item.pickupArea + " \u2022 " + item.pickupDays + " \u2022 " + item.pickupTime;
		locationWrapper.add(new CustomLabel(locationText, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		detailsWrapper.add(locationWrapper);
		detailsWrapper.add(Box.createVerticalStrut(20));
		
		// Show description
		CustomPanel descriptionWrapper = new CustomPanel();
		descriptionWrapper.setLayout(new BoxLayout(descriptionWrapper, BoxLayout.Y_AXIS));
		descriptionWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionWrapper.add(new CustomLabel("Product Description:", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD));
		descriptionWrapper.add(Box.createVerticalStrut(5));
		
		CustomTextArea descArea = CustomTextArea.createDisplayOnly();
		descArea.setText(item.description);
		descArea.setFontSize(Brand.STANDARD_TEXT_SIZE);
		descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionWrapper.add(descArea);
		
		detailsWrapper.add(descriptionWrapper);
		detailsWrapper.add(Box.createVerticalGlue()); 
		
		detailsContainer.add(detailsWrapper, BorderLayout.NORTH);
		itemWrapper.add(detailsContainer, BorderLayout.CENTER);
		
		wrapper.add(itemWrapper);
		wrapper.add(Box.createVerticalStrut(25));
		
		// Make buttons area
		CustomPanel btnWrapper = new CustomPanel();
		btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
		btnWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Setup buttons based on mode
		if (tabMode == MarketplaceTabMode.SHARING_APPROVAL) {
			CustomButton approveBtn = new CustomButton("Approve Borrow", 10);
			approveBtn.setDefaultColor(Brand.GREEN);
			approveBtn.setHoverColor(Brand.GREEN.darker());
			approveBtn.setPadding(20, 5); 
			approveBtn.addActionListener(e -> processAction(item.itemId, "Sharing_Return", "Borrow Request Approved! Waiting for item return.", false));
			
			CustomButton declineBtn = new CustomButton("Decline", 10);
			declineBtn.setDefaultColor(Brand.RED);
			declineBtn.setHoverColor(Brand.RED.darker());
			declineBtn.setPadding(20, 5); 
			declineBtn.addActionListener(e -> processAction(item.itemId, "Sharing", "Borrow Request Declined. Item returned to Sharing Center.", false));
			
			btnWrapper.add(approveBtn);
			btnWrapper.add(Box.createHorizontalStrut(10));
			btnWrapper.add(declineBtn);
			
		} else if (tabMode == MarketplaceTabMode.SHARING_RETURN) {
			CustomButton returnBtn = new CustomButton("Confirm Return", 10);
			returnBtn.setDefaultColor(Brand.GREEN);
			returnBtn.setHoverColor(Brand.GREEN.darker());
			returnBtn.setPadding(20, 5);
			returnBtn.addActionListener(e -> processAction(item.itemId, "Sharing", "Item successfully returned and placed back in the Sharing Center.", false));
			
			btnWrapper.add(returnBtn);
			
		} else if (tabMode == MarketplaceTabMode.TRADE_APPROVAL) {
			CustomButton acceptBtn = new CustomButton("Accept Trade", 10);
			acceptBtn.setDefaultColor(Brand.GREEN);
			acceptBtn.setHoverColor(Brand.GREEN.darker());
			acceptBtn.setPadding(20, 5);
			acceptBtn.addActionListener(e -> processAction(item.itemId, "Trade", "Trade Accepted successfully!", true));
			
			CustomButton declineBtn = new CustomButton("Decline", 10);
			declineBtn.setDefaultColor(Brand.RED);
			declineBtn.setHoverColor(Brand.RED.darker());
			declineBtn.setPadding(20, 5);
			declineBtn.addActionListener(e -> processAction(item.itemId, "Trade", "Trade Declined. Item returned to Trading Center.", false));
			
			btnWrapper.add(acceptBtn);
			btnWrapper.add(Box.createHorizontalStrut(10));
			btnWrapper.add(declineBtn);
		}
		
		// Add everything to screen
		wrapper.add(btnWrapper);
		centerPanel.add(wrapper, BorderLayout.NORTH);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	// Updates the database when a button is clicked
	private void processAction(int itemId, String newAction, String successMessage, boolean archive) {
        String transAction = "Update";
        String availabilityStatus = null;
        String logReason = "update";
        String actionCol = newAction;
        
		// Set values based on what button was clicked
        if (tabMode == MarketplaceTabMode.SHARING_APPROVAL && "Sharing_Return".equals(newAction)) {
            transAction = "Borrow-Approved";
            availabilityStatus = "Unavailable";
            logReason = "update - accept";
        } else if (tabMode == MarketplaceTabMode.SHARING_APPROVAL && "Sharing".equals(newAction)) {
            transAction = "Borrow-Declined";
            logReason = "update - decline";
        } else if (tabMode == MarketplaceTabMode.SHARING_RETURN && "Sharing".equals(newAction)) {
            transAction = "Borrow-Return";
            availabilityStatus = "Available";
            logReason = "update - return";
        } else if (tabMode == MarketplaceTabMode.TRADE_APPROVAL && archive) {
            transAction = "Trade-Approved";
            availabilityStatus = "Unavailable";
            logReason = "update - accept";
            actionCol = null;
        } else if (tabMode == MarketplaceTabMode.TRADE_APPROVAL && !archive) {
            transAction = "Trade-Declined";
            logReason = "update - decline";
        }

		// Save changes to database
		boolean success = ItemActionManager.processRequestApproval(itemId, user.user_id, 1, availabilityStatus, logReason, transAction, archive, actionCol, null);

		// Show message box
		if (success) {
            JOptionPane.showMessageDialog(this, successMessage + "\nPlease coordinate at the designated time and location.", "Success", JOptionPane.INFORMATION_MESSAGE);
            fetchRequests(); 
        } else {
            JOptionPane.showMessageDialog(this, "Database Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}