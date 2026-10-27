package com.seminarhall.ui.components;

import com.seminarhall.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Buttons in the web app's styles:
 * <ul>
 *   <li>PRIMARY — .glow-primary: 135° gradient #ff382e → #ff6835 with a white top highlight;
 *       call {@link #setGlow(boolean)} for the red drop glow (adds room for it around the button)</li>
 *   <li>SECONDARY — bg-white/5 border-white/10, hover bg-white/10</li>
 *   <li>OUTLINE — transparent, border-white/10, hover border brand red</li>
 *   <li>SUCCESS — emerald</li>
 *   <li>TAB — text only, gray-400 → white on hover (header links)</li>
 * </ul>
 * An icon set with setIcon() is drawn before the text (or after it with
 * setHorizontalTextPosition(SwingConstants.LEFT)).
 */
public class StyledButton extends JButton {
    public enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        OUTLINE,
        SUCCESS,
        TAB
    }

    private static final int GLOW_X = 8;
    private static final int GLOW_TOP = 2;
    private static final int GLOW_BOTTOM = 12;

    private ButtonStyle buttonStyle;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = ThemeManager.RADIUS_XL;
    private boolean glow = false;

    public StyledButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public StyledButton(String text, ButtonStyle style) {
        super(text);
        this.buttonStyle = style;
        setFont(ThemeManager.font(ThemeManager.Weight.BOLD, 13));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(11, 22, 11, 22));
        setIconTextGap(8);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setButtonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
        repaint();
    }

    /** rounded-xl = 12 (default), rounded-2xl = 16, fully round = 999. */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /** Red glow under a PRIMARY button (box-shadow 0 8px 24px rgba(255,56,46,.55)); enlarges the preferred size to fit it. */
    public void setGlow(boolean glow) {
        this.glow = glow;
        revalidate();
        repaint();
    }

    private boolean glowing() {
        return glow && buttonStyle == ButtonStyle.PRIMARY && isEnabled();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        FontMetrics fm = getFontMetrics(getFont());
        Insets m = getMargin();
        int textW = getText() == null ? 0 : fm.stringWidth(getText());
        Icon icon = getIcon();
        int iconW = icon == null ? 0 : icon.getIconWidth() + (textW > 0 ? getIconTextGap() : 0);
        int contentH = Math.max(fm.getHeight(), icon == null ? 0 : icon.getIconHeight());
        int w = m.left + iconW + textW + m.right;
        int h = m.top + contentH + m.bottom;
        if (glow && buttonStyle == ButtonStyle.PRIMARY) {
            w += GLOW_X * 2;
            h += GLOW_TOP + GLOW_BOTTOM;
        }
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();
        if (glowing()) {
            x = GLOW_X;
            y = GLOW_TOP;
            width -= GLOW_X * 2;
            height -= GLOW_TOP + GLOW_BOTTOM;
            paintGlow(g2, x, y, width, height);
        }
        int arc = Math.min(cornerRadius * 2, height);

        Color textColor = ThemeManager.TEXT_PRIMARY;
        Color borderColor = null;

        if (!isEnabled()) {
            g2.setColor(new Color(40, 43, 55));
            g2.fillRoundRect(x, y, width, height, arc, arc);
            textColor = ThemeManager.TEXT_MUTED;
        } else if (buttonStyle == ButtonStyle.PRIMARY) {
            Color start = isPressed ? ThemeManager.ACCENT_RED_PRESSED
                    : (isHovered ? ThemeManager.ACCENT_RED_HOVER : ThemeManager.BRAND_RED);
            Color end = isHovered ? new Color(255, 87, 33) : ThemeManager.BRAND_ORANGE;
            g2.setPaint(new GradientPaint(x, y, start, x + width, y + height, end));
            g2.fillRoundRect(x, y, width, height, arc, arc);
            // inset 0 1px 1px rgba(255,255,255,.35)
            g2.setColor(new Color(255, 255, 255, isHovered ? 110 : 80));
            g2.drawLine(x + arc / 2, y + 1, x + width - arc / 2, y + 1);
        } else if (buttonStyle == ButtonStyle.SUCCESS) {
            g2.setColor(isHovered ? ThemeManager.STATUS_GREEN.brighter() : ThemeManager.STATUS_GREEN);
            g2.fillRoundRect(x, y, width, height, arc, arc);
        } else if (buttonStyle == ButtonStyle.SECONDARY) {
            g2.setColor(isHovered ? ThemeManager.WHITE_10 : ThemeManager.WHITE_5);
            g2.fillRoundRect(x, y, width - 1, height - 1, arc, arc);
            borderColor = ThemeManager.BORDER_CARD;
        } else if (buttonStyle == ButtonStyle.TAB) {
            textColor = isHovered ? ThemeManager.TEXT_PRIMARY : getForeground();
        } else { // OUTLINE
            if (isHovered) {
                g2.setColor(ThemeManager.WHITE_5);
                g2.fillRoundRect(x, y, width - 1, height - 1, arc, arc);
            }
            borderColor = isHovered ? ThemeManager.BRAND_RED : ThemeManager.BORDER_CARD;
        }

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
        }

        paintContent(g2, x, y, width, height, textColor);
        g2.dispose();
    }

    private void paintGlow(Graphics2D g2, int x, int y, int w, int h) {
        // A soft drop shadow built from widening, fading rounded rects, offset downwards
        int layers = 8;
        for (int i = layers; i >= 1; i--) {
            int spread = i;
            int alpha = (int) (isHovered ? 26 : 18) * (layers - i + 1) / layers;
            g2.setColor(new Color(255, 56, 46, alpha));
            int arc = Math.min(cornerRadius * 2, h) + spread * 2;
            g2.fillRoundRect(x - spread, y + 6 - spread / 2, w + spread * 2, h + spread, arc, arc);
        }
    }

    private void paintContent(Graphics2D g2, int x, int y, int width, int height, Color textColor) {
        String text = getText() == null ? "" : getText();
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        Icon icon = getIcon();
        int iconW = icon == null ? 0 : icon.getIconWidth();
        int gap = icon != null && !text.isEmpty() ? getIconTextGap() : 0;
        int total = iconW + gap + textW;
        int startX = x + (width - total) / 2;
        boolean iconAfter = getHorizontalTextPosition() == SwingConstants.LEFT;

        int textX = iconAfter ? startX : startX + iconW + gap;
        int textY = y + (height + fm.getAscent() - fm.getDescent()) / 2;
        if (icon != null) {
            int iconX = iconAfter ? startX + textW + gap : startX;
            icon.paintIcon(this, g2, iconX, y + (height - icon.getIconHeight()) / 2);
        }
        g2.setColor(textColor);
        g2.drawString(text, textX, textY);
    }
}
