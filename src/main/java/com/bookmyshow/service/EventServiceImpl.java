package com.bookmyshow.service;

import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.dao.VenueDAO;
import com.bookmyshow.exception.EventNotFoundException;
import com.bookmyshow.exception.UnauthorizedException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.User;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of EventService.
 *
 * Authorization is enforced here — not in the UI.
 * Only ORGANIZER or ADMIN users may create/update/delete events.
 * An organizer may only modify their own events.
 */
public class EventServiceImpl implements EventService {

    private final EventDAO eventDAO;
    private final VenueDAO venueDAO;

    public EventServiceImpl(EventDAO eventDAO, VenueDAO venueDAO) {
        this.eventDAO = eventDAO;
        this.venueDAO = venueDAO;
    }

    @Override
    public Event createEvent(User organizer, String name, String description,
                             int venueId, LocalDateTime eventDateTime,
                             BigDecimal ticketPrice, int totalSeats) {

        requireOrganizerOrAdmin(organizer);
        validateEventName(name);
        validateEventDateTime(eventDateTime);
        validateTicketPrice(ticketPrice);
        validateTotalSeats(totalSeats);

        Venue venue = venueDAO.findById(venueId)
                .orElseThrow(() -> new ValidationException("venueId", "Venue not found with id: " + venueId));

        Event event = new Event();
        event.setName(name.trim());
        event.setDescription(description != null ? description.trim() : "");
        event.setVenue(venue);
        event.setOrganizerId(organizer.getId());
        event.setEventDateTime(eventDateTime);
        event.setTicketPrice(ticketPrice);
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(totalSeats);
        event.setStatus(EventStatus.PUBLISHED);  // Auto-publish on creation

        return eventDAO.create(event);
    }

    @Override
    public Event updateEvent(User requestingUser, int eventId, String name, String description,
                             LocalDateTime eventDateTime, BigDecimal ticketPrice) {

        Event existing = getEventById(eventId);
        requireOwnerOrAdmin(requestingUser, existing);

        if (name != null) {
            validateEventName(name);
            existing.setName(name.trim());
        }
        if (description != null) {
            existing.setDescription(description.trim());
        }
        if (eventDateTime != null) {
            validateEventDateTime(eventDateTime);
            existing.setEventDateTime(eventDateTime);
        }
        if (ticketPrice != null) {
            validateTicketPrice(ticketPrice);
            existing.setTicketPrice(ticketPrice);
        }

        return eventDAO.update(existing);
    }

    @Override
    public void deleteEvent(User requestingUser, int eventId) {
        Event existing = getEventById(eventId);
        requireOwnerOrAdmin(requestingUser, existing);
        existing.setStatus(EventStatus.CANCELLED);
        eventDAO.update(existing);
        eventDAO.delete(eventId);
    }

    @Override
    public Event getEventById(int eventId) {
        return eventDAO.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventDAO.findAll();
    }

    @Override
    public List<Event> getPublishedEvents() {
        return eventDAO.findByStatus(EventStatus.PUBLISHED);
    }

    @Override
    public List<Event> searchEvents(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ValidationException("keyword", "Search keyword must not be empty");
        }
        return eventDAO.searchByName(keyword.trim());
    }

    // --- Authorization helpers ---

    private void requireOrganizerOrAdmin(User user) {
        if (user == null) {
            throw new UnauthorizedException("User must be logged in");
        }
        if (!user.isOrganizer() && !user.isAdmin()) {
            throw new UnauthorizedException(
                    "Only organizers or admins can manage events. User role: " + user.getRole());
        }
    }

    private void requireOwnerOrAdmin(User user, Event event) {
        if (user == null) {
            throw new UnauthorizedException("User must be logged in");
        }
        if (!user.isAdmin() && !event.isOwnedBy(user.getId())) {
            throw new UnauthorizedException(
                    "User " + user.getId() + " is not authorized to modify event " + event.getId());
        }
    }

    // --- Validation helpers ---

    private void validateEventName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("name", "Event name must not be empty");
        }
        if (name.trim().length() < 2) {
            throw new ValidationException("name", "Event name must be at least 2 characters");
        }
    }

    private void validateEventDateTime(LocalDateTime eventDateTime) {
        if (eventDateTime == null) {
            throw new ValidationException("eventDateTime", "Event date/time must not be null");
        }
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("eventDateTime", "Event date/time must be in the future");
        }
    }

    private void validateTicketPrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("ticketPrice", "Ticket price must not be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("ticketPrice", "Ticket price must be >= 0");
        }
    }

    private void validateTotalSeats(int totalSeats) {
        if (totalSeats <= 0) {
            throw new ValidationException("totalSeats", "Total seats must be greater than 0");
        }
    }
}
