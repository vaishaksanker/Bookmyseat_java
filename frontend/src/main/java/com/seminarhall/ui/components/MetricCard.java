package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MetricCard extends JPanel {
    public MetricCard(String title, String value, String subtitle, Color accentColor) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(ThemeManager.FONT_SMALL_BOLD);
        titleLbl.setForeground(ThemeManager.TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(ThemeManager.FONT_HEADER_XL);
        valLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(ThemeManager.FONT_SMALL);
        subLbl.setForeground(accentColor != null ? accentColor : ThemeManager.TEXT_SECONDARY);

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(6));
        content.add(valLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(subLbl);

        add(content, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(ThemeManager.BG_SECONDARY);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        g2.setColor(ThemeManager.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
