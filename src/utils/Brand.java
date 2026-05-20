package utils;

import java.awt.*;
import enums.Condition;
import enums.Category;
import java.util.HashMap;
import java.util.Map;

public class Brand {
	public static final Color PRIMARY_COLOR = Color.decode("#201757");	
	public static final Color SECONDARY_COLOR = Color.decode("#f9cb0f");
	public static final Color BACKGROUND_COLOR = Color.decode("#201757");
	
	public static final Color RED = Color.decode("#dc3545");
	public static final Color GREEN = Color.decode("#28a745");
	public static final Color YELLOW = Color.decode("#ffc107");
	
	public static final float HEADER1_TEXT_SIZE = 38f;
	public static final float HEADER2_TEXT_SIZE = 30f;
	public static final float HEADER3_TEXT_SIZE = 24f;
	public static final float HEADER4_TEXT_SIZE = 20f;
	public static final float SUBHEADER_TEXT_SIZE = 16f;
	public static final float STANDARD_TEXT_SIZE = 14f;

	private static final Map<Category, Color> CATEGORY_COLORS = new HashMap<>();
	private static final Map<Condition, Color> CONDITION_COLORS = new HashMap<>();

	static {
		CATEGORY_COLORS.put(Category.TEXTBOOKS, Color.decode("#5eb061"));
		CATEGORY_COLORS.put(Category.ELECTRONICS, Color.decode("#e85a26"));
		CATEGORY_COLORS.put(Category.EQUIPMENT, Color.decode("#548EE4"));
		CATEGORY_COLORS.put(Category.SUPPLIES, Color.decode("#6c5ec1"));
		CATEGORY_COLORS.put(Category.CONSUMABLE_GOODS, Color.decode("#ff50be"));
		CATEGORY_COLORS.put(Category.OTHER, Color.decode("#737373"));

		CONDITION_COLORS.put(Condition.NEW, Color.decode("#37ad00"));
		CONDITION_COLORS.put(Condition.GOOD, Color.decode("#f9cb0f"));
		CONDITION_COLORS.put(Condition.FAIR, Color.decode("#ff8550"));
	}

	public static Color getCategoryColor(Category category) {
		return CATEGORY_COLORS.get(category);
	}

	public static Color getConditionColor(Condition condition) {
		return CONDITION_COLORS.get(condition);
	}
	
	public static final Color COLOR_GRID = new Color(235, 235, 237);
	public static final Color COLOR_BORDER = new Color(220, 220, 225);
}