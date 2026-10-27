package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Event card from the web app's home grid (App.jsx HomePage): a .glass-card rounded-2xl with
 * an h-48 photo (category tag top-left, "N seats left" bottom-right), the title, date · time and
 * hall rows, then "Price" and the glowing "Book Seats" button. Clicking the card opens the
 * details; the button starts booking.
 */
public class EventCard extends JPanel {

    /** Card height: h-48 photo + p-4 body (title, two rows, footer). */
    public static final int HEIGHT = 351;

    private static final int PHOTO_H = 192;
    private static final int PAD = 16;
    private static final int RADIUS = ThemeManager.RADIUS_2XL;
    private static final int FOOTER_Y = 290;   // border-t of the price row
    private static final Color FOOTER_LINE = new Color(29, 34, 50);       // #1d2232
    private static final Color CARD_FILL = new Color(18, 21, 32, 209);    // rgba(18,21,32,.82)
    private static final Color HOVER_BORDER = new Color(255, 56, 46, 115);

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final Event event;
    private final JLabel title;
    private final JLabel dateRow;
    private final JLabel hallRow;
    private final JLabel priceCaption;
    private final JLabel price;
    private final StyledButton bookButton;
    private final Photo photo;
    private boolean hovered;

    public EventCard(Event event, Consumer<Event> onViewDetails, Consumer<Event> onBookNow) {
        this.event = event;
        setOpaque(false);
        setLayout(null);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(286, HEIGHT));

