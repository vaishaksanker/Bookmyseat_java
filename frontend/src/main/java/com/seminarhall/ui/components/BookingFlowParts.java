package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Building blocks of the booking flow screens (seat selection, booking summary, payment),
 * translated from the web app's Tailwind classes in App.jsx. Only those screens use it.
 */
public final class BookingFlowParts {

    // bg-[#10131d] border-[#1d2232]: the flow's cards
    public static final Color CARD_BG = new Color(16, 19, 29);
    public static final Color CARD_BORDER = new Color(29, 34, 50);
    // seat chips: bg-[#1a1e2d] border-[#2a3046]
    public static final Color CHIP_BG = new Color(26, 30, 45);
    public static final Color CHIP_BORDER = new Color(42, 48, 70);
    /** border-white/5 */
    public static final Color DIVIDER = new Color(255, 255, 255, 13);
    public static final Color GRAY_600 = new Color(75, 85, 99);
    public static final Color GRAY_800 = new Color(31, 41, 55);
    public static final Color EMERALD_200 = new Color(167, 243, 208);
    public static final Color EMERALD_500 = new Color(16, 185, 129);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private BookingFlowParts() {
    }

    // ------------------------------------------------------------------ formatting

    /** "Dec 10, 2024", as the web shows event dates. */
    public static String date(Event event) {
        return event.getDate() != null ? event.getDate().format(DATE) : "";
    }

    /** "10:00 AM - 12:00 PM". */
    public static String timeRange(Event event) {
        if (event.getStartTime() == null) return "";
        String start = event.getStartTime().format(TIME);
        return event.getEndTime() != null ? start + " - " + event.getEndTime().format(TIME) : start;
    }

    /** "₹150": the web's {`₹${total}`}. */
    public static String rupees(double amount) {
        return "₹" + String.format("%.0f", amount);
    }

    public static Font font(Weight weight, float px) {
        return ThemeManager.font(weight, px);
    }

    /** The font with CSS letter-spacing in em (tracking-wider = 0.05, tracking-[0.3em] = 0.3). */
    public static Font tracked(Font font, float em) {
        return font.deriveFont(Map.of(TextAttribute.TRACKING, em));
    }

    static void hints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ------------------------------------------------------------------ text

    /** A one-line label with a CSS line-height (its preferred height), optionally capped in width (truncate). */
    public static class Text extends JLabel {
        private final int lineHeight;
        private int maxWidth = Integer.MAX_VALUE;

        public Text(String text, Weight weight, float size, Color color, int lineHeight) {
            this(text, font(weight, size), color, lineHeight);
        }

        public Text(String text, Font font, Color color, int lineHeight) {
            super(text);
            this.lineHeight = lineHeight;
            setFont(font);
            setForeground(color);
        }

