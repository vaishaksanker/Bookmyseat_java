package com.bookmyshow.exception;

/**
 * Thrown when an authenticated user attempts an action they are not permitted to perform.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
