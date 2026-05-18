package marketplace;

import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import components.*;
import utils.*;
import enums.Category;
import enums.Condition;
import database.ItemRecord;
import database.DatabaseManager;

public class ItemForm extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private CustomTextField itemNameField;
    private CustomLabel imagePathLabel;
    private String selectedImagePath = "/resources/items/default.jpg";
    
    private CustomTextArea descriptionField;
    private CustomComboBox<String> categoryField;
    private CustomComboBox<String> conditionField;
    private CustomTextField quantityField;
    private CustomTextField pickupLocationField;
    
    private CustomPanel pickupDateField;
    private JCheckBox[] pickupDayBoxes;
    private CustomSpinner pickupTimeField;
    
    private CustomTextField priceField;
    private CustomTextField maximumBorrowDaysField; // Changed to TextField
    private CustomTextField desiredItemField;
    
    private ItemRecord currentItem;
    private final String DB_URL = DatabaseManager.getURL();
    private final String USER = DatabaseManager.getUser();
    private final String PASSWORD = DatabaseManager.getPassword();
    
    public ItemForm(Window parent, String title, ItemRecord itemToUpdate) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.currentItem = itemToUpdate;
        setResizable(false);
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        init();
        
        if (currentItem != null) {
            preFillData();
        }
        
        pack();
        setSize(550, getPreferredSize().height);
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    private void init() {
        add(initHeader(), BorderLayout.NORTH);
        add(initForm(), BorderLayout.CENTER);
        add(initAction(), BorderLayout.SOUTH);
    }
    
    private CustomPanel initHeader() {
        CustomPanel wrapper = new CustomPanel(new GridBagLayout());
        wrapper.add(new CustomLabel(getTitle() != null ? getTitle().toUpperCase() : "ITEM FORM", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
        return wrapper;
    }
    
    private CustomPanel initForm() {
        CustomPanel wrapper = new CustomPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        itemNameField = new CustomTextField("Enter item name...");
        imagePathLabel = new CustomLabel(new File(selectedImagePath).getName(), Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
        
        CustomButton browseBtn = new CustomButton("Browse", 10);
        browseBtn.setPadding(20, 5);
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG)", "jpg", "png"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                selectedImagePath = "/resources/items/" + file.getName();
                imagePathLabel.setText(file.getName());
            }
        });

        descriptionField = new CustomTextArea("Provide a detailed description...", 3, 20);
        
        String[] categories = new String[Category.values().length];
        for (int i = 0; i < Category.values().length; i++) categories[i] = Category.values()[i].name().replace("_", " ");
        categoryField = new CustomComboBox<>(categories);
        
        String[] conditions = new String[Condition.values().length];
        for (int i = 0; i < Condition.values().length; i++) conditions[i] = Condition.values()[i].name();
        conditionField = new CustomComboBox<>(conditions);
        
        quantityField = new CustomTextField("1");
        pickupLocationField = new CustomTextField("e.g., CCIS Lobby");
        
        pickupDateField = new CustomPanel(new GridLayout(2, 4, 2, 2));
        pickupDateField.setOpaque(false);
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "All"};
        pickupDayBoxes = new JCheckBox[days.length];
        for (int i = 0; i < days.length; i++) {
            pickupDayBoxes[i] = new JCheckBox(days[i]);
            pickupDayBoxes[i].setOpaque(false); 
            pickupDateField.add(pickupDayBoxes[i]);
        }
        
        pickupTimeField = new CustomSpinner(new SpinnerListModel(new String[] {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"}));
        
        addField(wrapper, gbc, "Item Name:", itemNameField);
        
        CustomPanel imgRow = new CustomPanel(new BorderLayout(5, 0));
        imgRow.add(imagePathLabel, BorderLayout.CENTER);
        imgRow.add(browseBtn, BorderLayout.EAST);
        addField(wrapper, gbc, "Item Image:", imgRow);
        
        addField(wrapper, gbc, "Description:", descriptionField);
        
        CustomPanel row1 = new CustomPanel(new GridLayout(1, 2, 10, 0));
        row1.add(createColumn("Category:", categoryField));
        row1.add(createColumn("Condition:", conditionField));
        gbc.gridy++; wrapper.add(row1, gbc);
        
        addField(wrapper, gbc, "Quantity:", quantityField);
        addField(wrapper, gbc, "Pickup Location:", pickupLocationField);
        
        CustomPanel row2 = new CustomPanel(new GridLayout(1, 2, 10, 0));
        row2.add(createColumn("Pickup Days:", pickupDateField));
        row2.add(createColumn("Pickup Time:", pickupTimeField));
        gbc.gridy++; wrapper.add(row2, gbc);
        
        String lowerTitle = getTitle() != null ? getTitle().toLowerCase() : "";
        boolean isMarketplace = (currentItem != null && currentItem.action.toLowerCase().contains("marketplace")) || lowerTitle.contains("sell");
        boolean isSharing = (currentItem != null && currentItem.action.toLowerCase().contains("sharing")) || lowerTitle.contains("lend");
        boolean isTrade = (currentItem != null && currentItem.action.toLowerCase().contains("trade")) || (lowerTitle.contains("offer") || lowerTitle.contains("trade"));
        
        if (isMarketplace) {
            priceField = new CustomTextField("0");
            addField(wrapper, gbc, "Price:", priceField);
        } else if (isSharing) {
            maximumBorrowDaysField = new CustomTextField("1");
            addField(wrapper, gbc, "Max Borrow Days:", maximumBorrowDaysField);
        } else if (isTrade) {
            desiredItemField = new CustomTextField("e.g., Calculator");
            addField(wrapper, gbc, "Desired Item:", desiredItemField);
        }
        
        return wrapper;
    }

    private void addField(CustomPanel parent, GridBagConstraints gbc, String label, JComponent comp) {
        gbc.gridy++;
        parent.add(new CustomLabel(label, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD), gbc);
        gbc.gridy++;
        parent.add(comp, gbc);
    }
    
    private CustomPanel createColumn(String label, JComponent comp) {
        CustomPanel col = new CustomPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(new CustomLabel(label, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(comp);
        return col;
    }

    private void preFillData() {
        itemNameField.setText(currentItem.itemName);
        selectedImagePath = currentItem.itemImage;
        imagePathLabel.setText(new File(selectedImagePath).getName());
        descriptionField.setText(currentItem.description);
        categoryField.setSelectedItem(currentItem.category.replace("_", " "));
        conditionField.setSelectedItem(currentItem.condition);
        quantityField.setText(String.valueOf(currentItem.itemQuantity));
        pickupLocationField.setText(currentItem.pickupArea);
        pickupTimeField.setValue(currentItem.pickupTime);
        
        if (currentItem.pickupDays != null) {
            String[] daysSelected = currentItem.pickupDays.split(",");
            for (String d : daysSelected) {
                String prefix = d.trim().substring(0, 3);
                for (int i = 0; i < 7; i++) {
                    if (pickupDayBoxes[i].getText().equalsIgnoreCase(prefix)) pickupDayBoxes[i].setSelected(true);
                }
            }
        }
        if (priceField != null && currentItem.price > 0) priceField.setText(String.valueOf(currentItem.price));
        if (maximumBorrowDaysField != null && currentItem.maximumBorrowDays > 0) maximumBorrowDaysField.setText(String.valueOf(currentItem.maximumBorrowDays));
        if (desiredItemField != null && currentItem.desiredItem != null) desiredItemField.setText(currentItem.desiredItem);
    }
    
    private CustomPanel initAction() {
        CustomPanel wrapper = new CustomPanel(new FlowLayout(FlowLayout.CENTER));
        CustomButton cancelBtn = new CustomButton("Cancel", 10);
        cancelBtn.setPadding(20, 5);
        cancelBtn.addActionListener(e -> dispose());
        CustomButton submitBtn = new CustomButton(currentItem != null ? "Update" : "Submit", 10);
        submitBtn.setPadding(20, 5);
        submitBtn.addActionListener(e -> {
            if (isFormValid()) {
                saveData();
                JOptionPane.showMessageDialog(this, "Item saved successfully!");
                dispose();
            }
        });
        wrapper.add(cancelBtn);
        wrapper.add(submitBtn);
        return wrapper;
    }

    private boolean isFormValid() {
        if (itemNameField.getText().trim().isEmpty()) { showError("Name required"); return false; }
        try { Integer.parseInt(quantityField.getText().trim()); } catch (NumberFormatException e) { showError("Quantity must be a number"); return false; }
        
        if (maximumBorrowDaysField != null) {
            try { Integer.parseInt(maximumBorrowDaysField.getText().trim()); } catch (NumberFormatException e) { showError("Max borrow days must be a number"); return false; }
        }
        
        return true;
    }

    private void saveData() {
        if (currentItem == null) insertItemIntoDatabase();
        else updateItemInDatabase();
    }

    private void insertItemIntoDatabase() {
        // Implementation logic
    }

    private void updateItemInDatabase() {
        StringBuilder days = new StringBuilder();
        String[] fullDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) {
            if (pickupDayBoxes[i].isSelected()) {
                if (days.length() > 0) days.append(",");
                days.append(fullDays[i]);
            }
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE items SET item_name=?, item_quantity=?, item_image=?, description=?, category=?, condition=?, pickup_area=?, pickup_days=?, pickup_time=?, price=?, maximum_borrow_days=?, desired_item=? WHERE item_id=?")) {
            
            pstmt.setString(1, itemNameField.getText().trim());
            pstmt.setInt(2, Integer.parseInt(quantityField.getText().trim()));
            pstmt.setString(3, selectedImagePath);
            pstmt.setString(4, descriptionField.getText().trim());
            pstmt.setString(5, categoryField.getSelectedItem().toString().replace(" ", "_"));
            pstmt.setString(6, conditionField.getSelectedItem().toString());
            pstmt.setString(7, pickupLocationField.getText().trim());
            pstmt.setString(8, days.toString());
            pstmt.setString(9, pickupTimeField.getValue().toString());
            
            if (priceField != null) pstmt.setInt(10, Integer.parseInt(priceField.getText().trim()));
            else pstmt.setNull(10, Types.INTEGER);
            
            if (maximumBorrowDaysField != null) pstmt.setInt(11, Integer.parseInt(maximumBorrowDaysField.getText().trim()));
            else pstmt.setNull(11, Types.INTEGER);
            
            if (desiredItemField != null) pstmt.setString(12, desiredItemField.getText().trim());
            else pstmt.setNull(12, Types.VARCHAR);
            
            pstmt.setInt(13, currentItem.itemId);
            pstmt.executeUpdate();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database Error");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}