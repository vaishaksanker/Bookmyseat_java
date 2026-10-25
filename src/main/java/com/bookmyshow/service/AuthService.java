package com.bookmyshow.service;

import com.bookmyshow.model.User;
import com.bookmyshow.model.enums.Role;

/**
 * Service interface for authentication and user account management.
 */
public interface AuthService {

    /**
     * Registers a new user after validating all input.
     *
     * @throws com.bookmyshow.exception.ValidationException if any field is invalid
     * @throws com.bookmyshow.exception.BookingException if the email is already registered
     */
    User registerUser(String name, String email, String password, Role role);

    /**
     * Authenticates a user by email and password.
     *
     * @return the authenticated User domain object
     * @throws com.bookmyshow.exception.AuthenticationException if credentials are invalid or account is inactive
     */
    User login(String email, String password);

    /**
     * Retrieves a user by ID.
     *
     * @throws com.bookmyshow.exception.ValidationException if the user does not exist
     */
    User getUserById(int userId);

    /**
     * Validates that the raw password matches the stored hash.
     */
    boolean validateCredentials(String rawPassword, String storedHash);
}
