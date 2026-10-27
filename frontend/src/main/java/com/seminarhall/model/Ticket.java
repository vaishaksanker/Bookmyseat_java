package com.seminarhall.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ticketNumber;
    private String bookingId;
    private String eventTitle;
    private String venue;
    private String seminarHall;
    private LocalDateTime eventDateTime;
    private String attendeeName;
    private String attendeeEmail;
    private String seatIdentifier;
    private double price;
    private String qrPayload;

    public Ticket() {
    }

    public Ticket(String ticketNumber, String bookingId, String eventTitle, String venue,
                  String seminarHall, LocalDateTime eventDateTime, String attendeeName,
                  String attendeeEmail, String seatIdentifier, double price, String qrPayload) {
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.eventTitle = eventTitle;
        this.venue = venue;
        this.seminarHall = seminarHall;
        this.eventDateTime = eventDateTime;
        this.attendeeName = attendeeName;
        this.attendeeEmail = attendeeEmail;
        this.seatIdentifier = seatIdentifier;
        this.price = price;
        this.qrPayload = qrPayload;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setUserEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }

    public String getSeatIdentifier() {
        return seatIdentifier;
    }

    public void setSeatIdentifier(String seatIdentifier) {
        this.seatIdentifier = seatIdentifier;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }
}
