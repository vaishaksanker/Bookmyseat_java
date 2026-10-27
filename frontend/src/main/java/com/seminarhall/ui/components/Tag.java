package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Small rounded label, e.g. the web app's category tags
 * ("px-2.5 py-1 rounded-md text-[9px] font-black tracking-widest bg-blue-600")
 * and tinted status pills ("bg-emerald-600/20 text-emerald-400").
 */
public class Tag extends JLabel {

    private final Color background;
    private final int radius;

    /** Solid tag: white uppercase text on a colored background. */
    public Tag(String text, Color background) {
        this(text, background, Color.WHITE, 6, ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 9.5f));
    }

    /** Fully custom tag. */
    public Tag(String text, Color background, Color foreground, int radius, Font font) {
        super(text);
        this.background = background;
        this.radius = radius;
        setFont(font);
        setForeground(foreground);
        setOpaque(false);
        setBorder(new EmptyBorder(4, 9, 4, 9));
    }

    /** Tinted pill: text in the color, background the same color at 20% (bg-x/20 text-x-400). */
    public static Tag tinted(String text, Color color) {
        return new Tag(text, new Color(color.getRed(), color.getGreen(), color.getBlue(), 51), color,
                999, ThemeManager.font(ThemeManager.Weight.BOLD, 11));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = Math.min(radius * 2, getHeight());
        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
