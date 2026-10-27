package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.model.Seat;
import com.seminarhall.service.SeatService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.BookingFlowParts;
import com.seminarhall.ui.components.BookingFlowParts.Card;
import com.seminarhall.ui.components.BookingFlowParts.FlowButton;
import com.seminarhall.ui.components.BookingFlowParts.Page;
import com.seminarhall.ui.components.BookingFlowParts.Pill;
import com.seminarhall.ui.components.BookingFlowParts.Row;
import com.seminarhall.ui.components.BookingFlowParts.Stack;
import com.seminarhall.ui.components.BookingFlowParts.Text;
import com.seminarhall.ui.components.BookingFlowParts.Wrap;
import com.seminarhall.ui.components.BookingFlowParts.WrapText;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.PhotoPanel;
import com.seminarhall.ui.components.SeatMapPanel;
import com.seminarhall.ui.components.StepIndicatorBar;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.List;
import java.util.Map;

/**
 * The web app's SeatSelectionPage: header with the back link and the booking steps, the auditorium
 * map card (stage arc, seats, legend) and the summary card on the right with the selected seats,
 * the price breakdown and "Proceed to Review".
 */
public class SeatSelectionPanel extends JPanel {
    /** Sidebar: w-[360px] with p-6 and a 1px border. */
    private static final int SIDEBAR_W = 360;
    private static final int SIDEBAR_INNER = SIDEBAR_W - 48 - 2;

    private final NavigationController navController;
    private final SeatService seatService;
    private Event currentEvent;
    private int loadRequest;

    private Page page;
    private StepIndicatorBar stepBar;
    private SeatMapPanel seatMapPanel;
    private Text headerTitleLbl;
    private Text headerSubLbl;

    private PhotoPanel thumb;
    private WrapText eventTitleLbl;
    private Text eventDateLbl;
    private Text eventHallLbl;
    private Text selectedCountLbl;
    private Text clearLink;
    private Card emptyHint;
    private Wrap selectedSeatsChipsPanel;
    private Text ticketPriceLbl;
    private Text seatCountLbl;
    private Text subtotalLbl;
    private FlowButton continueBtn;
    private Stack sidebarContent;

    public SeatSelectionPanel(NavigationController navController) {
        this.navController = navController;
        this.seatService = ServiceFactory.getSeatService();

        setName(NavigationController.SCREEN_SEAT_SELECTION);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // max-w-7xl mx-auto px-8 py-8
        Stack content = new Stack();
        content.row(buildHeader(), 0);
        content.row(buildBody(), 32);

        page = new Page(content, 1280, 32, 32, 32);
        add(page.scroller(), BorderLayout.CENTER);
    }

