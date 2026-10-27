package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The web app's event card grid ("grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"), plus the
 * layout pieces the Home, Explore and Event Details screens share: a scrollable page column
 * ("max-w-7xl mx-auto px-8"), wrapping text with CSS line heights, and the
 * "Upcoming Events · N Available · category chips" header.
 */
public class EventGrid extends JPanel {

    /** A component whose height depends on the width it is given (wrapping text, grids). */
    public interface HeightForWidth {
        int heightForWidth(int width);
    }

    /** gap-6 */
    public static final int GAP = 24;
    /** Cards narrower than this drop a column: 4 at 1440px, 3 at ~1100px, 2 below ~880px. */
    private static final int MIN_CARD_WIDTH = 270;

    private final List<EventCard> cards = new ArrayList<>();
    private JComponent placeholder;

    public EventGrid() {
        setOpaque(false);
        setLayout(null);
    }

    /** Shows one card per event; an empty list shows the given empty view instead. */
    public void setEvents(List<Event> events, Consumer<Event> onOpen, Consumer<Event> onBook, JComponent emptyView) {
        removeAll();
        cards.clear();
        placeholder = null;
        for (Event event : events) {
            EventCard card = new EventCard(event, onOpen, onBook);
            cards.add(card);
            add(card);
        }
        if (events.isEmpty() && emptyView != null) {
            placeholder = emptyView;
            add(emptyView);
        }
        revalidate();
        repaint();
    }

    /** Replaces the cards with a loading or error view. */
    public void setPlaceholder(JComponent view) {
        removeAll();
        cards.clear();
        placeholder = view;
        add(view);
        revalidate();
        repaint();
    }

    public static int columnsFor(int width) {
        return Math.max(1, Math.min(4, (width + GAP) / (MIN_CARD_WIDTH + GAP)));
    }

    /** Height of the grid when laid out at this width. */
    public int heightForWidth(int width) {
        if (placeholder != null) {
            return Math.max(260, placeholder.getPreferredSize().height);
        }
        int cols = columnsFor(width);
        int rows = (cards.size() + cols - 1) / cols;
        return rows == 0 ? 0 : rows * EventCard.HEIGHT + (rows - 1) * GAP;
    }

