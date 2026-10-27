package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.BookingFlowParts;
import com.seminarhall.ui.components.BookingFlowParts.Card;
import com.seminarhall.ui.components.BookingFlowParts.DarkButton;
import com.seminarhall.ui.components.BookingFlowParts.FlowButton;
import com.seminarhall.ui.components.BookingFlowParts.Page;
import com.seminarhall.ui.components.BookingFlowParts.Pill;
import com.seminarhall.ui.components.BookingFlowParts.Row;
import com.seminarhall.ui.components.BookingFlowParts.Stack;
import com.seminarhall.ui.components.BookingFlowParts.Text;
import com.seminarhall.ui.components.BookingFlowParts.Wrap;
import com.seminarhall.ui.components.BookingFlowParts.WrapText;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.PhotoPanel;
import com.seminarhall.ui.components.Tag;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * The web app's OrderSummaryPage ("Booking Summary"): the event card with its photo, the selected
 * seats, the price breakdown, "← Modify Seats" and "Proceed to Payment".
 */
public class BookingSummaryPanel extends JPanel {
    /** max-w-3xl (768) with px-8 */
    private static final int CONTENT_W = 768 - 64;
    private static final int HALF_CARD_INNER = (CONTENT_W - 24) / 2 - 50;

    private final NavigationController navController;
    private Event currentEvent;
    private List<Seat> currentSeats;

    private Page page;
    private PhotoPanel photo;
    private JPanel categoryHolder;
    private WrapText eventTitleLbl;
    private Text eventTimeLbl;
    private Text venueLbl;
    private Text seatsTitleLbl;
    private Wrap selectedSeatsChips;
    private Text pricePerSeatLbl;
    private Text seatCountLbl;
    private Text subtotalLbl;
    private Text grandTotalLbl;

    public BookingSummaryPanel(NavigationController navController) {
        this.navController = navController;

        setName(NavigationController.SCREEN_BOOKING_SUMMARY);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        Stack content = new Stack();
        content.row(BookingFlowParts.backLink("← Back to Seat Selection", navController::goBack), 0, Stack.Align.LEFT);
        content.row(BookingFlowParts.pageTitle("Booking Summary"), 8);
        content.row(buildEventCard(), 24);

        // grid sm:grid-cols-2 gap-6
        Row cards = new Row(Row.VAlign.STRETCH);
        cards.grow(buildSeatsCard(), 0, 1);
        cards.grow(buildPriceCard(), 24, 1);
        content.row(cards, 24);

        DarkButton backBtn = new DarkButton("← Modify Seats");
        backBtn.addActionListener(e -> navController.goBack());

        // glow-primary text-sm font-bold px-8 py-3.5 rounded-xl
        FlowButton continueBtn = new FlowButton("Proceed to Payment", 14, 14, 32, ThemeManager.RADIUS_XL, 15);
        continueBtn.addActionListener(e -> {
            if (currentEvent != null && currentSeats != null) {
                navController.proceedToPayment(currentEvent, currentSeats);
            }
        });

        Row buttons = new Row(Row.VAlign.CENTER);
        buttons.cell(backBtn, 0);
        buttons.grow(Box.createHorizontalGlue(), 0, 1);
        buttons.cell(continueBtn, 0);
        content.row(buttons, 24 + 16);

        // max-w-3xl mx-auto px-8 py-10
        page = new Page(content, 768, 32, 40, 40);
        add(page.scroller(), BorderLayout.CENTER);
    }

