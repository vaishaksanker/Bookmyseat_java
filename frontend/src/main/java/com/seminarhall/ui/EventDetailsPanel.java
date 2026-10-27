package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.components.EventGrid;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.GlassPanel;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.PhotoPanel;
import com.seminarhall.ui.components.RemoteImages;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.Tag;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Event details, the web app's EventDetailsPage: the event photo as a full-width backdrop,
 * "Back to Explore", the category (and FEATURED) tags, title, description and the three info
 * boxes on the left, and the "Event Details & Timing" glass card with "Select Seats →" on the right.
 */
public class EventDetailsPanel extends JPanel implements NavigationController.RefreshableScreen {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private static final int PAD = 48;          // lg:p-12
    private static final int MAX_W = 1280;      // max-w-7xl
    private static final int CARD_W = 420;      // grid-cols-[1.3fr_420px]
    private static final int COL_GAP = 32;      // gap-8
    private static final int TOP = PAD + 34 + 24;          // back button + mb-6
    private static final int BOTTOM = 24 + 16 + PAD;       // footer pt-6 + text-xs line
    private static final int TAG_H = 23;
    private static final int BOX_H = 55;
    private static final int CARD_H = 468;

    private final NavigationController navController;
    private final EventService eventService;
    private Event currentEvent;
    private SwingWorker<Object[], Void> refresher;

