package com.seminarhall.ui.components;

import com.seminarhall.ui.NavigationController;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Faculty Console sidebar (web OrganizerDashboardPage): "w-60 bg-[#10131d] border border-[#1d2232]
 * rounded-3xl p-6" with the SeminarX / FACULTY CONSOLE brand, the console menu and
 * "← Return to Student View" at the bottom.
 */
public class SidebarNavigation extends JPanel {
    public static final String SECTION_DASHBOARD = "Dashboard Overview";
    public static final String SECTION_MANAGE = "Manage Events";
    public static final String SECTION_CREATE = "Schedule Event";

    private static final Color CARD_BG = new Color(16, 19, 29);        // #10131d
    private static final Color CARD_BORDER = new Color(29, 34, 50);    // #1d2232
    private static final Color BUTTON_BG = new Color(20, 23, 36);      // #141724
    private static final Color BUTTON_BORDER = new Color(35, 39, 58);  // #23273a
    private static final Color BUTTON_HOVER = new Color(26, 30, 47);   // #1a1e2f

    public interface OnNavSelect {
        void onSelect(String section);
    }

    private final NavigationController navController;
    private final OnNavSelect onNavSelect;
    private final Map<String, NavItem> items = new LinkedHashMap<>();
    private String activeSection = SECTION_DASHBOARD;

