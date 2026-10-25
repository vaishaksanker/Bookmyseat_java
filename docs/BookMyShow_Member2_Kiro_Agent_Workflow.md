# Member 2 — Backend / Business Logic
## Kiro CLI Agent Workflow — BookMyShow-Style Java Project

### Role
You are **Member 2**. Your responsibility is the Java backend/business-logic layer.

You own:
- Java domain/model classes
- Service layer
- Authentication logic
- Event management logic
- Seat validation and booking logic
- Payment simulation
- Ticket generation
- Business validation
- Application-level exceptions
- OOP implementation
- Integration between Member 1's DAO layer and Member 3's Swing UI
- Backend tests

You do **not** own the main PostgreSQL/DAO implementation or Swing UI implementation.

---

# 1. Architecture

Use:

```text
Swing UI (Member 3)
        ↓
Service / Business Logic (Member 2)
        ↓
DAO / JDBC (Member 1)
        ↓
PostgreSQL
```

Never put SQL directly inside services and never put business rules directly inside Swing event handlers.

---

# 2. MASTER DEVELOPMENT LOOP

For **every task**, follow this loop:

```text
GOAL
  ↓
INSPECT
  ↓
UNDERSTAND
  ↓
PLAN
  ↓
IMPLEMENT
  ↓
COMPILE / TEST
  ↓
CHECK RESULT
  ↓
PROBLEM?
 ├── YES → FIND ROOT CAUSE → FIX → TEST AGAIN
 └── NO
       ↓
VERIFY INTEGRATION
       ↓
UPDATE PROGRESS
       ↓
NEXT GOAL
```

Do not skip the inspection step.

Do not move to the next major feature while the current feature is broken.

---

# 3. GOAL → INSPECT → PLAN → IMPLEMENT RULE

For each goal, Kiro must first produce a short internal/work summary:

```text
GOAL:
What exactly are we implementing?

INSPECT:
What files/classes already exist?
What has other team members implemented?

PLAN:
What files need to be created/modified?
What dependencies are required?
What tests are required?

IMPLEMENT:
Make the smallest clean implementation.

CHECK:
Compile and run relevant tests.

PROBLEM:
If something fails, identify the root cause.

RESOLVE:
Fix the responsible layer.

VERIFY:
Run the feature again and run regression tests.

DONE:
Only mark the goal complete after verification.
```

---

# 4. PHASE 0 — PROJECT INSPECTION

## Goal

Understand the existing repository before writing backend code.

## Inspect

Check:

```text
pom.xml
src/main/java/
src/test/java/
model/
dao/
service/
exception/
ui/
util/
```

Inspect:

- Existing Java classes
- Existing package names
- Member 1's DAO classes/interfaces
- Member 3's UI classes
- Existing tests
- Maven configuration
- Current Git branch

Run:

```bash
mvn clean test
```

## If Build Fails

Use:

```text
Read first meaningful error
        ↓
Identify responsible class/layer
        ↓
Fix
        ↓
mvn test
        ↓
Repeat
```

Do not start new features on top of an unexplained broken build.

---

# 5. IMPLEMENTATION ORDER

Use this order:

```text
1. Project inspection
2. Domain models
3. Custom exceptions
4. Authentication service
5. Event service
6. Booking/seat service
7. Payment service
8. Ticket service
9. DAO integration
10. UI integration
11. Unit tests
12. End-to-end tests
13. Bug fixing
14. Final integration
```

---

# 6. PHASE 1 — DOMAIN MODELS

## Goal

Create clean Java representations of the business entities.

Expected models:

```text
User
Event
Venue
Seat
Booking
BookingSeat
Payment
Ticket
```

Possible enums:

```text
Role
BookingStatus
PaymentStatus
EventStatus
SeatStatus
```

Use enums instead of arbitrary strings where appropriate.

## OOP Requirements

Demonstrate:

- Encapsulation
- Abstraction
- Inheritance where meaningful
- Polymorphism where meaningful
- Exception handling
- Collections

