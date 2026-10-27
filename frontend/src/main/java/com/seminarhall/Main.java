package com.seminarhall;

import com.bookmyshow.dao.jdbc.Database;
import com.seminarhall.adapter.DatabaseServiceAdapter;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.MainFrame;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize Look and Feel
        ThemeManager.initializeTheme();

        // Use the real backend + database unless demo mode is requested
        // (-Dseminarhall.mode=mock or SEMINARHALL_MODE=mock)
        String backendError = null;
        String mode = System.getProperty("seminarhall.mode", System.getenv("SEMINARHALL_MODE"));
        if ("mock".equalsIgnoreCase(mode)) {
            ServiceFactory.initMockServices();
        } else {
            try {
                DatabaseServiceAdapter.activateBackend(Database.fromEnvironment());
            } catch (RuntimeException e) {
                backendError = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                System.err.println("Database unavailable, starting with demo data instead: " + backendError);
                ServiceFactory.initMockServices();
            }
        }

        // Launch UI on Swing Event Dispatch Thread (EDT)
        String startupWarning = backendError;
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            if (startupWarning != null) {
                JOptionPane.showMessageDialog(frame,
                        "Could not open the database, so the app is running with demo data.\n"
                                + "Bookings made now will not be saved.\n\nReason: " + startupWarning,
                        "Running in demo mode", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
