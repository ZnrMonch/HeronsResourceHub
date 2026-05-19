package marketplace;

import java.awt.*;
import components.*;
import database.UserRecord;

public class Marketplace extends CustomTabbedPane {
    private static final long serialVersionUID = 1L;
    
    public Marketplace(UserRecord user) {
        setBackground(Color.WHITE);
        setRadius(20);    
        
        addTab("MARKETPLACE", "/resources/icons/marketplace.png", new MarketplaceTabView(user));        
        addTab("SHARING CENTER", "/resources/icons/sharing.png", new SharingTabView(user));
        addTab("BARTER TRADING", "/resources/icons/barter.png", new TradingTabView(user));
    }
}