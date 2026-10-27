package com.seminarhall.service;

import com.seminarhall.model.Seat;

import java.util.List;

public interface SeatService {
    List<Seat> getSeatsForEvent(String eventId) throws ServiceException;
    boolean validateSeatsAvailable(String eventId, List<String> seatIds) throws ServiceException;
    void holdSeats(String eventId, List<String> seatIds, String userId) throws ServiceException;
    void releaseSeats(String eventId, List<String> seatIds) throws ServiceException;
}
