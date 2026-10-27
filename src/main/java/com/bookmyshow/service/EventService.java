package com.bookmyshow.service;

import com.bookmyshow.model.Event;
import com.bookmyshow.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for event management business logic.
 */
public interface EventService {

    /**
     * Creates a new event. Only ORGANIZER or ADMIN users may create events.
     *
     * @throws com.bookmyshow.exception.ValidationException   if any field is invalid
     * @throws com.bookmyshow.exception.UnauthorizedException if the user is not an organizer/admin
     */
    Event createEvent(User organizer, String name, String description,
                      int venueId, LocalDateTime eventDateTime,
                      BigDecimal ticketPrice, int totalSeats);

    /**
     * Creates a new event with an optional category and end time.
     * Only ORGANIZER or ADMIN users may create events.
     *
     * @param category    optional category name (e.g. "WORKSHOP"), may be null
     * @param endDateTime optional end time, may be null; must not be before eventDateTime
     * @throws com.bookmyshow.exception.ValidationException   if any field is invalid
     * @throws com.bookmyshow.exception.UnauthorizedException if the user is not an organizer/admin
     */
    Event createEvent(User organizer, String name, String description, String category,
                      int venueId, LocalDateTime eventDateTime, LocalDateTime endDateTime,
                      BigDecimal ticketPrice, int totalSeats);

    /**
     * Updates an existing event. Only the owning organizer or an admin may update it.
     *
     * @throws com.bookmyshow.exception.EventNotFoundException  if the event does not exist
     * @throws com.bookmyshow.exception.UnauthorizedException   if the user is not the owner/admin
     * @throws com.bookmyshow.exception.ValidationException     if any updated field is invalid
     */
    Event updateEvent(User requestingUser, int eventId, String name, String description,
                      LocalDateTime eventDateTime, BigDecimal ticketPrice);

    /**
     * Deletes (cancels) an event. Only the owning organizer or an admin may delete it.
     *
     * @throws com.bookmyshow.exception.EventNotFoundException  if the event does not exist
     * @throws com.bookmyshow.exception.UnauthorizedException   if the user is not the owner/admin
     */
    void deleteEvent(User requestingUser, int eventId);

    /**
     * Retrieves an event by its ID.
     *
     * @throws com.bookmyshow.exception.EventNotFoundException if the event does not exist
     */
    Event getEventById(int eventId);

    /**
     * Returns all events in the system.
     */
    List<Event> getAllEvents();

    /**
     * Returns all published events available for booking.
     */
    List<Event> getPublishedEvents();

    /**
     * Searches events by name keyword (case-insensitive).
     */
    List<Event> searchEvents(String keyword);
}
