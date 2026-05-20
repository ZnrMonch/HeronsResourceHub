package pages;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.*;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import components.*;
import database.DatabaseManager;
import database.UserRecord;
import utils.*;

public class TransactionHistory extends CustomPanel {
   private static final long serialVersionUID = 1L;

   private static final Color WHITE       = Color.WHITE;
   private static final Color BLUE_BG     = new Color(219, 234, 254);
   private static final Color YELLOW_BG   = new Color(254, 243, 199);
   private static final Color RED_BG      = new Color(254, 226, 226);
   private static final Color GRAY_BG     = new Color(241, 245, 249);
   private static final Color GREEN_TEXT  = new Color(22,  163,  74);
   private static final Color YELLOW_TEXT = new Color(161,  98,   7);
   private static final Color RED_TEXT    = new Color(185,  28,  28);
   private static final Color GREEN_PILL  = new Color(220, 252, 231);
   private static final Color YELLOW_PILL = new Color(254, 243, 199);
   private static final Color RED_PILL    = new Color(254, 226, 226);
   private static final Color DIVIDER     = new Color(226, 232, 240);

   private static final String[] TYPES    = {"All Types",  "Buy", "Borrow", "Lend", "Trade"};
   private static final String[] STATUSES = {"All Status", "Completed", "Pending", "Cancelled"};
   private static final String[] TIMES    = {"All Time",   "Today", "This Week", "This Month", "This Year"};

   private static final Map<String, String> ICONS = new HashMap<>();
   static {
       ICONS.put("Buy",    "/resources/icons/items.png");
       ICONS.put("Borrow", "/resources/icons/items.png");
       ICONS.put("Lend",   "/resources/icons/items.png");
       ICONS.put("Trade",  "/resources/icons/items.png");
   }

   private static final int PAGE_SIZE = 10;

   private final UserRecord user;
   private List<TxRow>      all   = new ArrayList<>();
   private List<TxRow>      shown = new ArrayList<>();
   private int              currentPage = 1;

   private CustomSearchField      search;
   private CustomComboBox<String> typeBox;
   private CustomComboBox<String> statusBox;
   private CustomComboBox<String> timeBox;
   private JPanel                 listPanel;
   private JScrollPane            scrollPane;

   private CustomLabel  pageInfoLabel;
   private CustomButton btnFirst;
   private CustomButton btnPrev;
   private CustomButton btnNext;
   private CustomButton btnLast;

   public TransactionHistory(UserRecord user) {
       this.user = user;
       setLayout(new BorderLayout());
       setBackground(WHITE);
       setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

       add(buildHeader(),     BorderLayout.NORTH);
       add(buildScrollArea(), BorderLayout.CENTER);
       add(buildPagination(), BorderLayout.SOUTH);

       loadFromDatabase();
   }

   private JPanel buildHeader() {
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBackground(WHITE);

       CustomLabel title = new CustomLabel(
           "Transaction History", Brand.HEADER3_TEXT_SIZE, FontStyle.BOLD);
       title.setForeground(Brand.PRIMARY_COLOR);
       title.setAlignmentX(LEFT_ALIGNMENT);

       CustomLabel subtitle = new CustomLabel(
           "A complete record of your borrowing, lending, buying, and trading activities.",
           Brand.STANDARD_TEXT_SIZE, FontStyle.REGULAR);
       subtitle.setForeground(Color.GRAY);
       subtitle.setAlignmentX(LEFT_ALIGNMENT);

       panel.add(title);
       panel.add(Box.createVerticalStrut(4));
       panel.add(subtitle);
       panel.add(Box.createVerticalStrut(16));

       search = new CustomSearchField("Search by item name...", 20, 10);
       search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
       search.setAlignmentX(LEFT_ALIGNMENT);
       search.getDocument().addDocumentListener(new DocumentListener() {
           public void insertUpdate(DocumentEvent e)  { resetAndFilter(); }
           public void removeUpdate(DocumentEvent e)  { resetAndFilter(); }
           public void changedUpdate(DocumentEvent e) { resetAndFilter(); }
       });
       panel.add(search);
       panel.add(Box.createVerticalStrut(10));

       typeBox   = new CustomComboBox<>(TYPES,    10);
       statusBox = new CustomComboBox<>(STATUSES, 10);
       timeBox   = new CustomComboBox<>(TIMES,    10);
       for (CustomComboBox<String> box : new CustomComboBox[]{typeBox, statusBox, timeBox}) {
           box.setLightWeightPopupEnabled(false);
           box.setMaximumRowCount(6);
           box.addActionListener(e -> resetAndFilter());
       }

       JPanel filterRow = new JPanel(new GridLayout(1, 3, 10, 0));
       filterRow.setOpaque(false);
       filterRow.setAlignmentX(LEFT_ALIGNMENT);
       filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
       filterRow.add(typeBox);
       filterRow.add(statusBox);
       filterRow.add(timeBox);

       panel.add(filterRow);
       panel.add(Box.createVerticalStrut(16));

       JSeparator line = new JSeparator();
       line.setForeground(Brand.COLOR_BORDER);
       line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
       panel.add(line);
       panel.add(Box.createVerticalStrut(12));

       return panel;
   }

