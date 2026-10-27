# BookMyShow — Java Backend

A ticket booking system backend built in pure Java, modelled after BookMyShow. This project implements the full business logic layer — user authentication, event management, seat booking, payment processing, and ticket generation — with a clean layered architecture and zero web framework dependency.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Features](#features)
- [How the Booking Flow Works](#how-the-booking-flow-works)
- [Running the Project](#running-the-project)
- [Running Tests](#running-tests)
- [Key Design Decisions](#key-design-decisions)
- [Documentation](#documentation)

---

## Overview

This is **Member 2's contribution** to a three-member BookMyShow clone project:

| Member | Responsibility |
|---|---|
| Member 1 | JDBC + PostgreSQL DAO implementations |
| **Member 2 (this repo)** | **Business logic — services, models, exceptions** |
| Member 3 | UI / controller layer |

The service layer is fully testable in isolation — all DAOs are injected as interfaces and mocked in tests. No real database is needed to run the test suite.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Apache Maven | 3.9.6 | Build and dependency management |
| JUnit Jupiter | 5.10.2 | Unit and integration testing |
| Mockito | 5.11.0 | Mocking DAOs in tests |
| PostgreSQL JDBC | 42.7.3 | DB driver (used by Member 1's DAO impl) |

---

## Project Structure

```
bms_java/
├── pom.xml
├── README.md
├── docs/
│   ├── BookMyShow_System_Documentation.md   ← Full code documentation
│   └── ...
├── frontend/                ← Swing desktop UI (Member 3), depends on this module
└── src/
    ├── main/java/com/bookmyshow/
    │   ├── model/           ← Domain entities (User, Event, Seat, Booking, etc.)
    │   │   └── enums/       ← Role, EventStatus, SeatStatus, BookingStatus, PaymentStatus
    │   ├── dao/             ← DAO interfaces (contracts for the DB layer)
    │   │   └── jdbc/        ← JDBC implementations (H2 by default, PostgreSQL supported)
    │   ├── service/         ← Business logic — interfaces + implementations
    │   └── exception/       ← Custom runtime exceptions
    ├── main/resources/
    │   └── schema.sql       ← Tables, created automatically on start
    └── test/java/com/bookmyshow/
        ├── dao/jdbc/        ← Services + JDBC DAOs against a real (in-memory) database
        ├── integration/     ← End-to-end booking flow test
        └── service/         ← Unit tests for each service
```

---

## Architecture

```
┌──────────────────────────────┐
│     UI / Controller Layer    │  ← Member 3 (frontend/, via an adapter)
└──────────────┬───────────────┘
               │ calls
┌──────────────▼───────────────┐
│       Service Layer          │  ← This project
│  AuthServiceImpl             │
│  EventServiceImpl            │
│  BookingServiceImpl          │
│  MockPaymentService          │
│  TicketServiceImpl           │
└──────────────┬───────────────┘
               │ calls
┌──────────────▼───────────────┐
│        DAO Interfaces        │  ← Member 1's layer
│  UserDAO, EventDAO,          │
│  BookingDAO, SeatDAO, etc.   │
└──────────────┬───────────────┘
               │ implemented by
┌──────────────▼───────────────┐
│   dao/jdbc (JdbcUserDAO …)   │  ← H2 file by default, PostgreSQL via settings
└──────────────────────────────┘
```

All services depend only on **interfaces**, not concrete implementations. This makes the business logic layer independently testable and swappable.

---

## Features

- **User Authentication** — registration with email/password, SHA-256 hashing, login with credential validation, role-based access (CUSTOMER / ORGANIZER / ADMIN)
- **Event Management** — create, update, delete (soft), search, and list events; organizers manage their own events; admins manage all
- **Seat Booking** — select multiple seats, validate availability, calculate total server-side, reserve seats atomically
- **Double-booking Prevention** — three-layer protection: service pre-check + DAO-level `SELECT FOR UPDATE` + DB UNIQUE constraint
- **Payment Processing** — mock payment service with deterministic success/failure (amounts ending in `.99` are declined); designed to be swapped for a real gateway
- **Ticket Generation** — digital tickets generated only after confirmed payment; unique 12-character ticket number; one ticket per booking

---

## How the Booking Flow Works

```
1. Register / Login
      AuthService.registerUser(...)  →  AuthService.login(...)

2. Browse Events
      EventService.getPublishedEvents()

3. Create Booking  (status = PENDING)
      BookingService.createBooking(user, eventId, seatIds)
      → validates event is bookable
      → validates all seats are AVAILABLE
      → calculates total = ticketPrice × seats  (backend never trusts UI price)
      → reserves seats in DB

4. Process Payment
      PaymentService.processPayment(bookingId, amount)
      → verifies amount matches booking total
      → simulates gateway call
      → on success: booking → CONFIRMED
      → on failure: booking → FAILED and its seats are released

5. Generate Ticket
      TicketService.generateTicket(bookingId)
      → only works for CONFIRMED bookings
      → generates unique ticket number
      → stores denormalized ticket snapshot
```

---

## Running the Project

**Prerequisites:** Java 17+, Maven 3.x (or use the bundled `mvnw`)

```bash
# Clone the repo
git clone https://github.com/vaishaksanker/Bookmyseat_java.git
cd Bookmyseat_java

# Build
mvn clean compile

# Package as JAR and install it for the frontend
mvn clean install -DskipTests
```

To run the whole product (Swing UI + this backend + database), install this module as above, then follow `frontend/README.md`.

### Database

The JDBC DAOs in `com.bookmyshow.dao.jdbc` need no setup: by default they use an embedded **H2** database file at `~/.bookmyseat/bookmyseat.mv.db`, created with `schema.sql` on first start. To use **PostgreSQL** instead, create an empty database and pass:

```bash
-Dbookmyseat.db.url=jdbc:postgresql://localhost:5432/bookmyseat -Dbookmyseat.db.user=postgres -Dbookmyseat.db.password=<password>
```

(or set `BOOKMYSEAT_DB_URL`, `BOOKMYSEAT_DB_USER`, `BOOKMYSEAT_DB_PASSWORD`). The tables are created automatically.

---

## Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=BookingServiceImplTest

# Run the full integration test
mvn test -Dtest=BookingFlowIntegrationTest
```

### Test Coverage

| Test Class | Covers |
|---|---|
| `AuthServiceImplTest` | Registration, login, password hashing, email normalization, edge cases |
| `BookingServiceImplTest` | Booking creation, confirmation, cancellation, double-booking rejection |
| `EventServiceImplTest` | Event CRUD, authorization (organizer vs admin vs customer) |
| `MockPaymentServiceTest` | Payment success, simulated decline, duplicate payment rejection |
| `TicketServiceImplTest` | Ticket generation guards, duplicate prevention, retrieval |
| `BookingFlowIntegrationTest` | Full Register → Login → Book → Pay → Ticket flow end-to-end |
| `JdbcBookingFlowTest` | Real services on the JDBC DAOs and an in-memory H2 database: seat maps, booking, payment, declined payments, cancel + re-book, double-booking, persistence |

---

## Key Design Decisions

**BigDecimal for all money** — `double`/`float` cannot represent decimal fractions exactly. Every price, total, and amount uses `BigDecimal` to avoid rounding bugs.

**Backend owns the price** — `BookingServiceImpl` always calculates `totalAmount = event.ticketPrice × seatCount`. The UI never passes a price. This prevents client-side price manipulation.

**Interfaces everywhere** — Services and DAOs are interfaces. Implementations are injected via constructor. This enables mocking in tests and swapping implementations (e.g., replacing `MockPaymentService` with a real gateway adapter).

**Three-layer double-booking protection** — Service pre-check (status == AVAILABLE) + DAO `SELECT FOR UPDATE` lock + a conditional `UPDATE … WHERE status = 'AVAILABLE'` that reserves all seats or none. (A UNIQUE constraint on `booking_seats(event_id, seat_id)` was not used: it would stop a cancelled seat from ever being booked again.)

**Unchecked exceptions** — All custom exceptions extend `RuntimeException`. No forced `throws` declarations; exceptions propagate to the layer that knows how to handle them (typically the UI/controller).

**Denormalized Ticket** — Tickets store customer name, event name, venue name, and seat labels directly. They are historical records — readable even if the underlying data later changes.

---

## Documentation

Full code documentation covering every Java file, concept, and design decision is in:

```
docs/BookMyShow_System_Documentation.md
```

Topics covered: architecture diagrams, per-class field tables, method flow breakdowns, Java concepts used, end-to-end flow walkthrough, and all design decisions with rationale.
