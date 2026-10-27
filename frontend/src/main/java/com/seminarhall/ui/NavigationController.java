package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * Switches screens, keeps the Back history and guards the pages that need an account.
 *
 * Like the web app, browsing (Home, event list, event details) is open to guests. Booking,
 * My Bookings and the Faculty Console ask the user to sign in first and then continue to
 * where they were going. The Faculty Console is for organizer accounts only.
 */
public class NavigationController {
    /** @deprecated the app starts on Home now; kept so old references still compile. Shows Home. */
    @Deprecated
    public static final String SCREEN_PRE_LOGIN = "SCREEN_PRE_LOGIN";
    public static final String SCREEN_LOGIN = "SCREEN_LOGIN";
    public static final String SCREEN_REGISTER = "SCREEN_REGISTER";
    public static final String SCREEN_STUDENT_DASHBOARD = "SCREEN_STUDENT_DASHBOARD";
    public static final String SCREEN_EVENT_LIST = "SCREEN_EVENT_LIST";
    public static final String SCREEN_EVENT_DETAILS = "SCREEN_EVENT_DETAILS";
    public static final String SCREEN_SEAT_SELECTION = "SCREEN_SEAT_SELECTION";
    public static final String SCREEN_BOOKING_SUMMARY = "SCREEN_BOOKING_SUMMARY";
    public static final String SCREEN_PAYMENT = "SCREEN_PAYMENT";
    public static final String SCREEN_BOOKING_CONFIRMATION = "SCREEN_BOOKING_CONFIRMATION";
    public static final String SCREEN_BOOKING_HISTORY = "SCREEN_BOOKING_HISTORY";
    public static final String SCREEN_ORGANIZER_DASHBOARD = "SCREEN_ORGANIZER_DASHBOARD";

    /** Screens a guest cannot open: they are sent to Login first. */
    private static final Set<String> SIGN_IN_REQUIRED = Set.of(
            SCREEN_SEAT_SELECTION, SCREEN_BOOKING_SUMMARY, SCREEN_PAYMENT,
            SCREEN_BOOKING_CONFIRMATION, SCREEN_BOOKING_HISTORY, SCREEN_ORGANIZER_DASHBOARD);

    private final MainFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    // Navigation history stack
    private final java.util.Stack<String> historyStack = new java.util.Stack<>();
    private String currentScreen;

    // Where a guest wanted to go before being sent to Login; run after a successful sign-in
    private Runnable afterSignIn;

    // Transient booking flow state
    private Event currentSelectedEvent;
    private List<Seat> currentSelectedSeats;
    private String currentCreatedBookingId;
    private String pendingSearchQuery;

    public NavigationController(MainFrame mainFrame, CardLayout cardLayout, JPanel containerPanel) {
        this.mainFrame = mainFrame;
        this.cardLayout = cardLayout;
        this.containerPanel = containerPanel;
    }

    public void navigateTo(String screenName) {
        navigateTo(screenName, true);
    }

    public void navigateTo(String screenName, boolean recordHistory) {
        if (SCREEN_PRE_LOGIN.equals(screenName)) {
            screenName = SCREEN_STUDENT_DASHBOARD;
        }
        if (SIGN_IN_REQUIRED.contains(screenName) && !SessionManager.getInstance().isLoggedIn()) {
            String target = screenName;
            requireSignIn(() -> navigateTo(target, true));
            return;
        }
        if (SCREEN_ORGANIZER_DASHBOARD.equals(screenName) && !SessionManager.getInstance().isOrganizer()) {
            mainFrame.showToast("The Faculty Console is for organizer accounts. "
                    + "Sign out and sign in with an organizer account to manage events.");
            return;
        }
        if (!isAuthScreen(screenName)) {
            afterSignIn = null; // the user went somewhere else instead of signing in
        }

        if (recordHistory && currentScreen != null && !currentScreen.equals(screenName)) {
            historyStack.push(currentScreen);
        }
        currentScreen = screenName;
        cardLayout.show(containerPanel, screenName);

        // Notify specific screens to refresh/reload if necessary
        Component comp = getComponentByName(screenName);
        if (comp instanceof RefreshableScreen) {
            ((RefreshableScreen) comp).onScreenDisplayed();
        }

        mainFrame.updateNavigationState(screenName);
    }

    public void goBack() {
        if (!historyStack.isEmpty()) {
            String prevScreen = historyStack.pop();
            navigateTo(prevScreen, false);
        } else {
            navigateTo(SCREEN_STUDENT_DASHBOARD, false);
        }
    }

