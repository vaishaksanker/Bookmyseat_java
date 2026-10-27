package com.seminarhall.service;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventServiceTest {
    private EventService eventService;

    @BeforeEach
    void setUp() {
        ServiceFactory.initMockServices();
        eventService = ServiceFactory.getEventService();
    }

    @Test
    @DisplayName("Test retrieve all events has seeded college events")
    void testGetAllEvents() throws ServiceException {
        List<Event> events = eventService.getAllEvents();
        assertNotNull(events);
        assertTrue(events.size() >= 5);
    }

    @Test
    @DisplayName("Test search events by query matches title")
    void testSearchByTitle() throws ServiceException {
        List<Event> results = eventService.searchEvents("AI in Modern World", EventCategory.ALL, "Date");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().contains("AI in Modern World"));
    }

    @Test
    @DisplayName("Test filter by category")
    void testFilterByCategory() throws ServiceException {
        List<Event> workshops = eventService.getEventsByCategory(EventCategory.WORKSHOP);
        assertNotNull(workshops);
        assertFalse(workshops.isEmpty());
        for (Event ev : workshops) {
            assertEquals(EventCategory.WORKSHOP, ev.getCategory());
        }
    }

    @Test
    @DisplayName("Test sort by price low to high")
    void testSortByPrice() throws ServiceException {
        List<Event> sorted = eventService.searchEvents("", EventCategory.ALL, "Price (Low to High)");
        assertNotNull(sorted);
        assertTrue(sorted.size() > 1);
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getTicketPrice() <= sorted.get(i + 1).getTicketPrice());
        }
    }
}