    @Override
    public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : 1216;
        return new Dimension(w, heightForWidth(w));
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        if (placeholder != null) {
            placeholder.setBounds(0, 0, w, getHeight());
            return;
        }
        int cols = columnsFor(w);
        int cardW = (w - GAP * (cols - 1)) / cols;
        for (int i = 0; i < cards.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            cards.get(i).setBounds(col * (cardW + GAP), row * (EventCard.HEIGHT + GAP), cardW, EventCard.HEIGHT);
        }
    }

    // ------------------------------------------------------------------------------------------

    /** Centered "No events" message shown inside the grid, with an optional button. */
    public static JComponent emptyView(String title, String message, String actionText, Runnable action) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.WHITE_5);
                g2.fillOval(0, 0, 63, 63);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.drawOval(0, 0, 63, 63);
                g2.dispose();
                Icons.get("search", 26, ThemeManager.TEXT_MUTED).paintIcon(this, g, 19, 19);
            }
        };
        Dimension iconSize = new Dimension(64, 64);
        icon.setPreferredSize(iconSize);
        icon.setMaximumSize(iconSize);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 18));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(ThemeManager.font(ThemeManager.Weight.REGULAR, 14));
        messageLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(icon);
        box.add(Box.createVerticalStrut(16));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(6));
        box.add(messageLabel);
        if (actionText != null && action != null) {
            StyledButton button = new StyledButton(actionText, StyledButton.ButtonStyle.SECONDARY);
            button.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 12));
            button.setMargin(new Insets(8, 16, 8, 16));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(e -> action.run());
            box.add(Box.createVerticalStrut(18));
            box.add(button);
        }

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(box);
        wrapper.setPreferredSize(new Dimension(400, 300));
        return wrapper;
    }

    /** A scroll pane for a page: vertical only, transparent over the screen's #050609 background. */
    public static JScrollPane scroll(JComponent page) {
        JScrollPane scrollPane = new JScrollPane(page,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /** Plus Jakarta Sans with CSS letter-spacing (tracking-tight = -0.025, tracking-wider = 0.05). */
    public static Font tracked(Font font, float tracking) {
        return font.deriveFont(Map.of(TextAttribute.TRACKING, tracking));
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Scrollable page column: children are stacked top to bottom at the content width of
     * "max-w-7xl mx-auto px-8" (at most 1216px wide, centered). Use Box.createVerticalStrut for gaps.
     */
    public static class Page extends JPanel implements Scrollable {
        private final int maxWidth;
        private final int padX;
        private final int padTop;
        private final int padBottom;

        public Page(int maxWidth, int padX, int padTop, int padBottom) {
            this.maxWidth = maxWidth;
            this.padX = padX;
            this.padTop = padTop;
            this.padBottom = padBottom;
            setOpaque(false);
            setLayout(null);
        }

        private int viewWidth() {
            Container parent = getParent();
            if (parent instanceof JViewport && parent.getWidth() > 0) {
                return parent.getWidth();
            }
            return getWidth() > 0 ? getWidth() : 1400;
        }

        private int contentWidth(int width) {
            return Math.max(100, Math.min(width, maxWidth) - 2 * padX);
        }

        private static int heightOf(Component c, int width) {
            if (c instanceof HeightForWidth) {
                return ((HeightForWidth) c).heightForWidth(width);
            }
            if (c instanceof EventGrid) {
                return ((EventGrid) c).heightForWidth(width);
            }
            return c.getPreferredSize().height;
        }

        @Override
        public Dimension getPreferredSize() {
            int width = viewWidth();
            int cw = contentWidth(width);
            int h = padTop + padBottom;
            for (Component c : getComponents()) {
                if (c.isVisible()) {
                    h += heightOf(c, cw);
                }
            }
            return new Dimension(width, h);
        }

        @Override
        public void doLayout() {
            int width = getWidth();
            int cw = contentWidth(width);
            int x = (width - cw) / 2;
            int y = padTop;
            for (Component c : getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                int h = heightOf(c, cw);
                c.setBounds(x, y, cw, h);
                y += h;
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
        }
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Text that wraps like a browser paragraph, with a CSS line height (e.g. leading-relaxed) and
     * an optional max width (max-w-lg). Newlines force breaks. Optionally painted with the
     * brand red → orange gradient (bg-gradient-to-r bg-clip-text).
     */
    public static class Paragraph extends JComponent implements HeightForWidth {
        private String text;
        private final int lineHeight;
        private int maxWidth = Integer.MAX_VALUE;
        private int maxLines;
        private boolean gradient;

        public Paragraph(String text, Font font, Color color, int lineHeight) {
            this.text = text == null ? "" : text;
            this.lineHeight = lineHeight;
            setFont(font);
            setForeground(color);
            setOpaque(false);
        }

        public void setText(String text) {
            this.text = text == null ? "" : text;
            revalidate();
            repaint();
        }

        public String getText() {
            return text;
        }

        /** max-w-*: the text wraps at this width even when it has more room. */
        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            revalidate();
        }

        /** line-clamp: extra lines are cut and the last one ends in an ellipsis. */
        public void setMaxLines(int maxLines) {
            this.maxLines = maxLines;
            revalidate();
        }

        public void setGradient(boolean gradient) {
            this.gradient = gradient;
            repaint();
        }

        public int getLineHeight() {
            return lineHeight;
        }

        /** Width of the widest line when nothing forces a wrap. */
        public int naturalWidth() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = 0;
            for (String line : text.split("\n", -1)) {
                w = Math.max(w, fm.stringWidth(line));
            }
            return Math.min(w + 1, maxWidth);
        }

        @Override
        public int heightForWidth(int width) {
            return Math.max(1, lines(Math.min(width, maxWidth)).size()) * lineHeight;
        }

        @Override
        public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) {
                return super.getPreferredSize();
            }
            int w = getWidth() > 0 ? getWidth() : naturalWidth();
            return new Dimension(naturalWidth(), heightForWidth(w));
        }

        private List<String> lines(int width) {
            FontMetrics fm = getFontMetrics(getFont());
            List<String> out = new ArrayList<>();
            for (String hard : text.split("\n", -1)) {
                String[] words = hard.split(" ");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String candidate = line.length() == 0 ? word : line + " " + word;
                    if (line.length() > 0 && fm.stringWidth(candidate) > width) {
                        out.add(line.toString());
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(candidate);
                    }
                }
                out.add(line.toString());
            }
            if (maxLines > 0 && out.size() > maxLines) {
                List<String> cut = new ArrayList<>(out.subList(0, maxLines));
                String last = cut.get(maxLines - 1);
                while (!last.isEmpty() && fm.stringWidth(last + "...") > width) {
                    last = last.substring(0, last.length() - 1);
                }
                cut.set(maxLines - 1, last.trim() + "...");
                return cut;
            }
            return out;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int halfLeading = (lineHeight - (fm.getAscent() + fm.getDescent())) / 2;
            int y = 0;
            for (String line : lines(Math.min(getWidth(), maxWidth))) {
                if (gradient) {
                    int w = Math.max(1, fm.stringWidth(line));
                    g2.setPaint(new GradientPaint(0, 0, ThemeManager.BRAND_RED, w, 0, ThemeManager.BRAND_ORANGE));
                } else {
                    g2.setColor(getForeground());
                }
                g2.drawString(line, 0, y + halfLeading + fm.getAscent());
                y += lineHeight;
            }
            g2.dispose();
        }
    }

    /** The same text anti-aliasing Swing labels use. */
    public static void applyTextHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Object hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (hints instanceof Map) {
            g2.addRenderingHints((Map<?, ?>) hints);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
    }

    // ------------------------------------------------------------------------------------------

    /**
     * "Upcoming Events [12 Available]" on the left and the category chips on the right,
     * with "pb-4 border-b border-white/5" below; the chips wrap under the title when narrow.
     */
    public static class SectionHeader extends JPanel implements HeightForWidth {
        private static final int ROW = 40;       // chips row: py-1 + 32px chips
        private static final int PAD_BOTTOM = 16;

        private final JLabel title;
        private final CountPill count;
        private final JPanel chips;
        private final List<Chip> chipButtons = new ArrayList<>();
        private EventCategory active = EventCategory.ALL;

        public SectionHeader(String titleText, Consumer<EventCategory> onCategory) {
            setOpaque(false);
            setLayout(null);

            title = new JLabel(titleText);
            title.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 24));
            title.setForeground(ThemeManager.TEXT_PRIMARY);
            count = new CountPill("");

            chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            chips.setOpaque(false);
            for (EventCategory category : EventVisuals.CHIP_ORDER) {
                Chip chip = new Chip(EventVisuals.chipLabel(category), category == active);
                chip.addActionListener(e -> {
                    setActive(category);
                    if (onCategory != null) {
                        onCategory.accept(category);
                    }
                });
                chipButtons.add(chip);
                chips.add(chip);
            }
            ((FlowLayout) chips.getLayout()).setAlignOnBaseline(false);

            add(title);
            add(count);
            add(chips);
        }

        public void setTitle(String text) {
            title.setText(text);
            revalidate();
            repaint();
        }

        public void setCountText(String text) {
            count.setText(text);
            count.setVisible(text != null && !text.isEmpty());
            revalidate();
            repaint();
        }

        public EventCategory getActive() {
            return active;
        }

        public void setActive(EventCategory category) {
            this.active = category;
            for (int i = 0; i < chipButtons.size(); i++) {
                chipButtons.get(i).setActive(EventVisuals.CHIP_ORDER.get(i) == category);
            }
        }

        private int leftWidth() {
            int w = title.getPreferredSize().width;
            if (count.isVisible()) {
                w += 12 + count.getPreferredSize().width;
            }
            return w;
        }

        private boolean oneRow(int width) {
            return leftWidth() + 16 + chips.getPreferredSize().width <= width;
        }

        @Override
        public int heightForWidth(int width) {
            return (oneRow(width) ? ROW : ROW + 16 + ROW) + PAD_BOTTOM + 1;
        }

        @Override
        public Dimension getPreferredSize() {
            int w = getWidth() > 0 ? getWidth() : 1216;
            return new Dimension(w, heightForWidth(w));
        }

        @Override
        public void doLayout() {
            int w = getWidth();
            Dimension c = count.getPreferredSize();
            int countRoom = count.isVisible() ? 12 + c.width : 0;
            int titleW = Math.max(0, Math.min(title.getPreferredSize().width, w - countRoom));
            title.setBounds(0, (ROW - 32) / 2, titleW, 32);
            count.setBounds(titleW + 12, (ROW - c.height) / 2, c.width, c.height);
            Dimension ch = chips.getPreferredSize();
            if (oneRow(w)) {
                chips.setBounds(w - ch.width, (ROW - ch.height) / 2, ch.width, ch.height);
            } else {
                chips.setBounds(0, ROW + 16 + (ROW - ch.height) / 2, w, ch.height);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(ThemeManager.WHITE_5);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
        }
    }

    /** "12 Available": text-xs font-mono gray-400 on #141724, rounded-full, px-2.5 py-1. */
    static class CountPill extends JLabel {
        CountPill(String text) {
            super(text);
            setFont(monoFont());
            setForeground(ThemeManager.TEXT_SECONDARY);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(20, 23, 36));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }

        private static Font mono;

        private static Font monoFont() {
            if (mono == null) {
                String family = Font.MONOSPACED;
                for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                    if (name.equals("Consolas")) {
                        family = name;
                        break;
                    }
                }
                mono = new Font(family, Font.PLAIN, 12);
            }
            return mono;
        }
    }
}
