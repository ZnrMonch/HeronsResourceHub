package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import database.UserRecord;
import enums.MarketplaceTabMode;

public class TradingTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    public TradingTabView(UserRecord user) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        subTabs.addTab("Browse Trades", new BaseBrowsePanel(user, "Search in Trading...", null, MarketplaceTabMode.TRADE) {
            @Override
            protected void onActionClicked() {
            }
        });
        
        subTabs.addTab("Trading Center", new BaseBrowsePanel(user, "Search in Trading Center...", "Offer an Item", MarketplaceTabMode.TRADE_WITHDRAWN) {
            @Override
            protected void onActionClicked() {
                new ItemForm((Window) SwingUtilities.getWindowAncestor(this), "Offer an Item", null);
            }
        });
        
        subTabs.addTab("Request Approval", new SplitRequestPanel(user, "Request Approval", MarketplaceTabMode.TRADE_APPROVAL));

        add(subTabs, BorderLayout.CENTER);
    }
}