    public SidebarNavigation(NavigationController navController, OnNavSelect onNavSelect) {
        this.navController = navController;
        this.onNavSelect = onNavSelect;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 25, 24, 25));

        buildUI();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(240, super.getPreferredSize().height);   // w-60
    }

    private void buildUI() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // Brand: "flex items-center gap-3 mb-6 pb-4 border-b border-[#1d2232]"
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, CARD_BORDER), new EmptyBorder(0, 0, 16, 0)));
        brand.add(new LogoMark(), BorderLayout.WEST);

        brand.add(new BrandText(), BorderLayout.CENTER);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 57));

        top.add(brand);
        top.add(Box.createVerticalStrut(24));

        // Menu: "space-y-1.5 text-xs font-semibold"
        // The web's emoji (📊 📑 ➕) as Lucide icons
        addItem(top, SECTION_DASHBOARD, "layout-dashboard");
        top.add(Box.createVerticalStrut(6));
        addItem(top, SECTION_MANAGE, "file-text");
        top.add(Box.createVerticalStrut(6));
        addItem(top, SECTION_CREATE, "plus");
        add(top, BorderLayout.NORTH);

        // Bottom: "pt-6 border-t border-[#1d2232]" + the return button
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, CARD_BORDER), new EmptyBorder(24, 0, 0, 0)));
        ReturnButton back = new ReturnButton("← Return to Student View");
        back.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD));
        bottom.add(back, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setActiveSection(SECTION_DASHBOARD);
    }

    private void addItem(JPanel menu, String section, String iconName) {
        NavItem item = new NavItem(section, iconName);
        item.addActionListener(e -> {
            if (onNavSelect != null) {
                onNavSelect.onSelect(section);
            }
        });
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        items.put(section, item);
        menu.add(item);
    }

    /** Highlights one menu item (bg-[#ff382e]/15 text-[#ff382e] font-bold). */
    public void setActiveSection(String section) {
        this.activeSection = section;
        for (Map.Entry<String, NavItem> entry : items.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(section));
        }
    }

    public String getActiveSection() {
        return activeSection;
    }

    /** font-mono (Consolas on Windows, as Chrome renders it). */
    private static Font monoFont(float size) {
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (family.equals("Consolas")) {
                return new Font("Consolas", Font.PLAIN, 1).deriveFont(size);
            }
        }
        return new Font(Font.MONOSPACED, Font.PLAIN, 1).deriveFont(size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ThemeManager.RADIUS_3XL * 2;
        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
    }

    /** "w-10 h-10 rounded-xl bg-gradient-to-r from-[#ff382e] to-[#ff6835]" with a black-weight Σ. */
    private static final class LogoMark extends JComponent {
        private final Font font;

        LogoMark() {
            Font jakarta = ThemeManager.font(Weight.EXTRABOLD, 18);
            font = jakarta.canDisplay('Σ') ? jakarta : sigmaFallback();
            setPreferredSize(new Dimension(40, 40));
        }

        private static Font sigmaFallback() {
            for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (family.equals("Segoe UI Black")) {
                    return new Font("Segoe UI Black", Font.PLAIN, 18);
                }
            }
            return new Font("Segoe UI", Font.BOLD, 18);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int y = (getHeight() - 40) / 2;
            g2.setPaint(new GradientPaint(0, 0, ThemeManager.BRAND_RED, 40, 0, ThemeManager.BRAND_ORANGE));
            g2.fillRoundRect(0, y, 40, 40, 24, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Σ", (40 - fm.stringWidth("Σ")) / 2, y + (40 + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** "Seminar<span text-[#ff382e]>X</span>" (font-black text-sm) over "FACULTY CONSOLE" (text-[9px] gray-500 font-mono). */
    private static final class BrandText extends JComponent {
        private final Font wordFont = ThemeManager.font(Weight.EXTRABOLD, 14);
        private final Font consoleFont = monoFont(9);

        BrandText() {
            setPreferredSize(new Dimension(120, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            int top = (getHeight() - 34) / 2;   // a 20px and a 14px line, centred on the logo
            g2.setFont(wordFont);
            FontMetrics fm = g2.getFontMetrics();
            int baseline = top + (20 + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(Color.WHITE);
            g2.drawString("Seminar", 0, baseline);
            g2.setColor(ThemeManager.BRAND_RED);
            g2.drawString("X", (float) fm.getStringBounds("Seminar", g2).getWidth(), baseline);

            g2.setFont(consoleFont);
            FontMetrics cfm = g2.getFontMetrics();
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.drawString("FACULTY CONSOLE", 0, top + 20 + (14 + cfm.getAscent() - cfm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Menu row: "px-3.5 py-2 rounded-xl flex items-center gap-2.5"; idle gray-400, hover white on white/5. */
    private static final class NavItem extends JButton {
        private final Icon activeIcon;
        private final Icon idleIcon;
        private final Icon hoverIcon;
        private boolean active;
        private boolean hovered;

        NavItem(String text, String iconName) {
            super(text);
            this.activeIcon = icon(iconName, ThemeManager.BRAND_RED);
            this.idleIcon = icon(iconName, ThemeManager.TEXT_SECONDARY);
            this.hoverIcon = icon(iconName, Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        private static Icon icon(String name, Color color) {
            return "plus".equals(name) ? new PlusIcon(14, color) : Icons.get(name, 14, color);
        }

        void setActive(boolean active) {
            this.active = active;
            setFont(ThemeManager.font(active ? Weight.BOLD : Weight.SEMIBOLD, 12));
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(14 + 14 + 10 + fm.stringWidth(getText()) + 14, 32);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color text;
            Icon icon;
            if (active) {
                g2.setColor(new Color(255, 56, 46, 38));   // bg-[#ff382e]/15
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                text = ThemeManager.BRAND_RED;
                icon = activeIcon;
            } else if (hovered) {
                g2.setColor(ThemeManager.WHITE_5);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                text = Color.WHITE;
                icon = hoverIcon;
            } else {
                text = ThemeManager.TEXT_SECONDARY;
                icon = idleIcon;
            }
            icon.paintIcon(this, g2, 14, (getHeight() - icon.getIconHeight()) / 2);
            g2.setFont(getFont());
            g2.setColor(text);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 14 + icon.getIconWidth() + 10, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** "w-full bg-[#141724] border border-[#23273a] hover:bg-[#1a1e2f] text-gray-300 text-xs py-2.5 rounded-xl font-semibold". */
    private static final class ReturnButton extends JButton {
        private boolean hovered;

        ReturnButton(String text) {
            super(text);
            setFont(ThemeManager.font(Weight.SEMIBOLD, 12));
            setForeground(ThemeManager.TEXT_GRAY_300);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 24, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(hovered ? BUTTON_HOVER : BUTTON_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.setColor(BUTTON_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Lucide-style "plus" (two round-capped strokes) for the web's ➕ Schedule Event item. */
    static final class PlusIcon implements Icon {
        private final int size;
        private final Color color;

        PlusIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            float scale = size / 24f;
            g2.translate(x, y);
            g2.scale(scale, scale);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new java.awt.geom.Line2D.Float(5, 12, 19, 12));
            g2.draw(new java.awt.geom.Line2D.Float(12, 5, 12, 19));
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}
