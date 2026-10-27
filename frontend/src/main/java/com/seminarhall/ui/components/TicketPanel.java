package com.seminarhall.ui.components;

import com.seminarhall.model.Ticket;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TicketPanel extends JPanel {
    private final Ticket ticket;

    public TicketPanel(Ticket ticket) {
        this.ticket = ticket;
        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(540, 260));
        setMaximumSize(new Dimension(620, 280));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        buildUI();
    }

    private void buildUI() {
        if (ticket == null) return;

        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);

        // Left Side: Event and Attendee info
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        StatusBadge passBadge = new StatusBadge("COLLEGE SEMINAR HALL ENTRY PASS", ThemeManager.ACCENT_RED);
        passBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(ticket.getEventTitle());
        titleLbl.setFont(ThemeManager.FONT_HEADER);
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel venueLbl = new JLabel("📍 " + ticket.getSeminarHall() + ", " + ticket.getVenue());
        venueLbl.setFont(ThemeManager.FONT_BODY);
        venueLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        venueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String dateStr = ticket.getEventDateTime().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - hh:mm a"));
        JLabel dateLbl = new JLabel("📅 " + dateStr);
        dateLbl.setFont(ThemeManager.FONT_BODY);
        dateLbl.setForeground(ThemeManager.TEXT_SECONDARY);
        dateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel attendeeLbl = new JLabel("👤 Attendee: " + ticket.getAttendeeName() + " (" + ticket.getAttendeeEmail() + ")");
        attendeeLbl.setFont(ThemeManager.FONT_SMALL);
        attendeeLbl.setForeground(ThemeManager.TEXT_MUTED);
        attendeeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tktIdLbl = new JLabel("Ticket #: " + ticket.getTicketNumber());
        tktIdLbl.setFont(ThemeManager.FONT_SMALL_BOLD);
        tktIdLbl.setForeground(ThemeManager.ACCENT_AMBER);
        tktIdLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(passBadge);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(titleLbl);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(venueLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(dateLbl);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(attendeeLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(tktIdLbl);

        // Right Side: Seat Badge & QR code stub
        JPanel stubPanel = new JPanel();
        stubPanel.setOpaque(false);
        stubPanel.setLayout(new BoxLayout(stubPanel, BoxLayout.Y_AXIS));
        stubPanel.setPreferredSize(new Dimension(140, 200));

        JLabel seatTitle = new JLabel("SEAT", SwingConstants.CENTER);
        seatTitle.setFont(ThemeManager.FONT_SMALL_BOLD);
        seatTitle.setForeground(ThemeManager.TEXT_MUTED);
        seatTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel seatBadge = new JLabel(ticket.getSeatIdentifier(), SwingConstants.CENTER);
        seatBadge.setFont(new Font("Segoe UI", Font.BOLD, 32));
        seatBadge.setForeground(ThemeManager.ACCENT_GREEN);
        seatBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stylized visual QR placeholder component
        JPanel qrBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int size = Math.min(getWidth(), getHeight()) - 8;
                int ox = (getWidth() - size) / 2;
                int oy = (getHeight() - size) / 2;

                g2.setColor(Color.WHITE);
                g2.fillRect(ox, oy, size, size);

                // Draw mock QR pixel patterns
                g2.setColor(Color.BLACK);
                int blocks = 9;
                int bs = size / blocks;
                for (int r = 0; r < blocks; r++) {
                    for (int c = 0; c < blocks; c++) {
                        // Corner finder patterns
                        if ((r < 3 && c < 3) || (r < 3 && c >= blocks - 3) || (r >= blocks - 3 && c < 3)) {
                            if (r == 1 && c == 1 || r == 1 && c == blocks - 2 || r == blocks - 2 && c == 1) {
                                g2.fillRect(ox + c * bs, oy + r * bs, bs, bs);
                            } else if (r == 0 || r == 2 || c == 0 || c == 2 ||
                                    r == 0 || r == 2 || c == blocks - 1 || c == blocks - 3 ||
                                    r == blocks - 1 || r == blocks - 3 || c == 0 || c == 2) {
                                g2.fillRect(ox + c * bs, oy + r * bs, bs, bs);
                            }
                        } else if ((r + c * 3) % 2 == 0) {
                            g2.fillRect(ox + c * bs, oy + r * bs, bs, bs);
                        }
                    }
                }
                g2.dispose();
            }
        };
        qrBox.setPreferredSize(new Dimension(90, 90));
        qrBox.setMaximumSize(new Dimension(90, 90));
        qrBox.setOpaque(false);
        qrBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scanLbl = new JLabel("SCAN AT VENUE", SwingConstants.CENTER);
        scanLbl.setFont(ThemeManager.FONT_SMALL);
        scanLbl.setForeground(ThemeManager.TEXT_MUTED);
        scanLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        stubPanel.add(seatTitle);
        stubPanel.add(seatBadge);
        stubPanel.add(Box.createVerticalStrut(6));
        stubPanel.add(qrBox);
        stubPanel.add(Box.createVerticalStrut(4));
        stubPanel.add(scanLbl);

        content.add(infoPanel, BorderLayout.CENTER);
        content.add(stubPanel, BorderLayout.EAST);

        add(content, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Ticket background card
        g2.setColor(ThemeManager.BG_SECONDARY);
        g2.fillRoundRect(0, 0, w, h, 16, 16);

        // Dashed perforated ticket tear line between info and stub
        int tearX = w - 165;
        Stroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0);
        g2.setStroke(dashed);
        g2.setColor(new Color(60, 66, 85));
        g2.drawLine(tearX, 10, tearX, h - 10);

        // Border
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(ThemeManager.BORDER_SUBTLE);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
