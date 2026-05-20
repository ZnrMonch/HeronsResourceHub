package marketplace;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    
    // FIELDS
    
    private static final long serialVersionUID = 1L;
    
    public interface OnSuccessListener {
        void onSuccess();
    }
    
    private CustomTextField itemNameField;
    private CustomLabel imagePathLabel;
    
    private String selectedImagePath = ""; 
    private File selectedImageFile = null; 
    
    private CustomTextArea descriptionField;
    private CustomComboBox<String> categoryField;
    private CustomComboBox<String> conditionField;
    private CustomTextField quantityField;
    private CustomTextField pickupLocationField;
    
    private CustomPanel pickupDateField;
    private JCheckBox[] pickupDayBoxes;
    private CustomSpinner pickupTimeField;
    
    private CustomTextField priceField;
    private CustomTextField maximumBorrowDaysField; 
    private CustomTextField desiredItemField;
    
    private ItemRecord currentItem;
    private int currentUserId;
    private OnSuccessListener successListener;
    
    // CONSTRUCTORS
    
    // Sets window traits, saves basic data objects, fits components, and displays the dialog box
    public ItemForm(Window parent, String title, ItemRecord itemToUpdate, int currentUserId, OnSuccessListener listener) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.currentItem = itemToUpdate;
        this.currentUserId = currentUserId;
        this.successListener = listener;
        setResizable(false);
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        init();
        
        if (currentItem != null) {
            preFillData();
            checkCategoryCondition();
        }
        
        pack();
        setSize(550, getPreferredSize().height);
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    // PRIVATE METHODS
    
    // Groups layout routines to drop sections into top, center, and bottom window spots
    private void init() {
        add(initHeader(), BorderLayout.NORTH);
        add(initForm(), BorderLayout.CENTER);
        add(initAction(), BorderLayout.SOUTH);
    }
    
    // Creates top panel containing the title of the current view mode
    private CustomPanel initHeader() {
        CustomPanel wrapper = new CustomPanel(new GridBagLayout());
        wrapper.add(new CustomLabel(getTitle() != null ? getTitle().toUpperCase() : "ITEM FORM", Brand.HEADER2_TEXT_SIZE, FontStyle.BOLD));
        return wrapper;
    }
    
    // Spawns input inputs, setup checkboxes, configures sub-rows, and tracks context field visibilities
    private CustomPanel initForm() {
        CustomPanel wrapper = new CustomPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        itemNameField = new CustomTextField("Enter item name...");
        imagePathLabel = new CustomLabel(selectedImagePath.isEmpty() ? "No image selected" : new File(selectedImagePath).getName(), Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
        
        CustomButton browseBtn = new CustomButton("Browse", 10);
        browseBtn.setPadding(20, 5);
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG)", "jpg", "png"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                selectedImagePath = "/resources/items/" + selectedImageFile.getName();
                imagePathLabel.setText(selectedImageFile.getName());
            }
        });

        descriptionField = new CustomTextArea("Provide a detailed description...", 3, 20);
        
        String[] categories = new String[Category.values().length];
        for (int i = 0; i < Category.values().length; i++) categories[i] = Category.values()[i].name().replace("_", " ");
        categoryField = new CustomComboBox<>(categories);
        categoryField.addActionListener(e -> checkCategoryCondition());
        
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
        
        JCheckBox allBox = pickupDayBoxes[7];
        allBox.addActionListener(e -> {
            boolean isSelected = allBox.isSelected();
            for (int i = 0; i < 7; i++) {
                pickupDayBoxes[i].setSelected(isSelected);
            }
        });
        
        for (int i = 0; i < 7; i++) {
            final int index = i; 
            pickupDayBoxes[i].addActionListener(e -> {
                if (!pickupDayBoxes[index].isSelected()) {
                    allBox.setSelected(false);
                } else {
                    boolean allSelected = true;
                    for (int j = 0; j < 7; j++) {
                        if (!pickupDayBoxes[j].isSelected()) {
                            allSelected = false;
                            break;
                        }
                    }
                    allBox.setSelected(allSelected);
                }
            });
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

    // Appends text headers followed by their core field boxes down the layout stack
    private void addField(CustomPanel parent, GridBagConstraints gbc, String label, JComponent comp) {
        gbc.gridy++;
        parent.add(createRequiredLabel(label), gbc);
        gbc.gridy++;
        parent.add(comp, gbc);
    }
    
    // Wraps an individual column block grouping a top label and its input child box
    private CustomPanel createColumn(String label, JComponent comp) {
        CustomPanel col = new CustomPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(createRequiredLabel(label));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(comp);
        return col;
    }
    
    // Builds a tiny side-by-side component containing title text and a red asterisks badge
    private CustomPanel createRequiredLabel(String text) {
        CustomPanel panel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(new CustomLabel(text, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD));
        panel.add(new CustomLabel(" *", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD, Brand.RED));
        return panel;
    }

    // Locks condition fields to New if selected category type matches consumable goods
    private void checkCategoryCondition() {
        if ("Consumable_Goods".equals(categoryField.getSelectedItem().toString().replace(" ", "_"))) {
            conditionField.setSelectedItem("New");
            conditionField.setEnabled(false);
        } else {
            conditionField.setEnabled(true);
        }
    }

    // Writes data from existing object into entry components during update requests
    private void preFillData() {
        itemNameField.setText(currentItem.itemName);
        selectedImagePath = currentItem.itemImage;
        imagePathLabel.setText(new File(selectedImagePath).getName());
        descriptionField.setText(currentItem.description);
        categoryField.setSelectedItem(currentItem.category.replace("_", " "));
        conditionField.setSelectedItem(currentItem.condition);
        quantityField.setText(String.valueOf(currentItem.itemQuantity));
        pickupLocationField.setText(currentItem.pickupArea);
        
        if (currentItem.pickupDays != null) {
            String[] daysSelected = currentItem.pickupDays.split(",");
            int selectedCount = 0;
            for (String d : daysSelected) {
                String prefix = d.trim().substring(0, 3);
                for (int i = 0; i < 7; i++) {
                    if (pickupDayBoxes[i].getText().equalsIgnoreCase(prefix)) {
                        pickupDayBoxes[i].setSelected(true);
                        selectedCount++;
                    }
                }
            }
            if (selectedCount == 7) {
                pickupDayBoxes[7].setSelected(true);
            }
        }
        if (priceField != null && currentItem.price > 0) priceField.setText(String.valueOf(currentItem.price));
        if (maximumBorrowDaysField != null && currentItem.maximumBorrowDays > 0) maximumBorrowDaysField.setText(String.valueOf(currentItem.maximumBorrowDays));
        if (desiredItemField != null && currentItem.desiredItem != null) desiredItemField.setText(currentItem.desiredItem);
    }
    
    // Spawns lower navigation control strip housing cancel and process confirmation buttons
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
                if (successListener != null) {
                    successListener.onSuccess();
                }
                
                Window parent = SwingUtilities.getWindowAncestor(this);
                if(parent != null) {
                    parent.revalidate();
                    parent.repaint();
                }
                
                dispose();
            }
        });
        wrapper.add(cancelBtn);
        wrapper.add(submitBtn);
        return wrapper;
    }

    // Inspects all form lengths, bounds, selection flags, and value limits for correctness
    private boolean isFormValid() {
        if (selectedImagePath == null || selectedImagePath.trim().isEmpty()) {
            showError("An item image is required. Please browse and select an image.");
            return false;
        }

        String itemName = itemNameField.getText().trim();
        if (itemName.length() < 1 || itemName.length() > 150) { 
            showError("Item Name must be between 1 and 150 characters long."); 
            return false; 
        }

        if (descriptionField.getText().trim().length() < 4) { 
            showError("Description must be at least 4 characters long."); 
            return false; 
        }

        if (pickupLocationField.getText().trim().length() < 4) { 
            showError("Pickup Location must be at least 4 characters long."); 
            return false; 
        }
        
        String qtyStr = quantityField.getText().trim();
        if (qtyStr.length() < 1 || qtyStr.length() > 3) {
            showError("Quantity must be between 1 and 3 characters long.");
            return false;
        }
        try { 
            int qty = Integer.parseInt(qtyStr); 
            if (qty < 1) {
                showError("Quantity must be at least 1."); 
                return false; 
            }
        } catch (NumberFormatException e) { 
            showError("Quantity must be a valid number."); 
            return false; 
        }
        
        if (priceField != null) {
            String priceStr = priceField.getText().trim();
            if (priceStr.length() < 1 || priceStr.length() > 6) {
                showError("Price must be between 1 and 6 characters long.");
                return false;
            }
            try { 
                int price = Integer.parseInt(priceStr); 
                if (price < 0) {
                    showError("Price cannot be negative."); 
                    return false; 
                }
            } catch (NumberFormatException e) { 
                showError("Price must be a valid number."); 
                return false; 
            }
        }
        
        if (maximumBorrowDaysField != null) {
            String daysStr = maximumBorrowDaysField.getText().trim();
            if (daysStr.length() < 1 || daysStr.length() > 3) {
                showError("Maximum borrow days must be between 1 and 3 digits.");
                return false;
            }
            try { 
                int days = Integer.parseInt(daysStr); 
                if (days < 1 || days > 100) {
                    showError("Maximum borrow days must be between 1 and 100."); 
                    return false; 
                }
            } catch (NumberFormatException e) { 
                showError("Maximum borrow days must be a valid number."); 
                return false; 
            }
        }
        
        if (desiredItemField != null) {
            if (desiredItemField.getText().trim().length() < 4) { 
                showError("Desired Item must be at least 4 characters long."); 
                return false; 
            }
        }
        
        boolean daySelected = false;
        for (int i = 0; i < 7; i++) {
            if (pickupDayBoxes[i].isSelected()) {
                daySelected = true;
                break;
            }
        }
        if (!daySelected) {
            showError("Please select at least one pickup day.");
            return false;
        }
        
        return true;
    }

    // Directs flow to copy files and choose between database insertion or updates
    private void saveData() {
        copyImageFileToLocalDirectory();
        
        if (currentItem == null) insertItemIntoDatabase();
        else updateItemInDatabase();
    }
    
    // Reads external paths and duplicates target files into project source and binary asset trees
    private void copyImageFileToLocalDirectory() {
        if (selectedImageFile != null) {
            try {
                String projectPath = System.getProperty("user.dir");
                File srcDestDir = new File(projectPath, "src" + File.separator + "resources" + File.separator + "items");
                
                if (!srcDestDir.exists()) {
                    srcDestDir.mkdirs();
                }
                
                File srcDestFile = new File(srcDestDir, selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), srcDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                File binDestDir = new File(projectPath, "bin" + File.separator + "resources" + File.separator + "items");
                if (!binDestDir.exists()) {
                    binDestDir.mkdirs();
                }
                File binDestFile = new File(binDestDir, selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), binDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to copy the image file to the local directory.");
            }
        }
    }

    // Assembles components into a fresh row record and passes it to manager insert queries
    private void insertItemIntoDatabase() {
        ItemRecord newItem = new ItemRecord();
        newItem.ownerId = this.currentUserId; 
        newItem.itemName = itemNameField.getText().trim();
        newItem.itemQuantity = Integer.parseInt(quantityField.getText().trim());
        newItem.itemImage = selectedImagePath; 
        newItem.description = descriptionField.getText().trim();
        newItem.category = categoryField.getSelectedItem().toString().replace(" ", "_");
        newItem.condition = conditionField.getSelectedItem().toString();
        newItem.pickupArea = pickupLocationField.getText().trim();
        
        StringBuilder days = new StringBuilder();
        String[] fullDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) {
            if (pickupDayBoxes[i].isSelected()) {
                if (days.length() > 0) days.append(",");
                days.append(fullDays[i]);
            }
        }
        newItem.pickupDays = days.toString();
        newItem.pickupTime = pickupTimeField.getValue().toString();
        
        if (priceField != null) {
            newItem.price = Integer.parseInt(priceField.getText().trim());
            newItem.action = "Marketplace";
            ItemActionManager.addMarketplaceItem(newItem);
        } else if (maximumBorrowDaysField != null) {
            newItem.maximumBorrowDays = Integer.parseInt(maximumBorrowDaysField.getText().trim());
            newItem.action = "Sharing";
            ItemActionManager.addSharingItem(newItem);
        } else if (desiredItemField != null) {
            newItem.desiredItem = desiredItemField.getText().trim();
            newItem.action = "Trade";
            ItemActionManager.addTradeItem(newItem);
        }
    }

    // Gathers fields to update values on tracking references and calls change logs
    private void updateItemInDatabase() {
        currentItem.itemName = itemNameField.getText().trim();
        currentItem.itemQuantity = Integer.parseInt(quantityField.getText().trim());
        currentItem.itemImage = selectedImagePath; 
        currentItem.description = descriptionField.getText().trim();
        currentItem.category = categoryField.getSelectedItem().toString().replace(" ", "_");
        currentItem.condition = conditionField.getSelectedItem().toString();
        currentItem.pickupArea = pickupLocationField.getText().trim();
        currentItem.pickupTime = pickupTimeField.getValue().toString();
        
        StringBuilder days = new StringBuilder();
        String[] fullDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) {
            if (pickupDayBoxes[i].isSelected()) {
                if (days.length() > 0) days.append(",");
                days.append(fullDays[i]);
            }
        }
        currentItem.pickupDays = days.toString();
        
        if (priceField != null) currentItem.price = Integer.parseInt(priceField.getText().trim());
        if (maximumBorrowDaysField != null) currentItem.maximumBorrowDays = Integer.parseInt(maximumBorrowDaysField.getText().trim());
        if (desiredItemField != null) currentItem.desiredItem = desiredItemField.getText().trim();

        boolean success = ItemActionManager.updateItemDetailed(currentItem, "update item details");
        if (!success) showError("Database Error");
    }

    // Opens a basic text box window showing error notification details
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}