package com.seminarhall.ui;

import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import java.awt.*;

public class PreLoginPanel extends JPanel {
    private final NavigationController navController;

    public PreLoginPanel(NavigationController navController) {
        this.navController = navController;

        setName(NavigationController.SCREEN_PRE_LOGIN);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Main container with deep cinematic theater gradient and spotlight glow
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Multi-stop gradient simulating red auditorium stage ambiance
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(42, 12, 14),
                        w / 2, h, new Color(8, 10, 16)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Stage ambient glow in center
                RadialGradientPaint ambientGlow = new RadialGradientPaint(
                        new Point(w / 2, h / 3),
                        Math.max(w, h),
                        new float[]{0.0f, 0.4f, 1.0f},
                        new Color[]{
                                new Color(255, 56, 46, 35),
                                new Color(255, 104, 53, 10),
                                new Color(0, 0, 0, 0)
                        }
                );
                g2.setPaint(ambientGlow);
                g2.fillRect(0, 0, w, h);

                g2.dispose();
            }
        };
        backgroundPanel.setOpaque(false);

        // ================= TOP NAVBAR =================
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setOpaque(false);
        topNav.setBorder(BorderFactory.createEmptyBorder(20, 40, 16, 40));

        // Brand on Left
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandBox.setOpaque(false);

        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ThemeManager.ACCENT_RED, getWidth(), getHeight(), ThemeManager.ACCENT_ORANGE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("SX", (getWidth() - fm.stringWidth("SX")) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(38, 38));
        logoIcon.setOpaque(false);

        JPanel brandTexts = new JPanel();
        brandTexts.setOpaque(false);
        brandTexts.setLayout(new BoxLayout(brandTexts, BoxLayout.Y_AXIS));

        JLabel brandName = new JLabel("SeminarX");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandName.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel brandTag = new JLabel("CAMPUS EVENT PORTAL");
        brandTag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        brandTag.setForeground(ThemeManager.TEXT_MUTED);

        brandTexts.add(brandName);
        brandTexts.add(brandTag);

        brandBox.add(logoIcon);
        brandBox.add(brandTexts);

        // Nav Actions on Right
        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        navActions.setOpaque(false);

        StyledButton exploreBtn = new StyledButton("Explore Events", StyledButton.ButtonStyle.SECONDARY);
        exploreBtn.setPreferredSize(new Dimension(130, 38));
        exploreBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_EVENT_LIST));

        StyledButton signInBtn = new StyledButton("Sign In →", StyledButton.ButtonStyle.PRIMARY);
        signInBtn.setPreferredSize(new Dimension(120, 38));
        signInBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_LOGIN));

        navActions.add(exploreBtn);
        navActions.add(signInBtn);

        topNav.add(brandBox, BorderLayout.WEST);
        topNav.add(navActions, BorderLayout.EAST);
        backgroundPanel.add(topNav, BorderLayout.NORTH);

        // ================= CENTER HERO SECTION =================
        JPanel centerHero = new JPanel(new GridBagLayout());
        centerHero.setOpaque(false);

        JPanel heroCard = new JPanel();
        heroCard.setOpaque(false);
        heroCard.setLayout(new BoxLayout(heroCard, BoxLayout.Y_AXIS));
        heroCard.setMaximumSize(new Dimension(860, 520));

        // Pill Tag: "PREMIUM COLLEGE AUDITORIUM SEATING & EVENTS"
        JPanel tagPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 56, 46, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 56, 46, 120));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tagPill.setOpaque(false);
        tagPill.setMaximumSize(new Dimension(380, 28));

        JLabel tagText = new JLabel("✨  PREMIUM COLLEGE AUDITORIUM SEATING & EVENTS");
        tagText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagText.setForeground(ThemeManager.ACCENT_RED);
        tagPill.add(tagText);

        // Giant Hero Title
        JLabel heroTitle = new JLabel("<html><center>Experience College Events<br><font color='#ff4a3f'>Like Never Before.</font></center></html>", SwingConstants.CENTER);
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 46));
        heroTitle.setForeground(Color.WHITE);
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle Description
        JLabel heroSub = new JLabel("<html><center>Book front-row auditorium seats for tech symposiums, workshops, guest lectures,<br>and cultural fests with interactive real-time seat maps and digital e-passes.</center></html>", SwingConstants.CENTER);
        heroSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        heroSub.setForeground(new Color(195, 202, 216));
        heroSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // CTA Buttons Row
        JPanel ctaRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        ctaRow.setOpaque(false);
        ctaRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledButton getStartedBtn = new StyledButton("Get Started / Sign In →", StyledButton.ButtonStyle.PRIMARY);
        getStartedBtn.setPreferredSize(new Dimension(210, 46));
        getStartedBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        getStartedBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_LOGIN));

        StyledButton createAccBtn = new StyledButton("Create Free Account", StyledButton.ButtonStyle.SECONDARY);
        createAccBtn.setPreferredSize(new Dimension(190, 46));
        createAccBtn.setFont(ThemeManager.FONT_BODY_BOLD);
        createAccBtn.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_REGISTER));

        ctaRow.add(getStartedBtn);
        ctaRow.add(createAccBtn);

        // Feature Highlights Bar
        JPanel featuresRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        featuresRow.setOpaque(false);
        featuresRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        featuresRow.add(createFeatureChip("⚡", "Real-Time Seating Map"));
        featuresRow.add(createFeatureChip("🎟️", "Instant QR E-Tickets"));
        featuresRow.add(createFeatureChip("🎓", "100% Verified Campus Events"));
        featuresRow.add(createFeatureChip("🏛️", "Multi-Hall Discovery"));

        heroCard.add(tagPill);
        heroCard.add(Box.createVerticalStrut(18));
        heroCard.add(heroTitle);
        heroCard.add(Box.createVerticalStrut(16));
        heroCard.add(heroSub);
        heroCard.add(Box.createVerticalStrut(30));
        heroCard.add(ctaRow);
        heroCard.add(Box.createVerticalStrut(36));
        heroCard.add(featuresRow);

        centerHero.add(heroCard);
        backgroundPanel.add(centerHero, BorderLayout.CENTER);

        // Bottom Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        footer.setOpaque(false);
        JLabel footLbl = new JLabel("SeminarX • Official College Event & Auditorium Seat Booking System • Designed for Campus Excellence");
        footLbl.setFont(ThemeManager.FONT_SMALL);
        footLbl.setForeground(new Color(110, 118, 135));
        footer.add(footLbl);
        backgroundPanel.add(footer, BorderLayout.SOUTH);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createFeatureChip(String icon, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(24, 28, 40, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel tx = new JLabel(text);
        tx.setFont(ThemeManager.FONT_SMALL_BOLD);
        tx.setForeground(ThemeManager.TEXT_SECONDARY);

        p.add(ic);
        p.add(tx);
        return p;
    }
}
