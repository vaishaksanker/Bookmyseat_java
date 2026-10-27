package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * The web app's tinted status pill (My Bookings "Status" column):
 * "text-[10px] font-bold px-2.5 py-1 rounded-md bg-blue-600/20 text-blue-400".
 */
public class StatusBadge extends JLabel {
    private Color background;

    /** Text in the given color on the same color at 20% opacity. */
    public StatusBadge(String text, Color color) {
        this(text, color, tint(color));
    }

    /** Text color and background chosen separately (e.g. text-blue-400 on bg-blue-600/20). */
    public StatusBadge(String text, Color textColor, Color background) {
        super(text);
        this.background = background;
        setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 10));
        setForeground(textColor);
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    }

    public void setBadgeColor(Color color) {
        this.background = tint(color);
        setForeground(color);
        repaint();
    }

    private static Color tint(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 51);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // rounded-md = 6px
        g2.dispose();
        super.paintComponent(g);
    }
}
