package items;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;
import components.*;
import utils.*;

public class ItemPanel extends CustomPanel {
	private static final long serialVersionUID = 1L;
	private View view = View.GRID;
	private CustomButton backButton;
	
	public ItemPanel() {
		setBackground(Color.WHITE);
		setPadding(20);
		setLayout(new BorderLayout(20, 20));
		
		restoreView();
	}

	private void restoreView() {
		removeAll();
		if (View.GRID.equals(view)) {
			initGridView();
		} else {
			initListView();
		}
		initNav();
		revalidate();
		repaint();
	}

	private void showItemView() {
		removeAll();
		viewItem();
		revalidate();
		repaint();
	}

	private void initGridView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new GridLayout(2, 4, 10, 10));
		
		for (int i = 0; i < 8; i++) {
			Item exampleCard = new Item(View.GRID);
			exampleCard.setName("Example Item " + (i + 1));
			wrapper.add(exampleCard);
		}
		
		add(wrapper, BorderLayout.CENTER);
	}
	
	private void initListView() {
		CustomPanel wrapper = new CustomPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBackground(Color.WHITE);
		wrapper.setPadding(10, 20);
		
		for (int i = 0; i < 8; i++) {
			Item exampleCard = new Item(View.LIST);
			exampleCard.setName("Example Item " + (i + 1));
			exampleCard.setPreferredSize(new Dimension(0, 100));
			exampleCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
			wrapper.add(exampleCard);
			wrapper.add(Box.createVerticalStrut(10));
		}
		
		JScrollPane scrollPane = new JScrollPane(wrapper);
		scrollPane.setBorder(null);
		scrollPane.setBackground(Color.WHITE);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		add(scrollPane, BorderLayout.CENTER);
	}
	
	private void initNav() {
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
        
        add(navWrapper, BorderLayout.SOUTH);
	}
	
	private void viewItem() {
		CustomPanel wrapper = new CustomPanel(new BorderLayout(30, 20));
		
		CustomPanel header = new CustomPanel(new BorderLayout());
		backButton = new CustomButton(IconLoader.loadAndScaleIcon("/resources/icons/arrow-left.png", 40, 40), 0);
		backButton.setTransparent();
		backButton.addActionListener(e -> restoreView());
		header.add(backButton, BorderLayout.WEST);
		wrapper.add(header, BorderLayout.NORTH);
		
		CustomPanel westWrapper = new CustomPanel();
		westWrapper.setLayout(new BoxLayout(westWrapper, BoxLayout.Y_AXIS));
		westWrapper.setPreferredSize(new Dimension(400, Integer.MAX_VALUE));

		JLabel imgLabel = new JLabel(IconLoader.loadAndScaleIcon("/resources/images/umak_img.jpg", 400, 300)); // Safely defaulting to a known image
		imgLabel.setPreferredSize(new Dimension(400, 300));
		imgLabel.setMaximumSize(new Dimension(400, 300));
		imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		CustomButton actionButton = new CustomButton("Buy Now", 10);
		actionButton.setPreferredSize(new Dimension(400, 35));
		actionButton.setMaximumSize(new Dimension(400, 35));
		actionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		westWrapper.add(imgLabel);
		westWrapper.add(Box.createVerticalStrut(20));
		westWrapper.add(actionButton);
		
		CustomPanel centerWrapper = new CustomPanel();
		centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
		
		CustomLabel nameLabel = new CustomLabel("COMPUTER PROGRAMMING 2 BOOK", Brand.HEADER1_TEXT_SIZE, FontStyle.BOLD);
		CustomLabel initiatorLabel = new CustomLabel("Renzjan Moncinilla", Brand.SUBHEADER_TEXT_SIZE, FontStyle.REGULAR);
		initiatorLabel.setForeground(Color.GRAY);
		
		CustomLabel priceLabel = new CustomLabel("\u20B1 999.00", Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD);
		CustomLabel quantityLabel = new CustomLabel("Quantity: 5 available", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
		
		CustomLabel descTitle = new CustomLabel("Product Description:", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);

		centerWrapper.add(nameLabel);
		centerWrapper.add(Box.createVerticalStrut(5));
		centerWrapper.add(initiatorLabel);
		centerWrapper.add(Box.createVerticalStrut(20));
		centerWrapper.add(priceLabel);
		centerWrapper.add(Box.createVerticalStrut(5));
		centerWrapper.add(quantityLabel);
		centerWrapper.add(Box.createVerticalStrut(20));
		centerWrapper.add(descTitle);
		
		wrapper.add(westWrapper, BorderLayout.WEST);
		wrapper.add(centerWrapper, BorderLayout.CENTER);
		
		add(wrapper, BorderLayout.CENTER);
	}
	
	public View getView() {
		return view;
	}

	public void setView(View view) {
		if (this.view != view) {
			this.view = view;
			restoreView();
		}
	}
	
	private class Item extends CustomPanel {
		private static final long serialVersionUID = 1L;
		private String image = "/resources/images/umak_img.jpg";
		private String name;
		private String description;
		private String initiator;
		private Category category;
		private Condition condition;
		private Availability availability;
		private int price;
		private CustomButton actionButton;
		
		private CustomLabel nameLabel;
		
		public Item(View view) {
			if (View.GRID.equals(view)) {
				initCardLayout();
			} else {
				initListLayout();
			}
		}
		
		private void initCardLayout() {
			setBackground(Color.WHITE);
			setLayout(new BorderLayout(0, 10));
			setBorder(1, Color.LIGHT_GRAY);
			setPadding(10);
			setRadius(20);
			
			CustomPanel imgPanel = new CustomPanel() {
				private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : null;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (imgId != null) {
						g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
						int h = (int)(getWidth() * ((double)imgId.getHeight(null) / imgId.getWidth(null)));
						int y = (getHeight() - h) / 2;
						g.drawImage(imgId, 0, y, getWidth(), h, this);
					}
				}
			};
			imgPanel.setPreferredSize(new Dimension(200, 150));
			imgPanel.setOpaque(false);
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			
			CustomPanel infoWrapper = new CustomPanel();
			infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
			CustomLabel nameLabel = new CustomLabel(name != null ? name : "Item Name", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
			CustomLabel initiatorLabel = new CustomLabel(initiator != null ? "Initiator: " + initiator : "N/A");
			CustomLabel priceLabel = new CustomLabel(price > 0 ? "\u20B1" + price : "Free", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
			initiatorLabel.setForeground(Color.GRAY);
			infoWrapper.add(nameLabel);
			infoWrapper.add(initiatorLabel);
			
			CustomPanel actionWrapper = new CustomPanel(new BorderLayout());
			actionWrapper.add(priceLabel, BorderLayout.WEST);
			actionButton = new CustomButton("Buy Item");
			actionButton.setPadding(5, 10, 5, 10);
			actionButton.setRadius(15);
			actionButton.addActionListener(e -> showItemView());
			actionWrapper.add(actionButton, BorderLayout.EAST);
			
			contentPanel.add(infoWrapper, BorderLayout.NORTH);
			contentPanel.add(actionWrapper, BorderLayout.SOUTH);
			
			add(imgPanel, BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);
		}
		
		private void initListLayout() {
			setBackground(Color.WHITE);
			setLayout(new BorderLayout(10, 0));
			setBorder(1, Color.LIGHT_GRAY);
			setPadding(10);
			setRadius(20);
			
			CustomPanel imgPanel = new CustomPanel() {
				private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : null;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (imgId != null) {
						g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
						g.drawImage(imgId, 0, 0, getWidth(), getHeight(), this);
					}
				}
			};
			imgPanel.setPreferredSize(new Dimension(75, 75));
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel centerWrapper = new CustomPanel();
			CustomPanel textWrapper = new CustomPanel();
			CustomPanel eastWrapper = new CustomPanel(new GridBagLayout());

			centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
			CustomLabel nameLabel = new CustomLabel(name != null ? name : "Item Name", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
			CustomLabel descriptionLabel = new CustomLabel(description != null ? description : "Item Description", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setForeground(Color.GRAY);
			
			textWrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			textWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			textWrapper.add(category != null ? new CustomLabel(category.toString(), Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR) : new CustomLabel("Category", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(condition != null ? new CustomLabel(condition.toString(), Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR) : new CustomLabel("Condition", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(initiator != null ? new CustomLabel("by " + initiator, Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR) : new CustomLabel("by Someone", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR));
			
			centerWrapper.add(Box.createVerticalGlue());
			centerWrapper.add(nameLabel);
			centerWrapper.add(descriptionLabel);
			centerWrapper.add(textWrapper);
			centerWrapper.add(Box.createVerticalGlue());
			
			eastWrapper.setLayout(new BoxLayout(eastWrapper, BoxLayout.Y_AXIS));
			eastWrapper.add(Box.createVerticalGlue());
			actionButton = new CustomButton("Buy Item");
			actionButton.setPadding(5, 10, 5, 10);
			actionButton.setRadius(15);
			actionButton.addActionListener(e -> showItemView());
			eastWrapper.add(actionButton);
			eastWrapper.add(Box.createVerticalGlue());
			
			contentPanel.add(centerWrapper, BorderLayout.CENTER);
			contentPanel.add(eastWrapper, BorderLayout.EAST);
			add(imgPanel, BorderLayout.WEST);
			add(contentPanel, BorderLayout.CENTER);
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
			if (nameLabel != null) {
				nameLabel.setText(name);
			}
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