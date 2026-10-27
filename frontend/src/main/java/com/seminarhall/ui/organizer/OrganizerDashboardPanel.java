package com.seminarhall.ui.organizer;

import com.seminarhall.model.Booking;
import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.model.OrganizerAnalytics;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.NavigationController;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.MetricCard;
import com.seminarhall.ui.components.SidebarNavigation;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Faculty Console (web OrganizerDashboardPage, ManageEventsPage, CreateEventPage). The dashboard shows
 * the console sidebar and the "Auditorium Analytics" tiles and charts from the real services; Manage
 * Events and the Schedule form are full-width pages of their own, as on the web.
 */
public class OrganizerDashboardPanel extends JPanel implements NavigationController.RefreshableScreen {
    private static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";
    private static final String VIEW_MANAGE = "VIEW_MANAGE";
    private static final String VIEW_CREATE = "VIEW_CREATE";

    private final NavigationController navController;
    private final EventService eventService;
    private final BookingService bookingService;

    private final CardLayout views = new CardLayout();
    private final JPanel viewHolder = new JPanel(views);
    private SidebarNavigation sidebar;
    private JScrollPane dashboardScroll;
    private final ConsoleUi.Banner dashboardError = new ConsoleUi.Banner();

    private final MetricCard totalEventsCard = new MetricCard("Total Events", Color.WHITE);
    private final MetricCard upcomingCard = new MetricCard("Upcoming", ThemeManager.EMERALD_400);
    private final MetricCard totalBookingsCard = new MetricCard("Total Bookings", ThemeManager.BRAND_RED);
    private final MetricCard availableSeatsCard = new MetricCard("Available Seats", ThemeManager.BLUE_400);
    private final DashboardCharts.BookingsBarChart bookingsChart = new DashboardCharts.BookingsBarChart();
    private final DashboardCharts.CategoryDonutChart categoryChart = new DashboardCharts.CategoryDonutChart();

    private final ManageEventsView manageView;
    private final CreateEventView createView;

    private int loadGeneration;

    public OrganizerDashboardPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();
        this.bookingService = ServiceFactory.getBookingService();

        setName(NavigationController.SCREEN_ORGANIZER_DASHBOARD);
        setOpaque(true);
        setBackground(ThemeManager.BG_PAGE);
        setLayout(new BorderLayout());

        manageView = new ManageEventsView(new ManageEventsView.Actions() {
            @Override public void showDashboard() { showDashboardView(); }
            @Override public void showCreate() { showCreateView(); }
            @Override public void edit(Event event) { showEditView(event); }
            @Override public void delete(Event event) { deleteEvent(event); }
        });
        createView = new CreateEventView(eventService, new CreateEventView.Listener() {
            @Override
            public void onCancel() {
                showManageView();   // web: onCancel → MANAGE_EVENTS
            }

            @Override
            public void onSaved(Event saved, boolean updated) {
                showManageView();
                manageView.showSuccess(updated ? "Event updated successfully!" : "Event published successfully!");
                refreshData();
            }
        });

