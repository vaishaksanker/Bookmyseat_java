package com.seminarhall.adapter.backend;

import com.bookmyshow.exception.DataAccessException;
import com.bookmyshow.exception.SeatNotAvailableException;
import com.bookmyshow.model.BookingSeat;
import com.bookmyshow.model.Payment;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.EventStatus;
import com.bookmyshow.model.enums.Role;
import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.ServiceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates between the backend model (int ids, BigDecimal, Venue objects) and the
 * UI model (string ids, doubles, building + hall names), and backend exceptions
 * into the ServiceException the UI already handles.
 */
final class Mappers {

    /** Events stored without an end time are shown as two hours long. */
    static final int DEFAULT_DURATION_HOURS = 2;

    private Mappers() {
    }

    @FunctionalInterface
    interface BackendCall<T> {
        T run() throws ServiceException;
    }

    /** Runs backend code and rethrows its unchecked exceptions as ServiceException with a UI-friendly message. */
    static <T> T call(BackendCall<T> work) throws ServiceException {
        try {
            return work.run();
        } catch (SeatNotAvailableException e) {
            throw new ServiceException("One or more of the selected seats were just booked by someone else. "
                    + "Please choose different seats.", e);
        } catch (DataAccessException e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new ServiceException("Database error: " + root.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ServiceException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // --- ids ---

    static String userId(int id) { return "usr_" + id; }
    static String eventId(int id) { return "ev_" + id; }
    static String seatId(int id) { return String.valueOf(id); }
    static String bookingId(int id) { return String.format("BK%06d", id); }

    /** Accepts any of the ids above ("ev_12", "BK000012", "12") and returns the numeric part. */
    static int parseId(String id, String what) throws ServiceException {
        if (id != null) {
            String digits = id.trim().replaceFirst("^[^0-9]*", "");
            if (!digits.isEmpty()) {
                try {
                    return Integer.parseInt(digits);
                } catch (NumberFormatException ignored) {
                    // falls through to "not found"
                }
            }
        }
        throw new ServiceException(what + " not found: " + id);
    }

    static List<Integer> parseIds(List<String> ids, String what) throws ServiceException {
        List<Integer> result = new java.util.ArrayList<>();
        for (String id : ids) {
            result.add(parseId(id, what));
        }
        return result;
    }

    // --- users ---

    static Role toBackendRole(UserRole role) {
        return role == UserRole.ORGANIZER ? Role.ORGANIZER : Role.CUSTOMER;
    }

    static UserRole toUiRole(Role role) {
        return role == Role.CUSTOMER ? UserRole.STUDENT : UserRole.ORGANIZER;
    }

    static User user(com.bookmyshow.model.User user) {
        User ui = new User(userId(user.getId()), user.getName(), user.getEmail(), "", toUiRole(user.getRole()));
        ui.setCreatedAt(user.getCreatedAt());
        return ui;
    }

    // --- events ---

    static EventCategory category(String name) {
        if (name != null) {
            try {
                EventCategory category = EventCategory.valueOf(name);
                if (category != EventCategory.ALL) {
                    return category;
                }
            } catch (IllegalArgumentException ignored) {
                // unknown category stored by another client
            }
        }
        return EventCategory.TECHNICAL_SEMINAR;
    }

    /** The UI's "venue" is the building (backend Venue.address); "seminarHall" is the hall (Venue.name). */
    static Event event(com.bookmyshow.model.Event event, com.bookmyshow.model.User organizer) {
        LocalDateTime start = event.getEventDateTime();
        LocalDateTime end = event.getEndDateTime() != null
                ? event.getEndDateTime() : start.plusHours(DEFAULT_DURATION_HOURS);
        Venue venue = event.getVenue();
        String hall = venue != null ? venue.getName() : "";
        String building = venue != null && venue.getAddress() != null && !venue.getAddress().isBlank()
                ? venue.getAddress() : hall;
        int booked = Math.max(0, event.getTotalSeats() - event.getAvailableSeats());
        double price = event.getTicketPrice() != null ? event.getTicketPrice().doubleValue() : 0.0;

        Event ui = new Event(
                eventId(event.getId()),
                event.getName(),
                event.getDescription() != null ? event.getDescription() : "",
                category(event.getCategory()),
                start.toLocalDate(),
                start.toLocalTime(),
                end.toLocalTime(),
                building,
                hall,
                organizer != null ? organizer.getName() : "Organizer",
                organizer != null ? organizer.getEmail() : "",
                price,
                event.getTotalSeats(),
                booked,
                null,
                price == 0.0 ? "FREE ENTRY" : category(event.getCategory()).name()
        );
        ui.setCancelled(event.getStatus() == EventStatus.CANCELLED);
        return ui;
    }

    // --- bookings ---

    static BookingStatus bookingStatus(com.bookmyshow.model.enums.BookingStatus status) {
        switch (status) {
            case CONFIRMED: return BookingStatus.CONFIRMED;
            case PENDING:   return BookingStatus.PENDING;
            default:        return BookingStatus.CANCELLED;   // CANCELLED and FAILED
        }
    }

    static Booking booking(com.bookmyshow.model.Booking booking, com.bookmyshow.model.Event event,
                           com.bookmyshow.model.User user, Payment payment) {
        Event uiEvent = event(event, null);
        boolean free = booking.getTotalAmount() == null || booking.getTotalAmount().compareTo(BigDecimal.ZERO) == 0;
        Booking ui = new Booking(
                bookingId(booking.getId()),
                eventId(event.getId()),
                event.getName(),
                uiEvent.getVenue(),
                uiEvent.getSeminarHall(),
                event.getEventDateTime(),
                userId(user.getId()),
                user.getName(),
                user.getEmail(),
                booking.getBookingSeats().stream().map(BookingSeat::getSeatLabel).collect(Collectors.toList()),
                booking.getBookingSeats().stream().map(s -> seatId(s.getSeatId())).collect(Collectors.toList()),
                free ? 0.0 : booking.getTotalAmount().doubleValue(),
                bookingStatus(booking.getStatus()),
                free ? "Free Registration" : "Online Payment",
                payment != null ? payment.getTransactionReference() : "FREE-REG-" + booking.getId()
        );
        ui.setBookingTime(booking.getBookingTime());
        return ui;
    }
}
