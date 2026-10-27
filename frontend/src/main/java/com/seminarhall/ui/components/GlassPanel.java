package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Rounded translucent container matching the web app's card styles:
 * <ul>
 *   <li>{@link Style#CARD} — .glass-card: rgba(18,21,32,.82), 1px white/8 border; red border on hover if hoverable</li>
 *   <li>{@link Style#MORPHIC} — .glass-morphic: rgba(14,16,25,.78), 1px white/12 border (login box, side panels)</li>
 *   <li>{@link Style#SOLID} — bg-[#12141c] border-[#222533] (tables, dashboard tiles)</li>
 *   <li>{@link Style#SUBTLE} — bg-white/5 border-white/10 (small info boxes, chips)</li>
 * </ul>
 * Children are laid out with whatever LayoutManager you set; give it an EmptyBorder for padding.
 */
public class GlassPanel extends JPanel {

    public enum Style { CARD, MORPHIC, SOLID, SUBTLE }

    private Style style;
    private int radius;
    private boolean hoverable;
    private boolean hovered;
    private Color borderOverride;

    public GlassPanel(Style style, int radius) {
        this(style, radius, null);
    }

    public GlassPanel(Style style, int radius, LayoutManager layout) {
        super(layout != null ? layout : new FlowLayout());
        this.style = style;
        this.radius = radius;
        setOpaque(false);
    }

    /** Hover lifts the border to brand red, like .glass-card:hover. Also shows a hand cursor. */
    public void setHoverable(boolean hoverable) {
        this.hoverable = hoverable;
        if (hoverable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter tracker = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setHovered(true); }
                @Override public void mouseExited(MouseEvent e) {
                    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), GlassPanel.this);
                    if (!new Rectangle(getSize()).contains(p)) setHovered(false);
                }
            };
            addMouseListener(tracker);
        }
    }

    /** Use a specific border color (e.g. brand red for a selected card). Null restores the style's border. */
    public void setBorderColor(Color color) {
        this.borderOverride = color;
        repaint();
    }

    public void setStyle(Style style) {
        this.style = style;
        repaint();
    }

    private void setHovered(boolean hovered) {
        if (this.hovered != hovered) {
            this.hovered = hovered;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arc = radius * 2;

        Color fill;
        Color border;
        switch (style) {
            case MORPHIC:
                fill = new Color(14, 16, 25, 205);
                border = ThemeManager.BORDER_GLASS;
                break;
            case SOLID:
                fill = ThemeManager.BG_SECONDARY;
                border = new Color(34, 37, 51);
                break;
            case SUBTLE:
                fill = ThemeManager.WHITE_5;
                border = ThemeManager.BORDER_CARD;
                break;
            default:
                fill = new Color(18, 21, 32, 215);
                border = ThemeManager.BORDER_SUBTLE;
                break;
        }
        if (hoverable && hovered) {
            border = new Color(255, 56, 46, 115);
        }
        if (borderOverride != null) {
            border = borderOverride;
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
        if (style == Style.MORPHIC) {
            // inset 0 1px 1px rgba(255,255,255,.15): faint top highlight
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawLine(radius, 1, w - radius, 1);
        }
        g2.setColor(border);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.dispose();
    }
}