    public void showEventDetails(Event event) {
        this.currentSelectedEvent = event;
        mainFrame.getEventDetailsPanel().loadEvent(event);
        navigateTo(SCREEN_EVENT_DETAILS);
    }

    public void startSeatSelection(Event event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            requireSignIn(() -> startSeatSelection(event));
            return;
        }
        this.currentSelectedEvent = event;
        this.currentSelectedSeats = null;
        mainFrame.getSeatSelectionPanel().loadEvent(event);
        navigateTo(SCREEN_SEAT_SELECTION);
    }

    public void proceedToBookingSummary(Event event, List<Seat> selectedSeats) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            requireSignIn(() -> proceedToBookingSummary(event, selectedSeats));
            return;
        }
        this.currentSelectedEvent = event;
        this.currentSelectedSeats = selectedSeats;
        mainFrame.getBookingSummaryPanel().loadSummary(event, selectedSeats);
        navigateTo(SCREEN_BOOKING_SUMMARY);
    }

    public void proceedToPayment(Event event, List<Seat> selectedSeats) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            requireSignIn(() -> proceedToPayment(event, selectedSeats));
            return;
        }
        this.currentSelectedEvent = event;
        this.currentSelectedSeats = selectedSeats;
        mainFrame.getPaymentPanel().preparePayment(event, selectedSeats);
        navigateTo(SCREEN_PAYMENT);
    }

    public void showBookingConfirmation(String bookingId) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            requireSignIn(() -> showBookingConfirmation(bookingId));
            return;
        }
        this.currentCreatedBookingId = bookingId;
        mainFrame.getBookingConfirmationPanel().loadBookingConfirmation(bookingId);
        navigateTo(SCREEN_BOOKING_CONFIRMATION);
    }

    /** Header search: show the event list filtered by this text. */
    public void showSearchResults(String query) {
        this.pendingSearchQuery = query;
        navigateTo(SCREEN_EVENT_LIST);
    }

    /** The text typed into the header search, returned once (null if there is none). */
    public String consumeSearchQuery() {
        String query = pendingSearchQuery;
        pendingSearchQuery = null;
        return query;
    }

    /**
     * Called by the Login and Register screens once the user is signed in: continues to the page
     * the user wanted before being asked to sign in, or to their start page.
     */
    public void onSignedIn(User user) {
        SessionManager.getInstance().setCurrentUser(user);
        // Back should not return to the sign-in forms
        while (!historyStack.isEmpty() && isAuthScreen(historyStack.peek())) {
            historyStack.pop();
        }
        if (isAuthScreen(currentScreen)) {
            currentScreen = null;
        }

        Runnable next = afterSignIn;
        afterSignIn = null;
        if (next != null) {
            next.run();
        } else if (user != null && user.getRole() == UserRole.ORGANIZER) {
            navigateTo(SCREEN_ORGANIZER_DASHBOARD);
        } else {
            navigateTo(SCREEN_STUDENT_DASHBOARD);
        }
        if (currentScreen == null) { // nothing was shown (e.g. a student asked for the Faculty Console)
            navigateTo(SCREEN_STUDENT_DASHBOARD);
        }
    }

    public boolean isLoggedIn() {
        return SessionManager.getInstance().isLoggedIn();
    }

    public void logout() {
        SessionManager.getInstance().logout();
        this.currentSelectedEvent = null;
        this.currentSelectedSeats = null;
        this.currentCreatedBookingId = null;
        this.afterSignIn = null;
        historyStack.clear();
        currentScreen = null;
        navigateTo(SCREEN_STUDENT_DASHBOARD, false);
    }

    public String getCurrentScreen() {
        return currentScreen;
    }

    public Event getCurrentSelectedEvent() {
        return currentSelectedEvent;
    }

    public List<Seat> getCurrentSelectedSeats() {
        return currentSelectedSeats;
    }

    public String getCurrentCreatedBookingId() {
        return currentCreatedBookingId;
    }

    private void requireSignIn(Runnable continuation) {
        if (!isAuthScreen(currentScreen)) {
            navigateTo(SCREEN_LOGIN);
        }
        afterSignIn = continuation;
    }

    private static boolean isAuthScreen(String screen) {
        return SCREEN_LOGIN.equals(screen) || SCREEN_REGISTER.equals(screen);
    }

    private Component getComponentByName(String name) {
        for (Component comp : containerPanel.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }

    public interface RefreshableScreen {
        void onScreenDisplayed();
    }
}
