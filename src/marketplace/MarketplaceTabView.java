package marketplace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import javax.swing.*;
import components.*;
import database.UserRecord;
import enums.MarketplaceTabMode;

public class MarketplaceTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    public MarketplaceTabView(UserRecord user) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        subTabs.addTab("Marketplace", new BaseBrowsePanel(user, "Search in Marketplace...", null, MarketplaceTabMode.MARKETPLACE) {
            @Override
            protected void onActionClicked() {
            }
        });
        
        subTabs.addTab("Selling Center", new BaseBrowsePanel(user, "Search in Selling Center...", "Sell an Item", MarketplaceTabMode.MARKETPLACE_WITHDRAWN) {
            @Override
            protected void onActionClicked() {
                new ItemForm((Window) SwingUtilities.getWindowAncestor(this), "Sell an Item", null);
            }
        });

        add(subTabs, BorderLayout.CENTER);
    }
}