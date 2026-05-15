package marketplace;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
<<<<<<< HEAD
import java.util.function.Consumer;
=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
import javax.swing.*;
import components.*;
import enums.View;
import utils.*;
import database.*;

public class ItemCard extends CustomPanel {
	private static final long serialVersionUID = 1L;
	private String image = "/resources/images/umak_img.jpg";
	
	private CustomLabel nameLabel = new CustomLabel("Item Name", Brand.HEADER4_TEXT_SIZE, FontStyle.BOLD);
	private CustomLabel descriptionLabel = new CustomLabel("Item Description", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
<<<<<<< HEAD
	private CustomLabel priceLabel = new CustomLabel("Free", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
	private CustomLabel categoryLabel = new CustomLabel("Category", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	private CustomLabel conditionLabel = new CustomLabel("Condition", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	private CustomLabel initiatorLabel = new CustomLabel("N/A", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	
	public ItemCard(View view, String actionText, ItemRecord record, boolean showPrice, Consumer<ItemRecord> onAction) {
=======
	private CustomLabel quantityLabel = new CustomLabel("Stock: 1", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	private CustomLabel priceLabel = new CustomLabel("", Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD, Brand.PRIMARY_COLOR);
	private CustomLabel categoryLabel = new CustomLabel("Category", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	private CustomLabel conditionLabel = new CustomLabel("Condition", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	private CustomLabel initiatorLabel = new CustomLabel("N/A", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR, Color.GRAY);
	
	public ItemCard(View view, String actionText, ItemRecord record, boolean showPrice, ItemActionListener onAction) {
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
		setBackground(Color.WHITE);
		setBorder(1, Color.LIGHT_GRAY);
		setPadding(10);
		setRadius(20);
		
<<<<<<< HEAD
		CustomPanel imgPanel = new CustomPanel() {
			private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : null;
=======
		if (record != null && record.itemsImage != null && !record.itemsImage.isEmpty()) {
			this.image = record.itemsImage;
		}
		
		CustomPanel imgPanel = new CustomPanel() {
			private Image imgId = IconLoader.loadIcon(image) != null ? IconLoader.loadIcon(image).getImage() : IconLoader.loadIcon("/resources/images/umak_img.jpg").getImage();
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
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
<<<<<<< HEAD
		actionButton.addActionListener(e -> onAction.accept(record));
=======
		actionButton.addActionListener(e -> {
			if (onAction != null) {
				onAction.onItemAction(record);
			}
		});
		
		if (record != null) {
			nameLabel.setText(record.itemName);
			descriptionLabel.setText(record.description);
			priceLabel.setText(record.price > 0 ? String.format("P%.0f", (double) record.price) : "");
			categoryLabel.setText(record.category != null ? record.category : "");
			conditionLabel.setText(record.condition != null ? record.condition : "");
			
			if (record.initiatorFirstName != null && record.initiatorLastName != null) {
				initiatorLabel.setText(record.initiatorFirstName + " " + record.initiatorLastName);
			} else {
				initiatorLabel.setText("[100] Someone");
			}
		}
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876

		if (View.GRID.equals(view)) {
			setLayout(new BorderLayout(0, 10));
			imgPanel.setPreferredSize(new Dimension(200, 150));
			
<<<<<<< HEAD
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel infoWrapper = new CustomPanel();
			infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
			initiatorLabel.setForeground(Color.GRAY);
			infoWrapper.add(nameLabel);
			infoWrapper.add(initiatorLabel);
			
			CustomPanel actionWrapper = new CustomPanel(new BorderLayout());
			if (showPrice) {
				actionWrapper.add(priceLabel, BorderLayout.WEST);
			}
			actionWrapper.add(actionButton, BorderLayout.EAST);
			
			contentPanel.add(infoWrapper, BorderLayout.NORTH);
			contentPanel.add(actionWrapper, BorderLayout.SOUTH);
=======
			imgPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			
			CustomPanel categoryBadge = new CustomPanel();
			categoryBadge.setBackground(Color.WHITE);
			categoryBadge.setRadius(15);
			categoryBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
			
			String catText = (record != null && record.category != null) ? record.category : "Category";
			CustomLabel topRightImgLabel = new CustomLabel(catText, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
			categoryBadge.add(topRightImgLabel);
			
			imgPanel.add(categoryBadge);
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel topWrapper = new CustomPanel();
			topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
			topWrapper.add(nameLabel);
			topWrapper.add(initiatorLabel);
			
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
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
			
			add(imgPanel, BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);
		} else {
			setLayout(new BorderLayout(10, 0));
			imgPanel.setPreferredSize(new Dimension(75, 75));
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel centerWrapper = new CustomPanel();
			centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
			
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
<<<<<<< HEAD
			descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setForeground(Color.GRAY);
=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
			
			CustomPanel textWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			textWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			textWrapper.add(categoryLabel);
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(conditionLabel);
<<<<<<< HEAD
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(initiatorLabel);
			
			centerWrapper.add(Box.createVerticalGlue());
			centerWrapper.add(nameLabel);
			centerWrapper.add(descriptionLabel);
=======
			
			centerWrapper.add(Box.createVerticalGlue());
			centerWrapper.add(nameLabel);
			centerWrapper.add(initiatorLabel);
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
			centerWrapper.add(textWrapper);
			centerWrapper.add(Box.createVerticalGlue());
			
			CustomPanel eastWrapper = new CustomPanel();
			eastWrapper.setLayout(new BoxLayout(eastWrapper, BoxLayout.Y_AXIS));
			eastWrapper.add(Box.createVerticalGlue());
			if (showPrice) {
				priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				eastWrapper.add(priceLabel);
<<<<<<< HEAD
				eastWrapper.add(Box.createVerticalStrut(10));
=======
				eastWrapper.add(Box.createVerticalStrut(5));
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
			}
			actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			eastWrapper.add(actionButton);
			eastWrapper.add(Box.createVerticalGlue());
			
			contentPanel.add(centerWrapper, BorderLayout.CENTER);
			contentPanel.add(eastWrapper, BorderLayout.EAST);
			add(imgPanel, BorderLayout.WEST);
			add(contentPanel, BorderLayout.CENTER);
		}
<<<<<<< HEAD

		if (record != null) {
			nameLabel.setText(record.itemName);
			descriptionLabel.setText(record.description);
			priceLabel.setText(record.price > 0 ? "P" + record.price : "Free");
			categoryLabel.setText(record.category != null ? record.category : "");
			conditionLabel.setText(record.itemCondition != null ? record.itemCondition : "");
			initiatorLabel.setText(record.ownerId > 0 ? "by Someone" : "N/A");
		}
=======
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876
	}
}