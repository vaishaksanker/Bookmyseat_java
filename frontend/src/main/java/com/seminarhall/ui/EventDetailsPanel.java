package com.seminarhall.ui;

import com.seminarhall.model.Event;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class EventDetailsPanel extends JPanel {
    private final NavigationController navController;
    private Event currentEvent;

    private JLabel titleLbl;
    private JLabel descLbl;
    private JLabel dateLbl;
    private JLabel timeLbl;
    private JLabel hallLbl;
    private JLabel deptLbl;
    private JLabel capacityLbl;
    private JLabel availLbl;
    private StyledButton selectSeatsBtn;

    public EventDetailsPanel(NavigationController navController) {
        this.navController = navController;

        setName(NavigationController.SCREEN_EVENT_DETAILS);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout(24, 0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(16, 24, 24, 24));

        // Top Back Button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel backBtn = new JLabel("← Back");
        backBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        backBtn.setForeground(ThemeManager.TEXT_SECONDARY);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navController.goBack();
            }
        });
        top.add(backBtn);
        add(top, BorderLayout.NORTH);

        // Center Split: Left Stage Image Banner, Right Details Card
        JPanel center = new JPanel(new GridLayout(1, 2, 24, 0));
        center.setOpaque(false);

        // Left Speaker / Stage Backdrop
        JPanel leftPoster = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 22, 20), w, h, new Color(18, 20, 28));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 16, 16);

                // Stage spotlight silhouette
                g2.setColor(new Color(255, 68, 56, 40));
                g2.fillOval(w / 4, h / 4, w / 2, h / 2);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 64));
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawString("🎙", (w - 70) / 2, (h + 30) / 2);

                g2.setColor(ThemeManager.BORDER_CARD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.dispose();
            }
        };
        leftPoster.setOpaque(false);

        // Right Content Column (matching Screen 4)
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Badges row: TECHNICAL, FEATURED
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badges.setOpaque(false);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel techBadge = createPill("TECHNICAL", ThemeManager.STATUS_BLUE);
        JLabel featBadge = createPill("FEATURED", ThemeManager.STATUS_ORANGE);
        badges.add(techBadge);
        badges.add(featBadge);

        titleLbl = new JLabel("AI in Modern World");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        descLbl = new JLabel("<html>A deep dive into the impact of Artificial Intelligence in modern industries and future opportunities.</html>");
        descLbl.setFont(ThemeManager.FONT_BODY);
        descLbl.setForeground(ThemeManager.TEXT_MUTED);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Metadata grid
        JPanel metaList = new JPanel();
        metaList.setOpaque(false);
        metaList.setLayout(new BoxLayout(metaList, BoxLayout.Y_AXIS));
        metaList.setAlignmentX(Component.LEFT_ALIGNMENT);

        dateLbl = createMetaRow("📅", "Dec 10, 2024");
        timeLbl = createMetaRow("🕒", "10:00 AM - 12:00 PM");
        hallLbl = createMetaRow("📍", "Seminar Hall A");
        deptLbl = createMetaRow("🏢", "Computer Science Department");
        capacityLbl = createMetaRow("👥", "Total Seats: 100");
        availLbl = createMetaRow("🟢", "Available Seats: 45");

        metaList.add(dateLbl);
        metaList.add(Box.createVerticalStrut(10));
        metaList.add(timeLbl);
        metaList.add(Box.createVerticalStrut(10));
        metaList.add(hallLbl);
        metaList.add(Box.createVerticalStrut(10));
        metaList.add(deptLbl);
        metaList.add(Box.createVerticalStrut(10));
        metaList.add(capacityLbl);
        metaList.add(Box.createVerticalStrut(10));
        metaList.add(availLbl);

        selectSeatsBtn = new StyledButton("Select Seats →", StyledButton.ButtonStyle.PRIMARY);
        selectSeatsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectSeatsBtn.setMaximumSize(new Dimension(340, 44));
        selectSeatsBtn.setPreferredSize(new Dimension(340, 44));
        selectSeatsBtn.addActionListener(e -> {
            if (currentEvent != null) {
                navController.startSeatSelection(currentEvent);
            }
        });

        rightCol.add(badges);
        rightCol.add(Box.createVerticalStrut(10));
        rightCol.add(titleLbl);
        rightCol.add(Box.createVerticalStrut(6));
        rightCol.add(descLbl);
        rightCol.add(Box.createVerticalStrut(24));
        rightCol.add(metaList);
        rightCol.add(Box.createVerticalStrut(28));
        rightCol.add(selectSeatsBtn);

        center.add(leftPoster);
        center.add(rightCol);

        container.add(center, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);
    }

    private JLabel createPill(String text, Color col) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.FONT_SMALL_BOLD);
        l.setForeground(Color.WHITE);
        l.setBackground(col);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return l;
    }

    private JLabel createMetaRow(String icon, String text) {
        JLabel l = new JLabel(icon + "  " + text);
        l.setFont(ThemeManager.FONT_BODY);
        l.setForeground(ThemeManager.TEXT_SECONDARY);
        return l;
    }

    public void loadEvent(Event event) {
        this.currentEvent = event;
        if (event == null) return;

        titleLbl.setText(event.getTitle());
        descLbl.setText("<html>" + event.getDescription() + "</html>");
        dateLbl.setText("📅  " + event.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        timeLbl.setText("🕒  " + event.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")) + " - " +
                event.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        hallLbl.setText("📍  " + event.getSeminarHall());
        deptLbl.setText("🏢  " + (event.getOrganizerName() != null ? event.getOrganizerName() : "College Committee"));
        capacityLbl.setText("👥  Total Seats: " + event.getTotalCapacity());
        availLbl.setText("🟢  Available Seats: " + event.getAvailableSeats());
    }
}
