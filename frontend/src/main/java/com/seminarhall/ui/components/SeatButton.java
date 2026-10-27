package com.seminarhall.ui.components;

import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SeatButton extends JComponent {
    private final Seat seat;
    private boolean isHovered = false;
    private Runnable onToggle;

    public SeatButton(Seat seat, Runnable onToggle) {
        this.seat = seat;
        this.onToggle = onToggle;

        setPreferredSize(new Dimension(32, 28));
        setMinimumSize(new Dimension(28, 24));
        setCursor(seat.getStatus() == SeatStatus.BOOKED || seat.getStatus() == SeatStatus.UNAVAILABLE
                ? Cursor.getDefaultCursor() : new Cursor(Cursor.HAND_CURSOR));

        String tooltip = String.format("<html><b>Seat %s</b><br>Row: %s, Number: %d<br>Price: ₹%.0f<br>Status: %s%s</html>",
                seat.getSeatIdentifier(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getPrice(),
                seat.getStatus(),
                seat.isAccessible() ? "<br><i>♿ Accessible Seat</i>" : "");
        setToolTipText(tooltip);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (seat.getStatus() != SeatStatus.BOOKED && seat.getStatus() != SeatStatus.UNAVAILABLE) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (seat.getStatus() == SeatStatus.BOOKED || seat.getStatus() == SeatStatus.UNAVAILABLE) {
                    return;
                }
                if (seat.getStatus() == SeatStatus.AVAILABLE) {
                    seat.setStatus(SeatStatus.SELECTED);
                } else if (seat.getStatus() == SeatStatus.SELECTED) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                }
                repaint();
                if (onToggle != null) {
                    onToggle.run();
                }
            }
        });
    }

    public Seat getSeat() {
        return seat;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int pad = 1;

        Color seatBg;
        Color borderColor;

        switch (seat.getStatus()) {
            case SELECTED:
                seatBg = ThemeManager.SEAT_SELECTED; // Vibrant Amber (#F59E0B)
                borderColor = Color.WHITE;
                break;
            case BOOKED:
                seatBg = ThemeManager.SEAT_BOOKED;   // Red booked (#EF4444)
                borderColor = new Color(180, 40, 40);
                break;
            case UNAVAILABLE:
                seatBg = ThemeManager.SEAT_UNAVAILABLE;
                borderColor = new Color(60, 65, 80);
                break;
            case AVAILABLE:
            default:
                if (isHovered) {
                    seatBg = ThemeManager.STATUS_GREEN.brighter();
                    borderColor = Color.WHITE;
                } else {
                    seatBg = ThemeManager.SEAT_AVAILABLE; // Emerald green (#10B981)
                    borderColor = new Color(14, 150, 105);
                }
                break;
        }

        // Pill shape matching the screenshot
        int arc = 8;
        g2.setColor(seatBg);
        g2.fillRoundRect(pad, pad, w - (pad * 2), h - (pad * 2), arc, arc);

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(pad, pad, w - (pad * 2) - 1, h - (pad * 2) - 1, arc, arc);

        g2.dispose();
    }
}
