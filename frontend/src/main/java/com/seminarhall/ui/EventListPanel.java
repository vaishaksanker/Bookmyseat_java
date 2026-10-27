package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.components.ErrorPanel;
import com.seminarhall.ui.components.EventGrid;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.LoadingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Explore / search results: the home page's "Upcoming Events" header, category chips and card
 * grid without the hero. Text typed in the header search arrives via
 * {@link NavigationController#consumeSearchQuery()} and shows "Results for ‘query’" with a
 * "Back to All Events" button; with no query it lists every upcoming event.
 */
public class EventListPanel extends JPanel implements NavigationController.RefreshableScreen {
    private final NavigationController navController;
    private final EventService eventService;

    private final JPanel backRow;
    private final EventGrid.SectionHeader header;
    private final EventGrid grid;

    private String query;
    private List<Event> results = new ArrayList<>();
    private SwingWorker<List<Event>, Void> loader;

    public EventListPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();

        setName(NavigationController.SCREEN_EVENT_LIST);
        setOpaque(true);
        setBackground(ThemeManager.BG_PAGE);
        setLayout(new BorderLayout());

        EventGrid.Page page = new EventGrid.Page(1280, 32, 24, 64);

        // "Back to All Events" pill (same style as the details page's "Back to Explore"), mb-6
        backRow = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 34 + 24);
            }

            @Override
            public void doLayout() {
                Component pill = getComponent(0);
                pill.setBounds(0, 0, pill.getPreferredSize().width, 34);
            }
        };
        backRow.setOpaque(false);
        backRow.add(new BackPill("Back to All Events", this::showAllEvents));

        header = new EventGrid.SectionHeader("Upcoming Events", category -> showResults());
        grid = new EventGrid();

        page.add(backRow);
        page.add(header);
        page.add(Box.createVerticalStrut(24));
        page.add(grid);

        add(EventGrid.scroll(page), BorderLayout.CENTER);
    }

    @Override
    public void onScreenDisplayed() {
        String q = navController.consumeSearchQuery();
        query = q == null || q.trim().isEmpty() ? null : q.trim();
        header.setActive(EventCategory.ALL);
        loadEvents();
    }

    private void showAllEvents() {
        query = null;
        header.setActive(EventCategory.ALL);
        loadEvents();
    }

    private void loadEvents() {
        if (loader != null && !loader.isDone()) {
            loader.cancel(true);
        }
        String q = query;
        backRow.setVisible(q != null);
        header.setTitle(q != null ? "Results for ‘" + q + "’" : "Upcoming Events");
        header.setCountText("");
        grid.setPlaceholder(new LoadingPanel(q != null ? "Searching campus events..." : "Loading SeminarX campus events..."));

        loader = new SwingWorker<>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                if (q == null) {
                    return eventService.getUpcomingEvents();
                }
                LocalDate today = LocalDate.now();
                return eventService.searchEvents(q, EventCategory.ALL, "Date (Soonest)").stream()
                        .filter(e -> e.getDate() == null || !e.getDate().isBefore(today))
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    List<Event> events = get();
                    results = events == null ? new ArrayList<>() : events.stream()
                            .filter(e -> !e.isCancelled())
                            .collect(Collectors.toList());
                    showResults();
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    results = new ArrayList<>();
                    grid.setPlaceholder(new ErrorPanel("Error loading events: " + msg, EventListPanel.this::loadEvents));
                }
            }
        };
        loader.execute();
    }

    private void showResults() {
        EventCategory active = header.getActive();
        List<Event> shown = results.stream()
                .filter(e -> active == EventCategory.ALL || e.getCategory() == active)
                .collect(Collectors.toList());

        header.setCountText(shown.size() + (query != null ? " Found" : " Available"));

        JComponent empty;
        if (query != null && results.isEmpty()) {
            empty = EventGrid.emptyView("No Matching Events Found",
                    "Nothing matches ‘" + query + "’. Try another keyword or browse every upcoming event.",
                    "Show All Events", this::showAllEvents);
        } else if (results.isEmpty()) {
            empty = EventGrid.emptyView("No Events Scheduled", "New seminars and hackathons will appear here.",
                    "Refresh Listings", this::loadEvents);
        } else {
            empty = EventGrid.emptyView("No Events in This Category", "Try another category to see more events.",
                    "Show All Categories", () -> {
                        header.setActive(EventCategory.ALL);
                        showResults();
                    });
        }
        grid.setEvents(shown, navController::showEventDetails, navController::startSeatSelection, empty);
    }

    /**
     * "bg-black/70 border border-white/10 hover:border-white/30 text-white text-xs px-4 py-2
     * rounded-xl font-semibold" with an arrow-left icon.
     */
    static final class BackPill extends JLabel {
        private boolean hovered;

        BackPill(String text, Runnable onClick) {
            super(text, Icons.get("arrow-left", 14, Color.WHITE), SwingConstants.LEFT);
            setFont(ThemeManager.font(ThemeManager.Weight.SEMIBOLD, 12));
            setForeground(Color.WHITE);
            setIconTextGap(8);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 34);           // py-2 + text-xs + border
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = ThemeManager.RADIUS_XL * 2;
            g2.setColor(new Color(0, 0, 0, 179));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(hovered ? new Color(255, 255, 255, 77) : ThemeManager.BORDER_CARD);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
