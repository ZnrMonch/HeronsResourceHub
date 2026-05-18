
package admin.logs;

import admin.database.LogsDatabase;
import enums.ItemLogAction;
import enums.ReputationLogAction;
import enums.TransactionLogAction;
import enums.UserLogAction;
import utils.SessionManager;

//Singleton facade for inserting activity logs; resolves the current user automatically
public class UserActivityLogger {

 private static final UserActivityLogger INSTANCE = new UserActivityLogger();

 private UserActivityLogger() {}

 public static UserActivityLogger get() {
     return INSTANCE;
 }

 private final LogsDatabase logDB = new LogsDatabase();

 // ── Session-scoped overloads (userId resolved from active session) ──

 public void user(UserLogAction action, String context) {
     logDB.insertLog(action, SessionManager.get().getCurrentUserId(), context);
 }

 public void item(ItemLogAction action, int itemId, String context) {
     logDB.insertLog(action, SessionManager.get().getCurrentUserId(), itemId, context);
 }

 public void transaction(TransactionLogAction action, int itemId, String context) {
     logDB.insertLog(action, SessionManager.get().getCurrentUserId(), itemId, context);
 }

 public void reputation(ReputationLogAction action, String context) {
     logDB.insertLog(action, SessionManager.get().getCurrentUserId(), context);
 }

 // ── Explicit-userId overloads (used when logging on behalf of another user) ──

 public void user(UserLogAction action, int userId, String context) {
     logDB.insertLog(action, userId, context);
 }

 public void item(ItemLogAction action, int userId, int itemId, String context) {
     logDB.insertLog(action, userId, itemId, context);
 }

 public void transaction(TransactionLogAction action, int userId, int itemId, String context) {
     logDB.insertLog(action, userId, itemId, context);
 }

 public void reputation(ReputationLogAction action, int userId, String context) {
     logDB.insertLog(action, userId, context);
 }
}