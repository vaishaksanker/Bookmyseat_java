package com.seminarhall.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Colors, fonts and sizes of the SeminarX web app (web/src/index.css + tailwind.config.js),
 * so the desktop screens match https://seminarx-booking.vercel.app.
 *
 * Tailwind sizes map 1:1 to Swing pixels: text-xs = 12, text-sm = 14, text-xl = 20,
 * rounded-xl = 12, rounded-2xl = 16, rounded-3xl = 24.
 */
public final class ThemeManager {

    // --- Surfaces (index.css body, tailwind darkBg / cardBg / cardBorder) ---
    public static final Color BG_PAGE = new Color(5, 6, 9);              // #050609 body
    public static final Color BG_DARKEST = BG_PAGE;
    public static final Color BG_PRIMARY = new Color(9, 10, 15);         // #090a0f darkBg
    public static final Color BG_SECONDARY = new Color(18, 20, 28);      // #12141c cardBg
    public static final Color BG_CARD = new Color(18, 21, 32);           // glass-card rgba(18,21,32,.82)
    public static final Color BG_CARD_HOVER = new Color(26, 29, 42);
    public static final Color BG_GLASS = new Color(14, 16, 25);          // glass-morphic rgba(14,16,25,.78)
    public static final Color BG_INPUT = new Color(8, 10, 16);           // #080a10 inputs
    public static final Color BG_HEADER = new Color(9, 10, 15, 235);     // sticky header, bg-[#090a0f]/90
    public static final Color BG_TERTIARY = BG_CARD;

    /** bg-white/5 and bg-white/10 on the page background, pre-blended. */
    public static final Color WHITE_5 = new Color(255, 255, 255, 13);
    public static final Color WHITE_10 = new Color(255, 255, 255, 26);

