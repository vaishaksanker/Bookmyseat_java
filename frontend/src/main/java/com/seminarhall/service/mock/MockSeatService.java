package com.seminarhall.service.mock;

import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.service.SeatService;
import com.seminarhall.service.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class MockSeatService implements SeatService {
    private final MockDataStore dataStore;

    public MockSeatService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Seat> getSeatsForEvent(String eventId) throws ServiceException {
        if (eventId == null) throw new ServiceException("Event ID cannot be null.");
        List<Seat> seats = dataStore.getEventSeats().get(eventId);
        if (seats == null) {
            throw new ServiceException("No seating configuration found for event: " + eventId);
        }
        // Return a defensive copy to prevent unintentional state mutation outside service
        List<Seat> copy = new ArrayList<>();
        for (Seat s : seats) {
            copy.add(new Seat(s.getId(), s.getEventId(), s.getRowLabel(), s.getSeatNumber(),
                    s.getStatus(), s.getPrice(), s.isAccessible()));
        }
        return copy;
    }

    @Override
    public boolean validateSeatsAvailable(String eventId, List<String> seatIds) throws ServiceException {
        if (eventId == null || seatIds == null || seatIds.isEmpty()) {
            return false;
        }
        List<Seat> seats = dataStore.getEventSeats().get(eventId);
        if (seats == null) return false;

        for (String id : seatIds) {
            boolean found = false;
            for (Seat s : seats) {
                if (s.getId().equals(id)) {
                    found = true;
                    if (s.getStatus() != SeatStatus.AVAILABLE) {
                        return false;
                    }
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public void holdSeats(String eventId, List<String> seatIds, String userId) throws ServiceException {
        // In real backend, this creates temporary holds. In mock, validates availability.
        if (!validateSeatsAvailable(eventId, seatIds)) {
            throw new ServiceException("One or more chosen seats are no longer available. Please select different seats.");
        }
    }

    @Override
    public void releaseSeats(String eventId, List<String> seatIds) {
        // Releases temporary reservation
    }
}