Use private fields and controlled access.

Use `equals()` and `hashCode()` where value comparison is needed.

Use `BigDecimal` for money.

Do not use `double` for financial calculations.

---

# 7. PHASE 2 — CUSTOM EXCEPTIONS

Create meaningful exceptions such as:

```text
ValidationException
AuthenticationException
UnauthorizedException
EventNotFoundException
SeatNotAvailableException
BookingException
PaymentException
TicketGenerationException
```

Never silently ignore exceptions.

Avoid:

```java
catch (Exception e) {
}
```

Errors must either be handled or propagated meaningfully.

---

# 8. PHASE 3 — AUTHENTICATION SERVICE

## Goal

Implement registration and login.

Create/use:

```text
AuthService
```

Possible methods:

```text
registerUser()
login()
getUser()
validateCredentials()
```

## Registration

Validate:

```text
Name is not empty
Email is valid
Password is valid
Role is valid
Email is not already registered
```

Flow:

```text
UI
 ↓
AuthService
 ↓
Validation
 ↓
UserDAO
 ↓
Database
```

## Login

Check:

```text
User exists?
Password correct?
Account valid?
```

Return a domain-level `User` object.

Do not expose JDBC details to the UI.

---

# 9. PHASE 4 — EVENT SERVICE

## Goal

Implement event business logic.

Possible methods:

```text
createEvent()
updateEvent()
deleteEvent()
getEventById()
getAllEvents()
searchEvents()
```

Validate:

```text
Event name not empty
Price >= 0
Valid date/time
Valid venue
Organizer authorized
```

Organizer-only operations must be checked in the service layer.

Do not rely only on hiding buttons in Swing.

---

# 10. PHASE 5 — SEAT + BOOKING SERVICE

## MOST IMPORTANT BACKEND FEATURE

The booking system must prevent double booking.

Example:

```text
User A selects A10
User B selects A10
```

Only one booking may succeed.

Use:

```text
BookingService
```

Possible method:

```java
createBooking(
    User user,
    int eventId,
    List<Integer> seatIds
)
```

Flow:

```text
Receive request
      ↓
Validate user
      ↓
Validate event
      ↓
Validate seat list
      ↓
Check seat availability
      ↓
Calculate total
      ↓
Create booking
      ↓
Reserve seats
      ↓
Commit
      ↓
Return booking
```

---

# 11. DOUBLE-BOOKING / CONCURRENCY

Do not depend only on Java-side availability checks.

Database transaction safety is required.

Concept:

```text
BEGIN TRANSACTION
       ↓
Check / lock seats
       ↓
Create booking
       ↓
Insert booking seats
       ↓
COMMIT
```

The database should also enforce an appropriate unique constraint such as:

```text
UNIQUE(event_id, seat_id)
```

If two users race for the same seat:

```text
User A → SUCCESS
User B → SeatNotAvailableException
```

Never:

```text
User A → SUCCESS
User B → SUCCESS
```

for the same event/seat.

---

# 12. BOOKING VALIDATION

Before booking verify:

```text
User exists
Event exists
Event is bookable
Seat list is not empty
Seat IDs are valid
No duplicate seat IDs
Seats belong to the event/venue
Seats are available
```

The final amount must be calculated by the backend.

Example:

```text
Ticket price × number of seats
+ applicable fee
= total
```

Never trust an amount sent by the UI.

---

# 13. PHASE 6 — PAYMENT SERVICE

Payment is simulated; no real payment gateway is required.

Create:

```text
PaymentService
MockPaymentService
```

Possible states:

```text
PENDING
SUCCESS
FAILED
```

Flow:

```text
Booking
 ↓
Calculate total
 ↓
Payment
 ↓
Success?
 ├── YES → Confirm booking → Generate ticket
 └── NO  → Mark payment failed / booking failed
```

Never generate a confirmed ticket for failed payment.

Test:

```text
Successful payment
Failed payment
Invalid amount
Invalid booking
Repeated payment attempt
```

---

# 14. PHASE 7 — TICKET SERVICE

