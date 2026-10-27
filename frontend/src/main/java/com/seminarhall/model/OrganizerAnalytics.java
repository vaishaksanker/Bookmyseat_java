package com.seminarhall.model;

import java.io.Serializable;
import java.util.List;

public class OrganizerAnalytics implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalEvents;
    private int upcomingEvents;
    private int totalBookings;
    private int totalSeats;
    private int bookedSeats;
    private int availableSeats;
    private double overallOccupancyPercentage;
    private double totalRevenue;
    private List<EventOccupancySummary> eventOccupancies;

    public OrganizerAnalytics() {
    }

    public OrganizerAnalytics(int totalEvents, int upcomingEvents, int totalBookings,
                              int totalSeats, int bookedSeats, int availableSeats,
                              double overallOccupancyPercentage, double totalRevenue,
                              List<EventOccupancySummary> eventOccupancies) {
        this.totalEvents = totalEvents;
        this.upcomingEvents = upcomingEvents;
        this.totalBookings = totalBookings;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.availableSeats = availableSeats;
        this.overallOccupancyPercentage = overallOccupancyPercentage;
        this.totalRevenue = totalRevenue;
        this.eventOccupancies = eventOccupancies;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getUpcomingEvents() {
        return upcomingEvents;
    }

    public void setUpcomingEvents(int upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getOverallOccupancyPercentage() {
        return overallOccupancyPercentage;
    }

    public void setOverallOccupancyPercentage(double overallOccupancyPercentage) {
        this.overallOccupancyPercentage = overallOccupancyPercentage;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<EventOccupancySummary> getEventOccupancies() {
        return eventOccupancies;
    }

    public void setEventOccupancies(List<EventOccupancySummary> eventOccupancies) {
        this.eventOccupancies = eventOccupancies;
    }

    public static class EventOccupancySummary implements Serializable {
        private static final long serialVersionUID = 1L;

        private String eventId;
        private String eventTitle;
        private int totalSeats;
        private int bookedSeats;
        private double occupancyPercentage;

        public EventOccupancySummary(String eventId, String eventTitle, int totalSeats, int bookedSeats) {
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.totalSeats = totalSeats;
            this.bookedSeats = bookedSeats;
            this.occupancyPercentage = totalSeats > 0 ? ((double) bookedSeats / totalSeats * 100.0) : 0.0;
        }

        public String getEventId() {
            return eventId;
        }

        public String getEventTitle() {
            return eventTitle;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public int getBookedSeats() {
            return bookedSeats;
        }

        public double getOccupancyPercentage() {
            return occupancyPercentage;
        }
    }
}