        viewHolder.setOpaque(false);
        viewHolder.add(buildDashboardView(), VIEW_DASHBOARD);
        viewHolder.add(manageView, VIEW_MANAGE);
        viewHolder.add(createView, VIEW_CREATE);
        add(viewHolder, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------ dashboard

    /** "max-w-7xl mx-auto px-8 py-8 flex flex-row gap-8": sidebar + analytics column. */
    private JComponent buildDashboardView() {
        sidebar = new SidebarNavigation(navController, section -> {
            if (SidebarNavigation.SECTION_MANAGE.equals(section)) {
                showManageView();
            } else if (SidebarNavigation.SECTION_CREATE.equals(section)) {
                showCreateView();
            } else {
                showDashboardView();
            }
        });

        // heading row: title + "+ Schedule New Event"
        JPanel titleBlock = new JPanel(new ConsoleUi.Stack(0));
        titleBlock.setOpaque(false);
        titleBlock.add(ConsoleUi.text("Auditorium Analytics", ConsoleUi.font(Weight.EXTRABOLD, 24), Color.WHITE, 32));
        titleBlock.add(ConsoleUi.text("Real-time occupancy, student reservations & ticket telemetry",
                ConsoleUi.font(Weight.REGULAR, 12), ThemeManager.TEXT_SECONDARY, 16));
        StyledButton schedule = ConsoleUi.primaryButton("+ Schedule New Event", 20);
        schedule.addActionListener(e -> showCreateView());
        ConsoleUi.GlowRow heading = new ConsoleUi.GlowRow(titleBlock, schedule, false, 12);

        // "grid grid-cols-2 lg:grid-cols-4 gap-4"
        JPanel metrics = new JPanel(new GridLayout(1, 4, 16, 0));
        metrics.setOpaque(false);
        metrics.add(totalEventsCard);
        metrics.add(upcomingCard);
        metrics.add(totalBookingsCard);
        metrics.add(availableSeatsCard);

        // "grid lg:grid-cols-[1.5fr_1fr] gap-6"
        JPanel charts = new JPanel(new ConsoleUi.Columns(24, 1.5, 1.0));
        charts.setOpaque(false);

        ConsoleUi.Card bookingsCard = new ConsoleUi.Card(ThemeManager.RADIUS_2XL, new ConsoleUi.Stack(0));
        bookingsCard.setBorder(new EmptyBorder(25, 25, 25, 25));
        bookingsCard.add(ConsoleUi.text("Event Bookings (Last 6 Months)", ConsoleUi.font(Weight.BOLD, 14), Color.WHITE, 20));
        bookingsCard.add(Box.createVerticalStrut(24));   // mb-6
        bookingsCard.add(bookingsChart);

        ConsoleUi.Card categoryCard = new ConsoleUi.Card(ThemeManager.RADIUS_2XL, new BorderLayout(0, 16));
        categoryCard.setBorder(new EmptyBorder(25, 25, 25, 25));
        categoryCard.add(ConsoleUi.text("Events by Category", ConsoleUi.font(Weight.BOLD, 14), Color.WHITE, 20),
                BorderLayout.NORTH);
        categoryCard.add(categoryChart, BorderLayout.CENTER);

        charts.add(bookingsCard);
        charts.add(categoryCard);

        dashboardError.setVisible(false);
        DashboardBody body = new DashboardBody(sidebar, heading, dashboardError, metrics, charts);
        dashboardScroll = ConsoleUi.scrollPage(body, 1280, 32, 32);
        return dashboardScroll;
    }

    /**
     * "flex flex-row gap-8": the sidebar (w-60, as tall as the row) beside the "flex-1 space-y-6" column.
     * Its bounds reach a little past the content box so the button glow is not cut off.
     */
    private static final class DashboardBody extends JPanel {
        private static final int SIDEBAR = 240;
        private static final int GAP_X = 32;
        private static final int GAP_Y = 24;

        private final JComponent sidebar;
        private final JComponent[] column;

        DashboardBody(JComponent sidebar, JComponent... column) {
            super(null);
            this.sidebar = sidebar;
            this.column = column;
            setOpaque(false);
            add(sidebar);
            for (JComponent c : column) {
                add(c);
            }
        }

        private int columnHeight() {
            int h = 0;
            int count = 0;
            for (JComponent c : column) {
                if (!c.isVisible()) continue;
                h += c.getPreferredSize().height;
                count++;
            }
            return h + Math.max(0, count - 1) * GAP_Y;
        }

        @Override
        public Dimension getPreferredSize() {
            int w = 0;
            for (JComponent c : column) {
                w = Math.max(w, c.getPreferredSize().width);
            }
            return new Dimension(SIDEBAR + GAP_X + w, Math.max(sidebar.getPreferredSize().height, columnHeight()));
        }

        @Override
        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int w, int h) {
            super.reshape(x, y, w + ConsoleUi.GlowRow.GLOW_X, h + ConsoleUi.GlowRow.GLOW_BOTTOM);
        }

        @Override
        public void doLayout() {
            int w = getWidth() - ConsoleUi.GlowRow.GLOW_X;
            int h = getHeight() - ConsoleUi.GlowRow.GLOW_BOTTOM;
            sidebar.setBounds(0, 0, SIDEBAR, h);
            int x = SIDEBAR + GAP_X;
            int y = 0;
            for (JComponent c : column) {
                if (!c.isVisible()) continue;
                int ch = c.getPreferredSize().height;
                c.setBounds(x, y, w - x, ch);
                y += ch + GAP_Y;
            }
        }
    }

    // ------------------------------------------------------------------ views

    private void showDashboardView() {
        sidebar.setActiveSection(SidebarNavigation.SECTION_DASHBOARD);
        views.show(viewHolder, VIEW_DASHBOARD);
        SwingUtilities.invokeLater(() -> dashboardScroll.getVerticalScrollBar().setValue(0));
    }

    private void showManageView() {
        manageView.clearNotice();
        views.show(viewHolder, VIEW_MANAGE);
        manageView.scrollToTop();
        refreshData();
    }

    private void showCreateView() {
        createView.startNew();
        views.show(viewHolder, VIEW_CREATE);
    }

    private void showEditView(Event event) {
        createView.startEdit(event);
        views.show(viewHolder, VIEW_CREATE);
    }

    @Override
    public void onScreenDisplayed() {
        manageView.clearNotice();
        showDashboardView();
        refreshData();
    }

    // ------------------------------------------------------------------ data

