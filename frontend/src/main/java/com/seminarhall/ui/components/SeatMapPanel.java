package com.seminarhall.ui.components;

import com.seminarhall.model.Seat;
import com.seminarhall.model.SeatStatus;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The web app's auditorium map: the glowing curved stage arc ("STAGE / SCREEN THIS WAY"),
 * the seat rows with their letter on both sides and a center aisle, and the status legend.
 * Seats come from SeatService; clicking a seat toggles it and calls onSelectionChanged.
 */
public class SeatMapPanel extends JPanel {
    private List<Seat> seats = new ArrayList<>();
    private final List<SeatButton> seatButtons = new ArrayList<>();
    /** Selected seats in the order they were picked (the web lists them in click order). */
    private final Set<SeatButton> selectionOrder = new LinkedHashSet<>();
    private final Runnable onSelectionChanged;

    private final SeatGrid grid = new SeatGrid();
    private final Legend legend = new Legend();
    private final BookingFlowParts.Text message =
            new BookingFlowParts.Text("", Weight.REGULAR, 12, ThemeManager.TEXT_MUTED, 16);

    public SeatMapPanel(Runnable onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
        setOpaque(false);
        setLayout(new BorderLayout());

        BookingFlowParts.Stack stack = new BookingFlowParts.Stack();
        stack.row(new StageArc(), 0);
        // mb-8 under the stage and above the legend; the grid keeps 12px of that for the seat glow
        stack.row(grid, 32 - SeatGrid.PAD, BookingFlowParts.Stack.Align.CENTER);
        stack.row(message, 32, BookingFlowParts.Stack.Align.CENTER);
        stack.row(legend, 32 - SeatGrid.PAD, BookingFlowParts.Stack.Align.CENTER);
        add(stack, BorderLayout.CENTER);
        showMessage("Loading seat map...");
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats != null ? seats : new ArrayList<>();
        rebuildSeatGrid();
    }

    /** Shows a line of text instead of the seats (loading, no seats, load errors). */
    public void showMessage(String text) {
        seatButtons.clear();
        selectionOrder.clear();
        grid.setRows(new ArrayList<>());
        grid.setVisible(false);
        legend.setVisible(false);
        message.setText(text);
        message.setVisible(true);
        revalidate();
        repaint();
    }

    /** Width the seat rows and the legend need, without the room kept for the seat glow. */
    public int seatsWidth() {
        int rows = grid.isVisible() ? grid.getPreferredSize().width - SeatGrid.PAD * 2 : 0;
        int keys = legend.isVisible() ? legend.getPreferredSize().width : 0;
        return Math.max(rows, keys);
    }

    public List<Seat> getSelectedSeats() {
        List<Seat> result = new ArrayList<>();
        for (SeatButton b : selectionOrder) {
            if (b.getSeat().getStatus() == SeatStatus.SELECTED) result.add(b.getSeat());
        }
        for (SeatButton b : seatButtons) {
            if (b.getSeat().getStatus() == SeatStatus.SELECTED && !selectionOrder.contains(b)) result.add(b.getSeat());
        }
        return result;
    }

    public void clearSelections() {
        for (SeatButton sb : seatButtons) {
            if (sb.getSeat().getStatus() == SeatStatus.SELECTED) {
                sb.getSeat().setStatus(SeatStatus.AVAILABLE);
                sb.repaint();
            }
        }
        selectionOrder.clear();
        grid.repaint();
    }

    /** Unselects one seat (the ✕ on a seat chip) and reports the change. */
    public void deselect(Seat seat) {
        for (SeatButton sb : seatButtons) {
            if (sb.getSeat() == seat || sb.getSeat().equals(seat)) {
                if (sb.getSeat().getStatus() == SeatStatus.SELECTED) {
                    sb.getSeat().setStatus(SeatStatus.AVAILABLE);
                }
                selectionOrder.remove(sb);
                sb.repaint();
            }
        }
        grid.repaint();
        if (onSelectionChanged != null) onSelectionChanged.run();
    }

    private void onToggled(SeatButton button) {
        if (button.getSeat().getStatus() == SeatStatus.SELECTED) {
            selectionOrder.add(button);
        } else {
            selectionOrder.remove(button);
        }
        grid.repaint();
        if (onSelectionChanged != null) onSelectionChanged.run();
    }

