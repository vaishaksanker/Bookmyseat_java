package com.seminarhall.service;

import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {
    private BookingService bookingService;
    private SeatService seatService;

    @BeforeEach
    void setUp() {
        ServiceFactory.initMockServices();
        bookingService = ServiceFactory.getBookingService();
        seatService = ServiceFactory.getSeatService();
    }

    @Test
    @DisplayName("Test seat selection and successful reservation flow")
    void testBookingCreationSuccess() throws ServiceException {
        String eventId = "ev_101";
        List<Seat> seats = seatService.getSeatsForEvent(eventId);

        // Find 2 available seats
        List<String> chosenSeatIds = new ArrayList<>();
        List<Seat> chosenSeats = new ArrayList<>();
        for (Seat s : seats) {
            if (s.getStatus() == SeatStatus.AVAILABLE) {
                chosenSeatIds.add(s.getId());
                chosenSeats.add(s);
                if (chosenSeatIds.size() == 2) break;
            }
        }
        assertEquals(2, chosenSeatIds.size());

        // Create booking
        Booking booking = bookingService.createBooking(
                eventId,
                "usr_student_1",
                chosenSeatIds,
                "Simulated UPI",
                "TXN_UNIT_TEST"
        );

        assertNotNull(booking);
        assertNotNull(booking.getId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(2, booking.getSeatIds().size());

        // Verify seats are now booked
        List<Seat> refreshedSeats = seatService.getSeatsForEvent(eventId);
        for (Seat s : refreshedSeats) {
            if (chosenSeatIds.contains(s.getId())) {
                assertEquals(SeatStatus.BOOKED, s.getStatus());
            }
        }
    }

    @Test
    @DisplayName("Test conflict when trying to book already booked seat")
    void testBookingAlreadyBookedSeat() throws ServiceException {
        String eventId = "ev_101";
        List<Seat> seats = seatService.getSeatsForEvent(eventId);

        // Pick an already booked seat
        String bookedSeatId = null;
        for (Seat s : seats) {
            if (s.getStatus() == SeatStatus.BOOKED) {
                bookedSeatId = s.getId();
                break;
            }
        }
        assertNotNull(bookedSeatId, "Should have seeded booked seats");

        final String targetId = bookedSeatId;
        assertThrows(ServiceException.class, () -> {
            bookingService.createBooking(eventId, "usr_student_1", Collections.singletonList(targetId), "UPI", "TXN_TEST");
        });
    }

    @Test
    @DisplayName("Test booking cancellation releases seats back to AVAILABLE")
    void testBookingCancellation() throws ServiceException {
        // Book seat
        List<Seat> seats = seatService.getSeatsForEvent("ev_102");
        Seat availableSeat = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).findFirst().orElseThrow();

        Booking booking = bookingService.createBooking(
                "ev_102",
                "usr_student_1",
                Collections.singletonList(availableSeat.getId()),
                "UPI",
                "TXN_CANCEL_TEST"
        );

        // Cancel it
        bookingService.cancelBooking(booking.getId());

        Booking reloaded = bookingService.getBookingById(booking.getId());
        assertEquals(BookingStatus.CANCELLED, reloaded.getStatus());

        // Check seat is available again
        List<Seat> reloadedSeats = seatService.getSeatsForEvent("ev_102");
        Seat target = reloadedSeats.stream().filter(s -> s.getId().equals(availableSeat.getId())).findFirst().orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, target.getStatus());
    }
}
