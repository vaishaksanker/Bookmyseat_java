package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Centered "nothing here" message in the web app's style: a Lucide icon in a bg-white/5 tile,
 * a font-black title, gray-400 helper text and an optional glow-primary button.
 *
 * iconText may be a Lucide icon name ("search", "ticket", ...) or one of the emoji older screens
 * pass (they are mapped to the matching icon, since the bundled font has no emoji).
 */
public class EmptyStatePanel extends JPanel {
    public EmptyStatePanel(String iconText, String title, String subtitle, String actionButtonText, Runnable onAction) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JPanel iconTile = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = ThemeManager.RADIUS_2XL * 2;
                g2.setColor(ThemeManager.WHITE_5);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };
        iconTile.setOpaque(false);
        Dimension tileSize = new Dimension(64, 64);
        iconTile.setPreferredSize(tileSize);
        iconTile.setMaximumSize(tileSize);
        iconTile.add(Icons.label(iconName(iconText), 28, ThemeManager.TEXT_SECONDARY));
        iconTile.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.font(ThemeManager.Weight.EXTRABOLD, 18));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        CenteredText subLabel = new CenteredText(subtitle == null ? "" : subtitle,
                ThemeManager.font(ThemeManager.Weight.REGULAR, 13), ThemeManager.TEXT_SECONDARY, 380, 20);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(iconTile);
        box.add(Box.createVerticalStrut(16));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(6));
        box.add(subLabel);

        if (actionButtonText != null && onAction != null) {
            box.add(Box.createVerticalStrut(14));
            StyledButton actionBtn = new StyledButton(actionButtonText, StyledButton.ButtonStyle.PRIMARY);
            actionBtn.setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 13));
            actionBtn.setGlow(true);
            actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionBtn.addActionListener(e -> onAction.run());
            box.add(actionBtn);
        }

        add(box);
    }

    private static String iconName(String iconText) {
        if (iconText == null || iconText.isBlank()) {
            return "sparkles";
        }
        String s = iconText.trim();
        if (s.matches("[a-z0-9-]+")) {
            return s;
        }
        if (s.startsWith("🔍")) return "search";       // magnifying glass
        if (s.startsWith("📅") || s.startsWith("📆")) return "calendar";
        if (s.startsWith("🎟") || s.startsWith("🎫")) return "ticket";
        if (s.startsWith("🔔")) return "bell";
        if (s.startsWith("👤")) return "user";
        return "sparkles";
    }

    /** Word-wrapped, centered text with a fixed line height (like a centered <p> with a max width). */
    private static class CenteredText extends JComponent {
        private final String text;
        private final int maxWidth;
        private final int lineHeight;

        CenteredText(String text, Font font, Color color, int maxWidth, int lineHeight) {
            this.text = text;
            this.maxWidth = maxWidth;
            this.lineHeight = lineHeight;
            setFont(font);
            setForeground(color);
        }

        private List<String> lines() {
            FontMetrics fm = getFontMetrics(getFont());
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.split("\\s+")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
            return lines;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = 0;
            List<String> lines = lines();
            for (String l : lines) {
                w = Math.max(w, fm.stringWidth(l));
            }
            return new Dimension(w, Math.max(1, lines.size()) * lineHeight);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int y = 0;
            for (String l : lines()) {
                int baseline = y + (lineHeight + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(l, (getWidth() - fm.stringWidth(l)) / 2, baseline);
                y += lineHeight;
            }
            g2.dispose();
        }
    }
}
