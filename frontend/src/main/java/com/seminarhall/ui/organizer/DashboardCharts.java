package com.seminarhall.ui.organizer;

import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The two analytics charts of the Faculty Console dashboard (web OrganizerDashboardPage). */
final class DashboardCharts {

    private DashboardCharts() {
    }

    /**
     * "Event Bookings (Last 6 Months)": the "h-36 flex items-end justify-between px-4 pb-2 border-b" area
     * with "w-10 rounded-t-lg glow-primary" bars over "text-[11px] text-gray-400 font-mono" month labels.
     */
    static final class BookingsBarChart extends JComponent {
        private static final int HEIGHT = 144;       // h-36
        private static final int BAR_WIDTH = 40;     // w-10
        private static final int LABEL_LINE = 16;
        private static final int MAX_BAR = 92;       // leaves room for the count above the tallest bar

        private List<YearMonth> months = lastSixMonths();
        private int[] counts = new int[6];

        BookingsBarChart() {
            setToolTipText("");
        }

        static List<YearMonth> lastSixMonths() {
            List<YearMonth> months = new ArrayList<>();
            YearMonth now = YearMonth.now();
            for (int i = 5; i >= 0; i--) {
                months.add(now.minusMonths(i));
            }
            return months;
        }

        void setData(List<YearMonth> months, int[] counts) {
            this.months = months;
            this.counts = counts;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, HEIGHT);
        }

        private int barX(int i) {
            int n = counts.length;
            double step = n > 1 ? (getWidth() - 32 - BAR_WIDTH) / (double) (n - 1) : 0;
            return (int) Math.round(16 + i * step);
        }

