package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;

public class SharingTabView extends CustomPanel {
    private static final long serialVersionUID = 1L;

    public SharingTabView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        CustomTabbedPane subTabs = new CustomTabbedPane();
        
        subTabs.addTab("Borrow Request", new BaseBrowsePanel("Search in Borrowing...", "Lend an Item", MarketplaceTabMode.SHARING) {
            @Override
            protected void onActionClicked() {
                new ItemForm((JFrame) SwingUtilities.getWindowAncestor(this), "Lend an Item");
            }
        });
        
        subTabs.addTab("Request Approval", new SplitRequestPanel("Pending Approvals"));
        subTabs.addTab("Return Item", new SplitRequestPanel("Items to Return"));

        add(subTabs, BorderLayout.CENTER);
    }
}