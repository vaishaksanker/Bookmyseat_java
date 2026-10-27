package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * A panel that paints a photo as a "bg-cover bg-center" background (fills and crops),
 * optionally with rounded corners and a darkening overlay like the web app's
 * linear-gradient(...) over url(...). Child components are laid out on top as usual.
 * While the photo loads (or if it cannot be loaded) a dark red-tinted gradient is shown.
 */
public class PhotoPanel extends JPanel {

    /** How the photo is darkened so text on top stays readable. */
    public enum Overlay {
        NONE,
        /** to top: dark at the bottom (event cards: bg-gradient-to-t from-black/80) */
        BOTTOM,
        /** to right: dark on the left (hero and login: rgba(8,9,14,.96) → .7) */
        LEFT,
        /** uniform dim */
        DIM,
        /** home hero: rgba(7,8,12) at .96 → .7 (55%) → .2 */
        HERO,
        /** event details backdrop: .96 → .7 (50%) → .96, dark on both sides */
        SIDES
    }

    private String url;
    private int radius;
    private Overlay overlay;
    private float overlayStrength = 1f;

    public PhotoPanel(String url, int radius, Overlay overlay) {
        this.url = url;
        this.radius = radius;
        this.overlay = overlay;
        setOpaque(false);
    }

    public void setUrl(String url) {
        this.url = url;
        repaint();
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
        repaint();
    }

    /** 0..1, scales the overlay opacity. */
    public void setOverlayStrength(float strength) {
        this.overlayStrength = Math.max(0f, Math.min(1f, strength));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        Shape clip = radius > 0 ? new RoundRectangle2D.Float(0, 0, w, h, radius * 2f, radius * 2f) : new Rectangle(0, 0, w, h);
        g2.clip(clip);

        BufferedImage image = RemoteImages.get(url, this::repaint);
        if (image != null) {
            double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
            int iw = (int) Math.ceil(image.getWidth() * scale);
            int ih = (int) Math.ceil(image.getHeight() * scale);
            g2.drawImage(image, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(40, 14, 16), w, h, ThemeManager.BG_SECONDARY));
            g2.fillRect(0, 0, w, h);
        }

        int a = (int) (255 * overlayStrength);
        switch (overlay) {
            case BOTTOM:
                g2.setPaint(new GradientPaint(0, h * 0.35f, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, (int) (0.85 * a))));
                g2.fillRect(0, 0, w, h);
                break;
            case LEFT:
                g2.setPaint(new GradientPaint(0, 0, new Color(8, 9, 14, (int) (0.96 * a)), w, 0, new Color(8, 9, 14, (int) (0.70 * a))));
                g2.fillRect(0, 0, w, h);
                break;
            case DIM:
                g2.setColor(new Color(5, 6, 9, (int) (0.80 * a)));
                g2.fillRect(0, 0, w, h);
                break;
            case HERO:
                g2.setPaint(new LinearGradientPaint(0, 0, Math.max(1, w), 0, new float[]{0f, 0.55f, 1f},
                        new Color[]{new Color(7, 8, 12, (int) (0.96 * a)), new Color(7, 8, 12, (int) (0.70 * a)),
                                new Color(7, 8, 12, (int) (0.20 * a))}));
                g2.fillRect(0, 0, w, h);
                break;
            case SIDES:
                g2.setPaint(new LinearGradientPaint(0, 0, Math.max(1, w), 0, new float[]{0f, 0.5f, 1f},
                        new Color[]{new Color(7, 8, 12, (int) (0.96 * a)), new Color(7, 8, 12, (int) (0.70 * a)),
                                new Color(7, 8, 12, (int) (0.96 * a))}));
                g2.fillRect(0, 0, w, h);
                break;
            default:
                break;
        }
        g2.dispose();
    }
}
