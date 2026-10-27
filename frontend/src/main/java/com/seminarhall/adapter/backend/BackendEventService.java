package com.seminarhall.adapter.backend;

import com.bookmyshow.model.Venue;
import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.SessionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.seminarhall.adapter.backend.Mappers.call;
import static com.seminarhall.adapter.backend.Mappers.parseId;

/**
 * EventService backed by the backend EventService. Writes run as the logged-in organizer,
 * so the backend's ownership and role checks apply.
 */
public class BackendEventService implements EventService {

    private static final Comparator<Event> BY_DATE =
            Comparator.comparing(Event::getDate).thenComparing(Event::getStartTime);

    private final BackendContext ctx;

    public BackendEventService(BackendContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public List<Event> getAllEvents() throws ServiceException {
        return call(() -> map(ctx.eventService.getAllEvents()));
    }

    @Override
    public List<Event> getUpcomingEvents() throws ServiceException {
        return getAllEvents().stream()
                .filter(e -> !e.isCancelled() && !e.getDate().isBefore(LocalDate.now()))
                .sorted(BY_DATE)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getFeaturedEvents() throws ServiceException {
        return getUpcomingEvents().stream()
                .sorted(Comparator.comparingInt(Event::getBookedSeats).reversed().thenComparing(BY_DATE))
                .limit(4)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByCategory(EventCategory category) throws ServiceException {
        if (category == null || category == EventCategory.ALL) {
            return getAllEvents();
        }
        return getAllEvents().stream()
                .filter(e -> e.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> searchEvents(String query, EventCategory category, String sortBy) throws ServiceException {
        String q = query != null ? query.trim().toLowerCase() : "";
        return getAllEvents().stream()
                .filter(e -> category == null || category == EventCategory.ALL || e.getCategory() == category)
                .filter(e -> q.isEmpty()
                        || e.getTitle().toLowerCase().contains(q)
                        || e.getDescription().toLowerCase().contains(q)
                        || e.getVenue().toLowerCase().contains(q)
                        || e.getSeminarHall().toLowerCase().contains(q)
                        || (e.getOrganizerName() != null && e.getOrganizerName().toLowerCase().contains(q)))
                .sorted(sortOrder(sortBy))
                .collect(Collectors.toList());
    }

    @Override
    public Event getEventById(String eventId) throws ServiceException {
        int id = parseId(eventId, "Event");
        return call(() -> map(ctx.eventService.getEventById(id), new HashMap<>()));
    }

    @Override
    public Event createEvent(Event event) throws ServiceException {
        if (event == null) {
            throw new ServiceException("Event cannot be null.");
        }
        validate(event);
        com.bookmyshow.model.User organizer = currentBackendUser();
        LocalDateTime start = LocalDateTime.of(event.getDate(), event.getStartTime());
        LocalDateTime end = LocalDateTime.of(event.getDate(), event.getEndTime());
        if (!end.isAfter(start)) {
            end = end.plusDays(1);   // e.g. a 24-hour hackathon 09:00 → 09:00
        }
        LocalDateTime finalEnd = end;
        return call(() -> {
            Venue venue = findOrCreateVenue(event.getVenue(), event.getSeminarHall(), event.getTotalCapacity());
            com.bookmyshow.model.Event created = ctx.eventService.createEvent(organizer,
                    event.getTitle(), event.getDescription(), event.getCategory().name(),
                    venue.getId(), start, finalEnd, BigDecimal.valueOf(event.getTicketPrice()),
                    event.getTotalCapacity());
            return map(created, new HashMap<>());
        });
    }

    @Override
    public Event updateEvent(Event event) throws ServiceException {
        if (event == null || event.getId() == null) {
            throw new ServiceException("Invalid event details for update.");
        }
        validate(event);
        int id = parseId(event.getId(), "Event");
        com.bookmyshow.model.User user = currentBackendUser();
        LocalDateTime start = LocalDateTime.of(event.getDate(), event.getStartTime());
        return call(() -> {
            com.bookmyshow.model.Event updated = ctx.eventService.updateEvent(user, id, event.getTitle(),
                    event.getDescription(), start, BigDecimal.valueOf(event.getTicketPrice()));
            // category and end time are display details the service does not validate
            updated.setCategory(event.getCategory().name());
            LocalDateTime end = LocalDateTime.of(event.getDate(), event.getEndTime());
            updated.setEndDateTime(end.isAfter(start) ? end : end.plusDays(1));
            return map(ctx.eventDAO.update(updated), new HashMap<>());
        });
    }

    @Override
    public void deleteEvent(String eventId) throws ServiceException {
        Event event = getEventById(eventId);
        if (event.getBookedSeats() > 0) {
            throw new ServiceException("Cannot delete event '" + event.getTitle()
                    + "' because tickets have already been booked.");
        }
        cancelEvent(eventId);
    }

    @Override
    public void cancelEvent(String eventId) throws ServiceException {
        int id = parseId(eventId, "Event");
        com.bookmyshow.model.User user = currentBackendUser();
        call(() -> {
            ctx.eventService.deleteEvent(user, id);   // backend delete = mark CANCELLED
            return null;
        });
    }

    private com.bookmyshow.model.User currentBackendUser() throws ServiceException {
        com.seminarhall.model.User current = SessionManager.getInstance().getCurrentUser();
        if (current == null) {
            throw new ServiceException("Please log in as an organizer to manage events.");
        }
        int id = parseId(current.getId(), "User");
        return call(() -> ctx.userDAO.findById(id)
                .orElseThrow(() -> new ServiceException("Logged-in user no longer exists.")));
    }

    private Venue findOrCreateVenue(String building, String hall, int capacity) {
        String name = hall != null && !hall.isBlank() ? hall.trim() : building.trim();
        String address = building.trim();
        for (Venue venue : ctx.venueDAO.findAll()) {
            if (venue.getName().equalsIgnoreCase(name) && address.equalsIgnoreCase(venue.getAddress())) {
                if (venue.getTotalCapacity() < capacity) {
                    venue.setTotalCapacity(capacity);
                    ctx.venueDAO.update(venue);
                }
                return venue;
            }
        }
        return ctx.venueDAO.create(new Venue(0, name, address, "Campus", capacity));
    }

    private List<Event> map(List<com.bookmyshow.model.Event> events) {
        Map<Integer, com.bookmyshow.model.User> organizers = new HashMap<>();
        return events.stream().map(e -> map(e, organizers)).collect(Collectors.toList());
    }

    private Event map(com.bookmyshow.model.Event event, Map<Integer, com.bookmyshow.model.User> organizers) {
        com.bookmyshow.model.User organizer = organizers.computeIfAbsent(event.getOrganizerId(),
                id -> ctx.userDAO.findById(id).orElse(null));
        return Mappers.event(event, organizer);
    }

    private static Comparator<Event> sortOrder(String sortBy) {
        if ("Price (Low to High)".equalsIgnoreCase(sortBy)) {
            return Comparator.comparingDouble(Event::getTicketPrice);
        } else if ("Price (High to Low)".equalsIgnoreCase(sortBy)) {
            return Comparator.comparingDouble(Event::getTicketPrice).reversed();
        } else if ("Availability".equalsIgnoreCase(sortBy)) {
            return Comparator.comparingInt(Event::getAvailableSeats).reversed();
        }
        return BY_DATE;
    }

    /** Same rules and messages as the demo services, so the organizer form behaves identically. */
    private static void validate(Event event) throws ServiceException {
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
        if (event.getCategory() == null || event.getCategory() == EventCategory.ALL) {
            throw new ServiceException("Please choose an event category.");
        }
        if (event.getTicketPrice() < 0) {
            throw new ServiceException("Ticket price cannot be negative.");
        }
        if (event.getTotalCapacity() <= 0) {
            throw new ServiceException("Total seating capacity must be greater than zero.");
        }
    }
}
