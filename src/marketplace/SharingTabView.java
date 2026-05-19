package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import database.UserRecord;
import enums.MarketplaceTabMode;

public class SharingTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    public SharingTabView(UserRecord user) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        subTabs.addTab("Borrow Request", new BaseBrowsePanel(user, "Search in Borrowing...", null, MarketplaceTabMode.SHARING) {
            @Override
            protected void onActionClicked() {
            }
        });
        
        subTabs.addTab("Lending Center", new BaseBrowsePanel(user, "Search in Lending Center...", "Lend an Item", MarketplaceTabMode.SHARING_WITHDRAWN) {
            @Override
            protected void onActionClicked() {
                new ItemForm((Window) SwingUtilities.getWindowAncestor(this), "Lend an Item", null, user.user_id, () -> {
                	// UPDATED: Now calls the unified refresh data method
                	this.refreshData();
                });
            }
        });
        
        subTabs.addTab("Request Approval", new SplitRequestPanel(user, "Pending Approvals", MarketplaceTabMode.SHARING_APPROVAL));
        subTabs.addTab("Return Item", new SplitRequestPanel(user, "Items to Return", MarketplaceTabMode.SHARING_RETURN));

        subTabs.addTabSelectionListener((index, title, content) -> {
        	if (content instanceof BaseBrowsePanel) {
        		((BaseBrowsePanel) content).refreshData();
        	} else if (content instanceof SplitRequestPanel) {
        		((SplitRequestPanel) content).refreshData();
        	}
        });

        add(subTabs, BorderLayout.CENTER);
    }
}