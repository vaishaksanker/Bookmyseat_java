package com.seminarhall.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String eventId;
    private String eventTitle;
    private String venue;
    private String seminarHall;
    private LocalDateTime eventDateTime;
    private String userId;
    private String userName;
    private String userEmail;
    private List<String> seatIdentifiers;
    private List<String> seatIds;
    private double totalAmount;
    private BookingStatus status;
    private LocalDateTime bookingTime;
    private String paymentMethod;
    private String transactionId;

    public Booking() {
        this.seatIdentifiers = new ArrayList<>();
        this.seatIds = new ArrayList<>();
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED;
    }

    public Booking(String id, String eventId, String eventTitle, String venue, String seminarHall,
                   LocalDateTime eventDateTime, String userId, String userName, String userEmail,
                   List<String> seatIdentifiers, List<String> seatIds, double totalAmount,
                   BookingStatus status, String paymentMethod, String transactionId) {
        this.id = id;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.venue = venue;
        this.seminarHall = seminarHall;
        this.eventDateTime = eventDateTime;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.seatIdentifiers = seatIdentifiers != null ? new ArrayList<>(seatIdentifiers) : new ArrayList<>();
        this.seatIds = seatIds != null ? new ArrayList<>(seatIds) : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.status = status;
        this.bookingTime = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
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

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<String> getSeatIdentifiers() {
        return seatIdentifiers;
    }

    public void setSeatIdentifiers(List<String> seatIdentifiers) {
        this.seatIdentifiers = seatIdentifiers;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFormattedSeats() {
        return String.join(", ", seatIdentifiers);
    }
}
