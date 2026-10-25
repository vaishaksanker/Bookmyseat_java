package com.bookmyshow.model;

import com.bookmyshow.model.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a bookable event (concert, movie, play, etc.).
 * Uses BigDecimal for price to avoid floating-point errors.
 */
public class Event {

    private int id;
    private String name;
    private String description;
    private Venue venue;
    private int organizerId;
    private LocalDateTime eventDateTime;
    private BigDecimal ticketPrice;
    private int totalSeats;
    private int availableSeats;
    private EventStatus status;
    private LocalDateTime createdAt;

    // --- Constructors ---

    public Event() {
        this.status = EventStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Event(int id, String name, String description, Venue venue,
                 int organizerId, LocalDateTime eventDateTime,
                 BigDecimal ticketPrice, int totalSeats) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.organizerId = organizerId;
        this.eventDateTime = eventDateTime;
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Venue getVenue() { return venue; }
    public int getOrganizerId() { return organizerId; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public EventStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }
    public void setEventDateTime(LocalDateTime eventDateTime) { this.eventDateTime = eventDateTime; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- Business helpers ---

    public boolean isBookable() {
        return EventStatus.PUBLISHED.equals(this.status)
                && this.availableSeats > 0
                && this.eventDateTime.isAfter(LocalDateTime.now());
    }

    public boolean isOwnedBy(int userId) {
        return this.organizerId == userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return id == event.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", name='" + name + "', status=" + status
                + ", availableSeats=" + availableSeats + "}";
    }
}
