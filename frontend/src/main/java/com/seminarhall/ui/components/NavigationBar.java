package com.seminarhall.ui.components;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.ui.NavigationController;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import java.util.Map;

/**
 * The web app's sticky header (App.jsx &lt;header&gt;): SeminarX logo with "CAMPUS AUDITORIUMS",
 * the Explore Events / My Bookings / Faculty Console links, the search box and the user chip
 * (or a Sign In button for guests).
 *
 * Web classes: bg-[#0d0f17]/95 border-b border-[#1c202e] px-8 py-3, content max-w-7xl,
 * flex justify-between gap-4 with the search box flex-1 max-w-md.
 */
public class NavigationBar extends JPanel {
    private static final Color HEADER_BG = new Color(13, 15, 23);       // #0d0f17
    private static final Color HEADER_BORDER = new Color(28, 32, 46);   // #1c202e
    private static final Color SEARCH_BG = new Color(17, 20, 32);       // #111420
    private static final Color CONTROL_BORDER = new Color(33, 38, 54);  // #212636
    private static final Color CHIP_BG = new Color(18, 21, 34);         // #121522
    private static final Color GRAY_200 = new Color(229, 231, 235);
    private static final Color GRAY_700 = new Color(55, 65, 81);
    private static final Color GRAY_900 = new Color(17, 24, 39);

    private static final int MAX_CONTENT = 1280; // max-w-7xl
    private static final int PAD_X = 32;         // px-8
    private static final int PAD_Y = 12;         // py-3
    private static final int GAP = 16;           // gap-4
    private static final int SEARCH_MAX = 448;   // max-w-md

    private final NavigationController navController;
    private final Brand brand = new Brand();
    private final NavLink exploreLink;
    private final NavLink bookingsLink;
    private final NavLink consoleLink;
    private final SearchBox searchBox;
    private final UserChip userChip = new UserChip();
    private final StyledButton signInButton;
    private final JPanel left;
    private final JPanel right;

