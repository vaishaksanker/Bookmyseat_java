package com.seminarhall.service;

import com.seminarhall.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private boolean demoMode = true;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isOrganizer() {
        return currentUser != null && currentUser.getRole() == com.seminarhall.model.UserRole.ORGANIZER;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }
}
