package com.bookmyshow.exception;

/**
 * Thrown when a booking operation fails for business logic reasons.
 */
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
