package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import database.UserRecord;
import enums.MarketplaceTabMode;

/**
 * Represents the main view for the Sharing Center section.
 * This panel uses a tabbed layout to separate general borrowing (Borrow Request),
 * managing the user's lent items (Lending Center), approving borrow requests, 
 * and processing returned items.
 */
public class SharingTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Constructs the SharingTabView.
     * * @param user The current logged-in user, used for fetching data, attaching ownership to new items, 
     * and managing pending requests.
     */
    public SharingTabView(UserRecord user) {
        
        // --- 1. Base Panel Setup ---
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 2. Sub-Tabs Initialization ---
        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        // Tab 1: Borrow Request (Browsing available items that the user can borrow from others)
        subTabs.addTab("Borrow Request", new BaseBrowsePanel(user, "Search in Borrowing...", null, MarketplaceTabMode.SHARING) {
            @Override
            protected void onActionClicked() {
                // No global action button needed for the general browsing tab
            }
        });
        
        // Tab 2: Lending Center (Managing items the user has listed for others to borrow)
        subTabs.addTab("Lending Center", new BaseBrowsePanel(user, "Search in Lending Center...", "Lend an Item", MarketplaceTabMode.SHARING_WITHDRAWN) {
            
            /**
             * Triggered when the "Lend an Item" button is clicked.
             * Opens the ItemForm dialog to create a new sharing/lending listing.
             */
            @Override
            protected void onActionClicked() {
                new ItemForm((Window) SwingUtilities.getWindowAncestor(this), "Lend an Item", null, user.user_id, () -> {
                    // Unified refresh: updates the UI immediately after a successful database insertion
                    this.refreshData();
                });
            }
        });
        
        // Tab 3: Request Approval (Split view to manage incoming requests from users who want to borrow your items)
        subTabs.addTab("Request Approval", new SplitRequestPanel(user, "Pending Approvals", MarketplaceTabMode.SHARING_APPROVAL));
        
        // Tab 4: Return Item (Split view to manage items that are being returned to you)
        subTabs.addTab("Return Item", new SplitRequestPanel(user, "Items to Return", MarketplaceTabMode.SHARING_RETURN));

        // --- 3. Event Listeners ---
        
        /**
         * Ensures that whenever the user switches between any of the sharing sub-tabs, 
         * the UI fetches the freshest data from the database. It checks the instance type
         * to call the appropriate refresh method.
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