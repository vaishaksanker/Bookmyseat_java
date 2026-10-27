package com.seminarhall.ui.components;

import com.seminarhall.model.User;
import com.seminarhall.ui.NavigationController;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class NavigationBar extends JPanel {
    private final NavigationController navController;
    private JLabel userLabel;
    private StyledTextField searchField;

    public NavigationBar(NavigationController navController) {
        this.navController = navController;
        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1000, 60));
        setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        buildUI();
    }

    private void buildUI() {
        // Left: SeminarX Branding with Red Logo Icon
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        brandPanel.setOpaque(false);

        // Stylized Red 'X' Box Logo
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ThemeManager.ACCENT_RED, getWidth(), getHeight(), ThemeManager.ACCENT_ORANGE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("SX", (getWidth() - fm.stringWidth("SX")) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(28, 28));
        logoIcon.setOpaque(false);

        JLabel brandTitle = new JLabel("SeminarX");
        brandTitle.setFont(ThemeManager.FONT_LOGO);
        brandTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        brandPanel.add(logoIcon);
        brandPanel.add(brandTitle);

        // Center: Navigation Tabs with Back button
        JPanel navTabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        navTabs.setOpaque(false);

        JButton backBtn = createNavTab("← Back", false, () -> navController.goBack());
        JButton homeTab = createNavTab("Home", true, () -> navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD));
        JButton exploreTab = createNavTab("Explore", false, () -> navController.navigateTo(NavigationController.SCREEN_EVENT_LIST));
        JButton bookingsTab = createNavTab("My Bookings", false, () -> navController.navigateTo(NavigationController.SCREEN_BOOKING_HISTORY));

        navTabs.add(backBtn);
        navTabs.add(homeTab);
        navTabs.add(exploreTab);
        navTabs.add(bookingsTab);

        // Right: Search bar & User Avatar with Name
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        rightPanel.setOpaque(false);

        searchField = new StyledTextField("Search events, workshops, seminars...");
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.addActionListener(e -> {
            navController.navigateTo(NavigationController.SCREEN_EVENT_LIST);
        });

        JLabel bellIcon = new JLabel("🔔");
        bellIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        bellIcon.setForeground(ThemeManager.TEXT_MUTED);
        bellIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        userLabel = new JLabel("Hi, Ananthakrishna ▼");
        userLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        userLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Click user avatar to open profile menu / logout
        userLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                popup.setBackground(ThemeManager.BG_CARD);
                JMenuItem logoutItem = new JMenuItem("Log Out");
                logoutItem.setForeground(ThemeManager.ACCENT_RED);
                logoutItem.addActionListener(ev -> navController.logout());
                popup.add(logoutItem);
                popup.show(userLabel, 0, userLabel.getHeight() + 4);
            }
        });

        rightPanel.add(searchField);
        rightPanel.add(bellIcon);
        rightPanel.add(userLabel);

        add(brandPanel, BorderLayout.WEST);
        add(navTabs, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createNavTab(String title, boolean active, Runnable onClick) {
        JButton btn = new JButton(title);
        btn.setFont(ThemeManager.FONT_BODY_BOLD);
        btn.setForeground(active ? ThemeManager.ACCENT_RED : ThemeManager.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onClick.run());
        return btn;
    }

    public void updateUserInfo(User user) {
        if (user != null) {
            String shortName = user.getFullName().split(" ")[0];
            userLabel.setText("Hi, " + shortName + " ▼");
        } else {
            userLabel.setText("Account ▼");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(ThemeManager.BG_DARKEST);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(ThemeManager.BORDER_SUBTLE);
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
        super.paintComponent(g);
    }
}
