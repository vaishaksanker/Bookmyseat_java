package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Faculty Console metric tile (web OrganizerDashboardPage):
 * "bg-[#10131d] border border-[#1d2232] rounded-2xl p-5" with a "text-xs text-gray-400" label
 * above a "text-3xl font-black" value in the tile's color.
 */
public class MetricCard extends JPanel {
    private static final Color CARD_BG = new Color(16, 19, 29);       // #10131d
    private static final Color CARD_BORDER = new Color(29, 34, 50);   // #1d2232

    private final JLabel valueLabel;

    public MetricCard(String label, Color valueColor) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 21, 20, 21));

        JLabel labelText = line(label, ThemeManager.font(Weight.REGULAR, 12), ThemeManager.TEXT_SECONDARY, 16);
        valueLabel = line("–", ThemeManager.font(Weight.EXTRABOLD, 30), valueColor, 36);

        add(labelText);
        add(Box.createVerticalStrut(4));
        add(valueLabel);
    }

    /** Shows the value, e.g. "12" ("–" while loading). */
    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public String getValue() {
        return valueLabel.getText();
    }

    private static JLabel line(String text, Font font, Color color, int lineHeight) {
        JLabel label = new JLabel(text) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, lineHeight);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, lineHeight);
            }
        };
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ThemeManager.RADIUS_2XL * 2;
        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
    }
}
