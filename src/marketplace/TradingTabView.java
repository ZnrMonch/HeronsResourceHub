package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import enums.MarketplaceTabMode;

public class TradingTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    public TradingTabView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        subTabs.addTab("Browse Trades", new BaseBrowsePanel("Search in Trading...", "Offer an Item", MarketplaceTabMode.TRADING) {
            @Override
            protected void onActionClicked() {
                new ItemForm((JFrame) SwingUtilities.getWindowAncestor(this), "Offer an Item");
            }
        });
        
        subTabs.addTab("In Return Request", new SplitRequestPanel("Inbound Requests"));
        subTabs.addTab("Accept Request", new SplitRequestPanel("Accepted Trades"));

        add(subTabs, BorderLayout.CENTER);
    }
}