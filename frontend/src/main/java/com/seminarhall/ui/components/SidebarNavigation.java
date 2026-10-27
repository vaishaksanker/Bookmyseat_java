package com.seminarhall.ui.components;

import com.seminarhall.ui.NavigationController;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class SidebarNavigation extends JPanel {
    private final NavigationController navController;
    private String activeSection = "Dashboard";

    public interface OnNavSelect {
        void onSelect(String section);
    }

    private final OnNavSelect onNavSelect;

    public SidebarNavigation(NavigationController navController, OnNavSelect onNavSelect) {
        this.navController = navController;
        this.onNavSelect = onNavSelect;

        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 700));
        setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        buildUI();
    }

    private void buildUI() {
        JPanel topBox = new JPanel();
        topBox.setOpaque(false);
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));

        // Brand
        JLabel brand = new JLabel("⚡ ORGANIZER");
        brand.setFont(ThemeManager.FONT_HEADER);
        brand.setForeground(ThemeManager.ACCENT_RED);

        JLabel sub = new JLabel("PORTAL CONSOLE");
        sub.setFont(ThemeManager.FONT_SMALL_BOLD);
        sub.setForeground(ThemeManager.TEXT_MUTED);

        topBox.add(brand);
        topBox.add(Box.createVerticalStrut(2));
        topBox.add(sub);
        topBox.add(Box.createVerticalStrut(28));

        // Menu items
        String[] items = {
                "📊 Overview Dashboard",
                "🎪 Manage Events",
                "➕ Create New Event",
                "📑 All Bookings",
                "📈 Occupancy Analytics"
        };

        for (String item : items) {
            StyledButton btn = new StyledButton(item, StyledButton.ButtonStyle.OUTLINE);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFont(ThemeManager.FONT_BODY_BOLD);
            btn.setMaximumSize(new Dimension(220, 42));

            String sectionName = item.substring(3).trim();
            btn.addActionListener(e -> {
                activeSection = sectionName;
                if (onNavSelect != null) {
                    onNavSelect.onSelect(sectionName);
                }
            });
            topBox.add(btn);
            topBox.add(Box.createVerticalStrut(6));
        }

        add(topBox, BorderLayout.NORTH);

        // Logout at bottom
        JPanel bottomBox = new JPanel();
        bottomBox.setOpaque(false);
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));

        StyledButton logoutBtn = new StyledButton("🚪 Logout", StyledButton.ButtonStyle.SECONDARY);
        logoutBtn.setMaximumSize(new Dimension(220, 40));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to sign out of the Organizer Portal?",
                    "Confirm Sign Out",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                navController.logout();
            }
        });

        bottomBox.add(logoutBtn);
        add(bottomBox, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(ThemeManager.BG_SECONDARY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(ThemeManager.BORDER_SUBTLE);
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

        g2.dispose();
        super.paintComponent(g);
    }
}
