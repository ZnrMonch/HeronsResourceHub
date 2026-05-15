package database;

import java.sql.Timestamp;

public class ItemRecord {
	public int itemId;
	public int ownerId;
	public String initiatorFirstName;	// ADDED: matches initiator_firstname
	public String initiatorLastName;	// ADDED: matches initiator_lastname
	public String itemName;
	public int itemQuantity;
	public String description;
	public String itemsImage;			// CHANGED: renamed from itemImage to itemsImage to exactly match items_image
	public String category;
	public String condition;			// CHANGED: renamed to condition to exactly match SQL column
	public int price;
	public String availabilityStatus;
	public int maximumBorrowDays;
	public String desiredItem;
	public Timestamp dateListed;
	public String pickupArea;
	public String pickupTime;
	public String pickupDays;			// CHANGED: datatype changed from java.sql.Date to String (to store the SET of days)
	public String action;
}