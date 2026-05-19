package marketplace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KarmaManager {

    public static final int MAX_TOTAL_KARMA = 1000;
    public static final int MAX_DAILY_KARMA = 50;
    public static final int MAX_USER_PAIR_TX_PER_DAY = 2;

    public static int getBaseKarma(String action) {
        switch(action) {
            case "Buy": return 2;
            case "Sell": return 5;
            case "Lend": return 10;
            case "Borrow Return Completed": return 15;
            case "Trade Completed": return 8;
            default: return 0; // Requests, Declines, List Created all return 0
        }
    }

    public static int getQuantityBonus(int quantity) {
        if (quantity >= 6) return 5;
        if (quantity >= 4) return 3;
        if (quantity >= 2) return 1;
        return 0; // 1 item yields +0 bonus
    }

    // Must be called with an active connection to ensure it stays in the same transaction
    public static void awardKarma(Connection conn, int targetUserId, int interactingUserId, int transactionId, String action, int quantity) throws SQLException {
        int baseKarma = getBaseKarma(action);
        if (baseKarma <= 0) return; // Ignore actions that don't yield points

        int pointsToAward = baseKarma + getQuantityBonus(quantity);

        // 1. Check User Pair Limit (Only first 2 successful transactions per user pair per day)
        String pairQuery = "SELECT COUNT(*) FROM reputation_log rl " +
                           "JOIN transactions t ON rl.transaction_id = t.transaction_id " +
                           "WHERE rl.user_id = ? AND DATE(rl.timestamp) = CURDATE() " +
                           "AND (t.borrower_id = ? OR t.item_id IN (SELECT item_id FROM items WHERE owner_id = ?))";
        
        try (PreparedStatement psPair = conn.prepareStatement(pairQuery)) {
            psPair.setInt(1, targetUserId);
            psPair.setInt(2, interactingUserId);
            psPair.setInt(3, interactingUserId);
            try (ResultSet rsPair = psPair.executeQuery()) {
                if (rsPair.next() && rsPair.getInt(1) >= MAX_USER_PAIR_TX_PER_DAY) {
                    return; // Pair limit reached today, abort awarding
                }
            }
        }

        // 2. Check Daily Limit (Maximum 50 points per day)
        String dailyQuery = "SELECT SUM(points_earned) FROM reputation_log WHERE user_id = ? AND DATE(timestamp) = CURDATE()";
        try (PreparedStatement psDaily = conn.prepareStatement(dailyQuery)) {
            psDaily.setInt(1, targetUserId);
            try (ResultSet rsDaily = psDaily.executeQuery()) {
                if (rsDaily.next()) {
                    int earnedToday = rsDaily.getInt(1);
                    if (earnedToday + pointsToAward > MAX_DAILY_KARMA) {
                        pointsToAward = MAX_DAILY_KARMA - earnedToday;
                    }
                }
            }
        }

        if (pointsToAward <= 0) return; // Stop if daily limit reached

        // 3. Check Total Absolute Limit (1000 points maximum)
        String totalQuery = "SELECT karma_score FROM users WHERE user_id = ?";
        try (PreparedStatement psTotal = conn.prepareStatement(totalQuery)) {
            psTotal.setInt(1, targetUserId);
            try (ResultSet rsTotal = psTotal.executeQuery()) {
                if (rsTotal.next()) {
                    int currentKarma = rsTotal.getInt(1);
                    if (currentKarma + pointsToAward > MAX_TOTAL_KARMA) {
                        pointsToAward = MAX_TOTAL_KARMA - currentKarma;
                    }
                }
            }
        }

        if (pointsToAward <= 0) return; // Stop if absolute limit reached

        // 4. Execute Updates 
        // Insert into reputation_log
        String insertLog = "INSERT INTO reputation_log (user_id, transaction_id, points_earned) VALUES (?, ?, ?)";
        try (PreparedStatement psInsertLog = conn.prepareStatement(insertLog)) {
            psInsertLog.setInt(1, targetUserId);
            psInsertLog.setInt(2, transactionId);
            psInsertLog.setInt(3, pointsToAward);
            psInsertLog.executeUpdate();
        }

        // Update total karma_score in the users table
        String updateUser = "UPDATE users SET karma_score = karma_score + ? WHERE user_id = ?";
        try (PreparedStatement psUpdateUser = conn.prepareStatement(updateUser)) {
            psUpdateUser.setInt(1, pointsToAward);
            psUpdateUser.setInt(2, targetUserId);
            psUpdateUser.executeUpdate();
        }

        // Update the accumulated karma_impact record strictly for this transaction table record
        String updateTrans = "UPDATE transactions SET karma_impact = karma_impact + ? WHERE transaction_id = ?";
        try (PreparedStatement psUpdateTrans = conn.prepareStatement(updateTrans)) {
            psUpdateTrans.setInt(1, pointsToAward);
            psUpdateTrans.setInt(2, transactionId);
            psUpdateTrans.executeUpdate();
        }
    }
}