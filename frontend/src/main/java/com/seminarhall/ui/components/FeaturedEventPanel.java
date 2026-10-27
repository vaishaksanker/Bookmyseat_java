package com.seminarhall.ui.components;

import com.seminarhall.model.Event;
import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class FeaturedEventPanel extends JPanel {
    private final Event event;
    private final Consumer<Event> onExplore;

    public FeaturedEventPanel(Event event, Consumer<Event> onExplore) {
        this.event = event;
        this.onExplore = onExplore;

        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(860, 220));
        setMaximumSize(new Dimension(1600, 240));
        setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title1 = new JLabel("IDEAS");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title1.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel title2 = new JLabel("PEOPLE");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title2.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel title3 = new JLabel("POSSIBILITIES");
        title3.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title3.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel sub = new JLabel("Seminars  •  Workshops  •  Hackathons  •  Cultural Events");
        sub.setFont(ThemeManager.FONT_BODY);
        sub.setForeground(ThemeManager.TEXT_SECONDARY);

        StyledButton exploreBtn = new StyledButton("Explore Events →", StyledButton.ButtonStyle.PRIMARY);
        exploreBtn.setPreferredSize(new Dimension(160, 38));
        exploreBtn.setMaximumSize(new Dimension(160, 38));
        exploreBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exploreBtn.addActionListener(e -> {
            if (onExplore != null) onExplore.accept(event);
        });

        content.add(title1);
        content.add(title2);
        content.add(title3);
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(14));
        content.add(exploreBtn);

        add(content, BorderLayout.WEST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Dark auditorium banner with stage crowd photography ambient lighting
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(42, 14, 16),
                w, h, new Color(18, 20, 28)
        );
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h, 16, 16);

        // Subtle stage spotlight on the right half
        g2.setColor(new Color(255, 68, 56, 16));
        g2.fillOval(w - 380, -40, 420, 300);

        g2.setColor(ThemeManager.BORDER_CARD);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
