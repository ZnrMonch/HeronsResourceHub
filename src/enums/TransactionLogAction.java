package enums;

public enum TransactionLogAction {
    TRANSACTION_BUY("Item purchased successfully."),
    TRANSACTION_SELL("Item sold successfully."),
    TRANSACTION_CANCEL("Transaction cancelled."),
    TRANSACTION_COMPLETE("Transaction completed successfully."),
    PAYMENT_COMPLETED("Payment completed successfully."),
    PAYMENT_FAILED("Payment failed."),
    TRADE_INITIATE("Trade request initiated."),
    TRADE_ACCEPT("Trade request accepted."),
    TRADE_DECLINE("Trade request declined."),
    TRADE_CANCEL("Trade request cancelled."),
    TRADE_COMPLETE("Trade completed successfully."),
    BORROW_REQUEST("Borrow request submitted."),
    BORROW_APPROVE("Borrow request approved."),
    BORROW_DECLINE("Borrow request declined."),
    BORROW_RETURN("Borrowed item returned successfully."),
    BORROW_CANCEL("Borrow request cancelled."),
    BORROW_COMPLETE("Borrow transaction completed.");

    private final String description;

    TransactionLogAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String format(String... replacements) {
        String result = description;
        String[] keys = { "{user_name}", "{item_name}", "{role}", "{field}" };
        for (int i = 0; i < replacements.length && i < keys.length; i++) {
            result = result.replace(keys[i], replacements[i]);
        }
        return result;
    }
    
    public String getFormattedName() {
        String[] words = this.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}