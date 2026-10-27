package com.seminarhall.model;

public enum EventCategory {
    ALL("All Categories"),
    TECHNICAL_SEMINAR("Technical Seminar"),
    HACKATHON("Hackathon"),
    WORKSHOP("Hands-on Workshop"),
    GUEST_LECTURE("Guest Lecture"),
    CULTURAL("Cultural & Arts"),
    SPORTS("Sports & Athletics");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
