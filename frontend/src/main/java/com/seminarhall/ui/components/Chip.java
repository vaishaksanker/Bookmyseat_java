package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Pill toggle used for filters (category chips, booking tabs):
 * active = glow-primary gradient with white text, inactive = bg-white/5 border-white/10 gray-300 text.
 * Clicks fire ActionListeners; the owner decides which chip is selected.
 */
public class Chip extends JButton {

    private boolean active;
    private boolean hovered;

    public Chip(String text, boolean active) {
        super(text);
        this.active = active;
        setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 12));
        setForeground(active ? Color.WHITE : ThemeManager.TEXT_GRAY_300);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(new EmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
        });
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : (hovered ? Color.WHITE : ThemeManager.TEXT_GRAY_300));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arc = ThemeManager.RADIUS_XL * 2;
        if (active) {
            g2.setPaint(new GradientPaint(0, 0, ThemeManager.BRAND_RED, w, h, ThemeManager.BRAND_ORANGE));
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        } else {
            g2.setColor(hovered ? ThemeManager.WHITE_10 : ThemeManager.WHITE_5);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(ThemeManager.BORDER_CARD);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
