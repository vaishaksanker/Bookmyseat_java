package com.bookmyshow.service;

import com.bookmyshow.dao.BookingDAO;
import com.bookmyshow.dao.EventDAO;
import com.bookmyshow.dao.SeatDAO;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.EventNotFoundException;
import com.bookmyshow.exception.SeatNotAvailableException;
import com.bookmyshow.exception.ValidationException;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.User;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.BookingStatus;
import com.bookmyshow.model.enums.EventStatus;
import com.bookmyshow.model.enums.Role;
import com.bookmyshow.model.enums.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl Unit Tests")
class BookingServiceImplTest {

    @Mock private BookingDAO bookingDAO;
    @Mock private SeatDAO seatDAO;
    @Mock private EventDAO eventDAO;

    private BookingServiceImpl bookingService;

    private User customer;
    private Event publishedEvent;
    private Seat seatA1;
    private Seat seatA2;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingDAO, seatDAO, eventDAO);

        customer = new User(1, "Alice", "alice@example.com", "hash", Role.CUSTOMER);

        Venue venue = new Venue(10, "Hall", "Addr", "City", 100);
        publishedEvent = new Event(5, "Concert", "Desc", venue, 99,
                LocalDateTime.now().plusDays(10), new BigDecimal("200.00"), 100);
        publishedEvent.setStatus(EventStatus.PUBLISHED);
        publishedEvent.setAvailableSeats(100);

        seatA1 = new Seat(101, 10, 5, "A", 1);
        seatA1.setStatus(SeatStatus.AVAILABLE);

        seatA2 = new Seat(102, 10, 5, "A", 2);
        seatA2.setStatus(SeatStatus.AVAILABLE);
    }

    // =========================================================
    // createBooking — happy path
    // =========================================================

    @Test
    @DisplayName("createBooking: valid request creates booking with correct total")
    void createBooking_valid_returnsBookingWithCorrectTotal() {
        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));
        when(seatDAO.findByIds(List.of(101, 102))).thenReturn(List.of(seatA1, seatA2));
        doNothing().when(seatDAO).reserveSeats(anyList());
        doNothing().when(bookingDAO).createBookingSeats(anyList());

        Booking saved = new Booking(1, customer.getId(), 5, new BigDecimal("400.00"));
        when(bookingDAO.create(any())).thenReturn(saved);

        Booking result = bookingService.createBooking(customer, 5, List.of(101, 102));

        assertNotNull(result);
        // Total = 200.00 * 2 = 400.00
        assertEquals(new BigDecimal("400.00"), result.getTotalAmount());
        verify(seatDAO).reserveSeats(List.of(101, 102));
        verify(bookingDAO).createBookingSeats(anyList());
    }

    @Test
    @DisplayName("createBooking: single seat calculates correct total")
    void createBooking_oneSeat_correctTotal() {
        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));
        when(seatDAO.findByIds(List.of(101))).thenReturn(List.of(seatA1));
        doNothing().when(seatDAO).reserveSeats(anyList());
        doNothing().when(bookingDAO).createBookingSeats(anyList());

        Booking saved = new Booking(2, customer.getId(), 5, new BigDecimal("200.00"));
        when(bookingDAO.create(any())).thenReturn(saved);

        Booking result = bookingService.createBooking(customer, 5, List.of(101));
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());
    }

    // =========================================================
    // createBooking — validation failures
    // =========================================================

    @Test
    @DisplayName("createBooking: null user throws ValidationException")
    void createBooking_nullUser_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(null, 5, List.of(101)));
    }

    @Test
    @DisplayName("createBooking: empty seat list throws ValidationException")
    void createBooking_emptySeatList_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(customer, 5, List.of()));
    }

    @Test
    @DisplayName("createBooking: null seat list throws ValidationException")
    void createBooking_nullSeatList_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(customer, 5, null));
    }

    @Test
    @DisplayName("createBooking: duplicate seat IDs throw ValidationException")
    void createBooking_duplicateSeatIds_throwsValidation() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(customer, 5, List.of(101, 101)));
    }

    @Test
    @DisplayName("createBooking: event not found throws EventNotFoundException")
    void createBooking_eventNotFound_throwsEventNotFound() {
        when(eventDAO.findById(99)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () ->
                bookingService.createBooking(customer, 99, List.of(101)));
    }

    @Test
    @DisplayName("createBooking: non-bookable event throws BookingException")
    void createBooking_cancelledEvent_throwsBookingException() {
        publishedEvent.setStatus(EventStatus.CANCELLED);
        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));

        assertThrows(BookingException.class, () ->
                bookingService.createBooking(customer, 5, List.of(101)));
    }

    @Test
    @DisplayName("createBooking: seat not belonging to event throws ValidationException")
    void createBooking_seatWrongEvent_throwsValidation() {
        Seat wrongEventSeat = new Seat(101, 10, 999, "A", 1); // eventId=999, not 5
        wrongEventSeat.setStatus(SeatStatus.AVAILABLE);

        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));
        when(seatDAO.findByIds(List.of(101))).thenReturn(List.of(wrongEventSeat));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(customer, 5, List.of(101)));
    }

    @Test
    @DisplayName("createBooking: unavailable seat throws SeatNotAvailableException")
    void createBooking_unavailableSeat_throwsSeatNotAvailable() {
        seatA1.setStatus(SeatStatus.BOOKED);

        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));
        when(seatDAO.findByIds(List.of(101))).thenReturn(List.of(seatA1));

        assertThrows(SeatNotAvailableException.class, () ->
                bookingService.createBooking(customer, 5, List.of(101)));
    }

    @Test
    @DisplayName("createBooking: seat not found throws ValidationException")
    void createBooking_seatNotFound_throwsValidation() {
        // Only 1 seat returned for 2 requested IDs
        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));
        when(seatDAO.findByIds(List.of(101, 102))).thenReturn(List.of(seatA1));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(customer, 5, List.of(101, 102)));
    }

    // =========================================================
    // createBooking — double-booking scenario
    // =========================================================

    @Test
    @DisplayName("createBooking: reserveSeats failure marks booking FAILED")
    void createBooking_reserveSeatsFailure_bookingMarkedFailed() {
        when(eventDAO.findById(5)).thenReturn(Optional.of(publishedEvent));
        when(seatDAO.findByIds(List.of(101))).thenReturn(List.of(seatA1));

        Booking pendingBooking = new Booking(7, customer.getId(), 5, new BigDecimal("200.00"));
        when(bookingDAO.create(any())).thenReturn(pendingBooking);

        // Simulate concurrent reservation failure (another user grabbed the seat)
        doThrow(new SeatNotAvailableException(101))
                .when(seatDAO).reserveSeats(anyList());
        when(bookingDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(SeatNotAvailableException.class, () ->
                bookingService.createBooking(customer, 5, List.of(101)));

        // Verify the booking was marked FAILED
        verify(bookingDAO).update(argThat(b -> BookingStatus.FAILED.equals(b.getStatus())));
    }

    // =========================================================
    // confirmBooking
    // =========================================================

    @Test
    @DisplayName("confirmBooking: PENDING booking transitions to CONFIRMED")
    void confirmBooking_pending_becomesConfirmed() {
        Booking pending = new Booking(1, customer.getId(), 5, new BigDecimal("200.00"));
        when(bookingDAO.findById(1)).thenReturn(Optional.of(pending));
        when(bookingDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.confirmBooking(1);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    @DisplayName("confirmBooking: CONFIRMED booking throws BookingException")
    void confirmBooking_alreadyConfirmed_throwsException() {
        Booking confirmed = new Booking(1, customer.getId(), 5, new BigDecimal("200.00"));
        confirmed.setStatus(BookingStatus.CONFIRMED);
        when(bookingDAO.findById(1)).thenReturn(Optional.of(confirmed));

        assertThrows(BookingException.class, () -> bookingService.confirmBooking(1));
    }

    // =========================================================
    // cancelBooking
    // =========================================================

    @Test
    @DisplayName("cancelBooking: cancels a PENDING booking")
    void cancelBooking_pending_becomesCancelled() {
        Booking pending = new Booking(1, customer.getId(), 5, new BigDecimal("200.00"));
        when(bookingDAO.findById(1)).thenReturn(Optional.of(pending));
        when(bookingDAO.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.cancelBooking(1);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("cancelBooking: already cancelled throws BookingException")
    void cancelBooking_alreadyCancelled_throwsException() {
        Booking cancelled = new Booking(1, customer.getId(), 5, new BigDecimal("200.00"));
        cancelled.setStatus(BookingStatus.CANCELLED);
        when(bookingDAO.findById(1)).thenReturn(Optional.of(cancelled));

        assertThrows(BookingException.class, () -> bookingService.cancelBooking(1));
    }

    // =========================================================
    // getBookingById
    // =========================================================

    @Test
    @DisplayName("getBookingById: not found throws BookingException")
    void getBookingById_notFound_throwsException() {
        when(bookingDAO.findById(99)).thenReturn(Optional.empty());
        assertThrows(BookingException.class, () -> bookingService.getBookingById(99));
    }
}
