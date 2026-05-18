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

public class SplitRequestPanel extends CustomPanel {
	private static final long serialVersionUID = 1L;

	private UserRecord user;
	private MarketplaceTabMode tabMode;
	
	private List<ItemRecord> itemsList;
	private JList<ItemRecord> requestList;
	private CustomPanel centerPanel;
	
	private final String DB_URL = DatabaseManager.getURL();
	private final String USER = DatabaseManager.getUser();
	private final String PASSWORD = DatabaseManager.getPassword();

	public SplitRequestPanel(UserRecord user, String listTitle, MarketplaceTabMode tabMode) {
		this.user = user;
		this.tabMode = tabMode;
		
		setLayout(new BorderLayout(15, 0));
		setPadding(10);
		setBackground(Color.WHITE);

		CustomPanel westPanel = new CustomPanel();
		westPanel.setLayout(new BorderLayout());
		westPanel.setPreferredSize(new Dimension(300, 0));
		westPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

		CustomLabel titleLabel = new CustomLabel(listTitle, 16f, FontStyle.BOLD);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		westPanel.add(titleLabel, BorderLayout.NORTH);

		itemsList = new ArrayList<>();
		requestList = new JList<>();
		requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		if (FontLib.POPPINS_REGULAR != null) {
			requestList.setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
		} else {
			requestList.setFont(new Font("SansSerif", Font.PLAIN, 14));
		}
		
		requestList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
			JLabel label = new JLabel(value.itemName); 
			label.setOpaque(true);
			
			if (FontLib.POPPINS_REGULAR != null) {
				label.setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
			} else {
				label.setFont(new Font("SansSerif", Font.PLAIN, 14));
			}
			
			label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
			
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

		JScrollPane listScroll = new JScrollPane(requestList);
		listScroll.setBorder(BorderFactory.createEmptyBorder());
		westPanel.add(listScroll, BorderLayout.CENTER);

		centerPanel = new CustomPanel();
		centerPanel.setLayout(new BorderLayout());
		showPlaceholder();

		requestList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				ItemRecord selected = requestList.getSelectedValue();
				if (selected != null) {
					showItemDetails(selected);
				}
			}
		});

		add(westPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		
		fetchRequests();
	}
	
	private void fetchRequests() {
		itemsList.clear();
		String targetAction = "";
		
		switch (tabMode) {
			case SHARING_APPROVAL: targetAction = "Sharing_Approval"; break;
			case SHARING_RETURN: targetAction = "Sharing_Return"; break;
			case TRADE_APPROVAL: targetAction = "Trade_Approval"; break;
			default: return;
		}

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			 PreparedStatement pstmt = conn.prepareStatement(
				"SELECT i.*, u.first_name AS initiator_firstname, u.last_name AS initiator_lastname FROM items i LEFT JOIN users u ON i.owner_id = u.user_id WHERE i.action = ? AND i.owner_id = ? AND i.items_is_archived = 0")) {
			
			pstmt.setString(1, targetAction);
			pstmt.setInt(2, user.userId);
			
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
				
				itemsList.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		requestList.setListData(itemsList.toArray(new ItemRecord[0]));
		showPlaceholder();
	}
	
	private void showPlaceholder() {
		centerPanel.removeAll();
		CustomLabel placeholderLabel = new CustomLabel("Select a request from the list to view details", 14f, FontStyle.REGULAR, Color.GRAY);
		placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		centerPanel.add(placeholderLabel, BorderLayout.CENTER);
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	private void showItemDetails(ItemRecord item) {
		centerPanel.removeAll();
		
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setPadding(20);
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
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
		
		CustomPanel itemWrapper = new CustomPanel(new BorderLayout(20, 0));
		itemWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		String imagePath = (item.itemImage != null && !item.itemImage.isEmpty()) ? item.itemImage : "/resources/images/umak_img.jpg";
		
		ImageIcon itemIcon = IconLoader.loadAndScaleIcon(imagePath, 350, 350);
		if (itemIcon == null) {
			itemIcon = IconLoader.loadAndScaleIcon("/resources/images/umak_img.jpg", 350, 350);
		}
		
		JLabel imgLabel = new JLabel(itemIcon);
		imgLabel.setPreferredSize(new Dimension(350, 350));
		itemWrapper.add(imgLabel, BorderLayout.WEST); 
		
		CustomPanel detailsContainer = new CustomPanel(new BorderLayout());
		
		CustomPanel detailsWrapper = new CustomPanel();
		detailsWrapper.setLayout(new BoxLayout(detailsWrapper, BoxLayout.Y_AXIS));
		detailsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
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
		
		CustomLabel conditionLabel = new CustomLabel(item.condition, Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
		conditionLabel.setForeground(conditionColor);
		conditionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		detailsWrapper.add(conditionLabel);
		detailsWrapper.add(Box.createVerticalStrut(10));
		
		CustomPanel requesterWrapper = new CustomPanel();
		requesterWrapper.setLayout(new BoxLayout(requesterWrapper, BoxLayout.X_AXIS));
		requesterWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		requesterWrapper.add(new CustomLabel("Owner/Lister: ", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD));
		requesterWrapper.add(Box.createHorizontalStrut(5));
		requesterWrapper.add(new CustomLabel(item.initiatorFirstName + " " + item.initiatorLastName, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR));
		detailsWrapper.add(requesterWrapper);
		detailsWrapper.add(Box.createVerticalStrut(10));
		
		CustomPanel locationWrapper = new CustomPanel();
		locationWrapper.setLayout(new BoxLayout(locationWrapper, BoxLayout.X_AXIS));
		locationWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationWrapper.add(new JLabel(IconLoader.loadAndScaleColorizedIcon("/resources/icons/location.png", 15, 15, Color.GRAY)));
		locationWrapper.add(Box.createHorizontalStrut(5));
		
		String locationText = item.pickupArea + " \u2022 " + item.pickupDays + " \u2022 " + item.pickupTime;
		locationWrapper.add(new CustomLabel(locationText, Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY));
		detailsWrapper.add(locationWrapper);
		detailsWrapper.add(Box.createVerticalStrut(20));
		
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
		
		CustomPanel btnWrapper = new CustomPanel();
		btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
		btnWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		
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
		
		wrapper.add(btnWrapper);
		centerPanel.add(wrapper, BorderLayout.NORTH);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	private void processAction(int itemId, String newAction, String successMessage, boolean archive) {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			 PreparedStatement pstmt = conn.prepareStatement("UPDATE items SET action = ?, items_is_archived = ? WHERE item_id = ?")) {
			pstmt.setString(1, newAction);
			pstmt.setBoolean(2, archive);
			pstmt.setInt(3, itemId);
			pstmt.executeUpdate();
			
			JOptionPane.showMessageDialog(this, successMessage + "\nPlease coordinate at the designated time and location.", "Success", JOptionPane.INFORMATION_MESSAGE);
			fetchRequests(); 
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}