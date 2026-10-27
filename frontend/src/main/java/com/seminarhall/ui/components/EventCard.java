package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class EventCard extends JPanel {
    private final Event event;
    private final Consumer<Event> onViewDetails;
    private final Consumer<Event> onBookNow;
    private boolean isHovered = false;

    public EventCard(Event event, Consumer<Event> onViewDetails, Consumer<Event> onBookNow) {
        this.event = event;
        this.onViewDetails = onViewDetails;
        this.onBookNow = onBookNow;

        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 270));
        setMinimumSize(new Dimension(220, 260));
        setMaximumSize(new Dimension(260, 290));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildUI();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onViewDetails != null) {
                    onViewDetails.accept(event);
                }
            }
        });
    }

    private void buildUI() {
        // Poster / Thumbnail Panel (Stage / Audience photo illustration)
        JPanel poster = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Warm cinematic stage lighting gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(48, 20, 18),
                        w, h, new Color(20, 22, 32)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                // Speaker silhouette / Stage spotlight icon
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 28));
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawString(getCategoryIcon(event.getCategory().name()), 16, 44);

                // Category pill tag at bottom left of thumbnail
                Color tagColor = getCategoryColor(event.getCategory().name());
                g2.setColor(tagColor);
                g2.fillRoundRect(10, h - 26, 76, 18, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(ThemeManager.FONT_SMALL_BOLD);
                String catName = getCategoryShort(event.getCategory().name());
                FontMetrics fm = g2.getFontMetrics();
                int tx = 10 + (76 - fm.stringWidth(catName)) / 2;
                g2.drawString(catName, tx, h - 13);

                g2.dispose();
            }
        };
        poster.setPreferredSize(new Dimension(220, 115));
        poster.setOpaque(false);
        add(poster, BorderLayout.NORTH);

        // Body info matching Screen 3 cards
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(8, 2, 4, 2));

        JLabel titleLbl = new JLabel("<html><b>" + event.getTitle() + "</b></html>");
        titleLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLbl.setPreferredSize(new Dimension(210, 36));

        String dateStr = event.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " • " +
                event.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        JLabel dateLbl = new JLabel("📅 " + dateStr);
        dateLbl.setFont(ThemeManager.FONT_SMALL);
        dateLbl.setForeground(ThemeManager.TEXT_MUTED);

        JLabel hallLbl = new JLabel("📍 " + event.getSeminarHall());
        hallLbl.setFont(ThemeManager.FONT_SMALL);
        hallLbl.setForeground(ThemeManager.TEXT_MUTED);

        // Price Row
        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);
        priceRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        String priceStr = event.getTicketPrice() > 0 ? "₹" + String.format("%.0f", event.getTicketPrice()) : "Free";
        JLabel priceLbl = new JLabel(priceStr);
        priceLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        priceLbl.setForeground(event.getTicketPrice() > 0 ? ThemeManager.TEXT_PRIMARY : ThemeManager.STATUS_GREEN);

        JLabel bookmarkIcon = new JLabel("🔖");
        bookmarkIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        bookmarkIcon.setForeground(ThemeManager.TEXT_MUTED);

        priceRow.add(priceLbl, BorderLayout.WEST);
        priceRow.add(bookmarkIcon, BorderLayout.EAST);

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(dateLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(hallLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(priceRow);

        add(info, BorderLayout.CENTER);
    }

    private Color getCategoryColor(String cat) {
        if (cat.contains("TECH")) return ThemeManager.STATUS_BLUE;
        if (cat.contains("WORK")) return ThemeManager.STATUS_ORANGE;
        if (cat.contains("CULTURAL")) return ThemeManager.STATUS_PURPLE;
        if (cat.contains("HACK")) return new Color(168, 85, 247);
        if (cat.contains("SPORT")) return new Color(14, 165, 233);
        return ThemeManager.STATUS_GREEN;
    }

    private String getCategoryShort(String cat) {
        if (cat.contains("TECH")) return "TECHNICAL";
        if (cat.contains("WORK")) return "WORKSHOP";
        if (cat.contains("CULTURAL")) return "CULTURAL";
        if (cat.contains("HACK")) return "HACKATHON";
        if (cat.contains("SPORT")) return "SPORTS";
        return "SEMINAR";
    }

    private String getCategoryIcon(String cat) {
        if (cat.contains("TECH")) return "💻";
        if (cat.contains("WORK")) return "🛠";
        if (cat.contains("CULTURAL")) return "🎭";
        if (cat.contains("HACK")) return "⚡";
        if (cat.contains("SPORT")) return "🏆";
        return "🎙";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(isHovered ? ThemeManager.BG_CARD_HOVER : ThemeManager.BG_CARD);
        g2.fillRoundRect(0, 0, w, h, 14, 14);

        g2.setColor(isHovered ? ThemeManager.ACCENT_RED : ThemeManager.BORDER_CARD);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
