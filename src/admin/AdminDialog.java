package admin;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import components.*;
import utils.*;

public class AdminDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public AdminDialog(Window owner, String titleText, JPanel contentPanel, String submitBtnText,
            ActionListener onSubmit) {
        super(owner, titleText, ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(true);

        CustomPanel mainPanel = new CustomPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setPadding(20);

        // Header
        CustomLabel title = new CustomLabel(titleText, 18f, FontStyle.BOLD);
        CustomPanel headerPanel = new CustomPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.add(title);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        if (contentPanel != null) {
            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(12);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }
        add(mainPanel, BorderLayout.CENTER);

        // Footer buttons
        CustomPanel bottomPanel = new CustomPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setPadding(10, 20);

        CustomButton cancelBtn = new CustomButton("Cancel", 8);
        cancelBtn.setDefaultColor(Color.GRAY);
        cancelBtn.setTextColor(Color.WHITE);
        cancelBtn.setPadding(5, 15, 5, 15);
        cancelBtn.addActionListener(__e__ -> dispose());

        CustomButton submitBtn = new CustomButton(submitBtnText != null ? submitBtnText : "Submit", 8);
        submitBtn.setDefaultColor(Brand.PRIMARY_COLOR);
        submitBtn.setTextColor(Color.WHITE);
        submitBtn.setPadding(5, 15, 5, 15);
        submitBtn.addActionListener(e -> {
            if (onSubmit != null) onSubmit.actionPerformed(e);
        });

        bottomPanel.add(cancelBtn);
        bottomPanel.add(submitBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        capToScreen();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void capToScreen() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxW = (int) (screen.width  * 0.90);
        int maxH = (int) (screen.height * 0.85);

        int w = getWidth();
        int h = getHeight();

        if (w > maxW) w = maxW;
        if (h > maxH) h = maxH;
        if (w < 520)  w = 520;
        if (h < 300)  h = 300;

        setSize(w, h);
        setLocationRelativeTo(getOwner());
    }
}