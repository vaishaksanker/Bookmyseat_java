package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class StyledPasswordField extends JPasswordField {
    private String placeholder = "";
    private boolean isFocused = false;

    public StyledPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setFont(ThemeManager.FONT_BODY);
        setForeground(ThemeManager.TEXT_PRIMARY);
        setCaretColor(ThemeManager.ACCENT_RED);
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(ThemeManager.BG_TERTIARY);
        g2.fillRoundRect(0, 0, width, height, 10, 10);

        // Border
        g2.setColor(isFocused ? ThemeManager.BORDER_ACTIVE : ThemeManager.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(isFocused ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

        super.paintComponent(g);

        // Placeholder
        if (getPassword().length == 0 && !isFocused && placeholder != null && !placeholder.isEmpty()) {
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, 14, y);
        }

        g2.dispose();
    }
}
