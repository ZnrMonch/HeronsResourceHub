package database;

import java.sql.Timestamp;

public class ItemRecord {
	public int itemId;
	public int ownerId;
	public String itemName;
	public int itemQuantity;
	public String description;
	public String itemImage;
	public String category;
	public String condition;
	public int price;
	public String availabilityStatus;
	public int maximumBorrowDays;
	public String desiredItem;
	public Timestamp dateListed;
	public String pickupArea;
	public String pickupTime;
	public String pickupDays;
	public String action;
	public boolean itemsIsArchived;
	public Timestamp itemsArchivedAt;

	public String initiatorFirstName;
	public String initiatorLastName;
}