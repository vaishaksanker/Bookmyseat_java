package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

/** Centered brand-red spinner with a gray message, shown while a screen loads its data. */
public class LoadingPanel extends JPanel {
    public LoadingPanel(String message) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        Spinner spinner = new Spinner();
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel(message != null ? message : "Loading data...", SwingConstants.CENTER);
        msgLabel.setFont(ThemeManager.font(ThemeManager.Weight.MEDIUM, 13));
        msgLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(spinner);
        box.add(Box.createVerticalStrut(14));
        box.add(msgLabel);

        add(box);
        setPreferredSize(new Dimension(400, 260));
    }

    /** A rotating red → orange arc; animates only while it is on screen. */
    private static final class Spinner extends JComponent {
        private int angle;
        private final Timer timer = new Timer(16, e -> {
            angle = (angle + 8) % 360;
            repaint();
        });

        Spinner() {
            Dimension size = new Dimension(32, 32);
            setPreferredSize(size);
            setMaximumSize(size);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            timer.start();
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ThemeManager.WHITE_10);
            g2.drawOval(2, 2, 27, 27);
            g2.setPaint(new GradientPaint(0, 0, ThemeManager.BRAND_RED, 32, 32, ThemeManager.BRAND_ORANGE));
            g2.drawArc(2, 2, 27, 27, -angle, 100);
            g2.dispose();
        }
    }
}
