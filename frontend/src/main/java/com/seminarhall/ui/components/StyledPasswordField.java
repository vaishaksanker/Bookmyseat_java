package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Password input matching {@link StyledTextField} (web app input style), with an optional leading icon.
 */
public class StyledPasswordField extends JPasswordField {
    private String placeholder = "";
    private boolean isFocused = false;
    private Icon leadingIcon;
    private int radius = ThemeManager.RADIUS_2XL;

    public StyledPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setFont(ThemeManager.font(ThemeManager.Weight.MEDIUM, 13));
        setForeground(ThemeManager.TEXT_PRIMARY);
        setCaretColor(ThemeManager.BRAND_RED);
        setSelectionColor(new Color(255, 56, 46, 90));
        setOpaque(false);
        updatePadding();

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

    /** Icon drawn inside the field on the left, e.g. Icons.get("lock", 16, ThemeManager.TEXT_MUTED). */
    public void setLeadingIcon(Icon icon) {
        this.leadingIcon = icon;
        updatePadding();
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    private void updatePadding() {
        int left = leadingIcon != null ? 16 + leadingIcon.getIconWidth() + 10 : 16;
        setBorder(new EmptyBorder(12, left, 12, 16));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = Math.min(radius * 2, height);

        g2.setColor(ThemeManager.BG_INPUT);
        g2.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);
        g2.setColor(isFocused ? ThemeManager.BORDER_FOCUS : ThemeManager.BORDER_CARD);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);

        if (leadingIcon != null) {
            leadingIcon.paintIcon(this, g2, 16, (height - leadingIcon.getIconHeight()) / 2);
        }

        super.paintComponent(g);

        if (getPassword().length == 0 && placeholder != null && !placeholder.isEmpty()) {
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }

        g2.dispose();
    }
}
