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
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.BookingFlowParts;
import com.seminarhall.ui.components.BookingFlowParts.Card;
import com.seminarhall.ui.components.BookingFlowParts.FlowButton;
import com.seminarhall.ui.components.BookingFlowParts.Page;
import com.seminarhall.ui.components.BookingFlowParts.Row;
import com.seminarhall.ui.components.BookingFlowParts.Stack;
import com.seminarhall.ui.components.BookingFlowParts.Text;
import com.seminarhall.ui.components.BookingFlowParts.WrapText;
import com.seminarhall.ui.components.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The web app's PaymentPage: "Choose Payment Method" cards (UPI, card, cash at the venue desk),
 * the payment breakdown, the secure checkout note and "Pay & Generate Pass (₹total)".
 */
public class PaymentPanel extends JPanel {
    /** max-w-3xl px-8, grid-cols-[1.3fr_1fr] gap-6: the right column's card content width */
    private static final int RIGHT_COL_W = Math.round((768 - 64 - 24) / 2.3f);

    private final NavigationController navController;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    private Event currentEvent;
    private List<Seat> currentSeats;

    private Page page;
    private MethodCard upiOption;
    private MethodCard cardOption;
    private MethodCard cashOption;
    private final List<MethodCard> methodCards = new ArrayList<>();
    /** The value passed to PaymentService: "UPI", "Card" or "Cash". */
    private String selectedMethod = "UPI";

    private Text eventNameLbl;
    private WrapText seatsListLbl;
    private Text seatCountLbl;
    private Text amountLbl;
    private Card secureBadge;
    private FlowButton payNowBtn;

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
        Stack content = new Stack();
        content.row(BookingFlowParts.backLink("← Back to Order Summary", navController::goBack), 0, Stack.Align.LEFT);
        content.row(BookingFlowParts.pageTitle("Payment"), 8);

        Row columns = new Row(Row.VAlign.TOP);
        columns.grow(buildMethods(), 0, 1.3f);
        columns.grow(buildBreakdown(), 24, 1f);
        content.row(columns, 24);