    /** Everything the console shows, loaded together off the EDT. */
    private static final class Snapshot {
        OrganizerAnalytics analytics;
        String analyticsError;
        List<Event> events;
        String eventsError;
        List<Booking> bookings = new ArrayList<>();
    }

    private void refreshData() {
        int generation = ++loadGeneration;
        new SwingWorker<Snapshot, Void>() {
            @Override
            protected Snapshot doInBackground() {
                Snapshot s = new Snapshot();
                try {
                    s.analytics = bookingService.getOrganizerAnalytics();
                    s.bookings = bookingService.getAllBookings();
                } catch (Exception ex) {
                    s.analyticsError = message(ex);
                }
                try {
                    s.events = eventService.getAllEvents();
                } catch (Exception ex) {
                    s.eventsError = message(ex);
                }
                return s;
            }

            @Override
            protected void done() {
                if (generation != loadGeneration) return;   // a newer load is on its way
                try {
                    apply(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    dashboardError.error("Could not load analytics: " + message(ex.getCause()));
                    dashboardError.setVisible(true);
                }
            }
        }.execute();
    }

    private void apply(Snapshot s) {
        if (s.analytics != null) {
            totalEventsCard.setValue(String.format("%,d", s.analytics.getTotalEvents()));
            upcomingCard.setValue(String.format("%,d", s.analytics.getUpcomingEvents()));
            totalBookingsCard.setValue(String.format("%,d", s.analytics.getTotalBookings()));
            availableSeatsCard.setValue(String.format("%,d", s.analytics.getAvailableSeats()));
            bookingsChart.setData(DashboardCharts.BookingsBarChart.lastSixMonths(), bookingsPerMonth(s.bookings));
            dashboardError.clear();
        } else {
            dashboardError.error("Could not load analytics: " + s.analyticsError);
        }

        if (s.events != null) {
            List<Event> events = new ArrayList<>(s.events);
            events.sort(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Event::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
            manageView.setEvents(events);
            createView.setKnownEvents(events);
            categoryChart.setSlices(categorySlices(events));
        } else {
            manageView.showLoadError(s.eventsError);
        }
        revalidate();
        repaint();
    }

    /** Bookings made in each of the last six months (oldest first). */
    private static int[] bookingsPerMonth(List<Booking> bookings) {
        List<YearMonth> months = DashboardCharts.BookingsBarChart.lastSixMonths();
        int[] counts = new int[months.size()];
        for (Booking booking : bookings) {
            if (booking.getBookingTime() == null) continue;
            int index = months.indexOf(YearMonth.from(booking.getBookingTime()));
            if (index >= 0) counts[index]++;
        }
        return counts;
    }

    /** Share of scheduled (not cancelled) events per category, largest first, in the web's colors. */
    private static List<DashboardCharts.CategoryDonutChart.Slice> categorySlices(List<Event> events) {
        Map<EventCategory, Integer> counts = new EnumMap<>(EventCategory.class);
        for (Event event : events) {
            if (!event.isCancelled() && event.getCategory() != null) {
                counts.merge(event.getCategory(), 1, Integer::sum);
            }
        }
        List<EventCategory> order = new ArrayList<>(counts.keySet());
        order.sort(Comparator.comparing((EventCategory c) -> -counts.get(c))
                .thenComparing(c -> EventVisuals.CHIP_ORDER.indexOf(c)));
        List<DashboardCharts.CategoryDonutChart.Slice> slices = new ArrayList<>();
        for (EventCategory category : order) {
            slices.add(new DashboardCharts.CategoryDonutChart.Slice(
                    CreateEventView.categoryName(category), counts.get(category), categoryColor(category)));
        }
        return slices;
    }

    /** Legend dots: Technical #ff382e, Workshop blue-500, Cultural emerald-500, Seminar amber-500. */
    private static Color categoryColor(EventCategory category) {
        switch (category) {
            case WORKSHOP: return ConsoleUi.BLUE_500;
            case CULTURAL: return ConsoleUi.EMERALD_500;
            case GUEST_LECTURE: return ConsoleUi.AMBER_500;
            case HACKATHON: return ConsoleUi.PURPLE_500;
            case SPORTS: return ConsoleUi.SKY_500;
            default: return ThemeManager.BRAND_RED;
        }
    }

    // ------------------------------------------------------------------ delete

    private void deleteEvent(Event event) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + event.getTitle() + "\"? Students will no longer be able to book it.",
                "Delete Event", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        manageView.clearNotice();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                eventService.deleteEvent(event.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    manageView.showSuccess("\"" + event.getTitle() + "\" was deleted and is now marked Cancelled.");
                    refreshData();
                } catch (ExecutionException ex) {
                    manageView.showError(message(ex.getCause()));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private static String message(Throwable t) {
        if (t == null) return "Unknown error";
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }
}
