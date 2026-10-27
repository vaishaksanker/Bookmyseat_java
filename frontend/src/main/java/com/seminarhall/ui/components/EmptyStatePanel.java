package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class EmptyStatePanel extends JPanel {
    public EmptyStatePanel(String iconText, String title, String subtitle, String actionButtonText, Runnable onAction) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(ThemeManager.TEXT_MUTED);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.FONT_HEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("<html><center>" + subtitle + "</center></html>", SwingConstants.CENTER);
        subLabel.setFont(ThemeManager.FONT_BODY);
        subLabel.setForeground(ThemeManager.TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(iconLabel);
        box.add(Box.createVerticalStrut(12));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(subLabel);

        if (actionButtonText != null && onAction != null) {
            box.add(Box.createVerticalStrut(18));
            StyledButton actionBtn = new StyledButton(actionButtonText, StyledButton.ButtonStyle.PRIMARY);
            actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionBtn.addActionListener(e -> onAction.run());
            box.add(actionBtn);
        }

        add(box);
    }
}
