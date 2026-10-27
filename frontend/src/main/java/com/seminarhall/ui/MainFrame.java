package com.seminarhall.ui;

import com.seminarhall.model.User;
import com.seminarhall.service.SessionManager;
import com.seminarhall.ui.components.NavigationBar;
import com.seminarhall.ui.organizer.OrganizerDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private NavigationController navController;
    private NavigationBar navigationBar;

    private JPanel rootPanel;
    private JPanel containerPanel;
    private CardLayout cardLayout;

    // Screens
    private PreLoginPanel preLoginPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private StudentDashboardPanel studentDashboardPanel;
    private EventListPanel eventListPanel;
    private EventDetailsPanel eventDetailsPanel;
    private SeatSelectionPanel seatSelectionPanel;
    private BookingSummaryPanel bookingSummaryPanel;
    private PaymentPanel paymentPanel;
    private BookingConfirmationPanel bookingConfirmationPanel;
    private BookingHistoryPanel bookingHistoryPanel;
    private OrganizerDashboardPanel organizerDashboardPanel;

    public MainFrame() {
        super("SeminarX - College Event & Auditorium Booking System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        initUI();
        initGlobalShortcuts();
    }

    private void initUI() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeManager.BG_PRIMARY);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setOpaque(false);

        navController = new NavigationController(this, cardLayout, containerPanel);
        navigationBar = new NavigationBar(navController);

        // Instantiate screens
        preLoginPanel = new PreLoginPanel(navController);
        loginPanel = new LoginPanel(navController);
        registerPanel = new RegisterPanel(navController);
        studentDashboardPanel = new StudentDashboardPanel(navController);
        eventListPanel = new EventListPanel(navController);
        eventDetailsPanel = new EventDetailsPanel(navController);
        seatSelectionPanel = new SeatSelectionPanel(navController);
        bookingSummaryPanel = new BookingSummaryPanel(navController);
        paymentPanel = new PaymentPanel(navController);
        bookingConfirmationPanel = new BookingConfirmationPanel(navController);
        bookingHistoryPanel = new BookingHistoryPanel(navController);
        organizerDashboardPanel = new OrganizerDashboardPanel(navController);

        // Add to card layout
        containerPanel.add(preLoginPanel, NavigationController.SCREEN_PRE_LOGIN);
        containerPanel.add(loginPanel, NavigationController.SCREEN_LOGIN);
        containerPanel.add(registerPanel, NavigationController.SCREEN_REGISTER);
        containerPanel.add(studentDashboardPanel, NavigationController.SCREEN_STUDENT_DASHBOARD);
        containerPanel.add(eventListPanel, NavigationController.SCREEN_EVENT_LIST);
        containerPanel.add(eventDetailsPanel, NavigationController.SCREEN_EVENT_DETAILS);
        containerPanel.add(seatSelectionPanel, NavigationController.SCREEN_SEAT_SELECTION);
        containerPanel.add(bookingSummaryPanel, NavigationController.SCREEN_BOOKING_SUMMARY);
        containerPanel.add(paymentPanel, NavigationController.SCREEN_PAYMENT);
        containerPanel.add(bookingConfirmationPanel, NavigationController.SCREEN_BOOKING_CONFIRMATION);
        containerPanel.add(bookingHistoryPanel, NavigationController.SCREEN_BOOKING_HISTORY);
        containerPanel.add(organizerDashboardPanel, NavigationController.SCREEN_ORGANIZER_DASHBOARD);

        rootPanel.add(navigationBar, BorderLayout.NORTH);
        rootPanel.add(containerPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Start with Pre-Login Landing Screen
        navController.navigateTo(NavigationController.SCREEN_PRE_LOGIN);
    }

    private void initGlobalShortcuts() {
        // Alt + Left or Escape navigates to previous screen
        KeyStroke altLeft = KeyStroke.getKeyStroke("alt LEFT");
        rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altLeft, "goBack");
        rootPanel.getActionMap().put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                navController.goBack();
            }
        });
    }

    public void updateNavigationState(String currentScreen) {
        // Hide global top bar on Pre-Login, Login, Register, or Organizer dashboard
        boolean showTopBar = !NavigationController.SCREEN_PRE_LOGIN.equals(currentScreen)
                && !NavigationController.SCREEN_LOGIN.equals(currentScreen)
                && !NavigationController.SCREEN_REGISTER.equals(currentScreen)
                && !NavigationController.SCREEN_ORGANIZER_DASHBOARD.equals(currentScreen);

        navigationBar.setVisible(showTopBar);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        navigationBar.updateUserInfo(currentUser);

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    public NavigationController getNavController() {
        return navController;
    }

    public EventDetailsPanel getEventDetailsPanel() {
        return eventDetailsPanel;
    }

    public SeatSelectionPanel getSeatSelectionPanel() {
        return seatSelectionPanel;
    }

    public BookingSummaryPanel getBookingSummaryPanel() {
        return bookingSummaryPanel;
    }

    public PaymentPanel getPaymentPanel() {
        return paymentPanel;
    }

    public BookingConfirmationPanel getBookingConfirmationPanel() {
        return bookingConfirmationPanel;
    }
}
