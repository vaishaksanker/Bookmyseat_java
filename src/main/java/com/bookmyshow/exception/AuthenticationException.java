package com.bookmyshow.exception;

/**
 * Thrown when a login attempt fails due to bad credentials or inactive account.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
