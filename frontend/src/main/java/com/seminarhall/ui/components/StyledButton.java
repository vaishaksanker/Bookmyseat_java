package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledButton extends JButton {
    public enum ButtonStyle {
        PRIMARY,    // Radiant Red-to-Orange gradient CTA
        SECONDARY,  // Dark card surface button
        OUTLINE,    // Subtle dark border
        SUCCESS,    // Emerald button
        TAB         // Navigation tab
    }

    private ButtonStyle buttonStyle;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 10;

    public StyledButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public StyledButton(String text, ButtonStyle style) {
        super(text);
        this.buttonStyle = style;
        setFont(ThemeManager.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 20, 10, 20));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setButtonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color textColor = ThemeManager.TEXT_PRIMARY;
        Color borderColor = null;

        if (!isEnabled()) {
            g2.setColor(new Color(42, 46, 58));
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
            textColor = ThemeManager.TEXT_MUTED;
        } else if (buttonStyle == ButtonStyle.PRIMARY) {
            // SeminarX gradient button (#FF4438 -> #FF6E3C)
            Color start = isPressed ? ThemeManager.ACCENT_RED_PRESSED :
                    (isHovered ? ThemeManager.ACCENT_RED_HOVER : ThemeManager.ACCENT_RED);
            Color end = isPressed ? ThemeManager.ACCENT_RED : ThemeManager.ACCENT_ORANGE;
            GradientPaint gp = new GradientPaint(0, 0, start, width, height, end);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        } else if (buttonStyle == ButtonStyle.SUCCESS) {
            g2.setColor(isHovered ? ThemeManager.STATUS_GREEN.brighter() : ThemeManager.STATUS_GREEN);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        } else if (buttonStyle == ButtonStyle.SECONDARY) {
            g2.setColor(isHovered ? ThemeManager.BG_CARD_HOVER : ThemeManager.BG_CARD);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
            borderColor = ThemeManager.BORDER_SUBTLE;
        } else if (buttonStyle == ButtonStyle.TAB) {
            if (isHovered) {
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
            }
        } else { // OUTLINE
            g2.setColor(isHovered ? ThemeManager.BG_CARD : new Color(0, 0, 0, 0));
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
            borderColor = isHovered ? ThemeManager.ACCENT_RED : ThemeManager.BORDER_SUBTLE;
        }

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        // Draw text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(textColor);
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
