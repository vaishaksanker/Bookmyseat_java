package com.seminarhall.adapter.backend;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.UserService;

import static com.seminarhall.adapter.backend.Mappers.call;
import static com.seminarhall.adapter.backend.Mappers.parseId;

/**
 * UserService backed by AuthService + UserDAO. Passwords are hashed by the backend.
 * The backend has no phone column, so the phone number is kept for the current session only.
 */
public class BackendUserService implements UserService {

    private final BackendContext ctx;

    public BackendUserService(BackendContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public User authenticate(String email, String password) throws ServiceException {
        return call(() -> Mappers.user(ctx.authService.login(email, password)));
    }

    @Override
    public User register(String fullName, String email, String phone, String password, UserRole role)
            throws ServiceException {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new ServiceException("Please enter a valid full name (minimum 3 characters).");
        }
        User user = call(() -> Mappers.user(ctx.authService.registerUser(
                fullName, email != null ? email.trim() : null, password, Mappers.toBackendRole(role))));
        user.setPhoneNumber(phone != null ? phone.trim() : "");
        return user;
    }

    @Override
    public User getUserById(String userId) throws ServiceException {
        int id = parseId(userId, "User");
        return call(() -> Mappers.user(ctx.userDAO.findById(id)
                .orElseThrow(() -> new ServiceException("User not found with ID: " + userId))));
    }

    @Override
    public boolean isEmailRegistered(String email) throws ServiceException {
        if (email == null) {
            return false;
        }
        return call(() -> ctx.userDAO.existsByEmail(email.trim().toLowerCase()));
    }

    @Override
    public void updateProfile(User user) throws ServiceException {
        if (user == null || user.getId() == null) {
            throw new ServiceException("User information cannot be empty.");
        }
        int id = parseId(user.getId(), "User");
        call(() -> {
            com.bookmyshow.model.User stored = ctx.userDAO.findById(id)
                    .orElseThrow(() -> new ServiceException("User not found with ID: " + user.getId()));
            if (user.getFullName() != null && !user.getFullName().isBlank()) {
                stored.setName(user.getFullName().trim());
            }
            return ctx.userDAO.update(stored);
        });
    }
}
