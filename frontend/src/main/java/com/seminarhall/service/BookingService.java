package com.seminarhall.service;

import com.seminarhall.model.Booking;
import com.seminarhall.model.OrganizerAnalytics;

import java.util.List;

public interface BookingService {
    Booking createBooking(String eventId, String userId, List<String> seatIds,
                          String paymentMethod, String transactionId) throws ServiceException;
    Booking getBookingById(String bookingId) throws ServiceException;
    List<Booking> getBookingsForUser(String userId) throws ServiceException;
    List<Booking> getBookingsForEvent(String eventId) throws ServiceException;
    List<Booking> getAllBookings() throws ServiceException;
    void cancelBooking(String bookingId) throws ServiceException;
    OrganizerAnalytics getOrganizerAnalytics() throws ServiceException;
}
