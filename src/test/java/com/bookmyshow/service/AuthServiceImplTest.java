package com.bookmyshow.service;

import com.bookmyshow.dao.UserDAO;
import com.bookmyshow.exception.AuthenticationException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.User;
import com.bookmyshow.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserDAO userDAO;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDAO);
    }

    // =========================================================
    // registerUser tests
    // =========================================================

    @Test
    @DisplayName("registerUser: valid input creates and returns user")
    void registerUser_validInput_returnsUser() {
        User savedUser = new User(1, "Alice Smith", "alice@example.com",
                authService.hashPassword("secret123"), Role.CUSTOMER);

        when(userDAO.existsByEmail("alice@example.com")).thenReturn(false);
        when(userDAO.create(any(User.class))).thenReturn(savedUser);

        User result = authService.registerUser("Alice Smith", "alice@example.com",
                "secret123", Role.CUSTOMER);

        assertNotNull(result);
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(Role.CUSTOMER, result.getRole());
        verify(userDAO).create(any(User.class));
    }

    @Test
    @DisplayName("registerUser: duplicate email throws ValidationException")
    void registerUser_duplicateEmail_throwsValidationException() {
        when(userDAO.existsByEmail("alice@example.com")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.registerUser("Alice", "alice@example.com", "secret123", Role.CUSTOMER));

        assertTrue(ex.getMessage().contains("already registered"));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("registerUser: empty name throws ValidationException")
    void registerUser_emptyName_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                authService.registerUser("", "alice@example.com", "secret123", Role.CUSTOMER));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("registerUser: invalid email format throws ValidationException")
    void registerUser_invalidEmail_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                authService.registerUser("Alice", "not-an-email", "secret123", Role.CUSTOMER));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("registerUser: short password throws ValidationException")
    void registerUser_shortPassword_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                authService.registerUser("Alice", "alice@example.com", "abc", Role.CUSTOMER));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("registerUser: null role throws ValidationException")
    void registerUser_nullRole_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                authService.registerUser("Alice", "alice@example.com", "secret123", null));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("registerUser: email is normalized to lowercase")
    void registerUser_emailNormalized() {
        when(userDAO.existsByEmail("alice@example.com")).thenReturn(false);

        User savedUser = new User(1, "Alice", "alice@example.com",
                authService.hashPassword("secret123"), Role.CUSTOMER);
        when(userDAO.create(any(User.class))).thenReturn(savedUser);

        authService.registerUser("Alice", "ALICE@EXAMPLE.COM", "secret123", Role.CUSTOMER);

        // The DAO should have been called with lowercase email
        verify(userDAO).existsByEmail("alice@example.com");
    }

    // =========================================================
    // login tests
    // =========================================================

    @Test
    @DisplayName("login: valid credentials returns user")
    void login_validCredentials_returnsUser() {
        String hash = authService.hashPassword("secret123");
        User user = new User(1, "Alice", "alice@example.com", hash, Role.CUSTOMER);
        user.setActive(true);

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        User result = authService.login("alice@example.com", "secret123");

        assertNotNull(result);
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    @DisplayName("login: unknown email throws AuthenticationException")
    void login_unknownEmail_throwsAuthenticationException() {
        when(userDAO.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () ->
                authService.login("unknown@example.com", "secret123"));
    }

    @Test
    @DisplayName("login: wrong password throws AuthenticationException")
    void login_wrongPassword_throwsAuthenticationException() {
        String hash = authService.hashPassword("correctPassword");
        User user = new User(1, "Alice", "alice@example.com", hash, Role.CUSTOMER);
        user.setActive(true);

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                authService.login("alice@example.com", "wrongPassword"));
    }

    @Test
    @DisplayName("login: inactive account throws AuthenticationException")
    void login_inactiveAccount_throwsAuthenticationException() {
        String hash = authService.hashPassword("secret123");
        User user = new User(1, "Alice", "alice@example.com", hash, Role.CUSTOMER);
        user.setActive(false);

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authService.login("alice@example.com", "secret123"));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("login: empty email throws AuthenticationException")
    void login_emptyEmail_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class, () ->
                authService.login("", "secret123"));
    }

    // =========================================================
    // getUserById tests
    // =========================================================

    @Test
    @DisplayName("getUserById: existing user is returned")
    void getUserById_existingUser_returnsUser() {
        User user = new User(42, "Bob", "bob@example.com", "hash", Role.ORGANIZER);
        when(userDAO.findById(42)).thenReturn(Optional.of(user));

        User result = authService.getUserById(42);
        assertEquals(42, result.getId());
    }

    @Test
    @DisplayName("getUserById: unknown ID throws ValidationException")
    void getUserById_unknownId_throwsValidationException() {
        when(userDAO.findById(99)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> authService.getUserById(99));
    }

    // =========================================================
    // validateCredentials tests
    // =========================================================

    @Test
    @DisplayName("validateCredentials: correct password returns true")
    void validateCredentials_correctPassword_returnsTrue() {
        String hash = authService.hashPassword("myPassword");
        assertTrue(authService.validateCredentials("myPassword", hash));
    }

    @Test
    @DisplayName("validateCredentials: wrong password returns false")
    void validateCredentials_wrongPassword_returnsFalse() {
        String hash = authService.hashPassword("myPassword");
        assertFalse(authService.validateCredentials("wrongPassword", hash));
    }

    @Test
    @DisplayName("validateCredentials: null inputs return false")
    void validateCredentials_nullInputs_returnsFalse() {
        assertFalse(authService.validateCredentials(null, "hash"));
        assertFalse(authService.validateCredentials("pass", null));
    }
}
