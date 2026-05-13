package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import utils.*;

public class CustomTextArea extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTextArea textArea;
	private JScrollPane scrollPane;

	private int radius;
	private boolean isHovered = false;
	private String placeholder = "";
	private Dimension customSize = null;

	/**
	 * When true, this component behaves like a multiline label: non-editable,
	 * non-focusable, non-selectable, and transparent to mouse/keyboard.
	 */
	private boolean displayOnly = false;

	public CustomTextArea() {
		this("", 10, 20, 10);
	}

	/**
	 * Creates a multiline, display-only text component (like a label that wraps
	 * lines).
	 */
	public static CustomTextArea createDisplayOnly() {
		CustomTextArea ta = new CustomTextArea("", 2, 20, 10);
		ta.setDisplayOnly(true);
		return ta;
	}

	public CustomTextArea(int rows, int columns) {
		this("", rows, columns, 10);
	}

	public CustomTextArea(int rows, int columns, int radius) {
		this("", rows, columns, radius);
	}

	public CustomTextArea(String placeholder) {
		this(placeholder, 10, 20, 10);
	}

	public CustomTextArea(String placeholder, int rows, int columns) {
		this(placeholder, rows, columns, 10);
	}

	public CustomTextArea(String placeholder, int rows, int columns, int radius) {
		super(new BorderLayout());
		this.placeholder = placeholder != null ? placeholder : "";
		this.radius = radius;

		setOpaque(false);
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

		textArea = new JTextArea(rows, columns) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (getText().isEmpty() && !CustomTextArea.this.placeholder.isEmpty()) {
					Graphics2D g2p = (Graphics2D) g.create();
					g2p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					g2p.setColor(Color.GRAY);
					g2p.setFont(getFont());

					FontMetrics fm = g2p.getFontMetrics();
					int x = getInsets().left;
					int y = getInsets().top + fm.getAscent();

					g2p.drawString(CustomTextArea.this.placeholder, x, y);
					g2p.dispose();
				}
			}
		};

		textArea.setOpaque(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder());
		// default behavior: editable textbox
		textArea.setEditable(true);
		textArea.setHighlighter(null);

		if (FontLib.POPPINS_REGULAR != null) {
			textArea.setFont(FontLib.POPPINS_REGULAR.deriveFont(14f));
		}

		scrollPane = new JScrollPane(textArea);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		// Custom scrollbar UI (optional, makes it cleaner)
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scrollPane.getVerticalScrollBar().setOpaque(false);

		add(scrollPane, BorderLayout.CENTER);

		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				isHovered = true;
				CustomTextArea.this.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				isHovered = false;
				CustomTextArea.this.repaint();
			}
		});

		textArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				CustomTextArea.this.repaint();
			}

			@Override
			public void focusLost(FocusEvent e) {
				CustomTextArea.this.repaint();
			}
		});

		applyDisplayOnlyState();
	}

	private void applyDisplayOnlyState() {
		if (textArea == null || scrollPane == null) {
			return;
		}
		if (displayOnly) {
			// Behave like a label
			textArea.setEditable(false);
			textArea.setFocusable(false);
			textArea.setCursor(Cursor.getDefaultCursor());
			textArea.setHighlighter(null);
			textArea.setSelectionColor(new Color(0, 0, 0, 0));
			textArea.setSelectedTextColor(textArea.getForeground());
			textArea.setComponentPopupMenu(null);
			// make it ignore mouse events so user can't place caret or select
			textArea.setEnabled(false);
			textArea.setDisabledTextColor(textArea.getForeground());
			// scroll should still work if content overflows
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		} else {
			textArea.setEnabled(true);
			textArea.setEditable(true);
			textArea.setFocusable(true);
			textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}
		repaint();
		revalidate();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(getBackground());
		g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
		g2.dispose();

		super.paintComponent(g);
	}

	@Override
	protected void paintBorder(Graphics g) {
		if (displayOnly) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(textArea.hasFocus() || isHovered ? Color.BLACK : Color.LIGHT_GRAY);
		g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));

		g2.dispose();
	}

	public String getText() {
		return textArea.getText();
	}

	public void setText(String text) {
		textArea.setText(text);
		// For display-only usage: keep caret at start so it doesn't jump if focus is
		// ever gained.
		try {
			textArea.setCaretPosition(0);
		} catch (Exception ignored) {
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (textArea != null) {
			textArea.setForeground(fg);
		}
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (textArea != null) {
			textArea.setBackground(bg);
		}
	}

	public void setEditable(boolean editable) {
		if (textArea != null) {
			textArea.setEditable(editable);
			textArea.setFocusable(editable);
		}
	}

	public void setDisplayOnly(boolean displayOnly) {
		this.displayOnly = displayOnly;
		applyDisplayOnlyState();
	}

	public boolean isDisplayOnly() {
		return displayOnly;
	}

	public void setFontSize(float size) {
		if (FontLib.POPPINS_REGULAR != null) {
			textArea.setFont(FontLib.POPPINS_REGULAR.deriveFont(size));
		} else {
			textArea.setFont(textArea.getFont().deriveFont(size));
		}
	}

	public void setCustomSize(int width, int height) {
		this.customSize = new Dimension(width, height);
		setPreferredSize(customSize);
		setMinimumSize(customSize);
		setMaximumSize(customSize);
		revalidate();
	}

	@Override
	public Dimension getPreferredSize() {
		if (customSize != null) {
			return customSize;
		}
		return super.getPreferredSize();
	}
}