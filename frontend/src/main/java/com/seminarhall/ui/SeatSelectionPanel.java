package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.service.SeatService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.components.SeatMapPanel;
import com.seminarhall.ui.components.StepIndicatorBar;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SeatSelectionPanel extends JPanel {
    private final NavigationController navController;
    private final SeatService seatService;
    private Event currentEvent;

    private StepIndicatorBar stepBar;
    private SeatMapPanel seatMapPanel;
    private JLabel eventTitleLbl;
    private JLabel eventDateLbl;
    private JLabel eventHallLbl;
    private JPanel selectedSeatsChipsPanel;
    private JLabel ticketPriceLbl;
    private JLabel seatCountLbl;
    private JLabel subtotalLbl;
    private StyledButton continueBtn;

    public SeatSelectionPanel(NavigationController navController) {
        this.navController = navController;
        this.seatService = ServiceFactory.getSeatService();

        setName(NavigationController.SCREEN_SEAT_SELECTION);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Top Header: Back Button + Step Indicator Bar (Screen 5)
        JPanel topHeader = new JPanel(new BorderLayout(16, 0));
        topHeader.setOpaque(false);
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));

        JLabel backBtn = new JLabel("← Back to Event");
        backBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        backBtn.setForeground(ThemeManager.TEXT_SECONDARY);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navController.goBack();
            }
        });

        stepBar = new StepIndicatorBar(1); // Step 1: Select Seats
        topHeader.add(backBtn, BorderLayout.WEST);
        topHeader.add(stepBar, BorderLayout.CENTER);
        add(topHeader, BorderLayout.NORTH);

        // Center Split: Left Auditorium Seat Map, Right Booking Card
        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        // Left Auditorium Map
        JPanel mapHolder = new JPanel(new BorderLayout());
        mapHolder.setOpaque(false);
        seatMapPanel = new SeatMapPanel(this::onSeatSelectionChanged);
        mapHolder.add(seatMapPanel, BorderLayout.CENTER);

        // Right Order Review Card (matching Screen 5 right sidebar)
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
        rightCard.setPreferredSize(new Dimension(340, 520));
        rightCard.setMaximumSize(new Dimension(360, 560));
        rightCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Event mini summary row with thumbnail
        JPanel eventMeta = new JPanel(new BorderLayout(12, 0));
        eventMeta.setOpaque(false);

        JPanel thumb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 20, 20), getWidth(), getHeight(), new Color(20, 22, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString("🎙", (getWidth() - 12) / 2, (getHeight() + 10) / 2);
                g2.dispose();
            }
        };
        thumb.setPreferredSize(new Dimension(50, 50));
        thumb.setOpaque(false);

        JPanel metaTexts = new JPanel();
        metaTexts.setOpaque(false);
        metaTexts.setLayout(new BoxLayout(metaTexts, BoxLayout.Y_AXIS));

        eventTitleLbl = new JLabel("AI in Modern World");
        eventTitleLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        eventTitleLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        eventDateLbl = new JLabel("Dec 10, 2024 • 10:00 AM");
        eventDateLbl.setFont(ThemeManager.FONT_SMALL);
        eventDateLbl.setForeground(ThemeManager.TEXT_MUTED);

        eventHallLbl = new JLabel("Seminar Hall A");
        eventHallLbl.setFont(ThemeManager.FONT_SMALL);
        eventHallLbl.setForeground(ThemeManager.TEXT_MUTED);

        metaTexts.add(eventTitleLbl);
        metaTexts.add(Box.createVerticalStrut(2));
        metaTexts.add(eventDateLbl);
        metaTexts.add(Box.createVerticalStrut(2));
        metaTexts.add(eventHallLbl);

        eventMeta.add(thumb, BorderLayout.WEST);
        eventMeta.add(metaTexts, BorderLayout.CENTER);

        // Selected Seats Chips
        JLabel selTitle = new JLabel("Selected Seats");
        selTitle.setFont(ThemeManager.FONT_BODY_BOLD);
        selTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        selectedSeatsChipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        selectedSeatsChipsPanel.setOpaque(false);
        selectedSeatsChipsPanel.setMaximumSize(new Dimension(320, 40));

        // Price Breakdown
        JPanel priceGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        priceGrid.setOpaque(false);
        priceGrid.setMaximumSize(new Dimension(320, 90));

        ticketPriceLbl = new JLabel("₹0");
        ticketPriceLbl.setFont(ThemeManager.FONT_BODY);
        ticketPriceLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        ticketPriceLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        seatCountLbl = new JLabel("0");
        seatCountLbl.setFont(ThemeManager.FONT_BODY);
        seatCountLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        seatCountLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        subtotalLbl = new JLabel("₹0");
        subtotalLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        subtotalLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        subtotalLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        priceGrid.add(createLabel("Ticket Price (per seat):"));
        priceGrid.add(ticketPriceLbl);
        priceGrid.add(createLabel("Number of Seats:"));
        priceGrid.add(seatCountLbl);
        priceGrid.add(createLabel("Subtotal:"));
        priceGrid.add(subtotalLbl);

        continueBtn = new StyledButton("Continue →", StyledButton.ButtonStyle.PRIMARY);
        continueBtn.setEnabled(false);
        continueBtn.setMaximumSize(new Dimension(320, 44));
        continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueBtn.addActionListener(e -> {
            List<Seat> sel = seatMapPanel.getSelectedSeats();
            if (!sel.isEmpty()) {
                navController.proceedToBookingSummary(currentEvent, sel);
            }
        });

        rightCard.add(eventMeta);
        rightCard.add(Box.createVerticalStrut(18));
        rightCard.add(selTitle);
        rightCard.add(Box.createVerticalStrut(6));
        rightCard.add(selectedSeatsChipsPanel);
        rightCard.add(Box.createVerticalStrut(18));
        rightCard.add(priceGrid);
        rightCard.add(Box.createVerticalStrut(24));
        rightCard.add(continueBtn);

        mainContent.add(mapHolder, BorderLayout.CENTER);
        mainContent.add(rightCard, BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);
    }

    private JLabel createLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(ThemeManager.FONT_SMALL);
        l.setForeground(ThemeManager.TEXT_MUTED);
        return l;
    }

    public void loadEvent(Event event) {
        this.currentEvent = event;
        if (event == null) return;

        eventTitleLbl.setText(event.getTitle());
        eventDateLbl.setText(event.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " • " +
                event.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        eventHallLbl.setText(event.getSeminarHall());

        SwingWorker<List<Seat>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Seat> doInBackground() throws Exception {
                return seatService.getSeatsForEvent(event.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Seat> seats = get();
                    seatMapPanel.setSeats(seats);
                    onSeatSelectionChanged();
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void onSeatSelectionChanged() {
        List<Seat> selected = seatMapPanel.getSelectedSeats();
        int count = selected.size();
        double total = selected.stream().mapToDouble(Seat::getPrice).sum();

        selectedSeatsChipsPanel.removeAll();
        for (Seat s : selected) {
            JLabel chip = new JLabel(s.getSeatIdentifier());
            chip.setFont(ThemeManager.FONT_SMALL_BOLD);
            chip.setForeground(Color.WHITE);
            chip.setBackground(ThemeManager.BG_PRIMARY);
            chip.setOpaque(true);
            chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            selectedSeatsChipsPanel.add(chip);
        }

        ticketPriceLbl.setText(currentEvent != null && currentEvent.getTicketPrice() > 0
                ? "₹" + String.format("%.0f", currentEvent.getTicketPrice()) : "₹0");
        seatCountLbl.setText(String.valueOf(count));
        subtotalLbl.setText("₹" + String.format("%.0f", total));

        continueBtn.setEnabled(count > 0);
        selectedSeatsChipsPanel.revalidate();
        selectedSeatsChipsPanel.repaint();
    }
}
