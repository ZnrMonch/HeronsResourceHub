package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import database.UserRecord;
import enums.MarketplaceTabMode;

/**
 * Represents the main view for the Barter Trading section.
 * This panel uses a tabbed layout to separate general trading (Browse Trades),
 * managing the user's offered trade items (Trading Center), 
 * and approving incoming trade proposals (Request Approval).
 */
public class TradingTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Constructs the TradingTabView.
     * @param user The current logged-in user, used for data fetching, 
     * ownership tracking, and managing trade requests.
     */
    public TradingTabView(UserRecord user) {
        
        // --- 1. Base Panel Setup ---
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 2. Sub-Tabs Initialization ---
        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        // Tab 1: Browse Trades (Marketplace for items open to barter)
        subTabs.addTab("Browse Trades", new BaseBrowsePanel(user, "Search in Trading...", null, MarketplaceTabMode.TRADE) {
            @Override
            protected void onActionClicked() {
                // No global action button needed for browsing
            }
        });
        
        // Tab 2: Trading Center (Managing the user's own trade listings)
        subTabs.addTab("Trading Center", new BaseBrowsePanel(user, "Search in Trading Center...", "Offer an Item", MarketplaceTabMode.TRADE_WITHDRAWN) {
            
            /**
             * Triggered when the "Offer an Item" button is clicked.
             * Opens the ItemForm dialog to create a new trade listing.
             */
            @Override
            protected void onActionClicked() {
                new ItemForm((Window) SwingUtilities.getWindowAncestor(this), "Offer an Item", null, user.user_id, () -> {
                    // Refreshes the panel after the user submits or updates an item
                    this.refreshData();
                });
            }
        });
        
        // Tab 3: Request Approval (Management of incoming trade proposals)
        subTabs.addTab("Request Approval", new SplitRequestPanel(user, "Request Approval", MarketplaceTabMode.TRADE_APPROVAL));

        // --- 3. Event Listeners ---
        
        /**
         * Refreshes the active sub-tab content whenever the user switches tabs,
         * ensuring the most recent trade requests and listings are displayed.
         */
        subTabs.addTabSelectionListener((index, title, content) -> {
            if (content instanceof BaseBrowsePanel) {
                ((BaseBrowsePanel) content).refreshData();
            } else if (content instanceof SplitRequestPanel) {
                ((SplitRequestPanel) content).refreshData();
            }
        });

        // --- 4. Final Layout Assembly ---
        add(subTabs, BorderLayout.CENTER);
    }
}