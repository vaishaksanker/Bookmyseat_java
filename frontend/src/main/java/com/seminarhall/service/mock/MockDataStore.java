package com.seminarhall.service.mock;

import com.seminarhall.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MockDataStore {
    private static MockDataStore instance;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> userPasswords = new ConcurrentHashMap<>();
    private final Map<String, Event> events = new ConcurrentHashMap<>();
    private final Map<String, List<Seat>> eventSeats = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    private MockDataStore() {
        seedInitialData();
    }

    public static synchronized MockDataStore getInstance() {
        if (instance == null) {
            instance = new MockDataStore();
        }
        return instance;
    }

    private void seedInitialData() {
        // Users matching prototype
        User student = new User("usr_student_1", "Ananthakrishna S", "student@college.edu", "+91 9876543210", UserRole.STUDENT);
        users.put(student.getEmail().toLowerCase(), student);
        userPasswords.put(student.getEmail().toLowerCase(), "student123");

        User organizer = new User("usr_organizer_1", "Faculty Coordinator", "organizer@college.edu", "+91 9123456789", UserRole.ORGANIZER);
        users.put(organizer.getEmail().toLowerCase(), organizer);
        userPasswords.put(organizer.getEmail().toLowerCase(), "admin123");

        // Events matching exact cards in Screen 3, 4, 9, 10
        Event e1 = new Event(
                "ev_101",
                "AI in Modern World",
                "A deep dive into the impact of Artificial Intelligence in modern industries and future opportunities.",
                EventCategory.TECHNICAL_SEMINAR,
                LocalDate.of(2026, 12, 10),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "Computer Science Department",
                "Seminar Hall A",
                "Computer Science Department",
                "cs@college.edu",
                0.0, // Free
                100,
                100,
                null,
                "FEATURED"
        );

        Event e2 = new Event(
                "ev_102",
                "Web Development Bootcamp",
                "Intensive hands-on workshop covering modern frontend architectures, responsive design systems, and cloud deployment pipelines.",
                EventCategory.WORKSHOP,
                LocalDate.of(2026, 12, 12),
                LocalTime.of(14, 0),
                LocalTime.of(17, 0),
                "IT Building",
                "Seminar Hall B",
                "Web Developers Society",
                "web@college.edu",
                100.0,
                100,
                100,
                null,
                "POPULAR"
        );

        Event e3 = new Event(
                "ev_103",
                "Annual Cultural Meet",
                "Inter-college cultural competition and live stage arts featuring classical performances, fusion bands, and drama.",
                EventCategory.CULTURAL,
                LocalDate.of(2026, 12, 15),
                LocalTime.of(18, 0),
                LocalTime.of(21, 30),
                "Campus Cultural Center",
                "Main Auditorium",
                "Fine Arts Club",
                "arts@college.edu",
                150.0,
                120,
                120,
                null,
                "TRENDING"
        );

        Event e4 = new Event(
                "ev_104",
                "Career Guidance Seminar",
                "Career roadmap session with senior industry architects on internships, overseas education, and technical interview mastery.",
                EventCategory.GUEST_LECTURE,
                LocalDate.of(2026, 12, 18),
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                "Administrative Block",
                "Seminar Hall A",
                "Placement & Training Cell",
                "placement@college.edu",
                0.0, // Free
                80,
                80,
                null,
                "FREE ENTRY"
        );

        Event e5 = new Event(
                "ev_105",
                "24-Hour Hackathon",
                "Build groundbreaking software and hardware prototypes with 24 hours of non-stop coding, mentoring, and prizes.",
                EventCategory.HACKATHON,
                LocalDate.of(2026, 12, 20),
                LocalTime.of(9, 0),
                LocalTime.of(9, 0),
                "Innovation & Incubation Hub",
                "Lab 201 & Auditorium",
                "Coding Club",
                "hack@college.edu",
                0.0,
                150,
                150,
                null,
                "HACKATHON"
        );

        Event e6 = new Event(
                "ev_106",
                "Robotics Workshop",
                "Hardware prototyping, sensor fusion, ROS 2 development, and autonomous mobile robot navigation.",
                EventCategory.WORKSHOP,
                LocalDate.of(2026, 12, 22),
                LocalTime.of(10, 0),
                LocalTime.of(16, 0),
                "Engineering Block 2",
                "Robotics Lab",
                "Robotics Club",
                "robotics@college.edu",
                200.0,
                80,
                80,
                null,
                "UPCOMING"
        );

        Event e7 = new Event(
                "ev_107",
                "Photography Contest & Expo",
                "Campus-wide photography showcase and live critique by acclaimed wildlife and street photographers.",
                EventCategory.CULTURAL,
                LocalDate.of(2026, 12, 24),
                LocalTime.of(15, 0),
                LocalTime.of(18, 0),
                "Fine Arts Pavilion",
                "Gallery Hall",
                "Shutterbugs Society",
                "photo@college.edu",
                50.0,
                100,
                100,
                null,
                "EXHIBITION"
        );

        Event e8 = new Event(
                "ev_108",
                "Cybersecurity Summit",
                "Ethical hacking, penetration testing, threat hunting, and modern Zero-Trust enterprise security architecture.",
                EventCategory.TECHNICAL_SEMINAR,
                LocalDate.of(2026, 12, 26),
                LocalTime.of(10, 0),
                LocalTime.of(13, 0),
                "Cyber Defense Center",
                "Seminar Hall B",
                "InfoSec Student Chapter",
                "cyber@college.edu",
                0.0,
                120,
                120,
                null,
                "SECURITY"
        );

        Event e9 = new Event(
                "ev_109",
                "Entrepreneurship Talk",
                "Venture building, pitching to angel investors, and scaling campus startups from zero to product-market fit.",
                EventCategory.GUEST_LECTURE,
                LocalDate.of(2026, 12, 28),
                LocalTime.of(14, 0),
                LocalTime.of(16, 30),
                "MBA Department",
                "Main Auditorium",
                "E-Cell",
                "ecell@college.edu",
                0.0,
                140,
                140,
                null,
                "KEYNOTE"
        );

        Event e10 = new Event(
                "ev_110",
                "Data Science Masterclass",
                "Applied machine learning, predictive analytics, pandas & scikit-learn, and real-world data pipelines.",
                EventCategory.WORKSHOP,
                LocalDate.of(2027, 1, 3),
                LocalTime.of(10, 0),
                LocalTime.of(15, 0),
                "Data Analytics Lab",
                "CS Lab 2",
                "AI & Data Science Guild",
                "data@college.edu",
                150.0,
                90,
                90,
                null,
                "MASTERCLASS"
        );

        Event e11 = new Event(
                "ev_111",
                "Inter-College Sports Fest",
                "Annual collegiate athletics tournament spanning basketball, badminton, indoor volleyball, and table tennis.",
                EventCategory.SPORTS,
                LocalDate.of(2027, 1, 5),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                "Sports Complex",
                "Indoor Arena & Grounds",
                "Physical Education Dept",
                "sports@college.edu",
                75.0,
                200,
                200,
                null,
                "TOURNAMENT"
        );

        Event e12 = new Event(
                "ev_112",
                "Open Mic & Standup Night",
                "An evening of campus acoustic melodies, poetry, standup comedy, and expressive live performances.",
                EventCategory.CULTURAL,
                LocalDate.of(2027, 1, 8),
                LocalTime.of(19, 0),
                LocalTime.of(21, 30),
                "Student Amphitheatre",
                "Open Air Theatre",
                "Literary & Music Club",
                "openmic@college.edu",
                50.0,
                150,
                150,
                null,
                "ENTERTAINMENT"
        );

        events.put(e1.getId(), e1);
        events.put(e2.getId(), e2);
        events.put(e3.getId(), e3);
        events.put(e4.getId(), e4);
        events.put(e5.getId(), e5);
        events.put(e6.getId(), e6);
        events.put(e7.getId(), e7);
        events.put(e8.getId(), e8);
        events.put(e9.getId(), e9);
        events.put(e10.getId(), e10);
        events.put(e11.getId(), e11);
        events.put(e12.getId(), e12);

        for (Event event : events.values()) {
            List<Seat> seats = generateAuditoriumSeats(event);
            eventSeats.put(event.getId(), seats);
        }

        // Pre-seed sample bookings matching Screen 9 (My Bookings)
        Booking b1 = new Booking(
                "BK001234",
                e1.getId(),
                e1.getTitle(),
                e1.getVenue(),
                e1.getSeminarHall(),
                LocalDateTime.of(e1.getDate(), e1.getStartTime()),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                Arrays.asList("B5", "B6", "B7"),
                Arrays.asList(e1.getId() + "_B5", e1.getId() + "_B6", e1.getId() + "_B7"),
                0.0,
                BookingStatus.CONFIRMED,
                "Free Registration",
                "TXN_REG_001234"
        );

        Booking b2 = new Booking(
                "BK001235",
                e2.getId(),
                e2.getTitle(),
                e2.getVenue(),
                e2.getSeminarHall(),
                LocalDateTime.of(e2.getDate(), e2.getStartTime()),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                Arrays.asList("A1", "A2"),
                Arrays.asList(e2.getId() + "_A1", e2.getId() + "_A2"),
                100.0,
                BookingStatus.CONFIRMED,
                "UPI (GPay)",
                "TXN_UPI_992144"
        );

        Booking b3 = new Booking(
                "BK001236",
                e3.getId(),
                e3.getTitle(),
                e3.getVenue(),
                e3.getSeminarHall(),
                LocalDateTime.of(e3.getDate(), e3.getStartTime()),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                Collections.singletonList("C3"),
                Collections.singletonList(e3.getId() + "_C3"),
                150.0,
                BookingStatus.CONFIRMED,
                "Credit Card",
                "TXN_CARD_772091"
        );

        Booking b4 = new Booking(
                "BK001237",
                e4.getId(),
                e4.getTitle(),
                e4.getVenue(),
                e4.getSeminarHall(),
                LocalDateTime.of(e4.getDate(), e4.getStartTime()),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                Arrays.asList("D1", "D2"),
                Arrays.asList(e4.getId() + "_D1", e4.getId() + "_D2"),
                0.0,
                BookingStatus.CANCELLED,
                "Direct",
                "TXN_DIRECT_102"
        );

        bookings.put(b1.getId(), b1);
        bookings.put(b2.getId(), b2);
        bookings.put(b3.getId(), b3);
        bookings.put(b4.getId(), b4);

        // Tickets for b1
        for (String seatIdfr : b1.getSeatIdentifiers()) {
            String ticketNum = "TKT-" + b1.getId() + "-" + seatIdfr;
            Ticket t = new Ticket(
                    ticketNum,
                    b1.getId(),
                    e1.getTitle(),
                    e1.getVenue(),
                    e1.getSeminarHall(),
                    b1.getEventDateTime(),
                    student.getFullName(),
                    student.getEmail(),
                    seatIdfr,
                    0.0,
                    "SEMINARX:EVENT:" + e1.getId() + ":TKT:" + ticketNum
            );
            tickets.put(t.getTicketNumber(), t);
        }
    }

    private List<Seat> generateAuditoriumSeats(Event event) {
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        int seatsPerRow = 10;
        int total = Math.min(event.getTotalCapacity(), rows.length * seatsPerRow);
        Random rnd = new Random(event.getId().hashCode());

        // All seats start clean and available
        Set<String> prebooked = new HashSet<>();

        int count = 0;
        for (String r : rows) {
            for (int n = 1; n <= seatsPerRow; n++) {
                if (count >= total) break;
                String seatIdfr = r + n;
                String seatId = event.getId() + "_" + seatIdfr;
                SeatStatus status = prebooked.contains(seatIdfr) ? SeatStatus.BOOKED : SeatStatus.AVAILABLE;
                seats.add(new Seat(seatId, event.getId(), r, n, status, event.getTicketPrice(), r.equals("A") && (n == 1 || n == 10)));
                count++;
            }
            if (count >= total) break;
        }
        return seats;
    }

    public Map<String, User> getUsers() { return users; }
    public Map<String, String> getUserPasswords() { return userPasswords; }
    public Map<String, Event> getEvents() { return events; }
    public Map<String, List<Seat>> getEventSeats() { return eventSeats; }
    public Map<String, Booking> getBookings() { return bookings; }
    public Map<String, Ticket> getTickets() { return tickets; }
}
