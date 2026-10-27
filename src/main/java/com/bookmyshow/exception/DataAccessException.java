package com.bookmyshow.exception;

/**
 * Thrown when the database layer fails (connection lost, SQL error, constraint violation).
 * Wraps the underlying SQLException so callers are not forced to handle checked exceptions.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
