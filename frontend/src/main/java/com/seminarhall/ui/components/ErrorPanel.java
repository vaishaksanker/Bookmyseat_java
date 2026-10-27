package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

/** Centered "Unable to Load" message with the error text and an optional "Try Again" button. */
public class ErrorPanel extends JPanel {
    public ErrorPanel(String errorMessage, Runnable onRetry) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        // x icon in a tinted red circle (bg-[#ff382e]/15 border-[#ff382e]/30)
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 56, 46, 38));
                g2.fillOval(0, 0, 55, 55);
                g2.setColor(new Color(255, 56, 46, 77));
                g2.drawOval(0, 0, 55, 55);
                g2.dispose();
                Icons.get("x", 24, ThemeManager.BRAND_RED).paintIcon(this, g, 16, 16);
            }
        };
        Dimension iconSize = new Dimension(56, 56);
        icon.setPreferredSize(iconSize);
        icon.setMaximumSize(iconSize);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Unable to Load", SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 18));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String safe = errorMessage == null ? "Something went wrong." : errorMessage
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel msgLabel = new JLabel("<html><div style='text-align:center;width:360px'>" + safe + "</div></html>",
                SwingConstants.CENTER);
        msgLabel.setFont(ThemeManager.font(ThemeManager.Weight.REGULAR, 14));
        msgLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(icon);
        box.add(Box.createVerticalStrut(14));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(6));
        box.add(msgLabel);

        if (onRetry != null) {
            box.add(Box.createVerticalStrut(18));
            StyledButton retryBtn = new StyledButton("Try Again", StyledButton.ButtonStyle.SECONDARY);
            retryBtn.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 12));
            retryBtn.setMargin(new Insets(8, 16, 8, 16));
            retryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            retryBtn.addActionListener(e -> onRetry.run());
            box.add(retryBtn);
        }

        add(box);
        setPreferredSize(new Dimension(420, 280));
    }
}
