package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel {
    public LoadingPanel(String message) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JProgressBar spinner = new JProgressBar();
        spinner.setIndeterminate(true);
        spinner.setForeground(ThemeManager.ACCENT_RED);
        spinner.setBackground(ThemeManager.BG_TERTIARY);
        spinner.setPreferredSize(new Dimension(180, 6));
        spinner.setMaximumSize(new Dimension(180, 6));
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel(message != null ? message : "Loading data...", SwingConstants.CENTER);
        msgLabel.setFont(ThemeManager.FONT_BODY);
        msgLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(spinner);
        box.add(Box.createVerticalStrut(14));
        box.add(msgLabel);

        add(box);
    }
}
