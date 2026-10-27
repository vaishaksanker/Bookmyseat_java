package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import java.awt.*;

/**
 * The web app's booking steps, shown in the seat page header:
 * "Step 1: Select Seats → Step 2: Summary → Step 3: Payment"
 * (flex gap-4 text-xs font-semibold; the current step gray-400, the others gray-500, arrows gray-600).
 */
public class StepIndicatorBar extends JComponent {
    private static final String[] STEPS = {"Step 1: Select Seats", "Step 2: Summary", "Step 3: Payment"};
    private static final String ARROW = "→";
    private static final int GAP = 16;
    private static final Font FONT = ThemeManager.font(Weight.SEMIBOLD, 12);

    private int currentStep; // 1: Select Seats, 2: Summary, 3: Payment

    public StepIndicatorBar(int currentStep) {
        this.currentStep = currentStep;
        setOpaque(false);
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(FONT);
        int w = 0;
        for (String step : STEPS) w += fm.stringWidth(step);
        w += fm.stringWidth(ARROW) * (STEPS.length - 1) + GAP * (STEPS.length * 2 - 2);
        return new Dimension(w, 16);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(FONT);
        FontMetrics fm = g2.getFontMetrics();
        int x = getWidth() - getPreferredSize().width;
        int baseline = (getHeight() - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
        for (int i = 0; i < STEPS.length; i++) {
            if (i > 0) {
                g2.setColor(BookingFlowParts.GRAY_600);
                g2.drawString(ARROW, x, baseline);
                x += fm.stringWidth(ARROW) + GAP;
            }
            g2.setColor(i + 1 == currentStep ? ThemeManager.TEXT_SECONDARY : ThemeManager.TEXT_MUTED);
            g2.drawString(STEPS[i], x, baseline);
            x += fm.stringWidth(STEPS[i]) + GAP;
        }
        g2.dispose();
    }
}
