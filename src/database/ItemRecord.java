package database;

import java.sql.Date;
import java.sql.Timestamp;

public class ItemRecord {
	public int itemId;
	public int ownerId;
	public String itemName;
	public int itemQuantity;
	public String description;
	public String category;
	public String itemCondition;
	public int price;
	public String availabilityStatus;
	public int maximumBorrowDays;
	public String desiredItem;
	public Timestamp dateListed;
	public String pickupArea;
	public String pickupTime;
	public Date pickupDate;
	public String action;
}