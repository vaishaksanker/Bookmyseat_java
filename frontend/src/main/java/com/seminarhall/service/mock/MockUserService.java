package com.seminarhall.service.mock;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.UserService;

import java.util.UUID;

public class MockUserService implements UserService {
    private final MockDataStore dataStore;

    public MockUserService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public User authenticate(String email, String password) throws ServiceException {
        if (email == null || email.trim().isEmpty()) {
            throw new ServiceException("Email address cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ServiceException("Password cannot be empty.");
        }

        String key = email.trim().toLowerCase();
        User user = dataStore.getUsers().get(key);
        String storedPassword = dataStore.getUserPasswords().get(key);

        if (user == null || storedPassword == null || !storedPassword.equals(password)) {
            throw new ServiceException("Invalid email or password. Please verify credentials.");
        }

        return user;
    }

    @Override
    public User register(String fullName, String email, String phone, String password, UserRole role) throws ServiceException {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new ServiceException("Please enter a valid full name (minimum 3 characters).");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ServiceException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new ServiceException("Password must be at least 6 characters long.");
        }

        String key = email.trim().toLowerCase();
        if (dataStore.getUsers().containsKey(key)) {
            throw new ServiceException("An account with email " + email + " is already registered.");
        }

        String userId = "usr_" + UUID.randomUUID().toString().substring(0, 8);
        User newUser = new User(userId, fullName.trim(), key, phone != null ? phone.trim() : "", role);

        dataStore.getUsers().put(key, newUser);
        dataStore.getUserPasswords().put(key, password);

        return newUser;
    }

    @Override
    public User getUserById(String userId) throws ServiceException {
        if (userId == null) throw new ServiceException("User ID is required.");
        for (User u : dataStore.getUsers().values()) {
            if (u.getId().equals(userId)) {
                return u;
            }
        }
        throw new ServiceException("User not found with ID: " + userId);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        if (email == null) return false;
        return dataStore.getUsers().containsKey(email.trim().toLowerCase());
    }

    @Override
    public void updateProfile(User user) throws ServiceException {
        if (user == null || user.getEmail() == null) {
            throw new ServiceException("User information cannot be empty.");
        }
        dataStore.getUsers().put(user.getEmail().toLowerCase(), user);
    }
}
