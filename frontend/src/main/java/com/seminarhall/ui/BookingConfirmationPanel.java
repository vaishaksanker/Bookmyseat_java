package com.seminarhall.ui;

import com.seminarhall.model.Booking;
import com.seminarhall.model.Ticket;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.TicketService;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingConfirmationPanel extends JPanel {
    private final NavigationController navController;
    private final BookingService bookingService;
    private final TicketService ticketService;

    private Booking currentBooking;
    private JLabel eventTitleLbl;
    private JLabel eventDateLbl;
    private JLabel seatsLbl;
    private JLabel bookingIdLbl;
    private JLabel amountLbl;

    public BookingConfirmationPanel(NavigationController navController) {
        this.navController = navController;
        this.bookingService = ServiceFactory.getBookingService();
        this.ticketService = ServiceFactory.getTicketService();

        setName(NavigationController.SCREEN_BOOKING_CONFIRMATION);
        setOpaque(false);
        setLayout(new GridBagLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        // Top Success Ring & Title (Screen 8)
        JPanel successCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 185, 129, 30));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeManager.STATUS_GREEN);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("✓", (getWidth() - fm.stringWidth("✓")) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        successCircle.setPreferredSize(new Dimension(56, 56));
        successCircle.setMaximumSize(new Dimension(56, 56));
        successCircle.setOpaque(false);
        successCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel successTitle = new JLabel("Booking Successful!", SwingConstants.CENTER);
        successTitle.setFont(ThemeManager.FONT_HEADER_XL);
        successTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        successTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel successSub = new JLabel("Your seats have been booked successfully.", SwingConstants.CENTER);
        successSub.setFont(ThemeManager.FONT_BODY);
        successSub.setForeground(ThemeManager.TEXT_MUTED);
        successSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerContainer.add(successCircle);
        centerContainer.add(Box.createVerticalStrut(10));
        centerContainer.add(successTitle);
        centerContainer.add(Box.createVerticalStrut(4));
        centerContainer.add(successSub);
        centerContainer.add(Box.createVerticalStrut(24));

        // Digital Ticket Card (Screen 8 perforated white ticket)
        JPanel ticketCard = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // White paper background matching screenshot
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                // Perforated notches on top and bottom
                g2.setColor(ThemeManager.BG_PRIMARY);
                g2.fillOval(w - 140, -10, 20, 20);
                g2.fillOval(w - 140, h - 10, 20, 20);

                // Dotted vertical perforation line
                Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0);
                g2.setStroke(dashed);
                g2.setColor(new Color(200, 205, 215));
                g2.drawLine(w - 130, 14, w - 130, h - 14);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        ticketCard.setOpaque(false);
        ticketCard.setPreferredSize(new Dimension(560, 160));
        ticketCard.setMaximumSize(new Dimension(580, 170));
        ticketCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Left photo thumbnail
        JPanel thumb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 20, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString("🎙", (getWidth() - 12) / 2, (getHeight() + 10) / 2);
                g2.dispose();
            }
        };
        thumb.setPreferredSize(new Dimension(64, 110));
        thumb.setOpaque(false);

        // Middle Ticket details (Dark text on white)
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        eventTitleLbl = new JLabel("AI in Modern World");
        eventTitleLbl.setFont(ThemeManager.FONT_HEADER);
        eventTitleLbl.setForeground(new Color(20, 25, 35));

        eventDateLbl = new JLabel("📅 Dec 10, 2024 • 10:00 AM  |  📍 Seminar Hall A");
        eventDateLbl.setFont(ThemeManager.FONT_SMALL);
        eventDateLbl.setForeground(new Color(90, 95, 105));

        seatsLbl = new JLabel("Seats: B5, B6, B7");
        seatsLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        seatsLbl.setForeground(new Color(30, 35, 45));

        bookingIdLbl = new JLabel("Booking ID: BK001234");
        bookingIdLbl.setFont(ThemeManager.FONT_SMALL);
        bookingIdLbl.setForeground(new Color(110, 115, 125));

        amountLbl = new JLabel("Amount Paid: ₹0");
        amountLbl.setFont(ThemeManager.FONT_SMALL_BOLD);
        amountLbl.setForeground(ThemeManager.STATUS_GREEN.darker());

        details.add(eventTitleLbl);
        details.add(Box.createVerticalStrut(4));
        details.add(eventDateLbl);
        details.add(Box.createVerticalStrut(6));
        details.add(seatsLbl);
        details.add(Box.createVerticalStrut(2));
        details.add(bookingIdLbl);
        details.add(Box.createVerticalStrut(2));
        details.add(amountLbl);

        // Right QR Code Stub
        JPanel qrBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int size = 70;
                int ox = (getWidth() - size) / 2;
                int oy = (getHeight() - size) / 2;

                g2.setColor(Color.WHITE);
                g2.fillRect(ox, oy, size, size);
                g2.setColor(Color.BLACK);
                int b = size / 7;
                for (int r = 0; r < 7; r++) {
                    for (int c = 0; c < 7; c++) {
                        if ((r < 2 && c < 2) || (r < 2 && c >= 5) || (r >= 5 && c < 2) || (r + c * 2) % 3 == 0) {
                            g2.fillRect(ox + c * b, oy + r * b, b, b);
                        }
                    }
                }
                g2.dispose();
            }
        };
        qrBox.setPreferredSize(new Dimension(100, 100));
        qrBox.setOpaque(false);

        ticketCard.add(thumb, BorderLayout.WEST);
        ticketCard.add(details, BorderLayout.CENTER);
        ticketCard.add(qrBox, BorderLayout.EAST);

        centerContainer.add(ticketCard);
        centerContainer.add(Box.createVerticalStrut(24));

        // Bottom Action Buttons matching Screen 8
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btns.setOpaque(false);

        StyledButton downloadBtn = new StyledButton("Download Ticket", StyledButton.ButtonStyle.PRIMARY);
        downloadBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "E-Pass downloaded to documents folder successfully!", "Downloaded", JOptionPane.INFORMATION_MESSAGE);
        });

        StyledButton myBookingsBtn = new StyledButton("My Bookings", StyledButton.ButtonStyle.SECONDARY);
        myBookingsBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_BOOKING_HISTORY));

        StyledButton homeBtn = new StyledButton("Back to Home", StyledButton.ButtonStyle.SECONDARY);
        homeBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD));

        btns.add(downloadBtn);
        btns.add(myBookingsBtn);
        btns.add(homeBtn);

        centerContainer.add(btns);
        add(centerContainer);
    }

    public void loadBookingConfirmation(String bookingId) {
        SwingWorker<Booking, Void> worker = new SwingWorker<>() {
            @Override
            protected Booking doInBackground() throws Exception {
                return bookingService.getBookingById(bookingId);
            }

            @Override
            protected void done() {
                try {
                    currentBooking = get();
                    eventTitleLbl.setText(currentBooking.getEventTitle());
                    eventDateLbl.setText("📅 " + currentBooking.getEventDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")) + "  |  📍 " + currentBooking.getSeminarHall());
                    seatsLbl.setText("Seats: " + currentBooking.getFormattedSeats());
                    bookingIdLbl.setText("Booking ID: " + currentBooking.getId());
                    amountLbl.setText("Amount Paid: ₹" + String.format("%.0f", currentBooking.getTotalAmount()));
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
