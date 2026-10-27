package com.seminarhall.ui;

import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.User;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.SessionManager;
import com.seminarhall.ui.components.Chip;
import com.seminarhall.ui.components.EmptyStatePanel;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.StatusBadge;
import com.seminarhall.ui.components.TicketPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * "My Bookings", as the web app's MyBookingsPage: the SeminarX student-portal sidebar card and the
 * "My Booked Passes" card with the All / Upcoming / Completed / Cancelled tabs and the passes table.
 * Shows only the logged-in student's bookings (BookingService.getBookingsForUser).
 */
public class BookingHistoryPanel extends JPanel implements NavigationController.RefreshableScreen {
    private static final String[] TABS = {"All", "Upcoming", "Completed", "Cancelled"};
    private static final String[] COLUMN_NAMES = {"Event", "Date & Time", "Seats", "Amount", "Status", "Pass"};
    /** Column widths as the browser's auto table layout gives them at this width. */
    private static final double[] COLUMN_WIDTHS = {0.30, 0.16, 0.105, 0.10, 0.155, 0.18};

    private static final Color CARD_BG = new Color(16, 19, 29);         // #10131d
    private static final Color CARD_BORDER = new Color(29, 34, 50);     // #1d2232
    private static final Color ROW_DIVIDER = new Color(24, 28, 42);     // #181c2a
    private static final Color GRAY_600 = new Color(75, 85, 99);
    private static final Color BLUE_600 = new Color(37, 99, 235);
    private static final Color EMERALD_600 = new Color(5, 150, 105);
    private static final Color RED_600 = new Color(220, 38, 38);
    private static final Color AMBER_600 = new Color(217, 119, 6);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final NavigationController navController;
    private final BookingService bookingService;

    private List<Booking> userBookings = new ArrayList<>();
    private String activeTab = "All";
    private boolean loading;
    private String loadError;
    private int loadSeq;

    private final List<TabChip> tabChips = new ArrayList<>();
    private JPanel tableBody;
    private Card mainCard;
    private AvatarTile avatar;
    private JLabel userNameLbl;
    private JLabel userDetailLbl;

