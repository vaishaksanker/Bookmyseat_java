package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * The home page's cinematic hero banner (App.jsx HomePage): a rounded-3xl auditorium photo
 * darkened from the left, the "PREMIUM COLLEGE AUDITORIUM SEATING" pill, the
 * "IDEAS / PEOPLE / POSSIBILITIES" heading, the subtitle and the glowing
 * "Explore Featured Event" button, which opens the featured event's details.
 */
public class FeaturedEventPanel extends PhotoPanel implements EventGrid.HeightForWidth {

    private static final int PAD = 56;            // sm:p-14
    private static final int MIN_HEIGHT = 440;    // sm:min-h-[440px]
    private static final int CONTENT_MAX = 576;   // max-w-xl
    private static final int H1_LINE = 60;        // lg:text-6xl (its line-height: 1 overrides leading-tight)

    private Event event;
    private final Consumer<Event> onExplore;

    private final JComponent pill;
    private final EventGrid.Paragraph heading;
    private final EventGrid.Paragraph headingAccent;
    private final EventGrid.Paragraph subtitle;
    private final StyledButton exploreButton;

    public FeaturedEventPanel(Event event, Consumer<Event> onExplore) {
        super(EventVisuals.AUDITORIUM_STAGE, ThemeManager.RADIUS_3XL, Overlay.HERO);
        this.event = event;
        this.onExplore = onExplore;
        setLayout(null);
        RemoteImages.get(EventVisuals.AUDITORIUM_STAGE, this::repaint);

        pill = new SparklePill("PREMIUM COLLEGE AUDITORIUM SEATING");

        Font h1 = EventGrid.tracked(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 60), -0.025f);
        heading = new EventGrid.Paragraph("IDEAS\nPEOPLE", h1, Color.WHITE, H1_LINE);
        headingAccent = new EventGrid.Paragraph("POSSIBILITIES", h1, ThemeManager.BRAND_RED, H1_LINE);
        headingAccent.setGradient(true);

        subtitle = new EventGrid.Paragraph(
                "Seminars • Workshops • Hackathons • Cultural Events. Reserve front-row auditorium seats "
                        + "with real-time seat maps and digital passes.",
                ThemeManager.font(ThemeManager.Weight.REGULAR, 16), ThemeManager.TEXT_GRAY_300, 24);
        // max-w-lg (512px) in font-light; the regular face is ~10% wider, so lines break where
        // the web's do ("...Reserve front-row / auditorium seats...")
        subtitle.setMaxWidth(563);

        exploreButton = new StyledButton("Explore Featured Event", StyledButton.ButtonStyle.PRIMARY);
        exploreButton.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 14));
        exploreButton.setMargin(new Insets(16, 32, 16, 32));
        exploreButton.setIcon(Icons.get("chevron-right", 16, Color.WHITE));
        exploreButton.setHorizontalTextPosition(SwingConstants.LEFT);
        exploreButton.setIconTextGap(10);
        exploreButton.setGlow(true);
        Dimension b = exploreButton.getPreferredSize();
        exploreButton.setPreferredSize(new Dimension(b.width, 52 + 14));   // py-4 + text-sm line, plus glow room
        exploreButton.addActionListener(e -> {
            if (this.onExplore != null && this.event != null) {
                this.onExplore.accept(this.event);
            }
        });

        add(pill);
        add(heading);
        add(headingAccent);
        add(subtitle);
        add(exploreButton);
    }

    /** The event the "Explore Featured Event" button opens. */
    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    private int contentWidth(int width) {
        return Math.max(200, Math.min(CONTENT_MAX, width - 2 * PAD));
    }

    private int contentHeight(int contentWidth) {
        return 30 + 16                                   // pill (py-1.5 + border), mb-4
                + 3 * H1_LINE + 12                       // heading, mb-3
                + subtitle.heightForWidth(contentWidth) + 32   // subtitle, mb-8
                + 52;                                    // button
    }

    @Override
    public int heightForWidth(int width) {
        return Math.max(MIN_HEIGHT, contentHeight(contentWidth(width)) + 2 * PAD);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : 1216;
        return new Dimension(w, heightForWidth(w));
    }

    @Override
    public void doLayout() {
        int cw = contentWidth(getWidth());
        int y = (getHeight() - contentHeight(cw)) / 2;    // flex-col justify-center
        Dimension p = pill.getPreferredSize();
        pill.setBounds(PAD, y, p.width, 30);
        y += 30 + 16;
        heading.setBounds(PAD, y, cw, 2 * H1_LINE);
        y += 2 * H1_LINE;
        headingAccent.setBounds(PAD, y, cw, H1_LINE);
        y += H1_LINE + 12;
        int subH = subtitle.heightForWidth(cw);
        subtitle.setBounds(PAD, y, cw, subH);
        y += subH + 32;
        Dimension b = exploreButton.getPreferredSize();
        exploreButton.setBounds(PAD - 8, y - 2, b.width, b.height);   // glow room around the button
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ThemeManager.RADIUS_3XL * 2;
        g2.setColor(ThemeManager.BORDER_CARD);          // border-white/10
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
    }

    /** "bg-[#ff382e]/20 border-[#ff382e]/40 rounded-full text-xs font-bold text-[#ff382e]" with a sparkles icon. */
    private static final class SparklePill extends JLabel {
        SparklePill(String text) {
            super(text, Icons.get("sparkles", 13, ThemeManager.BRAND_RED), SwingConstants.LEFT);
            setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 12));
            setForeground(ThemeManager.BRAND_RED);
            setIconTextGap(8);
            setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(255, 56, 46, 51));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(new Color(255, 56, 46, 102));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
