package auth;

import java.awt.*;
import javax.swing.*;
import java.sql.*;
import components.*;
import utils.Brand;
import utils.FontLib;
import utils.FontStyle;

public class SecurityDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    // ─── Database ────────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:4306/dbnorm2";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // ─── Security questions list ──────────────────────────────────────────────
    private static final String[] QUESTIONS = {
        "Select a Security Question",
        "What was the nickname your family called you as a child?",
        "What was the name of your first pet?",
        "What is your mother's complete maiden name?",
        "What was your favorite childhood food?",
        "What is the name of the hospital where you were born?",
        "What was your favorite family tradition during holidays?",
        "What is your favorite childhood movie?"
    };

    // ─── UI fields ───────────────────────────────────────────────────────────
    private final CustomComboBox<String> questionBox1 = new CustomComboBox<>(QUESTIONS, 5);
    private final CustomComboBox<String> questionBox2 = new CustomComboBox<>(QUESTIONS, 5);
    private final CustomComboBox<String> questionBox3 = new CustomComboBox<>(QUESTIONS, 5);

    private final CustomTextArea answerField1 = createAnswerField();
    private final CustomTextArea answerField2 = createAnswerField();
    private final CustomTextArea answerField3 = createAnswerField();

    private final CustomLabel errorLabel = new CustomLabel("");

    // ─── State ───────────────────────────────────────────────────────────────
    private final int     userId;
    private       boolean submitted = false;

    // ─── Constructor ─────────────────────────────────────────────────────────
    public SecurityDialog(JFrame parent, int userId) {
        super(parent, "Security Verification", true);
        this.userId = userId;

        FontLib.loadFonts();

        setSize(480, 650);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        buildUI();
        setVisible(true);
    }

    // ─── UI builder ──────────────────────────────────────────────────────────
    private void buildUI() {
        // Use a BorderLayout panel so children fill the full width naturally
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Title
        CustomLabel title = new CustomLabel("Set Security Questions");
        title.setFontStyle(FontStyle.BOLD);
        title.setFontSize(Brand.HEADER2_TEXT_SIZE);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 15, 0);
        content.add(title, gbc);

        // Three question blocks
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 10, 0);
        content.add(buildQuestionBlock("Security Question 1", questionBox1, answerField1), gbc);

        gbc.gridy = 2;
        content.add(buildQuestionBlock("Security Question 2", questionBox2, answerField2), gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 15, 0);
        content.add(buildQuestionBlock("Security Question 3", questionBox3, answerField3), gbc);

        // Error message
        errorLabel.setForeground(Color.RED);
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 10, 0);
        content.add(errorLabel, gbc);

        // Submit button — left-aligned to match the rest of the form
        CustomButton submitButton = new CustomButton("Confirm", 10);
        submitButton.addActionListener(e -> handleSubmit());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(submitButton);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0);
        content.add(buttonRow, gbc);

        add(content);
    }

    // ─── Builds one labeled question + answer block ───────────────────────────
    private JPanel buildQuestionBlock(String label,
                                      CustomComboBox<String> questionBox,
                                      CustomTextArea answerField) {
        JPanel block = new JPanel();
        block.setLayout(new GridBagLayout());
        block.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Section label
        CustomLabel sectionLabel = new CustomLabel(label, Brand.STANDARD_TEXT_SIZE, FontStyle.BOLD);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        block.add(sectionLabel, gbc);

        // Combo box — fills full width via GridBagLayout
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 5, 0);
        block.add(questionBox, gbc);

        // Answer field — fixed height, no scrollbar
        answerField.setLineWrap(true);
        answerField.setWrapStyleWord(true);
        answerField.setMinimumSize(new Dimension(0, 70));
        answerField.setPreferredSize(new Dimension(0, 70));
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        block.add(answerField, gbc);

        return block;
    }

    // ─── Submit handler ──────────────────────────────────────────────────────
    private void handleSubmit() {
        String q1 = (String) questionBox1.getSelectedItem();
        String q2 = (String) questionBox2.getSelectedItem();
        String q3 = (String) questionBox3.getSelectedItem();
        String a1 = answerField1.getText().trim();
        String a2 = answerField2.getText().trim();
        String a3 = answerField3.getText().trim();

        if (!validateInputs(q1, q2, q3, a1, a2, a3)) return;

        if (!saveToDatabase(q1, a1, q2, a2, q3, a3)) return;

        submitted = true;
        JOptionPane.showMessageDialog(this, "Security questions saved successfully!");
        dispose();
    }

    // ─── Input validation ────────────────────────────────────────────────────
    private boolean validateInputs(String q1, String q2, String q3,
                                   String a1, String a2, String a3) {
        if (questionBox1.getSelectedIndex() == 0
                || questionBox2.getSelectedIndex() == 0
                || questionBox3.getSelectedIndex() == 0) {
            showError("Please select all three security questions.");
            return false;
        }

        if (a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
            showError("Please answer all three security questions.");
            return false;
        }

        if (a1.length() < 3 || a2.length() < 3 || a3.length() < 3) {
            showError("Each answer must be at least 3 characters.");
            return false;
        }

        if (q1.equals(q2) || q1.equals(q3) || q2.equals(q3)) {
            showError("Please choose three different security questions.");
            return false;
        }

        return true;
    }

    // ─── Database save (insert or update) ───────────────────────────────────
    private boolean saveToDatabase(String q1, String a1,
                                   String q2, String a2,
                                   String q3, String a3) {
        String checkSql  = "SELECT 1 FROM SECURITY WHERE user_id = ? LIMIT 1";
        String insertSql = "INSERT INTO SECURITY "
                + "(user_id, security_question_1, security_answer_1, "
                + " security_question_2, security_answer_2, "
                + " security_question_3, security_answer_3) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE SECURITY SET "
                + "security_question_1 = ?, security_answer_1 = ?, "
                + "security_question_2 = ?, security_answer_2 = ?, "
                + "security_question_3 = ?, security_answer_3 = ? "
                + "WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            boolean exists = recordExists(conn, checkSql);

            if (exists) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, q1); ps.setString(2, a1);
                    ps.setString(3, q2); ps.setString(4, a2);
                    ps.setString(5, q3); ps.setString(6, a3);
                    ps.setInt(7, userId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, q1); ps.setString(3, a1);
                    ps.setString(4, q2); ps.setString(5, a2);
                    ps.setString(6, q3); ps.setString(7, a3);
                    ps.executeUpdate();
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error — could not save security questions.");
            return false;
        }
    }

    private boolean recordExists(Connection conn, String checkSql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private static CustomTextArea createAnswerField() {
        CustomTextArea field = new CustomTextArea();
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        return field;
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    // ─── Public accessor ─────────────────────────────────────────────────────
    public boolean isSubmitted() {
        return submitted;
    }
}