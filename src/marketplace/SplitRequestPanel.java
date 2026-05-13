package marketplace;

import java.awt.*;
import javax.swing.*;
import components.*;
import utils.*;

public class SplitRequestPanel extends CustomPanel {
    private static final long serialVersionUID = 1L;

    public SplitRequestPanel(String listTitle) {
        setLayout(new BorderLayout(15, 0));
        setPadding(10);

        CustomPanel westPanel = new CustomPanel();
        westPanel.setLayout(new BorderLayout());
        westPanel.setPreferredSize(new Dimension(250, 0));
        westPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        CustomLabel titleLabel = new CustomLabel(listTitle, 16f, FontStyle.BOLD);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        westPanel.add(titleLabel, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = 1; i <= 15; i++) {
            listModel.addElement("Request Item " + i);
        }
        JList<String> requestList = new JList<>(listModel);
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestList.setFixedCellHeight(40);

        JScrollPane listScroll = new JScrollPane(requestList);
        listScroll.setBorder(BorderFactory.createEmptyBorder());
        westPanel.add(listScroll, BorderLayout.CENTER);

        CustomPanel centerPanel = new CustomPanel();
        centerPanel.setLayout(new BorderLayout());
        CustomLabel placeholderLabel = new CustomLabel("Select an item to view details", 14f, FontStyle.REGULAR);
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(placeholderLabel, BorderLayout.CENTER);

        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                placeholderLabel.setText("Viewing details for: " + requestList.getSelectedValue());
            }
        });

        add(westPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }
}