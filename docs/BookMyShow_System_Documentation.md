# BookMyShow Java Backend — Full System Documentation

> **Project:** BookMyShow Backend (Member 2 — Business Logic Layer)
> **Language:** Java 17
> **Build Tool:** Apache Maven 3.9.6
> **Testing:** JUnit 5.10.2 + Mockito 5.11.0
> **Generated:** October 2026

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Project Structure](#2-project-structure)
3. [Architecture and Design](#3-architecture-and-design)
4. [Layer 1 — Models (`model/`)](#4-layer-1--models-model)
   - [User.java](#41-userjava)
   - [Event.java](#42-eventjava)
   - [Venue.java](#43-venuejava)
   - [Seat.java](#44-seatjava)
   - [Booking.java](#45-bookingjava)
   - [BookingSeat.java](#46-bookingseatjava)
   - [Payment.java](#47-paymentjava)
   - [Ticket.java](#48-ticketjava)
5. [Layer 2 — Enums (`model/enums/`)](#5-layer-2--enums-modelenums)
   - [Role.java](#51-rolejava)
   - [EventStatus.java](#52-eventstatusjava)
   - [SeatStatus.java](#53-seatstatusjava)
   - [BookingStatus.java](#54-bookingstatusjava)
   - [PaymentStatus.java](#55-paymentstatusjava)
6. [Layer 3 — DAOs (`dao/`)](#6-layer-3--daos-dao)
   - [UserDAO.java](#61-userdaojava)
   - [EventDAO.java](#62-eventdaojava)
   - [VenueDAO.java](#63-venuedaojava)
   - [SeatDAO.java](#64-seatdaojava)
   - [BookingDAO.java](#65-bookingdaojava)
   - [PaymentDAO.java](#66-paymentdaojava)
   - [TicketDAO.java](#67-ticketdaojava)
7. [Layer 4 — Services (`service/`)](#7-layer-4--services-service)
   - [AuthService + AuthServiceImpl](#71-authservice--authserviceimpl)
   - [EventService + EventServiceImpl](#72-eventservice--eventserviceimpl)
   - [BookingService + BookingServiceImpl](#73-bookingservice--bookingserviceimpl)
   - [PaymentService + MockPaymentService](#74-paymentservice--mockpaymentservice)
   - [PaymentResult.java](#75-paymentresultjava)
   - [TicketService + TicketServiceImpl](#76-ticketservice--ticketserviceimpl)
8. [Layer 5 — Exceptions (`exception/`)](#8-layer-5--exceptions-exception)
9. [Tests (`src/test/`)](#9-tests-srctest)
10. [Core Java Concepts Used](#10-core-java-concepts-used)
11. [End-to-End Flow: How a Booking Works](#11-end-to-end-flow-how-a-booking-works)
12. [Key Design Decisions](#12-key-design-decisions)

---

## 1. Project Overview

This is the **backend business logic layer** of a BookMyShow-style ticket booking system. The project is structured as a Maven JAR with no web framework — pure Java. It is intentionally decoupled into three horizontal layers:

| Layer | What it does |
|---|---|
| **Model** | Plain Java objects that represent data (User, Event, Seat, etc.) |
| **DAO (Data Access Object)** | Interfaces that abstract database operations. Implemented by another team member using JDBC + PostgreSQL |
| **Service** | All business logic lives here — validation, authorization, booking flow, payment simulation |

The separation means this layer can be tested entirely with mocked DAOs (no real database needed), and a UI layer (Member 3) can plug in by calling the service interfaces.

---

## 2. Project Structure

```
bms_java/
├── pom.xml                          ← Maven build config
├── docs/                            ← Documentation files
└── src/
    ├── main/java/com/bookmyshow/
    │   ├── model/                   ← Domain entities
    │   │   ├── enums/               ← Status enums
    │   │   ├── User.java
    │   │   ├── Event.java
    │   │   ├── Venue.java
    │   │   ├── Seat.java
    │   │   ├── Booking.java
    │   │   ├── BookingSeat.java
    │   │   ├── Payment.java
    │   │   └── Ticket.java
    │   ├── dao/                     ← DAO interfaces (contracts for DB layer)
    │   │   ├── UserDAO.java
    │   │   ├── EventDAO.java
    │   │   ├── VenueDAO.java
    │   │   ├── SeatDAO.java
    │   │   ├── BookingDAO.java
    │   │   ├── PaymentDAO.java
    │   │   └── TicketDAO.java
    │   ├── service/                 ← Business logic (interfaces + implementations)
    │   │   ├── AuthService.java / AuthServiceImpl.java
    │   │   ├── EventService.java / EventServiceImpl.java
    │   │   ├── BookingService.java / BookingServiceImpl.java
    │   │   ├── PaymentService.java / MockPaymentService.java
    │   │   ├── TicketService.java / TicketServiceImpl.java
    │   │   └── PaymentResult.java
    │   └── exception/               ← Custom runtime exceptions
    └── test/java/com/bookmyshow/
        ├── integration/
        │   └── BookingFlowIntegrationTest.java
        └── service/
            ├── AuthServiceImplTest.java
            ├── BookingServiceImplTest.java
            ├── EventServiceImplTest.java
            ├── MockPaymentServiceTest.java
            └── TicketServiceImplTest.java
```

---

## 3. Architecture and Design

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         UI / Controller Layer           │  ← Member 3 (calls Service interfaces)
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│           Service Layer                 │  ← This project (business logic)
│  AuthServiceImpl, BookingServiceImpl,   │
│  EventServiceImpl, MockPaymentService,  │
│  TicketServiceImpl                      │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│              DAO Layer                  │  ← Member 1 (JDBC + PostgreSQL)
│  UserDAO, EventDAO, BookingDAO, etc.    │
│  (interfaces defined here, impls there) │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│            PostgreSQL Database          │
└─────────────────────────────────────────┘
```

### Key Design Patterns Used

| Pattern | Where applied |
|---|---|
| **Interface/Implementation split** | Every service and DAO has an interface + impl |
| **DAO Pattern** | DAOs abstract all SQL queries from business logic |
| **Factory Method** | `PaymentResult.success()` and `PaymentResult.failure()` |
| **Value Object** | `PaymentResult` carries outcome data, not just a boolean |
| **Encapsulation** | All model fields are private; accessed via getters/setters |
| **Defensive Copying** | `Booking.getBookingSeats()` returns `Collections.unmodifiableList` |
| **Constructor Chaining** | Models use `this()` to reduce duplication |

---

## 4. Layer 1 — Models (`model/`)

Models are plain Java classes (POJOs) with no logic other than simple business helper methods. They represent the core domain objects of the system.

---

### 4.1 `User.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A registered user of the system. Can be a customer buying tickets, an organizer creating events, or an admin with full control.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key from the database |
| `name` | `String` | Full display name |
| `email` | `String` | Unique login identifier |
| `passwordHash` | `String` | SHA-256 hash of the password (never stored in plain text) |
| `role` | `Role` (enum) | CUSTOMER, ORGANIZER, or ADMIN |
| `active` | `boolean` | Whether the account is enabled. Defaults to `true` |
| `createdAt` | `LocalDateTime` | Timestamp of account creation, auto-set to now |

**Key methods:**

```java
public boolean isOrganizer()  // returns true if role == ORGANIZER
public boolean isAdmin()      // returns true if role == ADMIN
public boolean isCustomer()   // returns true if role == CUSTOMER
```

These helper methods are used throughout the service layer for authorization checks instead of raw enum comparisons like `user.getRole() == Role.ORGANIZER`.

**Java concepts used:**
- **Encapsulation:** all fields are `private`
- **`java.time.LocalDateTime`** for timestamps (modern Java date/time API, not `Date`)
- **Overriding `equals()` and `hashCode()`** using `Objects.hash()` — equality is based on `id` and `email`, so the same user is recognized across different object instances
- **Constructor chaining** with `this()` — the 5-arg constructor calls the no-arg constructor to set defaults first

---

### 4.2 `Event.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A bookable event — a concert, movie screening, play, etc. — hosted at a Venue.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key |
| `name` | `String` | Display name of the event |
| `description` | `String` | Optional longer description |
| `venue` | `Venue` | Object reference to the hosting venue |
| `organizerId` | `int` | ID of the User who owns this event |
| `eventDateTime` | `LocalDateTime` | When the event starts |
| `ticketPrice` | `BigDecimal` | Price per seat |
| `totalSeats` | `int` | Immutable capacity set at creation |
| `availableSeats` | `int` | Decremented as seats are booked |
| `status` | `EventStatus` | DRAFT → PUBLISHED → COMPLETED / CANCELLED |
| `createdAt` | `LocalDateTime` | Auto-set timestamp |

**Key business method:**

```java
public boolean isBookable() {
    return EventStatus.PUBLISHED.equals(this.status)
        && this.availableSeats > 0
        && this.eventDateTime.isAfter(LocalDateTime.now());
}
```

This combines three conditions — status, availability, and time — into one readable method. The `BookingServiceImpl` calls `event.isBookable()` before creating a booking.

```java
public boolean isOwnedBy(int userId) {
    return this.organizerId == userId;
}
```

Used in `EventServiceImpl` for authorization: an organizer can only edit their own events.

**Java concepts used:**
- **`java.math.BigDecimal`** for ticket price — using `double` for money is a classic bug because of floating-point precision (e.g., `0.1 + 0.2 != 0.3`). BigDecimal avoids this entirely.
- **Object composition** — `Event` contains a `Venue` object, not just a `venueId`

---

### 4.3 `Venue.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A physical venue where events are held — an auditorium, stadium, cinema, etc.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key |
| `name` | `String` | Venue name (e.g., "Grand Hall") |
| `address` | `String` | Street address |
| `city` | `String` | City name |
| `totalCapacity` | `int` | Maximum seating capacity |

Venue is a relatively simple data-holding class. It is referenced by `Event` (composition). The `VenueDAO` is used by `EventServiceImpl` when creating a new event to validate that the venue exists.

**Java concepts used:**
- **Standard POJO pattern** with full getters/setters
- `equals()` / `hashCode()` based on `id` alone

---

### 4.4 `Seat.java`

**Package:** `com.bookmyshow.model`

**What it represents:** One physical seat in a venue for a specific event. A seat is event-specific — the same physical chair in a venue gets a separate `Seat` record for each event.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key |
| `venueId` | `int` | Which venue this seat belongs to |
| `eventId` | `int` | Which event this seat is for |
| `seatRow` | `String` | Row label, e.g., "A", "B", "C" |
| `seatNumber` | `int` | Number within the row, e.g., 1, 2, 10 |
| `seatLabel` | `String` | Derived label like "A10" |
| `status` | `SeatStatus` | AVAILABLE → RESERVED → BOOKED / BLOCKED |

**Key detail — `seatLabel` derivation:**
```java
private void updateLabel() {
    if (seatRow != null) {
        this.seatLabel = seatRow + seatNumber;  // e.g., "A" + 10 = "A10"
    }
}
```
This is called automatically when `setSeatRow()` or `setSeatNumber()` is used, keeping the label consistent without manual updates.

**Key business method:**
```java
public boolean isAvailable() {
    return SeatStatus.AVAILABLE.equals(this.status);
}
```

Used in `BookingServiceImpl` to check if a seat can be booked.

**Java concepts used:**
- **Encapsulation with side effects in setters** — `setSeatRow()` and `setSeatNumber()` both call `updateLabel()` to maintain derived state
- Default status set in no-arg constructor: `this.status = SeatStatus.AVAILABLE`

---

### 4.5 `Booking.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A booking made by a user for one or more seats at an event. A booking starts as PENDING (created but not paid), becomes CONFIRMED after payment, or CANCELLED/FAILED.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key |
| `userId` | `int` | The user who made this booking |
| `eventId` | `int` | The event being booked |
| `status` | `BookingStatus` | PENDING / CONFIRMED / CANCELLED / FAILED |
| `totalAmount` | `BigDecimal` | Total price (calculated by backend, not UI) |
| `bookingTime` | `LocalDateTime` | When the booking was created |
| `bookingSeats` | `List<BookingSeat>` | The seats included in this booking |

**Important design — defensive collection exposure:**
```java
public List<BookingSeat> getBookingSeats() {
    return Collections.unmodifiableList(bookingSeats);
}
```
The internal list is `private`. The getter returns an unmodifiable view — callers can read the seats but cannot add/remove elements directly. To add a seat, callers must use `addBookingSeat()` or `setBookingSeats()`. This protects internal state.

**Java concepts used:**
- **`java.util.Collections.unmodifiableList()`** for defensive immutability
- **`java.util.ArrayList`** as backing implementation for the seat list
- **`BigDecimal`** for monetary total
- Constructor defaults: `status = PENDING`, `bookingTime = now()`, `bookingSeats = new ArrayList<>()`

---

### 4.6 `BookingSeat.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A join/link entity connecting a Booking to a Seat. In relational databases, a many-to-many relationship between bookings and seats requires a join table — `BookingSeat` is the Java representation of a row in that table.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key of the join record |
| `bookingId` | `int` | FK to `bookings` table |
| `seatId` | `int` | FK to `seats` table |
| `seatLabel` | `String` | Denormalized for display (e.g., "A10") |

The `seatLabel` is stored here even though it exists on `Seat` — this is **intentional denormalization** for display performance. When printing a ticket, you don't want to join back to the seats table just to show "A1, A2".

`equals()` is based on the `(bookingId, seatId)` pair — the combination must be unique (matching the DB UNIQUE constraint).

**Java concepts used:**
- **Constructor overloading** — three constructors for different use cases (without id, with id, full)
- **Denormalized data** — a deliberate trade-off for read performance

---

### 4.7 `Payment.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A single payment transaction for a booking. Created when the user submits payment, updated based on gateway outcome.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key |
| `bookingId` | `int` | FK to the booking being paid for |
| `amount` | `BigDecimal` | Amount charged |
| `status` | `PaymentStatus` | PENDING / SUCCESS / FAILED / REFUNDED |
| `transactionReference` | `String` | External gateway reference (e.g., "TXN-ABC123") |
| `failureReason` | `String` | Populated when payment fails |
| `paymentTime` | `LocalDateTime` | When payment was attempted |

One booking has at most one Payment record (enforced by `MockPaymentService` which checks `paymentDAO.findByBookingId(bookingId).isPresent()` before creating a new one).

**Java concepts used:**
- **`BigDecimal`** for monetary amounts
- **Three constructors** covering different creation scenarios — no args (for DAO mapping), partial (for creation), full (for retrieval from DB)

---

### 4.8 `Ticket.java`

**Package:** `com.bookmyshow.model`

**What it represents:** A confirmed digital ticket generated after a successful payment. A ticket is a read-heavy denormalized document — it captures all the info a customer needs without requiring joins at read time.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `int` | Primary key |
| `ticketNumber` | `String` | 12-character unique reference (auto-generated from UUID) |
| `bookingId` | `int` | Which booking this covers |
| `userId` | `int` | The customer |
| `eventId` | `int` | The event |
| `customerName` | `String` | Denormalized customer name |
| `eventName` | `String` | Denormalized event name |
| `venueName` | `String` | Denormalized venue name |
| `eventDateTime` | `LocalDateTime` | Denormalized event start time |
| `seatLabels` | `List<String>` | All seat labels (e.g., ["A1", "A2"]) |
| `totalAmount` | `BigDecimal` | Total paid |
| `paymentStatus` | `PaymentStatus` | Always SUCCESS for a generated ticket |
| `generatedAt` | `LocalDateTime` | When the ticket was created |

**Ticket number generation:**
```java
this.ticketNumber = UUID.randomUUID().toString()
    .toUpperCase()
    .replace("-", "")
    .substring(0, 12);
// Example: "A3F9B2C1D4E7"
```
`UUID.randomUUID()` generates a 128-bit cryptographically random identifier. Taking 12 uppercase hex chars gives 16^12 = ~281 trillion possible values — effectively collision-free.

**Java concepts used:**
- **`java.util.UUID`** for unique ticket number generation
- **Defensive copying** in both constructor (`new ArrayList<>(seatLabels)`) and setter
- **`Collections.unmodifiableList()`** on the `getSeatLabels()` getter

---

## 5. Layer 2 — Enums (`model/enums/`)

Enums define the fixed set of possible states for domain objects. Using enums instead of String constants prevents typos and makes state comparisons type-safe.

---

### 5.1 `Role.java`

```java
public enum Role {
    CUSTOMER,    // Can browse events and book tickets
    ORGANIZER,   // Can create and manage events
    ADMIN        // Full access — can manage anything
}
```

Used in: `User` model, `AuthServiceImpl`, `EventServiceImpl` (authorization checks)

---

### 5.2 `EventStatus.java`

```java
public enum EventStatus {
    DRAFT,       // Created but not visible to the public
    PUBLISHED,   // Visible and bookable
    CANCELLED,   // No longer bookable, shown as cancelled
    COMPLETED    // Event has occurred
}
```

Lifecycle: `DRAFT → PUBLISHED` (on creation in this system, events are auto-published). `PUBLISHED → CANCELLED` when `EventServiceImpl.deleteEvent()` is called.

---

### 5.3 `SeatStatus.java`

```java
public enum SeatStatus {
    AVAILABLE,   // Can be selected for booking
    RESERVED,    // Temporarily held (e.g., in cart) — not used actively in current flow
    BOOKED,      // Committed — a confirmed booking owns this seat
    BLOCKED      // Admin-blocked (e.g., broken seat, VIP hold)
}
```

The double-booking prevention logic depends entirely on this status. `BookingServiceImpl.validateSeatsAvailability()` rejects any seat that is not `AVAILABLE`.

---

### 5.4 `BookingStatus.java`

```java
public enum BookingStatus {
    PENDING,     // Created, awaiting payment
    CONFIRMED,   // Payment successful
    CANCELLED,   // Cancelled by user or system
    FAILED       // Payment failed or seat reservation failed
}
```

State transitions:
- `createBooking()` → `PENDING`
- `confirmBooking()` → `CONFIRMED` (called inside `MockPaymentService` on success)
- `cancelBooking()` → `CANCELLED`
- Payment failure → `FAILED`

---

### 5.5 `PaymentStatus.java`

```java
public enum PaymentStatus {
    PENDING,     // Created but not processed
    SUCCESS,     // Payment was accepted
    FAILED,      // Payment was declined
    REFUNDED     // Amount returned to customer
}
```

---

## 6. Layer 3 — DAOs (`dao/`)

All DAOs are **interfaces only** in this layer. They define the contract for data access. The actual JDBC + PostgreSQL implementations are provided by Member 1. This separation means:

1. Services only depend on interfaces, not concrete implementations
2. Tests can mock DAOs without needing a real database
3. The DB implementation can change without touching service logic

The common pattern used is `Optional<T>` for lookups that may return nothing, rather than returning `null` (which forces null checks everywhere).

---

### 6.1 `UserDAO.java`

```java
User create(User user);                          // Insert and return with generated ID
Optional<User> findById(int id);                 // Lookup by PK
Optional<User> findByEmail(String email);        // For login
User update(User user);                          // Update existing record
boolean existsByEmail(String email);             // For duplicate-check during registration
```

---

### 6.2 `EventDAO.java`

```java
Event create(Event event);
Optional<Event> findById(int id);
List<Event> findAll();
List<Event> findByStatus(EventStatus status);    // e.g., fetch all PUBLISHED events
List<Event> findByOrganizer(int organizerId);
List<Event> searchByName(String keyword);        // Case-insensitive partial match
Event update(Event event);
void delete(int id);
```

---

### 6.3 `VenueDAO.java`

```java
Venue create(Venue venue);
Optional<Venue> findById(int id);
List<Venue> findAll();
Venue update(Venue venue);
```

Used by `EventServiceImpl` to validate that a venue exists when creating an event.

---

### 6.4 `SeatDAO.java`

The most complex DAO due to concurrency requirements.

```java
Seat create(Seat seat);
Optional<Seat> findById(int id);
List<Seat> findByEventId(int eventId);
List<Seat> findByEventIdAndStatus(int eventId, SeatStatus status);  // e.g., find AVAILABLE
List<Seat> findByIds(List<Integer> seatIds);      // Batch fetch for booking validation
void reserveSeats(List<Integer> seatIds);         // Marks seats as BOOKED; must be transactional
void releaseSeats(List<Integer> seatIds);         // Reverts to AVAILABLE on cancellation
Seat update(Seat seat);
```

**`reserveSeats()` is the most critical method in the system.** It must be implemented with a `SELECT FOR UPDATE` lock in SQL to prevent two concurrent requests from booking the same seat. The service layer does a pre-check, but without the DB-level lock, a race condition could still allow double booking.

---

### 6.5 `BookingDAO.java`

```java
Booking create(Booking booking);
Optional<Booking> findById(int id);
List<Booking> findByUserId(int userId);
List<Booking> findByEventId(int eventId);
Booking update(Booking booking);
void createBookingSeats(List<BookingSeat> bookingSeats);  // Batch insert join records
```

`createBookingSeats()` inserts multiple `booking_seats` rows in one operation — more efficient than inserting one at a time.

---

### 6.6 `PaymentDAO.java`

```java
Payment create(Payment payment);
Optional<Payment> findById(int id);
Optional<Payment> findByBookingId(int bookingId);  // One booking = one payment
Payment update(Payment payment);
```

---

### 6.7 `TicketDAO.java`

```java
Ticket create(Ticket ticket);
Optional<Ticket> findById(int id);
Optional<Ticket> findByBookingId(int bookingId);
Optional<Ticket> findByTicketNumber(String ticketNumber);  // For QR-code scan lookup
List<Ticket> findByUserId(int userId);                     // User's ticket history
```

---

## 7. Layer 4 — Services (`service/`)

Services contain all business logic. They take inputs, validate them, apply rules, coordinate DAOs, and return results. Each service follows the interface + implementation pattern so the UI layer and tests can swap implementations.

---

### 7.1 `AuthService` + `AuthServiceImpl`

**Purpose:** User registration and login.

**Interface contract:**
```java
User registerUser(String name, String email, String password, Role role);
User login(String email, String password);
User getUserById(int userId);
boolean validateCredentials(String rawPassword, String storedHash);
```

**How `registerUser()` works:**
1. Validates name (not blank, ≥ 2 chars)
2. Validates email format via regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
3. Validates password (not blank, ≥ 6 chars)
4. Checks role is not null
5. Checks email is not already registered (`userDAO.existsByEmail()`)
6. Hashes the password with SHA-256
7. Normalizes email to lowercase
8. Creates and persists the User

**How `login()` works:**
1. Validates email and password are not blank
2. Looks up user by email
3. Checks account is active
4. Hashes the supplied password and compares to stored hash
5. Returns the User object on success

**Password hashing:**
```java
public String hashPassword(String rawPassword) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
```
`MessageDigest` from `java.security` computes a one-way SHA-256 hash. The result is 32 bytes → 64 hex characters. **Note:** Production systems should use bcrypt or Argon2 with a random salt — SHA-256 without a salt is vulnerable to rainbow-table attacks.

**Java concepts used:**
- **`java.util.regex.Pattern`** — compiled once as `static final` for performance
- **`java.security.MessageDigest`** — JVM-guaranteed SHA-256
- **`java.nio.charset.StandardCharsets.UTF_8`** — explicit charset for consistent byte encoding
- **`Optional<User>`** handling — `.isEmpty()` check before accessing the value
- **`String.format("%02x", b)`** — formats each byte as 2-character hex (padding with `0` if needed)
- **Constructor injection** — `UserDAO` is passed in, not created inside the class (enables testing)

---

### 7.2 `EventService` + `EventServiceImpl`

**Purpose:** Full CRUD management of events with authorization enforcement.

**Interface contract:**
```java
Event createEvent(User organizer, String name, String description,
                  int venueId, LocalDateTime eventDateTime,
                  BigDecimal ticketPrice, int totalSeats);
Event updateEvent(User requestingUser, int eventId, String name, String description,
                  LocalDateTime eventDateTime, BigDecimal ticketPrice);
void deleteEvent(User requestingUser, int eventId);
Event getEventById(int eventId);
List<Event> getAllEvents();
List<Event> getPublishedEvents();
List<Event> searchEvents(String keyword);
```

**Authorization rules:**
- Creating an event: user must be `ORGANIZER` or `ADMIN`
- Updating/deleting: user must be the event owner (`organizerId == user.getId()`) OR `ADMIN`
- Getting/searching events: no restriction

**`createEvent()` flow:**
1. `requireOrganizerOrAdmin(organizer)` — throws `UnauthorizedException` if not qualified
2. Validates name, date (must be future), price (must be ≥ 0), seats (must be > 0)
3. Fetches venue from DB — throws `ValidationException` if not found
4. Builds `Event` object with status `PUBLISHED` (auto-published on creation)
5. Persists via `eventDAO.create()`

**`deleteEvent()` behavior:**
Events are not hard-deleted. The status is set to `CANCELLED`, then `eventDAO.delete()` is called. The DAO implementation may either soft-delete (mark deleted) or hard-delete — the service doesn't care.

**Java concepts used:**
- **Authorization separation** — auth logic is in the service, not in a filter or UI layer
- **Partial update pattern** in `updateEvent()` — only non-null parameters are applied, allowing callers to update specific fields

---

### 7.3 `BookingService` + `BookingServiceImpl`

**Purpose:** The core of the system — creating, confirming, and cancelling bookings.

**Interface contract:**
```java
Booking createBooking(User user, int eventId, List<Integer> seatIds);
Booking confirmBooking(int bookingId);
Booking cancelBooking(int bookingId);
Booking getBookingById(int bookingId);
List<Booking> getBookingsByUser(int userId);
```

**`createBooking()` — step-by-step flow:**

```
Step 1: Validate user (not null)
    ↓
Step 2: Validate seat IDs (not empty, no duplicates, no negative values)
    ↓
Step 3: Load event from DB — throw EventNotFoundException if missing
        Check event.isBookable() — throw BookingException if not
    ↓
Step 4: Load seats from DB by IDs
        Validate all requested seats exist and belong to the event
        Validate all seats are AVAILABLE
    ↓
Step 5: Calculate total = ticketPrice × numberOfSeats  (backend owns price!)
    ↓
Step 6: Create and persist Booking (status = PENDING)
    ↓
Step 7: Call seatDAO.reserveSeats(seatIds) — marks seats BOOKED
        If this throws SeatNotAvailableException:
            → set booking to FAILED and persist
            → rethrow
    ↓
Step 8: Build BookingSeat join records, persist them
        Add to booking's in-memory list
    ↓
Return: the saved Booking
```

**Double-booking prevention — three layers:**

| Layer | Mechanism |
|---|---|
| **Service (pre-check)** | `validateSeatsAvailability()` rejects non-AVAILABLE seats before proceeding |
| **DAO (DB lock)** | `reserveSeats()` must use `SELECT FOR UPDATE` in SQL to lock rows during the transaction |
| **Database (constraint)** | `UNIQUE(event_id, seat_id)` on `booking_seats` table is the final safety net |

**Total amount calculation:**
```java
BigDecimal totalAmount = event.getTicketPrice()
    .multiply(BigDecimal.valueOf(seatIds.size()));
```
The price is always calculated by the backend using the event's `ticketPrice`. It is never trusted from the UI — this prevents price tampering.

**`cancelBooking()` behavior:**
- If booking is CONFIRMED, seats are released back to AVAILABLE via `seatDAO.releaseSeats()`
- If booking is PENDING, seats may not have been reserved yet (edge case handled by checking if `bookingSeats` is non-empty)
- Already CANCELLED bookings throw `BookingException`

**Java concepts used:**
- **`java.util.HashSet`** for O(1) duplicate detection in `validateSeatIds()`
- **`java.util.stream.Collectors`** for filtering and mapping with Java Streams
- **`BigDecimal.multiply()`** for safe monetary calculation
- **Constructor injection** for all three DAOs
- **Exception handling mid-flow** — if `reserveSeats()` fails, the booking is marked FAILED and the exception is rethrown

---

### 7.4 `PaymentService` + `MockPaymentService`

**Purpose:** Processes payments for bookings. The real implementation would call a payment gateway (Razorpay, Stripe, etc.). Here, a mock simulates success and failure deterministically.

**Interface contract:**
```java
PaymentResult processPayment(int bookingId, BigDecimal amount);
Payment getPaymentByBookingId(int bookingId);
```

**`processPayment()` flow:**

```
Validate amount (not null, > 0)
    ↓
Load booking → validate it's in PENDING status
    ↓
Verify amount matches booking.getTotalAmount()
    ↓
Check no payment already exists for this booking
    ↓
Generate transaction reference: "TXN-" + 12 random hex chars
    ↓
Simulate outcome:
    if cents == 99 → FAILED
    otherwise     → SUCCESS
    ↓
Persist Payment record
    ↓
If SUCCESS:
    → bookingService.confirmBooking(bookingId)
    → return PaymentResult.success(payment)
If FAILED:
    → mark booking as FAILED
    → return PaymentResult.failure(payment, reason)
```

**Simulation rule:**
```java
private boolean simulatePaymentOutcome(BigDecimal amount) {
    BigDecimal cents = amount.remainder(BigDecimal.ONE)
        .multiply(BigDecimal.valueOf(100))
        .abs();
    return cents.compareTo(BigDecimal.valueOf(99)) != 0;
}
```
Any amount ending in `.99` (e.g., `399.99`) triggers a simulated decline. All other amounts succeed. This allows tests to deterministically test both the success and failure paths.

**Why `MockPaymentService` instead of a real one:**
The `PaymentService` interface is the contract. `MockPaymentService` is a pluggable implementation. When a real payment gateway is integrated, a new class (`RazorpayPaymentService`, etc.) is written that also implements `PaymentService`. The rest of the codebase changes nothing.

**Java concepts used:**
- **`BigDecimal.remainder()`** for extracting the cents portion without floating-point errors
- **`UUID.randomUUID()`** for transaction reference generation
- **Strategy pattern** — `PaymentService` interface allows swapping implementations
- **Dependency injection** — `BookingService` is injected (not created) to allow confirmBooking

---

### 7.5 `PaymentResult.java`

**Purpose:** A Value Object that wraps the outcome of a payment attempt. Instead of returning `null` for failure or a Payment object that callers must inspect, `PaymentResult` provides a clean API.

```java
public class PaymentResult {
    private final Payment payment;
    private final boolean success;
    private final String message;

    // Factory methods
    public static PaymentResult success(Payment payment) { ... }
    public static PaymentResult failure(Payment payment, String reason) { ... }

    // Accessors
    public boolean isSuccess() { ... }
    public String getMessage() { ... }
    public PaymentStatus getStatus() { ... }
}
```

**Java concepts used:**
- **Immutable class** — all fields are `final`, no setters
- **Factory method pattern** — `success()` and `failure()` are named constructors with clear intent
- **Value Object** — the object's identity is defined by its values, not by object reference

---

### 7.6 `TicketService` + `TicketServiceImpl`

**Purpose:** Generates and retrieves digital tickets for confirmed bookings.

**Interface contract:**
```java
Ticket generateTicket(int bookingId);
Ticket getTicketByBookingId(int bookingId);
Ticket getTicketByNumber(String ticketNumber);
List<Ticket> getTicketsByUser(int userId);
```

**`generateTicket()` flow:**

```
Step 1: Load booking — throw BookingException if not found
        Validate status == CONFIRMED — throw TicketGenerationException if not
    ↓
Step 2: Check no ticket already exists for this booking — throw if duplicate
    ↓
Step 3: Load Event from DB (for name, venue, datetime)
        Load User from DB (for customer name)
    ↓
Step 4: Collect seat labels from booking's BookingSeat list
    ↓
Step 5: Build Ticket object with all denormalized fields
        Persist via ticketDAO.create()
    ↓
Return: the saved Ticket
```

**Guard conditions:**
- Ticket generation is blocked for any booking status other than `CONFIRMED`. This means if payment failed, no ticket is ever generated.
- Duplicate ticket generation is blocked by checking `ticketDAO.findByBookingId()` before creating. Only one ticket per booking.

**Java concepts used:**
- **`java.util.stream.Collectors.toList()`** for extracting seat labels from `BookingSeat` list
- **Method chaining** with `Optional.orElseThrow()`
- **Constructor injection** for all four DAOs

---

## 8. Layer 5 — Exceptions (`exception/`)

All exceptions extend `RuntimeException` — they are unchecked. This means callers are not forced to catch them with try-catch, but they can if they want to handle specific error cases (e.g., the UI layer catching `AuthenticationException` to show a login error).

| Exception | When thrown | Extra data |
|---|---|---|
| `AuthenticationException` | Login fails (bad credentials or inactive account) | — |
| `BookingException` | Business logic violation in booking operations | — |
| `EventNotFoundException` | An event ID does not exist in the DB | `eventId` field accessible via `getEventId()` |
| `PaymentException` | Payment processing fails (booking invalid, already paid, etc.) | — |
| `SeatNotAvailableException` | One or more seats are not AVAILABLE | `List<Integer> unavailableSeatIds` |
| `TicketGenerationException` | Cannot generate ticket (not confirmed, already generated) | — |
| `UnauthorizedException` | User attempts an action beyond their role | — |
| `ValidationException` | Input fails validation (blank name, bad email, etc.) | `field` — name of the failing field |

**Notable exception designs:**

`SeatNotAvailableException` accepts multiple seat IDs:
```java
public SeatNotAvailableException(List<Integer> unavailableSeatIds) {
    super("Seats not available: " + unavailableSeatIds);
    this.unavailableSeatIds = List.copyOf(unavailableSeatIds);  // Defensive copy
}
```
The calling code can retrieve `getUnavailableSeatIds()` to show the user which specific seats are taken.

`ValidationException` carries a `field` name:
```java
throw new ValidationException("email", "Email format is invalid: " + email);
```
The UI layer can use `ex.getField()` to highlight the specific input field that failed.

`EventNotFoundException` carries `eventId` for logging:
```java
public EventNotFoundException(int eventId) {
    super("Event not found with id: " + eventId);
    this.eventId = eventId;  // Accessible separately for structured logging
}
```

**Java concepts used:**
- **`extends RuntimeException`** — unchecked exceptions, no forced handling
- **Constructor chaining** with `super(message)` and `super(message, cause)`
- **`List.copyOf()`** — immutable copy in `SeatNotAvailableException`
- **`List.of()`** — for single-element immutable lists

---

## 9. Tests (`src/test/`)

The project uses **JUnit 5** (Jupiter) for test structure and **Mockito** for mocking DAOs. All tests operate without a real database.

### Test Files

| File | What it tests |
|---|---|
| `AuthServiceImplTest.java` | Registration, login, password hashing, email normalization |
| `BookingServiceImplTest.java` | Booking creation, confirmation, cancellation, double-booking |
| `EventServiceImplTest.java` | Event CRUD, authorization checks, validation |
| `MockPaymentServiceTest.java` | Payment success, decline (`.99` amounts), duplicate payment rejection |
| `TicketServiceImplTest.java` | Ticket generation, guard conditions, retrieval |
| `BookingFlowIntegrationTest.java` | Full end-to-end flow from register to ticket retrieval |

### Testing Patterns

**1. Mockito annotations:**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock private UserDAO userDAO;       // Mockito creates a mock implementation
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDAO);  // Inject mock
    }
}
```

**2. Stubbing behavior:**
```java
when(userDAO.existsByEmail("alice@example.com")).thenReturn(false);
when(userDAO.create(any(User.class))).thenReturn(savedUser);
```

**3. Asserting exceptions:**
```java
assertThrows(ValidationException.class, () ->
    authService.registerUser("", "alice@example.com", "secret123", Role.CUSTOMER));
```

**4. Verify interactions:**
```java
verify(userDAO, never()).create(any());   // Ensure create was NOT called
verify(seatDAO).reserveSeats(any());     // Ensure reserveSeats WAS called
```

**5. Integration test structure (BookingFlowIntegrationTest):**

The full booking flow is tested in one class with 7 mocked DAOs:
- `Register → Login → Browse Events → View Event → Create Booking → Process Payment → Generate Ticket → Retrieve Ticket`

Each step's mock is set up before the step executes, and assertions verify the output at each stage. This tests the entire service layer pipeline in sequence.

**Failure path tests:**
- Payment of `399.99` → PaymentResult returns `isSuccess() == false`
- Attempting `generateTicket()` on a FAILED booking → `TicketGenerationException`
- Second user tries to book a BOOKED seat → `SeatNotAvailableException`

---

## 10. Core Java Concepts Used

This project demonstrates a wide range of core Java concepts. Here is a consolidated reference:

### OOP Principles

| Concept | Where |
|---|---|
| **Encapsulation** | All model fields are `private`, accessed via getters/setters |
| **Abstraction** | Service and DAO interfaces define contracts without revealing implementation |
| **Inheritance** | All custom exceptions extend `RuntimeException` |
| **Polymorphism** | `PaymentService` can be `MockPaymentService` or a future real implementation |

### Java Standard Library

| API | Used for |
|---|---|
| `java.time.LocalDateTime` | Timestamps for events, bookings, payments, tickets |
| `java.math.BigDecimal` | Exact monetary arithmetic (price, total, amount) |
| `java.util.Optional<T>` | Safe DAO return types that may be absent |
| `java.util.Collections.unmodifiableList()` | Defensive read-only view of internal lists |
| `java.util.UUID` | Generating unique ticket numbers and transaction references |
| `java.util.regex.Pattern` | Email format validation in `AuthServiceImpl` |
| `java.security.MessageDigest` | SHA-256 password hashing |
| `java.nio.charset.StandardCharsets` | Explicit UTF-8 encoding for hashing |
| `java.util.HashSet` | O(1) duplicate detection for seat IDs |
| `java.util.stream.Stream` + `Collectors` | Filtering and transforming lists |

### Design Patterns

| Pattern | Implementation |
|---|---|
| **Interface/Implementation** | Every service (`BookingService` / `BookingServiceImpl`) |
| **DAO Pattern** | All 7 DAO interfaces |
| **Factory Method** | `PaymentResult.success()`, `PaymentResult.failure()` |
| **Value Object** | `PaymentResult` — immutable, identified by value |
| **Strategy** | `PaymentService` allows swapping `MockPaymentService` for a real one |
| **Constructor Injection (DI)** | All service implementations receive their DAOs via constructor |

### Java Collections

| Collection | Used in |
|---|---|
| `ArrayList` | `Booking.bookingSeats`, `Ticket.seatLabels` |
| `HashSet` | Duplicate seat ID detection in `BookingServiceImpl` |
| `Collections.unmodifiableList()` | Exposing `bookingSeats` and `seatLabels` safely |
| `List.copyOf()` | Immutable copy in `SeatNotAvailableException` |
| `List.of()` | Immutable single-element lists |

---

## 11. End-to-End Flow: How a Booking Works

```
1. USER REGISTRATION
   AuthService.registerUser("Alice", "alice@email.com", "pass123", CUSTOMER)
   → validate input
   → hash password (SHA-256)
   → userDAO.create(user)
   → return User{id=1, role=CUSTOMER}

2. LOGIN
   AuthService.login("alice@email.com", "pass123")
   → userDAO.findByEmail(...)
   → check isActive()
   → hash supplied password → compare with stored hash
   → return User{id=1}

3. BROWSE EVENTS
   EventService.getPublishedEvents()
   → eventDAO.findByStatus(PUBLISHED)
   → return List<Event>

4. CREATE BOOKING
   BookingService.createBooking(user, eventId=5, seatIds=[101, 102])
   → validate user, seatIds
   → load event, check isBookable()
   → load seats, check all AVAILABLE
   → totalAmount = 200.00 × 2 = 400.00
   → bookingDAO.create(booking) → Booking{id=1, status=PENDING}
   → seatDAO.reserveSeats([101, 102]) → marks seats BOOKED
   → bookingDAO.createBookingSeats([...])
   → return Booking{id=1, status=PENDING, total=400.00}

5. PROCESS PAYMENT
   PaymentService.processPayment(bookingId=1, amount=400.00)
   → validate amount
   → load booking, verify PENDING
   → verify amount matches booking total
   → check no existing payment
   → generate txRef = "TXN-A3F9B2C1D4E7"
   → simulateOutcome(400.00) → 400.00 does NOT end in .99 → SUCCESS
   → paymentDAO.create(payment{status=SUCCESS})
   → bookingService.confirmBooking(1) → booking.status = CONFIRMED
   → return PaymentResult{success=true}

6. GENERATE TICKET
   TicketService.generateTicket(bookingId=1)
   → load booking, verify CONFIRMED
   → check no duplicate ticket
   → load event, user
   → collect seatLabels = ["A1", "A2"]
   → build Ticket{ticketNumber="A3F9...", customerName="Alice", ...}
   → ticketDAO.create(ticket)
   → return Ticket

7. RETRIEVE TICKET
   TicketService.getTicketByBookingId(1)
   → ticketDAO.findByBookingId(1)
   → return Ticket
```

---

## 12. Key Design Decisions

**1. Backend owns the price**
Total booking amount is always calculated by `BookingServiceImpl` using `event.getTicketPrice()`. The UI never passes a price. This prevents price manipulation where a client sends a modified request with a lower amount.

**2. Interfaces everywhere**
Every service and DAO is an interface. This enables:
- Independent development (Member 2 doesn't wait for Member 1's DB code)
- Mocking in tests (no real DB needed)
- Swappable implementations (e.g., swap `MockPaymentService` for a real one)

**3. Three-layer double-booking prevention**
Single pre-check at the service layer is not enough under concurrent load. The system relies on all three layers: service pre-check, DAO `SELECT FOR UPDATE`, and DB UNIQUE constraint.

**4. `Optional<T>` instead of null**
DAO lookups return `Optional<T>` so callers are forced to handle the "not found" case. Services use `.orElseThrow(...)` to convert to domain exceptions with meaningful messages.

**5. `BigDecimal` for all money**
`double` and `float` cannot represent decimal fractions exactly. `BigDecimal` is mandatory for any monetary calculation to avoid subtle rounding bugs in prices, totals, and comparisons.

**6. Denormalized Ticket**
The `Ticket` object stores customer name, event name, venue name, and seat labels directly. This is intentional — a ticket should be readable years later even if the underlying data changes. It is a historical record, not a live view.

**7. Unchecked exceptions**
Custom exceptions extend `RuntimeException`, not `Exception`. This avoids polluting every method signature with `throws` clauses and lets exceptions propagate naturally to the layer that knows how to handle them (typically the UI/controller layer).

**8. SHA-256 password hashing (with noted production limitation)**
SHA-256 is used for simplicity and zero external dependencies. The code includes a clear comment that production systems must use bcrypt or Argon2 with a per-user salt to resist brute-force and rainbow-table attacks.

---

*Documentation covers all 45 Java source files across model, enum, DAO, service, exception, and test packages.*
