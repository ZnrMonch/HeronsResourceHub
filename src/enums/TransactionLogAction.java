package enums;

public enum TransactionLogAction {

    SELL("Sell",                   "Item listed for sale"),
    SELL_RELISTED("Sell-Relisted", "Sale item relisted"),
    SELL_WITHDRAWN("Sell-Withdrawn","Sale item withdrawn"),

    BUY("Buy", "Item purchased"),

    LEND("Lend",                     "Item listed for lending"),
    LEND_RELISTED("Lend-Relisted",   "Lend item relisted"),
    LEND_WITHDRAWN("Lend-Withdrawn", "Lend item withdrawn"),

    BORROW_REQUEST("Borrow-Request",   "Borrow request submitted"),
    BORROW_APPROVED("Borrow-Approved", "Borrow request approved"),
    BORROW_DECLINED("Borrow-Declined", "Borrow request declined"),
    BORROW_RETURN("Borrow-Return",     "Borrowed item returned"),

    TRADE_RELISTED("Trade-Relisted",   "Trade item relisted"),
    TRADE_WITHDRAWN("Trade-Withdrawn", "Trade item withdrawn"),
    TRADE_INITIATE("Trade-Initiate",   "Trade initiated"),
    TRADE_REQUEST("Trade-Request",     "Trade request submitted"),
    TRADE_APPROVED("Trade-Approved",   "Trade request approved"),
    TRADE_DECLINED("Trade-Declined",   "Trade request declined");

    private final String dbValue;
    private final String description;

    TransactionLogAction(String dbValue, String description) {
        this.dbValue     = dbValue;
        this.description = description;
    }

    
    public String getDbValue() { return dbValue; }


    public String getDescription() { return description; }
}