package marketplace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import javax.swing.*;
import components.*;
import database.UserRecord;
import enums.MarketplaceTabMode;

/**
 * Represents the main view for the Marketplace section.
 * This panel uses a tabbed layout to separate general browsing (Marketplace) 
 * from the user's personal selling management (Selling Center).
 */
public class MarketplaceTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Constructs the MarketplaceTabView.
     * * @param user The current logged-in user, used for data fetching and attaching ownership to new items.
     */
    public MarketplaceTabView(UserRecord user) {
        
        // --- 1. Base Panel Setup ---
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 2. Sub-Tabs Initialization ---
        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        // Tab 1: Marketplace (Browsing active listings to buy)
        subTabs.addTab("Marketplace", new BaseBrowsePanel(user, "Search in Marketplace...", null, MarketplaceTabMode.MARKETPLACE) {
            @Override
            protected void onActionClicked() {
                // No global action button needed for the general browsing tab
            }
        });
        
        // Tab 2: Selling Center (Managing the user's own items for sale)
        subTabs.addTab("Selling Center", new BaseBrowsePanel(user, "Search in Selling Center...", "Sell an Item", MarketplaceTabMode.MARKETPLACE_WITHDRAWN) {
            
            /**
             * Triggered when the "Sell an Item" button is clicked.
             * Opens the ItemForm dialog to create a new marketplace listing.
             */
            @Override
            protected void onActionClicked() {
                new ItemForm((Window) SwingUtilities.getWindowAncestor(this), "Sell an Item", null, user.user_id, () -> {
                    // Unified refresh: updates the UI immediately after a successful database insertion
                    this.refreshData();
                });
            }
        });

        // --- 3. Event Listeners ---
        
        /**
         * Ensures that whenever the user switches between "Marketplace" and "Selling Center", 
         * the UI fetches the freshest data from the database.
         */
        subTabs.addTabSelectionListener((index, title, content) -> {
            if (content instanceof BaseBrowsePanel) {
                ((BaseBrowsePanel) content).refreshData();
            }
        });

        // --- 4. Final Layout Assembly ---
        add(subTabs, BorderLayout.CENTER);
    }
}