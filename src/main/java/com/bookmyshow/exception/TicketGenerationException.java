package com.bookmyshow.exception;

/**
 * Thrown when a ticket cannot be generated (e.g., payment not confirmed, invalid booking).
 */
public class TicketGenerationException extends RuntimeException {

    public TicketGenerationException(String message) {
        super(message);
    }

    public TicketGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
