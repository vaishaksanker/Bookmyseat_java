package com.seminarhall.ui.organizer;

import com.seminarhall.model.*;
import com.seminarhall.model.Event;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.NavigationController;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.StyledTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrganizerDashboardPanel extends JPanel implements NavigationController.RefreshableScreen {
    private final NavigationController navController;
    private final EventService eventService;
    private final BookingService bookingService;

    private JPanel dynamicContentArea;
    private CardLayout contentCardLayout;

    private static final String CARD_DASHBOARD = "CARD_DASHBOARD";
    private static final String CARD_MANAGE_EVENTS = "CARD_MANAGE_EVENTS";
    private static final String CARD_CREATE_EVENT = "CARD_CREATE_EVENT";
    private static final String CARD_BOOKINGS = "CARD_BOOKINGS";

    // KPI labels
    private JLabel totalEventsVal;
    private JLabel upcomingVal;
    private JLabel totalBookingsVal;
    private JLabel availableSeatsVal;

    // Tables
    private DefaultTableModel recentBookingsModel;
    private JTable recentBookingsTable;
    private DefaultTableModel manageEventsModel;
    private JTable manageEventsTable;

    // Create Event inputs
    private StyledTextField titleInput;
    private JComboBox<EventCategory> catCombo;
    private StyledTextField descInput;
    private StyledTextField dateInput;
    private StyledTextField startInput;
    private StyledTextField endInput;
    private StyledTextField venueInput;
    private StyledTextField hallInput;
    private StyledTextField capacityInput;
    private StyledTextField priceInput;

    public OrganizerDashboardPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();
        this.bookingService = ServiceFactory.getBookingService();

        setName(NavigationController.SCREEN_ORGANIZER_DASHBOARD);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Left Organizer Sidebar matching Screens 10, 11, 12
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ThemeManager.BG_DARKEST);
        sidebar.setPreferredSize(new Dimension(210, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));

        JPanel topSide = new JPanel();
        topSide.setOpaque(false);
        topSide.setLayout(new BoxLayout(topSide, BoxLayout.Y_AXIS));

        // SeminarX Logo & Brand
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandBox.setOpaque(false);
        JLabel sxIcon = new JLabel("SX") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, ThemeManager.ACCENT_RED, getWidth(), getHeight(), ThemeManager.ACCENT_ORANGE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(ThemeManager.FONT_SMALL_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("SX", (getWidth() - fm.stringWidth("SX")) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        sxIcon.setPreferredSize(new Dimension(24, 24));
        sxIcon.setOpaque(false);

        JLabel brandLbl = new JLabel("SeminarX");
        brandLbl.setFont(ThemeManager.FONT_HEADER);
        brandLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        brandBox.add(sxIcon);
        brandBox.add(brandLbl);
        topSide.add(brandBox);
        topSide.add(Box.createVerticalStrut(24));

        // Nav Menu items: Dashboard, Manage Events, Create Event, Bookings, Attendees, Analytics
        JButton dashBtn = createSidebarItem("📊  Dashboard", true, () -> contentCardLayout.show(dynamicContentArea, CARD_DASHBOARD));
        JButton manageBtn = createSidebarItem("🎪  Manage Events", false, () -> {
            loadManageEvents();
            contentCardLayout.show(dynamicContentArea, CARD_MANAGE_EVENTS);
        });
        JButton createBtn = createSidebarItem("➕  Create Event", false, () -> contentCardLayout.show(dynamicContentArea, CARD_CREATE_EVENT));
        JButton bookingsBtn = createSidebarItem("📑  Bookings", false, () -> contentCardLayout.show(dynamicContentArea, CARD_BOOKINGS));

        topSide.add(dashBtn);
        topSide.add(Box.createVerticalStrut(4));
        topSide.add(manageBtn);
        topSide.add(Box.createVerticalStrut(4));
        topSide.add(createBtn);
        topSide.add(Box.createVerticalStrut(4));
        topSide.add(bookingsBtn);

        sidebar.add(topSide, BorderLayout.NORTH);

        // Bottom User profile + Logout
        JPanel botSide = new JPanel();
        botSide.setOpaque(false);
        botSide.setLayout(new BoxLayout(botSide, BoxLayout.Y_AXIS));

        JLabel orgUser = new JLabel("👤  Organizer");
        orgUser.setFont(ThemeManager.FONT_BODY_BOLD);
        orgUser.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel facTitle = new JLabel("Faculty Coordinator");
        facTitle.setFont(ThemeManager.FONT_SMALL);
        facTitle.setForeground(ThemeManager.TEXT_MUTED);

        StyledButton logoutBtn = new StyledButton("Sign Out", StyledButton.ButtonStyle.SECONDARY);
        logoutBtn.setMaximumSize(new Dimension(180, 32));
        logoutBtn.addActionListener(e -> navController.logout());

        botSide.add(orgUser);
        botSide.add(facTitle);
        botSide.add(Box.createVerticalStrut(10));
        botSide.add(logoutBtn);

        sidebar.add(botSide, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);

        // Center Content Area
        contentCardLayout = new CardLayout();
        dynamicContentArea = new JPanel(contentCardLayout);
        dynamicContentArea.setOpaque(false);

        dynamicContentArea.add(buildDashboardPanel(), CARD_DASHBOARD);
        dynamicContentArea.add(buildManageEventsPanel(), CARD_MANAGE_EVENTS);
        dynamicContentArea.add(buildCreateEventPanel(), CARD_CREATE_EVENT);
        dynamicContentArea.add(buildBookingsPanel(), CARD_BOOKINGS);

        add(dynamicContentArea, BorderLayout.CENTER);
    }

    private JButton createSidebarItem(String title, boolean active, Runnable action) {
        JButton btn = new JButton(title);
        btn.setFont(ThemeManager.FONT_BODY);
        btn.setForeground(active ? Color.WHITE : ThemeManager.TEXT_SECONDARY);
        btn.setBackground(active ? ThemeManager.ACCENT_RED : new Color(0, 0, 0, 0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(active);
        btn.setMaximumSize(new Dimension(180, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // -------------------------------------------------------------
    // SCREEN 10: ORGANIZER DASHBOARD (KPIS, CHARTS, RECENT BOOKINGS)
    // -------------------------------------------------------------
    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Dashboard");
        title.setFont(ThemeManager.FONT_HEADER_XL);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel sub = new JLabel("Overview of your events and bookings");
        sub.setFont(ThemeManager.FONT_BODY);
        sub.setForeground(ThemeManager.TEXT_MUTED);

        p.add(title);
        p.add(Box.createVerticalStrut(2));
        p.add(sub);
        p.add(Box.createVerticalStrut(18));

        // 4 KPI Cards (Screen 10: 12 Total Events, 5 Upcoming, 320 Total Bookings, 450 Available Seats)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(1600, 80));

        totalEventsVal = new JLabel("12");
        upcomingVal = new JLabel("5");
        totalBookingsVal = new JLabel("320");
        availableSeatsVal = new JLabel("450");

        kpiGrid.add(createMiniKpiCard("📅", "12", "Total Events", ThemeManager.STATUS_BLUE));
        kpiGrid.add(createMiniKpiCard("⚡", "5", "Upcoming", ThemeManager.STATUS_GREEN));
        kpiGrid.add(createMiniKpiCard("🎟", "320", "Total Bookings", ThemeManager.ACCENT_RED));
        kpiGrid.add(createMiniKpiCard("💺", "450", "Available Seats", ThemeManager.STATUS_GREEN));

        p.add(kpiGrid);
        p.add(Box.createVerticalStrut(20));

        // Visual Charts Mock: Left Event Bookings Bar Chart, Right Events by Category Donut Chart
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(1600, 180));

        // Bar Chart
        JPanel barChart = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Title
                g2.setColor(ThemeManager.TEXT_PRIMARY);
                g2.setFont(ThemeManager.FONT_BODY_BOLD);
                g2.drawString("Event Bookings (Last 6 Months)", 16, 26);

                // Bars
                int[] heights = {40, 65, 50, 85, 70, 95};
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
                int startX = 24;
                int barW = 20;
                int baseY = getHeight() - 36;
                for (int i = 0; i < heights.length; i++) {
                    g2.setColor(i % 2 == 0 ? ThemeManager.STATUS_BLUE : ThemeManager.STATUS_PURPLE);
                    g2.fillRoundRect(startX + (i * 54), baseY - heights[i], barW, heights[i], 4, 4);
                    g2.setColor(ThemeManager.TEXT_MUTED);
                    g2.setFont(ThemeManager.FONT_SMALL);
                    g2.drawString(months[i], startX + (i * 54), baseY + 18);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        barChart.setOpaque(false);

        // Donut Chart
        JPanel donutChart = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2.setColor(ThemeManager.TEXT_PRIMARY);
                g2.setFont(ThemeManager.FONT_BODY_BOLD);
                g2.drawString("Events by Category", 16, 26);

                // Donut arc slices
                int cx = 80;
                int cy = 100;
                int r = 50;
                g2.setColor(ThemeManager.STATUS_BLUE);
                g2.fillArc(cx - r, cy - r, r * 2, r * 2, 0, 140);
                g2.setColor(ThemeManager.STATUS_ORANGE);
                g2.fillArc(cx - r, cy - r, r * 2, r * 2, 140, 100);
                g2.setColor(ThemeManager.STATUS_PURPLE);
                g2.fillArc(cx - r, cy - r, r * 2, r * 2, 240, 120);

                // Cut donut hole
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillOval(cx - 25, cy - 25, 50, 50);

                // Center percentage
                g2.setColor(ThemeManager.TEXT_PRIMARY);
                g2.setFont(ThemeManager.FONT_BODY_BOLD);
                g2.drawString("52%", cx - 12, cy + 5);

                // Legend on right
                int legX = 180;
                drawLeg(g2, legX, 70, ThemeManager.STATUS_BLUE, "Technical (52%)");
                drawLeg(g2, legX, 95, ThemeManager.STATUS_ORANGE, "Workshop (28%)");
                drawLeg(g2, legX, 120, ThemeManager.STATUS_PURPLE, "Cultural (20%)");

                g2.dispose();
                super.paintComponent(g);
            }

            private void drawLeg(Graphics2D g2, int x, int y, Color c, String lbl) {
                g2.setColor(c);
                g2.fillOval(x, y - 8, 8, 8);
                g2.setColor(ThemeManager.TEXT_SECONDARY);
                g2.setFont(ThemeManager.FONT_SMALL);
                g2.drawString(lbl, x + 14, y);
            }
        };
        donutChart.setOpaque(false);

        chartsRow.add(barChart);
        chartsRow.add(donutChart);
        p.add(chartsRow);
        p.add(Box.createVerticalStrut(20));

        // Recent Bookings Table matching Screen 10
        JLabel recTitle = new JLabel("Recent Bookings");
        recTitle.setFont(ThemeManager.FONT_HEADER);
        recTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        p.add(recTitle);
        p.add(Box.createVerticalStrut(8));

        String[] cols = {"Event", "Student", "Seats", "Status", "Time"};
        recentBookingsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentBookingsTable = new JTable(recentBookingsModel);
        recentBookingsTable.setRowHeight(30);
        recentBookingsTable.setBackground(ThemeManager.BG_CARD);
        recentBookingsTable.setForeground(ThemeManager.TEXT_PRIMARY);
        recentBookingsTable.getTableHeader().setBackground(ThemeManager.BG_SECONDARY);
        recentBookingsTable.getTableHeader().setForeground(ThemeManager.TEXT_MUTED);

        JScrollPane recScroll = new JScrollPane(recentBookingsTable);
        recScroll.setOpaque(false);
        recScroll.getViewport().setOpaque(false);
        recScroll.setMaximumSize(new Dimension(1600, 160));
        recScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_CARD));
        p.add(recScroll);

        JScrollPane mainScroll = new JScrollPane(p);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(mainScroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMiniKpiCard(String icon, String val, String title, Color c) {
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
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
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel vLbl = new JLabel(val);
        vLbl.setFont(ThemeManager.FONT_HEADER);
        vLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(ThemeManager.FONT_SMALL);
        tLbl.setForeground(ThemeManager.TEXT_MUTED);

        texts.add(vLbl);
        texts.add(tLbl);

        p.add(iconLbl, BorderLayout.WEST);
        p.add(texts, BorderLayout.CENTER);
        return p;
    }

    // -------------------------------------------------------------
    // SCREEN 11: CREATE NEW EVENT FORM
    // -------------------------------------------------------------
    private JPanel buildCreateEventPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Create New Event");
        title.setFont(ThemeManager.FONT_HEADER_XL);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        p.add(title);
        p.add(Box.createVerticalStrut(18));

        titleInput = new StyledTextField("Event Title (e.g. AI in Modern World)");
        catCombo = new JComboBox<>(EventCategory.values());
        catCombo.setFont(ThemeManager.FONT_BODY);
        catCombo.setBackground(ThemeManager.BG_CARD);
        catCombo.setForeground(ThemeManager.TEXT_PRIMARY);

        descInput = new StyledTextField("Enter event description");
        dateInput = new StyledTextField("YYYY-MM-DD (e.g. 2026-12-10)");
        startInput = new StyledTextField("10:00 AM");
        endInput = new StyledTextField("12:00 PM");
        venueInput = new StyledTextField("Computer Science Department");
        hallInput = new StyledTextField("Seminar Hall A");
        capacityInput = new StyledTextField("100");
        priceInput = new StyledTextField("0");

        p.add(createFormRow("Event Title", titleInput));
        p.add(createFormRow("Category", catCombo));
        p.add(createFormRow("Description", descInput));
        p.add(createFormRow("Date", dateInput));
        p.add(createFormRow("Start Time", startInput));
        p.add(createFormRow("End Time", endInput));
        p.add(createFormRow("Department / Block", venueInput));
        p.add(createFormRow("Seminar Hall", hallInput));
        p.add(createFormRow("Total Seats", capacityInput));
        p.add(createFormRow("Ticket Price (₹)", priceInput));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        StyledButton cancelBtn = new StyledButton("Cancel", StyledButton.ButtonStyle.SECONDARY);
        cancelBtn.addActionListener(e -> contentCardLayout.show(dynamicContentArea, CARD_MANAGE_EVENTS));

        StyledButton createBtn = new StyledButton("Create Event", StyledButton.ButtonStyle.PRIMARY);
        createBtn.addActionListener(e -> performCreateEvent());

        btnRow.add(cancelBtn);
        btnRow.add(createBtn);

        p.add(Box.createVerticalStrut(12));
        p.add(btnRow);

        JScrollPane scroll = new JScrollPane(p);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel r = new JPanel(new BorderLayout(14, 0));
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(800, 38));

        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(140, 34));
        l.setFont(ThemeManager.FONT_BODY_BOLD);
        l.setForeground(ThemeManager.TEXT_SECONDARY);

        r.add(l, BorderLayout.WEST);
        r.add(field, BorderLayout.CENTER);
        return r;
    }

    private void performCreateEvent() {
        try {
            Event ev = new Event();
            ev.setTitle(titleInput.getText().trim());
            ev.setCategory((EventCategory) catCombo.getSelectedItem());
            ev.setDescription(descInput.getText().trim());
            ev.setDate(LocalDate.parse(dateInput.getText().trim().substring(0, 10)));
            ev.setStartTime(LocalTime.of(10, 0));
            ev.setEndTime(LocalTime.of(12, 0));
            ev.setVenue(venueInput.getText().trim());
            ev.setSeminarHall(hallInput.getText().trim());
            ev.setTotalCapacity(Integer.parseInt(capacityInput.getText().trim()));
            ev.setTicketPrice(Double.parseDouble(priceInput.getText().trim()));
            ev.setOrganizerName("Faculty Coordinator");

            eventService.createEvent(ev);
            JOptionPane.showMessageDialog(this, "Event published successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadManageEvents();
            contentCardLayout.show(dynamicContentArea, CARD_MANAGE_EVENTS);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving event: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------
    // SCREEN 12: MANAGE EVENTS TABLE
    // -------------------------------------------------------------
    private JPanel buildManageEventsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Manage Events");
        title.setFont(ThemeManager.FONT_HEADER_XL);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        StyledButton newBtn = new StyledButton("+ Create Event", StyledButton.ButtonStyle.PRIMARY);
        newBtn.addActionListener(e -> contentCardLayout.show(dynamicContentArea, CARD_CREATE_EVENT));

        top.add(title, BorderLayout.WEST);
        top.add(newBtn, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"Event", "Date", "Venue", "Seats", "Status", "Action"};
        manageEventsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        manageEventsTable = new JTable(manageEventsModel);
        manageEventsTable.setRowHeight(36);
        manageEventsTable.setBackground(ThemeManager.BG_CARD);
        manageEventsTable.setForeground(ThemeManager.TEXT_PRIMARY);
        manageEventsTable.getTableHeader().setBackground(ThemeManager.BG_SECONDARY);
        manageEventsTable.getTableHeader().setForeground(ThemeManager.TEXT_MUTED);

        JScrollPane scroll = new JScrollPane(manageEventsTable);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_CARD));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private void loadManageEvents() {
        manageEventsModel.setRowCount(0);
        SwingWorker<List<Event>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                return eventService.getAllEvents();
            }

            @Override
            protected void done() {
                try {
                    List<Event> list = get();
                    for (Event ev : list) {
                        manageEventsModel.addRow(new Object[]{
                                ev.getTitle(),
                                ev.getDate().toString(),
                                ev.getSeminarHall(),
                                ev.getTotalCapacity(),
                                ev.isCancelled() ? "Cancelled" : "Upcoming",
                                "✏️ Edit / 🗑️ Delete"
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private JPanel buildBookingsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        JLabel title = new JLabel("All Student Bookings");
        title.setFont(ThemeManager.FONT_HEADER_XL);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        p.add(title, BorderLayout.NORTH);
        return p;
    }

    @Override
    public void onScreenDisplayed() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        recentBookingsModel.setRowCount(0);
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.getAllBookings();
            }

            @Override
            protected void done() {
                try {
                    List<Booking> list = get();
                    for (Booking b : list) {
                        recentBookingsModel.addRow(new Object[]{
                                b.getEventTitle(),
                                b.getUserName(),
                                b.getFormattedSeats(),
                                b.getStatus().name(),
                                "Just now"
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
