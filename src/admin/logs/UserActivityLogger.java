// ══════════════════════════════════════════════════════════════════════════════
// UserActivityLogger.java
// ──────────────────────────────────────────────────────────────────────────────
// Singleton logger used throughout the app to record what happened and who did it.
//
// WHY A SINGLETON?
//   One shared instance means every part of the app writes logs the same way,
//   through the same object, without passing it around as a parameter.
//
// HOW TO USE:
//   UserActivityLogger.get().user(UserLogAction.USER_ARCHIVE, targetUserId, "John Doe");
//   UserActivityLogger.get().transaction(TransactionLogAction.BUY, buyerId, itemId, "Bought notebook");
//
// RULE: Always call the logger AFTER the DB operation succeeds, never before.
// ══════════════════════════════════════════════════════════════════════════════
package admin.logs;

import admin.database.LogsDatabase;
import enums.ItemLogAction;
import enums.ReputationLogAction;
import enums.TransactionLogAction;
import enums.UserLogAction;
import utils.SessionManager;

public class UserActivityLogger {

    // ── Singleton setup ───────────────────────────────────────────────────────

    // The one and only instance, created when the class is first loaded.
    private static final UserActivityLogger INSTANCE = new UserActivityLogger();

    // Private so nobody can call "new UserActivityLogger()" from outside.
    private UserActivityLogger() {}

    /** Returns the single shared instance of this logger. */
    public static UserActivityLogger get() {
        return INSTANCE;
    }

    // ── Dependencies ──────────────────────────────────────────────────────────

    // The database layer that actually writes to the log tables.
    private final LogsDatabase logDB = new LogsDatabase();


    // ── Session-user convenience methods ─────────────────────────────────────
    //
    // These use whoever is currently logged in (via SessionManager) as the actor.
    // Use these when an admin is performing an action on their own account,
    // or when you are certain the session user IS the relevant user.

    /**
     * Logs a user-related action performed BY the current session user.
     *
     * @param action   What happened (e.g. USER_UPDATE_PROFILE)
     * @param context  Extra detail shown in the reason column (can be empty "")
     */
    public void user(UserLogAction action, String context) {
        int userId = SessionManager.get().getCurrentUserId();
        logDB.insertLog(action, userId, context);
    }

    /**
     * Logs an item-related action performed BY the current session user.
     *
     * @param action   What happened (e.g. ITEM_ARCHIVE)
     * @param itemId   The item this action was performed on
     * @param context  Extra detail (can be empty "")
     */
    public void item(ItemLogAction action, int itemId, String context) {
        int userId = SessionManager.get().getCurrentUserId();
        logDB.insertLog(action, userId, itemId, context);
    }

    /**
     * Logs a transaction action performed BY the current session user.
     *
     * @param action   What happened (e.g. TransactionLogAction.BUY)
     * @param itemId   The item involved in the transaction
     * @param context  Extra detail (can be empty "")
     */
    public void transaction(TransactionLogAction action, int itemId, String context) {
        int userId = SessionManager.get().getCurrentUserId();
        logDB.insertLog(action, userId, itemId, context);
    }

    /**
     * Logs a reputation change for the current session user.
     *
     * @param action   What happened (e.g. REPUTATION_EARNED)
     * @param context  Extra detail (can be empty "")
     */
    public void reputation(ReputationLogAction action, String context) {
        int userId = SessionManager.get().getCurrentUserId();
        logDB.insertLog(action, userId, context);
    }


    // ── Explicit-userId overloads ─────────────────────────────────────────────
    //
    // Use these when the action is being recorded for a SPECIFIC user who may
    // NOT be the currently logged-in admin.
    //
    // Examples:
    //   - An admin archives a student's account  → pass the student's userId
    //   - A buyer completes a purchase            → pass the buyer's userId
    //   - A lender approves a borrow request      → pass the lender's userId

    /**
     * Logs a user-related action for a specific user (not necessarily the session user).
     *
     * @param action   What happened
     * @param userId   The user this action is being recorded FOR
     * @param context  Extra detail (can be empty "")
     */
    public void user(UserLogAction action, int userId, String context) {
        logDB.insertLog(action, userId, context);
    }

    /**
     * Logs an item-related action for a specific user.
     *
     * @param action   What happened
     * @param userId   The user this action is being recorded FOR
     * @param itemId   The item involved
     * @param context  Extra detail (can be empty "")
     */
    public void item(ItemLogAction action, int userId, int itemId, String context) {
        logDB.insertLog(action, userId, itemId, context);
    }

    /**
     * Logs a transaction action for a specific user.
     *
     * @param action   What happened
     * @param userId   The user this action is being recorded FOR
     * @param itemId   The item involved
     * @param context  Extra detail (can be empty "")
     */
    public void transaction(TransactionLogAction action, int userId, int itemId, String context) {
        logDB.insertLog(action, userId, itemId, context);
    }

    /**
     * Logs a reputation change for a specific user.
     *
     * @param action   What happened
     * @param userId   The user this action is being recorded FOR
     * @param context  Extra detail (can be empty "")
     */
    public void reputation(ReputationLogAction action, int userId, String context) {
        logDB.insertLog(action, userId, context);
    }
}