Generate a digital ticket only after successful payment.

Ticket should contain:

```text
Ticket ID
Booking ID
Customer
Event
Venue
Date
Time
Selected seats
Total amount
Payment status
```

Possible service:

```text
TicketService
```

Possible method:

```text
generateTicket()
```

Use database-generated IDs or UUIDs where appropriate.

---

# 15. DAO INTEGRATION

Member 2 consumes Member 1's DAO layer.

Expected dependencies:

```text
AuthService
 → UserDAO

EventService
 → EventDAO

BookingService
 → BookingDAO
 → SeatDAO

PaymentService
 → PaymentDAO

TicketService
 → TicketDAO
```

Never duplicate SQL inside services.

Prefer dependency injection:

```java
public BookingService(
    BookingDAO bookingDAO,
    SeatDAO seatDAO
) {
    this.bookingDAO = bookingDAO;
    this.seatDAO = seatDAO;
}
```

---

# 16. WHEN A DAO METHOD IS MISSING

Do not write SQL inside the service as a workaround.

Instead:

```text
Identify missing DAO contract
        ↓
Document required method
        ↓
Coordinate with Member 1
        ↓
Update service
        ↓
Run tests
```

Example:

```text
BookingService requires:

BookingDAO.createBooking(...)
BookingDAO.findBookingById(...)
BookingDAO.reserveSeats(...)
```

---

# 17. UI INTEGRATION

Member 3 should call the service layer.

Correct:

```text
Button click
 ↓
Service
 ↓
DAO
 ↓
Database
```

Incorrect:

```text
Button click
 ↓
SQL directly
```

Services should return business objects/results.

Examples:

```text
Booking
PaymentResult
Ticket
User
List<Event>
```

The UI should not need to understand:

```text
SQLState
JDBC exceptions
database constraint names
```

Translate technical failures into meaningful business exceptions.

---

# 18. TESTING LOOP

For every service:

```text
Implement
 ↓
Write happy-path test
 ↓
Write invalid-input tests
 ↓
Write edge-case tests
 ↓
Run tests
 ↓
Fix failures
 ↓
Run regression tests
```

## AuthService

Test:

```text
Valid registration
Duplicate email
Valid login
Invalid password
Unknown user
```

## EventService

Test:

```text
Create event
Invalid event
Get event
Update event
Unauthorized organizer
```

## BookingService

Test:

```text
Valid booking
Empty seat list
Invalid seat
Unavailable seat
Duplicate seat
Multiple seats
Price calculation
Double booking
```

## PaymentService

Test:

```text
Success
Failure
Invalid amount
Invalid booking
Repeated payment
```

## TicketService

Test:

```text
Ticket generation
Invalid booking
Failed payment cannot generate confirmed ticket
```

---

# 19. CRITICAL END-TO-END TEST

The backend should eventually support:

```text
Register
 ↓
Login
 ↓
Get event
 ↓
Select seats
 ↓
Create booking
 ↓
Calculate total
 ↓
Process payment
 ↓
Generate ticket
 ↓
Retrieve ticket
```

This complete flow must pass before the backend is considered complete.

---

# 20. ERROR-RESOLUTION LOOP

Whenever Kiro encounters an error:

```text
ERROR
 ↓
Read complete error / stack trace
 ↓
Find first meaningful application frame
 ↓
Identify root cause
 ↓
Classify error
 ↓
Fix responsible layer
 ↓
Run failing test
 ↓
Run full regression test
```

Classify as:

```text
Compilation
Dependency
Model
Service/business logic
DAO
Database
Concurrency
Validation
Integration
Test
```

Do not randomly edit unrelated files.

---

# 21. PROBLEM-SOLVING RULES

### Rule 1
Inspect before editing.

### Rule 2
Fix the root cause, not the symptom.

### Rule 3
Fix the correct layer.

```text
SQL/database problem → DAO/database
Business rule → Service
Data representation → Model
Display → UI
```

### Rule 4
Never bypass a problem by duplicating architecture.

