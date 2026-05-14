package admin.models;



public class AdminLogs {

    private int id;
    private String logType;     
    private int userId;
    private int itemId;
    private String description;
    private String createdAt;  
    private boolean archived;



    public int getId(){ return id; }
    public String getLogType(){ return logType; }
    public int getUserId(){ return userId; }
    public int getItemId(){ return itemId; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public boolean isArchived() { return archived; }



    public void setId(int id) { this.id = id; }
    public void setLogType(String logType) { this.logType = logType; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setDescription(String description){ this.description = description; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setArchived(boolean archived) { this.archived = archived; }
}