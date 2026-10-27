package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.ui.ThemeManager;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Photos, category tags and filter labels exactly as the web app shows them (web/src/App.jsx).
 * The backend stores no images, so photos are chosen by event title, then by category.
 */
public final class EventVisuals {

    // Backdrops used by the web app
    public static final String RED_THEATER = unsplash("photo-1489599849927-2ee91cede3ba", 1600);
    public static final String AUDITORIUM_STAGE = unsplash("photo-1517457373958-b7bdd4587205", 1600);

    private static final String AI_IMG = unsplash("photo-1540575467063-178a50c2df87", 1200);
    private static final String WEB_IMG = unsplash("photo-1531482615713-2afd69097998", 1000);
    private static final String CULT_IMG = unsplash("photo-1514525253161-7a46d19cd819", 1000);
    private static final String CAREER_IMG = unsplash("photo-1524178232363-1fb2b075b655", 1000);
    private static final String HACK_IMG = unsplash("photo-1504384308090-c894fdcc538d", 1000);
    private static final String SPORTS_IMG = unsplash("photo-1461896836934-ffe607ba8211", 1000);

    private static final Map<String, String> BY_TITLE = Map.ofEntries(
            Map.entry("AI in Modern World", AI_IMG),
            Map.entry("Web Development Bootcamp", WEB_IMG),
            Map.entry("Annual Cultural Meet", CULT_IMG),
            Map.entry("Career Guidance Seminar", CAREER_IMG),
            Map.entry("24-Hour Campus Hackathon", HACK_IMG),
            Map.entry("Robotics & IoT Workshop", unsplash("photo-1485827404703-89b55fcc595e", 1000)),
            Map.entry("Photography & Short Film Fest", unsplash("photo-1516035069371-29a1b244cc32", 1000)),
            Map.entry("Cybersecurity Summit 2024", unsplash("photo-1550751827-4bd374c3f58b", 1000)),
            Map.entry("Campus Startup Ideathon & Pitch", unsplash("photo-1507679799987-c73779587ccf", 1000)),
            Map.entry("Data Science & ML Hands-on Lab", unsplash("photo-1551288049-bebda4e38f71", 1000)),
            Map.entry("Inter-College Sports & Esports Arena", SPORTS_IMG),
            Map.entry("Open Mic Night & Comedy Roast", unsplash("photo-1493225457124-a3eb161ffa5f", 1000))
    );

    /** Filter chips in the order the web app shows them. */
    public static final List<EventCategory> CHIP_ORDER = List.of(
            EventCategory.ALL, EventCategory.TECHNICAL_SEMINAR, EventCategory.WORKSHOP, EventCategory.CULTURAL,
            EventCategory.GUEST_LECTURE, EventCategory.HACKATHON, EventCategory.SPORTS);

    private EventVisuals() {
    }

    private static String unsplash(String id, int width) {
        return "https://images.unsplash.com/" + id + "?auto=format&fit=crop&w=" + width + "&q=85";
    }

    /** The event's photo URL. */
    public static String imageFor(Event event) {
        if (event == null) {
            return AUDITORIUM_STAGE;
        }
        if (event.getBannerImagePath() != null && event.getBannerImagePath().startsWith("http")) {
            return event.getBannerImagePath();
        }
        String byTitle = BY_TITLE.get(event.getTitle());
        if (byTitle != null) {
            return byTitle;
        }
        switch (event.getCategory() == null ? EventCategory.TECHNICAL_SEMINAR : event.getCategory()) {
            case WORKSHOP: return WEB_IMG;
            case CULTURAL: return CULT_IMG;
            case GUEST_LECTURE: return CAREER_IMG;
            case HACKATHON: return HACK_IMG;
            case SPORTS: return SPORTS_IMG;
            default: return AI_IMG;
        }
    }

    /** Tag text on cards: TECHNICAL, WORKSHOP, CULTURAL, SEMINAR, HACKATHON, SPORTS. */
    public static String tagLabel(EventCategory category) {
        if (category == null) return "TECHNICAL";
        switch (category) {
            case WORKSHOP: return "WORKSHOP";
            case CULTURAL: return "CULTURAL";
            case GUEST_LECTURE: return "SEMINAR";
            case HACKATHON: return "HACKATHON";
            case SPORTS: return "SPORTS";
            default: return "TECHNICAL";
        }
    }

    /** Tag color: blue-600, amber-600, pink-600, emerald-600, purple-600, sky-600. */
    public static Color tagColor(EventCategory category) {
        if (category == null) return ThemeManager.BLUE_600;
        switch (category) {
            case WORKSHOP: return ThemeManager.AMBER_600;
            case CULTURAL: return ThemeManager.PINK_600;
            case GUEST_LECTURE: return ThemeManager.EMERALD_600;
            case HACKATHON: return ThemeManager.PURPLE_600;
            case SPORTS: return ThemeManager.SKY_600;
            default: return ThemeManager.BLUE_600;
        }
    }

    /** Filter chip text: All Events, Technical, Workshops, Cultural, Seminars, Hackathons, Sports. */
    public static String chipLabel(EventCategory category) {
        switch (category) {
            case ALL: return "All Events";
            case WORKSHOP: return "Workshops";
            case CULTURAL: return "Cultural";
            case GUEST_LECTURE: return "Seminars";
            case HACKATHON: return "Hackathons";
            case SPORTS: return "Sports";
            default: return "Technical";
        }
    }

    /** "Free" or "₹ 150", as on the web cards. */
    public static String priceLabel(double price) {
        return price <= 0 ? "Free" : "₹ " + String.format("%.0f", price);
    }
}
