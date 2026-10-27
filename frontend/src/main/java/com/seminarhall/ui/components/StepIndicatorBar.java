package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class StepIndicatorBar extends JPanel {
    private int currentStep; // 1: Select Seats, 2: Booking Summary, 3: Payment, 4: Confirmation

    public StepIndicatorBar(int currentStep) {
        this.currentStep = currentStep;
        setOpaque(false);
        setPreferredSize(new Dimension(560, 40));
        setMaximumSize(new Dimension(650, 40));
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String[] stepTitles = {"Select Seats", "Booking Summary", "Payment", "Confirmation"};
        int totalSteps = 4;
        int w = getWidth();
        int stepWidth = w / totalSteps;
        int y = getHeight() / 2;

        // Draw connecting track line
        g2.setColor(ThemeManager.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine(stepWidth / 2, y, w - (stepWidth / 2), y);

        for (int i = 0; i < totalSteps; i++) {
            int stepNum = i + 1;
            int x = (i * stepWidth) + (stepWidth / 2);
            boolean isCompleted = stepNum < currentStep;
            boolean isActive = stepNum == currentStep;

            // Circle indicator
            int radius = 13;
            if (isActive) {
                g2.setColor(ThemeManager.ACCENT_RED);
                g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                g2.setColor(Color.WHITE);
            } else if (isCompleted) {
                g2.setColor(ThemeManager.STATUS_GREEN);
                g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                g2.setColor(ThemeManager.BORDER_SUBTLE);
                g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                g2.setColor(ThemeManager.TEXT_MUTED);
            }

            // Step number text
            g2.setFont(ThemeManager.FONT_SMALL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            String numStr = isCompleted ? "✓" : String.valueOf(stepNum);
            int nx = x - (fm.stringWidth(numStr) / 2);
            int ny = y + (fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(numStr, nx, ny);

            // Step title text next to circle
            g2.setFont(isActive ? ThemeManager.FONT_SMALL_BOLD : ThemeManager.FONT_SMALL);
            g2.setColor(isActive ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            String title = stepTitles[i];
            g2.drawString(title, x + radius + 6, y + 4);
        }

        g2.dispose();
    }
}
