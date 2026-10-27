package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.components.EmptyStatePanel;
import com.seminarhall.ui.components.EventCard;
import com.seminarhall.ui.components.LoadingPanel;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.StyledTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class EventListPanel extends JPanel implements NavigationController.RefreshableScreen {
    private final NavigationController navController;
    private final EventService eventService;

    private StyledTextField searchField;
    private JComboBox<EventCategory> categoryComboBox;
    private JComboBox<String> sortComboBox;
    private JPanel cardsGrid;
    private JScrollPane scrollPane;

    public EventListPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();

        setName(NavigationController.SCREEN_EVENT_LIST);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Top Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout(16, 12));
        filterBar.setOpaque(false);
        filterBar.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        // Header Title
        JLabel pageTitle = new JLabel("Explore Campus Events");
        pageTitle.setFont(ThemeManager.FONT_HEADER_XL);
        pageTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        // Controls Box
        JPanel controlsBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlsBox.setOpaque(false);

        // Search input
        searchField = new StyledTextField("Search by event, speaker, hall, topic...");
        searchField.setPreferredSize(new Dimension(320, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterEvents(); }
            @Override public void removeUpdate(DocumentEvent e) { filterEvents(); }
            @Override public void changedUpdate(DocumentEvent e) { filterEvents(); }
        });

        // Category dropdown
        categoryComboBox = new JComboBox<>(EventCategory.values());
        categoryComboBox.setFont(ThemeManager.FONT_BODY);
        categoryComboBox.setBackground(ThemeManager.BG_TERTIARY);
        categoryComboBox.setForeground(ThemeManager.TEXT_PRIMARY);
        categoryComboBox.setPreferredSize(new Dimension(200, 38));
        categoryComboBox.addActionListener(e -> filterEvents());

        // Sort dropdown
        String[] sortOptions = {"Date (Soonest)", "Price (Low to High)", "Price (High to Low)", "Availability"};
        sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.setFont(ThemeManager.FONT_BODY);
        sortComboBox.setBackground(ThemeManager.BG_TERTIARY);
        sortComboBox.setForeground(ThemeManager.TEXT_PRIMARY);
        sortComboBox.setPreferredSize(new Dimension(170, 38));
        sortComboBox.addActionListener(e -> filterEvents());

        // Clear filters button
        StyledButton clearBtn = new StyledButton("Reset Filters", StyledButton.ButtonStyle.SECONDARY);
        clearBtn.setPreferredSize(new Dimension(120, 38));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            categoryComboBox.setSelectedItem(EventCategory.ALL);
            sortComboBox.setSelectedIndex(0);
            filterEvents();
        });

        controlsBox.add(searchField);
        controlsBox.add(categoryComboBox);
        controlsBox.add(sortComboBox);
        controlsBox.add(clearBtn);

        JPanel topHeader = new JPanel();
        topHeader.setOpaque(false);
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));
        topHeader.add(pageTitle);
        topHeader.add(Box.createVerticalStrut(12));
        topHeader.add(controlsBox);

        filterBar.add(topHeader, BorderLayout.CENTER);
        add(filterBar, BorderLayout.NORTH);

        // Main cards grid container
        cardsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        cardsGrid.setOpaque(false);

        scrollPane = new JScrollPane(cardsGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 16, 20, 16));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void onScreenDisplayed() {
        filterEvents();
    }

    private void filterEvents() {
        String query = searchField.getText().trim();
        EventCategory selectedCategory = (EventCategory) categoryComboBox.getSelectedItem();
        String sortBy = (String) sortComboBox.getSelectedItem();

        cardsGrid.removeAll();
        cardsGrid.setLayout(new GridBagLayout());
        cardsGrid.add(new LoadingPanel("Filtering seminar listings..."));
        cardsGrid.revalidate();
        cardsGrid.repaint();

        SwingWorker<List<Event>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                return eventService.searchEvents(query, selectedCategory, sortBy);
            }

            @Override
            protected void done() {
                try {
                    List<Event> results = get();
                    cardsGrid.removeAll();

                    if (results == null || results.isEmpty()) {
                        cardsGrid.setLayout(new GridBagLayout());
                        cardsGrid.add(new EmptyStatePanel(
                                "🔍",
                                "No Matching Events Found",
                                "Try adjusting your search keywords or clearing category filters.",
                                "Clear Filters",
                                () -> {
                                    searchField.setText("");
                                    categoryComboBox.setSelectedItem(EventCategory.ALL);
                                }
                        ));
                    } else {
                        cardsGrid.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 18));
                        for (Event ev : results) {
                            EventCard card = new EventCard(
                                    ev,
                                    e -> navController.showEventDetails(e),
                                    e -> navController.startSeatSelection(e)
                            );
                            cardsGrid.add(card);
                        }
                    }
                    cardsGrid.revalidate();
                    cardsGrid.repaint();
                } catch (Exception e) {
                    cardsGrid.removeAll();
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    cardsGrid.add(new JLabel("Error loading events: " + msg));
                    cardsGrid.revalidate();
                    cardsGrid.repaint();
                }
            }
        };
        worker.execute();
    }
}