    private void rebuildSeatGrid() {
        seatButtons.clear();
        selectionOrder.clear();

        if (seats.isEmpty()) {
            showMessage("No seating data available for this hall.");
            return;
        }

        // Rows A, B, C ... (AA after Z), seats by number
        Map<String, List<Seat>> rowsMap = new TreeMap<>(Comparator.comparingInt(String::length).thenComparing(s -> s));
        for (Seat s : seats) {
            rowsMap.computeIfAbsent(s.getRowLabel(), k -> new ArrayList<>()).add(s);
        }

        List<GridRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<Seat>> entry : rowsMap.entrySet()) {
            List<Seat> rowSeats = entry.getValue();
            rowSeats.sort(Comparator.comparingInt(Seat::getSeatNumber));
            GridRow row = new GridRow(entry.getKey());
            // center aisle: seats 1-5 | 6-10 like the web (rows of four or fewer have no aisle)
            int aisleIndex = rowSeats.size() > 4 ? (rowSeats.size() + 1) / 2 : rowSeats.size();
            for (int i = 0; i < rowSeats.size(); i++) {
                SeatButton[] self = new SeatButton[1];
                self[0] = new SeatButton(rowSeats.get(i), () -> onToggled(self[0]));
                seatButtons.add(self[0]);
                if (self[0].getSeat().getStatus() == SeatStatus.SELECTED) selectionOrder.add(self[0]);
                (i < aisleIndex ? row.left : row.right).add(self[0]);
            }
            rows.add(row);
        }

        grid.setRows(rows);
        message.setVisible(false);
        grid.setVisible(true);
        legend.setVisible(true);
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------ stage

    /**
     * .stage-arc: a 16px-high box, rounded-t-full, with a 4px #ff382e top border and
     * box-shadow 0 -8px 30px rgba(255,56,46,.65), 0 0 15px rgba(255,104,53,.45);
     * w-3/4 max-w-md min-w-[300px]; then the label in text-[10px] font-black tracking-[0.3em] text-gray-400.
     * The component starts 40px higher (the card's p-10) so the glow above the arc has room.
     */
    private static class StageArc extends JComponent {
        static final int GLOW_ROOM = 40;
        private static final Font LABEL = BookingFlowParts.tracked(ThemeManager.font(Weight.EXTRABOLD, 10), 0.3f);

        StageArc() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, GLOW_ROOM + 16 + 6 + 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            BookingFlowParts.hints(g2);
            int w = getWidth();
            int arcW = (int) Math.max(300, Math.min(448, w * 0.75));
            float x = (w - arcW) / 2f;
            float y = GLOW_ROOM;

            Area box = new Area(new RoundRectangle2D.Float(x, y, arcW, 32, 32, 32));
            box.intersect(new Area(new Rectangle2D.Float(x, y, arcW, 16)));
            Area inner = new Area(new RoundRectangle2D.Float(x, y + 4, arcW, 24, 32, 24));
            inner.intersect(new Area(new Rectangle2D.Float(x, y + 4, arcW, 12)));
            Area border = new Area(box);
            border.subtract(inner);

            BookingFlowParts.paintShadow(g2, box, new Color(255, 56, 46, 166), 30, 0, -8);
            BookingFlowParts.paintShadow(g2, box, new Color(255, 104, 53, 115), 15, 0, 0);
            g2.setColor(ThemeManager.BRAND_RED);
            g2.fill(border);

