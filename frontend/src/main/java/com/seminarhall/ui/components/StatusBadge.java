package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class StatusBadge extends JLabel {
    private Color badgeColor;

    public StatusBadge(String text, Color color) {
        super(text);
        this.badgeColor = color;
        setFont(ThemeManager.FONT_SMALL_BOLD);
        setForeground(color);
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
    }

    public void setBadgeColor(Color color) {
        this.badgeColor = color;
        setForeground(color);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Semi-transparent badge background
        Color bg = new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 35);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        // Thin border
        g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 90));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

        g2.dispose();
        super.paintComponent(g);
    }
}
