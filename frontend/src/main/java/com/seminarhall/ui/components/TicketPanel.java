package com.seminarhall.ui.components;

import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The white digital pass on the booking confirmation page (web TicketConfirmationPage, ".ticket-stub"):
 * "w-full bg-white rounded-3xl p-6 flex-row gap-5" with the event photo (w-24 h-28 rounded-2xl),
 * the event details and the black "ENTRY PASS" QR block (w-20 h-20 rounded-2xl).
 * Fill it with {@link #setBooking(Booking, Event)}.
 */
public class TicketPanel extends JPanel {

    /** max-w-xl (576) minus the page's px-8 padding. */
    public static final int WIDTH = 512;

    private static final int PAD = 24;           // p-6
    private static final int GAP = 20;           // gap-5
    private static final int PHOTO_W = 96;       // sm:w-24
    private static final int PHOTO_H = 112;      // sm:h-28
    private static final int QR = 80;            // w-20 h-20
    private static final int DETAILS_W = WIDTH - 2 * PAD - PHOTO_W - QR - 2 * GAP;

    private static final Color GRAY_900 = new Color(17, 24, 39);
    private static final Color GRAY_600 = new Color(75, 85, 99);
    private static final Color GRAY_500 = new Color(107, 114, 128);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final PhotoPanel photo = new PhotoPanel(null, ThemeManager.RADIUS_2XL, PhotoPanel.Overlay.NONE);
    private final JPanel details = new JPanel();
    private final QrBlock qr = new QrBlock();

    public TicketPanel() {
        setOpaque(false);
        setLayout(null);
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        add(photo);
        add(details);
        add(qr);
    }

    /** Shows a booking; the event (may be null) supplies the photo, category tag and time range. */
    public void setBooking(Booking booking, Event event) {
        details.removeAll();
        if (booking == null) {
            revalidate();
            repaint();
            return;
        }

        Event forImage = event;
        if (forImage == null) {
            forImage = new Event();
            forImage.setTitle(booking.getEventTitle());
        }
        String url = EventVisuals.imageFor(forImage);
        photo.setUrl(url);
        RemoteImages.get(url, photo::repaint); // start loading now, not on first paint

        // bg-blue-600 text-white font-extrabold text-[8px] px-2 py-0.5 rounded tracking-wider
        Font tagFont = ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 8)
                .deriveFont(Map.of(TextAttribute.TRACKING, 0.05f));
        Tag tag = new Tag(EventVisuals.tagLabel(event != null ? event.getCategory() : null),
                ThemeManager.BLUE_600, Color.WHITE, 4, tagFont);
        tag.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        addLine(tag, 0);
        details.add(Box.createVerticalStrut(4));

        // font-black text-base text-gray-900 leading-tight
        WrapText title = new WrapText(booking.getEventTitle(), ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 16),
                GRAY_900, DETAILS_W, 20);
        addLine(title, 0);
        details.add(Box.createVerticalStrut(4));

        Font small = ThemeManager.font(ThemeManager.Weight.REGULAR, 11);
        addLine(Icons.withText("calendar", 12, GRAY_600, whenText(booking, event), small, GRAY_600), 16);
        details.add(Box.createVerticalStrut(2));
        addLine(Icons.withText("map-pin", 12, GRAY_600, nz(booking.getSeminarHall()), small, GRAY_600), 16);
        details.add(Box.createVerticalStrut(8));

        // font-extrabold text-sm text-gray-900
        addLine(new WrapText("Seats: " + booking.getFormattedSeats(), ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 14),
                GRAY_900, DETAILS_W, 20), 0);
        details.add(Box.createVerticalStrut(2));

        // text-[10px] text-gray-500 font-mono
        Font mono = monoFont(Font.PLAIN, 10);
        addLine(label("Booking ID: " + booking.getId(), mono, GRAY_500), 16);
        addLine(label("Amount Paid: " + rupees(booking.getTotalAmount()), mono, GRAY_500), 16);

        qr.setCaption(booking.getStatus() == BookingStatus.CANCELLED ? "CANCELLED" : "ENTRY PASS");
        revalidate();
        repaint();
    }

    private void addLine(JComponent c, int height) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (height > 0) {
            // full column width, so text measured slightly narrower than it paints is never cut off
            Dimension d = new Dimension(DETAILS_W, height);
            c.setPreferredSize(d);
            c.setMaximumSize(d);
        }
        details.add(c);
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** "Dec 10, 2024 · 10:00 AM - 12:00 PM", as on the web pass. */
    private static String whenText(Booking booking, Event event) {
        if (event != null && event.getDate() != null && event.getStartTime() != null) {
            String time = event.getStartTime().format(TIME);
            if (event.getEndTime() != null) {
                time += " - " + event.getEndTime().format(TIME);
            }
            return event.getDate().format(DATE) + " · " + time;
        }
        LocalDateTime at = booking.getEventDateTime();
        return at == null ? "" : at.format(DATE) + " · " + at.format(TIME);
    }

    /** "₹200" (whole rupees) or "₹199.98", like the web's ₹{amount}. */
    public static String rupees(double amount) {
        if (Math.abs(amount - Math.rint(amount)) < 0.005) {
            return "₹" + String.format(Locale.ENGLISH, "%.0f", amount);
        }
        return "₹" + String.format(Locale.ENGLISH, "%.2f", amount);
    }

    private static String monoFamily;

    /** The browser's font-mono on Windows (Consolas), falling back to Java's monospaced font. */
    public static Font monoFont(int style, float size) {
        if (monoFamily == null) {
            monoFamily = Font.MONOSPACED;
            for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (family.equalsIgnoreCase("Consolas")) {
                    monoFamily = family;
                    break;
                }
            }
        }
        Font font = new Font(monoFamily, style, Math.round(size));
        return size == Math.round(size) ? font : font.deriveFont(size);
    }

    @Override
    public Dimension getPreferredSize() {
        int detailsH = details.getPreferredSize().height;
        return new Dimension(WIDTH, 2 * PAD + Math.max(PHOTO_H, Math.max(detailsH, QR)));
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        photo.setBounds(PAD, PAD, PHOTO_W, PHOTO_H);
        int dx = PAD + PHOTO_W + GAP;
        int dw = w - dx - GAP - QR - PAD;
        details.setBounds(dx, PAD, dw, Math.max(0, h - 2 * PAD));
        qr.setBounds(w - PAD - QR, (h - QR) / 2, QR, QR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ThemeManager.RADIUS_3XL * 2;
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
    }

    /** "w-20 h-20 bg-black rounded-2xl": the white QrCode icon (40px) over "ENTRY PASS" (7px mono, tracking-widest). */
    private static class QrBlock extends JComponent {
        private final Icon icon = Icons.get("qr-code", 40, Color.WHITE);
        private final Font captionFont = monoFont(Font.PLAIN, 7).deriveFont(Map.of(TextAttribute.TRACKING, 0.1f));
        private String caption = "ENTRY PASS";

        void setCaption(String caption) {
            this.caption = caption;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_2XL * 2;
            g2.setColor(Color.BLACK);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setFont(captionFont);
            FontMetrics fm = g2.getFontMetrics();
            int captionH = 11;
            int contentH = icon.getIconHeight() + 4 + captionH;
            int top = (h - contentH) / 2;
            icon.paintIcon(this, g2, (w - icon.getIconWidth()) / 2, top);
            g2.setColor("CANCELLED".equals(caption) ? ThemeManager.RED_400 : ThemeManager.TEXT_SECONDARY);
            int baseline = top + icon.getIconHeight() + 4 + (captionH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(caption, (w - fm.stringWidth(caption)) / 2, baseline);
            g2.dispose();
        }
    }

    /** Word-wrapped left-aligned text with a fixed width and line height (e.g. a long event title). */
    private static class WrapText extends JComponent {
        private final String text;
        private final int width;
        private final int lineHeight;

        WrapText(String text, Font font, Color color, int width, int lineHeight) {
            this.text = text == null ? "" : text;
            this.width = width;
            this.lineHeight = lineHeight;
            setFont(font);
            setForeground(color);
        }

        private List<String> lines() {
            FontMetrics fm = getFontMetrics(getFont());
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) > width && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            lines.add(line.toString());
            return lines;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width, lines().size() * lineHeight);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int y = 0;
            for (String l : lines()) {
                g2.drawString(l, 0, y + (lineHeight + fm.getAscent() - fm.getDescent()) / 2);
                y += lineHeight;
            }
            g2.dispose();
        }
    }
}