    /** Back link, event title, "hall · date" and the steps, over a border-b border-white/5. */
    private JComponent buildHeader() {
        Stack left = new Stack();
        left.row(BookingFlowParts.backLink("← Back to Event Details", navController::goBack), 0, Stack.Align.LEFT);
        headerTitleLbl = BookingFlowParts.pageTitle("");
        left.row(headerTitleLbl, 6);
        headerSubLbl = new Text("", Weight.REGULAR, 12, ThemeManager.TEXT_SECONDARY, 16);
        left.row(headerSubLbl, 0);

        stepBar = new StepIndicatorBar(1); // Step 1: Select Seats
        Row header = new Row(Row.VAlign.CENTER);
        header.grow(left, 0, 1);
        header.cell(stepBar, 16);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BookingFlowParts.DIVIDER), new EmptyBorder(0, 0, 16, 0)));
        return header;
    }

    /** grid-cols-[1fr_360px] gap-8 items-start */
    private JComponent buildBody() {
        // bg-[#10131d] border-[#1d2232] rounded-3xl p-10; the stage component keeps the top padding for its glow.
        // Near the 1000px minimum window width the side padding gives way so the seat rows are never cut.
        Card mapCard = new Card(ThemeManager.RADIUS_3XL, 0, 40, 40, 40) {
            @Override
            public Insets getInsets() {
                Insets in = super.getInsets();
                if (seatMapPanel == null || getWidth() == 0) return in;
                int side = Math.max(16, Math.min(40, (getWidth() - 2 - seatMapPanel.seatsWidth()) / 2)) + 1;
                return new Insets(in.top, side, in.bottom, side);
            }
        };
        seatMapPanel = new SeatMapPanel(this::onSeatSelectionChanged);
        mapCard.add(seatMapPanel, BorderLayout.CENTER);

        Card sidebar = new Card(ThemeManager.RADIUS_3XL, 24);
        sidebarContent = buildSidebar();
        sidebar.add(sidebarContent, BorderLayout.CENTER);

        Row body = new Row(Row.VAlign.TOP);
        body.grow(mapCard, 0, 1);
        body.fixed(sidebar, 32, SIDEBAR_W);
        return body;
    }

    private Stack buildSidebar() {
        // Event thumbnail, title, date and hall (flex gap-3 mb-5)
        thumb = new PhotoPanel(EventVisuals.AUDITORIUM_STAGE, ThemeManager.RADIUS_XL, PhotoPanel.Overlay.NONE);
        thumb.setPreferredSize(new Dimension(64, 64));
        eventTitleLbl = new WrapText(Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 19, SIDEBAR_INNER - 64 - 12);
        eventDateLbl = BookingFlowParts.iconLine("calendar", ThemeManager.BRAND_RED, "", 11, ThemeManager.TEXT_SECONDARY, 16);
        eventHallLbl = BookingFlowParts.iconLine("map-pin", ThemeManager.BRAND_RED, "", 11, ThemeManager.TEXT_SECONDARY, 16);
        Stack eventTexts = new Stack();
        eventTexts.row(eventTitleLbl, 0);
        eventTexts.row(eventDateLbl, 4);
        eventTexts.row(eventHallLbl, 0);
        Row eventRow = new Row(Row.VAlign.TOP);
        eventRow.fixed(thumb, 0, 64);
        eventRow.grow(eventTexts, 12, 1);

        // "Selected Seats (n)" and the Clear link
        selectedCountLbl = new Text("Selected Seats (0)", Weight.REGULAR, 12, ThemeManager.TEXT_SECONDARY, 16);
        clearLink = new Text("Clear", Weight.REGULAR, 10, ThemeManager.BRAND_RED, 15);
        clearLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Font plain = clearLink.getFont();
        clearLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seatMapPanel.clearSelections();
                onSeatSelectionChanged();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                clearLink.setFont(plain.deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                clearLink.setFont(plain);
            }
        });
        Row selectedHeader = new Row(Row.VAlign.CENTER);
        selectedHeader.grow(selectedCountLbl, 0, 1);
        selectedHeader.cell(clearLink, 8);

        // empty state: p-4 rounded-xl bg-[#090b11] border border-dashed border-[#242938]
        emptyHint = new Card(ThemeManager.RADIUS_XL, 16).colors(new Color(9, 11, 17), new Color(36, 41, 56)).dashed(true);
        Text hint = new Text("Click on the seats map to select your seats", Weight.REGULAR, 12, ThemeManager.TEXT_MUTED, 16);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        emptyHint.add(hint, BorderLayout.CENTER);
        selectedSeatsChipsPanel = new Wrap(SIDEBAR_INNER, 8);

        // price breakdown: border-t pt-4 space-y-2 text-xs text-gray-300
        ticketPriceLbl = value("Free", ThemeManager.TEXT_PRIMARY);
        seatCountLbl = value("0", ThemeManager.TEXT_PRIMARY);
        subtotalLbl = new Text("₹0", Weight.BOLD, 14, ThemeManager.BRAND_RED, 20);
        Stack prices = new Stack();
        prices.row(BookingFlowParts.rule(BookingFlowParts.CARD_BORDER), 0);
        prices.row(priceRow("Ticket Price (per seat)", ticketPriceLbl), 16);
        prices.row(priceRow("Number of Seats", seatCountLbl), 8);
        prices.row(priceRow("Campus Tech Fee", value("Free", ThemeManager.EMERALD_400)), 8);
        prices.row(BookingFlowParts.rule(BookingFlowParts.CARD_BORDER), 8);
        Row totalRow = new Row(Row.VAlign.TOP);
        totalRow.grow(new Text("Total Amount", Weight.BOLD, 14, ThemeManager.TEXT_PRIMARY, 20), 0, 1);
        totalRow.cell(subtotalLbl, 8);
        prices.row(totalRow, 8);

        // glow-primary w-full py-4 rounded-2xl text-sm font-bold
        continueBtn = new FlowButton("Proceed to Review (0 Seats)", 14, 16, 24, ThemeManager.RADIUS_2XL, 16);
        continueBtn.setEnabled(false);
        continueBtn.addActionListener(e -> {
            List<Seat> sel = seatMapPanel.getSelectedSeats();
            if (currentEvent != null && !sel.isEmpty()) {
                navController.proceedToBookingSummary(currentEvent, sel);
            }
        });

        Stack sidebar = new Stack();
        sidebar.row(eventRow, 0);
        sidebar.row(selectedHeader, 20);
        sidebar.row(emptyHint, 8);
        sidebar.row(selectedSeatsChipsPanel, 8);
        sidebar.row(prices, 24);
        sidebar.row(continueBtn, 24);
        return sidebar;
    }

    private static Text value(String text, Color color) {
        return new Text(text, Weight.SEMIBOLD, 12, color, 16);
    }

    private static Row priceRow(String label, Text value) {
        Row row = new Row(Row.VAlign.TOP);
        row.grow(new Text(label, Weight.REGULAR, 12, ThemeManager.TEXT_GRAY_300, 16), 0, 1);
        row.cell(value, 8);
        return row;
    }

    /** bg-[#1a1e2d] border-[#2a3046] text-xs font-bold px-3 py-1 rounded-lg, with a ✕ that unselects the seat. */
    private JComponent seatChip(Seat seat) {
        Pill chip = new Pill(ThemeManager.RADIUS_LG, 4, 12, BookingFlowParts.CHIP_BG, BookingFlowParts.CHIP_BORDER);
        chip.add(new Text(seat.getSeatIdentifier(), Weight.BOLD, 12, ThemeManager.TEXT_PRIMARY, 16));
        chip.add(Box.createHorizontalStrut(6));
        Icon x = Icons.get("x", 13, ThemeManager.TEXT_SECONDARY);
        Icon xHover = Icons.get("x", 13, ThemeManager.TEXT_PRIMARY);
        JLabel remove = new JLabel(x);
        remove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        remove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seatMapPanel.deselect(seat);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                remove.setIcon(xHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                remove.setIcon(x);
            }
        });
        chip.add(remove);
        return chip;
    }

    public void loadEvent(Event event) {
        this.currentEvent = event;
        if (event == null) return;

        headerTitleLbl.setText(event.getTitle());
        headerSubLbl.setText(event.getSeminarHall() + " · " + BookingFlowParts.date(event));
        thumb.setUrl(EventVisuals.imageFor(event));
        eventTitleLbl.setText(event.getTitle());
        eventDateLbl.setText(BookingFlowParts.date(event));
        eventHallLbl.setText(event.getSeminarHall());
        seatMapPanel.showMessage("Loading seat map...");
        onSeatSelectionChanged();
        page.scrollToTop();

        int request = ++loadRequest;
        SwingWorker<List<Seat>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Seat> doInBackground() throws Exception {
                return seatService.getSeatsForEvent(event.getId());
            }

            @Override
            protected void done() {
                if (request != loadRequest) {
                    return; // another event was opened meanwhile
                }
                try {
                    seatMapPanel.setSeats(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    seatMapPanel.showMessage("Could not load the seat map: " + cause.getMessage());
                }
                onSeatSelectionChanged();
            }
        };
        worker.execute();
    }

    private void onSeatSelectionChanged() {
        List<Seat> selected = seatMapPanel.getSelectedSeats();
        int count = selected.size();
        double total = selected.stream().mapToDouble(Seat::getPrice).sum();

        selectedCountLbl.setText("Selected Seats (" + count + ")");
        clearLink.setVisible(count > 0);
        selectedSeatsChipsPanel.removeAll();
        for (Seat s : selected) {
            selectedSeatsChipsPanel.add(seatChip(s));
        }
        emptyHint.setVisible(count == 0);
        selectedSeatsChipsPanel.setVisible(count > 0);

        ticketPriceLbl.setText(EventVisuals.priceLabel(currentEvent != null ? currentEvent.getTicketPrice() : 0));
        seatCountLbl.setText(String.valueOf(count));
        subtotalLbl.setText(BookingFlowParts.rupees(total));

        continueBtn.setText("Proceed to Review (" + count + " Seats)");
        continueBtn.setEnabled(count > 0);
        sidebarContent.revalidate();
        sidebarContent.repaint();
        page.revalidate();
        page.repaint();
    }
}
