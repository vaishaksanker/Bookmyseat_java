package com.bookmyshow.exception;

/**
 * Thrown when user-provided input fails business validation rules.
 */
public class ValidationException extends RuntimeException {

    private final String field;

    public ValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
    }

    /** Returns the name of the field that failed validation, or null if not field-specific. */
    public String getField() {
        return field;
    }
}
