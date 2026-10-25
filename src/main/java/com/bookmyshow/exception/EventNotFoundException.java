package com.bookmyshow.exception;

/**
 * Thrown when a requested event does not exist in the system.
 */
public class EventNotFoundException extends RuntimeException {

    private final int eventId;

    public EventNotFoundException(int eventId) {
        super("Event not found with id: " + eventId);
        this.eventId = eventId;
    }

    public EventNotFoundException(String message) {
        super(message);
        this.eventId = -1;
    }

    public int getEventId() {
        return eventId;
    }
}