   private JScrollPane buildScrollArea() {
       listPanel = new JPanel();
       listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
       listPanel.setBackground(WHITE);

       JPanel wrapper = new JPanel(new BorderLayout());
       wrapper.setBackground(WHITE);
       wrapper.add(listPanel, BorderLayout.NORTH);

       scrollPane = new JScrollPane(wrapper);
       scrollPane.setBorder(BorderFactory.createEmptyBorder());
       scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       scrollPane.getVerticalScrollBar().setUnitIncrement(16);
       scrollPane.getViewport().setBackground(WHITE);

       return scrollPane;
   }

   private JPanel buildPagination() {
       JPanel bar = new JPanel(new BorderLayout());
       bar.setBackground(WHITE);
       bar.setBorder(BorderFactory.createCompoundBorder(
           BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
           BorderFactory.createEmptyBorder(10, 0, 0, 0)
       ));

       pageInfoLabel = new CustomLabel("", Brand.STANDARD_TEXT_SIZE - 1f, FontStyle.REGULAR);
       pageInfoLabel.setForeground(new Color(100, 116, 139));
       bar.add(pageInfoLabel, BorderLayout.WEST);

       btnFirst = new CustomButton(
               IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-left.png", 20, 20, Color.WHITE), 5);
       btnPrev = new CustomButton(
               IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-left.png", 20, 20, Color.WHITE), 5);
       btnNext = new CustomButton(
               IconLoader.loadAndScaleColorizedIcon("/resources/icons/arrow-right.png", 20, 20, Color.WHITE), 5);
       btnLast = new CustomButton(
               IconLoader.loadAndScaleColorizedIcon("/resources/icons/double-arrow-right.png", 20, 20, Color.WHITE),
               5);

       btnFirst.addActionListener(e -> goToPage(1));
       btnPrev.addActionListener(e -> goToPage(currentPage - 1));
       btnNext.addActionListener(e -> goToPage(currentPage + 1));
       btnLast.addActionListener(e -> goToPage(totalPages()));

       CustomPanel wrapper = new CustomPanel();
       wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
       wrapper.add(btnFirst);
       wrapper.add(Box.createHorizontalStrut(5));
       wrapper.add(btnPrev);
       wrapper.add(Box.createHorizontalStrut(5));
       wrapper.add(btnNext);
       wrapper.add(Box.createHorizontalStrut(5));
       wrapper.add(btnLast);

       bar.add(wrapper, BorderLayout.EAST);

       return bar;
   }

   private int totalPages() {
       if (shown.isEmpty()) return 1;
       return (int) Math.ceil((double) shown.size() / PAGE_SIZE);
   }

   private void goToPage(int page) {
       int total = totalPages();
       currentPage = Math.max(1, Math.min(page, total));
       renderCards();
       SwingUtilities.invokeLater(() ->
           scrollPane.getVerticalScrollBar().setValue(0));
   }

   private List<TxRow> currentPageItems() {
       int from = (currentPage - 1) * PAGE_SIZE;
       int to   = Math.min(from + PAGE_SIZE, shown.size());
       if (from >= shown.size()) return Collections.emptyList();
       return shown.subList(from, to);
   }

   private void updatePageInfo() {
       int total = shown.size();
       if (total == 0) {
           pageInfoLabel.setText("Showing 0 items");
       } else {
           int from = (currentPage - 1) * PAGE_SIZE + 1;
           int to   = Math.min(currentPage * PAGE_SIZE, total);
           pageInfoLabel.setText(
               "Showing " + from + " to " + to + " out of " + total + " items");
       }

       btnFirst.setEnabled(currentPage > 1);
       btnPrev.setEnabled(currentPage > 1);
       btnNext.setEnabled(currentPage < totalPages());
       btnLast.setEnabled(currentPage < totalPages());
   }

   private void renderCards() {
       listPanel.removeAll();

       List<TxRow> page = currentPageItems();

       if (page.isEmpty()) {
           listPanel.add(buildEmptyState());
       } else {
           for (int i = 0; i < page.size(); i++) {
               listPanel.add(buildCard(page.get(i)));
               if (i < page.size() - 1) {
                   JSeparator sep = new JSeparator();
                   sep.setForeground(DIVIDER);
                   sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                   listPanel.add(sep);
               }
           }
       }

       listPanel.revalidate();
       listPanel.repaint();
       updatePageInfo();
   }

   private JPanel buildEmptyState() {
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setOpaque(false);
       panel.setBorder(BorderFactory.createEmptyBorder(48, 0, 48, 0));

       CustomLabel icon = new CustomLabel("📋", 36f, FontStyle.REGULAR);
       icon.setAlignmentX(CENTER_ALIGNMENT);
       icon.setHorizontalAlignment(SwingConstants.CENTER);

       CustomLabel msg = new CustomLabel(
           "No transactions found.", Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
       msg.setForeground(new Color(100, 116, 139));
       msg.setAlignmentX(CENTER_ALIGNMENT);
       msg.setHorizontalAlignment(SwingConstants.CENTER);

       CustomLabel hint = new CustomLabel(
           "Try adjusting your filters or search query.",
           Brand.STANDARD_TEXT_SIZE - 1f, FontStyle.REGULAR);
       hint.setForeground(new Color(148, 163, 184));
       hint.setAlignmentX(CENTER_ALIGNMENT);
       hint.setHorizontalAlignment(SwingConstants.CENTER);

       panel.add(icon);
       panel.add(Box.createVerticalStrut(10));
       panel.add(msg);
       panel.add(Box.createVerticalStrut(4));
       panel.add(hint);

       JPanel centeredWrapper = new JPanel(new GridBagLayout());
       centeredWrapper.setOpaque(false);
       centeredWrapper.add(panel);
       return centeredWrapper;
   }

   private JPanel buildCard(TxRow tx) {
       Color bg = getRowColor(tx.status);

       JPanel wrapper = new JPanel(new BorderLayout()) {
           @Override public Dimension getMaximumSize()  { return new Dimension(Integer.MAX_VALUE, 90); }
           @Override public Dimension getPreferredSize(){ return new Dimension(super.getPreferredSize().width, 90); }
           @Override public Dimension getMinimumSize()  { return new Dimension(200, 90); }
       };
       wrapper.setOpaque(false);
       wrapper.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

       JPanel card = new JPanel(new BorderLayout(14, 0)) {
           @Override
           protected void paintComponent(Graphics g) {
               Graphics2D g2 = (Graphics2D) g.create();
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setColor(bg);
               g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
               g2.dispose();
           }
       };
       card.setOpaque(false);
       card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
       card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

       String iconPath = ICONS.getOrDefault(tx.type, "/resources/icons/logs.png");
       JLabel iconBadge = new JLabel(IconLoader.loadAndScaleIcon(iconPath, 20, 20)) {
           @Override
           protected void paintComponent(Graphics g) {
               Graphics2D g2 = (Graphics2D) g.create();
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setColor(new Color(255, 255, 255, 180));
               g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
               g2.dispose();
               super.paintComponent(g);
           }
       };
       iconBadge.setOpaque(false);
       iconBadge.setPreferredSize(new Dimension(40, 40));
       iconBadge.setMinimumSize(new Dimension(40, 40));
       iconBadge.setMaximumSize(new Dimension(40, 40));
       iconBadge.setHorizontalAlignment(SwingConstants.CENTER);
       iconBadge.setVerticalAlignment(SwingConstants.CENTER);
       card.add(iconBadge, BorderLayout.WEST);

       JPanel center = new JPanel();
       center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
       center.setOpaque(false);
       center.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));

       CustomLabel name = new CustomLabel(
           tx.itemName, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
       name.setForeground(Brand.PRIMARY_COLOR);
       name.setAlignmentX(LEFT_ALIGNMENT);

       CustomLabel meta = new CustomLabel(
           tx.role + "  ·  " + tx.date,
           Brand.STANDARD_TEXT_SIZE - 2f, FontStyle.REGULAR);
       meta.setForeground(new Color(140, 140, 140));
       meta.setAlignmentX(LEFT_ALIGNMENT);

       center.add(Box.createVerticalGlue());
       center.add(name);
       center.add(Box.createVerticalStrut(3));
       center.add(meta);
       center.add(Box.createVerticalGlue());
       card.add(center, BorderLayout.CENTER);

       JPanel right = new JPanel();
       right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
       right.setOpaque(false);

       JLabel pill = buildPill(tx.status);
       pill.setAlignmentX(RIGHT_ALIGNMENT);

       CustomLabel karma = buildKarma(tx.karmaDelta);
       karma.setAlignmentX(RIGHT_ALIGNMENT);

       right.add(Box.createVerticalGlue());
       right.add(pill);
       right.add(Box.createVerticalStrut(4));
       right.add(karma);
       right.add(Box.createVerticalGlue());
       card.add(right, BorderLayout.EAST);

       wrapper.add(card, BorderLayout.CENTER);
       return wrapper;
   }

   private JLabel buildPill(String status) {
       JLabel pill = new JLabel(status, SwingConstants.CENTER) {
           @Override
           protected void paintComponent(Graphics g) {
               Graphics2D g2 = (Graphics2D) g.create();
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setColor(getBackground());
               g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
               g2.dispose();
               super.paintComponent(g);
           }
       };
       pill.setOpaque(false);
       pill.setBackground(getPillColor(status));
       pill.setForeground(getPillText(status));
       pill.setFont(FontLib.POPPINS_REGULAR != null
           ? FontLib.POPPINS_REGULAR.deriveFont(11f)
           : new Font("SansSerif", Font.PLAIN, 11));
       pill.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
       return pill;
   }

   private CustomLabel buildKarma(int delta) {
       String text = (delta >= 0 ? "+ " : "\u2212 ") + Math.abs(delta);
       CustomLabel lbl = new CustomLabel(text, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
       lbl.setForeground(delta >= 0 ? GREEN_TEXT : RED_TEXT);
       lbl.setHorizontalAlignment(SwingConstants.RIGHT);
       return lbl;
   }

   private void resetAndFilter() {
       currentPage = 1;
       filter();
   }

   private void filter() {
       String query  = search.getText().trim().toLowerCase();
       String type   = (String) typeBox.getSelectedItem();
       String status = (String) statusBox.getSelectedItem();
       String time   = (String) timeBox.getSelectedItem();

       LocalDate today = LocalDate.now();

       shown = all.stream().filter(tx -> {
           if (!query.isEmpty() && !tx.itemName.toLowerCase().contains(query)) return false;
           if (!"All Types".equals(type)    && !tx.type.equalsIgnoreCase(type))     return false;
           if (!"All Status".equals(status) && !tx.status.equalsIgnoreCase(status)) return false;
           if (!"All Time".equals(time)) {
               if (tx.localDate == null) return false;
               return switch (time) {
                   case "Today"      -> tx.localDate.isEqual(today);
                   case "This Week"  -> !tx.localDate.isBefore(today.with(DayOfWeek.MONDAY));
                   case "This Month" -> tx.localDate.getMonth() == today.getMonth()
                                       && tx.localDate.getYear() == today.getYear();
                   case "This Year"  -> tx.localDate.getYear() == today.getYear();
                   default           -> true;
               };
           }
           return true;
       }).collect(Collectors.toList());

       renderCards();
   }

   private static final String SQL =
       "SELECT t.transaction_id, i.item_name, t.action, t.karma_impact, " +
       "       DATE_FORMAT(t.timestamp, '%b %d, %Y') AS fmt_date, " +
       "       CAST(t.timestamp AS DATE) AS raw_date, 'borrower' AS role " +
       "FROM transactions t JOIN items i ON t.item_id = i.item_id " +
       "WHERE t.borrower_id = ? " +
       "UNION " +
       "SELECT t.transaction_id, i.item_name, t.action, t.karma_impact, " +
       "       DATE_FORMAT(t.timestamp, '%b %d, %Y') AS fmt_date, " +
       "       CAST(t.timestamp AS DATE) AS raw_date, 'owner' AS role " +
       "FROM transactions t JOIN items i ON t.item_id = i.item_id " +
       "WHERE i.owner_id = ? AND t.borrower_id != ? " +
       "ORDER BY raw_date DESC";

   private void loadFromDatabase() {
       all.clear();
       try (Connection conn = DriverManager.getConnection(
               DatabaseManager.getURL(),
               DatabaseManager.getUser(),
               DatabaseManager.getPassword());
            PreparedStatement ps = conn.prepareStatement(SQL)) {

           ps.setInt(1, user.user_id);
           ps.setInt(2, user.user_id);
           ps.setInt(3, user.user_id);

           ResultSet rs = ps.executeQuery();
           while (rs.next()) {
               TxRow row         = new TxRow();
               row.transactionId = rs.getInt("transaction_id");
               row.itemName      = rs.getString("item_name");
               row.karmaDelta    = rs.getInt("karma_impact");
               row.date          = rs.getString("fmt_date");
               java.sql.Date d = rs.getDate("raw_date");
               if (d != null) row.localDate = d.toLocalDate();
               String action  = rs.getString("action");
               String roleRaw = rs.getString("role");
               row.type   = toType(action);
               row.status = toStatus(action);
               row.role   = toRoleLabel(action, roleRaw);
               all.add(row);
           }
       } catch (SQLException e) {
           System.err.println("DB error: " + e.getMessage());
       }
       shown = new ArrayList<>(all);
       currentPage = 1;
       renderCards();
   }

   private static String toType(String a) {
       if (a == null) return "Unknown";
       String s = a.toLowerCase();
       if (s.startsWith("buy") || s.startsWith("sell")) return "Buy";
       if (s.startsWith("borrow"))                       return "Borrow";
       if (s.startsWith("lend"))                         return "Lend";
       if (s.startsWith("trade"))                        return "Trade";
       return cap(a);
   }

   private static String toStatus(String a) {
       if (a == null) return "Pending";
       String s = a.toLowerCase();
       if (s.endsWith("-completed") || s.equals("buy"))         return "Completed";
       if (s.endsWith("-withdrawn") || s.endsWith("-declined")) return "Cancelled";
       return "Pending";
   }

   private static String toRoleLabel(String a, String role) {
       if (a == null) return "";
       String s = a.toLowerCase();
       boolean owner = "owner".equals(role);
       if (s.startsWith("buy") || s.startsWith("sell"))    return owner ? "Sell"   : "Buy";
       if (s.startsWith("borrow") || s.startsWith("lend")) return owner ? "Lend"   : "Borrow";
       if (s.startsWith("trade"))                           return "Trade";
       return cap(a);
   }

   private static String cap(String s) {
       if (s == null || s.isEmpty()) return s;
       return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
   }

   private Color getRowColor(String status) {
       if (status == null) return GRAY_BG;
       return switch (status.toLowerCase()) {
           case "completed" -> BLUE_BG;
           case "pending"   -> YELLOW_BG;
           case "cancelled" -> RED_BG;
           default          -> GRAY_BG;
       };
   }

   private Color getPillColor(String status) {
       if (status == null) return GREEN_PILL;
       return switch (status.toLowerCase()) {
           case "completed" -> GREEN_PILL;
           case "pending"   -> YELLOW_PILL;
           case "cancelled" -> RED_PILL;
           default          -> GREEN_PILL;
       };
   }

   private Color getPillText(String status) {
       if (status == null) return GREEN_TEXT;
       return switch (status.toLowerCase()) {
           case "completed" -> GREEN_TEXT;
           case "pending"   -> YELLOW_TEXT;
           case "cancelled" -> RED_TEXT;
           default          -> GREEN_TEXT;
       };
   }

   public static class TxRow {
       public int       transactionId;
       public String    itemName;
       public String    type;
       public String    role;
       public String    status;
       public int       karmaDelta;
       public String    date;
       public LocalDate localDate;
   }
}