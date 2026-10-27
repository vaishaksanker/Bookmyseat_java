package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.ui.components.StepIndicatorBar;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingSummaryPanel extends JPanel {
    private final NavigationController navController;
    private Event currentEvent;
    private List<Seat> currentSeats;

    private StepIndicatorBar stepBar;
    private JLabel eventTitleLbl;
    private JLabel eventCategoryLbl;
    private JLabel eventTimeLbl;
    private JLabel venueLbl;
    private JPanel selectedSeatsChips;
    private JLabel pricePerSeatLbl;
    private JLabel seatCountLbl;
    private JLabel subtotalLbl;
    private JLabel grandTotalLbl;

    public BookingSummaryPanel(NavigationController navController) {
        this.navController = navController;

        setName(NavigationController.SCREEN_BOOKING_SUMMARY);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Step Indicator Top Bar (Step 2: Booking Summary)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        stepBar = new StepIndicatorBar(2);
        topHeader.add(stepBar, BorderLayout.CENTER);
        add(topHeader, BorderLayout.NORTH);

        // Center card container matching Screen 6
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = new JPanel() {
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
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(560, 480));
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Top event info with thumbnail
        JPanel topRow = new JPanel(new BorderLayout(14, 0));
        topRow.setOpaque(false);

        JPanel thumb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 20, 20), getWidth(), getHeight(), new Color(20, 22, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 24));
                g2.drawString("🎙", (getWidth() - 20) / 2, (getHeight() + 16) / 2);
                g2.dispose();
            }
        };
        thumb.setPreferredSize(new Dimension(80, 80));
        thumb.setOpaque(false);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        eventTitleLbl = new JLabel("AI in Modern World");
        eventTitleLbl.setFont(ThemeManager.FONT_HEADER);
        eventTitleLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        eventCategoryLbl = new JLabel("Technical Seminar");
        eventCategoryLbl.setFont(ThemeManager.FONT_SMALL);
        eventCategoryLbl.setForeground(ThemeManager.STATUS_BLUE);

        eventTimeLbl = new JLabel("Dec 10, 2024 • 10:00 AM - 12:00 PM");
        eventTimeLbl.setFont(ThemeManager.FONT_SMALL);
        eventTimeLbl.setForeground(ThemeManager.TEXT_MUTED);

        venueLbl = new JLabel("Seminar Hall A");
        venueLbl.setFont(ThemeManager.FONT_SMALL);
        venueLbl.setForeground(ThemeManager.TEXT_MUTED);

        info.add(eventTitleLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(eventCategoryLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(eventTimeLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(venueLbl);

        topRow.add(thumb, BorderLayout.WEST);
        topRow.add(info, BorderLayout.CENTER);

        // Middle split: Left Selected Seats, Right Price Details
        JPanel midRow = new JPanel(new GridLayout(1, 2, 24, 0));
        midRow.setOpaque(false);
        midRow.setBorder(BorderFactory.createEmptyBorder(20, 0, 16, 0));

        // Left: Selected Seats
        JPanel leftBox = new JPanel();
        leftBox.setOpaque(false);
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));

        JLabel selTitle = new JLabel("Selected Seats");
        selTitle.setFont(ThemeManager.FONT_BODY_BOLD);
        selTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        selectedSeatsChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        selectedSeatsChips.setOpaque(false);

        leftBox.add(selTitle);
        leftBox.add(Box.createVerticalStrut(8));
        leftBox.add(selectedSeatsChips);

        // Right: Price Details
        JPanel rightBox = new JPanel();
        rightBox.setOpaque(false);
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));

        JLabel priceTitle = new JLabel("Price Details");
        priceTitle.setFont(ThemeManager.FONT_BODY_BOLD);
        priceTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        JPanel pGrid = new JPanel(new GridLayout(4, 2, 8, 8));
        pGrid.setOpaque(false);

        pricePerSeatLbl = new JLabel("₹0", SwingConstants.RIGHT);
        seatCountLbl = new JLabel("0", SwingConstants.RIGHT);
        subtotalLbl = new JLabel("₹0", SwingConstants.RIGHT);
        grandTotalLbl = new JLabel("₹0", SwingConstants.RIGHT);

        for (JLabel l : new JLabel[]{pricePerSeatLbl, seatCountLbl, subtotalLbl}) {
            l.setFont(ThemeManager.FONT_SMALL);
            l.setForeground(ThemeManager.TEXT_SECONDARY);
        }
        grandTotalLbl.setFont(ThemeManager.FONT_HEADER);
        grandTotalLbl.setForeground(ThemeManager.STATUS_GREEN);

        pGrid.add(createCaption("Ticket Price (per seat)"));
        pGrid.add(pricePerSeatLbl);
        pGrid.add(createCaption("Number of Seats"));
        pGrid.add(seatCountLbl);
        pGrid.add(createCaption("Subtotal"));
        pGrid.add(subtotalLbl);
        pGrid.add(new JLabel("Total Amount") {{
            setFont(ThemeManager.FONT_BODY_BOLD);
            setForeground(ThemeManager.TEXT_PRIMARY);
        }});
        pGrid.add(grandTotalLbl);

        rightBox.add(priceTitle);
        rightBox.add(Box.createVerticalStrut(8));
        rightBox.add(pGrid);

        midRow.add(leftBox);
        midRow.add(rightBox);

        // Buttons row
        JPanel btnRow = new JPanel(new BorderLayout(14, 0));
        btnRow.setOpaque(false);

        StyledButton backBtn = new StyledButton("← Back", StyledButton.ButtonStyle.SECONDARY);
        backBtn.setPreferredSize(new Dimension(100, 42));
        backBtn.addActionListener(e -> {
            if (currentEvent != null) {
                navController.startSeatSelection(currentEvent);
            }
        });

        StyledButton continueBtn = new StyledButton("Continue to Payment →", StyledButton.ButtonStyle.PRIMARY);
        continueBtn.addActionListener(e -> {
            if (currentEvent != null && currentSeats != null) {
                navController.proceedToPayment(currentEvent, currentSeats);
            }
        });

        btnRow.add(backBtn, BorderLayout.WEST);
        btnRow.add(continueBtn, BorderLayout.CENTER);

        card.add(topRow);
        card.add(midRow);
        card.add(Box.createVerticalStrut(10));
        card.add(btnRow);

        center.add(card);
        add(center, BorderLayout.CENTER);
    }

    private JLabel createCaption(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(ThemeManager.FONT_SMALL);
        l.setForeground(ThemeManager.TEXT_MUTED);
        return l;
    }

    public void loadSummary(Event event, List<Seat> selectedSeats) {
        this.currentEvent = event;
        this.currentSeats = selectedSeats;
        if (event == null || selectedSeats == null) return;

        eventTitleLbl.setText(event.getTitle());
        eventCategoryLbl.setText(event.getCategory().getDisplayName());
        eventTimeLbl.setText(event.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " • " +
                event.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")) + " - " +
                event.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        venueLbl.setText(event.getSeminarHall());

        selectedSeatsChips.removeAll();
        for (Seat s : selectedSeats) {
            JLabel chip = new JLabel(s.getSeatIdentifier());
            chip.setFont(ThemeManager.FONT_SMALL_BOLD);
            chip.setForeground(Color.WHITE);
            chip.setBackground(ThemeManager.BG_PRIMARY);
            chip.setOpaque(true);
            chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            selectedSeatsChips.add(chip);
        }

        double total = selectedSeats.stream().mapToDouble(Seat::getPrice).sum();
        pricePerSeatLbl.setText("₹" + String.format("%.0f", event.getTicketPrice()));
        seatCountLbl.setText(String.valueOf(selectedSeats.size()));
        subtotalLbl.setText("₹" + String.format("%.0f", total));
        grandTotalLbl.setText("₹" + String.format("%.0f", total));
    }
}
