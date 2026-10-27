package com.seminarhall.ui.organizer;

import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.Tag;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * "Scheduled Campus Events" (web ManageEventsPage): a "max-w-7xl" card with the events table
 * (Event Title, Date, Venue, Available Seats, Ticket Price, Action) and "+ Schedule New Event".
 */
final class ManageEventsView extends JPanel {

    interface Actions {
        void showDashboard();

        void showCreate();

        void edit(Event event);

        void delete(Event event);
    }

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private final Actions actions;
    private final ConsoleUi.Banner notice = new ConsoleUi.Banner();
    private final JPanel noticeSlot;
    private final EventsTable table;
    private final JScrollPane scroll;

    ManageEventsView(Actions actions) {
        super(new BorderLayout());
        this.actions = actions;
        setOpaque(false);

        // "bg-[#10131d] border border-[#1d2232] rounded-3xl p-6 sm:p-8"
        ConsoleUi.Card card = new ConsoleUi.Card(ThemeManager.RADIUS_3XL, new ConsoleUi.Stack(0));
        card.setBorder(new EmptyBorder(33, 33, 33, 33));

        JPanel titleBlock = new JPanel(new ConsoleUi.Stack(0));
        titleBlock.setOpaque(false);
        titleBlock.add(ConsoleUi.text("Scheduled Campus Events", ConsoleUi.font(Weight.EXTRABOLD, 20), Color.WHITE, 28));
        titleBlock.add(Box.createVerticalStrut(4));
        ConsoleUi.LinkButton back = new ConsoleUi.LinkButton("← Return to Dashboard");
        back.addActionListener(e -> actions.showDashboard());
        titleBlock.add(ConsoleUi.leftAligned(back));

        StyledButton create = ConsoleUi.primaryButton("+ Schedule New Event", 20);
        create.addActionListener(e -> actions.showCreate());
        card.add(new ConsoleUi.GlowRow(titleBlock, create, false, 16));
        card.add(Box.createVerticalStrut(24));   // mb-6

        noticeSlot = new JPanel(new BorderLayout());
        noticeSlot.setOpaque(false);
        noticeSlot.setBorder(new EmptyBorder(0, 0, 16, 0));
        noticeSlot.add(notice, BorderLayout.CENTER);
        noticeSlot.setVisible(false);
        card.add(noticeSlot);

        table = new EventsTable();
        card.add(table);

        scroll = ConsoleUi.scrollPage(card, 1280, 32, 32);
        add(scroll, BorderLayout.CENTER);
        showLoading();
    }

    void showLoading() {
        table.setMessage("Loading scheduled events…", ThemeManager.TEXT_SECONDARY);
    }

    void showLoadError(String message) {
        table.setMessage("Could not load events: " + message, ThemeManager.RED_400);
    }

    void setEvents(List<Event> events) {
        if (events.isEmpty()) {
            table.setMessage("No events scheduled yet. Use \"+ Schedule New Event\" to publish one.",
                    ThemeManager.TEXT_SECONDARY);
        } else {
            table.setEvents(events);
        }
    }

    void showSuccess(String message) {
        notice.success(message);
        noticeSlot.setVisible(true);
        revalidate();
    }

    void showError(String message) {
        notice.error(message);
        noticeSlot.setVisible(true);
        revalidate();
    }

    void clearNotice() {
        notice.clear();
        noticeSlot.setVisible(false);
        revalidate();
    }

    void scrollToTop() {
        scroll.getVerticalScrollBar().setValue(0);
    }

    // ------------------------------------------------------------------ table

    /**
     * "w-full text-left text-xs": header "border-b border-[#1d2232] text-gray-500 text-[11px]" (py-3 px-2),
     * body "divide-y divide-[#181c2a] text-gray-300" rows (py-4 px-2, hover:bg-white/[0.02]).
     * Column widths follow the content, like an auto-layout HTML table.
     */
    private final class EventsTable extends JPanel {
        private final String[] headers = {"Event Title", "Date", "Venue", "Available Seats", "Ticket Price", "Action"};
        private int[] widths = new int[headers.length];

        EventsTable() {
            setOpaque(false);
            setLayout(new TableLayout());
        }

        void setMessage(String message, Color color) {
            removeAll();
            add(headerRow());
            Row row = new Row(false, true);
            JLabel label = cell(message, ConsoleUi.font(Weight.REGULAR, 12), color);
            row.add(label);
            add(row);
            revalidate();
            repaint();
        }

        void setEvents(List<Event> events) {
            removeAll();
            add(headerRow());
            for (Event event : events) {
                add(eventRow(event));
            }
            revalidate();
            repaint();
        }

        private Row headerRow() {
            Row row = new Row(true, false);
            for (String header : headers) {
                row.add(cell(header, ConsoleUi.font(Weight.BOLD, 11), ThemeManager.TEXT_MUTED));
            }
            return row;
        }

