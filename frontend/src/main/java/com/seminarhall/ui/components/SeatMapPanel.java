package com.seminarhall.ui.components;

import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SeatMapPanel extends JPanel {
    private List<Seat> seats = new ArrayList<>();
    private final List<SeatButton> seatButtons = new ArrayList<>();
    private final Runnable onSelectionChanged;

    public SeatMapPanel(Runnable onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats != null ? seats : new ArrayList<>();
        rebuildSeatGrid();
    }

    public List<Seat> getSelectedSeats() {
        return seatButtons.stream()
                .map(SeatButton::getSeat)
                .filter(s -> s.getStatus() == SeatStatus.SELECTED)
                .collect(Collectors.toList());
    }

    public void clearSelections() {
        for (SeatButton sb : seatButtons) {
            if (sb.getSeat().getStatus() == SeatStatus.SELECTED) {
                sb.getSeat().setStatus(SeatStatus.AVAILABLE);
                sb.repaint();
            }
        }
    }

    private void rebuildSeatGrid() {
        removeAll();
        seatButtons.clear();

        if (seats.isEmpty()) {
            add(new JLabel("No seating data available for this hall.", SwingConstants.CENTER), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Top Screen / Curved Stage indicator
        JPanel stagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int bannerW = Math.min(380, w - 40);
                int ox = (w - bannerW) / 2;

                // Subtle dark curved backdrop
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(ox, 4, bannerW, 26, 12, 12);

                g2.setColor(ThemeManager.BORDER_CARD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(ox, 4, bannerW, 26, 12, 12);

                // Stage text
                g2.setColor(ThemeManager.TEXT_MUTED);
                g2.setFont(ThemeManager.FONT_SMALL_BOLD);
                String msg = "STAGE";
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(msg)) / 2;
                g2.drawString(msg, tx, 21);

                g2.dispose();
            }
        };
        stagePanel.setOpaque(false);
        stagePanel.setPreferredSize(new Dimension(500, 36));
        add(stagePanel, BorderLayout.NORTH);

        // Group seats by row label (A through J)
        Map<String, List<Seat>> rowsMap = new LinkedHashMap<>();
        for (Seat s : seats) {
            rowsMap.computeIfAbsent(s.getRowLabel(), k -> new ArrayList<>()).add(s);
        }

        JPanel auditoriumGrid = new JPanel();
        auditoriumGrid.setOpaque(false);
        auditoriumGrid.setLayout(new BoxLayout(auditoriumGrid, BoxLayout.Y_AXIS));

        for (Map.Entry<String, List<Seat>> entry : rowsMap.entrySet()) {
            String rowLabel = entry.getKey();
            List<Seat> rowSeats = entry.getValue();

            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 3));
            rowPanel.setOpaque(false);

            // Left row letter (A, B, C...)
            JLabel leftLbl = new JLabel(rowLabel, SwingConstants.CENTER);
            leftLbl.setFont(ThemeManager.FONT_SMALL_BOLD);
            leftLbl.setForeground(ThemeManager.TEXT_MUTED);
            leftLbl.setPreferredSize(new Dimension(18, 26));
            rowPanel.add(leftLbl);

            // Row seats split with a center aisle
            int aisleIndex = rowSeats.size() / 2;
            for (int i = 0; i < rowSeats.size(); i++) {
                if (i == aisleIndex && rowSeats.size() > 4) {
                    rowPanel.add(Box.createHorizontalStrut(28)); // Center aisle
                }
                Seat s = rowSeats.get(i);
                SeatButton sb = new SeatButton(s, onSelectionChanged);
                seatButtons.add(sb);
                rowPanel.add(sb);
            }

            auditoriumGrid.add(rowPanel);
        }

        // Legend at bottom matching screenshot: Available (green), Selected (amber), Booked (red), Unavailable (grey)
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        legendPanel.setOpaque(false);

        legendPanel.add(createLegendItem(ThemeManager.SEAT_AVAILABLE, "Available"));
        legendPanel.add(createLegendItem(ThemeManager.SEAT_SELECTED, "Selected"));
        legendPanel.add(createLegendItem(ThemeManager.SEAT_BOOKED, "Booked"));
        legendPanel.add(createLegendItem(ThemeManager.SEAT_UNAVAILABLE, "Unavailable"));

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.add(auditoriumGrid, BorderLayout.CENTER);
        centerContainer.add(legendPanel, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createLegendItem(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        box.setPreferredSize(new Dimension(12, 12));
        box.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_SMALL);
        lbl.setForeground(ThemeManager.TEXT_SECONDARY);

        p.add(box);
        p.add(lbl);
        return p;
    }
}
