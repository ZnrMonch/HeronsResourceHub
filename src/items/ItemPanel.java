package items;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class ItemPanel extends CustomPanel {
	private static final long serialVersionUID = 1L;
	
	public ItemPanel() {
		setBackground(Color.WHITE);
		setPadding(20);
		setLayout(new BorderLayout(20, 20));
		
		CustomPanel contentWrapper = new CustomPanel();
		contentWrapper.setLayout(new GridLayout(2, 4, 20, 20));
		
		for (int i = 0; i < 8; i++) {
			ItemCard exampleCard = new ItemCard();
			exampleCard.setName("Example Item " + (i + 1));
			contentWrapper.add(exampleCard);
		}
		
		CustomPanel navWrapper = new CustomPanel(new BorderLayout());
		
		CustomButton btnFirst = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-left.png", 20, 20, Color.WHITE), 5);
        CustomButton btnPrev = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-left.png", 20, 20, Color.WHITE), 5);
        CustomButton btnNext = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-right.png", 20, 20, Color.WHITE), 5);
        CustomButton btnLast = new CustomButton(IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-right.png", 20, 20, Color.WHITE), 5);
        
        CustomPanel wrapper = new CustomPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.add(btnFirst);
        wrapper.add(Box.createHorizontalStrut(5));
        wrapper.add(btnPrev);
        wrapper.add(Box.createHorizontalStrut(5));
        wrapper.add(btnNext);
        wrapper.add(Box.createHorizontalStrut(5));
        wrapper.add(btnLast);
        
        navWrapper.add(wrapper, BorderLayout.EAST);
        
        add(contentWrapper, BorderLayout.CENTER);
        add(navWrapper, BorderLayout.SOUTH);
		
	}
	
	private class ItemCard extends CustomPanel {
		private static final long serialVersionUID = 1L;
		private String name;
		private String description;
		private Category category;
		private Condition condition;
		private Availability availability;
		
		public ItemCard() {
			setBackground(Color.WHITE);
			setLayout(new BorderLayout());
			setBorder(1, Color.LIGHT_GRAY);
			setRadius(10);
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public Category getCategory() {
			return category;
		}

		public Condition getCondition() {
			return condition;
		}

		public Availability getAvailability() {
			return availability;
		}

		public void setName(String name) {
			this.name = name;
			revalidate();
			repaint();
		}

		public void setDescription(String description) {
			this.description = description;
			revalidate();
			repaint();
		}

		public void setCategory(Category category) {
			this.category = category;
			revalidate();
			repaint();
		}

		public void setCondition(Condition condition) {
			this.condition = condition;
			revalidate();
			repaint();
		}

		public void setAvailability(Availability availability) {
			this.availability = availability;
			revalidate();
			repaint();
		}
	}

}