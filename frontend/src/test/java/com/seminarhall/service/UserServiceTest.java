package com.seminarhall.service;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.mock.MockDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        ServiceFactory.initMockServices();
        userService = ServiceFactory.getUserService();
    }

    @Test
    @DisplayName("Test successful authentication with student demo account")
    void testAuthenticationSuccess() throws ServiceException {
        User user = userService.authenticate("student@college.edu", "student123");
        assertNotNull(user);
        assertEquals("Ananthakrishna S", user.getFullName());
        assertEquals(UserRole.STUDENT, user.getRole());
    }

    @Test
    @DisplayName("Test successful authentication with organizer demo account")
    void testOrganizerAuthentication() throws ServiceException {
        User user = userService.authenticate("organizer@college.edu", "admin123");
        assertNotNull(user);
        assertEquals(UserRole.ORGANIZER, user.getRole());
    }

    @Test
    @DisplayName("Test authentication failure with incorrect password")
    void testAuthenticationWrongPassword() {
        assertThrows(ServiceException.class, () -> {
            userService.authenticate("student@college.edu", "wrongpass");
        });
    }

    @Test
    @DisplayName("Test registration of new student account")
    void testRegistrationSuccess() throws ServiceException {
        User registered = userService.register(
                "Ananya Rao",
                "ananya@college.edu",
                "+91 9988776655",
                "securePass123",
                UserRole.STUDENT
        );
        assertNotNull(registered);
        assertEquals("Ananya Rao", registered.getFullName());
        assertEquals(UserRole.STUDENT, registered.getRole());

        // Authenticate immediately after
        User auth = userService.authenticate("ananya@college.edu", "securePass123");
        assertNotNull(auth);
        assertEquals(registered.getId(), auth.getId());
    }

    @Test
    @DisplayName("Test registration failure on duplicate email")
    void testDuplicateEmailRegistration() {
        assertThrows(ServiceException.class, () -> {
            userService.register("Duplicate Test", "student@college.edu", "+91 0000000000", "pass123", UserRole.STUDENT);
        });
    }
}
