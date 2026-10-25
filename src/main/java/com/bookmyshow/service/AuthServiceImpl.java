package com.bookmyshow.service;

import com.bookmyshow.dao.UserDAO;
import com.bookmyshow.exception.AuthenticationException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.User;
import com.bookmyshow.model.enums.Role;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of AuthService.
 *
 * Password strategy: SHA-256 hash (no external libraries).
 * In a production system, use bcrypt/Argon2 with a salt.
 */
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User registerUser(String name, String email, String password, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);

        if (role == null) {
            throw new ValidationException("role", "Role must not be null");
        }

        if (userDAO.existsByEmail(email.trim().toLowerCase())) {
            throw new ValidationException("email", "Email is already registered: " + email);
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(hashPassword(password));
        user.setRole(role);
        user.setActive(true);

        return userDAO.create(user);
    }

    @Override
    public User login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new AuthenticationException("Email must not be empty");
        }
        if (password == null || password.isBlank()) {
            throw new AuthenticationException("Password must not be empty");
        }

        Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            throw new AuthenticationException("Account is inactive");
        }

        if (!validateCredentials(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return user;
    }

    @Override
    public User getUserById(int userId) {
        return userDAO.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found with id: " + userId));
    }

    @Override
    public boolean validateCredentials(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return hashPassword(rawPassword).equals(storedHash);
    }

    // --- Private helpers ---

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("name", "Name must not be empty");
        }
        if (name.trim().length() < 2) {
            throw new ValidationException("name", "Name must be at least 2 characters");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "Email must not be empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("email", "Email format is invalid: " + email);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("password", "Password must not be empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("password",
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    /**
     * Hashes a plain-text password using SHA-256.
     * NOTE: For production, replace with bcrypt + salt.
     */
    public String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in all JVMs
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
