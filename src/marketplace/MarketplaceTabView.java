package marketplace;

import javax.swing.*;

import enums.MarketplaceTabMode;

public class MarketplaceTabView extends BaseBrowsePanel {
    private static final long serialVersionUID = 1L;

    public MarketplaceTabView() {
        super("Search in Marketplace...", "Sell Item", MarketplaceTabMode.MARKETPLACE);
    }

    @Override
    protected void onActionClicked() {
        new ItemForm((JFrame) SwingUtilities.getWindowAncestor(this), "Sell an Item");
    }
}