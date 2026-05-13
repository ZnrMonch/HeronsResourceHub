package marketplace;

import java.awt.*;
import components.*;

public class Marketplace extends CustomTabbedPane {
    private static final long serialVersionUID = 1L;
    
    public Marketplace() {
        setBackground(Color.WHITE);
        setRadius(20);    
        
        addTab("MARKETPLACE", "/resources/icons/marketplace.png", new MarketplaceTabView());        
        addTab("SHARING", "/resources/icons/sharing.png", new SharingTabView());
        addTab("BARTER TRADING", "/resources/icons/barter.png", new TradingTabView());
    }
}