    public NavigationBar(NavigationController navController) {
        this.navController = navController;
        setOpaque(false);
        setLayout(new HeaderLayout());

        brand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD);
            }
        });

        exploreLink = new NavLink("Explore Events",
                () -> navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD));
        bookingsLink = new NavLink("My Bookings",
                () -> navController.navigateTo(NavigationController.SCREEN_BOOKING_HISTORY));
        consoleLink = new NavLink("Faculty Console",
                () -> navController.navigateTo(NavigationController.SCREEN_ORGANIZER_DASHBOARD));

        // Logo + links: flex items-center gap-6
        left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(brand);
        left.add(Box.createHorizontalStrut(24));
        left.add(exploreLink);
        left.add(Box.createHorizontalStrut(24));
        left.add(bookingsLink);
        left.add(Box.createHorizontalStrut(24));
        left.add(consoleLink);

        searchBox = new SearchBox(query -> navController.showSearchResults(query));

        signInButton = new StyledButton("Sign In", StyledButton.ButtonStyle.PRIMARY);
        signInButton.setFont(ThemeManager.font(Weight.BOLD, 12));
        signInButton.setMargin(new Insets(9, 18, 9, 18));
        signInButton.addActionListener(e -> navController.navigateTo(NavigationController.SCREEN_LOGIN));

        userChip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showAccountMenu();
            }
        });

        right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(signInButton, BorderLayout.CENTER);

        add(left);
        add(searchBox);
        add(right);
    }

    /** Highlights the link for this screen and shows the signed-in user (or Sign In for guests). */
    public void updateState(String screen, User user) {
        exploreLink.setActive(NavigationController.SCREEN_STUDENT_DASHBOARD.equals(screen)
                || NavigationController.SCREEN_EVENT_LIST.equals(screen));
        bookingsLink.setActive(NavigationController.SCREEN_BOOKING_HISTORY.equals(screen));
        consoleLink.setActive(NavigationController.SCREEN_ORGANIZER_DASHBOARD.equals(screen));

        right.removeAll();
        if (user != null) {
            userChip.setUser(user);
            right.add(userChip, BorderLayout.CENTER);
        } else {
            right.add(signInButton, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private void showAccountMenu() {
        User user = userChip.user;
        if (user == null) {
            return;
        }
        JPopupMenu menu = buildAccountMenu(user);
        Dimension size = menu.getPreferredSize();
        int width = Math.max(size.width, userChip.getWidth());
        menu.setPopupSize(width, size.height);
        menu.show(userChip, userChip.getWidth() - width, userChip.getHeight() + 6);
    }

    /** The user chip's menu: who is signed in, My Bookings, Faculty Console (organizers) and Sign Out. */
    private JPopupMenu buildAccountMenu(User user) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ThemeManager.BG_SECONDARY);
        menu.setBorder(new CompoundBorder(new LineBorder(CONTROL_BORDER, 1), new EmptyBorder(6, 0, 6, 0)));

        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        who.setBorder(new EmptyBorder(6, 14, 8, 14));
        JLabel name = new JLabel(user.getFullName());
        name.setFont(ThemeManager.font(Weight.BOLD, 12));
        name.setForeground(ThemeManager.TEXT_PRIMARY);
        JLabel email = new JLabel(user.getEmail());
        email.setFont(ThemeManager.font(Weight.REGULAR, 11));
        email.setForeground(ThemeManager.TEXT_MUTED);
        who.add(name);
        who.add(Box.createVerticalStrut(2));
        who.add(email);
        menu.add(who);
        menu.addSeparator();

        menu.add(menuItem("My Bookings", "ticket", GRAY_200,
                () -> navController.navigateTo(NavigationController.SCREEN_BOOKING_HISTORY)));
        if (user.getRole() == UserRole.ORGANIZER) {
            menu.add(menuItem("Faculty Console", "layout-dashboard", GRAY_200,
                    () -> navController.navigateTo(NavigationController.SCREEN_ORGANIZER_DASHBOARD)));
        }
        menu.addSeparator();
        menu.add(menuItem("Sign Out", "x", ThemeManager.RED_400, navController::logout));
        return menu;
    }

    private static JMenuItem menuItem(String text, String icon, Color color, Runnable action) {
        JMenuItem item = new JMenuItem(text, Icons.get(icon, 14, color));
        item.setFont(ThemeManager.font(Weight.SEMIBOLD, 12));
        item.setForeground(color);
        item.setIconTextGap(10);
        item.setMargin(new Insets(7, 6, 7, 14));
        item.putClientProperty("FlatLaf.style",
                "selectionBackground: #1d2131; selectionForeground: " + (color == GRAY_200 ? "#ffffff" : "#f87171"));
        item.addActionListener(e -> action.run());
        return item;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(HEADER_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(HEADER_BORDER);
        g2.fillRect(0, getHeight() - 1, getWidth(), 1);

        // Logo mark glow: shadow-[0_0_20px_rgba(255,56,46,0.65)]
        Rectangle mark = SwingUtilities.convertRectangle(brand, brand.markBounds(), this);
        g2.clipRect(0, 0, getWidth(), getHeight() - 1);
        g2.drawImage(Brand.GLOW, mark.x - Brand.GLOW_PAD, mark.y - Brand.GLOW_PAD, null);
        g2.dispose();
    }

    // ------------------------------------------------------------------------------------------

    /** Lays out [logo + links] [search] [user] like flex justify-between with a flex-1 max-w-md search. */
    private final class HeaderLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int h = Math.max(left.getPreferredSize().height,
                    Math.max(searchBox.getPreferredSize().height, right.getPreferredSize().height));
            h = Math.max(h, 38); // the user chip's height, so the header does not jump on sign-in
            int w = left.getPreferredSize().width + 300 + right.getPreferredSize().width + 2 * PAD_X;
            return new Dimension(w, PAD_Y + h + PAD_Y + 1);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
            int height = parent.getHeight() - 1; // bottom border
            int contentW = Math.min(MAX_CONTENT, width - 2 * PAD_X);
            int x0 = (width - contentW) / 2;

            Dimension l = left.getPreferredSize();
            Dimension r = right.getPreferredSize();
            Dimension s = searchBox.getPreferredSize();
            int searchW = Math.max(0, Math.min(SEARCH_MAX, contentW - l.width - r.width - 2 * GAP));
            int gap = (contentW - l.width - r.width - searchW) / 2;

            left.setBounds(x0, (height - l.height) / 2, l.width, l.height);
            searchBox.setBounds(x0 + l.width + gap, (height - s.height) / 2, searchW, s.height);
            searchBox.setVisible(searchW >= 160);
            right.setBounds(x0 + contentW - r.width, (height - r.height) / 2, r.width, r.height);
        }
    }

    /** Logo: gradient "Σ" tile, "Seminar" + red "X" (text-xl font-black tracking-tight), "CAMPUS AUDITORIUMS". */
    private static final class Brand extends JComponent {
        static final int MARK = 36;
        static final int GLOW_PAD = 30;
        static final BufferedImage GLOW = glow();

        private final Font wordFont = ThemeManager.font(Weight.EXTRABOLD, 20)
                .deriveFont(Map.of(TextAttribute.TRACKING, -0.025f));
        private final Font subFont = monoFont().deriveFont(Map.of(TextAttribute.TRACKING, 0.1f));
        private final Font sigmaFont = sigmaFont();

        Brand() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Home");
        }

        Rectangle markBounds() {
            return new Rectangle(0, (getHeight() - MARK) / 2, MARK, MARK);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics wm = getFontMetrics(wordFont);
            FontMetrics sm = getFontMetrics(subFont);
            int textW = Math.max(wm.stringWidth("SeminarX"), sm.stringWidth("CAMPUS AUDITORIUMS"));
            return new Dimension(MARK + 10 + textW + 2, 38);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            // w-9 h-9 rounded-xl bg-gradient-to-r from-[#ff382e] to-[#ff6835], font-black text-lg "Σ"
            Rectangle m = markBounds();
            g2.setPaint(new GradientPaint(m.x, 0, ThemeManager.BRAND_RED, m.x + MARK, 0, ThemeManager.BRAND_ORANGE));
            g2.fillRoundRect(m.x, m.y, MARK, MARK, 24, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(sigmaFont);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Σ", m.x + (MARK - fm.stringWidth("Σ")) / 2f,
                    m.y + (MARK + fm.getAscent() - fm.getDescent()) / 2f);

            // Two lines: 28px line box for the wordmark, 9px (leading-none) for the subline
            int x = MARK + 10;
            int top = (getHeight() - 37) / 2;
            g2.setFont(wordFont);
            fm = g2.getFontMetrics();
            float base = top + (28 - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString("Seminar", x, base);
            g2.setColor(ThemeManager.BRAND_RED);
            g2.drawString("X", x + fm.stringWidth("Seminar"), base);

            g2.setFont(subFont);
            fm = g2.getFontMetrics();
            float subBase = top + 28 + (9 - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            g2.drawString("CAMPUS AUDITORIUMS", x, subBase);
            g2.dispose();
        }

        private static Font monoFont() {
            Font consolas = new Font("Consolas", Font.PLAIN, 9);
            return "Consolas".equals(consolas.getFamily()) ? consolas : new Font(Font.MONOSPACED, Font.PLAIN, 9);
        }

        /** Plus Jakarta Sans has no Greek letters; the browser falls back to Arial, so do the same. */
        private static Font sigmaFont() {
            Font arial = new Font("Arial", Font.BOLD, 18);
            return arial.canDisplay('Σ') ? arial : new Font(Font.SANS_SERIF, Font.BOLD, 18);
        }

        /** The mark's red glow, blurred once (three box blurs ~ a 10px-sigma gaussian). */
        private static BufferedImage glow() {
            int size = MARK + 2 * GLOW_PAD;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(255, 56, 46, 0)); // keep the colour in transparent pixels so the blur stays red
            g.fillRect(0, 0, size, size);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 56, 46, 166));
            g.fillRoundRect(GLOW_PAD, GLOW_PAD, MARK, MARK, 24, 24);
            g.dispose();

            int radius = 8;
            float[] weights = new float[radius * 2 + 1];
            Arrays.fill(weights, 1f / weights.length);
            ConvolveOp horizontal = new ConvolveOp(new Kernel(weights.length, 1, weights), ConvolveOp.EDGE_NO_OP, null);
            ConvolveOp vertical = new ConvolveOp(new Kernel(1, weights.length, weights), ConvolveOp.EDGE_NO_OP, null);
            for (int pass = 0; pass < 3; pass++) {
                img = vertical.filter(horizontal.filter(img, null), null);
            }
            return img;
        }
    }

    /** Header link: text-xs font-semibold text-gray-300 hover:text-white; active text-[#ff382e] font-bold. */
    private static final class NavLink extends JLabel {
        private boolean active;
        private boolean hovered;

        NavLink(String text, Runnable onClick) {
            super(text);
            setBorder(new EmptyBorder(4, 0, 4, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    restyle();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    restyle();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }
            });
            restyle();
        }

        void setActive(boolean active) {
            this.active = active;
            restyle();
        }

        private void restyle() {
            setFont(ThemeManager.font(active ? Weight.BOLD : Weight.SEMIBOLD, 12));
            setForeground(active ? ThemeManager.BRAND_RED : hovered ? Color.WHITE : ThemeManager.TEXT_GRAY_300);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    /**
     * bg-[#111420] border border-[#212636] rounded-xl px-3.5 py-2 gap-2.5, focus-within:border-[#ff382e];
     * search icon, text-xs input, and a clear button while there is text. Enter shows the results.
     */
    private static final class SearchBox extends JPanel {
        private final JTextField field;
        private boolean focused;

        SearchBox(java.util.function.Consumer<String> onSearch) {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(9, 15, 9, 15)); // 1px border + px-3.5 py-2

            field = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getText().isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setFont(getFont());
                        g2.setColor(ThemeManager.TEXT_MUTED);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString("Search for events, seminars, workshops, halls...", 0,
                                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                        g2.dispose();
                    }
                }
            };
            field.setFont(ThemeManager.font(Weight.REGULAR, 12));
            field.setForeground(Color.WHITE);
            field.setCaretColor(Color.WHITE);
            field.setSelectionColor(new Color(255, 56, 46, 90));
            field.setOpaque(false);
            field.setBorder(new EmptyBorder(0, 0, 0, 0));
            field.setPreferredSize(new Dimension(100, 16));
            field.addActionListener(e -> onSearch.accept(field.getText().trim()));
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });

            JLabel clear = Icons.label("x", 12, ThemeManager.TEXT_MUTED);
            clear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clear.setToolTipText("Clear search");
            clear.setVisible(false);
            clear.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    clear.setIcon(Icons.get("x", 12, Color.WHITE));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    clear.setIcon(Icons.get("x", 12, ThemeManager.TEXT_MUTED));
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    field.setText("");
                    field.requestFocusInWindow();
                }
            });
            field.getDocument().addDocumentListener(new DocumentListener() {
                private void update() {
                    clear.setVisible(!field.getText().isEmpty());
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });

            add(Icons.label("search", 14, ThemeManager.TEXT_MUTED), BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
            add(clear, BorderLayout.EAST);

            // Clicking anywhere in the box focuses the input
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    field.requestFocusInWindow();
                }
            });
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = ThemeManager.RADIUS_XL * 2;
            g2.setColor(SEARCH_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(focused ? ThemeManager.BRAND_RED : CONTROL_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
        }
    }

    /**
     * bg-[#121522] border border-[#212636] hover:border-white/20 px-3 py-1.5 rounded-xl gap-2:
     * initials avatar (w-6 h-6 rounded-lg gradient gray-700 → gray-900), name (text-xs bold gray-200)
     * and role (text-[9px] gray-400).
     */
    private static final class UserChip extends JComponent {
        private final Font nameFont = ThemeManager.font(Weight.BOLD, 12);
        private final Font roleFont = ThemeManager.font(Weight.REGULAR, 9);
        private final Font initialsFont = ThemeManager.font(Weight.BOLD, 10);
        private User user;
        private String name = "";
        private String role = "";
        private String initials = "";
        private boolean hovered;

        UserChip() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Account");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        void setUser(User user) {
            this.user = user;
            String full = user.getFullName() == null || user.getFullName().isBlank()
                    ? user.getEmail() : user.getFullName().trim();
            name = ellipsize(full, getFontMetrics(nameFont), 170);
            role = user.getRole() == UserRole.ORGANIZER ? "Organizer" : "Student";
            initials = initials(full);
            revalidate();
            repaint();
        }

        private static String initials(String fullName) {
            StringBuilder sb = new StringBuilder();
            for (String part : fullName.split("[\\s@._-]+")) {
                if (!part.isEmpty() && sb.length() < 2) {
                    sb.append(Character.toUpperCase(part.charAt(0)));
                }
            }
            return sb.length() > 0 ? sb.toString() : "?";
        }

        private static String ellipsize(String text, FontMetrics fm, int maxWidth) {
            if (fm.stringWidth(text) <= maxWidth) {
                return text;
            }
            String cut = text;
            while (cut.length() > 1 && fm.stringWidth(cut + "…") > maxWidth) {
                cut = cut.substring(0, cut.length() - 1);
            }
            return cut.trim() + "…";
        }

        @Override
        public Dimension getPreferredSize() {
            int textW = Math.max(getFontMetrics(nameFont).stringWidth(name), getFontMetrics(roleFont).stringWidth(role));
            return new Dimension(1 + 12 + 24 + 8 + textW + 12 + 1, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = ThemeManager.RADIUS_XL * 2;
            g2.setColor(CHIP_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(hovered ? new Color(255, 255, 255, 51) : CONTROL_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Avatar: bg-gradient-to-tr from-gray-700 to-gray-900, border white/20
            int ax = 13;
            int ay = (h - 24) / 2;
            g2.setPaint(new GradientPaint(ax, ay + 24, GRAY_700, ax + 24, ay, GRAY_900));
            g2.fillRoundRect(ax, ay, 24, 24, 16, 16);
            g2.setColor(new Color(255, 255, 255, 51));
            g2.drawRoundRect(ax, ay, 23, 23, 16, 16);
            g2.setFont(initialsFont);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(initials, ax + (24 - fm.stringWidth(initials)) / 2f,
                    ay + (24 + fm.getAscent() - fm.getDescent()) / 2f);

            // Name (leading-tight, 15px line) over role (leading-none, 9px line)
            int tx = ax + 24 + 8;
            g2.setFont(nameFont);
            fm = g2.getFontMetrics();
            g2.setColor(GRAY_200);
            g2.drawString(name, tx, ay + (15 - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent());
            g2.setFont(roleFont);
            fm = g2.getFontMetrics();
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            g2.drawString(role, tx, ay + 15 + (9 - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }
}
