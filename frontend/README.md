# Seminar Hall Event & Seat Booking System
> **BookMyShow-Inspired Premium Desktop Frontend for Campus Seminar & Auditorium Reservations**

An enterprise-grade Java desktop application engineered with **Java 17**, **Java Swing**, and **FlatLaf**, delivering a high-fidelity cinematic event discovery and interactive auditorium seat-booking experience for college symposiums, hackathons, guest lectures, and cultural galas.

---

## 🌟 Key Highlights & Architectural Features

- **Cinematic Dark Theme UI/UX**: Custom charcoal/near-black canvas (`#0F1117`), vibrant BookMyShow cinematic red (`#E62E4D`), soft card elevations, translucent status badges, and rounded button geometry.
- **Strict Layered Architecture (MVC / Interface Separation)**:
  - **Zero SQL / Zero JDBC** in any Swing components or UI classes.
  - All screens interact solely with domain service interfaces: `UserService`, `EventService`, `SeatService`, `BookingService`, `PaymentService`, and `TicketService`.
  - Easy drop-in integration with Member 2's PostgreSQL backend using `DatabaseServiceAdapter` or `ServiceFactory.registerCustomServices(...)`.
- **EDT Safety & Async Concurrency**: All long-running searches, authentications, payment simulations, and booking operations execute asynchronously off-EDT via `SwingWorker` with friendly error dialogues and indeterminate progress indicators.
- **Interactive Auditorium Seating Canvas**:
  - Configurable multi-row layout (Rows A through J) with central aisle navigation.
  - Realistic seat states: **Available**, **Selected** (emerald highlight), **Booked / Taken** (disabled grey), and **♿ Accessible Seats**.
  - Dynamic stage/projector banner, real-time ticket price calculation, and pre-booking seat availability re-validation against race conditions.
- **Complete Booking & Pass Lifecycle**:
  - Order summary breakdown with fee transparency.
  - Simulated payment gateway supporting **Instant UPI**, **Test Card**, and **Desk Cash**.
  - Digital E-Pass with simulated QR verification stub and text/pass export.
  - Booking history ledger with real-time seat cancellation and seat release.
- **Comprehensive Organizer Control Console**:
  - Live metric cards (Total Events, Total Bookings, Occupied Seats, Average Occupancy).
  - Visual occupancy percentage bars: $\text{Occupancy} = \frac{\text{Booked Seats}}{\text{Total Seats}} \times 100$.
  - Full CRUD for campus events with field validation.
  - Central registrations ledger.

---

## 🚀 Quick Start & Launch

### Prerequisites
- **JDK 17** or newer installed.
- **Apache Maven 3.9+** (installed and in PATH, or use the Maven binary located in `Local\Programs\apache-maven-3.9.6\bin`).

This frontend lives in the `frontend/` folder of the repository and runs on the backend in the repository root. Install the backend once (and again whenever it changes), from the repository root:
```bash
mvn install -DskipTests
```

Then run every command below from the `frontend/` folder:
```bash
cd frontend
```

### 1. Compile and Run Unit Tests
```bash
mvn clean test
```
*Runs 26 tests: the demo-mode services, plus the UI services running on the real backend and an in-memory database (login, registration, search, seat maps, paid and free bookings, declined payments, cancellations, organizer events, analytics).*

### 2. Run the Application Directly
```bash
mvn compile exec:java -Dexec.mainClass="com.seminarhall.Main"
```

### 3. Build & Run the Executable Fat JAR
```bash
mvn package -DskipTests
java -jar target/seminar-hall-booking-1.0.0.jar
```

### Where data is stored
The app uses the real backend services and saves everything to a database:
- **Default:** an embedded H2 database at `~/.bookmyseat/bookmyseat.mv.db`, created and filled with the demo accounts and events on first start. Delete that file to reset the demo data.
- **PostgreSQL:** add `-Dbookmyseat.db.url=jdbc:postgresql://localhost:5432/bookmyseat -Dbookmyseat.db.user=postgres -Dbookmyseat.db.password=<password>` to the `java` command (see the root `README.md`).
- **Demo mode (no database):** add `-Dseminarhall.mode=mock` to run on in-memory sample data. If the database cannot be opened, the app falls back to demo mode and says so.

Payments are simulated by the backend: totals ending in `.99` are declined, which is handy for testing the failure path. Free events are confirmed without a payment.

---

## 🔑 Quick Demo Credentials

The application includes built-in quick-fill buttons on the login screen for instant demonstration:

| Role | Email | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **Student** | `student@college.edu` | `student123` | Student discovery homepage, auditorium seat booking, e-pass view, booking history |
| **Organizer / Admin** | `organizer@college.edu` | `admin123` | Organizer control center, occupancy analytics, event creation & scheduling, bookings ledger |

*You can also click **"Register here"** on the login screen to register brand-new accounts with immediate validation and persistence.*

---

## 📂 Project Architecture