        private int max() {
            int max = 0;
            for (int c : counts) max = Math.max(max, c);
            return max;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            for (int i = 0; i < counts.length; i++) {
                int x = barX(i);
                if (e.getX() >= x && e.getX() <= x + BAR_WIDTH) {
                    YearMonth m = months.get(i);
                    return m.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + m.getYear()
                            + ": " + counts[i] + (counts[i] == 1 ? " booking" : " bookings");
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int h = getHeight();

            // border-b border-[#1d2232]
            g2.setColor(ConsoleUi.CARD_BORDER);
            g2.fillRect(0, h - 1, getWidth(), 1);

            int labelBottom = h - 1 - 8;                      // pb-2
            int barBottom = labelBottom - LABEL_LINE - 8;     // gap-2
            int max = max();
            Font monoFont = ConsoleUi.mono(false, 11);

            for (int i = 0; i < counts.length; i++) {
                int x = barX(i);
                int barH = counts[i] == 0 || max == 0 ? 0
                        : Math.max(8, (int) Math.round(counts[i] / (double) max * MAX_BAR));
                if (barH > 0) {
                    paintBar(g2, x, barBottom - barH, barH);
                    g2.setFont(monoFont);
                    g2.setColor(ThemeManager.TEXT_GRAY_300);
                    String value = String.valueOf(counts[i]);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(value, x + (BAR_WIDTH - fm.stringWidth(value)) / 2, barBottom - barH - 6);
                } else {
                    // empty month: a faint stub keeps the slot readable
                    g2.setColor(new Color(255, 255, 255, 14));
                    g2.fillRoundRect(x, barBottom - 3, BAR_WIDTH, 3, 3, 3);
                }

                String month = months.get(i).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                g2.setFont(monoFont);
                g2.setColor(ThemeManager.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                int baseline = labelBottom - LABEL_LINE + (LABEL_LINE + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(month, x + (BAR_WIDTH - fm.stringWidth(month)) / 2, baseline);
            }
            g2.dispose();
        }

        private void paintBar(Graphics2D g2, int x, int y, int h) {
            // glow-primary shadow: 0 8px 24px -4px rgba(255,56,46,.55)
            for (int spread = 8; spread >= 1; spread--) {
                int alpha = 16 * (9 - spread) / 8;
                g2.setColor(new Color(255, 56, 46, alpha));
                g2.fill(new RoundRectangle2D.Float(x - spread + 4, y + 6 - spread / 2f + 4,
                        BAR_WIDTH + spread * 2 - 8, h + spread - 8, 16 + spread, 16 + spread));
            }
            // rounded-t-lg: 8px top corners, square bottom
            Area shape = new Area(new RoundRectangle2D.Float(x, y, BAR_WIDTH, h, 16, 16));
            shape.add(new Area(new Rectangle2D.Float(x, y + Math.min(8, h / 2f), BAR_WIDTH, h - Math.min(8, h / 2f))));
            g2.setPaint(new GradientPaint(x, y, ThemeManager.BRAND_RED, x + BAR_WIDTH, y + Math.min(h, BAR_WIDTH),
                    ThemeManager.BRAND_ORANGE));
            g2.fill(shape);
            // inset 0 1px 1px rgba(255,255,255,.35)
            g2.setColor(new Color(255, 255, 255, 80));
            g2.drawLine(x + 6, y + 1, x + BAR_WIDTH - 7, y + 1);
        }
    }

    /**
     * "Events by Category": a "w-24 h-24 rounded-full border-8" ring split by category with the largest
     * share in the middle, and a "space-y-1.5 text-xs text-gray-300" legend of "● Name (N%)" rows,
     * spread with "justify-around".
     */
    static final class CategoryDonutChart extends JComponent {
        private static final int RING = 96;
        private static final int STROKE = 8;

        static final class Slice {
            final String name;
            final int count;
            final Color color;
            int percent;

            Slice(String name, int count, Color color) {
                this.name = name;
                this.count = count;
                this.color = color;
            }
        }

        private List<Slice> slices = new ArrayList<>();
        private int total;
        private final Font legendFont = ConsoleUi.font(Weight.REGULAR, 12);

        void setSlices(List<Slice> slices) {
            this.slices = slices;
            this.total = slices.stream().mapToInt(s -> s.count).sum();
            assignPercents();
            revalidate();
            repaint();
        }

        /** Whole percentages that add up to 100 (largest remainder). */
        private void assignPercents() {
            if (total == 0) return;
            int sum = 0;
            double[] remainders = new double[slices.size()];
            for (int i = 0; i < slices.size(); i++) {
                double exact = slices.get(i).count * 100.0 / total;
                slices.get(i).percent = (int) Math.floor(exact);
                remainders[i] = exact - Math.floor(exact);
                sum += slices.get(i).percent;
            }
            while (sum < 100) {
                int best = 0;
                for (int i = 1; i < remainders.length; i++) {
                    if (remainders[i] > remainders[best]) best = i;
                }
                slices.get(best).percent++;
                remainders[best] = -1;
                sum++;
            }
        }

        private List<String> legendLines() {
            List<String> lines = new ArrayList<>();
            for (Slice s : slices) {
                lines.add(s.name + " (" + s.percent + "%)");
            }
            if (lines.isEmpty()) {
                lines.add("No events yet");
            }
            return lines;
        }

        private int legendHeight() {
            int n = legendLines().size();
            return n * 16 + (n - 1) * 6;
        }

        private int legendWidth() {
            FontMetrics fm = getFontMetrics(legendFont);
            int w = 0;
            for (String line : legendLines()) {
                w = Math.max(w, fm.stringWidth(line));
            }
            return w + (slices.isEmpty() ? 0 : 17);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(RING + 24 + legendWidth(), Math.max(RING, legendHeight()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int legendW = legendWidth();
            int free = Math.max(0, getWidth() - RING - legendW);
            int ringX = free / 4;                           // justify-around
            int legendX = ringX + RING + free / 2;
            int ringY = (getHeight() - RING) / 2;

            // ring: first slice starts at the top-left diagonal, like the CSS border-top segment
            double r = (RING - STROKE) / 2.0;
            double cx = ringX + RING / 2.0;
            double cy = ringY + RING / 2.0;
            g2.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            if (total == 0) {
                g2.setColor(ConsoleUi.CARD_BORDER);
                g2.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 0, 360, Arc2D.OPEN));
            } else {
                double start = 135;
                for (Slice s : slices) {
                    double extent = -360.0 * s.count / total;
                    g2.setColor(s.color);
                    g2.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, start, extent, Arc2D.OPEN));
                    start += extent;
                }
            }

            // centre: the largest share, "font-black text-sm text-white"
            String centre = slices.isEmpty() ? "0%" : slices.get(0).percent + "%";
            g2.setFont(ConsoleUi.font(Weight.EXTRABOLD, 14));
            g2.setColor(Color.WHITE);
            FontMetrics cfm = g2.getFontMetrics();
            g2.drawString(centre, (float) (cx - cfm.stringWidth(centre) / 2.0),
                    (float) (cy + (cfm.getAscent() - cfm.getDescent()) / 2.0));

            // legend
            g2.setFont(legendFont);
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - legendHeight()) / 2;
            List<String> lines = legendLines();
            for (int i = 0; i < lines.size(); i++) {
                int baseline = y + (16 + fm.getAscent() - fm.getDescent()) / 2;
                int textX = legendX;
                if (!slices.isEmpty()) {
                    g2.setColor(slices.get(i).color);
                    g2.fillOval(legendX, y + 4, 8, 8);
                    textX += 17;
                }
                g2.setColor(ThemeManager.TEXT_GRAY_300);
                g2.drawString(lines.get(i), textX, baseline);
                y += 16 + 6;
            }
            g2.dispose();
        }
    }
}
