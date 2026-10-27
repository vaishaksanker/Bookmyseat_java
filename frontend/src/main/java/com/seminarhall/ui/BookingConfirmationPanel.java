package com.seminarhall.ui;

import com.seminarhall.model.Booking;
import com.seminarhall.model.BookingStatus;
import com.seminarhall.model.Event;
import com.seminarhall.model.Ticket;
import com.seminarhall.service.BookingService;
import com.seminarhall.service.EventService;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.TicketService;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.TicketPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Booking confirmation, as the web app's TicketConfirmationPage: the glowing check, "Booking Confirmed!",
 * the white digital pass and the Download Pass (PDF) / My Bookings / Explore More Events buttons.
 */
public class BookingConfirmationPanel extends JPanel {
    private static final int COLUMN_WIDTH = TicketPanel.WIDTH;
    /** Room inside the column for the glows the column paints around the check mark and the primary button. */
    private static final int GLOW_PAD_TOP = 32;
    private static final int GLOW_PAD_X = 16;
    private static final int GLOW_PAD_BOTTOM = 24;

    private final NavigationController navController;
    private final BookingService bookingService;
    private final EventService eventService;
    private final TicketService ticketService;

    private Booking currentBooking;
    private int loadSeq;

    private final StatusCircle statusCircle = new StatusCircle();
    private final JLabel headingLbl = new JLabel("Booking Confirmed!", SwingConstants.CENTER);
    private final JLabel subLbl = new JLabel(" ", SwingConstants.CENTER);
    private final TicketPanel ticketPanel = new TicketPanel();
    private final Component ticketGap = Box.createVerticalStrut(32);
    private final StyledButton downloadBtn = new StyledButton("Download Pass (PDF)", StyledButton.ButtonStyle.PRIMARY);
    private final DarkButton myBookingsBtn = new DarkButton("My Bookings");
    private final DarkButton exploreBtn = new DarkButton("Explore More Events");
    private final JPanel buttonRow = new JPanel(new GridLayout(1, 0, 12, 0));
    private Column column;

    public BookingConfirmationPanel(NavigationController navController) {
        this.navController = navController;
        this.bookingService = ServiceFactory.getBookingService();
        this.eventService = ServiceFactory.getEventService();
        this.ticketService = ServiceFactory.getTicketService();

        setName(NavigationController.SCREEN_BOOKING_CONFIRMATION);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        column = new Column();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(GLOW_PAD_TOP, GLOW_PAD_X, GLOW_PAD_BOTTOM, GLOW_PAD_X));

        // w-16 h-16 rounded-full bg-emerald-500/20 border-2 border-emerald-500, mb-3
        statusCircle.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(statusCircle);
        column.add(Box.createVerticalStrut(12));

