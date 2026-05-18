package marketplace;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import components.*;
import enums.View;
import utils.*;
import database.*;
import enums.Category;
import enums.Condition;

public class ItemCard extends CustomPanel {
	private static final long serialVersionUID = 1L;
	private String image = "/resources/images/umak_img.jpg";
	
	private CustomLabel nameLabel = new CustomLabel("Item Name", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
	private CustomLabel descriptionLabel = new CustomLabel("Item Description", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	private CustomLabel quantityLabel = new CustomLabel("Stock: 1", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	private CustomLabel priceLabel = new CustomLabel("", Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD, Brand.PRIMARY_COLOR);
	private CustomLabel categoryLabel = new CustomLabel("Category", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	private CustomLabel conditionLabel = new CustomLabel("Condition", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD, Color.GRAY);
	private CustomLabel initiatorLabel = new CustomLabel("N/A", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	
	public ItemCard(View view, String actionText, ItemRecord record, boolean showPrice, ItemActionListener onAction) {
		setBackground(Color.WHITE);
		setBorder(1, Color.LIGHT_GRAY);
		setPadding(10);
		setRadius(20);
		
		if (record != null && record.itemImage != null && !record.itemImage.isEmpty()) {
			this.image = record.itemImage;
		}
		
		CustomPanel imgPanel = new CustomPanel() {
			private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : IconLoader.loadIcon("/resources/images/umak_img.jpg").getImage();
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (imgId != null) {
					g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
					if (View.GRID.equals(view)) {
						int h = (int)(getWidth() * ((double)imgId.getHeight(null) / imgId.getWidth(null)));
						g.drawImage(imgId, 0, (getHeight() - h) / 2, getWidth(), h, this);
					} else {
						g.drawImage(imgId, 0, 0, getWidth(), getHeight(), this);
					}
				}
			}
		};
		imgPanel.setOpaque(false);

		CustomButton actionButton = new CustomButton(actionText);
		actionButton.setPadding(5, 10, 5, 10);
		actionButton.setRadius(15);
		actionButton.addActionListener(e -> {
			if (onAction != null) {
				onAction.onItemAction(record);
			}
		});
		
		Color categoryColor = Color.GRAY;
		Color conditionColor = Color.GRAY;

		if (record != null) {
			nameLabel.setText(record.itemName);
			descriptionLabel.setText(record.description);
			priceLabel.setText(record.price > 0 ? String.format("P%.0f", (double) record.price) : "");
			categoryLabel.setText(record.category != null ? record.category : "");
			conditionLabel.setText(record.condition != null ? record.condition : "");
			
			if (record.category != null) {
				try {
					Category catEnum = Category.valueOf(record.category.toUpperCase().replace(" ", "_"));
					categoryColor = Brand.getCategoryColor(catEnum);
				} catch (IllegalArgumentException e) {
					categoryColor = Color.GRAY;
				}
			}

			if (record.condition != null) {
				try {
					Condition condEnum = Condition.valueOf(record.condition.toUpperCase().replace(" ", "_"));
					conditionColor = Brand.getConditionColor(condEnum);
				} catch (IllegalArgumentException e) {
					conditionColor = Color.GRAY;
				}
			}

			categoryLabel.setForeground(categoryColor);
			conditionLabel.setForeground(conditionColor);

			if (record.initiatorFirstName != null && record.initiatorLastName != null) {
				initiatorLabel.setText(record.initiatorFirstName + " " + record.initiatorLastName);
			} else {
				initiatorLabel.setText("[100] Someone");
			}
		}

		if (View.GRID.equals(view)) {
			setLayout(new BorderLayout(0, 10));
			imgPanel.setPreferredSize(new Dimension(200, 150));
			
			imgPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			
			CustomPanel categoryBadge = new CustomPanel();
			categoryBadge.setBackground(categoryColor);
			categoryBadge.setRadius(15);
			categoryBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
			
			String catText = (record != null && record.category != null) ? record.category : "Category";
			CustomLabel topRightImgLabel = new CustomLabel(catText, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD, Color.WHITE);
			categoryBadge.add(topRightImgLabel);
			
			imgPanel.add(categoryBadge);
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel topWrapper = new CustomPanel();
			topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
	
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			topWrapper.add(nameLabel);
			
			CustomPanel infoWrapper = new CustomPanel();
			infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.X_AXIS));
			infoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			infoWrapper.add(initiatorLabel);
			infoWrapper.add(Box.createHorizontalGlue());
			infoWrapper.add(conditionLabel);
			
			topWrapper.add(infoWrapper);
			
			CustomPanel bottomWrapper = new CustomPanel();
			bottomWrapper.setLayout(new BoxLayout(bottomWrapper, BoxLayout.X_AXIS));
			
			bottomWrapper.add(priceLabel);
			if (!priceLabel.getText().isEmpty()) {
				bottomWrapper.add(Box.createHorizontalStrut(5));				
			}
			bottomWrapper.add(quantityLabel);
			bottomWrapper.add(Box.createHorizontalGlue());
			bottomWrapper.add(actionButton);
			
			contentPanel.add(topWrapper, BorderLayout.NORTH);
			contentPanel.add(bottomWrapper, BorderLayout.SOUTH);
			
			add(imgPanel, BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);
		} else {
			setLayout(new BorderLayout(10, 0));
			imgPanel.setPreferredSize(new Dimension(75, 75));
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel centerWrapper = new CustomPanel();
			centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
			
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			CustomPanel textWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			textWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			textWrapper.add(categoryLabel);
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(conditionLabel);
			
			centerWrapper.add(Box.createVerticalGlue());
			centerWrapper.add(nameLabel);
			centerWrapper.add(initiatorLabel);
			centerWrapper.add(textWrapper);
			centerWrapper.add(Box.createVerticalGlue());
			
			CustomPanel eastWrapper = new CustomPanel();
			eastWrapper.setLayout(new BoxLayout(eastWrapper, BoxLayout.Y_AXIS));
			eastWrapper.add(Box.createVerticalGlue());
			if (showPrice) {
				priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				eastWrapper.add(priceLabel);
				eastWrapper.add(Box.createVerticalStrut(5));
			}
			actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			eastWrapper.add(actionButton);
			eastWrapper.add(Box.createVerticalGlue());
			
			contentPanel.add(centerWrapper, BorderLayout.CENTER);
			contentPanel.add(eastWrapper, BorderLayout.EAST);
			add(imgPanel, BorderLayout.WEST);
			add(contentPanel, BorderLayout.CENTER);
		}
	}
}