    private final Backdrop backdrop;
    private final EventListPanel.BackPill backButton;
    private final Font tagFont = EventGrid.tracked(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 10), 0.05f);
    private Tag categoryTag;
    private final Tag featuredTag;
    private final EventGrid.Paragraph titleText;
    private final EventGrid.Paragraph descriptionText;
    private final InfoBox hallBox;
    private final InfoBox departmentBox;
    private final InfoBox priceBox;
    private final GlassPanel detailsCard;
    private final JLabel cardTitle;
    private final DetailRow dateRow;
    private final DetailRow timeRow;
    private final DetailRow venueRow;
    private final DetailRow departmentRow;
    private final DetailRow seatsRow;
    private final StyledButton selectSeatsBtn;
    private final JLabel footer;

    public EventDetailsPanel(NavigationController navController) {
        this.navController = navController;
        this.eventService = ServiceFactory.getEventService();

        setName(NavigationController.SCREEN_EVENT_DETAILS);
        setOpaque(true);
        setBackground(ThemeManager.BG_PAGE);
        setLayout(new BorderLayout());

        backdrop = new Backdrop();

        backButton = new EventListPanel.BackPill("Back to Explore", navController::goBack);

        categoryTag = categoryTag("TECHNICAL", ThemeManager.BLUE_600);
        featuredTag = new Tag("FEATURED", ThemeManager.AMBER_600, Color.WHITE, 6, tagFont);
        featuredTag.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        featuredTag.setVisible(false);

        titleText = new EventGrid.Paragraph("", EventGrid.tracked(
                ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 48), -0.025f), Color.WHITE, 48);
        descriptionText = new EventGrid.Paragraph("", ThemeManager.font(ThemeManager.Weight.REGULAR, 16),
                ThemeManager.TEXT_GRAY_300, 24);
        // max-w-xl (576px) in font-light; the regular face is ~10% wider, so lines break where the web's do
        descriptionText.setMaxWidth(620);

        hallBox = new InfoBox("AUDITORIUM", Color.WHITE);
        departmentBox = new InfoBox("DEPARTMENT", Color.WHITE);
        priceBox = new InfoBox("PASS PRICE", ThemeManager.EMERALD_400);

        detailsCard = new GlassPanel(GlassPanel.Style.MORPHIC, ThemeManager.RADIUS_3XL, null) {
            @Override
            public void doLayout() {
                layoutCard(this);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // divide-y divide-white/5: a line 12px (pt-3) above each row after the first
                g.setColor(DIVIDER);
                for (DetailRow row : new DetailRow[]{timeRow, venueRow, departmentRow, seatsRow}) {
                    g.fillRect(row.getX(), row.getY() - 13, row.getWidth(), 1);
                }
            }
        };
        detailsCard.setLayout(null);
        cardTitle = new JLabel("Event Details & Timing");
        cardTitle.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 20));
        cardTitle.setForeground(Color.WHITE);
        dateRow = new DetailRow("calendar", "Date", false);
        timeRow = new DetailRow("clock", "Timing", false);
        venueRow = new DetailRow("map-pin", "Venue", false);
        departmentRow = new DetailRow("users", "Department", false);
        seatsRow = new DetailRow("circle-check", "Seat Availability", true);

        selectSeatsBtn = new StyledButton("Select Seats →", StyledButton.ButtonStyle.PRIMARY);
        selectSeatsBtn.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 14));
        selectSeatsBtn.setCornerRadius(ThemeManager.RADIUS_2XL);
        selectSeatsBtn.setGlow(true);
        selectSeatsBtn.addActionListener(e -> {
            if (currentEvent != null) {
                navController.startSeatSelection(currentEvent);
            }
        });

        detailsCard.add(cardTitle);
        detailsCard.add(dateRow);
        detailsCard.add(timeRow);
        detailsCard.add(venueRow);
        detailsCard.add(departmentRow);
        detailsCard.add(seatsRow);
        detailsCard.add(selectSeatsBtn);

        footer = new JLabel("Campus Event Portal • Instant Digital Pass Delivery");
        footer.setFont(ThemeManager.font(ThemeManager.Weight.REGULAR, 12));
        footer.setForeground(ThemeManager.TEXT_MUTED);

        backdrop.add(backButton);
        backdrop.add(categoryTag);
        backdrop.add(featuredTag);
        backdrop.add(titleText);
        backdrop.add(descriptionText);
        backdrop.add(hallBox);
        backdrop.add(departmentBox);
        backdrop.add(priceBox);
        backdrop.add(detailsCard);
        backdrop.add(footer);

        add(EventGrid.scroll(backdrop), BorderLayout.CENTER);
    }

    public void loadEvent(Event event) {
        this.currentEvent = event;
        if (event == null) {
            return;
        }
        backdrop.setUrl(EventVisuals.imageFor(event));
        RemoteImages.get(EventVisuals.imageFor(event), backdrop::repaint);
        backdrop.remove(categoryTag);
        // The web's details page always shows a blue category tag next to an amber FEATURED tag
        categoryTag = categoryTag(EventVisuals.tagLabel(event.getCategory()), ThemeManager.BLUE_600);
        backdrop.add(categoryTag, 1);
        featuredTag.setVisible(true);

        titleText.setText(event.getTitle());
        descriptionText.setText(event.getDescription());

        String department = event.getOrganizerName() != null && !event.getOrganizerName().isBlank()
                ? event.getOrganizerName() : "College Committee";
        String price = EventVisuals.priceLabel(event.getTicketPrice());
        hallBox.setValue(event.getSeminarHall());
        departmentBox.setValue(department);
        priceBox.setValue(price);

        dateRow.setValue(event.getDate() != null ? event.getDate().format(DATE) : "");
        String timing = event.getStartTime() != null ? event.getStartTime().format(TIME) : "";
        if (event.getEndTime() != null) {
            timing += " - " + event.getEndTime().format(TIME);
        }
        timeRow.setValue(timing);
        venueRow.setValue(event.getSeminarHall());
        departmentRow.setValue(department);
        seatsRow.setValue(event.getAvailableSeats() + " of " + event.getTotalCapacity() + " seats open");

        if (event.isCancelled()) {
            selectSeatsBtn.setText("Event Cancelled");
            selectSeatsBtn.setEnabled(false);
        } else if (event.isSoldOut()) {
            selectSeatsBtn.setText("Sold Out");
            selectSeatsBtn.setEnabled(false);
        } else {
            selectSeatsBtn.setText("Select Seats →");
            selectSeatsBtn.setEnabled(true);
        }

        backdrop.revalidate();
        backdrop.repaint();
    }

    /** Re-reads the event, since seat counts change after bookings. */
    @Override
    public void onScreenDisplayed() {
        Event shown = currentEvent;
        if (shown == null || shown.getId() == null) {
            return;
        }
        if (refresher != null && !refresher.isDone()) {
            refresher.cancel(true);
        }
        refresher = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{eventService.getEventById(shown.getId())};
            }

            @Override
            protected void done() {
                if (isCancelled() || currentEvent == null || !shown.getId().equals(currentEvent.getId())) {
                    return;
                }
                try {
                    Object[] result = get();
                    loadEvent(result[0] != null ? (Event) result[0] : currentEvent);
                } catch (Exception e) {
                    // keep showing the event as it was passed in
                }
            }
        };
        refresher.execute();
    }

    /** "text-[10px] font-extrabold px-3 py-1 rounded-md tracking-wider" on the category color. */
    private Tag categoryTag(String text, Color color) {
        Tag tag = new Tag(text, color, Color.WHITE, 6, tagFont);
        tag.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return tag;
    }

    // ---------------------------------------------------------------------------------------

    private int leftHeight(int leftW) {
        return TAG_H + 12 + titleText.heightForWidth(leftW) + 16
                + descriptionText.heightForWidth(leftW) + 24 + BOX_H;
    }

    private void layoutCard(JComponent card) {
        int w = card.getWidth();
        int inner = w - 64;                                    // p-8
        int y = 32;
        cardTitle.setBounds(32, y, inner, 28);                 // text-xl, mb-4
        y += 28 + 16;
        DetailRow[] rows = {dateRow, timeRow, venueRow, departmentRow, seatsRow};
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) {
                y += 14 + 1;                                   // space-y-3.5, divide-y
            }
            y += i == 0 ? 8 : 12;                              // pt-2 / pt-3
            rows[i].setBounds(32, y, inner, 32);
            y += 32;
        }
        y += 32;                                               // mb-8
        selectSeatsBtn.setBounds(32 - 8, y - 2, inner + 16, 52 + 14);
    }

    /** Divider lines between the detail rows (border-top white/5). */
    private static final Color DIVIDER = new Color(255, 255, 255, 13);

    /** The photo backdrop that also lays out the page (flex-col justify-between, grid in the middle). */
    private final class Backdrop extends PhotoPanel implements Scrollable {

        Backdrop() {
            super(EventVisuals.AUDITORIUM_STAGE, 0, Overlay.SIDES);
            setLayout(null);
        }

        private int viewWidth() {
            Container parent = getParent();
            if (parent instanceof JViewport && parent.getWidth() > 0) {
                return parent.getWidth();
            }
            return getWidth() > 0 ? getWidth() : 1400;
        }

        private int contentWidth(int width) {
            return Math.max(300, Math.min(MAX_W, width - 2 * PAD));
        }

        private boolean stacked(int cw) {
            return cw < 860;
        }

        private int leftWidth(int cw) {
            return stacked(cw) ? cw : cw - CARD_W - COL_GAP;
        }

        private int middleHeight(int cw) {
            int left = leftHeight(leftWidth(cw));
            return stacked(cw) ? left + COL_GAP + CARD_H : Math.max(left, CARD_H);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = viewWidth();
            return new Dimension(width, TOP + middleHeight(contentWidth(width)) + BOTTOM);
        }

        @Override
        public void doLayout() {
            int w = getWidth();
            int h = getHeight();
            int cw = contentWidth(w);
            int x0 = (w - cw) / 2;

            backButton.setBounds(x0, PAD, backButton.getPreferredSize().width, 34);

            int middleH = middleHeight(cw);
            int middleY = TOP + Math.max(0, (h - TOP - BOTTOM - middleH) / 2);   // my-auto
            int leftW = leftWidth(cw);
            int leftH = leftHeight(leftW);
            int y = stacked(cw) ? middleY : middleY + (middleH - leftH) / 2;     // items-center

            Dimension ct = categoryTag.getPreferredSize();
            categoryTag.setBounds(x0, y, ct.width, TAG_H);
            Dimension ft = featuredTag.getPreferredSize();
            featuredTag.setBounds(x0 + ct.width + 8, y, ft.width, TAG_H);
            y += TAG_H + 12;
            int titleH = titleText.heightForWidth(leftW);
            titleText.setBounds(x0, y, leftW, titleH);
            y += titleH + 16;
            int descH = descriptionText.heightForWidth(leftW);
            descriptionText.setBounds(x0, y, leftW, descH);
            y += descH + 24;
            int boxesW = Math.min(512, leftW);                // max-w-lg, grid-cols-3 gap-4
            int boxW = (boxesW - 32) / 3;
            hallBox.setBounds(x0, y, boxW, BOX_H);
            departmentBox.setBounds(x0 + boxW + 16, y, boxW, BOX_H);
            priceBox.setBounds(x0 + 2 * (boxW + 16), y, boxW, BOX_H);

            if (stacked(cw)) {
                detailsCard.setBounds(x0, middleY + leftH + COL_GAP, cw, CARD_H);
            } else {
                detailsCard.setBounds(x0 + leftW + COL_GAP, middleY + (middleH - CARD_H) / 2, CARD_W, CARD_H);
            }

            footer.setBounds(x0, h - PAD - 16, cw, 16);
        }

        @Override
        protected void paintChildren(Graphics g) {
            paintCardShadow(g);
            super.paintChildren(g);
        }

        /** shadow-2xl under the glass card: 0 25px 50px -12px rgba(0,0,0,.25), plus the glass-morphic shadow. */
        private void paintCardShadow(Graphics g) {
            Rectangle r = detailsCard.getBounds();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 10; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 12));
                int s = i * 3;
                g2.fillRoundRect(r.x - s / 2, r.y + 14 - s / 2, r.width + s, r.height + s,
                        48 + s, 48 + s);
            }
            g2.dispose();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            // min-h-[calc(100vh-75px)]: fill the window, scroll only when the content is taller
            return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
        }
    }

    /** "bg-[#121522]/80 border-[#212636] rounded-xl p-3": uppercase gray label over a bold value. */
    private static final class InfoBox extends JComponent {
        private final String label;
        private final Color valueColor;
        private String value = "";

        InfoBox(String label, Color valueColor) {
            this.label = label;
            this.valueColor = valueColor;
        }

        void setValue(String value) {
            this.value = value == null ? "" : value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            EventGrid.applyTextHints(g2);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_XL * 2;
            g2.setColor(new Color(18, 21, 34, 204));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(new Color(33, 38, 54));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            Font labelFont = EventGrid.tracked(ThemeManager.font(ThemeManager.Weight.REGULAR, 10), 0.05f);
            Font valueFont = ThemeManager.font(ThemeManager.Weight.BOLD, 12);
            drawLine(g2, label, labelFont, ThemeManager.TEXT_MUTED, 12, 12, 15, w - 24);
            drawLine(g2, value, valueFont, valueColor, 12, 27, 16, w - 24);
            g2.dispose();
        }
    }

    /** A detail row: tinted 32px icon box, gray label and bold value ("Date / Dec 10, 2024"). */
    private static final class DetailRow extends JComponent {
        private final String icon;
        private final String label;
        private final boolean success;
        private String value = "";

        DetailRow(String icon, String label, boolean success) {
            this.icon = icon;
            this.label = label;
            this.success = success;
        }

        void setValue(String value) {
            this.value = value == null ? "" : value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            EventGrid.applyTextHints(g2);
            Color accent = success ? new Color(16, 185, 129) : ThemeManager.BRAND_RED;
            Color iconColor = success ? ThemeManager.EMERALD_400 : ThemeManager.BRAND_RED;
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 38));
            g2.fillRoundRect(0, 0, 31, 31, 16, 16);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 77));
            g2.drawRoundRect(0, 0, 31, 31, 16, 16);
            Icons.get(icon, 16, iconColor).paintIcon(this, g2, 8, 8);

            int x = 32 + 12;                                   // gap-3
            int textW = getWidth() - x;
            // both lines inherit the list's text-xs 16px line height
            drawLine(g2, label, ThemeManager.font(ThemeManager.Weight.REGULAR, 10), ThemeManager.TEXT_SECONDARY,
                    x, 0, 16, textW);
            drawLine(g2, value, ThemeManager.font(ThemeManager.Weight.BOLD, 12),
                    success ? ThemeManager.EMERALD_400 : Color.WHITE, x, 16, 16, textW);
            g2.dispose();
        }
    }

    /** One line of text in a CSS line box (half-leading above and below), cut with "..." if too wide. */
    private static void drawLine(Graphics2D g2, String text, Font font, Color color, int x, int top, int lineHeight, int maxW) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        String shown = text;
        if (fm.stringWidth(shown) > maxW) {
            while (!shown.isEmpty() && fm.stringWidth(shown + "...") > maxW) {
                shown = shown.substring(0, shown.length() - 1);
            }
            shown = shown.trim() + "...";
        }
        g2.setColor(color);
        int baseline = top + (lineHeight - (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();
        g2.drawString(shown, x, baseline);
    }
}