        photo = new Photo(EventVisuals.imageFor(event));
        RemoteImages.get(EventVisuals.imageFor(event), photo::repaint);   // start the download now, not at first paint
        Tag tag = new Tag(EventVisuals.tagLabel(event.getCategory()), EventVisuals.tagColor(event.getCategory()),
                Color.WHITE, 6, EventGrid.tracked(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 9), 0.05f));
        tag.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        SeatsPill seats = new SeatsPill(event.getAvailableSeats() + " seats left");
        photo.add(tag);
        photo.add(seats);
        add(photo);

        title = new JLabel(event.getTitle());
        title.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 14));
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        add(title);

        String when = (event.getDate() != null ? event.getDate().format(DATE) : "")
                + (event.getStartTime() != null ? " · " + event.getStartTime().format(TIME) : "");
        dateRow = infoRow("calendar", when);
        hallRow = infoRow("map-pin", event.getSeminarHall());
        add(dateRow);
        add(hallRow);

        priceCaption = new JLabel("Price");
        priceCaption.setFont(ThemeManager.font(ThemeManager.Weight.REGULAR, 10));
        priceCaption.setForeground(ThemeManager.TEXT_MUTED);
        add(priceCaption);

        boolean free = event.getTicketPrice() <= 0;
        price = new JLabel(EventVisuals.priceLabel(event.getTicketPrice()));
        price.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 14));
        price.setForeground(free ? ThemeManager.EMERALD_400 : ThemeManager.AMBER_400);
        add(price);

        bookButton = new StyledButton("Book Seats", StyledButton.ButtonStyle.PRIMARY);
        bookButton.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 12));
        bookButton.setMargin(new Insets(8, 16, 8, 16));
        bookButton.setIcon(Icons.get("chevron-right", 13, Color.WHITE));
        bookButton.setHorizontalTextPosition(SwingConstants.LEFT);
        bookButton.setIconTextGap(4);
        bookButton.setGlow(true);
        bookButton.addActionListener(e -> {
            if (onBookNow != null) {
                onBookNow.accept(event);
            }
        });
        add(bookButton);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), EventCard.this);
                if (!new Rectangle(getSize()).contains(p)) {
                    setHovered(false);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onViewDetails != null && SwingUtilities.isLeftMouseButton(e)) {
                    onViewDetails.accept(event);
                }
            }
        };
        addMouseListener(mouse);
        bookButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouse.mouseExited(e);
            }
        });
    }

    public Event getEvent() {
        return event;
    }

    private static JLabel infoRow(String icon, String text) {
        JLabel label = Icons.withText(icon, 13, ThemeManager.TEXT_MUTED, text,
                ThemeManager.font(ThemeManager.Weight.REGULAR, 12), ThemeManager.TEXT_SECONDARY);
        label.setIconTextGap(8);
        return label;
    }

    private void setHovered(boolean hovered) {
        if (this.hovered != hovered) {
            this.hovered = hovered;
            title.setForeground(hovered ? ThemeManager.BRAND_RED : ThemeManager.TEXT_PRIMARY);
            repaint();
        }
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int inner = w - 2 * PAD;
        photo.setBounds(0, 0, w, PHOTO_H);
        title.setBounds(PAD, PHOTO_H + PAD, inner, 20);                 // text-sm, mb-2
        dateRow.setBounds(PAD, PHOTO_H + PAD + 28, inner, 16);          // text-xs, space-y-1.5
        hallRow.setBounds(PAD, PHOTO_H + PAD + 50, inner, 16);

        int rowY = FOOTER_Y + 1 + 12;                                   // border-t pt-3
        int rowH = 32;
        priceCaption.setBounds(PAD, rowY + 1, inner / 2, 11);           // leading-none
        price.setBounds(PAD, rowY + 11, inner / 2, 20);
        Dimension b = bookButton.getPreferredSize();
        // the glow adds 8px at the sides, 2px above and 12px below the visible button
        int visibleH = b.height - 14;
        bookButton.setBounds(w - PAD - b.width + 8, rowY + (rowH - visibleH) / 2 - 2, b.width, b.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(CARD_FILL);
        g2.fillRoundRect(0, 0, w - 1, h - 1, RADIUS * 2, RADIUS * 2);
        g2.setColor(FOOTER_LINE);
        g2.fillRect(PAD, FOOTER_Y, w - 2 * PAD, 1);
        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? HOVER_BORDER : ThemeManager.BORDER_SUBTLE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS * 2, RADIUS * 2);
        g2.dispose();
    }

    /** The h-48 photo: cover-fit, top corners rounded, "bg-gradient-to-t from-[#10131d] via-transparent to-black/30". */
    private static final class Photo extends JComponent {
        private final String url;

        Photo(String url) {
            this.url = url;
            setLayout(null);
        }

        @Override
        public void doLayout() {
            Component tag = getComponent(0);
            Component seats = getComponent(1);
            Dimension t = tag.getPreferredSize();
            tag.setBounds(12, 12, t.width, 21);
            Dimension s = seats.getPreferredSize();
            seats.setBounds(getWidth() - 12 - s.width, getHeight() - 12 - s.height, s.width, s.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            Area clip = new Area(new RoundRectangle2D.Float(0, 0, w, h + RADIUS * 2, RADIUS * 2, RADIUS * 2));
            clip.intersect(new Area(new Rectangle(0, 0, w, h)));
            g2.clip(clip);

            BufferedImage image = RemoteImages.get(url, this::repaint);
            if (image != null) {
                double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
                int iw = (int) Math.ceil(image.getWidth() * scale);
                int ih = (int) Math.ceil(image.getHeight() * scale);
                g2.drawImage(image, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(40, 14, 16), w, h, ThemeManager.BG_SECONDARY));
                g2.fillRect(0, 0, w, h);
            }
            g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.5f, 1f},
                    new Color[]{new Color(0, 0, 0, 77), new Color(16, 19, 29, 0), new Color(16, 19, 29)}));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    /** "45 seats left": bg-black/70 text-[10px] text-emerald-400 font-bold rounded border-emerald-500/30. */
    private static final class SeatsPill extends JLabel {
        SeatsPill(String text) {
            super(text);
            setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 10));
            setForeground(ThemeManager.EMERALD_400);
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 21);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 179));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(new Color(16, 185, 129, 77));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
