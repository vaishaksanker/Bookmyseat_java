package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.components.ErrorPanel;
import com.seminarhall.ui.components.EventGrid;
import com.seminarhall.ui.components.FeaturedEventPanel;
import com.seminarhall.ui.components.LoadingPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Student home, the web app's HomePage: the "IDEAS / PEOPLE / POSSIBILITIES" hero banner,
 * "Upcoming Events" with the available count and category chips, and the event card grid.
 */
public class StudentDashboardPanel extends JPanel implements NavigationController.RefreshableScreen {
    private final NavigationController navController;
    private final EventService eventService;

    private final FeaturedEventPanel hero;
    private final EventGrid.SectionHeader header;
    private final EventGrid grid;

    private List<Event> upcoming = new ArrayList<>();
    private SwingWorker<List<Event>, Void> loader;

    public StudentDashboardPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();

        setName(NavigationController.SCREEN_STUDENT_DASHBOARD);
        setOpaque(true);
        setBackground(ThemeManager.BG_PAGE);
        setLayout(new BorderLayout());

        // max-w-7xl mx-auto px-8; hero section py-6, chips section mb-6, page pb-16
        EventGrid.Page page = new EventGrid.Page(1280, 32, 24, 64);

        hero = new FeaturedEventPanel(null, navController::showEventDetails);
        header = new EventGrid.SectionHeader("Upcoming Events", category -> showEvents());
        grid = new EventGrid();

        page.add(hero);
        page.add(Box.createVerticalStrut(24));
        page.add(header);
        page.add(Box.createVerticalStrut(24));
        page.add(grid);

        add(EventGrid.scroll(page), BorderLayout.CENTER);
    }

    @Override
    public void onScreenDisplayed() {
        loadDashboardContent();
    }

    private void loadDashboardContent() {
        if (loader != null && !loader.isDone()) {
            loader.cancel(true);
        }
        if (upcoming.isEmpty()) {
            header.setCountText("");
            grid.setPlaceholder(new LoadingPanel("Loading SeminarX campus events..."));
        }

        loader = new SwingWorker<>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                return eventService.getUpcomingEvents();
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    List<Event> events = get();
                    upcoming = events == null ? new ArrayList<>() : events.stream()
                            .filter(e -> !e.isCancelled())
                            .collect(Collectors.toList());
                    showEvents();
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    upcoming = new ArrayList<>();
                    header.setCountText("");
                    hero.setEvent(null);
                    grid.setPlaceholder(new ErrorPanel("Failed to load events: " + msg,
                            StudentDashboardPanel.this::loadDashboardContent));
                }
            }
        };
        loader.execute();
    }

    /** Filters the loaded events by the active chip, like the web's activeCategory filter. */
    private void showEvents() {
        EventCategory active = header.getActive();
        List<Event> shown = upcoming.stream()
                .filter(e -> active == EventCategory.ALL || e.getCategory() == active)
                .collect(Collectors.toList());

        header.setCountText(shown.size() + " Available");
        hero.setEvent(!shown.isEmpty() ? shown.get(0) : (!upcoming.isEmpty() ? upcoming.get(0) : null));

        JComponent empty = upcoming.isEmpty()
                ? EventGrid.emptyView("No Events Scheduled", "New seminars and hackathons will appear here.",
                        "Refresh Listings", this::loadDashboardContent)
                : EventGrid.emptyView("No Events in This Category", "Try another category to see more events.",
                        "Show All Events", () -> {
                            header.setActive(EventCategory.ALL);
                            showEvents();
                        });
        grid.setEvents(shown, navController::showEventDetails, navController::startSeatSelection, empty);
    }
}
