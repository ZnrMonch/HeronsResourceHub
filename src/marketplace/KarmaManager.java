package marketplace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/* * OVERALL EXPLANATION:
 * The KarmaManager class is like a reward system. 
 * Whenever users buy, sell, or trade items, they earn "Karma points".
 * This class calculates those points, checks if the user is breaking any rules 
 * (like trying to farm points with a friend), and saves the points to the database.
 */
public class KarmaManager {

    /* * EXPLANATION - THE RULES:
     * These are the absolute limits of the reward system.
     * 1. You can never have more than 1000 points.
     * 2. You can only earn a maximum of 50 points in a single day.
     * 3. You can only get points from the SAME person twice a day (stops friends from cheating).
     */
    public static final int MAX_TOTAL_KARMA = 1000;
    public static final int MAX_DAILY_KARMA = 50;
    public static final int MAX_USER_PAIR_TX_PER_DAY = 2;

    /*
     * EXPLANATION - BASE POINTS:
     * This function looks at what the user just did and gives them a starting score.
     * Harder/nicer actions give more points (like returning a borrowed item = 15).
     * Simple actions give less points (Buy = 2).
     */
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

    /*
     * EXPLANATION - BONUS POINTS:
     * This function gives extra points if the user is dealing with a lot of items at once.
     * - 6 or more items = +5 points
     * - 4 or 5 items = +3 points
     * - 2 or 3 items = +1 point
     * - Just 1 item = 0 bonus points
     */
    public static int getQuantityBonus(int quantity) {
        if (quantity >= 6) return 5;
        if (quantity >= 4) return 3;
        if (quantity >= 2) return 1;
        return 0; // 1 item yields +0 bonus
    }

    /*
     * EXPLANATION - THE MAIN REWARD FUNCTION:
     * This is the function that actually does the work. It takes the base points, 
     * adds the bonus points, checks all the rules, and then updates the database.
     */
    // Must be called with an active connection to ensure it stays in the same transaction
    public static void awardKarma(Connection conn, int targetUserId, int interactingUserId, int transactionId, String action, int quantity) throws SQLException {
        
        // Step A: Get the starting points. If the action doesn't give points (like declining a trade), stop here.
        int baseKarma = getBaseKarma(action);
        if (baseKarma <= 0) return; // Ignore actions that don't yield points

        // Step B: Add the base points and the quantity bonus together.
        int pointsToAward = baseKarma + getQuantityBonus(quantity);


        /* * EXPLANATION - RULE 1 (No Cheating with Friends):
         * We ask the database: "How many times have these two specific users traded today?"
         * If they have already traded 2 times today, we STOP and give no points.
         */
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


        /* * EXPLANATION - RULE 2 (Daily Limit):
         * We ask the database: "How many points has this user earned today?"
         * If they earned 45 points, and are about to get 10 more, they would hit 55.
         * Since the daily max is 50, we shrink their reward to just 5 points.
         */
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

        // If the daily limit adjustment squished their points to 0, stop here.
        if (pointsToAward <= 0) return; // Stop if daily limit reached


        /* * EXPLANATION - RULE 3 (Total Max Limit):
         * We ask the database: "What is this user's all-time total score?"
         * If they have 995 points, and are about to get 10 more, they would hit 1005.
         * Since the absolute max is 1000, we shrink their reward to just 5 points.
         */
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

        // If the total limit adjustment squished their points to 0, stop here.
        if (pointsToAward <= 0) return; // Stop if absolute limit reached


        /* * EXPLANATION - SAVE THE REWARD:
         * The user passed all the rules! Now we save the points in 3 different places
         * to make sure the database is fully updated.
         */
        // 4. Execute Updates 
        
        // Save #1: Write it in the history log (so we can see exactly when they got points).
        String insertLog = "INSERT INTO reputation_log (user_id, transaction_id, points_earned) VALUES (?, ?, ?)";
        try (PreparedStatement psInsertLog = conn.prepareStatement(insertLog)) {
            psInsertLog.setInt(1, targetUserId);
            psInsertLog.setInt(2, transactionId);
            psInsertLog.setInt(3, pointsToAward);
            psInsertLog.executeUpdate();
        }

        // Save #2: Update the user's main profile score.
        String updateUser = "UPDATE users SET karma_score = karma_score + ? WHERE user_id = ?";
        try (PreparedStatement psUpdateUser = conn.prepareStatement(updateUser)) {
            psUpdateUser.setInt(1, pointsToAward);
            psUpdateUser.setInt(2, targetUserId);
            psUpdateUser.executeUpdate();
        }

        // Save #3: Mark the transaction to show that it generated these points.
        String updateTrans = "UPDATE transactions SET karma_impact = karma_impact + ? WHERE transaction_id = ?";
        try (PreparedStatement psUpdateTrans = conn.prepareStatement(updateTrans)) {
            psUpdateTrans.setInt(1, pointsToAward);
            psUpdateTrans.setInt(2, transactionId);
            psUpdateTrans.executeUpdate();
        }
    }
}