        // max-w-3xl mx-auto px-8 py-10
        page = new Page(content, 768, 32, 40, 40);
        for (MethodCard card : methodCards) {
            // selected: shadow-[0_0_20px_rgba(255,56,46,0.35)]
            page.glow(card, card::isSelected, ThemeManager.RADIUS_2XL, 20, new Color(255, 56, 46, 89));
        }
        page.glow(secureBadge, () -> true, ThemeManager.RADIUS_2XL, 15, new Color(16, 185, 129, 38));
        add(page.scroller(), BorderLayout.CENTER);
    }

    private JComponent buildMethods() {
        upiOption = new MethodCard("UPI", Emoji.PHONE, "UPI Payment", "Google Pay, PhonePe, Paytm, BHIM");
        cardOption = new MethodCard("Card", Emoji.CARD, "Credit / Debit Card", "Visa, Mastercard, RuPay");
        cashOption = new MethodCard("Cash", Emoji.CASH, "Cash on Venue Desk", "Pay directly at auditorium entrance");

        Stack left = new Stack();
        left.row(heading("Choose Payment Method"), 0);
        left.row(upiOption, 16);
        left.row(cardOption, 12);
        left.row(cashOption, 12);
        return left;
    }

    private JComponent buildBreakdown() {
        // bg-[#10131d] rounded-2xl p-5 space-y-2 text-xs text-gray-300
        eventNameLbl = new Text("", Weight.BOLD, 12, ThemeManager.TEXT_PRIMARY, 16).maxWidth(140);
        int labelW = getFontMetrics(ThemeManager.font(Weight.REGULAR, 12)).stringWidth("Seats");
        seatsListLbl = new WrapText(Weight.BOLD, 12, ThemeManager.TEXT_PRIMARY, 16, RIGHT_COL_W - 42 - labelW - 12);
        seatCountLbl = new Text("0", Weight.BOLD, 12, ThemeManager.TEXT_PRIMARY, 16);
        amountLbl = new Text("₹0", Weight.BOLD, 14, ThemeManager.BRAND_RED, 20);

        Row total = new Row(Row.VAlign.TOP);
        total.grow(new Text("Payable Amount", Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 20), 0, 1);
        total.cell(amountLbl, 8);

        Stack rows = new Stack();
        rows.row(detailRow("Event", eventNameLbl), 0);
        rows.row(detailRow("Seats", seatsListLbl), 8);
        rows.row(detailRow("Total Seats", seatCountLbl), 8);
        rows.row(BookingFlowParts.rule(BookingFlowParts.CARD_BORDER), 8);
        rows.row(total, 8);
        Card breakdown = new Card(ThemeManager.RADIUS_2XL, 20);
        breakdown.add(rows, BorderLayout.CENTER);

        // bg-emerald-500/10 border-emerald-500/30 rounded-2xl p-3.5 shadow-[0_0_15px_rgba(16,185,129,0.15)]
        Card secure = new Card(ThemeManager.RADIUS_2XL, 14)
                .colors(new Color(16, 185, 129, 26), new Color(16, 185, 129, 77));
        Stack secureTexts = new Stack();
        secureTexts.row(new Text("Secure & Encrypted Checkout", Weight.BOLD, 12, ThemeManager.EMERALD_400, 16), 0);
        secureTexts.row(new Text("256-bit SSL verified campus transaction.", Weight.REGULAR, 10,
                BookingFlowParts.EMERALD_200, 15), 0);
        Row secureRow = new Row(Row.VAlign.CENTER);
        secureRow.cell(Icons.label("shield", 18, ThemeManager.EMERALD_400), 0);
        secureRow.grow(secureTexts, 12, 1);
        secure.add(secureRow, BorderLayout.CENTER);

        // w-full glow-primary py-4 rounded-2xl text-sm font-bold
        payNowBtn = new FlowButton(payLabel(0), 14, 16, 24, ThemeManager.RADIUS_2XL, 15);
        payNowBtn.addActionListener(e -> executePayment());

        Stack right = new Stack();
        right.row(heading("Payment Breakdown"), 0);
        right.row(breakdown, 16);
        right.row(secure, 16);
        right.row(payNowBtn, 20);
        secureBadge = secure;
        return right;
    }

    private static Text heading(String text) {
        return new Text(text, Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 20);
    }

    private static Row detailRow(String label, JComponent value) {
        Row row = new Row(Row.VAlign.TOP);
        row.grow(new Text(label, Weight.REGULAR, 12, ThemeManager.TEXT_GRAY_300, 16), 0, 1);
        row.cell(value, 12);
        return row;
    }

    private static String payLabel(double total) {
        return "Pay & Generate Pass (" + BookingFlowParts.rupees(total) + ")";
    }

    private double total() {
        return currentSeats == null ? 0 : currentSeats.stream().mapToDouble(Seat::getPrice).sum();
    }

    private void selectMethod(String method) {
        selectedMethod = method;
        for (MethodCard card : methodCards) card.refresh();
        page.repaint();
    }

    public void preparePayment(Event event, List<Seat> selectedSeats) {
        this.currentEvent = event;
        this.currentSeats = selectedSeats;

        if (event != null && selectedSeats != null) {
            eventNameLbl.setText(event.getTitle());
            String seatNames = selectedSeats.stream().map(Seat::getSeatIdentifier).collect(Collectors.joining(", "));
            seatsListLbl.setText(seatNames);
            seatCountLbl.setText(String.valueOf(selectedSeats.size()));
            amountLbl.setText(BookingFlowParts.rupees(total()));
            payNowBtn.setText(payLabel(total()));
            page.revalidate();
            page.repaint();
            page.scrollToTop();
        }
    }

    private void executePayment() {
        if (currentEvent == null || currentSeats == null) return;

        payNowBtn.setEnabled(false);
        payNowBtn.setText("Processing...");

        double total = total();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getId() : "usr_student_1";
        String payer = currentUser != null ? currentUser.getEmail() : "guest";
        List<String> seatIds = currentSeats.stream().map(Seat::getId).collect(Collectors.toList());
        String method = selectedMethod;

        SwingWorker<Booking, Void> worker = new SwingWorker<>() {
            @Override
            protected Booking doInBackground() throws Exception {
                PaymentService.PaymentResult res = paymentService.processPayment(method, total, payer);
                if (!res.isSuccess()) {
                    // never reserve seats for a payment that did not go through
                    throw new ServiceException(res.getMessage());
                }
                return bookingService.createBooking(currentEvent.getId(), userId, seatIds, method, res.getTransactionId());
            }

            @Override
            protected void done() {
                payNowBtn.setEnabled(true);
                payNowBtn.setText(payLabel(total));
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

    // ------------------------------------------------------------------ method cards

    /**
     * "p-4 rounded-2xl border flex items-center justify-between": emoji, title + description, radio.
     * Selected: bg-[#ff382e]/10 border-[#ff382e] with a red glow; otherwise bg-[#10131d] hover:bg-[#141724].
     */
    private class MethodCard extends Card {
        private final String method;
        private final Radio radio = new Radio();
        private boolean hover;

        MethodCard(String method, Emoji emoji, String title, String desc) {
            super(ThemeManager.RADIUS_2XL, 16);
            this.method = method;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            Stack texts = new Stack();
            texts.row(new Text(title, Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 20), 0);
            texts.row(new Text(desc, Weight.REGULAR, 12, ThemeManager.TEXT_SECONDARY, 16), 0);

            Row row = new Row(Row.VAlign.CENTER);
            row.cell(new JLabel(new EmojiIcon(emoji)), 0);
            row.grow(texts, 14, 1);
            row.cell(radio, 8);
            add(row, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectMethod(method);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    refresh();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), MethodCard.this);
                    if (!new Rectangle(getSize()).contains(p)) {
                        hover = false;
                        refresh();
                    }
                }
            });
            methodCards.add(this);
            refresh();
        }

        boolean isSelected() {
            return method.equals(selectedMethod);
        }

        void refresh() {
            if (isSelected()) {
                colors(new Color(255, 56, 46, 26), ThemeManager.BRAND_RED);
            } else {
                colors(hover ? new Color(20, 23, 36) : BookingFlowParts.CARD_BG, BookingFlowParts.CARD_BORDER);
            }
            radio.repaint();
        }

        /** w-5 h-5 rounded-full border-2 border-[#ff382e], with a w-2.5 h-2.5 dot when selected. */
        private class Radio extends JComponent {
            Radio() {
                setPreferredSize(new Dimension(20, 20));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(ThemeManager.BRAND_RED);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Float(1, 1, 18, 18));
                if (isSelected()) {
                    g2.fill(new Ellipse2D.Float(5, 5, 10, 10));
                }
                g2.dispose();
            }
        }
    }

    // ------------------------------------------------------------------ emoji

    private enum Emoji { PHONE, CARD, CASH }

    /**
     * The web shows 📱 💳 💵 at text-2xl. Java2D cannot draw color emoji, so they are drawn here
     * as small flat pictures of the same objects.
     */
    private static class EmojiIcon implements Icon {
        private final Emoji emoji;

        EmojiIcon(Emoji emoji) {
            this.emoji = emoji;
        }

        @Override
        public int getIconWidth() {
            return 30;
        }

        @Override
        public int getIconHeight() {
            return 32;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            switch (emoji) {
                case PHONE:
                    g2.setPaint(new GradientPaint(0, 4, new Color(88, 96, 112), 0, 28, new Color(45, 50, 62)));
                    g2.fill(new RoundRectangle2D.Float(8, 4, 15, 25, 5, 5));
                    g2.setPaint(new GradientPaint(0, 7, new Color(96, 190, 255), 0, 25, new Color(52, 110, 235)));
                    g2.fill(new RoundRectangle2D.Float(9.5f, 7, 12, 18.5f, 2, 2));
                    g2.setColor(new Color(20, 22, 28));
                    g2.fill(new RoundRectangle2D.Float(13.5f, 5.2f, 4, 1, 1, 1));
                    g2.setColor(new Color(200, 205, 215, 160));
                    g2.fill(new Ellipse2D.Float(14.5f, 26, 2, 2));
                    break;
                case CARD:
                    g2.setPaint(new GradientPaint(2, 9, new Color(110, 170, 255), 28, 25, new Color(40, 95, 220)));
                    g2.fill(new RoundRectangle2D.Float(2, 8, 26, 17, 5, 5));
                    g2.setColor(new Color(251, 191, 36));
                    g2.fill(new RoundRectangle2D.Float(5.5f, 12, 6, 4.5f, 2, 2));
                    g2.setColor(new Color(255, 255, 255, 170));
                    g2.fill(new RoundRectangle2D.Float(5.5f, 19.5f, 9, 1.6f, 1, 1));
                    g2.fill(new RoundRectangle2D.Float(16, 19.5f, 6, 1.6f, 1, 1));
                    break;
                case CASH:
                default:
                    g2.setPaint(new GradientPaint(2, 9, new Color(110, 205, 120), 28, 24, new Color(46, 150, 72)));
                    g2.fill(new RoundRectangle2D.Float(2, 9, 26, 15, 3, 3));
                    g2.setColor(new Color(220, 252, 231, 150));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(4, 11, 22, 11, 2, 2));
                    g2.setColor(new Color(30, 110, 55));
                    g2.fill(new Ellipse2D.Float(11.5f, 12.5f, 7, 7));
                    g2.setColor(new Color(160, 230, 170));
                    g2.fill(new Ellipse2D.Float(13, 14, 4, 4));
                    break;
            }
            g2.dispose();
        }
    }
}
