package com.seminarhall.adapter.backend;

import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Event;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.User;
import com.bookmyshow.model.Venue;
import com.bookmyshow.model.enums.Role;
import com.seminarhall.model.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fills an empty database with the demo accounts, the 12 events of the SeminarX web app
 * (web/src/App.jsx INITIAL_EVENTS) and the demo student's sample bookings, so the desktop
 * app shows the same content as https://seminarx-booking.vercel.app.
 *
 * Event dates are relative to the day the database is first created, so the demo events
 * are always in the future (the backend refuses to create past events).
 */
public class DemoDataSeeder {

    public static final String STUDENT_EMAIL = "student@college.edu";
    public static final String STUDENT_PASSWORD = "student123";
    public static final String ORGANIZER_EMAIL = "organizer@college.edu";
    public static final String ORGANIZER_PASSWORD = "admin123";

    private static final String CAMPUS = "Main Campus";

    private final BackendContext ctx;
    private final Map<String, Venue> venues = new HashMap<>();

    public DemoDataSeeder(BackendContext ctx) {
        this.ctx = ctx;
    }

    /** Seeds only when the demo organizer account does not exist yet. Returns true if it seeded. */
    public boolean seedIfEmpty() {
        if (ctx.userDAO.existsByEmail(ORGANIZER_EMAIL)) {
            return false;
        }
        User student = ctx.authService.registerUser("Ananthakrishna S", STUDENT_EMAIL, STUDENT_PASSWORD, Role.CUSTOMER);
        // ADMIN so the Faculty Console can edit and cancel every department's events
        ctx.authService.registerUser("Faculty Coordinator", ORGANIZER_EMAIL, ORGANIZER_PASSWORD, Role.ADMIN);

        LocalDate first = LocalDate.now().plusDays(14);
        Event ai = event(first, 0, "AI in Modern World",
                "A deep dive into the impact of Artificial Intelligence in modern industries, generative AI models, autonomous agents, and breakthrough opportunities for college engineers.",
                EventCategory.TECHNICAL_SEMINAR, "10:00", "12:00", "Seminar Hall A",
                "Computer Science & Engineering", "cs@college.edu", "0", 100);
        Event web = event(first, 2, "Web Development Bootcamp",
                "Master modern full-stack web engineering: React 18, Tailwind CSS, REST APIs, and production deployment with automated CI/CD.",
                EventCategory.WORKSHOP, "14:00", "17:00", "Seminar Hall B",
                "Information Technology", "web@college.edu", "100", 100);
        Event cultural = event(first, 5, "Annual Cultural Meet",
                "The grand annual college cultural evening featuring acoustic musical sets, theater performances, fusion dances, and stand-up comedy.",
                EventCategory.CULTURAL, "18:00", "21:30", "Main Auditorium",
                "Student Arts Council", "arts@college.edu", "150", 250);
        Event career = event(first, 8, "Career Guidance Seminar",
                "Crucial strategic session on cracking top campus placements, resume polishing, technical interviews, and applying for global master's programs.",
                EventCategory.GUEST_LECTURE, "11:00", "13:00", "Seminar Hall A",
                "Placement & Training Cell", "placement@college.edu", "0", 100);
        event(first, 10, "24-Hour Campus Hackathon",
                "Build cutting-edge prototypes in AI, FinTech, and Smart Campus solutions over a 24-hour sprint with prizes worth ₹1,00,000.",
                EventCategory.HACKATHON, "09:00", "09:00", "Innovation Lab",
                "Innovation & Incubation Cell", "hack@college.edu", "0", 120);
        event(first, 12, "Robotics & IoT Workshop",
                "Hands-on robotics hardware workshop covering microcontrollers, ultrasonic sensors, motor drivers, and real-time obstacle avoidance.",
                EventCategory.WORKSHOP, "10:00", "16:00", "Engineering Lab",
                "Robotics Club", "robotics@college.edu", "200", 80);
        event(first, 14, "Photography & Short Film Fest",
                "Screening of student short documentaries, visual storytelling masterclass, and campus photography gallery showcase.",
                EventCategory.CULTURAL, "15:00", "18:00", "Gallery Hall",
                "Media Arts Society", "photo@college.edu", "50", 100);
        event(first, 16, "Cybersecurity Summit 2024",
                "Explore ethical hacking, network packet sniffing, zero-day mitigation, bug bounty workflows, and modern web application defense.",
                EventCategory.TECHNICAL_SEMINAR, "10:00", "13:00", "Seminar Hall B",
                "Cyber Defense Cell", "cyber@college.edu", "0", 100);
        event(first, 18, "Campus Startup Ideathon & Pitch",
                "Pitch your early startup ideas before seasoned venture capitalists, receive seed mentoring, and win student grant allocations.",
                EventCategory.GUEST_LECTURE, "14:00", "17:00", "Main Auditorium",
                "Entrepreneurship Development Cell", "ecell@college.edu", "0", 200);
        event(first, 24, "Data Science & ML Hands-on Lab",
                "Practical deep-learning laboratory on training neural nets, handling large tabular datasets with Pandas, and deploying models with FastAPI.",
                EventCategory.WORKSHOP, "10:00", "15:00", "CS Lab 2",
                "Data Science & AI", "data@college.edu", "150", 60);
        event(first, 26, "Inter-College Sports & Esports Arena",
                "Annual athletics tournament, badminton finals, and live LAN esports championship with commentary on the big auditorium displays.",
                EventCategory.SPORTS, "08:00", "18:00", "Sports Complex",
                "Physical Education & Gaming Guild", "sports@college.edu", "75", 300);
        event(first, 29, "Open Mic Night & Comedy Roast",
                "An open stage evening under the stars for campus poets, comedians, acoustic guitarists, and storytellers.",
                EventCategory.CULTURAL, "19:00", "22:00", "Open Air Theatre",
                "Literary & Music Club", "openmic@college.edu", "50", 180);

        // Sample history for the demo student (the web app's "My Bookings" rows)
        book(student, ai, "B5", "B6");
        book(student, web, "A1", "A2");
        book(student, cultural, "C3");
        Booking cancelled = book(student, career, "D1", "D2");
        ctx.bookingService.cancelBooking(cancelled.getId());
        return true;
    }