        // text-3xl font-black text-white mb-1
        headingLbl.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 30));
        headingLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        fixHeight(headingLbl, 36);
        column.add(headingLbl);
        column.add(Box.createVerticalStrut(4));

        // text-xs text-gray-400 mb-8
        subLbl.setFont(ThemeManager.font(ThemeManager.Weight.REGULAR, 12));
        subLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        fixHeight(subLbl, 16);
        column.add(subLbl);
        column.add(Box.createVerticalStrut(32));

        ticketPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(ticketPanel);
        column.add(ticketGap);

        // flex-1 buttons, gap-3, text-sm py-3.5 rounded-xl
        downloadBtn.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 14));
        downloadBtn.setMargin(new Insets(14, 24, 14, 24));
        downloadBtn.addActionListener(e -> downloadPass());

        myBookingsBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_BOOKING_HISTORY));
        exploreBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD));

        buttonRow.setOpaque(false);
        setDownloadShown(true);
        Dimension rowSize = new Dimension(COLUMN_WIDTH, 48);
        buttonRow.setPreferredSize(rowSize);
        buttonRow.setMaximumSize(rowSize);
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(buttonRow);

        // max-w-xl mx-auto py-12: the column sits at the top, centered
        JPanel page = new ScrollPage();
        page.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(48 - GLOW_PAD_TOP, 32 - GLOW_PAD_X, 48 - GLOW_PAD_BOTTOM, 32 - GLOW_PAD_X);
        page.add(column, gbc);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        showLoading();
    }

    private static void fixHeight(JLabel label, int height) {
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension d = new Dimension(COLUMN_WIDTH, height);
        label.setPreferredSize(d);
        label.setMaximumSize(d);
    }

    public void loadBookingConfirmation(String bookingId) {
        int seq = ++loadSeq;
        currentBooking = null;
        showLoading();

        SwingWorker<Loaded, Void> worker = new SwingWorker<>() {
            @Override
            protected Loaded doInBackground() throws Exception {
                Booking booking = bookingService.getBookingById(bookingId);
                Event event = null;
                try {
                    if (booking != null && booking.getEventId() != null) {
                        event = eventService.getEventById(booking.getEventId()); // photo, category, time range
                    }
                } catch (Exception ignored) {
                    // the pass still shows everything the booking itself carries
                }
                return new Loaded(booking, event);
            }

            @Override
            protected void done() {
                if (seq != loadSeq) {
                    return;
                }
                try {
                    Loaded loaded = get();
                    if (loaded.booking == null) {
                        showError("Booking " + bookingId + " was not found.");
                    } else {
                        showBooking(loaded.booking, loaded.event);
                    }
                } catch (Exception ex) {
                    showError(messageOf(ex));
                }
            }
        };
        worker.execute();
    }

    private void showLoading() {
        statusCircle.setState(StatusCircle.State.CONFIRMED);
        headingLbl.setText("Booking Confirmed!");
        subLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        subLbl.setText("Loading your pass...");
        ticketPanel.setVisible(false);
        ticketGap.setVisible(false);
        setDownloadShown(false);
        refresh();
    }

    private void showBooking(Booking booking, Event event) {
        currentBooking = booking;
        subLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        BookingStatus status = booking.getStatus() == null ? BookingStatus.CONFIRMED : booking.getStatus();
        switch (status) {
            case CANCELLED:
                statusCircle.setState(StatusCircle.State.CANCELLED);
                headingLbl.setText("Booking Cancelled");
                subLbl.setText("This booking was cancelled. The pass below is no longer valid at the entrance.");
                break;
            case PENDING:
                statusCircle.setState(StatusCircle.State.PENDING);
                headingLbl.setText("Payment Pending");
                subLbl.setText("Your seats are on hold. The pass is issued once the payment is confirmed.");
                break;
            default:
                statusCircle.setState(StatusCircle.State.CONFIRMED);
                headingLbl.setText("Booking Confirmed!");
                subLbl.setText("Your auditorium seats are reserved. Present the digital pass at the entrance.");
                break;
        }
        ticketPanel.setBooking(booking, event);
        ticketPanel.setVisible(true);
        ticketGap.setVisible(true);
        setDownloadShown(status == BookingStatus.CONFIRMED);
        refresh();
    }

    private void showError(String message) {
        statusCircle.setState(StatusCircle.State.ERROR);
        headingLbl.setText("Could not load this booking");
        subLbl.setForeground(ThemeManager.RED_400);
        subLbl.setText(message);
        subLbl.setToolTipText(message);
        ticketPanel.setVisible(false);
        ticketGap.setVisible(false);
        setDownloadShown(false);
        refresh();
    }

    /** The download button is left out (not just hidden) so the other two share the row, like flex-1. */
    private void setDownloadShown(boolean shown) {
        buttonRow.removeAll();
        if (shown) {
            buttonRow.add(downloadBtn);
        }
        buttonRow.add(myBookingsBtn);
        buttonRow.add(exploreBtn);
        buttonRow.revalidate();
    }

    private void refresh() {
        column.revalidate();
        column.repaint();
    }

    private static String messageOf(Exception ex) {
        Throwable cause = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
    }

    /** Saves the booking's passes (one page per seat, from TicketService.exportTicketPdf) as a PDF file. */
    private void downloadPass() {
        Booking booking = currentBooking;
        if (booking == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        File downloads = new File(System.getProperty("user.home"), "Downloads");
        File dir = downloads.isDirectory() ? downloads : new File(System.getProperty("user.home"));
        chooser.setSelectedFile(new File(dir, "SeminarX-Pass-" + booking.getId() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF document (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File chosen = chooser.getSelectedFile();
        File target = chosen.getName().toLowerCase().endsWith(".pdf") ? chosen
                : new File(chosen.getParentFile(), chosen.getName() + ".pdf");

        downloadBtn.setEnabled(false);
        SwingWorker<File, Void> worker = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                List<Ticket> passes = ticketService.generateTicketsForBooking(booking);
                List<String> pages = new ArrayList<>();
                for (Ticket pass : passes) {
                    pages.add(new String(ticketService.exportTicketPdf(pass.getTicketNumber()), StandardCharsets.UTF_8));
                }
                if (pages.isEmpty()) {
                    throw new IllegalStateException("No passes were issued for booking " + booking.getId() + ".");
                }
                Files.write(target.toPath(), SimplePdf.build("SeminarX Entry Pass", pages));
                return target;
            }

            @Override
            protected void done() {
                downloadBtn.setEnabled(true);
                try {
                    File saved = get();
                    JOptionPane.showMessageDialog(BookingConfirmationPanel.this,
                            "Your pass was saved to:\n" + saved.getAbsolutePath(),
                            "Pass Downloaded", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookingConfirmationPanel.this,
                            "Could not download the pass: " + messageOf(ex),
                            "Download Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private static final class Loaded {
        final Booking booking;
        final Event event;

        Loaded(Booking booking, Event event) {
            this.booking = booking;
            this.event = event;
        }
    }

    /** Fixed-width centered column; paints the glows that CSS box-shadows draw outside their elements. */
    private class Column extends JPanel {
        Column() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(COLUMN_WIDTH + 2 * GLOW_PAD_X, d.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // shadow-[0_0_30px_rgba(16,185,129,0.6)] around the status circle
            Rectangle c = statusCircle.getBounds();
            float cx = c.x + c.width / 2f;
            float cy = c.y + c.height / 2f;
            float r = c.width / 2f;
            float outer = r + 30;
            Color glow = statusCircle.color();
            Area ring = new Area(new Ellipse2D.Float(cx - outer, cy - outer, outer * 2, outer * 2));
            ring.subtract(new Area(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2)));
            g2.setPaint(new RadialGradientPaint(cx, cy, outer, new float[]{r / outer, (r + 10) / outer, 1f},
                    new Color[]{alpha(glow, 80), alpha(glow, 30), alpha(glow, 0)}));
            g2.fill(ring);

            // glow-primary: 0 8px 24px -4px rgba(255,56,46,.55) under Download Pass
            if (downloadBtn.getParent() == buttonRow && downloadBtn.getWidth() > 0) {
                Rectangle b = SwingUtilities.convertRectangle(buttonRow, downloadBtn.getBounds(), this);
                int layers = 12;
                g2.setColor(new Color(255, 56, 46, 13));
                for (int i = 0; i < layers; i++) {
                    // the -4px spread rect, blurred over 24px: stacked rects from 12px outside to 12px inside
                    float e = -4f + 12f - i * (24f / (layers - 1));
                    float arc = Math.max(0f, 24f + 2 * e);
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(b.x - e, b.y + 8 - e, b.width + 2 * e,
                            b.height + 2 * e, arc, arc));
                }
            }
            g2.dispose();
        }

        private Color alpha(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
        }
    }

    /** The round status mark: emerald check when confirmed, red x when cancelled or failed, amber clock when pending. */
    private static class StatusCircle extends JComponent {
        enum State { CONFIRMED, PENDING, CANCELLED, ERROR }

        private static final Color EMERALD_500 = new Color(16, 185, 129);
        private static final Color RED_500 = new Color(239, 68, 68);
        private static final Color AMBER_500 = new Color(245, 158, 11);

        private State state = State.CONFIRMED;

        StatusCircle() {
            Dimension d = new Dimension(64, 64);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        void setState(State state) {
            this.state = state;
            repaint();
            if (getParent() != null) {
                getParent().repaint();
            }
        }

        Color color() {
            switch (state) {
                case PENDING: return AMBER_500;
                case CANCELLED:
                case ERROR: return RED_500;
                default: return EMERALD_500;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = color();
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 51));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);

            String iconName;
            Color iconColor;
            switch (state) {
                case PENDING:
                    iconName = "clock";
                    iconColor = ThemeManager.AMBER_400;
                    break;
                case CANCELLED:
                case ERROR:
                    iconName = "x";
                    iconColor = ThemeManager.RED_400;
                    break;
                default:
                    iconName = "check";
                    iconColor = ThemeManager.EMERALD_400;
                    break;
            }
            Icon icon = Icons.get(iconName, 32, iconColor);
            icon.paintIcon(this, g2, (getWidth() - 32) / 2, (getHeight() - 32) / 2);
            g2.dispose();
        }
    }

    /** "bg-[#141724] border border-[#23273a] hover:bg-[#1a1e2f] text-white text-sm font-semibold rounded-xl". */
    private static class DarkButton extends JButton {
        private boolean hovered;

        DarkButton(String text) {
            super(text);
            setFont(ThemeManager.font(ThemeManager.Weight.SEMIBOLD, 14));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_XL * 2;
            g2.setColor(hovered ? new Color(26, 30, 47) : new Color(20, 23, 36));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(new Color(35, 39, 58));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Page body inside the scroll pane: follows the viewport width and fills its height. */
    private static class ScrollPage extends JPanel implements Scrollable {
        ScrollPage() {
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height - 32; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
        }
    }

    /** Writes plain text pages as a minimal PDF 1.4 document (Helvetica, one page per pass). */
    private static final class SimplePdf {
        private SimplePdf() {
        }

        static byte[] build(String title, List<String> pages) {
            List<String> objects = new ArrayList<>();
            objects.add("<< /Type /Catalog /Pages 2 0 R >>");
            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < pages.size(); i++) {
                kids.append(5 + 2 * i).append(" 0 R ");
            }
            objects.add("<< /Type /Pages /Kids [ " + kids + "] /Count " + pages.size() + " >>");
            objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
            objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
            for (int i = 0; i < pages.size(); i++) {
                StringBuilder content = new StringBuilder();
                content.append("BT /F2 20 Tf 56 780 Td (").append(escape(title)).append(") Tj ET\n");
                content.append("BT /F1 11 Tf 16 TL 56 740 Td\n");
                for (String line : pages.get(i).split("\\r?\\n")) {
                    content.append("(").append(escape(line)).append(") Tj T*\n");
                }
                content.append("ET\n");
                objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                        + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + (6 + 2 * i) + " 0 R >>");
                byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
                objects.add("<< /Length " + stream.length + " >>\nstream\n" + content + "endstream");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            write(out, "%PDF-1.4\n");
            for (int i = 0; i < objects.size(); i++) {
                offsets.add(out.size());
                write(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
            }
            int xref = out.size();
            StringBuilder table = new StringBuilder("xref\n0 " + (objects.size() + 1) + "\n0000000000 65535 f \n");
            for (int offset : offsets) {
                table.append(String.format("%010d 00000 n \n", offset));
            }
            write(out, table.toString());
            write(out, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
            return out.toByteArray();
        }

        private static void write(ByteArrayOutputStream out, String s) {
            byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
            out.write(bytes, 0, bytes.length);
        }

        /** PDF string escaping; characters outside Latin-1 (e.g. ₹) are spelled out or replaced. */
        private static String escape(String s) {
            StringBuilder sb = new StringBuilder();
            for (char ch : s.replace("₹", "Rs. ").toCharArray()) {
                if (ch == '(' || ch == ')' || ch == '\\') {
                    sb.append('\\').append(ch);
                } else if (ch < 32 || ch > 255) {
                    sb.append(ch == '\t' ? ' ' : '?');
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }
}
