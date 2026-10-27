package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NavigationController {
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

    private final MainFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    // Navigation history stack
    private final java.util.Stack<String> historyStack = new java.util.Stack<>();
    private String currentScreen = SCREEN_PRE_LOGIN;

    // Transient booking flow state
    private Event currentSelectedEvent;
    private List<Seat> currentSelectedSeats;
    private String currentCreatedBookingId;

    public NavigationController(MainFrame mainFrame, CardLayout cardLayout, JPanel containerPanel) {
        this.mainFrame = mainFrame;
        this.cardLayout = cardLayout;
        this.containerPanel = containerPanel;
    }

    public void navigateTo(String screenName) {
        navigateTo(screenName, true);
    }

    public void navigateTo(String screenName, boolean recordHistory) {
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
            navigateTo(SCREEN_PRE_LOGIN, false);
        }
    }

    public void showEventDetails(Event event) {
        this.currentSelectedEvent = event;
        mainFrame.getEventDetailsPanel().loadEvent(event);
        navigateTo(SCREEN_EVENT_DETAILS);
    }

    public void startSeatSelection(Event event) {
        this.currentSelectedEvent = event;
        this.currentSelectedSeats = null;
        mainFrame.getSeatSelectionPanel().loadEvent(event);
        navigateTo(SCREEN_SEAT_SELECTION);
    }

    public void proceedToBookingSummary(Event event, List<Seat> selectedSeats) {
        this.currentSelectedEvent = event;
        this.currentSelectedSeats = selectedSeats;
        mainFrame.getBookingSummaryPanel().loadSummary(event, selectedSeats);
        navigateTo(SCREEN_BOOKING_SUMMARY);
    }

    public void proceedToPayment(Event event, List<Seat> selectedSeats) {
        this.currentSelectedEvent = event;
        this.currentSelectedSeats = selectedSeats;
        mainFrame.getPaymentPanel().preparePayment(event, selectedSeats);
        navigateTo(SCREEN_PAYMENT);
    }

    public void showBookingConfirmation(String bookingId) {
        this.currentCreatedBookingId = bookingId;
        mainFrame.getBookingConfirmationPanel().loadBookingConfirmation(bookingId);
        navigateTo(SCREEN_BOOKING_CONFIRMATION);
    }

    public void logout() {
        SessionManager.getInstance().logout();
        this.currentSelectedEvent = null;
        this.currentSelectedSeats = null;
        this.currentCreatedBookingId = null;
        navigateTo(SCREEN_LOGIN);
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