    private Event event(LocalDate first, int dayOffset, String title, String description, EventCategory category,
                        String start, String end, String hall,
                        String department, String organizerEmail, String price, int capacity) {
        // The organizer account carries the department name the web app shows on each event
        User organizer = ctx.userDAO.findByEmail(organizerEmail)
                .orElseGet(() -> ctx.authService.registerUser(department, organizerEmail,
                        ORGANIZER_PASSWORD, Role.ORGANIZER));
        Venue venue = venues.computeIfAbsent(hall,
                k -> ctx.venueDAO.create(new Venue(0, hall, CAMPUS, "Campus", capacity)));
        LocalDate date = first.plusDays(dayOffset);
        LocalDateTime startAt = LocalDateTime.of(date, LocalTime.parse(start));
        LocalDateTime endAt = LocalDateTime.of(date, LocalTime.parse(end));
        if (!endAt.isAfter(startAt)) {
            endAt = endAt.plusDays(1);
        }
        return ctx.eventService.createEvent(organizer, title, description, category.name(),
                venue.getId(), startAt, endAt, new BigDecimal(price), capacity);
    }

    private Booking book(User user, Event event, String... seatLabels) {
        List<String> wanted = List.of(seatLabels);
        List<Integer> seatIds = ctx.seatDAO.findByEventId(event.getId()).stream()
                .filter(s -> wanted.contains(s.getSeatLabel()))
                .map(Seat::getId)
                .collect(Collectors.toList());
        Booking booking = ctx.bookingService.createBooking(user, event.getId(), seatIds);
        if (booking.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            ctx.bookingService.confirmBooking(booking.getId());
        } else {
            ctx.paymentService.processPayment(booking.getId(), booking.getTotalAmount());
        }
        ctx.ticketService.generateTicket(booking.getId());
        return booking;
    }
}
