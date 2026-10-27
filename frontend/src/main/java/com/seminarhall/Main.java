package com.seminarhall;

import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.MainFrame;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize Look and Feel
        ThemeManager.initializeTheme();

        // Initialize default mock services for high-fidelity demonstration
        ServiceFactory.initMockServices();

        // Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