        private Row eventRow(Event event) {
            Row row = new Row(false, false);
            Font regular = ConsoleUi.font(Weight.REGULAR, 12);
            Color text = event.isCancelled() ? ThemeManager.TEXT_MUTED : ThemeManager.TEXT_GRAY_300;

            JLabel title = cell(event.getTitle(), ConsoleUi.font(Weight.BOLD, 12),
                    event.isCancelled() ? ThemeManager.TEXT_SECONDARY : Color.WHITE);
            if (event.isCancelled()) {
                JPanel titleCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                titleCell.setOpaque(false);
                titleCell.setBorder(new EmptyBorder(0, -8, 0, 0));
                titleCell.add(title);
                titleCell.add(Tag.tinted("Cancelled", ThemeManager.RED_400));
                row.add(titleCell);
            } else {
                row.add(title);
            }
            row.add(cell(event.getDate() != null ? DATE.format(event.getDate()) : "", regular, text));
            row.add(cell(event.getSeminarHall(), regular, text));
            row.add(cell(event.getAvailableSeats() + " of " + event.getTotalCapacity(), ConsoleUi.mono(true, 12),
                    event.isCancelled() ? ThemeManager.TEXT_MUTED : ThemeManager.EMERALD_400));
            row.add(cell(EventVisuals.priceLabel(event.getTicketPrice()), ConsoleUi.font(Weight.BOLD, 12), text));

            JPanel actionsCell = new JPanel();
            actionsCell.setOpaque(false);
            actionsCell.setLayout(new BoxLayout(actionsCell, BoxLayout.X_AXIS));
            if (!event.isCancelled()) {
                IconButton edit = new IconButton("pencil", Color.WHITE, "Edit event");
                edit.addActionListener(e -> actions.edit(event));
                IconButton delete = new IconButton("trash-2", ThemeManager.RED_400, "Delete event");
                delete.addActionListener(e -> actions.delete(event));
                actionsCell.add(edit);
                actionsCell.add(Box.createHorizontalStrut(12));   // mr-3
                actionsCell.add(delete);
            }
            row.add(actionsCell);
            return row;
        }

        private JLabel cell(String text, Font font, Color color) {
            JLabel label = new JLabel(text != null ? text : "");
            label.setFont(font);
            label.setForeground(color);
            return label;
        }

        /** Auto table layout: each column gets its content width plus px-2, extra space shared in proportion. */
        private void computeWidths(int available) {
            int[] natural = new int[headers.length];
            for (Component c : getComponents()) {
                Row row = (Row) c;
                if (row.spanning) continue;
                for (int i = 0; i < row.getComponentCount() && i < natural.length; i++) {
                    natural[i] = Math.max(natural[i], row.getComponent(i).getPreferredSize().width + 16);
                }
            }
            int total = 0;
            for (int n : natural) total += n;
            widths = new int[headers.length];
            if (total == 0) return;
            double scale = available / (double) total;
            int used = 0;
            for (int i = 0; i < natural.length; i++) {
                widths[i] = (int) Math.floor(natural[i] * scale);
                used += widths[i];
            }
            widths[0] += available - used;
        }

        private final class TableLayout implements LayoutManager {
            @Override public void addLayoutComponent(String name, Component comp) { }
            @Override public void removeLayoutComponent(Component comp) { }

            @Override
            public Dimension preferredLayoutSize(Container parent) {
                int h = 0;
                for (Component c : parent.getComponents()) h += c.getPreferredSize().height;
                return new Dimension(550, h);   // min-w-[550px]
            }

            @Override
            public Dimension minimumLayoutSize(Container parent) {
                return preferredLayoutSize(parent);
            }

            @Override
            public void layoutContainer(Container parent) {
                computeWidths(parent.getWidth());
                int y = 0;
                for (Component c : parent.getComponents()) {
                    int h = c.getPreferredSize().height;
                    c.setBounds(0, y, parent.getWidth(), h);
                    y += h;
                }
            }
        }

        /** One table row; header rows are 41px (py-3 + border-b), body rows 49px (py-4 + divider). */
        private final class Row extends JPanel {
            private final boolean header;
            private final boolean spanning;
            private boolean hovered;

            Row(boolean header, boolean spanning) {
                super(null);
                this.header = header;
                this.spanning = spanning;
                setOpaque(false);
                if (!header && !spanning) {
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { setHovered(true); }
                        @Override public void mouseExited(MouseEvent e) {
                            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), Row.this);
                            if (!new Rectangle(getSize()).contains(p)) setHovered(false);
                        }
                    });
                }
            }

            private void setHovered(boolean hovered) {
                if (this.hovered != hovered) {
                    this.hovered = hovered;
                    repaint();
                }
            }

            private boolean first() {
                return getParent() != null && getParent().getComponentCount() > 1 && getParent().getComponent(1) == this;
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(550, header ? 41 : 49);
            }

            @Override
            public void doLayout() {
                int top = header ? 0 : 1;
                int h = getHeight() - 1;
                if (spanning) {
                    Component c = getComponent(0);
                    c.setBounds(8, top, getWidth() - 16, h);
                    return;
                }
                int x = 0;
                for (int i = 0; i < getComponentCount() && i < widths.length; i++) {
                    Component c = getComponent(i);
                    int ph = Math.min(h, c.getPreferredSize().height);
                    c.setBounds(x + 8, top + (h - ph) / 2, Math.max(0, widths[i] - 16), ph);
                    x += widths[i];
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    g.setColor(new Color(255, 255, 255, 5));   // hover:bg-white/[0.02]
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                if (header) {
                    g.setColor(ConsoleUi.CARD_BORDER);
                    g.fillRect(0, getHeight() - 1, getWidth(), 1);
                } else if (!first()) {
                    g.setColor(ConsoleUi.ROW_DIVIDER);
                    g.fillRect(0, 0, getWidth(), 1);
                }
            }
        }
    }

    /** Lucide icon button: "text-gray-400 hover:text-white" (pencil) / "hover:text-red-400" (trash), size 14. */
    private static final class IconButton extends JButton {
        IconButton(String icon, Color hoverColor, String tooltip) {
            super(Icons.get(icon, 14, ThemeManager.TEXT_SECONDARY));
            setRolloverIcon(Icons.get(icon, 14, hoverColor));
            setRolloverEnabled(true);
            setToolTipText(tooltip);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(14, 14);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }
}
