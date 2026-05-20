package admin.tabledialogs;

import admin.models.AdminItems;
import admin.services.AdminItemsServices;
import utils.*;
import components.*;
import admin.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//Static factory for the add and update item dialogs
public class ItemFormDialog {

    private static final int LABEL_WIDTH  = 120;
    private static final int FIELD_WIDTH  = 300;
    private static final int FIELD_HEIGHT = 35;
    private static final int ROW_HEIGHT   = 40;

    // ── Init ──

    public static void showAdd(Window owner, AdminItemsServices itemService, Runnable onSuccess) {
        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField nameField  = new CustomTextField("");
        CustomTextField stockField = new CustomTextField("");
        CustomTextField priceField = new CustomTextField("");
        CustomTextField descField  = new CustomTextField("");

        CustomComboBox<String> categoryBox = new CustomComboBox<>(
            new String[]{ "Textbooks", "Electronics", "Equipment",
                          "Supplies", "Consumable-Goods", "Other" });
        CustomComboBox<String> conditionBox = new CustomComboBox<>(
            new String[]{ "Fair", "Good", "New" });
        CustomComboBox<String> statusBox = new CustomComboBox<>(
            new String[]{ "Available", "Unavailable" });
        CustomComboBox<String> actionBox = new CustomComboBox<>(
            new String[]{
                "Marketplace",
                "Marketplace_Withdrawn",
                "Marketplace_Completed",
                "Sharing",
                "Sharing_Withdrawn",
                "Sharing_Approval",
                "Sharing_Return",
                "Sharing_Completed",
                "Trade",
                "Trade_Withdrawn",
                "Trade_Approval",
                "Trade_Completed"
            });

        final String[] imagePathHolder = { "" };
        CustomTextField imageField = new CustomTextField("No image selected");
        imageField.setEditable(false);
        imageField.setPreferredSize(new Dimension(150, FIELD_HEIGHT));
        imageField.setMinimumSize(new Dimension(150, FIELD_HEIGHT));

        CustomButton browseBtn = new CustomButton("Browse", 6);
        browseBtn.setFontSize(11f);
        browseBtn.setPadding(4, 10, 4, 10);
        browseBtn.setDefaultColor(Color.decode("#6c757d"));
        browseBtn.setTextColor(Color.WHITE);
        browseBtn.setHoverColor(Color.decode("#5a6268"));
        browseBtn.addActionListener(e -> {
            String path = ImageBrowserHelper.browse(owner);
            if (!path.isEmpty()) {
                imagePathHolder[0] = path;
                imageField.setText(path.substring(path.lastIndexOf(java.io.File.separator) + 1));
            }
        });

        JPanel imageInputPanel = new JPanel(new BorderLayout(6, 0));
        imageInputPanel.setOpaque(false);
        imageInputPanel.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        imageInputPanel.add(imageField, BorderLayout.CENTER);
        imageInputPanel.add(browseBtn,  BorderLayout.EAST);

        formPanel.add(createFieldPanel("Item Name:",   nameField,    true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Category:",    categoryBox,  true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Condition:",   conditionBox, true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Stock:",       stockField,   false));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Price:",       priceField,   false));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Status:",      statusBox,    true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Action:",      actionBox,    true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Description:", descField,    false));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buildManualRow("Image:", imageInputPanel, false));

        new AdminDialog(owner, "Create New Item", formPanel, "Create Item", e -> {
            String name      = nameField.getText().trim();
            String category  = comboValue(categoryBox, "");
            String cond      = comboValue(conditionBox, "Good");
            String status    = comboValue(statusBox,    "Available");
            String action    = comboValue(actionBox,    "Sharing");
            String desc      = descField.getText().trim();
            String imagePath = imagePathHolder[0];

            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Error: Please complete all required fields.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (imagePath.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Please select an image for the item.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int stock, price;
            try {
                stock = stockField.getText().trim().isEmpty() ? 0 : Integer.parseInt(stockField.getText().trim());
                price = priceField.getText().trim().isEmpty() ? 0 : Integer.parseInt(priceField.getText().trim());
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
                name, category, cond, stock, price, status, action, desc, imagePath);

            if (success) {
                onSuccess.run();
                JOptionPane.showMessageDialog(null, "Item added successfully.");
                SwingUtilities.getWindowAncestor((Component) e.getSource()).dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                    "Failed to add item. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).setVisible(true);
    }

    public static void showUpdate(Window owner, int itemId,
            AdminItemsServices itemService, Runnable onSuccess) {

        AdminItems item = itemService.getItemById(itemId);

        CustomPanel formPanel = new CustomPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        CustomTextField nameField  = new CustomTextField(item != null ? item.getItemName() : "");
        CustomTextField stockField = new CustomTextField(item != null ? String.valueOf(item.getItemQuantity()) : "");
        CustomTextField priceField = new CustomTextField(item != null ? String.valueOf(item.getPrice()) : "");

        CustomComboBox<String> categoryBox = new CustomComboBox<>(
            new String[]{ "Textbooks", "Electronics", "Equipment",
                          "Supplies", "Consumable-Goods", "Other" });
        CustomComboBox<String> conditionBox = new CustomComboBox<>(
            new String[]{ "Fair", "Good", "New" });
        CustomComboBox<String> statusBox = new CustomComboBox<>(
            new String[]{ "Available", "Unavailable" });
        CustomComboBox<String> actionBox = new CustomComboBox<>(
            new String[]{
                "Marketplace",
                "Marketplace_Withdrawn",
                "Marketplace_Completed",
                "Sharing",
                "Sharing_Withdrawn",
                "Sharing_Approval",
                "Sharing_Return",
                "Sharing_Completed",
                "Trade",
                "Trade_Withdrawn",
                "Trade_Approval",
                "Trade_Completed"
            });

        if (item != null) {
            categoryBox.setSelectedItem(item.getCategory());
            conditionBox.setSelectedItem(item.getItemCondition());
            statusBox.setSelectedItem(item.getAvailabilityStatus());
            actionBox.setSelectedItem(item.getAction());
        }

        formPanel.add(createFieldPanel("Item Name:", nameField,    true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Category:",  categoryBox,  true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Condition:", conditionBox, true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Stock:",     stockField,   false));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Price:",     priceField,   false));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Status:",    statusBox,    true));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Action:",    actionBox,    true));

        AdminDialog[] dialog = { null };
        dialog[0] = new AdminDialog(owner, "Update Item: " + itemId, formPanel, "Save Changes", e -> {
            if (item == null) return;

            String resolvedName   = nameField.getText().trim().isEmpty()
                ? item.getItemName()            : nameField.getText().trim();
            String resolvedCat    = comboValue(categoryBox, item.getCategory());
            String resolvedCond   = comboValue(conditionBox, item.getItemCondition());
            String resolvedStat   = comboValue(statusBox,   item.getAvailabilityStatus());
            String resolvedAction = comboValue(actionBox,   item.getAction());

            if (resolvedName.isEmpty() || resolvedCat.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Item Name and Category cannot be empty.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int resolvedStock, resolvedPrice;
            try {
                resolvedStock = stockField.getText().trim().isEmpty()
                    ? item.getItemQuantity() : Integer.parseInt(stockField.getText().trim());
                resolvedPrice = priceField.getText().trim().isEmpty()
                    ? item.getPrice()        : Integer.parseInt(priceField.getText().trim());
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

            // Dismiss the form before showing the confirm dialog so they don't stack
            dialog[0].dispose();
            int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to update Item ID " + itemId + "?",
                "Confirm Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

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
                JOptionPane.showMessageDialog(null,
                    "Success! The item has been updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    "Update failed. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog[0].setVisible(true);
    }

    // ── Helpers ──

    // Null-safe combo read; returns fallback when nothing is selected
    private static String comboValue(CustomComboBox<String> box, String fallback) {
        Object val = box.getSelectedItem();
        return val != null ? val.toString() : fallback;
    }

    private static JPanel createFieldPanel(String label, JComponent component, boolean required) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH + 20, ROW_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        labelRow.setOpaque(false);
        labelRow.setPreferredSize(new Dimension(LABEL_WIDTH, 30));

        CustomLabel lbl = new CustomLabel(label, 14f, FontStyle.REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        labelRow.add(lbl);

        if (required) {
            CustomLabel star = new CustomLabel("*");
            star.setForeground(Color.RED);
            star.setFont(lbl.getFont());
            labelRow.add(star);
        }
        panel.add(labelRow, BorderLayout.WEST);

        component.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        component.setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        component.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(component, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);
        return panel;
    }

    // Identical layout to createFieldPanel but skips the fixed FIELD_WIDTH constraint
    // so composite inputs (e.g. image field + browse button) can size themselves freely
    private static JPanel buildManualRow(String labelText, JComponent inputComponent, boolean required) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(LABEL_WIDTH + FIELD_WIDTH + 20, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        labelRow.setOpaque(false);
        labelRow.setPreferredSize(new Dimension(LABEL_WIDTH, 30));

        CustomLabel lbl = new CustomLabel(labelText, 14f, FontStyle.REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        labelRow.add(lbl);

        if (required) {
            CustomLabel star = new CustomLabel("*");
            star.setForeground(Color.RED);
            star.setFont(lbl.getFont());
            labelRow.add(star);
        }
        row.add(labelRow, BorderLayout.WEST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        wrap.add(inputComponent, BorderLayout.CENTER);
        row.add(wrap, BorderLayout.CENTER);
        return row;
    }
}