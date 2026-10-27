package com.seminarhall.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

/**
 * The web app's Lucide icons (resources/icons/lucide, generated from lucide-react, ISC license).
 *
 * Names are the lucide file names: calendar, clock, map-pin, users, check, search, bell, user,
 * lock, mail, eye, eye-off, pencil, trash-2, bookmark, file-text, circle-check, shield, sparkles,
 * chevron-right, arrow-left, menu, x, download, qr-code, ticket, compass, layout-dashboard,
 * share-2, printer.
 */
public final class Icons {

    private Icons() {
    }

    /** A Lucide icon at the given pixel size, stroked in the given color (lucide default stroke width 2). */
    public static Icon get(String name, int size, Color color) {
        FlatSVGIcon icon = new FlatSVGIcon("icons/lucide/" + name + ".svg", size, size);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        return icon;
    }

    /** A label showing only an icon. */
    public static JLabel label(String name, int size, Color color) {
        return new JLabel(get(name, size, color));
    }

    /** An icon followed by text, e.g. calendar + "Dec 10, 2024". */
    public static JLabel withText(String name, int size, Color iconColor, String text, Font font, Color textColor) {
        JLabel label = new JLabel(text, get(name, size, iconColor), SwingConstants.LEFT);
        label.setIconTextGap(6);
        label.setFont(font);
        label.setForeground(textColor);
        return label;
    }
}
