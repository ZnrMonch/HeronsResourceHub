package marketplace;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
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
	private CustomLabel priceLabel = new CustomLabel("Free", Brand.SUBHEADER_TEXT_SIZE, FontStyle.BOLD);
	private CustomLabel categoryLabel = new CustomLabel("Category", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	private CustomLabel conditionLabel = new CustomLabel("Condition", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	private CustomLabel initiatorLabel = new CustomLabel("N/A", Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
	
	public ItemCard(View view, String actionText, ItemRecord record, boolean showPrice, Consumer<ItemRecord> onAction) {
		setBackground(Color.WHITE);
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
		actionButton.addActionListener(e -> onAction.accept(record));

		if (View.GRID.equals(view)) {
			setLayout(new BorderLayout(0, 10));
			imgPanel.setPreferredSize(new Dimension(200, 150));
			
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
			
			add(imgPanel, BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);
		} else {
			setLayout(new BorderLayout(10, 0));
			imgPanel.setPreferredSize(new Dimension(75, 75));
			
			CustomPanel contentPanel = new CustomPanel(new BorderLayout());
			CustomPanel centerWrapper = new CustomPanel();
			centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
			
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			descriptionLabel.setForeground(Color.GRAY);
			
			CustomPanel textWrapper = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			textWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			textWrapper.add(categoryLabel);
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(conditionLabel);
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(new CustomLabel("\u2022"));
			textWrapper.add(Box.createHorizontalStrut(5));
			textWrapper.add(initiatorLabel);
			
			centerWrapper.add(Box.createVerticalGlue());
			centerWrapper.add(nameLabel);
			centerWrapper.add(descriptionLabel);
			centerWrapper.add(textWrapper);
			centerWrapper.add(Box.createVerticalGlue());
			
			CustomPanel eastWrapper = new CustomPanel();
			eastWrapper.setLayout(new BoxLayout(eastWrapper, BoxLayout.Y_AXIS));
			eastWrapper.add(Box.createVerticalGlue());
			if (showPrice) {
				priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				eastWrapper.add(priceLabel);
				eastWrapper.add(Box.createVerticalStrut(10));
			}
			actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			eastWrapper.add(actionButton);
			eastWrapper.add(Box.createVerticalGlue());
			
			contentPanel.add(centerWrapper, BorderLayout.CENTER);
			contentPanel.add(eastWrapper, BorderLayout.EAST);
			add(imgPanel, BorderLayout.WEST);
			add(contentPanel, BorderLayout.CENTER);
		}

		if (record != null) {
			nameLabel.setText(record.itemName);
			descriptionLabel.setText(record.description);
			priceLabel.setText(record.price > 0 ? "P" + record.price : "Free");
			categoryLabel.setText(record.category != null ? record.category : "");
			conditionLabel.setText(record.itemCondition != null ? record.itemCondition : "");
			initiatorLabel.setText(record.ownerId > 0 ? "by Someone" : "N/A");
		}
	}
}