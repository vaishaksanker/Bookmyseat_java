package com.seminarhall.adapter.backend;

import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.service.SeatService;
import com.seminarhall.service.ServiceException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.seminarhall.adapter.backend.Mappers.call;
import static com.seminarhall.adapter.backend.Mappers.parseId;

/**
 * SeatService backed by SeatDAO. RESERVED (awaiting payment) and BOOKED seats both show as taken.
 * Seats are actually reserved by BookingService.createBooking, so holdSeats only re-validates.
 */
public class BackendSeatService implements SeatService {

    private final BackendContext ctx;

    public BackendSeatService(BackendContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public List<Seat> getSeatsForEvent(String eventId) throws ServiceException {
        int id = parseId(eventId, "Event");
        return call(() -> {
            com.bookmyshow.model.Event event = ctx.eventService.getEventById(id);
            double price = event.getTicketPrice().doubleValue();
            return ctx.seatDAO.findByEventId(id).stream()
                    .map(s -> new Seat(Mappers.seatId(s.getId()), Mappers.eventId(id), s.getSeatRow(),
                            s.getSeatNumber(),
                            s.isAvailable() ? SeatStatus.AVAILABLE : SeatStatus.BOOKED,
                            price,
                            "A".equals(s.getSeatRow()) && (s.getSeatNumber() == 1 || s.getSeatNumber() == 10)))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public boolean validateSeatsAvailable(String eventId, List<String> seatIds) throws ServiceException {
        if (eventId == null || seatIds == null || seatIds.isEmpty()) {
            return false;
        }
        int event = parseId(eventId, "Event");
        List<Integer> ids = Mappers.parseIds(seatIds, "Seat");
        return call(() -> {
            Map<Integer, com.bookmyshow.model.Seat> found = ctx.seatDAO.findByIds(ids).stream()
                    .collect(Collectors.toMap(com.bookmyshow.model.Seat::getId, Function.identity()));
            return ids.stream().allMatch(seatId -> {
                com.bookmyshow.model.Seat seat = found.get(seatId);
                return seat != null && seat.getEventId() == event && seat.isAvailable();
            });
        });
    }

    @Override
    public void holdSeats(String eventId, List<String> seatIds, String userId) throws ServiceException {
        if (!validateSeatsAvailable(eventId, seatIds)) {
            throw new ServiceException("One or more chosen seats are no longer available. Please select different seats.");
        }
    }

    @Override
    public void releaseSeats(String eventId, List<String> seatIds) {
        // Nothing is held before booking; cancelled/failed bookings release their own seats.
    }
}
