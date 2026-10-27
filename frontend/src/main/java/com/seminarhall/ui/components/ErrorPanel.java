package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class ErrorPanel extends JPanel {
    public ErrorPanel(String errorMessage, Runnable onRetry) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("⚠️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Unable to Load", SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.FONT_HEADER);
        titleLabel.setForeground(ThemeManager.ACCENT_RED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("<html><center>" + errorMessage + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(ThemeManager.FONT_BODY);
        msgLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(iconLabel);
        box.add(Box.createVerticalStrut(10));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(msgLabel);

        if (onRetry != null) {
            box.add(Box.createVerticalStrut(16));
            StyledButton retryBtn = new StyledButton("Try Again", StyledButton.ButtonStyle.SECONDARY);
            retryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            retryBtn.addActionListener(e -> onRetry.run());
            box.add(retryBtn);
        }

        add(box);
    }
}
