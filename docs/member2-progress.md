# Member 2 Progress — Backend / Business Logic Layer

## Status: COMPLETE

---

## Completed

- [x] Project inspection
- [x] Domain models
- [x] Custom exceptions
- [x] DAO interfaces
- [x] Authentication service
- [x] Event service
- [x] Booking / seat service
- [x] Payment service (simulated)
- [x] Ticket service
- [x] Unit tests — AuthService
- [x] Unit tests — EventService
- [x] Unit tests — BookingService
- [x] Unit tests — PaymentService
- [x] Unit tests — TicketService
- [x] End-to-end integration test

---

## Package Structure

```
src/main/java/com/bookmyshow/
├── model/
│   ├── enums/
│   │   ├── Role.java
│   │   ├── BookingStatus.java
│   │   ├── PaymentStatus.java
│   │   ├── EventStatus.java
│   │   └── SeatStatus.java
│   ├── User.java
│   ├── Venue.java
│   ├── Event.java
│   ├── Seat.java
│   ├── Booking.java
│   ├── BookingSeat.java
│   ├── Payment.java
│   └── Ticket.java
├── exception/
│   ├── ValidationException.java
│   ├── AuthenticationException.java
│   ├── UnauthorizedException.java
│   ├── EventNotFoundException.java
│   ├── SeatNotAvailableException.java
│   ├── BookingException.java
│   ├── PaymentException.java
│   └── TicketGenerationException.java
├── dao/
│   ├── UserDAO.java
│   ├── VenueDAO.java
│   ├── EventDAO.java
│   ├── SeatDAO.java
│   ├── BookingDAO.java
│   ├── PaymentDAO.java
│   └── TicketDAO.java
└── service/
    ├── AuthService.java
    ├── AuthServiceImpl.java
    ├── EventService.java
    ├── EventServiceImpl.java
    ├── BookingService.java
    ├── BookingServiceImpl.java
    ├── PaymentService.java
    ├── MockPaymentService.java
    ├── PaymentResult.java
    ├── TicketService.java
    └── TicketServiceImpl.java

src/test/java/com/bookmyshow/
├── service/
│   ├── AuthServiceImplTest.java       (17 tests)
│   ├── EventServiceImplTest.java      (18 tests)
│   ├── BookingServiceImplTest.java    (20 tests)
│   ├── MockPaymentServiceTest.java    (14 tests)
│   └── TicketServiceImplTest.java     (16 tests)
└── integration/
    └── BookingFlowIntegrationTest.java (3 scenarios)
```

---

## Key Design Decisions

### OOP Principles Applied

| Principle | Where |
|---|---|
| Encapsulation | All models: private fields, getters/setters |
| Abstraction | Service interfaces (AuthService, EventService, etc.) |
| Abstraction | DAO interfaces (UserDAO, EventDAO, etc.) |
| Polymorphism | PaymentService → MockPaymentService (can swap for real gateway) |
| Collections | Booking.bookingSeats (List, unmodifiable view) |

### Double-Booking Prevention (3 layers)

1. **Service layer**: checks `SeatStatus.AVAILABLE` before proceeding
2. **DAO layer**: `SeatDAO.reserveSeats()` executes within a DB transaction with row-level locking
3. **Database**: `UNIQUE(event_id, seat_id)` constraint on `booking_seats` table

If a race condition bypasses layers 1+2, the DB constraint still rejects the duplicate.

### Price Calculation

Total is **always calculated by the backend**:
```
totalAmount = event.ticketPrice × numberOfSeats
```
The UI never passes an amount — it receives the total from the booking object.

### Payment Simulation

`MockPaymentService` simulates outcomes deterministically for testing:
- Amounts ending in `.99` cents → FAILED (e.g. `399.99`)
- All other valid positive amounts → SUCCESS

This allows both success and failure paths to be tested without randomness.

### Password Hashing

Uses SHA-256 (standard JVM library, no external dependencies).
For production, replace `hashPassword()` in `AuthServiceImpl` with bcrypt/Argon2.

---

## Tests Summary

| Test Class | Tests | Coverage |
|---|---|---|
| AuthServiceImplTest | 17 | register, login, validation, credentials |
| EventServiceImplTest | 18 | create, update, delete, search, auth |
| BookingServiceImplTest | 20 | booking, double-booking, seat validation, confirm, cancel |
| MockPaymentServiceTest | 14 | success, failure, invalid amount, repeated payment |
| TicketServiceImplTest | 16 | generation, duplicates, retrieval, invalid states |
| BookingFlowIntegrationTest | 3 | full flow, payment failure, double-booking |
| **Total** | **88** | |

---

## DAO Contract for Member 1

Member 1 must implement all interfaces in `com.bookmyshow.dao`:

### Critical methods (required for booking to work)

```java
// SeatDAO
void reserveSeats(List<Integer> seatIds);   // Must use transaction + row lock
void releaseSeats(List<Integer> seatIds);

// BookingDAO
Booking create(Booking booking);
void createBookingSeats(List<BookingSeat> bookingSeats);

// UserDAO
boolean existsByEmail(String email);
Optional<User> findByEmail(String email);
```

### Database constraint required

```sql
ALTER TABLE booking_seats
  ADD CONSTRAINT uq_booking_seat UNIQUE (event_id, seat_id);
```

---

## UI Contract for Member 3

Member 3's Swing UI calls services like this:

```java
// Authentication
AuthService auth = new AuthServiceImpl(userDAO);
User user = auth.login("user@example.com", "password");

// Browse events
EventService events = new EventServiceImpl(eventDAO, venueDAO);
List<Event> available = events.getPublishedEvents();

// Book seats
BookingService booking = new BookingServiceImpl(bookingDAO, seatDAO, eventDAO);
Booking b = booking.createBooking(user, eventId, List.of(seatId1, seatId2));

// Pay
PaymentService payment = new MockPaymentService(paymentDAO, bookingDAO, booking);
PaymentResult result = payment.processPayment(b.getId(), b.getTotalAmount());

// Generate ticket
TicketService tickets = new TicketServiceImpl(ticketDAO, bookingDAO, eventDAO, userDAO);
Ticket ticket = tickets.generateTicket(b.getId());
```

All exceptions are `RuntimeException` subclasses — Member 3 can catch them by type:

```java
try {
    Booking b = bookingService.createBooking(user, eventId, seats);
} catch (SeatNotAvailableException e) {
    JOptionPane.showMessageDialog(frame, "Seat no longer available: " + e.getMessage());
} catch (BookingException e) {
    JOptionPane.showMessageDialog(frame, "Booking failed: " + e.getMessage());
}
```

---

## Blocked

Nothing currently blocked.
Pending Member 1's JDBC implementation of the DAO interfaces.

---

## Next Steps (after Member 1 delivers DAOs)

1. Replace mocks in integration tests with real DAO implementations
2. Run `mvn clean test` against a test PostgreSQL database
3. Verify transaction safety of `reserveSeats()` under concurrent load
