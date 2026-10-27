package com.seminarhall.service.mock;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MockEventService implements EventService {
    private final MockDataStore dataStore;

    public MockEventService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Event> getAllEvents() {
        return new ArrayList<>(dataStore.getEvents().values());
    }

    @Override
    public List<Event> getUpcomingEvents() {
        return dataStore.getEvents().values().stream()
                .filter(e -> !e.isCancelled() && !e.getDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Event::getDate).thenComparing(Event::getStartTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getFeaturedEvents() {
        return dataStore.getEvents().values().stream()
                .filter(e -> !e.isCancelled() && "FEATURED".equalsIgnoreCase(e.getAccentTag()) || "TRENDING".equalsIgnoreCase(e.getAccentTag()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByCategory(EventCategory category) {
        if (category == null || category == EventCategory.ALL) {
            return getAllEvents();
        }
        return dataStore.getEvents().values().stream()
                .filter(e -> e.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> searchEvents(String query, EventCategory category, String sortBy) {
        return dataStore.getEvents().values().stream()
                .filter(e -> {
                    if (category != null && category != EventCategory.ALL && e.getCategory() != category) {
                        return false;
                    }
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        boolean matchTitle = e.getTitle().toLowerCase().contains(q);
                        boolean matchDesc = e.getDescription().toLowerCase().contains(q);
                        boolean matchVenue = e.getVenue().toLowerCase().contains(q);
                        boolean matchHall = e.getSeminarHall().toLowerCase().contains(q);
                        boolean matchOrganizer = e.getOrganizerName() != null && e.getOrganizerName().toLowerCase().contains(q);
                        if (!matchTitle && !matchDesc && !matchVenue && !matchHall && !matchOrganizer) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((e1, e2) -> {
                    if ("Price (Low to High)".equalsIgnoreCase(sortBy)) {
                        return Double.compare(e1.getTicketPrice(), e2.getTicketPrice());
                    } else if ("Price (High to Low)".equalsIgnoreCase(sortBy)) {
                        return Double.compare(e2.getTicketPrice(), e1.getTicketPrice());
                    } else if ("Availability".equalsIgnoreCase(sortBy)) {
                        return Integer.compare(e2.getAvailableSeats(), e1.getAvailableSeats());
                    } else { // Date ascending
                        int dateCmp = e1.getDate().compareTo(e2.getDate());
                        return dateCmp != 0 ? dateCmp : e1.getStartTime().compareTo(e2.getStartTime());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Event getEventById(String eventId) throws ServiceException {
        if (eventId == null) throw new ServiceException("Event ID cannot be empty.");
        Event event = dataStore.getEvents().get(eventId);
        if (event == null) {
            throw new ServiceException("Event not found with ID: " + eventId);
        }
        return event;
    }

    @Override
    public Event createEvent(Event event) throws ServiceException {
        if (event == null) throw new ServiceException("Event cannot be null.");
        validateEvent(event);

        if (event.getId() == null || event.getId().trim().isEmpty()) {
            event.setId("ev_" + System.currentTimeMillis() % 100000);
        }
        dataStore.getEvents().put(event.getId(), event);

        // Generate default seating layout for this new event
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
        int seatsPerRow = 10;
        int total = Math.min(event.getTotalCapacity(), rows.length * seatsPerRow);
        int count = 0;
        for (String r : rows) {
            for (int n = 1; n <= seatsPerRow; n++) {
                if (count >= total) break;
                seats.add(new Seat(event.getId() + "_" + r + n, event.getId(), r, n,
                        SeatStatus.AVAILABLE, event.getTicketPrice(), r.equals("A") && n <= 2));
                count++;
            }
            if (count >= total) break;
        }
        dataStore.getEventSeats().put(event.getId(), seats);

        return event;
    }

    @Override
    public Event updateEvent(Event event) throws ServiceException {
        if (event == null || event.getId() == null) {
            throw new ServiceException("Invalid event details for update.");
        }
        validateEvent(event);
        if (!dataStore.getEvents().containsKey(event.getId())) {
            throw new ServiceException("Event not found with ID: " + event.getId());
        }
        dataStore.getEvents().put(event.getId(), event);
        return event;
    }

    @Override
    public void deleteEvent(String eventId) throws ServiceException {
        if (eventId == null) throw new ServiceException("Event ID cannot be null.");
        Event ev = dataStore.getEvents().get(eventId);
        if (ev == null) throw new ServiceException("Event not found with ID: " + eventId);

        if (ev.getBookedSeats() > 0) {
            throw new ServiceException("Cannot delete event '" + ev.getTitle() + "' because tickets have already been booked.");
        }
        dataStore.getEvents().remove(eventId);
        dataStore.getEventSeats().remove(eventId);
    }

    @Override
    public void cancelEvent(String eventId) throws ServiceException {
        Event ev = getEventById(eventId);
        ev.setCancelled(true);
    }

    private void validateEvent(Event event) throws ServiceException {
        if (event.getTitle() == null || event.getTitle().trim().length() < 3) {
            throw new ServiceException("Event title is required (minimum 3 characters).");
        }
        if (event.getDate() == null) {
            throw new ServiceException("Event date is required.");
        }
        if (event.getStartTime() == null || event.getEndTime() == null) {
            throw new ServiceException("Start and End times are required.");
        }
        if (event.getEndTime().isBefore(event.getStartTime())) {
            throw new ServiceException("End time cannot be earlier than start time.");
        }
        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new ServiceException("Venue name is required.");
        }
        if (event.getTicketPrice() < 0) {
            throw new ServiceException("Ticket price cannot be negative.");
        }
        if (event.getTotalCapacity() <= 0) {
            throw new ServiceException("Total seating capacity must be greater than zero.");
        }
    }
}