        public Text maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.min(d.width, maxWidth), lineHeight);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, lineHeight);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, lineHeight);
        }
    }

    /** Text that wraps at word boundaries within a fixed width (a heading or a value that may run long). */
    public static class WrapText extends JComponent {
        private final Font font;
        private final Color color;
        private final int lineHeight;
        private final int wrapWidth;
        private List<String> lines = new ArrayList<>();
        private int textWidth;

        public WrapText(Weight weight, float size, Color color, int lineHeight, int wrapWidth) {
            this.font = font(weight, size);
            this.color = color;
            this.lineHeight = lineHeight;
            this.wrapWidth = wrapWidth;
            setOpaque(false);
        }

        public void setText(String text) {
            FontMetrics fm = getFontMetrics(font);
            lines = new ArrayList<>();
            textWidth = 0;
            StringBuilder line = new StringBuilder();
            for (String word : (text == null ? "" : text.trim()).split("\\s+")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (line.length() > 0 && fm.stringWidth(candidate) > wrapWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            lines.add(line.toString());
            for (String l : lines) {
                textWidth = Math.max(textWidth, Math.min(wrapWidth, fm.stringWidth(l)));
            }
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(textWidth, Math.max(1, lines.size()) * lineHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            g2.setFont(font);
            g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics();
            int baseline = (lineHeight - (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();
            for (int i = 0; i < lines.size(); i++) {
                g2.drawString(lines.get(i), 0, i * lineHeight + baseline);
            }
            g2.dispose();
        }
    }

    /** "← Back to ..." links: text-xs font-semibold text-gray-400 hover:text-white. */
    public static Text backLink(String text, Runnable action) {
        Text link = new Text(text, Weight.SEMIBOLD, 12, ThemeManager.TEXT_SECONDARY, 16);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                link.setForeground(ThemeManager.TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                link.setForeground(ThemeManager.TEXT_SECONDARY);
            }
        });
        return link;
    }

    /** Page headings: text-2xl font-black text-white. */
    public static Text pageTitle(String text) {
        return new Text(text, Weight.EXTRABOLD, 24, ThemeManager.TEXT_PRIMARY, 32);
    }

    /** A small Lucide icon followed by text, replacing the web's inline emoji (📅 📍). */
    public static Text iconLine(String icon, Color iconColor, String text, float size, Color color, int lineHeight) {
        Text t = new Text(text, Weight.REGULAR, size, color, lineHeight);
        t.setIcon(Icons.get(icon, Math.round(size) + 1, iconColor));
        t.setIconTextGap(5);
        return t;
    }

    // ------------------------------------------------------------------ surfaces

    /** A rounded card (fill + 1px border), e.g. bg-[#10131d] border border-[#1d2232] rounded-3xl. */
    public static class Card extends JPanel {
        private final int radius;
        private Color fill;
        private Color border;
        private boolean dashed;

        public Card(int radius, int padding) {
            this(radius, padding, padding, padding, padding);
        }

        public Card(int radius, int top, int left, int bottom, int right) {
            super(new BorderLayout());
            this.radius = radius;
            this.fill = CARD_BG;
            this.border = CARD_BORDER;
            setOpaque(false);
            // the 1px border sits inside the box, as with box-sizing: border-box
            setBorder(new EmptyBorder(top + 1, left + 1, bottom + 1, right + 1));
        }

        public Card colors(Color fill, Color border) {
            this.fill = fill;
            this.border = border;
            repaint();
            return this;
        }

        public Card dashed(boolean dashed) {
            this.dashed = dashed;
            return this;
        }

        public int radius() {
            return radius;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            float w = getWidth();
            float h = getHeight();
            if (fill != null) {
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius * 2f, radius * 2f));
            }
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(dashed
                        ? new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 3f}, 0f)
                        : new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, radius * 2f, radius * 2f));
            }
            g2.dispose();
        }
    }

    /** A rounded chip around a row of components (seat chips "B7 ✕"). */
    public static class Pill extends JPanel {
        private final int radius;
        private final Color fill;
        private final Color border;

        public Pill(int radius, int padY, int padX, Color fill, Color border) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            this.radius = radius;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
            setBorder(new EmptyBorder(padY + 1, padX + 1, padY + 1, padX + 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            float w = getWidth();
            float h = getHeight();
            // shadow: a faint dark edge under the chip
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fill(new RoundRectangle2D.Float(0, 1, w, h, radius * 2f, radius * 2f));
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h - 1, radius * 2f, radius * 2f));
            g2.setColor(border);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 2, radius * 2f, radius * 2f));
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ buttons

    /**
     * The glow-primary button (gradient + white top highlight) with the Lucide chevron after the text.
     * Its red glow (box-shadow 0 8px 24px -4px rgba(255,56,46,.55), on hover 0 12px 30px -4px .75)
     * is painted by the {@link Page}, because it reaches outside the containers the button sits in.
     * Disabled it looks like the web's "bg-gray-800 text-gray-500 cursor-not-allowed".
     */
    public static class FlowButton extends StyledButton {
        private final int padX;
        private final int boxHeight;
        private final Icon chevron;
        private final Icon chevronDisabled;
        private final int radius;
        private boolean hover;

        /**
         * @param fontSize   text-sm = 14, text-xs = 12
         * @param padY       py-4 = 16, py-3.5 = 14
         * @param padX       px-8 = 32 (ignored when the button fills its column)
         * @param radius     rounded-2xl = 16, rounded-xl = 12
         * @param chevron    ChevronRight size (15 or 16), 0 for none
         */
        public FlowButton(String text, float fontSize, int padY, int padX, int radius, int chevron) {
            super(text, ButtonStyle.PRIMARY);
            this.padX = padX;
            this.radius = radius;
            this.boxHeight = padY * 2 + (fontSize <= 12 ? 16 : 20);
            setFont(font(Weight.BOLD, fontSize));
            setCornerRadius(radius);
            setHorizontalTextPosition(SwingConstants.LEFT);
            setIconTextGap(8);
            this.chevron = chevron > 0 ? Icons.get("chevron-right", chevron, Color.WHITE) : null;
            this.chevronDisabled = chevron > 0 ? Icons.get("chevron-right", chevron, ThemeManager.TEXT_MUTED) : null;
            setIcon(this.chevron);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setHover(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setHover(false);
                }
            });
        }

        private void setHover(boolean hover) {
            this.hover = hover;
            Page page = (Page) SwingUtilities.getAncestorOfClass(Page.class, this);
            if (page != null) page.repaint();
        }

        public int radius() {
            return radius;
        }

        boolean isHover() {
            return hover && isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (chevron != null) {
                setIcon(enabled ? chevron : chevronDisabled);
            }
            Page page = (Page) SwingUtilities.getAncestorOfClass(Page.class, this);
            if (page != null) page.repaint();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            Icon icon = getIcon();
            int w = padX * 2 + fm.stringWidth(getText() == null ? "" : getText())
                    + (icon != null ? icon.getIconWidth() + getIconTextGap() : 0);
            return new Dimension(w, boxHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isEnabled()) {
                super.paintComponent(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            g2.setColor(GRAY_800);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius * 2f, radius * 2f));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText() == null ? "" : getText();
            Icon icon = getIcon();
            int iconW = icon != null ? icon.getIconWidth() + getIconTextGap() : 0;
            int x = (getWidth() - fm.stringWidth(text) - iconW) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.drawString(text, x, y);
            if (icon != null) {
                icon.paintIcon(this, g2, x + fm.stringWidth(text) + getIconTextGap(), (getHeight() - icon.getIconHeight()) / 2);
            }
            g2.dispose();
        }
    }

    /** "← Modify Seats": bg-[#141724] border-[#23273a] hover:bg-[#1a1e2f] text-gray-300 text-xs font-semibold. */
    public static class DarkButton extends JButton {
        private boolean hover;

        public DarkButton(String text) {
            super(text);
            setFont(font(Weight.SEMIBOLD, 12));
            setForeground(ThemeManager.TEXT_GRAY_300);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 40 + 2, 12 + 16 + 12 + 2);   // px-5 py-3 + border
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            g2.setColor(hover ? new Color(26, 30, 47) : new Color(20, 23, 36));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
            g2.setColor(new Color(35, 39, 58));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ layout

    static int flowHeight(Component c) {
        return c.getPreferredSize().height;
    }

    static int flowWidth(Component c) {
        return c.getPreferredSize().width;
    }

    static void place(Component c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
    }

    /** Vertical stack (a flex-col / space-y-*): each child gets a gap before it and an alignment. */
    public static class Stack extends JPanel {
        public enum Align { FILL, LEFT, CENTER, RIGHT }

        private final Map<Component, Object[]> spec = new HashMap<>();

        public Stack() {
            super(null);
            setOpaque(false);
            setLayout(new StackLayout());
        }

        public Stack row(Component c, int gapBefore) {
            return row(c, gapBefore, Align.FILL);
        }

        public Stack row(Component c, int gapBefore, Align align) {
            spec.put(c, new Object[]{gapBefore, align});
            add(c);
            return this;
        }

        private class StackLayout implements LayoutManager {
            @Override public void addLayoutComponent(String name, Component comp) { }

            @Override public void removeLayoutComponent(Component comp) {
                spec.remove(comp);
            }

            @Override
            public Dimension preferredLayoutSize(Container parent) {
                Insets in = parent.getInsets();
                int w = 0;
                int h = 0;
                for (Component c : parent.getComponents()) {
                    if (!c.isVisible()) continue;
                    Object[] s = spec.getOrDefault(c, new Object[]{0, Align.FILL});
                    h += (Integer) s[0] + flowHeight(c);
                    w = Math.max(w, flowWidth(c));
                }
                return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
            }

            @Override
            public Dimension minimumLayoutSize(Container parent) {
                return preferredLayoutSize(parent);
            }

            @Override
            public void layoutContainer(Container parent) {
                Insets in = parent.getInsets();
                int width = parent.getWidth() - in.left - in.right;
                int y = in.top;
                for (Component c : parent.getComponents()) {
                    if (!c.isVisible()) continue;
                    Object[] s = spec.getOrDefault(c, new Object[]{0, Align.FILL});
                    y += (Integer) s[0];
                    int h = flowHeight(c);
                    Align align = (Align) s[1];
                    int w = align == Align.FILL ? width : Math.min(width, flowWidth(c));
                    int x = in.left;
                    if (align == Align.CENTER) x += (width - w) / 2;
                    if (align == Align.RIGHT) x += width - w;
                    place(c, x, y, w, h);
                    y += h;
                }
            }
        }
    }

    /**
     * Horizontal row (a flex row / grid row): children keep their width unless they have a weight,
     * in which case they share the remaining width (flex-1, grid fractions).
     */
    public static class Row extends JPanel {
        public enum VAlign { TOP, CENTER, STRETCH }

        private final VAlign valign;
        private final Map<Component, float[]> spec = new LinkedHashMap<>();

        public Row(VAlign valign) {
            super(null);
            this.valign = valign;
            setOpaque(false);
            setLayout(new RowLayout());
        }

        /** A child at its preferred width. */
        public Row cell(Component c, int gapBefore) {
            return add(c, gapBefore, 0, -1);
        }

        /** A child at a fixed width. */
        public Row fixed(Component c, int gapBefore, int width) {
            return add(c, gapBefore, 0, width);
        }

        /** A child sharing the leftover width by weight. */
        public Row grow(Component c, int gapBefore, float weight) {
            return add(c, gapBefore, weight, -1);
        }

        private Row add(Component c, int gap, float weight, int width) {
            spec.put(c, new float[]{gap, weight, width});
            super.add(c);
            return this;
        }

        private int naturalWidth(Component c, float[] s) {
            return s[2] >= 0 ? (int) s[2] : flowWidth(c);
        }

        private class RowLayout implements LayoutManager {
            @Override public void addLayoutComponent(String name, Component comp) { }

            @Override public void removeLayoutComponent(Component comp) {
                spec.remove(comp);
            }

            @Override
            public Dimension preferredLayoutSize(Container parent) {
                Insets in = parent.getInsets();
                int w = 0;
                int h = 0;
                for (Component c : parent.getComponents()) {
                    if (!c.isVisible()) continue;
                    float[] s = spec.getOrDefault(c, new float[]{0, 0, -1});
                    w += (int) s[0] + naturalWidth(c, s);
                    h = Math.max(h, flowHeight(c));
                }
                return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
            }

            @Override
            public Dimension minimumLayoutSize(Container parent) {
                return preferredLayoutSize(parent);
            }

            @Override
            public void layoutContainer(Container parent) {
                Insets in = parent.getInsets();
                int width = parent.getWidth() - in.left - in.right;
                int height = parent.getHeight() - in.top - in.bottom;
                int used = 0;
                float weights = 0;
                for (Component c : parent.getComponents()) {
                    if (!c.isVisible()) continue;
                    float[] s = spec.getOrDefault(c, new float[]{0, 0, -1});
                    used += (int) s[0];
                    if (s[1] > 0) weights += s[1];
                    else used += naturalWidth(c, s);
                }
                int free = Math.max(0, width - used);
                int x = in.left;
                for (Component c : parent.getComponents()) {
                    if (!c.isVisible()) continue;
                    float[] s = spec.getOrDefault(c, new float[]{0, 0, -1});
                    x += (int) s[0];
                    int w = s[1] > 0 ? Math.round(free * s[1] / weights) : naturalWidth(c, s);
                    int h = valign == VAlign.STRETCH ? height : Math.min(height, flowHeight(c));
                    int y = in.top + (valign == VAlign.CENTER ? (height - h) / 2 : 0);
                    place(c, x, y, w, h);
                    x += w;
                }
            }
        }
    }

    /** flex-wrap: children flow left to right and wrap within the width (seat chips). */
    public static class Wrap extends JPanel {
        private final int wrapWidth;
        private final int gap;

        public Wrap(int wrapWidth, int gap) {
            super(null);
            this.wrapWidth = wrapWidth;
            this.gap = gap;
            setOpaque(false);
            setLayout(new LayoutManager() {
                @Override public void addLayoutComponent(String name, Component comp) { }
                @Override public void removeLayoutComponent(Component comp) { }

                @Override
                public Dimension preferredLayoutSize(Container parent) {
                    return new Dimension(width(), arrange(width(), false));
                }

                @Override
                public Dimension minimumLayoutSize(Container parent) {
                    return preferredLayoutSize(parent);
                }

                @Override
                public void layoutContainer(Container parent) {
                    arrange(width(), true);
                }
            });
        }

        private int width() {
            return getWidth() > 0 ? getWidth() : wrapWidth;
        }

        private int arrange(int width, boolean apply) {
            int x = 0;
            int y = 0;
            int lineH = 0;
            for (Component c : getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = c.getPreferredSize();
                if (x > 0 && x + d.width > width) {
                    x = 0;
                    y += lineH + gap;
                    lineH = 0;
                }
                if (apply) c.setBounds(x, y, d.width, d.height);
                x += d.width + gap;
                lineH = Math.max(lineH, d.height);
            }
            return y + lineH;
        }
    }

    /** A horizontal rule as a component: border-t with the given color. */
    public static JComponent rule(Color color) {
        JComponent rule = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(color);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        rule.setPreferredSize(new Dimension(1, 1));
        return rule;
    }

    // ------------------------------------------------------------------ page

    /**
     * A scrolling page with a centered column, e.g. "max-w-3xl mx-auto px-8 py-10": maxWidth includes
     * the side padding. The page also paints the glows (box-shadows) registered with {@link #glow}.
     */
    public static class Page extends JPanel implements Scrollable {
        private final JComponent content;
        private final int maxWidth;
        private final int padX;
        private final int padTop;
        private final int padBottom;
        private final List<Glow> glows = new ArrayList<>();

        public Page(JComponent content, int maxWidth, int padX, int padTop, int padBottom) {
            super(null);
            this.content = content;
            this.maxWidth = maxWidth;
            this.padX = padX;
            this.padTop = padTop;
            this.padBottom = padBottom;
            setOpaque(false);
            add(content);
        }

        /** Paints a CSS "0 0 {blur}px color" shadow around target while active says so. */
        public void glow(JComponent target, BooleanSupplier active, int radius, int blur, Color color) {
            glows.add(new Glow(target, active, radius, blur, color));
        }

        public JScrollPane scroller() {
            JScrollPane scroll = new JScrollPane(this);
            scroll.setBorder(null);
            scroll.setViewportBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            return scroll;
        }

        public void scrollToTop() {
            if (getParent() instanceof JViewport) {
                ((JViewport) getParent()).setViewPosition(new Point(0, 0));
            }
        }

        @Override
        public void doLayout() {
            int box = Math.min(maxWidth, getWidth());
            int x = (getWidth() - box) / 2 + padX;
            content.setBounds(x, padTop, box - padX * 2, content.getPreferredSize().height);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(maxWidth, padTop + content.getPreferredSize().height + padBottom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            for (Glow glow : glows) {
                if (!glow.active.getAsBoolean() || !onPage(glow.target)) continue;
                Rectangle r = SwingUtilities.convertRectangle(glow.target.getParent(), glow.target.getBounds(), this);
                Shape shape = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, glow.radius * 2f, glow.radius * 2f);
                paintShadow(g2, shape, shape, glow.color, glow.blur, 0, 0);
            }
            g2.dispose();
        }

        /** The glow-primary shadows go on top of the cards the buttons sit in, as in the browser. */
        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            Graphics2D g2 = (Graphics2D) g.create();
            hints(g2);
            for (FlowButton b : buttons(this, new ArrayList<>())) {
                if (!b.isEnabled() || !onPage(b)) continue;
                Rectangle r = SwingUtilities.convertRectangle(b.getParent(), b.getBounds(), this);
                float arc = Math.min(b.radius() * 2f, r.height);
                Shape box = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, arc, arc);
                // spread -4px: the blurred shape is 4px smaller on every side
                Shape shrunk = new RoundRectangle2D.Float(r.x + 4, r.y + 4, r.width - 8, r.height - 8,
                        Math.max(0, arc - 8), Math.max(0, arc - 8));
                boolean hover = b.isHover();
                paintShadow(g2, shrunk, box, new Color(255, 56, 46, hover ? 191 : 140), hover ? 30 : 24, 0, hover ? 12 : 8);
            }
            g2.dispose();
        }

        private boolean onPage(Component c) {
            for (Component p = c; p != this; p = p.getParent()) {
                if (p == null || !p.isVisible()) return false;
            }
            return true;
        }

        private static List<FlowButton> buttons(Container root, List<FlowButton> out) {
            for (Component c : root.getComponents()) {
                if (c instanceof FlowButton) out.add((FlowButton) c);
                else if (c instanceof Container) buttons((Container) c, out);
            }
            return out;
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height - 32; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
        }

        private static final class Glow {
            final JComponent target;
            final BooleanSupplier active;
            final int radius;
            final int blur;
            final Color color;

            Glow(JComponent target, BooleanSupplier active, int radius, int blur, Color color) {
                this.target = target;
                this.active = active;
                this.radius = radius;
                this.blur = blur;
                this.color = color;
            }
        }
    }

    // ------------------------------------------------------------------ shadows

    /** Blurred shadow images by shape, size and color; least recently used ones are dropped (window resizes). */
    private static final Map<String, BufferedImage> SHADOWS = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > 48;
        }
    };

    /**
     * Paints a CSS box-shadow of the shape: blurred by {@code blur} px (the CSS blur radius), offset by dx/dy,
     * and, like CSS, only outside the shape itself.
     */
    public static void paintShadow(Graphics2D g, Shape shape, Color color, int blur, int dx, int dy) {
        paintShadow(g, shape, shape, color, blur, dx, dy);
    }

    /** As above, blurring {@code shape} but leaving out {@code box} (the element's border box). */
    public static void paintShadow(Graphics2D g, Shape shape, Shape box, Color color, int blur, int dx, int dy) {
        Rectangle2D b = shape.getBounds2D();
        float sigma = Math.max(0.5f, blur / 2f);
        int pad = (int) Math.ceil(sigma * 3);
        Shape local = AffineTransform.getTranslateInstance(-b.getX(), -b.getY()).createTransformedShape(shape);
        String key = shapeKey(local) + "|" + color.getRGB() + "|" + color.getAlpha() + "|" + blur;
        BufferedImage img = SHADOWS.computeIfAbsent(key, k -> blurredMask(local, b, pad, sigma, color));

        Graphics2D g2 = (Graphics2D) g.create();
        Area outside = new Area(new Rectangle2D.Double(b.getX() + dx - pad - 2, b.getY() + dy - pad - 2,
                b.getWidth() + pad * 2 + 4, b.getHeight() + pad * 2 + 4));
        outside.subtract(new Area(box));
        g2.clip(outside);
        g2.drawImage(img, (int) Math.floor(b.getX()) + dx - pad, (int) Math.floor(b.getY()) + dy - pad, null);
        g2.dispose();
    }

    private static String shapeKey(Shape shape) {
        StringBuilder sb = new StringBuilder();
        float[] pts = new float[6];
        for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(pts);
            sb.append(type);
            for (int i = 0; i < 6; i++) sb.append(',').append(Math.round(pts[i] * 4));
        }
        return Integer.toHexString(sb.toString().hashCode()) + sb.length();
    }

    private static BufferedImage blurredMask(Shape local, Rectangle2D b, int pad, float sigma, Color color) {
        int w = (int) Math.ceil(b.getWidth()) + pad * 2 + 1;
        int h = (int) Math.ceil(b.getHeight()) + pad * 2 + 1;
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = mask.createGraphics();
        mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        mg.translate(pad + (b.getX() - Math.floor(b.getX())), pad + (b.getY() - Math.floor(b.getY())));
        mg.setColor(Color.WHITE);
        mg.fill(local);
        mg.dispose();

        int[] px = mask.getRGB(0, 0, w, h, null, 0, w);
        float[] a = new float[w * h];
        for (int i = 0; i < px.length; i++) a[i] = (px[i] >>> 24) / 255f;

        int r = (int) Math.ceil(sigma * 3);
        float[] k = new float[r * 2 + 1];
        float sum = 0;
        for (int i = -r; i <= r; i++) {
            k[i + r] = (float) Math.exp(-(i * i) / (2 * sigma * sigma));
            sum += k[i + r];
        }
        for (int i = 0; i < k.length; i++) k[i] /= sum;

        float[] tmp = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = 0;
                for (int i = -r; i <= r; i++) {
                    int xx = x + i;
                    if (xx >= 0 && xx < w) v += a[y * w + xx] * k[i + r];
                }
                tmp[y * w + x] = v;
            }
        }
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                float v = 0;
                for (int i = -r; i <= r; i++) {
                    int yy = y + i;
                    if (yy >= 0 && yy < h) v += tmp[yy * w + x] * k[i + r];
                }
                a[y * w + x] = v;
            }
        }
        int rgb = color.getRGB() & 0xFFFFFF;
        for (int i = 0; i < px.length; i++) {
            int alpha = Math.min(255, Math.round(a[i] * color.getAlpha()));
            px[i] = (alpha << 24) | rgb;
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, w, h, px, 0, w);
        return out;
    }
}
