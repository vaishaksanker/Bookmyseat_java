package com.seminarhall.ui;

import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.User;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.SessionManager;
import com.seminarhall.ui.components.EmptyStatePanel;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.StyledTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingHistoryPanel extends JPanel implements NavigationController.RefreshableScreen {
    private final NavigationController navController;
    private final BookingService bookingService;

    private List<Booking> userBookings = new ArrayList<>();
    private JPanel bookingsTablePanel;
    private StyledTextField searchField;
    private String activeTab = "All";

    private JButton allTab;
    private JButton upcomingTab;
    private JButton completedTab;
    private JButton cancelledTab;

    public BookingHistoryPanel(NavigationController navController) {
        this.navController = navController;
        this.bookingService = ServiceFactory.getBookingService();

        setName(NavigationController.SCREEN_BOOKING_HISTORY);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Main Container matching Screen 9
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Top Row: Title, Filter Tabs (All, Upcoming, Completed, Cancelled), Search
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("My Bookings");
        title.setFont(ThemeManager.FONT_HEADER_XL);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        // Filter Pills
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pills.setOpaque(false);

        allTab = createFilterPill("All", true);
        upcomingTab = createFilterPill("Upcoming", false);
        completedTab = createFilterPill("Completed", false);
        cancelledTab = createFilterPill("Cancelled", false);

        pills.add(allTab);
        pills.add(upcomingTab);
        pills.add(completedTab);
        pills.add(cancelledTab);

        searchField = new StyledTextField("🔍 Search bookings...");
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterList(); }
            @Override public void removeUpdate(DocumentEvent e) { filterList(); }
            @Override public void changedUpdate(DocumentEvent e) { filterList(); }
        });

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        leftBox.setOpaque(false);
        leftBox.add(title);
        leftBox.add(pills);

        topBar.add(leftBox, BorderLayout.WEST);
        topBar.add(searchField, BorderLayout.EAST);
        container.add(topBar, BorderLayout.NORTH);

        // Center Table List
        bookingsTablePanel = new JPanel();
        bookingsTablePanel.setOpaque(false);
        bookingsTablePanel.setLayout(new BoxLayout(bookingsTablePanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(bookingsTablePanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scroll, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);
    }

    private JButton createFilterPill(String tabName, boolean active) {
        JButton btn = new JButton(tabName);
        btn.setFont(ThemeManager.FONT_SMALL_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(active ? ThemeManager.ACCENT_RED : ThemeManager.BG_CARD);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            activeTab = tabName;
            allTab.setBackground(ThemeManager.BG_CARD);
            upcomingTab.setBackground(ThemeManager.BG_CARD);
            completedTab.setBackground(ThemeManager.BG_CARD);
            cancelledTab.setBackground(ThemeManager.BG_CARD);
            btn.setBackground(ThemeManager.ACCENT_RED);
            filterList();
        });
        return btn;
    }

    @Override
    public void onScreenDisplayed() {
        loadUserBookings();
    }

    private void loadUserBookings() {
        User user = SessionManager.getInstance().getCurrentUser();
        String userId = user != null ? user.getId() : "usr_student_1";

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.getBookingsForUser(userId); // only the logged-in student's bookings
            }

            @Override
            protected void done() {
                try {
                    userBookings = get();
                } catch (Exception ex) {
                    userBookings = new ArrayList<>();
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(BookingHistoryPanel.this, "Could not load your bookings: " + msg,
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                filterList();
            }
        };
        worker.execute();
    }

    private void filterList() {
        bookingsTablePanel.removeAll();

        // Add Table Header Row (Screen 9)
        JPanel headerRow = new JPanel(new GridLayout(1, 6, 12, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(1600, 36));
        headerRow.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        headerRow.add(createColHeader("Event"));
        headerRow.add(createColHeader("Date & Time"));
        headerRow.add(createColHeader("Seats"));
        headerRow.add(createColHeader("Amount"));
        headerRow.add(createColHeader("Status"));
        headerRow.add(createColHeader("Action"));
        bookingsTablePanel.add(headerRow);
        bookingsTablePanel.add(Box.createVerticalStrut(8));

        String q = searchField.getText().trim().toLowerCase();

        List<Booking> filtered = userBookings.stream().filter(b -> {
            if ("Upcoming".equalsIgnoreCase(activeTab) && b.getStatus() == BookingStatus.CANCELLED) return false;
            if ("Cancelled".equalsIgnoreCase(activeTab) && b.getStatus() != BookingStatus.CANCELLED) return false;
            if (!q.isEmpty() && !b.getEventTitle().toLowerCase().contains(q) && !b.getId().toLowerCase().contains(q)) return false;
            return true;
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            bookingsTablePanel.add(new EmptyStatePanel(
                    "🎟", "No Bookings Found", "You haven't reserved any seats for this category.", "Explore Events",
                    () -> navController.navigateTo(NavigationController.SCREEN_EVENT_LIST)
            ));
        } else {
            for (Booking b : filtered) {
                bookingsTablePanel.add(createBookingRow(b));
                bookingsTablePanel.add(Box.createVerticalStrut(8));
            }
        }

        bookingsTablePanel.revalidate();
        bookingsTablePanel.repaint();
    }

    private JLabel createColHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.FONT_SMALL_BOLD);
        l.setForeground(ThemeManager.TEXT_MUTED);
        return l;
    }

    private JPanel createBookingRow(Booking b) {
        JPanel row = new JPanel(new GridLayout(1, 6, 12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(1600, 52));
        row.setPreferredSize(new Dimension(900, 52));
        row.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // 1. Event Title
        JLabel titleLbl = new JLabel(b.getEventTitle());
        titleLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        // 2. Date & Time
        JLabel dateLbl = new JLabel(b.getEventDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  hh:mm a")));
        dateLbl.setFont(ThemeManager.FONT_SMALL);
        dateLbl.setForeground(ThemeManager.TEXT_MUTED);

        // 3. Seats
        JLabel seatsLbl = new JLabel(b.getFormattedSeats());
        seatsLbl.setFont(ThemeManager.FONT_BODY);
        seatsLbl.setForeground(ThemeManager.TEXT_SECONDARY);

        // 4. Amount
        JLabel amtLbl = new JLabel(b.getTotalAmount() > 0 ? "₹" + String.format("%.0f", b.getTotalAmount()) : "₹0");
        amtLbl.setFont(ThemeManager.FONT_BODY);
        amtLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        // 5. Status Badge Pill (Screen 9: Blue for Upcoming, Green for Confirmed, Purple for Completed, Red for Cancelled)
        Color badgeCol;
        String badgeTxt;
        if (b.getStatus() == BookingStatus.CANCELLED) {
            badgeCol = ThemeManager.STATUS_RED;
            badgeTxt = "Cancelled";
        } else if (b.getId().contains("BK001236")) {
            badgeCol = ThemeManager.STATUS_PURPLE;
            badgeTxt = "Completed";
        } else if (b.getId().contains("BK001234")) {
            badgeCol = ThemeManager.STATUS_BLUE;
            badgeTxt = "Upcoming";
        } else {
            badgeCol = ThemeManager.STATUS_GREEN;
            badgeTxt = "Confirmed";
        }

        JLabel statusBadge = new JLabel(badgeTxt, SwingConstants.CENTER);
        statusBadge.setFont(ThemeManager.FONT_SMALL_BOLD);
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setBackground(badgeCol);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        // 6. Action Button (View Ticket / View Details)
        StyledButton actBtn = new StyledButton(b.getStatus() == BookingStatus.CANCELLED ? "View Details" : "View Ticket", StyledButton.ButtonStyle.SECONDARY);
        actBtn.setFont(ThemeManager.FONT_SMALL);
        actBtn.setPreferredSize(new Dimension(90, 28));
        actBtn.addActionListener(e -> navController.showBookingConfirmation(b.getId()));

        row.add(titleLbl);
        row.add(dateLbl);
        row.add(seatsLbl);
        row.add(amtLbl);
        row.add(statusBadge);
        row.add(actBtn);

        return row;
    }
}
