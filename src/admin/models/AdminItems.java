package admin.models;

// Blueprint for one row from the items table.

public class AdminItems {

    private int itemId;
    private int ownerId;
    private String itemName;
    private int itemQuantity;
    private String description;
    private String category;         
    private String itemCondition;   
    private int price;
    private String availabilityStatus; 
    private String action;         
    private boolean archived;

   

    public int getItemId()                  { return itemId; }
    public int getOwnerId()                 { return ownerId; }
    public String getItemName()             { return itemName; }
    public int getItemQuantity()            { return itemQuantity; }
    public String getDescription()          { return description; }
    public String getCategory()             { return category; }
    public String getItemCondition()        { return itemCondition; }
    public int getPrice()                   { return price; }
    public String getAvailabilityStatus()   { return availabilityStatus; }
    public String getAction()               { return action; }
    public boolean getArchived()             { return archived; }


    public void setItemId(int itemId)                       { this.itemId = itemId; }
    public void setOwnerId(int ownerId)                     { this.ownerId = ownerId; }
    public void setItemName(String itemName)                { this.itemName = itemName; }
    public void setItemQuantity(int itemQuantity)           { this.itemQuantity = itemQuantity; }
    public void setDescription(String description)          { this.description = description; }
    public void setCategory(String category)                { this.category = category; }
    public void setItemCondition(String itemCondition)      { this.itemCondition = itemCondition; }
    public void setPrice(int price)                         { this.price = price; }
    public void setAvailabilityStatus(String status)        { this.availabilityStatus = status; }
    public void setAction(String action)                    { this.action = action; }
    public void setArchived(boolean archived)               { this.archived = archived; }
}