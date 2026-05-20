package marketplace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import admin.database.LogsDatabase;
import database.*;
import enums.TransactionLogAction;
import enums.UserLogAction;

public class ItemActionManager {

    // STATIC METHODS

    // Deducts item stock, logs event, inserts transaction records, and awards karma points to both buyer and seller
    public static boolean buyItem(int itemId, int buyerId, int quantity, int totalPrice, String paymentMethod, String paymentReference) {
        String updateItemQuery = "UPDATE items SET item_quantity = item_quantity - ?, availability_status = CASE WHEN (item_quantity - ?) < 1 THEN 'Unavailable' ELSE availability_status END WHERE item_id = ?";
        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Update', 'update selling item')";
        String insertTransQuery = "INSERT INTO transactions (item_id, borrower_id, quantity, total_price, payment_method, payment_reference, action) VALUES (?, ?, ?, ?, ?, ?, 'Buy')";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateItemQuery);
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery);
                 PreparedStatement psTrans = conn.prepareStatement(insertTransQuery, Statement.RETURN_GENERATED_KEYS)) {

                psUpdate.setInt(1, quantity);
                psUpdate.setInt(2, quantity);
                psUpdate.setInt(3, itemId);
                psUpdate.executeUpdate();

                psLog.setInt(1, itemId);
                psLog.executeUpdate();

                psTrans.setInt(1, itemId);
                psTrans.setInt(2, buyerId);
                psTrans.setInt(3, quantity);
                psTrans.setInt(4, totalPrice);
                psTrans.setString(5, paymentMethod);
                psTrans.setString(6, paymentReference);
                psTrans.executeUpdate();

                ResultSet rsTrans = psTrans.getGeneratedKeys();
                int transactionId = -1;
                if (rsTrans.next()) {
                    transactionId = rsTrans.getInt(1);
                }

                if (transactionId != -1) {
                    int sellerId = -1;
                    try (PreparedStatement psOwner = conn.prepareStatement("SELECT owner_id FROM items WHERE item_id = ?")) {
                        psOwner.setInt(1, itemId);
                        ResultSet rsOwner = psOwner.executeQuery();
                        if (rsOwner.next()) sellerId = rsOwner.getInt("owner_id");
                    }
                    
                    if (sellerId != -1) {
                        KarmaManager.awardKarma(conn, buyerId, sellerId, transactionId, "Buy", quantity);
                        KarmaManager.awardKarma(conn, sellerId, buyerId, transactionId, "Sell", quantity);
                    }
                }

                conn.commit();
               
            
                try {
                    LogsDatabase logDB = new LogsDatabase();
                    logDB.insertLog(TransactionLogAction.BUY, buyerId, itemId, null);
                } catch (Exception ex) {
                  
                }
                
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Inserts a new cash-sale marketplace item record, creates its initial change log, and tracks the initial sell intent
    public static boolean addMarketplaceItem(ItemRecord item) {
        String insertItemQuery = "INSERT INTO items (owner_id, item_name, item_quantity, description, item_image, category, `condition`, price, pickup_area, pickup_days, pickup_time, action) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Create', 'Created a Selling Item')";
        String insertTransQuery = "INSERT INTO transactions (item_id, borrower_id, quantity, action) VALUES (?, ?, ?, 'Sell')";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psItem = conn.prepareStatement(insertItemQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery);
                 PreparedStatement psTrans = conn.prepareStatement(insertTransQuery)) {

                psItem.setInt(1, item.ownerId);
                psItem.setString(2, item.itemName);
                psItem.setInt(3, item.itemQuantity);
                psItem.setString(4, item.description);
                psItem.setString(5, item.itemImage);
                psItem.setString(6, item.category);
                psItem.setString(7, item.condition);
                psItem.setInt(8, item.price);
                psItem.setString(9, item.pickupArea);
                psItem.setString(10, item.pickupDays);
                psItem.setString(11, item.pickupTime);
                psItem.setString(12, item.action);
                psItem.executeUpdate();

                ResultSet rs = psItem.getGeneratedKeys();
                if (rs.next()) {
                    int generatedItemId = rs.getInt(1);

                    psLog.setInt(1, generatedItemId);
                    psLog.executeUpdate();

                    psTrans.setInt(1, generatedItemId);
                    psTrans.setInt(2, item.ownerId);
                    psTrans.setInt(3, item.itemQuantity);
                    psTrans.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Overwrites all core data fields of an existing item and pushes a custom reason message into the database log
    public static boolean updateItemDetailed(ItemRecord item, String logReason) {
        String updateItemQuery = "UPDATE items SET item_name = ?, item_quantity = ?, description = ?, item_image= ?, category = ?, `condition` = ?, price = ?, pickup_area = ?, pickup_days = ?, pickup_time = ?, maximum_borrow_days = ?, desired_item = ?, availability_status = CASE WHEN ? <= 0 THEN 'Unavailable' ELSE availability_status END WHERE item_id = ?";
        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Update', ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateItemQuery);
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery)) {

                psUpdate.setString(1, item.itemName);
                psUpdate.setInt(2, item.itemQuantity);
                psUpdate.setString(3, item.description);
                psUpdate.setString(4, item.itemImage);
                psUpdate.setString(5, item.category);
                psUpdate.setString(6, item.condition);
                psUpdate.setInt(7, item.price);
                psUpdate.setString(8, item.pickupArea);
                psUpdate.setString(9, item.pickupDays);
                psUpdate.setString(10, item.pickupTime);
                psUpdate.setInt(11, item.maximumBorrowDays);
                psUpdate.setString(12, item.desiredItem);
                psUpdate.setInt(13, item.itemQuantity);
                psUpdate.setInt(14, item.itemId);
                psUpdate.executeUpdate();

                psLog.setInt(1, item.itemId);
                psLog.setString(2, logReason);
                psLog.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Toggles the soft-delete archival flag, updates the deletion timestamp, and logs the reason for removal
    public static boolean deleteArchivedItem(int itemId, String logReason) {
        String updateItemQuery = "UPDATE items SET items_is_archived = 1, items_archived_at = CURRENT_TIMESTAMP WHERE item_id = ?";
        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Archive', ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateItemQuery);
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery)) {

                psUpdate.setInt(1, itemId);
                psUpdate.executeUpdate();

                psLog.setInt(1, itemId);
                psLog.setString(2, logReason);
                psLog.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Submits an interaction entry (such as a claim or bid request) into the system transaction tables
    public static boolean requestActionItem(int itemId, int requesterId, int quantity, String actionType) {
        String insertTransQuery = "INSERT INTO transactions (item_id, borrower_id, quantity, action) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword());
             PreparedStatement psTrans = conn.prepareStatement(insertTransQuery)) {

            psTrans.setInt(1, itemId);
            psTrans.setInt(2, requesterId);
            psTrans.setInt(3, quantity);
            psTrans.setString(4, actionType);
            psTrans.executeUpdate();
            return true;

            
       
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Saves a free peer-to-peer item allocation entry specifying the exact maximum timeframe allowed for loans
    public static boolean addSharingItem(ItemRecord item) {
        String insertItemQuery = "INSERT INTO items (owner_id, item_name, item_quantity, description, item_image, category, `condition`, price, pickup_area, pickup_days, pickup_time, maximum_borrow_days, action) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Create', 'Created a Sharing Item')";
        String insertTransQuery = "INSERT INTO transactions (item_id, borrower_id, quantity, action) VALUES (?, ?, ?, 'Lend')";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psItem = conn.prepareStatement(insertItemQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery);
                 PreparedStatement psTrans = conn.prepareStatement(insertTransQuery)) {

                psItem.setInt(1, item.ownerId);
                psItem.setString(2, item.itemName);
                psItem.setInt(3, item.itemQuantity);
                psItem.setString(4, item.description);
                psItem.setString(5, item.itemImage);
                psItem.setString(6, item.category);
                psItem.setString(7, item.condition);
                psItem.setInt(8, item.price);
                psItem.setString(9, item.pickupArea);
                psItem.setString(10, item.pickupDays);
                psItem.setString(11, item.pickupTime);
                psItem.setInt(12, item.maximumBorrowDays);
                psItem.setString(13, item.action);
                psItem.executeUpdate();

                ResultSet rs = psItem.getGeneratedKeys();
                if (rs.next()) {
                    int generatedItemId = rs.getInt(1);

                    psLog.setInt(1, generatedItemId);
                    psLog.executeUpdate();

                    psTrans.setInt(1, generatedItemId);
                    psTrans.setInt(2, item.ownerId);
                    psTrans.setInt(3, item.itemQuantity);
                    psTrans.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Dynamic SQL processor that runs visibility changes, archival routines, status adjustments, and tracking histories
 // Dynamic SQL processor that runs visibility changes, archival routines, status adjustments, and tracking histories
    public static boolean processRequestApproval(int itemId, int requesterId, int quantity, String newStatus, String logReason, String transAction, boolean archiveItem, String updateActionColumn, String proposedItem) {
        StringBuilder updateItemQueryStr = new StringBuilder("UPDATE items SET ");
        
        boolean needsComma = false;
        if (newStatus != null) {
            updateItemQueryStr.append("availability_status = ?");
            needsComma = true;
        }
        if (archiveItem) {
            if (needsComma) updateItemQueryStr.append(", ");
            updateItemQueryStr.append("items_is_archived = 1, items_archived_at = CURRENT_TIMESTAMP");
            needsComma = true;
        }
        if (updateActionColumn != null) {
            if (needsComma) updateItemQueryStr.append(", ");
            updateItemQueryStr.append("action = ?");
            needsComma = true;
        }
        if (proposedItem != null) {
             if (needsComma) updateItemQueryStr.append(", ");
             updateItemQueryStr.append("desired_item = ?");
             needsComma = true;
        }
        updateItemQueryStr.append(" WHERE item_id = ?");

        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Update', ?)";
        String insertTransQuery = "INSERT INTO transactions (item_id, borrower_id, quantity, action, proposed_item) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateItemQueryStr.toString());
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery);
                 PreparedStatement psTrans = conn.prepareStatement(insertTransQuery)) {

                // 1. Update Items Table
                int paramIndex = 1;
                if (newStatus != null) {
                    psUpdate.setString(paramIndex++, newStatus);
                }
                if (updateActionColumn != null) {
                    psUpdate.setString(paramIndex++, updateActionColumn);
                }
                if (proposedItem != null) {
                    psUpdate.setString(paramIndex++, proposedItem);
                }
                psUpdate.setInt(paramIndex, itemId);
                psUpdate.executeUpdate();

                // 2. Update Logs Table
                psLog.setInt(1, itemId);
                psLog.setString(2, logReason);
                psLog.executeUpdate();

                // 3. Update Transactions Table (FIXED ORDER)
                psTrans.setInt(1, itemId);
                psTrans.setInt(2, requesterId);
                psTrans.setInt(3, quantity);
                psTrans.setString(4, transAction);
                
                // Safe null handling for MySQL
                if (proposedItem != null) {
                    psTrans.setString(5, proposedItem);
                } else {
                    psTrans.setNull(5, java.sql.Types.VARCHAR);
                }
                
                psTrans.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Creates an item entry bound specifically to item-for-item exchange requests along with target criteria specs
    public static boolean addTradeItem(ItemRecord item) {
        String insertItemQuery = "INSERT INTO items (owner_id, item_name, item_quantity, description, item_image, category, `condition`, price, pickup_area, pickup_days, pickup_time, desired_item, action) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertLogQuery = "INSERT INTO items_log (item_id, action, reason) VALUES (?, 'Create', 'Created a Trade Item')";
        String insertTransQuery = "INSERT INTO transactions (item_id, borrower_id, quantity, action) VALUES (?, ?, ?, 'Trade-Initiate')";

        try (Connection conn = DriverManager.getConnection(DatabaseManager.getURL(), DatabaseManager.getUser(), DatabaseManager.getPassword())) {
            conn.setAutoCommit(false);

            try (PreparedStatement psItem = conn.prepareStatement(insertItemQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psLog = conn.prepareStatement(insertLogQuery);
                 PreparedStatement psTrans = conn.prepareStatement(insertTransQuery)) {

                psItem.setInt(1, item.ownerId);
                psItem.setString(2, item.itemName);
                psItem.setInt(3, item.itemQuantity);
                psItem.setString(4, item.description);
                psItem.setString(5, item.itemImage);
                psItem.setString(6, item.category);
                psItem.setString(7, item.condition);
                psItem.setInt(8, item.price);
                psItem.setString(9, item.pickupArea);
                psItem.setString(10, item.pickupDays);
                psItem.setString(11, item.pickupTime);
                psItem.setString(12, item.desiredItem);
                psItem.setString(13, item.action);
                psItem.executeUpdate();

                ResultSet rs = psItem.getGeneratedKeys();
                if (rs.next()) {
                    int generatedItemId = rs.getInt(1);

                    psLog.setInt(1, generatedItemId);
                    psLog.executeUpdate();

                    psTrans.setInt(1, generatedItemId);
                    psTrans.setInt(2, item.ownerId);
                    psTrans.setInt(3, item.itemQuantity);
                    psTrans.executeUpdate();
                }

                conn.commit();
                
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}