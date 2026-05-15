package database;

<<<<<<< HEAD
import java.sql.Date;
=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
import java.sql.Timestamp;

public class ItemRecord {
	public int itemId;
	public int ownerId;
<<<<<<< HEAD
	public String itemName;
	public int itemQuantity;
	public String description;
	public String category;
	public String itemCondition;
=======
	public String initiatorFirstName;	// ADDED: matches initiator_firstname
	public String initiatorLastName;	// ADDED: matches initiator_lastname
	public String itemName;
	public int itemQuantity;
	public String description;
	public String itemsImage;			// CHANGED: renamed from itemImage to itemsImage to exactly match items_image
	public String category;
	public String condition;			// CHANGED: renamed to condition to exactly match SQL column
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	public int price;
	public String availabilityStatus;
	public int maximumBorrowDays;
	public String desiredItem;
	public Timestamp dateListed;
	public String pickupArea;
	public String pickupTime;
<<<<<<< HEAD
	public Date pickupDate;
=======
	public String pickupDays;			// CHANGED: datatype changed from java.sql.Date to String (to store the SET of days)
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	public String action;
}