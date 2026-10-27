package com.seminarhall.ui.organizer;

import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * Look of the Faculty Console screens (web App.jsx: OrganizerDashboardPage, CreateEventPage,
 * ManageEventsPage): their card, input and button colors, plus the small layouts they need.
 */
final class ConsoleUi {

    // bg-[#10131d] border-[#1d2232] cards
    static final Color CARD_BG = new Color(16, 19, 29);
    static final Color CARD_BORDER = new Color(29, 34, 50);
    static final Color ROW_DIVIDER = new Color(24, 28, 42);      // divide-[#181c2a]
    static final Color INPUT_BG = new Color(18, 21, 34);         // bg-[#121522]
    static final Color INPUT_BORDER = new Color(33, 38, 54);     // border-[#212636]
    static final Color BUTTON_BG = new Color(20, 23, 36);        // bg-[#141724]
    static final Color BUTTON_BORDER = new Color(35, 39, 58);    // border-[#23273a]
    static final Color BUTTON_HOVER = new Color(26, 30, 47);     // hover:bg-[#1a1e2f]

    static final Color BLUE_500 = new Color(59, 130, 246);
    static final Color EMERALD_500 = new Color(16, 185, 129);
    static final Color AMBER_500 = new Color(245, 158, 11);
    static final Color PURPLE_500 = new Color(168, 85, 247);
    static final Color SKY_500 = new Color(14, 165, 233);

    private static final String MONO_FAMILY = pickMonoFamily();

    private ConsoleUi() {
    }

    static Font font(Weight weight, float size) {
        return ThemeManager.font(weight, size);
    }

    /** font-mono: Chrome on Windows renders it with Consolas. */
    static Font mono(boolean bold, float size) {
        return new Font(MONO_FAMILY, bold ? Font.BOLD : Font.PLAIN, 1).deriveFont(size);
    }

