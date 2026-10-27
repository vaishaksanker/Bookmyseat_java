package com.seminarhall.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private EventCategory category;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private String seminarHall;
    private String organizerName;
    private String organizerContact;
    private double ticketPrice;
    private int totalCapacity;
    private int bookedSeats;
    private boolean cancelled;
    private String bannerImagePath;
    private String accentTag;

    public Event() {
    }

    public Event(String id, String title, String description, EventCategory category,
                 LocalDate date, LocalTime startTime, LocalTime endTime,
                 String venue, String seminarHall, String organizerName,
                 String organizerContact, double ticketPrice, int totalCapacity,
                 int bookedSeats, String bannerImagePath, String accentTag) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.seminarHall = seminarHall;
        this.organizerName = organizerName;
        this.organizerContact = organizerContact;
        this.ticketPrice = ticketPrice;
        this.totalCapacity = totalCapacity;
        this.bookedSeats = bookedSeats;
        this.cancelled = false;
        this.bannerImagePath = bannerImagePath;
        this.accentTag = accentTag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getSeminarHall() {
        return seminarHall;
    }

    public void setSeminarHall(String seminarHall) {
        this.seminarHall = seminarHall;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerContact() {
        return organizerContact;
    }

    public void setOrganizerContact(String organizerContact) {
        this.organizerContact = organizerContact;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public int getAvailableSeats() {
        return Math.max(0, totalCapacity - bookedSeats);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getBannerImagePath() {
        return bannerImagePath;
    }

    public void setBannerImagePath(String bannerImagePath) {
        this.bannerImagePath = bannerImagePath;
    }

    public String getAccentTag() {
        return accentTag;
    }

    public void setAccentTag(String accentTag) {
        this.accentTag = accentTag;
    }

    public boolean isSoldOut() {
        return getAvailableSeats() <= 0;
    }

    public double getOccupancyRate() {
        if (totalCapacity <= 0) return 0.0;
        return (double) bookedSeats / totalCapacity * 100.0;
    }
}