```
src/
├── main/
│   └── java/
│       └── com/seminarhall/
│           ├── Main.java                          # Main entry point, EDT launcher, theme bootstrap
│           ├── adapter/
│           │   └── DatabaseServiceAdapter.java    # Bridge adapter for Member 2's PostgreSQL backend
│           ├── model/
│           │   ├── Booking.java                   # Booking entity
│           │   ├── BookingStatus.java             # CONFIRMED, CANCELLED, PENDING
│           │   ├── Event.java                     # Event entity with dates, hall, pricing, capacities
│           │   ├── EventCategory.java             # TECHNICAL_SEMINAR, HACKATHON, WORKSHOP, etc.
│           │   ├── OrganizerAnalytics.java        # KPI and occupancy summary model
│           │   ├── Seat.java                      # Individual seat entity
│           │   ├── SeatStatus.java                # AVAILABLE, SELECTED, BOOKED, UNAVAILABLE
│           │   ├── Ticket.java                    # Digital pass with QR payload
│           │   ├── User.java                      # User entity
│           │   └── UserRole.java                  # STUDENT, ORGANIZER
│           ├── service/
│           │   ├── BookingService.java            # Interface for bookings & analytics
│           │   ├── EventService.java              # Interface for event queries & CRUD
│           │   ├── PaymentService.java            # Interface for payment processing
│           │   ├── SeatService.java               # Interface for seat layout & availability
│           │   ├── ServiceException.java          # Clean checked exception for service errors
│           │   ├── ServiceFactory.java            # Factory & registration hub for services
│           │   ├── SessionManager.java            # Current session & role state manager
│           │   ├── TicketService.java             # Interface for e-ticket generation & export
│           │   ├── UserService.java               # Interface for authentication & registration
│           │   └── mock/
│           │       ├── MockBookingService.java    # Thread-safe in-memory booking implementation
│           │       ├── MockDataStore.java         # Seeded campus events, seats, and mock store
│           │       ├── MockEventService.java      # Search, filter, and CRUD implementation
│           │       ├── MockPaymentService.java    # Realistic payment simulation
│           │       ├── MockSeatService.java       # Seat map and hold/release implementation
│           │       ├── MockTicketService.java     # E-Pass generation and file export
│           │       └── MockUserService.java       # User authentication and validation
│           └── ui/
│               ├── BookingConfirmationPanel.java  # E-ticket confirmation screen
│               ├── BookingHistoryPanel.java       # Student booking history & cancellation
│               ├── BookingSummaryPanel.java       # Order review & cost calculation screen
│               ├── EventDetailsPanel.java         # Comprehensive seminar details screen
│               ├── EventListPanel.java            # Search, filter, and browse catalog screen
│               ├── LoginPanel.java                # Branded authentication screen with quick-fill
│               ├── MainFrame.java                 # Main JFrame and CardLayout screen host
│               ├── NavigationController.java      # Central routing controller
│               ├── PaymentPanel.java              # Simulated checkout gateway screen
│               ├── RegisterPanel.java             # Account registration screen
│               ├── SeatSelectionPanel.java        # Interactive auditorium seat map screen
│               ├── StudentDashboardPanel.java     # Discovery homepage with hero banner
│               ├── ThemeManager.java              # Color palette, FlatLaf config, typography
│               ├── components/
│               │   ├── EmptyStatePanel.java       # Empty state component
│               │   ├── ErrorPanel.java            # Retryable error display component
│               │   ├── EventCard.java             # BookMyShow-style interactive card
│               │   ├── FeaturedEventPanel.java    # Spotlight hero banner
│               │   ├── LoadingPanel.java          # Indeterminate progress panel
│               │   ├── MetricCard.java            # KPI card for organizer console
│               │   ├── NavigationBar.java         # Top navigation bar
│               │   ├── SeatButton.java            # Custom painted auditorium seat
│               │   ├── SeatMapPanel.java          # Multi-row auditorium grid with stage
│               │   ├── SidebarNavigation.java     # Organizer console sidebar
│               │   ├── StatusBadge.java           # Translucent status pill
│               │   ├── StyledButton.java          # Modern rounded button with hover states
│               │   ├── StyledPasswordField.java   # Dark input with placeholder support
│               │   ├── StyledTextField.java       # Dark input with placeholder support
│               │   └── TicketPanel.java           # Perforated stub e-ticket component
│               └── organizer/
│                   └── OrganizerDashboardPanel.java # Organizer management console & occupancy
└── test/
    └── java/
        └── com/seminarhall/service/
            ├── BookingServiceTest.java            # Unit tests for booking & cancellation
            ├── EventServiceTest.java              # Unit tests for event search & filtering
            └── UserServiceTest.java               # Unit tests for authentication & registration
```

---

## 🔌 Member 2 (Backend & PostgreSQL) Integration Guide

Connecting Member 2's PostgreSQL JDBC services requires **no modifications to UI classes**:

1. Member 2 implements the service interfaces in package `com.seminarhall.service`:
   - `UserService`, `EventService`, `SeatService`, `BookingService`, `PaymentService`, `TicketService`
2. In the application initialization (or in a startup configuration class), call:
   ```java
   DatabaseServiceAdapter.activateDatabaseBackend(
       new PostgresUserService(dbConnection),
       new PostgresEventService(dbConnection),
       new PostgresSeatService(dbConnection),
       new PostgresBookingService(dbConnection),
       new PostgresPaymentService(),
       new PostgresTicketService(dbConnection)
   );
   ```
3. The UI seamlessly consumes the persisted PostgreSQL data through the existing service contracts.