    // --- Brand (glow-primary gradient #ff382e → #ff6835) ---
    public static final Color BRAND_RED = new Color(255, 56, 46);        // #ff382e
    public static final Color BRAND_ORANGE = new Color(255, 104, 53);    // #ff6835
    public static final Color ACCENT_RED = BRAND_RED;
    public static final Color ACCENT_ORANGE = BRAND_ORANGE;
    public static final Color ACCENT_RED_HOVER = new Color(255, 35, 24); // hover #ff2318
    public static final Color ACCENT_RED_PRESSED = new Color(220, 40, 30);
    public static final Color BRAND_GLOW = new Color(255, 56, 46, 140);  // rgba(255,56,46,.55)
    public static final Color ACCENT_GREEN = new Color(16, 185, 129);
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);
    public static final Color BORDER_ACTIVE = BRAND_RED;

    // --- Tailwind palette used by tags and statuses ---
    public static final Color BLUE_600 = new Color(37, 99, 235);
    public static final Color AMBER_600 = new Color(217, 119, 6);
    public static final Color PINK_600 = new Color(219, 39, 119);
    public static final Color EMERALD_600 = new Color(5, 150, 105);
    public static final Color PURPLE_600 = new Color(147, 51, 234);
    public static final Color SKY_600 = new Color(2, 132, 199);
    public static final Color EMERALD_400 = new Color(52, 211, 153);
    public static final Color BLUE_400 = new Color(96, 165, 250);
    public static final Color RED_400 = new Color(248, 113, 113);
    public static final Color AMBER_400 = new Color(251, 191, 36);

    public static final Color STATUS_GREEN = new Color(16, 185, 129);
    public static final Color STATUS_BLUE = new Color(59, 130, 246);
    public static final Color STATUS_PURPLE = new Color(147, 51, 234);
    public static final Color STATUS_ORANGE = new Color(249, 115, 22);
    public static final Color STATUS_AMBER = new Color(245, 158, 11);
    public static final Color STATUS_RED = new Color(239, 68, 68);

    // --- Text (white, gray-300, gray-400, gray-500) ---
    public static final Color TEXT_PRIMARY = Color.WHITE;
    public static final Color TEXT_GRAY_300 = new Color(209, 213, 219);
    public static final Color TEXT_SECONDARY = new Color(156, 163, 175);  // gray-400
    public static final Color TEXT_MUTED = new Color(107, 114, 128);      // gray-500

    // --- Borders (white/8, white/10, white/12) ---
    public static final Color BORDER_SUBTLE = new Color(255, 255, 255, 20);
    public static final Color BORDER_CARD = new Color(255, 255, 255, 26);
    public static final Color BORDER_GLASS = new Color(255, 255, 255, 31);
    public static final Color BORDER_FOCUS = BRAND_RED;

    // --- Seat map ---
    public static final Color SEAT_AVAILABLE = new Color(16, 185, 129);
    public static final Color SEAT_SELECTED = BRAND_RED;
    public static final Color SEAT_BOOKED = new Color(55, 60, 75);
    public static final Color SEAT_UNAVAILABLE = new Color(48, 52, 65);

    // --- Radii ---
    public static final int RADIUS_LG = 8;
    public static final int RADIUS_XL = 12;
    public static final int RADIUS_2XL = 16;
    public static final int RADIUS_3XL = 24;

    // --- Typography: Plus Jakarta Sans (bundled in /fonts) ---
    public enum Weight { REGULAR, MEDIUM, SEMIBOLD, BOLD, EXTRABOLD }

    private static final Map<Weight, Font> FACES = loadFaces();

    /** Plus Jakarta Sans at a Tailwind weight and pixel size. font-black renders as EXTRABOLD, as on the web. */
    public static Font font(Weight weight, float size) {
        return FACES.get(weight).deriveFont(size).deriveFont(TEXT_FEATURES);
    }

    /** Browsers apply kerning and ligatures by default; Java needs them switched on. */
    private static final Map<java.awt.font.TextAttribute, Object> TEXT_FEATURES = Map.of(
            java.awt.font.TextAttribute.KERNING, java.awt.font.TextAttribute.KERNING_ON,
            java.awt.font.TextAttribute.LIGATURES, java.awt.font.TextAttribute.LIGATURES_ON);

    public static final Font FONT_LOGO = font(Weight.EXTRABOLD, 20);
    public static final Font FONT_HEADER_XL = font(Weight.EXTRABOLD, 28);
    public static final Font FONT_HEADER = font(Weight.EXTRABOLD, 20);
    public static final Font FONT_SUBHEADER = font(Weight.BOLD, 14);
    public static final Font FONT_BODY = font(Weight.REGULAR, 13);
    public static final Font FONT_BODY_BOLD = font(Weight.SEMIBOLD, 13);
    public static final Font FONT_SMALL = font(Weight.REGULAR, 11);
    public static final Font FONT_SMALL_BOLD = font(Weight.BOLD, 11);

    private ThemeManager() {}

    private static Map<Weight, Font> loadFaces() {
        Map<Weight, Font> faces = new EnumMap<>(Weight.class);
        String[] files = {"Regular", "Medium", "SemiBold", "Bold", "ExtraBold"};
        int[] fallbackStyles = {Font.PLAIN, Font.PLAIN, Font.BOLD, Font.BOLD, Font.BOLD};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Weight weight : Weight.values()) {
            String file = "/fonts/PlusJakartaSans-" + files[weight.ordinal()] + ".ttf";
            try (InputStream in = ThemeManager.class.getResourceAsStream(file)) {
                Font face = Font.createFont(Font.TRUETYPE_FONT, in);
                ge.registerFont(face);
                faces.put(weight, face);
            } catch (Exception e) {
                System.err.println("Could not load " + file + ", using Segoe UI: " + e.getMessage());
                faces.put(weight, new Font("Segoe UI", fallbackStyles[weight.ordinal()], 13));
            }
        }
        return faces;
    }

    public static void initializeTheme() {
        try {
            UIManager.put("defaultFont", FONT_BODY);
            UIManager.put("Panel.background", BG_PAGE);
            UIManager.put("OptionPane.background", BG_SECONDARY);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            UIManager.put("Button.arc", RADIUS_XL);
            UIManager.put("Component.arc", RADIUS_XL);
            UIManager.put("ProgressBar.arc", RADIUS_XL);
            UIManager.put("TextComponent.arc", RADIUS_XL);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.track", BG_PAGE);
            UIManager.put("ScrollBar.thumb", new Color(40, 44, 58));
            UIManager.put("ComboBox.background", BG_INPUT);
            UIManager.put("ComboBox.buttonBackground", BG_INPUT);
            UIManager.put("ComboBox.popupBackground", BG_SECONDARY);
            UIManager.put("List.selectionBackground", BRAND_RED);

            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Theme initialization error: " + e.getMessage());
        }
    }
}
