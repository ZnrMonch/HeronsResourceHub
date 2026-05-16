package admin.logs;

import admin.database.LogsDatabase;
import enums.ItemLogAction;
import enums.ReputationLogAction;
import enums.TransactionLogAction;
import enums.UserLogAction;
import utils.SessionManager;

public class UserActivityLogger {

    private static final UserActivityLogger INSTANCE = new UserActivityLogger();

    private UserActivityLogger() {}

    public static UserActivityLogger get() {
        return INSTANCE;
    }

    private final LogsDatabase logDB = new LogsDatabase();

    
    private int currentUserId() {
        return SessionManager.get().getCurrentUserId();
    }
  
    public void transaction(TransactionLogAction action, int itemId, String context) {
        int userId = currentUserId();
        logDB.insertLog(action, userId, itemId, context);
    }

   
    public void user(UserLogAction action, String fullName) {
        int userId = currentUserId();
        logDB.insertLog(action, userId, fullName);
    }

  
    public void item(ItemLogAction action, int itemId, String context) {
        int userId = currentUserId();
        logDB.insertLog(action, userId, itemId, context);
    }

  
    public void reputation(ReputationLogAction action, String context) {
        int userId = currentUserId();
        logDB.insertLog(action, userId, context);
    }
}