    public BookingHistoryPanel(NavigationController navController) {
        this.navController = navController;
        this.bookingService = ServiceFactory.getBookingService();

        setName(NavigationController.SCREEN_BOOKING_HISTORY);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // max-w-7xl mx-auto px-8 py-8 flex-row gap-8
        JPanel content = new JPanel(new BorderLayout(32, 0));
        content.setOpaque(false);
        content.add(buildSidebar(), BorderLayout.WEST);
        content.add(buildMainCard(), BorderLayout.CENTER);

        ScrollPage page = new ScrollPage(content);
        JScrollPane scroll = new JScrollPane(page);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------ sidebar

    private JComponent buildSidebar() {
        // w-60 bg-[#10131d] border border-[#1d2232] rounded-3xl p-6 flex-col justify-between
        Card sidebar = new Card(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(240, super.getPreferredSize().height);
            }
        };
        sidebar.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // logo + SeminarX / STUDENT PORTAL, pb-4 border-b mb-6
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.X_AXIS));
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER), new EmptyBorder(0, 0, 16, 0)));
        brand.add(new LogoTile());
        brand.add(Box.createHorizontalStrut(12));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(stretch(new BrandName(), 20));
        brandText.add(Box.createVerticalStrut(2));
        brandText.add(stretch(label("STUDENT PORTAL", TicketPanel.monoFont(Font.PLAIN, 9), ThemeManager.TEXT_MUTED), 13));
        brand.add(brandText);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, brand.getPreferredSize().height));

        top.add(brand);
        top.add(Box.createVerticalStrut(24));
        top.add(new NavItem("compass", "Explore Events", false,
                () -> navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD)));
        top.add(Box.createVerticalStrut(6));
        top.add(new NavItem("ticket", "My Bookings", true, null));

        // signed-in student, pt-6 border-t
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER), new EmptyBorder(24, 0, 0, 0)));
        avatar = new AvatarTile();
        bottom.add(avatar);
        bottom.add(Box.createHorizontalStrut(12));
        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        userNameLbl = label(" ", ThemeManager.font(ThemeManager.Weight.BOLD, 12), Color.WHITE);
        userDetailLbl = label(" ", TicketPanel.monoFont(Font.PLAIN, 10), ThemeManager.TEXT_MUTED);
        who.add(stretch(userNameLbl, 16));
        who.add(Box.createVerticalStrut(2));
        who.add(stretch(userDetailLbl, 15));
        bottom.add(who);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void updateUserCard() {
        User user = SessionManager.getInstance().getCurrentUser();
        String fullName = user != null && user.getFullName() != null ? user.getFullName() : "Guest";
        userNameLbl.setText(fullName);
        userDetailLbl.setText(user != null && user.getEmail() != null ? user.getEmail() : "");
        userDetailLbl.setToolTipText(userDetailLbl.getText());
        avatar.setInitials(initials(fullName));
    }

    private static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ bookings card

    private JComponent buildMainCard() {
        // flex-1 bg-[#10131d] border border-[#1d2232] rounded-3xl p-8
        mainCard = new Card(null);
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBorder(new EmptyBorder(32, 32, 32, 32));

        // text-xl font-black text-white mb-4
        JLabel title = label("My Booked Passes", ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 20), Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setPreferredSize(new Dimension(title.getPreferredSize().width, 28));
        mainCard.add(title);
        mainCard.add(Box.createVerticalStrut(16));

        // flex gap-2 mb-6
        JPanel tabsRow = new JPanel();
        tabsRow.setOpaque(false);
        tabsRow.setLayout(new BoxLayout(tabsRow, BoxLayout.X_AXIS));
        for (String tab : TABS) {
            TabChip chip = new TabChip(tab, tab.equals(activeTab));
            chip.addActionListener(e -> selectTab(tab));
            if (!tabChips.isEmpty()) {
                tabsRow.add(Box.createHorizontalStrut(8));
            }
            tabChips.add(chip);
            tabsRow.add(chip);
        }
        tabsRow.add(Box.createHorizontalGlue());
        tabsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, tabsRow.getPreferredSize().height));
        mainCard.add(tabsRow);
        mainCard.add(Box.createVerticalStrut(24));

        // table header: border-b border-[#1d2232] text-gray-500 text-[11px]
        Row header = new Row(40, false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        for (String col : COLUMN_NAMES) {
            header.add(label(col, ThemeManager.font(ThemeManager.Weight.BOLD, 11), ThemeManager.TEXT_MUTED));
        }
        mainCard.add(header);

        tableBody = new JPanel();
        tableBody.setOpaque(false);
        tableBody.setLayout(new BoxLayout(tableBody, BoxLayout.Y_AXIS));
        tableBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.add(tableBody);
        return mainCard;
    }

    private void selectTab(String tab) {
        activeTab = tab;
        for (TabChip chip : tabChips) {
            chip.setActive(chip.getText().equals(tab));
        }
        filterList();
    }

    @Override
    public void onScreenDisplayed() {
        updateUserCard();
        loadUserBookings();
    }

    private void loadUserBookings() {
        int seq = ++loadSeq;
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getId() == null) {
            userBookings = new ArrayList<>();
            loading = false;
            loadError = "Please log in to see your bookings.";
            filterList();
            return;
        }
        String userId = user.getId();
        loading = true;
        loadError = null;
        filterList();

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.getBookingsForUser(userId); // only the logged-in student's bookings
            }

            @Override
            protected void done() {
                if (seq != loadSeq) {
                    return;
                }
                loading = false;
                try {
                    List<Booking> result = get();
                    userBookings = result != null ? result : new ArrayList<>();
                } catch (Exception ex) {
                    userBookings = new ArrayList<>();
                    Throwable cause = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
                    loadError = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
                }
                filterList();
            }
        };
        worker.execute();
    }

    private void filterList() {
        tableBody.removeAll();

        if (loading) {
            JLabel msg = label("Loading your bookings...", ThemeManager.font(ThemeManager.Weight.REGULAR, 12),
                    ThemeManager.TEXT_MUTED);
            msg.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel holder = new JPanel(new BorderLayout());
            holder.setOpaque(false);
            holder.setBorder(new EmptyBorder(40, 0, 24, 0));
            holder.add(msg, BorderLayout.CENTER);
            addBodyBlock(holder);
        } else if (loadError != null) {
            addBodyBlock(emptyState(new EmptyStatePanel("ticket", "Could not load your bookings",
                    loadError, "Try Again", this::loadUserBookings)));
        } else {
            List<Booking> filtered = userBookings.stream()
                    .filter(b -> "All".equals(activeTab) || statusOf(b).equals(activeTab))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                String sub = userBookings.isEmpty() ? "You haven't reserved any seats yet."
                        : "You haven't reserved any seats for this category.";
                addBodyBlock(emptyState(new EmptyStatePanel("ticket", "No Bookings Found", sub, "Explore Events",
                        () -> navController.navigateTo(NavigationController.SCREEN_EVENT_LIST))));
            } else {
                for (int i = 0; i < filtered.size(); i++) {
                    tableBody.add(createBookingRow(filtered.get(i), i > 0));
                }
            }
        }

        mainCard.revalidate();
        mainCard.repaint();
    }

    private JComponent emptyState(EmptyStatePanel panel) {
        panel.setBorder(new EmptyBorder(40, 0, 8, 0));
        return panel;
    }

    private void addBodyBlock(JComponent block) {
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
        tableBody.add(block);
    }

    /** Upcoming / Completed / Cancelled as the web shows them; "Confirmed" while the event is under way today. */
    private static String statusOf(Booking b) {
        if (b.getStatus() == BookingStatus.CANCELLED) {
            return "Cancelled";
        }
        if (b.getStatus() == BookingStatus.PENDING) {
            return "Pending";
        }
        LocalDateTime at = b.getEventDateTime();
        LocalDateTime now = LocalDateTime.now();
        if (at == null) {
            return "Confirmed";
        }
        if (at.isAfter(now)) {
            return "Upcoming";
        }
        return at.toLocalDate().equals(now.toLocalDate()) ? "Confirmed" : "Completed";
    }

    private static StatusBadge badgeFor(String status) {
        switch (status) {
            case "Upcoming": return new StatusBadge(status, ThemeManager.BLUE_400, tint(BLUE_600));
            case "Confirmed": return new StatusBadge(status, ThemeManager.EMERALD_400, tint(EMERALD_600));
            case "Cancelled": return new StatusBadge(status, ThemeManager.RED_400, tint(RED_600));
            case "Pending": return new StatusBadge(status, ThemeManager.AMBER_400, tint(AMBER_600));
            default: return new StatusBadge(status, ThemeManager.TEXT_GRAY_300, tint(GRAY_600));
        }
    }

    private static Color tint(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 51);
    }

    private JComponent createBookingRow(Booking b, boolean dividerAbove) {
        Row row = new Row(64, true);
        if (dividerAbove) {
            row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ROW_DIVIDER));
        }

        // font-bold text-white
        JLabel title = label(b.getEventTitle(), ThemeManager.font(ThemeManager.Weight.BOLD, 12), Color.WHITE);
        title.setToolTipText(b.getEventTitle());

        // date, then the start time in text-[10px] text-gray-500
        JPanel when = new JPanel();
        when.setOpaque(false);
        when.setLayout(new BoxLayout(when, BoxLayout.Y_AXIS));
        LocalDateTime at = b.getEventDateTime();
        when.add(fixedHeight(label(at != null ? at.format(DATE) : "-", ThemeManager.font(ThemeManager.Weight.REGULAR, 12),
                ThemeManager.TEXT_GRAY_300), 16));
        when.add(fixedHeight(label(at != null ? at.format(TIME) : "", ThemeManager.font(ThemeManager.Weight.REGULAR, 10),
                ThemeManager.TEXT_MUTED), 16));

        // font-mono font-bold text-white
        JLabel seats = label(b.getFormattedSeats(), TicketPanel.monoFont(Font.BOLD, 12), Color.WHITE);
        seats.setToolTipText(b.getFormattedSeats());

        // font-bold text-emerald-400
        JLabel amount = label(TicketPanel.rupees(b.getTotalAmount()), ThemeManager.font(ThemeManager.Weight.BOLD, 12),
                ThemeManager.EMERALD_400);

        PassButton pass = new PassButton("View Ticket");
        pass.addActionListener(e -> navController.showBookingConfirmation(b.getId()));

        row.add(title);
        row.add(when);
        row.add(seats);
        row.add(amount);
        row.add(badgeFor(statusOf(b)));
        row.add(pass);
        return row;
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** A line that takes the full width of its column at a fixed line height. */
    private static JComponent stretch(JComponent c, int height) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, height));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return c;
    }

    private static JLabel fixedHeight(JLabel l, int height) {
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setPreferredSize(new Dimension(l.getPreferredSize().width, height));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return l;
    }

    // ------------------------------------------------------------------ small components

    /** Rounded #10131d card with a #1d2232 border; the bookings card also paints the active tab's glow. */
    private class Card extends JPanel {
        Card(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = ThemeManager.RADIUS_3XL * 2;
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            // shadow-[0_0_12px_rgba(255,56,46,0.5)] around the active tab
            if (this == mainCard) {
                for (TabChip chip : tabChips) {
                    if (chip.isActive() && chip.getWidth() > 0) {
                        Rectangle r = SwingUtilities.convertRectangle(chip.getParent(), chip.getBounds(), this);
                        g2.setColor(new Color(255, 56, 46, 16));
                        for (int i = 0; i < 8; i++) {
                            float e = 6f - i * (12f / 7);
                            float a = Math.max(0f, 24f + 2 * e);
                            g2.fill(new RoundRectangle2D.Float(r.x - e, r.y - e, r.width + 2 * e, r.height + 2 * e, a, a));
                        }
                    }
                }
            }
            g2.dispose();
        }
    }

    /**
     * A filter tab: "text-xs font-bold px-4 py-1.5 rounded-xl", active bg-[#ff382e] text-white,
     * inactive bg-[#141724] border-[#23273a] text-gray-400 (white on hover).
     */
    private static class TabChip extends Chip {
        private boolean hovered;

        TabChip(String text, boolean active) {
            super(text, active);
            setBorder(new EmptyBorder(6, 16, 6, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 32, 28);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_XL * 2;
            Color text;
            if (isActive()) {
                g2.setColor(ThemeManager.BRAND_RED);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                text = Color.WHITE;
            } else {
                g2.setColor(new Color(20, 23, 36));
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.setColor(new Color(35, 39, 58));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                text = hovered ? Color.WHITE : ThemeManager.TEXT_SECONDARY;
            }
            g2.setFont(getFont());
            g2.setColor(text);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** "bg-[#171a25] border border-[#242938] hover:border-white/30 text-white text-xs px-3 py-1.5 rounded-lg font-semibold". */
    private static class PassButton extends JButton {
        private boolean hovered;

        PassButton(String text) {
            super(text);
            setFont(ThemeManager.font(ThemeManager.Weight.SEMIBOLD, 12));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 26, 30);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_LG * 2;
            g2.setColor(new Color(23, 26, 37));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(hovered ? new Color(255, 255, 255, 77) : new Color(36, 41, 56));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** One table row (or the header): cells placed in the shared column grid, px-2 padding, vertically centered. */
    private static class Row extends JPanel {
        private final int height;
        private boolean hovered;

        Row(int height, boolean hoverable) {
            this.height = height;
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setLayout(new ColumnsLayout());
            if (hoverable) {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { setHovered(true); }
                    @Override public void mouseExited(MouseEvent e) {
                        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), Row.this);
                        if (!new Rectangle(getSize()).contains(p)) {
                            setHovered(false);
                        }
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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, height);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (hovered) {
                g.setColor(new Color(255, 255, 255, 5)); // hover:bg-white/[0.02]
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private static class ColumnsLayout implements LayoutManager {
        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }
        @Override public Dimension preferredLayoutSize(Container parent) { return parent.getPreferredSize(); }
        @Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0, 0); }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int width = parent.getWidth() - in.left - in.right;
            int height = parent.getHeight() - in.top - in.bottom;
            double x = in.left;
            for (int i = 0; i < parent.getComponentCount() && i < COLUMN_WIDTHS.length; i++) {
                Component c = parent.getComponent(i);
                double colW = COLUMN_WIDTHS[i] * width;
                int avail = (int) colW - 16;
                Dimension pref = c.getPreferredSize();
                // text cells get the whole column (no clipping from measuring); pills and buttons keep their size
                boolean fill = !(c instanceof StatusBadge || c instanceof AbstractButton);
                int w = fill ? avail : Math.min(pref.width, avail);
                int h = Math.min(pref.height, height);
                c.setBounds((int) x + 8, in.top + (height - h) / 2, w, h);
                x += colW;
            }
        }
    }

    /** w-10 h-10 rounded-xl bg-gradient-to-r from-[#ff382e] to-[#ff6835], "Σ" font-black text-lg. */
    private static class LogoTile extends JComponent {
        LogoTile() {
            Dimension d = new Dimension(40, 40);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, ThemeManager.BRAND_RED, w, 0, ThemeManager.BRAND_ORANGE));
            g2.fillRoundRect(0, 0, w, h, ThemeManager.RADIUS_XL * 2, ThemeManager.RADIUS_XL * 2);
            String sigma = "Σ";
            Font font = ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 18);
            if (font.canDisplay(sigma.charAt(0)) == false) {
                font = new Font("Segoe UI", Font.BOLD, 18);
            }
            g2.setFont(font);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(sigma, (w - fm.stringWidth(sigma)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** "Seminar<span text-[#ff382e]>X</span>" in font-black text-sm, drawn as one run so the X sits right after. */
    private static class BrandName extends JComponent {
        BrandName() {
            setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 14));
            setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth("SeminarX") + 4, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int baseline = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(Color.WHITE);
            g2.drawString("Seminar", 0, baseline);
            g2.setColor(ThemeManager.BRAND_RED);
            g2.drawString("X", fm.stringWidth("Seminar"), baseline);
            g2.dispose();
        }
    }

    /** w-10 h-10 rounded-xl bg-gradient-to-tr from-gray-700 to-gray-900 border-white/20 with the initials. */
    private static class AvatarTile extends JComponent {
        private String initials = "";

        AvatarTile() {
            Dimension d = new Dimension(40, 40);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        void setInitials(String initials) {
            this.initials = initials;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_XL * 2;
            g2.setPaint(new GradientPaint(0, h, new Color(55, 65, 81), w, 0, new Color(17, 24, 39)));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(new Color(255, 255, 255, 51));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 12));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, (w - fm.stringWidth(initials)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Sidebar link: "px-3.5 py-2 rounded-xl text-xs font-semibold gap-2.5"; active bg-[#ff382e]/15 text-[#ff382e] font-bold. */
    private static class NavItem extends JPanel {
        private final boolean active;
        private boolean hovered;
        private final JLabel text;
        private final String iconName;

        NavItem(String iconName, String label, boolean active, Runnable onClick) {
            this.active = active;
            this.iconName = iconName;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(8, 14, 8, 14));
            text = new JLabel(label);
            text.setIconTextGap(10);
            text.setFont(ThemeManager.font(active ? ThemeManager.Weight.BOLD : ThemeManager.Weight.SEMIBOLD, 12));
            add(text, BorderLayout.CENTER);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            setPreferredSize(new Dimension(192, 32));
            applyColors();
            if (onClick != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; applyColors(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; applyColors(); }
                    @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
                });
            }
        }

        private void applyColors() {
            Color c = active ? ThemeManager.BRAND_RED : (hovered ? Color.WHITE : ThemeManager.TEXT_SECONDARY);
            text.setForeground(c);
            text.setIcon(Icons.get(iconName, 14, c));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color bg = active ? new Color(255, 56, 46, 38) : (hovered ? ThemeManager.WHITE_5 : null);
            if (bg != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeManager.RADIUS_XL * 2, ThemeManager.RADIUS_XL * 2);
                g2.dispose();
            }
        }
    }

    /** Page body in the scroll pane: the content sits in a centered max-w-7xl column with px-8 py-8. */
    private static class ScrollPage extends JPanel implements Scrollable {
        private static final int MAX_WIDTH = 1280;
        private static final int PAD = 32;
        private final JComponent content;

        ScrollPage(JComponent content) {
            this.content = content;
            setOpaque(false);
            setLayout(null);
            add(content);
        }

        private Rectangle contentBounds(int width) {
            int outer = Math.min(width, MAX_WIDTH);
            int x = (width - outer) / 2 + PAD;
            int w = Math.max(0, outer - 2 * PAD);
            return new Rectangle(x, PAD, w, content.getPreferredSize().height);
        }

        @Override
        public void doLayout() {
            content.setBounds(contentBounds(getWidth()));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(MAX_WIDTH, content.getPreferredSize().height + 2 * PAD);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height - 32; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
        }
    }
}
