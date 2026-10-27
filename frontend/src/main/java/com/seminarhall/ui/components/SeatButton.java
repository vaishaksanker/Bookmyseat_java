package com.seminarhall.ui.components;

import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * One seat of the web app's seat map: a w-7 h-7 rounded-md square showing the seat number in
 * font-mono text-[9px] font-bold. Available seats are emerald (#10b981, hover #059669), a selected
 * seat is amber (#f59e0b) with an amber glow and scale-105, booked seats are red (#ef4444) at 80% opacity.
 * Clicking toggles AVAILABLE and SELECTED; booked and unavailable seats ignore clicks.
 * The selected glow is painted by the seat map behind the seats.
 */
public class SeatButton extends JComponent {
    public static final Color AVAILABLE = new Color(16, 185, 129);        // #10b981
    public static final Color AVAILABLE_HOVER = new Color(5, 150, 105);   // #059669
    public static final Color SELECTED = new Color(245, 158, 11);         // #f59e0b
    public static final Color SELECTED_GLOW = new Color(245, 158, 11, 204); // shadow rgba(245,158,11,.8)
    public static final Color BOOKED = new Color(239, 68, 68);            // #ef4444
    public static final Color UNAVAILABLE = new Color(55, 65, 81);        // #374151

    /** w-7 h-7 */
    public static final int SIZE = 28;
    /** The component is a little larger than the seat so the selected seat can scale to 105%. */
    public static final int BOX = 30;

    private static final Font MONO = monoFont();

    private final Seat seat;
    private final Runnable onToggle;
    private boolean hovered;

    public SeatButton(Seat seat, Runnable onToggle) {
        this.seat = seat;
        this.onToggle = onToggle;

        setOpaque(false);
        setPreferredSize(new Dimension(BOX, BOX));
        setMinimumSize(new Dimension(BOX, BOX));
        setCursor(isTaken() ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isTaken()) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isTaken()) {
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

    private boolean isTaken() {
        return seat.getStatus() == SeatStatus.BOOKED || seat.getStatus() == SeatStatus.UNAVAILABLE;
    }

    private static Font monoFont() {
        // font-mono resolves to Consolas on Windows
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (family.equals("Consolas")) {
                return new Font("Consolas", Font.BOLD, 9);
            }
        }
        return new Font(Font.MONOSPACED, Font.BOLD, 9);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        SeatStatus status = seat.getStatus();
        boolean selected = status == SeatStatus.SELECTED;
        float scale = selected ? 1.05f : 1f;
        float size = SIZE * scale;
        float x = (getWidth() - size) / 2f;
        float y = (getHeight() - size) / 2f;

        Color fill;
        Color text;
        switch (status) {
            case SELECTED:
                fill = SELECTED;
                text = Color.BLACK;
                break;
            case BOOKED:
                fill = BOOKED;
                text = new Color(255, 255, 255, 128);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                break;
            case UNAVAILABLE:
                fill = UNAVAILABLE;
                text = new Color(255, 255, 255, 128);
                break;
            case AVAILABLE:
            default:
                fill = hovered ? AVAILABLE_HOVER : AVAILABLE;
                text = Color.WHITE;
                break;
        }

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(x, y, size, size, 12 * scale, 12 * scale));

        String number = String.valueOf(seat.getSeatNumber());
        g2.setFont(MONO.deriveFont(9f * scale));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(text);
        float tx = x + (size - (float) fm.getStringBounds(number, g2).getWidth()) / 2f;
        float ty = y + (size + fm.getAscent() - fm.getDescent()) / 2f;
        g2.drawString(number, tx, ty);
        g2.dispose();
    }
}
