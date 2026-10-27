package com.seminarhall.ui;

import com.seminarhall.model.User;
import com.seminarhall.service.SessionManager;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.NavigationBar;
import com.seminarhall.ui.organizer.OrganizerDashboardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The app window: the web app's sticky header on top of every screen, the screens in a CardLayout below.
 * Starts on Home as a guest, like https://seminarx-booking.vercel.app.
 */
public class MainFrame extends JFrame {
    private NavigationController navController;
    private NavigationBar navigationBar;

    private JPanel rootPanel;
    private JPanel containerPanel;
    private CardLayout cardLayout;

    // Screens
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

    // Short notice shown under the header (e.g. "Faculty Console is for organizers")
    private Toast toast;
    private final Timer toastTimer = new Timer(4500, e -> hideToast());

    public MainFrame() {
        super("SeminarX - College Event & Auditorium Booking System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1440, screen.width), Math.min(900, screen.height));
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        toastTimer.setRepeats(false);

        initUI();
        initGlobalShortcuts();
    }

    private void initUI() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeManager.BG_PAGE);
        rootPanel.setFocusable(true);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setOpaque(false);

        navController = new NavigationController(this, cardLayout, containerPanel);
        navigationBar = new NavigationBar(navController);

        // Instantiate screens
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
        containerPanel.add(studentDashboardPanel, NavigationController.SCREEN_STUDENT_DASHBOARD);
        containerPanel.add(loginPanel, NavigationController.SCREEN_LOGIN);
        containerPanel.add(registerPanel, NavigationController.SCREEN_REGISTER);
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

        // Keep the toast centred under the header when the window is resized
        getLayeredPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionToast();
            }
        });
        // Do not start with the caret in the header search box (it would draw its focus border)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                rootPanel.requestFocusInWindow();
            }
        });

        // Like the web app, open on Home; signing in is only needed to book or manage
        navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD);
    }

    private void initGlobalShortcuts() {
        // Alt + Left navigates to previous screen
        KeyStroke altLeft = KeyStroke.getKeyStroke("alt LEFT");
        rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altLeft, "goBack");
        rootPanel.getActionMap().put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                navController.goBack();
            }
        });
    }

    /** Called after every screen change: the header shows on every screen, like the web app. */
    public void updateNavigationState(String currentScreen) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        navigationBar.updateState(currentScreen, currentUser);

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    /** Shows a short message under the header for a few seconds. */
    public void showToast(String message) {
        hideToast();
        toast = new Toast(message);
        toast.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                hideToast();
            }
        });
        getLayeredPane().add(toast, JLayeredPane.POPUP_LAYER);
        positionToast();
        toastTimer.restart();
    }

    private void hideToast() {
        toastTimer.stop();
        if (toast != null) {
            JLayeredPane layers = getLayeredPane();
            Rectangle bounds = toast.getBounds();
            layers.remove(toast);
            layers.repaint(bounds);
            toast = null;
        }
    }

    private void positionToast() {
        if (toast == null) {
            return;
        }
        JLayeredPane layers = getLayeredPane();
        Dimension size = toast.getPreferredSize();
        Point headerBottom = SwingUtilities.convertPoint(navigationBar, 0, navigationBar.getHeight(), layers);
        toast.setBounds((layers.getWidth() - size.width) / 2, headerBottom.y + 16, size.width, size.height);
        toast.revalidate();
        toast.repaint();
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

    /** rounded-2xl card, bg #12141c with a brand-red border, shield icon + text-xs message. */
    private static final class Toast extends JPanel {
        Toast(String message) {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 15, 18)); // 3px at the bottom are the drop shadow
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel icon = Icons.label("shield", 16, ThemeManager.BRAND_RED);
            icon.setVerticalAlignment(SwingConstants.TOP);
            // Swing's CSS px are 4/3 screen pixels, so this wraps the text at about 520px
            JLabel text = new JLabel("<html><div style='width:"
                    + Math.min(390, message.length() * 5) + "px'>" + message + "</div></html>");
            text.setFont(ThemeManager.font(ThemeManager.Weight.SEMIBOLD, 12));
            text.setForeground(new Color(229, 231, 235));
            add(icon, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = ThemeManager.RADIUS_2XL * 2;
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, arc, arc);
            g2.setColor(ThemeManager.BG_SECONDARY);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 3, arc, arc);
            g2.setColor(new Color(255, 56, 46, 110));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, arc, arc);
            g2.dispose();
        }
    }
}
