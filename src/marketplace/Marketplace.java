package marketplace;

import java.awt.*;
import components.*;
import database.UserRecord;

public class Marketplace extends CustomTabbedPane {
    
    // **FIELDS**
    
    // Set version ID for data saving
    private static final long serialVersionUID = 1L;
    
    // **CONSTRUCTORS**
    
    // Build the main tab panel layout
    public Marketplace(UserRecord user) {
        // Change color to white
        setBackground(Color.WHITE);
        // Curve the corners
        setRadius(20);    
        
        // Insert marketplace buy and sell tab
        addTab("MARKETPLACE", "/resources/icons/marketplace.png", new MarketplaceTabView(user));        
        // Insert sharing center borrow tab
        addTab("SHARING CENTER", "/resources/icons/sharing.png", new SharingTabView(user));
        // Insert barter trade exchange tab
        addTab("BARTER TRADING", "/resources/icons/barter.png", new TradingTabView(user));
    }
}