    private static String pickMonoFamily() {
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String wanted : new String[]{"Consolas", "Menlo", "DejaVu Sans Mono"}) {
            if (Arrays.asList(families).contains(wanted)) {
                return wanted;
            }
        }
        return Font.MONOSPACED;
    }

    /** A label whose box is exactly one CSS line tall (text vertically centred, like half-leading). */
    static JLabel text(String text, Font font, Color color, int lineHeight) {
        JLabel label = new JLabel(text) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, lineHeight);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /** "glow-primary text-sm font-bold text-white px-N py-2.5 rounded-xl shadow-lg". */
    static StyledButton primaryButton(String text, int padX) {
        StyledButton button = new StyledButton(text, StyledButton.ButtonStyle.PRIMARY);
        button.setFont(font(Weight.BOLD, 14));
        button.setMargin(new Insets(10, padX, 10, padX));
        button.setGlow(true);
        return button;
    }

    /** Wraps a component so a layout that stretches children leaves it at its preferred size (left aligned). */
    static JPanel leftAligned(JComponent component) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(component);
        return wrap;
    }

    /** Wraps a component so it keeps its preferred size, vertically centred (flex items-center). */
    static JPanel centeredVertically(JComponent component) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(component);
        return wrap;
    }

    static JScrollPane scrollPage(JComponent content, int maxWidth, int padX, int padY) {
        ScrollPage page = new ScrollPage(new Centered(maxWidth, padX, padY));
        page.add(content);
        JScrollPane scroll = new JScrollPane(page,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ------------------------------------------------------------------ components

    /** "bg-[#10131d] border border-[#1d2232] rounded-2xl / rounded-3xl" card. Set an EmptyBorder for padding. */
    static class Card extends JPanel {
        private final int radius;

        Card(int radius, LayoutManager layout) {
            super(layout);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = radius * 2;
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
        }
    }

    /** "bg-[#141724] border border-[#23273a] hover:bg-[#1a1e2f] text-gray-300 text-xs font-semibold py-2.5 rounded-xl". */
    static class DarkButton extends JButton {
        private boolean hovered;

        DarkButton(String text, int padX) {
            super(text);
            setFont(font(Weight.SEMIBOLD, 12));
            setForeground(ThemeManager.TEXT_GRAY_300);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(11, padX + 1, 11, padX + 1));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            Insets in = getInsets();
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + in.left + in.right, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(hovered && isEnabled() ? BUTTON_HOVER : BUTTON_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 24, 24);
            g2.setColor(BUTTON_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(isEnabled() ? getForeground() : ThemeManager.TEXT_MUTED);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Text button: "text-xs text-[#ff382e] hover:underline font-semibold". */
    static class LinkButton extends JButton {
        private boolean hovered;

        LinkButton(String text) {
            super(text);
            setFont(font(Weight.SEMIBOLD, 12));
            setForeground(ThemeManager.BRAND_RED);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()), 16);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int baseline = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(getForeground());
            g2.drawString(getText(), 0, baseline);
            if (hovered) {
                g2.fillRect(0, baseline + 2, fm.stringWidth(getText()), 1);
            }
            g2.dispose();
        }
    }

    /**
     * Form input: "w-full bg-[#121522] border border-[#212636] rounded-xl px-4 py-2.5 text-xs text-white
     * outline-none focus:border-[#ff382e]", 38px tall, with an optional icon on the right.
     */
    static class FormField extends JTextField {
        private final String placeholder;
        private Icon trailingIcon;
        private boolean focused;

        FormField(String placeholder) {
            this.placeholder = placeholder;
            setFont(font(Weight.REGULAR, 12));
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setSelectionColor(new Color(255, 56, 46, 90));
            setSelectedTextColor(Color.WHITE);
            setDisabledTextColor(ThemeManager.TEXT_MUTED);
            setOpaque(false);
            updatePadding();
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
            });
        }

        void setTrailingIcon(Icon icon) {
            this.trailingIcon = icon;
            updatePadding();
        }

        private void updatePadding() {
            int right = trailingIcon != null ? 17 + trailingIcon.getIconWidth() + 8 : 17;
            setBorder(new EmptyBorder(11, 17, 11, right));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            paintFieldBox(g2, getWidth(), getHeight(), focused && isEnabled(), isEnabled());
            if (trailingIcon != null) {
                trailingIcon.paintIcon(this, g2, getWidth() - 17 - trailingIcon.getIconWidth(),
                        (getHeight() - trailingIcon.getIconHeight()) / 2);
            }
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null) {
                g2.setFont(getFont());
                g2.setColor(ThemeManager.TEXT_SECONDARY);   // preflight: placeholder gray-400
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    /** Multi-line input in the same style as {@link FormField} (fixed number of rows, scrolls inside). */
    static class FormArea extends JPanel {
        private final JTextArea area;
        private boolean focused;

        FormArea(String placeholder, int rows) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(10, 17, 10, 6));
            area = new JTextArea(rows, 20) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getText().isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setFont(getFont());
                        g2.setColor(ThemeManager.TEXT_SECONDARY);
                        g2.drawString(placeholder, getInsets().left, getInsets().top + g2.getFontMetrics().getAscent());
                        g2.dispose();
                    }
                }
            };
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setOpaque(false);
            area.setFont(font(Weight.REGULAR, 12));
            area.setForeground(Color.WHITE);
            area.setCaretColor(Color.WHITE);
            area.setSelectionColor(new Color(255, 56, 46, 90));
            area.setBorder(new EmptyBorder(0, 0, 0, 10));
            area.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
            });
            JScrollPane scroll = new JScrollPane(area,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            add(scroll, BorderLayout.CENTER);
            int lineHeight = 16;
            setPreferredSize(new Dimension(100, rows * lineHeight + 22));
        }

        String getText() {
            return area.getText();
        }

        void setText(String text) {
            area.setText(text);
            area.setCaretPosition(0);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintFieldBox(g2, getWidth(), getHeight(), focused, true);
            g2.dispose();
        }
    }

    private static void paintFieldBox(Graphics2D g2, int w, int h, boolean focused, boolean enabled) {
        g2.setColor(enabled ? INPUT_BG : new Color(14, 16, 26));
        g2.fillRoundRect(0, 0, w - 1, h - 1, 24, 24);
        g2.setColor(focused ? ThemeManager.BRAND_RED : INPUT_BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);
    }

    /** Native-looking select in the input style (FlatLaf per-component styling). */
    static <T> JComboBox<T> select() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(font(Weight.REGULAR, 12));
        combo.setForeground(Color.WHITE);
        combo.setBackground(INPUT_BG);
        combo.putClientProperty("FlatLaf.style", "arc: 24; borderWidth: 1; focusWidth: 0; innerFocusWidth: 0;"
                + " borderColor: #212636; focusedBorderColor: #ff382e; disabledBorderColor: #1d2232;"
                + " focusedBackground: #121522; disabledBackground: #0e101a;"
                + " buttonBackground: #121522; buttonEditableBackground: #121522; buttonFocusedBackground: #121522;"
                + " buttonSeparatorWidth: 0; buttonArrowColor: #9ca3af; buttonHoverArrowColor: #ffffff;"
                + " buttonDisabledArrowColor: #4b5563; disabledForeground: #6b7280; padding: 9,16,9,8;"
                + " popupBackground: #121522");
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return combo;
    }

    /** Field label: "text-xs text-gray-300 font-semibold block mb-1.5" above its input. */
    static JPanel labeled(String label, JComponent field) {
        JPanel group = new JPanel(new Stack(0));
        group.setOpaque(false);
        group.add(text(label, font(Weight.SEMIBOLD, 12), ThemeManager.TEXT_GRAY_300, 16));
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        return group;
    }

    /** Inline status message (tinted like the web's status pills): emerald for success, red for errors. */
    static class Banner extends JLabel {
        private Color tint = ThemeManager.EMERALD_400;

        Banner() {
            setFont(font(Weight.SEMIBOLD, 12));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setIconTextGap(8);
            setVisible(false);
        }

        void success(String message) {
            display(ThemeManager.EMERALD_400, Icons.get("circle-check", 14, ThemeManager.EMERALD_400), message);
        }

        void error(String message) {
            display(ThemeManager.RED_400, Icons.get("x", 14, ThemeManager.RED_400), message);
        }

        void clear() {
            setText("");
            setVisible(false);
        }

        private void display(Color color, Icon icon, String message) {
            tint = color;
            setForeground(color);
            setIcon(icon);
            setText(message);
            setVisible(true);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 26));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 70));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * "flex items-center justify-between" row with a glow-primary button on the right. The button's
     * visible box is aligned and centred like the web's; its glow spills outside the row like a CSS
     * box-shadow (the row grows its bounds by the glow size, so its parent needs that much padding).
     * With {@code hugButton} the left component sits just before the button ("justify-end gap-N").
     */
    static final class GlowRow extends JPanel {
        static final int GLOW_X = 8;       // StyledButton glow margins
        static final int GLOW_TOP = 2;
        static final int GLOW_BOTTOM = 12;

        private final JComponent left;
        private final JComponent button;
        private final boolean hugButton;
        private final int gap;
        private int padTop;
        private boolean topRule;

        GlowRow(JComponent left, JComponent button, boolean hugButton, int gap) {
            super(null);
            this.left = left;
            this.button = button;
            this.hugButton = hugButton;
            this.gap = gap;
            setOpaque(false);
            if (left != null) add(left);
            add(button);
        }

        /** "pt-N border-t border-[#1d2232]". */
        void setTopRule(int paddingTop) {
            this.topRule = true;
            this.padTop = paddingTop;
        }

        private int visibleHeight() {
            return button.getPreferredSize().height - GLOW_TOP - GLOW_BOTTOM;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension l = left != null ? left.getPreferredSize() : new Dimension();
            Dimension b = button.getPreferredSize();
            return new Dimension(l.width + gap + b.width - GLOW_X * 2, padTop + Math.max(l.height, visibleHeight()));
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int w, int h) {
            // the parent lays out the visible row; the component itself also covers the glow
            super.reshape(x, y, w + GLOW_X, h + GLOW_BOTTOM);
        }

        @Override
        public void doLayout() {
            int w = getWidth() - GLOW_X;
            int h = getHeight() - GLOW_BOTTOM - padTop;
            Dimension b = button.getPreferredSize();
            int bx = w - b.width + GLOW_X;
            int by = padTop + (h - visibleHeight()) / 2 - GLOW_TOP;
            button.setBounds(bx, by, b.width, b.height);
            if (left != null) {
                Dimension l = left.getPreferredSize();
                int lx = hugButton ? bx + GLOW_X - gap - l.width : 0;
                int lw = hugButton ? l.width : Math.max(0, bx + GLOW_X - gap);
                left.setBounds(lx, padTop + (h - l.height) / 2, lw, l.height);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (topRule) {
                g.setColor(CARD_BORDER);
                g.fillRect(0, 0, getWidth() - GLOW_X, 1);
            }
        }
    }

    // ------------------------------------------------------------------ layouts

    /** Children top to bottom, each at full width and its preferred height (a CSS block flow). */
    static final class Stack implements LayoutManager {
        private final int gap;

        Stack(int gap) {
            this.gap = gap;
        }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int w = 0;
            int h = 0;
            int count = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = c.getPreferredSize();
                w = Math.max(w, d.width);
                h += d.height;
                count++;
            }
            h += Math.max(0, count - 1) * gap;
            return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int x = in.left;
            int w = parent.getWidth() - in.left - in.right;
            int y = in.top;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) continue;
                int h = c.getPreferredSize().height;
                c.setBounds(x, y, w, h);
                y += h + gap;
            }
        }
    }

    /** Children side by side, widths shared by weight (CSS grid fr units), all as tall as the tallest. */
    static final class Columns implements LayoutManager {
        private final int gap;
        private final double[] weights;

        Columns(int gap, double... weights) {
            this.gap = gap;
            this.weights = weights;
        }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int w = 0;
            int h = 0;
            Component[] cs = parent.getComponents();
            for (Component c : cs) {
                Dimension d = c.getPreferredSize();
                w += d.width;
                h = Math.max(h, d.height);
            }
            w += Math.max(0, cs.length - 1) * gap;
            return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            Component[] cs = parent.getComponents();
            int n = cs.length;
            if (n == 0) return;
            double total = 0;
            for (int i = 0; i < n; i++) total += weight(i);
            int available = parent.getWidth() - in.left - in.right - gap * (n - 1);
            int h = parent.getHeight() - in.top - in.bottom;
            double x = in.left;
            for (int i = 0; i < n; i++) {
                double w = available * weight(i) / total;
                cs[i].setBounds((int) Math.round(x), in.top, (int) Math.round(x + w) - (int) Math.round(x), h);
                x += w + gap;
            }
        }

        private double weight(int i) {
            return i < weights.length ? weights[i] : 1.0;
        }
    }

    /** One child, centred with a max width and padding: "max-w-* mx-auto px-8 py-8 w-full". */
    static final class Centered implements LayoutManager {
        private final int maxWidth;
        private final int padX;
        private final int padY;

        Centered(int maxWidth, int padX, int padY) {
            this.maxWidth = maxWidth;
            this.padX = padX;
            this.padY = padY;
        }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            if (parent.getComponentCount() == 0) return new Dimension(0, 0);
            Dimension d = parent.getComponent(0).getPreferredSize();
            return new Dimension(Math.min(d.width + padX * 2, maxWidth), d.height + padY * 2);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, preferredLayoutSize(parent).height);
        }

        @Override
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() == 0) return;
            Component child = parent.getComponent(0);
            int box = Math.min(parent.getWidth(), maxWidth);
            int x = (parent.getWidth() - box) / 2 + padX;
            child.setBounds(x, padY, box - padX * 2, child.getPreferredSize().height);
        }
    }

    /** Page inside a vertical scroll pane: always as wide as the viewport, never scrolls sideways. */
    static final class ScrollPage extends JPanel implements Scrollable {
        ScrollPage(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(16, r.height - 48); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
        }
    }
}