### Rule 5
After every meaningful fix:

```bash
mvn clean test
```

### Rule 6
Do not mark a task complete until the feature is verified.

---

# 22. PROGRESS TRACKING

Maintain:

```text
docs/member2-progress.md
```

Example:

```markdown
# Member 2 Progress

## Completed
- [x] Project inspection
- [x] Domain models
- [x] Custom exceptions
- [x] Authentication service

## In Progress
- [ ] Booking service

## Blocked
- DAO seat reservation method required from Member 1

## Tests
- [x] Authentication
- [ ] Booking
- [ ] Payment
- [ ] Ticket

## Next Goal
Implement seat availability validation.
```

Update it after each major goal.

---

# 23. GIT WORKFLOW

Use a dedicated branch:

```bash
git checkout -b feature/member2-backend
```

Make focused commits:

```bash
git add .
git commit -m "feat: add domain models"
```

```bash
git commit -m "feat: implement authentication service"
```

```bash
git commit -m "feat: implement booking service"
```

```bash
git commit -m "feat: add payment simulation"
```

```bash
git commit -m "test: add backend service tests"
```

Before committing:

```bash
mvn clean test
git status
git diff
```

Never commit:

```text
Passwords
Database credentials
.env files
Secrets
Generated build files
Unrelated changes
```

---

# 24. FINAL CHECKLIST

Before declaring Member 2 complete:

```text
[ ] Domain models complete
[ ] Encapsulation implemented
[ ] Abstraction/interfaces used
[ ] Inheritance/polymorphism used meaningfully
[ ] Custom exceptions implemented
[ ] Authentication works
[ ] Event management works
[ ] Authorization works
[ ] Seat validation works
[ ] Booking works
[ ] Double booking prevented
[ ] Correct price calculation
[ ] Payment simulation works
[ ] Ticket generation works
[ ] DAO integration works
[ ] UI integration contract documented
[ ] Unit tests pass
[ ] End-to-end booking flow passes
[ ] mvn clean test passes
[ ] Git branch clean
```

---

# 25. DEFINITION OF DONE

Member 2 is complete only when:

```text
User
 ↓
Authentication
 ↓
Event
 ↓
Seat selection
 ↓
Booking
 ↓
Payment
 ↓
Ticket
```

works through:

```text
Swing UI
   ↓
Member 2 Service Layer
   ↓
Member 1 DAO Layer
   ↓
PostgreSQL
```

with:

- Correct business rules
- Proper validation
- Exception handling
- OOP principles
- Transaction-safe booking
- Double-booking protection
- Payment simulation
- Ticket generation
- Automated tests
- Clean integration

---

# 26. FIRST COMMAND FOR KIRO

When this file is loaded, **do not immediately generate the entire backend**.

Start with:

```text
GOAL:
Inspect the current repository and determine the exact implementation state.

INSPECT:
1. Project structure
2. pom.xml
3. Existing models
4. Existing DAOs
5. Existing services
6. Existing exceptions
7. Existing tests
8. Existing UI integration
9. Current Git branch
10. Current build status

CHECK:
Run mvn clean test.

REPORT:
1. What already exists
2. What is missing for Member 2
3. What is broken
4. What depends on Member 1
5. What Member 3 will need
6. Recommended implementation order

PLAN:
Create a step-by-step implementation plan.

IMPORTANT:
Do not overwrite existing work.
Do not create duplicate classes.
Do not implement everything in one step.

After the inspection, begin with the first smallest goal and follow:

GOAL → INSPECT → PLAN → IMPLEMENT → CHECK → RESOLVE → VERIFY → NEXT GOAL
```

---

# 27. MASTER PRINCIPLE

The objective is **not** to generate many Java files.

The objective is to build a backend that is:

```text
Correct
+
Testable
+
Maintainable
+
Well-structured
+
Integrated
+
Concurrency-safe
```

Always prefer a small verified change over a large unverified implementation.

**END OF MEMBER 2 KIRO WORKFLOW**
