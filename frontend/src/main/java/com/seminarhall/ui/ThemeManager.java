package com.seminarhall.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public final class ThemeManager {
    // SeminarX Exact Palette from UI design
    public static final Color BG_DARKEST = new Color(11, 12, 16);       // #0B0C10 pure dark base
    public static final Color BG_PRIMARY = new Color(16, 18, 23);       // #101217 main surface
    public static final Color BG_SECONDARY = new Color(22, 25, 33);     // #161921 card / sidebar / container
    public static final Color BG_CARD = new Color(27, 30, 40);          // #1B1E28 elevated card surface
    public static final Color BG_CARD_HOVER = new Color(34, 38, 50);    // #222632
    public static final Color BG_INPUT = new Color(20, 22, 30);         // #14161E input background
    public static final Color BG_TERTIARY = BG_CARD;                     // Alias for components

    // Gradients & Accents
    public static final Color ACCENT_RED = new Color(255, 68, 56);      // #FF4438 vibrant coral red
    public static final Color ACCENT_ORANGE = new Color(255, 110, 60);   // #FF6E3C gradient end
    public static final Color ACCENT_RED_HOVER = new Color(255, 90, 78);
    public static final Color ACCENT_RED_PRESSED = new Color(220, 50, 40);
    public static final Color ACCENT_GREEN = new Color(16, 185, 129);
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);
    public static final Color BORDER_ACTIVE = ACCENT_RED;

    // Status Colors (exact to badges in screenshot)
    public static final Color STATUS_GREEN = new Color(16, 185, 129);   // #10B981 Confirmed / Available / Free
    public static final Color STATUS_BLUE = new Color(59, 130, 246);    // #3B82F6 Upcoming / Technical
    public static final Color STATUS_PURPLE = new Color(147, 51, 234);  // #9333EA Cultural / Completed
    public static final Color STATUS_ORANGE = new Color(249, 115, 22);  // #F97316 Workshop / Featured
    public static final Color STATUS_AMBER = new Color(245, 158, 11);   // #F59E0B Warning / Selected
    public static final Color STATUS_RED = new Color(239, 68, 68);      // #EF4444 Cancelled / Booked

    // Text hierarchy
    public static final Color TEXT_PRIMARY = new Color(255, 255, 255);  // #FFFFFF pure white
    public static final Color TEXT_SECONDARY = new Color(180, 185, 200);// #B4B9C8 clear readable grey
    public static final Color TEXT_MUTED = new Color(110, 115, 135);    // #6E7387 subtle captions

    // Borders
    public static final Color BORDER_SUBTLE = new Color(36, 40, 52);    // #242834 clean edge
    public static final Color BORDER_FOCUS = new Color(255, 75, 60);    // #FF4B3C
    public static final Color BORDER_CARD = new Color(42, 46, 60);      // #2A2E3C

    // Seat Map Colors
    public static final Color SEAT_AVAILABLE = new Color(16, 185, 129); // Green pill in screenshot
    public static final Color SEAT_SELECTED = new Color(255, 158, 11);  // Amber/Orange selected in screenshot
    public static final Color SEAT_BOOKED = new Color(239, 68, 68);     // Red occupied in screenshot
    public static final Color SEAT_UNAVAILABLE = new Color(48, 52, 65); // Muted dark grey

    // Typography
    public static final Font FONT_LOGO = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_XL = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);

    private ThemeManager() {}

    public static void initializeTheme() {
        try {
            UIManager.put("Panel.background", BG_PRIMARY);
            UIManager.put("OptionPane.background", BG_SECONDARY);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.track", BG_PRIMARY);
            UIManager.put("ScrollBar.thumb", BG_CARD);

            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Theme initialization error: " + e.getMessage());
        }
    }
}
