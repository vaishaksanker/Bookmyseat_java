package com.bookmyshow.model;

import com.bookmyshow.model.enums.Role;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a registered user of the system.
 * Demonstrates encapsulation: all fields private, accessed via getters/setters.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    // --- Constructors ---

    public User() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public User(int id, String name, String email, String passwordHash, Role role) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(Role role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- Utility ---

    public boolean isOrganizer() { return Role.ORGANIZER.equals(this.role); }
    public boolean isAdmin() { return Role.ADMIN.equals(this.role); }
    public boolean isCustomer() { return Role.CUSTOMER.equals(this.role); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', role=" + role + "}";
    }
}
