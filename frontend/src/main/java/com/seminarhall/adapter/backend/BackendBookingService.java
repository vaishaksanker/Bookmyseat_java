package com.seminarhall.adapter.backend;

import com.bookmyshow.model.Payment;
import com.bookmyshow.service.PaymentResult;
import com.seminarhall.model.Booking;
import com.seminarhall.model.OrganizerAnalytics;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.ServiceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.seminarhall.adapter.backend.Mappers.call;
import static com.seminarhall.adapter.backend.Mappers.parseId;

/**
 * BookingService backed by the backend booking, payment and ticket services.
 *
 * createBooking runs the backend flow in order: reserve seats (PENDING) → charge the
 * backend-calculated total (free events are confirmed without a charge) → CONFIRMED →
 * ticket. If anything fails after the seats were reserved, the seats are released.
 * Failed payment attempts are not shown in booking lists.
 */
public class BackendBookingService implements BookingService {

    private final BackendContext ctx;

    public BackendBookingService(BackendContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Booking createBooking(String eventId, String userId, List<String> seatIds,
                                 String paymentMethod, String transactionId) throws ServiceException {
        if (eventId == null || userId == null || seatIds == null || seatIds.isEmpty()) {
            throw new ServiceException("Invalid booking request. Missing event, user, or seat selections.");
        }
        int event = parseId(eventId, "Event");
        int user = parseId(userId, "User");
        List<Integer> seats = Mappers.parseIds(seatIds, "Seat");

        com.bookmyshow.model.Booking pending = call(() -> {
            com.bookmyshow.model.User booker = ctx.userDAO.findById(user)
                    .orElseThrow(() -> new ServiceException("User not found with ID: " + userId));
            return ctx.bookingService.createBooking(booker, event, seats);
        });

        int bookingId = pending.getId();
        try {
            call(() -> {
                if (pending.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                    ctx.bookingService.confirmBooking(bookingId);   // free registration, nothing to charge
                } else {
                    PaymentResult result = ctx.paymentService.processPayment(bookingId, pending.getTotalAmount());
                    if (!result.isSuccess()) {
                        // the backend already marked the booking FAILED and released the seats
                        throw new ServiceException(result.getMessage()
                                + ". Your seats have been released and nothing was charged.");
                    }
                }
                ctx.ticketService.generateTicket(bookingId);
                return null;
            });
        } catch (ServiceException e) {
            releaseIfStillHeld(bookingId);
            throw e;
        }
        return getBookingById(Mappers.bookingId(bookingId));
    }

    @Override
    public Booking getBookingById(String bookingId) throws ServiceException {
        int id = parseId(bookingId, "Booking");
        return call(() -> toUi(ctx.bookingService.getBookingById(id), new HashMap<>(), new HashMap<>()));
    }

    @Override
    public List<Booking> getBookingsForUser(String userId) throws ServiceException {
        if (userId == null) {
            return List.of();
        }
        int id = parseId(userId, "User");
        return call(() -> toUi(ctx.bookingService.getBookingsByUser(id)));
    }

    @Override
    public List<Booking> getBookingsForEvent(String eventId) throws ServiceException {
        if (eventId == null) {
            return List.of();
        }
        int id = parseId(eventId, "Event");
        return call(() -> toUi(ctx.bookingDAO.findByEventId(id)));
    }

    @Override
    public List<Booking> getAllBookings() throws ServiceException {
        return call(() -> {
            List<com.bookmyshow.model.Booking> all = new ArrayList<>();
            for (com.bookmyshow.model.Event event : ctx.eventService.getAllEvents()) {
                all.addAll(ctx.bookingDAO.findByEventId(event.getId()));
            }
            return toUi(all);
        });
    }

    @Override
    public void cancelBooking(String bookingId) throws ServiceException {
        int id = parseId(bookingId, "Booking");
        call(() -> ctx.bookingService.cancelBooking(id));
    }

    @Override
    public OrganizerAnalytics getOrganizerAnalytics() throws ServiceException {
        return call(() -> {
            List<com.bookmyshow.model.Event> events = ctx.eventService.getAllEvents();
            int upcoming = 0;
            int totalSeats = 0;
            int bookedSeats = 0;
            int totalBookings = 0;
            double revenue = 0;
            List<OrganizerAnalytics.EventOccupancySummary> summaries = new ArrayList<>();
            for (com.bookmyshow.model.Event event : events) {
                if (!event.getEventDateTime().toLocalDate().isBefore(LocalDate.now())) {
                    upcoming++;
                }
                int booked = event.getTotalSeats() - event.getAvailableSeats();
                totalSeats += event.getTotalSeats();
                bookedSeats += booked;
                for (com.bookmyshow.model.Booking booking : ctx.bookingDAO.findByEventId(event.getId())) {
                    if (isListed(booking)) {
                        totalBookings++;
                    }
                    if (booking.isConfirmed()) {
                        revenue += booking.getTotalAmount().doubleValue();
                    }
                }
                summaries.add(new OrganizerAnalytics.EventOccupancySummary(
                        Mappers.eventId(event.getId()), event.getName(), event.getTotalSeats(), booked));
            }
            double occupancy = totalSeats > 0 ? (double) bookedSeats / totalSeats * 100.0 : 0.0;
            return new OrganizerAnalytics(events.size(), upcoming, totalBookings, totalSeats, bookedSeats,
                    Math.max(0, totalSeats - bookedSeats), occupancy, revenue, summaries);
        });
    }

    private void releaseIfStillHeld(int bookingId) {
        try {
            com.bookmyshow.model.Booking booking = ctx.bookingService.getBookingById(bookingId);
            if (booking.isPending()) {
                ctx.bookingService.failBooking(bookingId);
            }
        } catch (RuntimeException ignored) {
            // best effort: the original error is the one worth reporting
        }
    }

    /** Failed payment attempts are not bookings from the user's point of view. */
    private static boolean isListed(com.bookmyshow.model.Booking booking) {
        return booking.getStatus() != com.bookmyshow.model.enums.BookingStatus.FAILED;
    }

    private List<Booking> toUi(List<com.bookmyshow.model.Booking> bookings) throws ServiceException {
        Map<Integer, com.bookmyshow.model.Event> events = new HashMap<>();
        Map<Integer, com.bookmyshow.model.User> users = new HashMap<>();
        List<Booking> result = new ArrayList<>();
        for (com.bookmyshow.model.Booking booking : bookings) {
            if (isListed(booking)) {
                result.add(toUi(booking, events, users));
            }
        }
        result.sort(Comparator.comparing(Booking::getBookingTime).reversed());
        return result;
    }

    private Booking toUi(com.bookmyshow.model.Booking booking,
                         Map<Integer, com.bookmyshow.model.Event> events,
                         Map<Integer, com.bookmyshow.model.User> users) throws ServiceException {
        com.bookmyshow.model.Event event = events.computeIfAbsent(booking.getEventId(),
                id -> ctx.eventDAO.findById(id).orElse(null));
        com.bookmyshow.model.User user = users.computeIfAbsent(booking.getUserId(),
                id -> ctx.userDAO.findById(id).orElse(null));
        if (event == null || user == null) {
            throw new ServiceException("Booking " + booking.getId() + " refers to a missing event or user.");
        }
        Payment payment = ctx.paymentDAO.findByBookingId(booking.getId()).orElse(null);
        return Mappers.booking(booking, event, user, payment);
    }
}
