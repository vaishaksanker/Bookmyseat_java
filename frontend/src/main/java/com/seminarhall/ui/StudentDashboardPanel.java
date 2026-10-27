package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.components.EmptyStatePanel;
import com.seminarhall.ui.components.EventCard;
import com.seminarhall.ui.components.FeaturedEventPanel;
import com.seminarhall.ui.components.LoadingPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentDashboardPanel extends JPanel implements NavigationController.RefreshableScreen {
    private final NavigationController navController;
    private final EventService eventService;

    private JPanel contentContainer;
    private JScrollPane scrollPane;

    public StudentDashboardPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();

        setName(NavigationController.SCREEN_STUDENT_DASHBOARD);
        setOpaque(false);
        setLayout(new BorderLayout());

        contentContainer = new JPanel();
        contentContainer.setOpaque(false);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBorder(BorderFactory.createEmptyBorder(16, 24, 36, 24));

        scrollPane = new JScrollPane(contentContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void onScreenDisplayed() {
        loadDashboardContent();
    }

    private void loadDashboardContent() {
        contentContainer.removeAll();
        contentContainer.add(new LoadingPanel("Loading SeminarX campus events..."));
        contentContainer.revalidate();
        contentContainer.repaint();

        SwingWorker<List<Event>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                return eventService.getAllEvents();
            }

            @Override
            protected void done() {
                try {
                    List<Event> events = get();
                    renderDashboard(events);
                } catch (Exception e) {
                    contentContainer.removeAll();
                    contentContainer.add(new JLabel("Failed to load events: " + e.getMessage()));
                    contentContainer.revalidate();
                    contentContainer.repaint();
                }
            }
        };
        worker.execute();
    }

    private void renderDashboard(List<Event> events) {
        contentContainer.removeAll();

        if (events == null || events.isEmpty()) {
            contentContainer.add(new EmptyStatePanel(
                    "📅",
                    "No Events Scheduled",
                    "New seminars and hackathons will appear here.",
                    "Refresh Listings",
                    this::loadDashboardContent
            ));
            contentContainer.revalidate();
            contentContainer.repaint();
            return;
        }

        // 1. Featured Hero Banner ("IDEAS PEOPLE POSSIBILITIES")
        Event heroEv = events.get(0);
        FeaturedEventPanel hero = new FeaturedEventPanel(heroEv, e -> navController.navigateTo(NavigationController.SCREEN_EVENT_LIST));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentContainer.add(hero);
        contentContainer.add(Box.createVerticalStrut(28));

        // 2. Upcoming Events Section Header with "View All →"
        JPanel sectionHead = new JPanel(new BorderLayout());
        sectionHead.setOpaque(false);
        sectionHead.setMaximumSize(new Dimension(1600, 32));
        sectionHead.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel upLbl = new JLabel("Upcoming Events");
        upLbl.setFont(ThemeManager.FONT_HEADER);
        upLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel viewAll = new JLabel("View All →");
        viewAll.setFont(ThemeManager.FONT_BODY_BOLD);
        viewAll.setForeground(ThemeManager.ACCENT_RED);
        viewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navController.navigateTo(NavigationController.SCREEN_EVENT_LIST);
            }
        });

        sectionHead.add(upLbl, BorderLayout.WEST);
        sectionHead.add(viewAll, BorderLayout.EAST);

        contentContainer.add(sectionHead);
        contentContainer.add(Box.createVerticalStrut(14));

        // 3. Grid of event cards (AI in Modern World, Web Dev, Cultural Meet, Career Guidance, etc.)
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Event ev : events) {
            EventCard card = new EventCard(
                    ev,
                    e -> navController.showEventDetails(e),
                    e -> navController.startSeatSelection(e)
            );
            cardsRow.add(card);
        }

        contentContainer.add(cardsRow);

        contentContainer.revalidate();
        contentContainer.repaint();
    }
}
