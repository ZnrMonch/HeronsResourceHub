package admin.tabledialogs;

import admin.models.AdminItems;
import admin.services.AdminItemsServices;
import utils.*;
import components.*;
import admin.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ItemFormDialog {

    // Opens the Add New Item dialog
    public static void showAdd(Window owner, AdminItemsServices itemService, Runnable onSuccess) {
        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField nameField  = new CustomTextField("");
        CustomTextField stockField = new CustomTextField("");
        CustomTextField priceField = new CustomTextField("");
        CustomTextField descField  = new CustomTextField("");

        CustomComboBox<String> categoryBox = new CustomComboBox<>(
            new String[] { "Textbooks", "Electronics", "Equipment",
                           "Supplies", "Consumable-Goods", "Other" });
        CustomComboBox<String> conditionBox = new CustomComboBox<>(
            new String[] { "Fair", "Good", "New" });
        CustomComboBox<String> statusBox = new CustomComboBox<>(
            new String[] { "Available", "Unavailable" });
        CustomComboBox<String> actionBox = new CustomComboBox<>(
            new String[] { "Sharing", "Marketplace", "Barter-Trading" });

        final String[] imagePathHolder = { "" };

        CustomTextField imageField = new CustomTextField("No image selected");
        imageField.setEditable(false);
        imageField.setPreferredSize(new Dimension(170, 32));
        imageField.setMinimumSize(new Dimension(170, 32));

        CustomButton browseBtn = new CustomButton("Browse", 6);
        browseBtn.setFontSize(11f);
        browseBtn.setPadding(4, 10, 4, 10);
        browseBtn.setDefaultColor(Color.decode("#6c757d"));
        browseBtn.setTextColor(Color.WHITE);
        browseBtn.setHoverColor(Color.decode("#5a6268"));
        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = ImageBrowserHelper.browse(owner);
                if (!path.isEmpty()) {
                    imagePathHolder[0] = path;
                    imageField.setText(
                        path.substring(path.lastIndexOf(java.io.File.separator) + 1));
                }
            }
        });

        JPanel imageInputPanel = new JPanel(new BorderLayout(6, 0));
        imageInputPanel.setOpaque(false);
        imageInputPanel.add(imageField, BorderLayout.CENTER);
        imageInputPanel.add(browseBtn,  BorderLayout.EAST);

        JPanel imageRow = new JPanel(new BorderLayout(10, 0));
        imageRow.setOpaque(false);
        imageRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        imageRow.setPreferredSize(new Dimension(0, 35));
        CustomLabel imageLabel = new CustomLabel("Image:", 14f, FontStyle.REGULAR);
        imageLabel.setPreferredSize(new Dimension(100, 30));
        imageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel imageWrap = new JPanel(new BorderLayout());
        imageWrap.setOpaque(false);
        imageWrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        imageWrap.add(imageInputPanel, BorderLayout.CENTER);
        imageRow.add(imageLabel, BorderLayout.WEST);
        imageRow.add(imageWrap,  BorderLayout.CENTER);

        formPanel.add(createFieldPanel("Item Name:",   nameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Category:",    categoryBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Condition:",   conditionBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Stock:",       stockField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Price:",       priceField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Status:",      statusBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Action:",      actionBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Description:", descField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(imageRow);

        new AdminDialog(owner, "Add New Item", formPanel, "Add Item",
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String name      = nameField.getText().trim();
                    String category  = categoryBox.getSelectedItem() != null
                                           ? categoryBox.getSelectedItem().toString() : "";
                    String cond      = conditionBox.getSelectedItem() != null
                                           ? conditionBox.getSelectedItem().toString() : "Good";
                    String status    = statusBox.getSelectedItem() != null
                                           ? statusBox.getSelectedItem().toString() : "Available";
                    String action    = actionBox.getSelectedItem() != null
                                           ? actionBox.getSelectedItem().toString() : "Sharing";
                    String desc      = descField.getText().trim();
                    String imagePath = imagePathHolder[0];

                    if (name.isEmpty() || category.isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                            "Item Name and Category are required.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (imagePath.isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                            "Please select an image for the item.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int stock, price;
                    try {
                        stock = stockField.getText().trim().isEmpty() ? 0
                                    : Integer.parseInt(stockField.getText().trim());
                        price = priceField.getText().trim().isEmpty() ? 0
                                    : Integer.parseInt(priceField.getText().trim());
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null,
                            "Stock and Price must be valid whole numbers.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (stock < 0 || price < 0) {
                        JOptionPane.showMessageDialog(null,
                            "Stock and Price cannot be negative.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean success = itemService.addItem(
                        name, category, cond, stock, price,
                        status, action, desc, imagePath);

                    if (success) {
                        onSuccess.run();
                        JOptionPane.showMessageDialog(null, "Item added successfully.");
                        SwingUtilities.getWindowAncestor((Component) e.getSource()).dispose();
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "Failed to add item. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }).setVisible(true);
    }

    // Opens the Update Item dialog pre-filled with existing data
    public static void showUpdate(Window owner, int itemId,
            AdminItemsServices itemService, Runnable onSuccess) {

        AdminItems item = itemService.getItemById(itemId);

        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField nameField  = new CustomTextField(item != null ? item.getItemName() : "");
        CustomTextField stockField = new CustomTextField(item != null
                                         ? String.valueOf(item.getItemQuantity()) : "");
        CustomTextField priceField = new CustomTextField(item != null
                                         ? String.valueOf(item.getPrice()) : "");

        CustomComboBox<String> categoryBox = new CustomComboBox<>(
            new String[] { "Textbooks", "Electronics", "Equipment",
                           "Supplies", "Consumable-Goods", "Other" });
        CustomComboBox<String> conditionBox = new CustomComboBox<>(
            new String[] { "Fair", "Good", "New" });
        CustomComboBox<String> statusBox = new CustomComboBox<>(
            new String[] { "Available", "Unavailable" });
        CustomComboBox<String> actionBox = new CustomComboBox<>(
            new String[] { "Sharing", "Marketplace", "Barter-Trading" });

        if (item != null) {
            categoryBox.setSelectedItem(item.getCategory());
            conditionBox.setSelectedItem(item.getItemCondition());
            statusBox.setSelectedItem(item.getAvailabilityStatus());
            actionBox.setSelectedItem(item.getAction());
        }

        formPanel.add(createFieldPanel("Item Name:", nameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Category:",  categoryBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Condition:", conditionBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Stock:",     stockField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Price:",     priceField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Status:",    statusBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Action:",    actionBox));

        new AdminDialog(owner, "Update Item: " + itemId, formPanel, "Save Changes",
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (item == null) return;

                    String resolvedName = nameField.getText().trim().isEmpty()
                        ? item.getItemName() : nameField.getText().trim();
                    String resolvedCat  = categoryBox.getSelectedItem() != null
                        ? categoryBox.getSelectedItem().toString() : item.getCategory();
                    String resolvedCond = conditionBox.getSelectedItem() != null
                        ? conditionBox.getSelectedItem().toString() : item.getItemCondition();
                    String resolvedStat = statusBox.getSelectedItem() != null
                        ? statusBox.getSelectedItem().toString() : item.getAvailabilityStatus();
                    String resolvedAction = actionBox.getSelectedItem() != null
                        ? actionBox.getSelectedItem().toString() : item.getAction();

                    if (resolvedName.isEmpty() || resolvedCat.isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                            "Item Name and Category cannot be empty.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int resolvedStock, resolvedPrice;
                    try {
                        resolvedStock = stockField.getText().trim().isEmpty()
                            ? item.getItemQuantity()
                            : Integer.parseInt(stockField.getText().trim());
                        resolvedPrice = priceField.getText().trim().isEmpty()
                            ? item.getPrice()
                            : Integer.parseInt(priceField.getText().trim());
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null,
                            "Stock and Price must be valid whole numbers.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (resolvedStock < 0 || resolvedPrice < 0) {
                        JOptionPane.showMessageDialog(null,
                            "Stock and Price cannot be negative.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    item.setItemName(resolvedName);
                    item.setCategory(resolvedCat);
                    item.setItemCondition(resolvedCond);
                    item.setItemQuantity(resolvedStock);
                    item.setPrice(resolvedPrice);
                    item.setAvailabilityStatus(resolvedStat);
                    item.setAction(resolvedAction);

                    boolean success = itemService.updateItem(item);
                    if (success) {
                        onSuccess.run();
                        JOptionPane.showMessageDialog(null, "Item updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Update failed. Please try again.");
                    }
                }
            }).setVisible(true);
    }

    // Creates a label + input field row used inside dialogs
    private static JPanel createFieldPanel(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setPreferredSize(new Dimension(0, 35));

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setPreferredSize(new Dimension(100, 30));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl, BorderLayout.WEST);

        component.setPreferredSize(new Dimension(250, 32));
        component.setMinimumSize(new Dimension(250, 32));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(component, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);

        return panel;
    }
}