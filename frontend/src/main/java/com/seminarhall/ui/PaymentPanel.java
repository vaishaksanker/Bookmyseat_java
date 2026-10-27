package com.seminarhall.ui;

import com.seminarhall.model.Booking;
import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.model.User;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.PaymentService;
import com.seminarhall.service.ServiceException;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.SessionManager;
import com.seminarhall.ui.components.StepIndicatorBar;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentPanel extends JPanel {
    private final NavigationController navController;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    private Event currentEvent;
    private List<Seat> currentSeats;

    private StepIndicatorBar stepBar;
    private JPanel upiOption;
    private JPanel cardOption;
    private JPanel cashOption;
    private String selectedMethod = "UPI";

    private JLabel eventNameLbl;
    private JLabel seatsListLbl;
    private JLabel amountLbl;
    private StyledButton payNowBtn;

    public PaymentPanel(NavigationController navController) {
        this.navController = navController;
        this.paymentService = ServiceFactory.getPaymentService();
        this.bookingService = ServiceFactory.getBookingService();

        setName(NavigationController.SCREEN_PAYMENT);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Step Indicator Top Bar (Step 3: Payment)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        stepBar = new StepIndicatorBar(3);
        topHeader.add(stepBar, BorderLayout.CENTER);
        add(topHeader, BorderLayout.NORTH);

        // Center Split matching Screen 7
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel mainSplit = new JPanel(new GridLayout(1, 2, 24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setPreferredSize(new Dimension(840, 480));

        // LEFT: Choose Payment Method Cards
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel chooseTitle = new JLabel("Choose Payment Method");
        chooseTitle.setFont(ThemeManager.FONT_HEADER);
        chooseTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        upiOption = createMethodCard("UPI", "GPay • PhonePe • Paytm", "📱", true);
        cardOption = createMethodCard("Credit / Debit Card", "Visa, Mastercard, RuPay", "💳", false);
        cashOption = createMethodCard("Cash", "Pay at venue desk", "💵", false);

        leftCol.add(chooseTitle);
        leftCol.add(Box.createVerticalStrut(14));
        leftCol.add(upiOption);
        leftCol.add(Box.createVerticalStrut(12));
        leftCol.add(cardOption);
        leftCol.add(Box.createVerticalStrut(12));
        leftCol.add(cashOption);

        // RIGHT: Payment Details Card
        JPanel rightCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rightCard.setOpaque(false);
        rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));
        rightCard.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel payDetailsTitle = new JLabel("Payment Details");
        payDetailsTitle.setFont(ThemeManager.FONT_HEADER);
        payDetailsTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        JPanel metaGrid = new JPanel(new GridLayout(3, 2, 8, 12));
        metaGrid.setOpaque(false);
        metaGrid.setMaximumSize(new Dimension(380, 100));

        eventNameLbl = new JLabel("AI in Modern World", SwingConstants.RIGHT);
        seatsListLbl = new JLabel("B5, B6, B7", SwingConstants.RIGHT);
        amountLbl = new JLabel("₹0", SwingConstants.RIGHT);

        for (JLabel l : new JLabel[]{eventNameLbl, seatsListLbl, amountLbl}) {
            l.setFont(ThemeManager.FONT_BODY_BOLD);
            l.setForeground(ThemeManager.TEXT_PRIMARY);
        }

        metaGrid.add(createLabel("Event:"));
        metaGrid.add(eventNameLbl);
        metaGrid.add(createLabel("Seats:"));
        metaGrid.add(seatsListLbl);
        metaGrid.add(createLabel("Amount:"));
        metaGrid.add(amountLbl);

        // Green Secure & Safe Payment badge
        JPanel secureBadge = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 185, 129, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(16, 185, 129, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        secureBadge.setOpaque(false);
        secureBadge.setMaximumSize(new Dimension(380, 52));
        secureBadge.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JPanel secTexts = new JPanel();
        secTexts.setOpaque(false);
        secTexts.setLayout(new BoxLayout(secTexts, BoxLayout.Y_AXIS));

        JLabel secTitle = new JLabel("Secure & Safe Payment");
        secTitle.setFont(ThemeManager.FONT_SMALL_BOLD);
        secTitle.setForeground(ThemeManager.STATUS_GREEN);

        JLabel secSub = new JLabel("Your payment information is secure and encrypted.");
        secSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        secSub.setForeground(ThemeManager.TEXT_MUTED);

        secTexts.add(secTitle);
        secTexts.add(secSub);

        secureBadge.add(lockIcon, BorderLayout.WEST);
        secureBadge.add(secTexts, BorderLayout.CENTER);

        payNowBtn = new StyledButton("Pay Now →", StyledButton.ButtonStyle.PRIMARY);
        payNowBtn.setMaximumSize(new Dimension(380, 44));
        payNowBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        payNowBtn.addActionListener(e -> executePayment());

        JLabel demoNotice = new JLabel("<html><center>Note: This is a demo payment system. No real money will be charged.</center></html>", SwingConstants.CENTER);
        demoNotice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        demoNotice.setForeground(ThemeManager.TEXT_MUTED);
        demoNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightCard.add(payDetailsTitle);
        rightCard.add(Box.createVerticalStrut(16));
        rightCard.add(metaGrid);
        rightCard.add(Box.createVerticalStrut(20));
        rightCard.add(secureBadge);
        rightCard.add(Box.createVerticalStrut(24));
        rightCard.add(payNowBtn);
        rightCard.add(Box.createVerticalStrut(8));
        rightCard.add(demoNotice);

        mainSplit.add(leftCol);
        mainSplit.add(rightCard);

        center.add(mainSplit);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createMethodCard(String title, String subtitle, String icon, boolean selected) {
        JPanel p = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isSel = title.startsWith(selectedMethod);
                g2.setColor(isSel ? ThemeManager.BG_CARD_HOVER : ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(isSel ? ThemeManager.ACCENT_RED : ThemeManager.BORDER_CARD);
                g2.setStroke(new BasicStroke(isSel ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(380, 70));
        p.setPreferredSize(new Dimension(380, 70));
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        tLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(ThemeManager.FONT_SMALL);
        subLbl.setForeground(ThemeManager.TEXT_MUTED);

        texts.add(tLbl);
        texts.add(Box.createVerticalStrut(2));
        texts.add(subLbl);

        p.add(iconLbl, BorderLayout.WEST);
        p.add(texts, BorderLayout.CENTER);

        p.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedMethod = title.startsWith("UPI") ? "UPI" : (title.contains("Card") ? "Card" : "Cash");
                upiOption.repaint();
                cardOption.repaint();
                cashOption.repaint();
            }
        });

        return p;
    }

    private JLabel createLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(ThemeManager.FONT_SMALL);
        l.setForeground(ThemeManager.TEXT_MUTED);
        return l;
    }

    public void preparePayment(Event event, List<Seat> selectedSeats) {
        this.currentEvent = event;
        this.currentSeats = selectedSeats;

        if (event != null && selectedSeats != null) {
            eventNameLbl.setText(event.getTitle());
            String seatNames = selectedSeats.stream().map(Seat::getSeatIdentifier).collect(Collectors.joining(", "));
            seatsListLbl.setText(seatNames);

            double total = selectedSeats.stream().mapToDouble(Seat::getPrice).sum();
            amountLbl.setText("₹" + String.format("%.0f", total));
        }
    }

    private void executePayment() {
        if (currentEvent == null || currentSeats == null) return;

        payNowBtn.setEnabled(false);
        payNowBtn.setText("Processing...");

        double total = currentSeats.stream().mapToDouble(Seat::getPrice).sum();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getId() : "usr_student_1";
        String payer = currentUser != null ? currentUser.getEmail() : "guest";
        List<String> seatIds = currentSeats.stream().map(Seat::getId).collect(Collectors.toList());

        SwingWorker<Booking, Void> worker = new SwingWorker<>() {
            @Override
            protected Booking doInBackground() throws Exception {
                PaymentService.PaymentResult res = paymentService.processPayment(selectedMethod, total, payer);
                if (!res.isSuccess()) {
                    // never reserve seats for a payment that did not go through
                    throw new ServiceException(res.getMessage());
                }
                return bookingService.createBooking(currentEvent.getId(), userId, seatIds, selectedMethod, res.getTransactionId());
            }

            @Override
            protected void done() {
                payNowBtn.setEnabled(true);
                payNowBtn.setText("Pay Now →");
                try {
                    Booking b = get();
                    navController.showBookingConfirmation(b.getId());
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(PaymentPanel.this, "Payment / Reservation error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
