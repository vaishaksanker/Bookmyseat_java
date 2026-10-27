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
 * Fills an empty database with the same demo accounts, events and sample bookings the
 * UI's mock mode shows, so the login quick-fill buttons and screenshots keep working.
 *
 * Event dates are relative to the day the database is first created, so the demo events
 * are always in the future (the backend refuses to create past events).
 */
public class DemoDataSeeder {

    public static final String STUDENT_EMAIL = "student@college.edu";
    public static final String STUDENT_PASSWORD = "student123";
    public static final String ORGANIZER_EMAIL = "organizer@college.edu";
    public static final String ORGANIZER_PASSWORD = "admin123";

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
        ctx.authService.registerUser("Faculty Coordinator", ORGANIZER_EMAIL, ORGANIZER_PASSWORD, Role.ORGANIZER);

        LocalDate first = LocalDate.now().plusDays(14);
        Event e1 = event(first, 0, "AI in Modern World",
                "A deep dive into the impact of Artificial Intelligence in modern industries and future opportunities.",
                EventCategory.TECHNICAL_SEMINAR, "10:00", "12:00", "Computer Science Department", "Seminar Hall A",
                "Computer Science Department", "cs@college.edu", "0", 100);
        Event e2 = event(first, 2, "Web Development Bootcamp",
                "Intensive hands-on workshop covering modern frontend architectures, responsive design systems, and cloud deployment pipelines.",
                EventCategory.WORKSHOP, "14:00", "17:00", "IT Building", "Seminar Hall B",
                "Web Developers Society", "web@college.edu", "100", 100);
        Event e3 = event(first, 5, "Annual Cultural Meet",
                "Inter-college cultural competition and live stage arts featuring classical performances, fusion bands, and drama.",
                EventCategory.CULTURAL, "18:00", "21:30", "Campus Cultural Center", "Main Auditorium",
                "Fine Arts Club", "arts@college.edu", "150", 120);
        Event e4 = event(first, 8, "Career Guidance Seminar",
                "Career roadmap session with senior industry architects on internships, overseas education, and technical interview mastery.",
                EventCategory.GUEST_LECTURE, "11:00", "13:00", "Administrative Block", "Seminar Hall A",
                "Placement & Training Cell", "placement@college.edu", "0", 80);
        event(first, 10, "24-Hour Hackathon",
                "Build groundbreaking software and hardware prototypes with 24 hours of non-stop coding, mentoring, and prizes.",
                EventCategory.HACKATHON, "09:00", "09:00", "Innovation & Incubation Hub", "Lab 201 & Auditorium",
                "Coding Club", "hack@college.edu", "0", 150);
        event(first, 12, "Robotics Workshop",
                "Hardware prototyping, sensor fusion, ROS 2 development, and autonomous mobile robot navigation.",
                EventCategory.WORKSHOP, "10:00", "16:00", "Engineering Block 2", "Robotics Lab",
                "Robotics Club", "robotics@college.edu", "200", 80);
        event(first, 14, "Photography Contest & Expo",
                "Campus-wide photography showcase and live critique by acclaimed wildlife and street photographers.",
                EventCategory.CULTURAL, "15:00", "18:00", "Fine Arts Pavilion", "Gallery Hall",
                "Shutterbugs Society", "photo@college.edu", "50", 100);
        event(first, 16, "Cybersecurity Summit",
                "Ethical hacking, penetration testing, threat hunting, and modern Zero-Trust enterprise security architecture.",
                EventCategory.TECHNICAL_SEMINAR, "10:00", "13:00", "Cyber Defense Center", "Seminar Hall B",
                "InfoSec Student Chapter", "cyber@college.edu", "0", 120);
        event(first, 18, "Entrepreneurship Talk",
                "Venture building, pitching to angel investors, and scaling campus startups from zero to product-market fit.",
                EventCategory.GUEST_LECTURE, "14:00", "16:30", "MBA Department", "Main Auditorium",
                "E-Cell", "ecell@college.edu", "0", 140);
        event(first, 24, "Data Science Masterclass",
                "Applied machine learning, predictive analytics, pandas & scikit-learn, and real-world data pipelines.",
                EventCategory.WORKSHOP, "10:00", "15:00", "Data Analytics Lab", "CS Lab 2",
                "AI & Data Science Guild", "data@college.edu", "150", 90);
        event(first, 26, "Inter-College Sports Fest",
                "Annual collegiate athletics tournament spanning basketball, badminton, indoor volleyball, and table tennis.",
                EventCategory.SPORTS, "08:00", "18:00", "Sports Complex", "Indoor Arena & Grounds",
                "Physical Education Dept", "sports@college.edu", "75", 200);
        event(first, 29, "Open Mic & Standup Night",
                "An evening of campus acoustic melodies, poetry, standup comedy, and expressive live performances.",
                EventCategory.CULTURAL, "19:00", "21:30", "Student Amphitheatre", "Open Air Theatre",
                "Literary & Music Club", "openmic@college.edu", "50", 150);

        // Sample history for the demo student (matches the mock "My Bookings" screen)
        book(student, e1, "B5", "B6", "B7");
        book(student, e2, "A1", "A2");
        book(student, e3, "C3");
        Booking cancelled = book(student, e4, "D1", "D2");
        ctx.bookingService.cancelBooking(cancelled.getId());
        return true;
    }

    private Event event(LocalDate first, int dayOffset, String title, String description, EventCategory category,
                        String start, String end, String building, String hall,
                        String organizerName, String organizerEmail, String price, int capacity) {
        User organizer = ctx.userDAO.findByEmail(organizerEmail)
                .orElseGet(() -> ctx.authService.registerUser(organizerName, organizerEmail,
                        ORGANIZER_PASSWORD, Role.ORGANIZER));
        Venue venue = venues.computeIfAbsent(hall + "|" + building,
                k -> ctx.venueDAO.create(new Venue(0, hall, building, "Campus", capacity)));
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