            String label = "STAGE / SCREEN THIS WAY";
            g2.setFont(LABEL);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            int top = GLOW_ROOM + 16 + 6;
            g2.drawString(label, (w - fm.stringWidth(label)) / 2, top + (15 - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ grid

    private static final class GridRow {
        final String label;
        final List<SeatButton> left = new ArrayList<>();
        final List<SeatButton> right = new ArrayList<>();

        GridRow(String label) {
            this.label = label;
        }
    }

    /**
     * space-y-2.5 rows; each row: letter (w-4 text-xs font-bold text-gray-500), gap-3, seats 1-5 (gap-2),
     * a w-8 aisle, seats 6-10, the letter again. Selected seats glow (shadow 0 0 12px amber/80).
     */
    private static class SeatGrid extends JPanel {
        static final int CELL = SeatButton.SIZE;
        static final int SEAT_GAP = 8;
        static final int ITEM_GAP = 12;
        static final int LABEL_W = 16;
        static final int AISLE = 32;
        static final int ROW_GAP = 10;
        static final int PAD = 12;
        private static final Font ROW_FONT = ThemeManager.font(Weight.BOLD, 12);

        private List<GridRow> rows = new ArrayList<>();

        SeatGrid() {
            super(null);
            setOpaque(false);
        }

        void setRows(List<GridRow> rows) {
            removeAll();
            this.rows = rows;
            for (GridRow row : rows) {
                row.left.forEach(this::add);
                row.right.forEach(this::add);
            }
            revalidate();
            repaint();
        }

        private static int groupWidth(List<SeatButton> group) {
            return group.isEmpty() ? 0 : group.size() * CELL + (group.size() - 1) * SEAT_GAP;
        }

        private static int rowWidth(GridRow row) {
            int w = LABEL_W * 2 + ITEM_GAP * 2 + groupWidth(row.left);
            if (!row.right.isEmpty()) {
                w += ITEM_GAP + AISLE + ITEM_GAP + groupWidth(row.right);
            }
            return w;
        }

        @Override
        public Dimension getPreferredSize() {
            int w = 0;
            for (GridRow row : rows) w = Math.max(w, rowWidth(row));
            int h = rows.isEmpty() ? 0 : rows.size() * CELL + (rows.size() - 1) * ROW_GAP;
            return new Dimension(w + PAD * 2, h + PAD * 2);
        }

        @Override
        public void doLayout() {
            for (int r = 0; r < rows.size(); r++) {
                GridRow row = rows.get(r);
                int x = (getWidth() - rowWidth(row)) / 2 + LABEL_W + ITEM_GAP;
                int y = PAD + r * (CELL + ROW_GAP);
                x = placeGroup(row.left, x, y);
                if (!row.right.isEmpty()) {
                    placeGroup(row.right, x + ITEM_GAP + AISLE + ITEM_GAP, y);
                }
            }
        }

        private int placeGroup(List<SeatButton> group, int x, int y) {
            int off = (SeatButton.BOX - CELL) / 2;
            for (int i = 0; i < group.size(); i++) {
                group.get(i).setBounds(x + i * (CELL + SEAT_GAP) - off, y - off, SeatButton.BOX, SeatButton.BOX);
            }
            return x + groupWidth(group);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            BookingFlowParts.hints(g2);

            // glow of selected seats, behind the seats
            for (Component c : getComponents()) {
                SeatButton b = (SeatButton) c;
                if (b.getSeat().getStatus() == SeatStatus.SELECTED) {
                    float size = SeatButton.SIZE * 1.05f;
                    float sx = b.getX() + (b.getWidth() - size) / 2f;
                    float sy = b.getY() + (b.getHeight() - size) / 2f;
                    BookingFlowParts.paintShadow(g2, new RoundRectangle2D.Float(sx, sy, size, size, 12.6f, 12.6f),
                            SeatButton.SELECTED_GLOW, 12, 0, 0);
                }
            }

            // row letters on both sides
            g2.setFont(ROW_FONT);
            g2.setColor(ThemeManager.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            for (int r = 0; r < rows.size(); r++) {
                GridRow row = rows.get(r);
                int left = (getWidth() - rowWidth(row)) / 2;
                int right = left + rowWidth(row) - LABEL_W;
                int y = PAD + r * (CELL + ROW_GAP);
                int baseline = y + (CELL - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
                int tw = fm.stringWidth(row.label);
                g2.drawString(row.label, left + (LABEL_W - tw) / 2, baseline);
                g2.drawString(row.label, right + (LABEL_W - tw) / 2, baseline);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ legend

    /** "flex justify-center gap-8 text-xs text-gray-400 border-t border-white/5 pt-4" with w-3.5 h-3.5 rounded swatches. */
    private static class Legend extends JComponent {
        private static final String[] LABELS = {"Available", "Selected", "Booked", "Unavailable"};
        private static final Color[] COLORS = {SeatButton.AVAILABLE, SeatButton.SELECTED, SeatButton.BOOKED, SeatButton.UNAVAILABLE};
        private static final Font FONT = ThemeManager.font(Weight.REGULAR, 12);

        Legend() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(FONT);
            int w = 0;
            for (String label : LABELS) w += 14 + 8 + fm.stringWidth(label);
            return new Dimension(w + 32 * (LABELS.length - 1), 1 + 16 + 16);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            BookingFlowParts.hints(g2);
            g2.setColor(BookingFlowParts.DIVIDER);
            g2.fillRect(0, 0, getWidth(), 1);

            g2.setFont(FONT);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - getPreferredSize().width) / 2;
            int top = 17;
            int baseline = top + (16 - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
            for (int i = 0; i < LABELS.length; i++) {
                g2.setColor(COLORS[i]);
                g2.fill(new RoundRectangle2D.Float(x, top + 1, 14, 14, 8, 8));
                x += 14 + 8;
                g2.setColor(ThemeManager.TEXT_SECONDARY);
                g2.drawString(LABELS[i], x, baseline);
                x += fm.stringWidth(LABELS[i]) + 32;
            }
            g2.dispose();
        }
    }
}
