package com.seminarhall.service.mock;

import com.seminarhall.model.*;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.ServiceException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MockBookingService implements BookingService {
    private final MockDataStore dataStore;

    public MockBookingService(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public synchronized Booking createBooking(String eventId, String userId, List<String> seatIds,
                                              String paymentMethod, String transactionId) throws ServiceException {
        if (eventId == null || userId == null || seatIds == null || seatIds.isEmpty()) {
            throw new ServiceException("Invalid booking request. Missing event, user, or seat selections.");
        }

        Event event = dataStore.getEvents().get(eventId);
        if (event == null) throw new ServiceException("Event not found with ID: " + eventId);
        if (event.isCancelled()) throw new ServiceException("This event has been cancelled and cannot be booked.");

        // Check and validate user
        User user = null;
        for (User u : dataStore.getUsers().values()) {
            if (u.getId().equals(userId)) {
                user = u;
                break;
            }
        }
        if (user == null) throw new ServiceException("User not found with ID: " + userId);

        // Validate seats atomically
        List<Seat> hallSeats = dataStore.getEventSeats().get(eventId);
        if (hallSeats == null) throw new ServiceException("Seating configuration missing for event.");

        List<Seat> selectedSeats = new ArrayList<>();
        List<String> seatIdentifiers = new ArrayList<>();
        double calculatedTotal = 0;

        for (String sId : seatIds) {
            Seat found = null;
            for (Seat s : hallSeats) {
                if (s.getId().equals(sId)) {
                    found = s;
                    break;
                }
            }
            if (found == null) {
                throw new ServiceException("Seat " + sId + " does not exist.");
            }
            if (found.getStatus() != SeatStatus.AVAILABLE) {
                throw new ServiceException("Seat " + found.getSeatIdentifier() + " was just booked by another user! Please choose other seats.");
            }
            selectedSeats.add(found);
            seatIdentifiers.add(found.getSeatIdentifier());
            calculatedTotal += found.getPrice();
        }

        // Mark seats as BOOKED in dataStore
        for (Seat s : selectedSeats) {
            s.setStatus(SeatStatus.BOOKED);
        }

        // Update event booked seat counter
        event.setBookedSeats(event.getBookedSeats() + selectedSeats.size());

        // Generate Booking ID
        String bookingId = "BK-" + (10000 + new Random().nextInt(90000));
        Booking booking = new Booking(
                bookingId,
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getSeminarHall(),
                LocalDateTime.of(event.getDate(), event.getStartTime()),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                seatIdentifiers,
                seatIds,
                calculatedTotal,
                BookingStatus.CONFIRMED,
                paymentMethod != null ? paymentMethod : "Simulated Direct",
                transactionId != null ? transactionId : "TXN-" + System.currentTimeMillis()
        );

        dataStore.getBookings().put(booking.getId(), booking);

        // Generate tickets
        for (String seatIdfr : seatIdentifiers) {
            String ticketNum = "TKT-" + booking.getId() + "-" + seatIdfr;
            Ticket t = new Ticket(
                    ticketNum,
                    booking.getId(),
                    event.getTitle(),
                    event.getVenue(),
                    event.getSeminarHall(),
                    booking.getEventDateTime(),
                    user.getFullName(),
                    user.getEmail(),
                    seatIdfr,
                    event.getTicketPrice(),
                    "VERIFIED:EVENT:" + event.getId() + ":BOOKING:" + booking.getId() + ":SEAT:" + seatIdfr
            );
            dataStore.getTickets().put(ticketNum, t);
        }

        return booking;
    }

    @Override
    public Booking getBookingById(String bookingId) throws ServiceException {
        if (bookingId == null) throw new ServiceException("Booking ID is required.");
        Booking booking = dataStore.getBookings().get(bookingId);
        if (booking == null) throw new ServiceException("Booking not found: " + bookingId);
        return booking;
    }

    @Override
    public List<Booking> getBookingsForUser(String userId) {
        if (userId == null) return Collections.emptyList();
        return dataStore.getBookings().values().stream()
                .filter(b -> userId.equals(b.getUserId()))
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getBookingsForEvent(String eventId) {
        if (eventId == null) return Collections.emptyList();
        return dataStore.getBookings().values().stream()
                .filter(b -> eventId.equals(b.getEventId()))
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getAllBookings() {
        return dataStore.getBookings().values().stream()
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void cancelBooking(String bookingId) throws ServiceException {
        Booking booking = getBookingById(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ServiceException("This booking is already cancelled.");
        }

        // Release seats
        List<Seat> seats = dataStore.getEventSeats().get(booking.getEventId());
        if (seats != null) {
            for (Seat s : seats) {
                if (booking.getSeatIds().contains(s.getId())) {
                    s.setStatus(SeatStatus.AVAILABLE);
                }
            }
        }

        // Update event counter
        Event ev = dataStore.getEvents().get(booking.getEventId());
        if (ev != null) {
            ev.setBookedSeats(Math.max(0, ev.getBookedSeats() - booking.getSeatIds().size()));
        }

        booking.setStatus(BookingStatus.CANCELLED);
    }

    @Override
    public OrganizerAnalytics getOrganizerAnalytics() {
        int totalEvents = dataStore.getEvents().size();
        int upcomingEvents = 0;
        int totalSeats = 0;
        int bookedSeats = 0;
        double totalRevenue = 0;

        List<OrganizerAnalytics.EventOccupancySummary> summaries = new ArrayList<>();

        for (Event e : dataStore.getEvents().values()) {
            if (!e.getDate().isBefore(java.time.LocalDate.now())) {
                upcomingEvents++;
            }
            totalSeats += e.getTotalCapacity();
            bookedSeats += e.getBookedSeats();
            totalRevenue += (e.getBookedSeats() * e.getTicketPrice());

            summaries.add(new OrganizerAnalytics.EventOccupancySummary(
                    e.getId(), e.getTitle(), e.getTotalCapacity(), e.getBookedSeats()
            ));
        }

        int availableSeats = Math.max(0, totalSeats - bookedSeats);
        double overallOccupancy = totalSeats > 0 ? ((double) bookedSeats / totalSeats * 100.0) : 0.0;

        return new OrganizerAnalytics(
                totalEvents,
                upcomingEvents,
                dataStore.getBookings().size(),
                totalSeats,
                bookedSeats,
                availableSeats,
                overallOccupancy,
                totalRevenue,
                summaries
        );
    }
}