    /** bg-[#10131d] rounded-3xl p-6 flex gap-5 items-center: photo, category tag, title, date and venue. */
    private JComponent buildEventCard() {
        photo = new PhotoPanel(EventVisuals.AUDITORIUM_STAGE, ThemeManager.RADIUS_2XL, PhotoPanel.Overlay.NONE);
        photo.setPreferredSize(new Dimension(112, 112));

        categoryHolder = new JPanel(new BorderLayout());
        categoryHolder.setOpaque(false);
        eventTitleLbl = new WrapText(Weight.BOLD, 20, ThemeManager.TEXT_PRIMARY, 28, CONTENT_W - 50 - 112 - 20);
        eventTimeLbl = BookingFlowParts.iconLine("calendar", ThemeManager.BRAND_RED, "", 12, ThemeManager.TEXT_GRAY_300, 16);
        venueLbl = BookingFlowParts.iconLine("map-pin", ThemeManager.BRAND_RED, "", 12, ThemeManager.TEXT_GRAY_300, 16);

        Stack info = new Stack();
        info.row(categoryHolder, 0, Stack.Align.LEFT);
        info.row(eventTitleLbl, 6);
        info.row(eventTimeLbl, 8);
        info.row(venueLbl, 4);

        Row row = new Row(Row.VAlign.CENTER);
        row.fixed(photo, 0, 112);
        row.grow(info, 20, 1);

        Card card = new Card(ThemeManager.RADIUS_3XL, 24);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildSeatsCard() {
        seatsTitleLbl = new Text("Selected Seats (0)", Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 20);
        selectedSeatsChips = new Wrap(HALF_CARD_INNER, 8);
        Stack stack = new Stack();
        stack.row(seatsTitleLbl, 0);
        stack.row(selectedSeatsChips, 12);
        Card card = new Card(ThemeManager.RADIUS_3XL, 24);
        card.add(stack, BorderLayout.CENTER);
        return card;
    }

    /** "Price Breakdown": space-y-2 text-xs text-gray-300, total in text-base font-bold. */
    private JComponent buildPriceCard() {
        pricePerSeatLbl = value();
        seatCountLbl = value();
        subtotalLbl = value();
        grandTotalLbl = new Text("₹0", Weight.BOLD, 16, ThemeManager.BRAND_RED, 24);

        Row total = new Row(Row.VAlign.TOP);
        total.grow(new Text("Total Amount", Weight.BOLD, 16, ThemeManager.TEXT_PRIMARY, 24), 0, 1);
        total.cell(grandTotalLbl, 8);

        Stack stack = new Stack();
        stack.row(new Text("Price Breakdown", Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 20), 0);
        stack.row(priceRow("Ticket Price (per seat)", pricePerSeatLbl), 12);
        stack.row(priceRow("Seats Selected", seatCountLbl), 8);
        stack.row(priceRow("Subtotal", subtotalLbl), 8);
        stack.row(BookingFlowParts.rule(BookingFlowParts.CARD_BORDER), 8);
        stack.row(total, 12);
        Card card = new Card(ThemeManager.RADIUS_3XL, 24);
        card.add(stack, BorderLayout.CENTER);
        return card;
    }

    private static Text value() {
        return new Text("", Weight.SEMIBOLD, 12, ThemeManager.TEXT_PRIMARY, 16);
    }

    private static Row priceRow(String label, Text value) {
        Row row = new Row(Row.VAlign.TOP);
        row.grow(new Text(label, Weight.REGULAR, 12, ThemeManager.TEXT_GRAY_300, 16), 0, 1);
        row.cell(value, 8);
        return row;
    }

    /** bg-blue-600/90 text-white font-extrabold text-[9px] px-2.5 py-0.5 rounded tracking-wider */
    private static Tag categoryTag(Event event) {
        Tag tag = new Tag(EventVisuals.tagLabel(event.getCategory()), new Color(37, 99, 235, 230), Color.WHITE, 4,
                BookingFlowParts.tracked(ThemeManager.font(Weight.EXTRABOLD, 9), 0.05f));
        tag.setBorder(new EmptyBorder(2, 10, 2, 10));
        return tag;
    }

    public void loadSummary(Event event, List<Seat> selectedSeats) {
        this.currentEvent = event;
        this.currentSeats = selectedSeats;
        if (event == null || selectedSeats == null) return;

        photo.setUrl(EventVisuals.imageFor(event));
        categoryHolder.removeAll();
        categoryHolder.add(categoryTag(event), BorderLayout.WEST);
        eventTitleLbl.setText(event.getTitle());
        eventTimeLbl.setText(BookingFlowParts.date(event) + " · " + BookingFlowParts.timeRange(event));
        String dept = event.getOrganizerName();
        venueLbl.setText(event.getSeminarHall() + (dept != null && !dept.isBlank() ? " (" + dept + ")" : ""));

        seatsTitleLbl.setText("Selected Seats (" + selectedSeats.size() + ")");
        selectedSeatsChips.removeAll();
        for (Seat s : selectedSeats) {
            // bg-[#1a1e2d] border-[#2a3046] text-xs font-bold px-3 py-1.5 rounded-xl
            Pill chip = new Pill(ThemeManager.RADIUS_XL, 6, 12, BookingFlowParts.CHIP_BG, BookingFlowParts.CHIP_BORDER);
            chip.add(new Text(s.getSeatIdentifier(), Weight.BOLD, 12, ThemeManager.TEXT_PRIMARY, 16));
            selectedSeatsChips.add(chip);
        }

        double total = selectedSeats.stream().mapToDouble(Seat::getPrice).sum();
        pricePerSeatLbl.setText(EventVisuals.priceLabel(event.getTicketPrice()));
        seatCountLbl.setText(String.valueOf(selectedSeats.size()));
        subtotalLbl.setText(BookingFlowParts.rupees(total));
        grandTotalLbl.setText(BookingFlowParts.rupees(total));

        page.revalidate();
        page.repaint();
        page.scrollToTop();
    }
}
