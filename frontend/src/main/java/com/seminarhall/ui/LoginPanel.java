package com.seminarhall.ui;

import com.seminarhall.model.User;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.UserService;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.GlassPanel;
import com.seminarhall.ui.components.PhotoPanel;
import com.seminarhall.ui.components.RemoteImages;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.StyledPasswordField;
import com.seminarhall.ui.components.StyledTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The web app's LoginPage: the red theatre photo behind a centred 400px glass card with
 * "Welcome Back", email + password and the "Sign In →" button. Signs in through UserService.
 */
public class LoginPanel extends JPanel implements NavigationController.RefreshableScreen {
    private static final String STUDENT_EMAIL = "student@college.edu";
    private static final String STUDENT_PASSWORD = "student123";
    private static final String ORGANIZER_EMAIL = "organizer@college.edu";
    private static final String ORGANIZER_PASSWORD = "admin123";
    private static final String SIGN_IN_TEXT = "Sign In →";

    private final NavigationController navController;
    private final UserService userService;

    private StyledTextField emailField;
    private StyledPasswordField passwordField;
    private JLabel errorLabel;
    private StyledButton loginBtn;
    private JLabel demoPrompt;
    private JLabel demoLink;
    private boolean organizerDemoFilled;

    public LoginPanel(NavigationController navController) {
        this.navController = navController;
        this.userService = ServiceFactory.getUserService();

        setName(NavigationController.SCREEN_LOGIN);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // w-full max-w-[400px] glass-morphic rounded-3xl p-8
        AuthPage.Card card = new AuthPage.Card();
        card.addRow(AuthPage.heading("Welcome Back"), 0);
        card.addRow(AuthPage.subtitle("Login with your college account"), 4);

        // Pre-filled like the web form (defaultValue), with the working demo student account
        emailField = AuthPage.field(new StyledTextField("Email"), 42);
        emailField.setText(STUDENT_EMAIL);
        passwordField = AuthPage.field(new StyledPasswordField("Password"), 42);
        passwordField.setText(STUDENT_PASSWORD);
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> performLogin());

        errorLabel = AuthPage.errorLabel();

        loginBtn = AuthPage.submitButton(SIGN_IN_TEXT, 14, 48); // py-3.5 text-sm
        loginBtn.addActionListener(e -> performLogin());

        card.addRow(emailField, 24);     // mb-6
        card.addRow(passwordField, 16);  // space-y-4
        card.addRow(errorLabel, 12);
        card.addButton(loginBtn, 16);
        card.addRow(AuthPage.switchLine("Don't have an account?", "Register", () -> {
            clearError();
            navController.navigateTo(NavigationController.SCREEN_REGISTER);
        }), 24 - AuthPage.GLOW_BOTTOM); // mt-6

        // Small muted line under the card that swaps in the organizer demo account
        JPanel demoLine = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        demoLine.setOpaque(false);
        demoPrompt = new JLabel();
        demoPrompt.setFont(ThemeManager.font(Weight.REGULAR, 12));
        demoPrompt.setForeground(ThemeManager.TEXT_MUTED);
        demoLink = AuthPage.link("", ThemeManager.TEXT_GRAY_300, Weight.SEMIBOLD, this::toggleDemoAccount);
        demoLine.add(demoPrompt);
        demoLine.add(demoLink);
        updateDemoLine();

        add(AuthPage.backdrop(card, demoLine), BorderLayout.CENTER);
    }

    @Override
    public void onScreenDisplayed() {
        clearError();
        loginBtn.setEnabled(true);
        loginBtn.setText(SIGN_IN_TEXT);
    }

    private void toggleDemoAccount() {
        organizerDemoFilled = !organizerDemoFilled;
        emailField.setText(organizerDemoFilled ? ORGANIZER_EMAIL : STUDENT_EMAIL);
        passwordField.setText(organizerDemoFilled ? ORGANIZER_PASSWORD : STUDENT_PASSWORD);
        clearError();
        updateDemoLine();
    }

    private void updateDemoLine() {
        demoPrompt.setText(organizerDemoFilled ? "Student?" : "Faculty?");
        demoLink.setText(organizerDemoFilled ? "Use the student demo account" : "Use the organizer demo account");
    }

    private void clearError() {
        AuthPage.showError(errorLabel, null);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (email.isEmpty()) {
            AuthPage.showError(errorLabel, "Please enter your email address.");
            return;
        }
        if (pass.isEmpty()) {
            AuthPage.showError(errorLabel, "Please enter your password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing In...");
        clearError();

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.authenticate(email, pass);
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText(SIGN_IN_TEXT);
                try {
                    User user = get();
                    navController.onSignedIn(user);
                } catch (Exception ex) {
                    AuthPage.showError(errorLabel, AuthPage.errorMessage(ex, "Could not sign in. Please try again."));
                }
            }
        };
        worker.execute();
    }

    /**
     * Building blocks shared by the Login and Register pages (web: LoginPage / RegisterPage).
     */
    static final class AuthPage {
        /** Room StyledButton keeps around a glowing button for its shadow. */
        static final int GLOW_X = 8;
        static final int GLOW_TOP = 2;
        static final int GLOW_BOTTOM = 12;

        private AuthPage() {
        }

        /**
         * Full-page RED_THEATER photo with linear-gradient(to right, rgba(8,9,14,.96), rgba(8,9,14,.7)),
         * the card centred (flex items-center justify-center p-6), an optional line under it.
         */
        static JComponent backdrop(JComponent card, JComponent below) {
            PhotoPanel page = new Backdrop(card);
            page.setLayout(new GridBagLayout());
            page.setBorder(new EmptyBorder(24, 24, 24, 24));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            page.add(card, gbc);
            if (below != null) {
                gbc.gridy = 1;
                gbc.insets = new Insets(18, 0, 0, 0);
                page.add(below, gbc);
            }
            return page;
        }

        /** The photo page; under the card it paints a blurred copy of itself (backdrop-filter: blur(28px)). */
        private static final class Backdrop extends PhotoPanel {
            private static final int MARGIN = 48;
            private final JComponent card;
            private BufferedImage blurred;
            private List<Object> blurredFor;

            Backdrop(JComponent card) {
                super(EventVisuals.RED_THEATER, 0, PhotoPanel.Overlay.LEFT);
                this.card = card;
                RemoteImages.get(EventVisuals.RED_THEATER, null); // start loading now, not on first display
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage photo = RemoteImages.get(EventVisuals.RED_THEATER, this::repaint);
                Rectangle r = card.getBounds();
                if (photo == null || card.getParent() != this || r.isEmpty()) {
                    return;
                }
                List<Object> key = List.of(photo, getWidth(), getHeight(), r);
                if (!key.equals(blurredFor)) {
                    blurred = blur(r);
                    blurredFor = key;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = ThemeManager.RADIUS_3XL * 2;
                g2.clip(new RoundRectangle2D.Float(r.x, r.y, r.width - 1, r.height - 1, arc, arc));
                g2.drawImage(blurred, r.x - MARGIN, r.y - MARGIN, null);
                g2.dispose();
            }

            /** Paints the page around the card off-screen, then blurs it by scaling down 16x and back up. */
            private BufferedImage blur(Rectangle r) {
                int w = r.width + 2 * MARGIN;
                int h = r.height + 2 * MARGIN;
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.translate(-(r.x - MARGIN), -(r.y - MARGIN));
                super.paintComponent(g);
                g.dispose();
                for (int i = 0; i < 4; i++) {
                    image = scaled(image, Math.max(1, image.getWidth() / 2), Math.max(1, image.getHeight() / 2));
                }
                while (image.getWidth() * 2 <= w) {
                    image = scaled(image, image.getWidth() * 2, image.getHeight() * 2);
                }
                return scaled(image, w, h);
            }

            private static BufferedImage scaled(BufferedImage src, int w, int h) {
                BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = out.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(src, 0, 0, w, h, null);
                g.dispose();
                return out;
            }
        }

        /**
         * glass-morphic rounded-3xl p-8, 400px wide. Rows are stacked full width at their preferred
         * heights with explicit gaps; hidden rows (the error line) take no space.
         */
        static final class Card extends GlassPanel {
            private static final int WIDTH = 400;
            private final java.util.Map<Component, int[]> rows = new java.util.LinkedHashMap<>(); // {gap, outset}

            Card() {
                super(Style.MORPHIC, ThemeManager.RADIUS_3XL, null);
                setBorder(new EmptyBorder(32, 32, 32, 32));
                setLayout(new LayoutManager() {
                    @Override
                    public void addLayoutComponent(String name, Component comp) {
                    }

                    @Override
                    public void removeLayoutComponent(Component comp) {
                        rows.remove(comp);
                    }

                    @Override
                    public Dimension preferredLayoutSize(Container parent) {
                        Insets in = getInsets();
                        int height = in.top + in.bottom;
                        for (Map.Entry<Component, int[]> row : rows.entrySet()) {
                            if (row.getKey().isVisible()) {
                                height += row.getValue()[0] + row.getKey().getPreferredSize().height;
                            }
                        }
                        return new Dimension(WIDTH, height);
                    }

                    @Override
                    public Dimension minimumLayoutSize(Container parent) {
                        return preferredLayoutSize(parent);
                    }

                    @Override
                    public void layoutContainer(Container parent) {
                        Insets in = getInsets();
                        int width = getWidth() - in.left - in.right;
                        int y = in.top;
                        for (Map.Entry<Component, int[]> row : rows.entrySet()) {
                            Component c = row.getKey();
                            if (!c.isVisible()) {
                                continue;
                            }
                            int outset = row.getValue()[1];
                            int h = c.getPreferredSize().height;
                            y += row.getValue()[0];
                            c.setBounds(in.left - outset, y, width + 2 * outset, h);
                            y += h;
                        }
                    }
                });
            }

            /** Adds a full-width row, {@code gapAbove} px below the previous one. */
            void addRow(JComponent row, int gapAbove) {
                rows.put(row, new int[]{gapAbove, 0});
                add(row);
            }

            /** Adds a glowing button whose visible edges line up with the other rows. */
            void addButton(StyledButton button, int gapAbove) {
                rows.put(button, new int[]{gapAbove - GLOW_TOP, GLOW_X});
                add(button);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        }

        /** text-2xl font-black text-white */
        static JLabel heading(String text) {
            JLabel label = new JLabel(text);
            label.setFont(ThemeManager.font(Weight.EXTRABOLD, 24));
            label.setForeground(Color.WHITE);
            label.setPreferredSize(new Dimension(10, 32)); // line-height 2rem
            return label;
        }

        /** text-xs text-gray-400 font-light */
        static JLabel subtitle(String text) {
            JLabel label = new JLabel(text);
            label.setFont(ThemeManager.font(Weight.REGULAR, 12));
            label.setForeground(ThemeManager.TEXT_SECONDARY);
            label.setPreferredSize(new Dimension(10, 16));
            return label;
        }

        /** bg-[#080a10]/90 border-white/10 rounded-2xl px-4 text-xs; height 42 (py-3) or 38 (py-2.5). */
        static <T extends JTextField> T field(T field, int height) {
            field.setFont(ThemeManager.font(Weight.REGULAR, 12));
            field.setCaretColor(Color.WHITE);
            int padY = (height - 16) / 2;
            field.setBorder(new EmptyBorder(padY, 16, height - 16 - padY, 16));
            field.setPreferredSize(new Dimension(10, height));
            return field;
        }

        /** w-full glow-primary rounded-2xl font-bold text-white shadow-xl shadow-[#ff382e]/40 */
        static StyledButton submitButton(String text, int fontSize, int height) {
            StyledButton button = new StyledButton(text, StyledButton.ButtonStyle.PRIMARY);
            button.setFont(ThemeManager.font(Weight.BOLD, fontSize));
            button.setCornerRadius(ThemeManager.RADIUS_2XL);
            button.setGlow(true);
            button.setPreferredSize(new Dimension(10, height + GLOW_TOP + GLOW_BOTTOM));
            return button;
        }

        /** Red-400 text-xs message shown above the button; hidden while there is no error. */
        static JLabel errorLabel() {
            JLabel label = new JLabel();
            label.setFont(ThemeManager.font(Weight.SEMIBOLD, 12));
            label.setForeground(ThemeManager.RED_400);
            label.setIcon(com.seminarhall.ui.components.Icons.get("shield", 14, ThemeManager.RED_400));
            label.setIconTextGap(8);
            label.setVerticalTextPosition(SwingConstants.CENTER);
            label.setVisible(false);
            return label;
        }

        static void showError(JLabel label, String message) {
            if (message == null || message.isBlank()) {
                label.setText("");
                label.setVisible(false);
            } else {
                // Swing's CSS px are 4/3 screen pixels: 235px wraps at ~313px, the card width minus the icon
                label.setText("<html><div style='width:235px'>" + escape(message) + "</div></html>");
                label.setVisible(true);
            }
            Container parent = label.getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        }

        /** The service's message (not the wrapper exception's class name). */
        static String errorMessage(Exception ex, String fallback) {
            Throwable cause = ex;
            while ((cause instanceof ExecutionException || cause instanceof RuntimeException && cause.getMessage() == null)
                    && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            String message = cause.getMessage();
            return message == null || message.isBlank() ? fallback : message;
        }

        /** "Don't have an account? Register" — text-center text-xs text-gray-400, link text-[#ff382e] font-bold. */
        static JPanel switchLine(String text, String linkText, Runnable onClick) {
            JPanel line = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            line.setOpaque(false);
            JLabel prompt = new JLabel(text);
            prompt.setFont(ThemeManager.font(Weight.REGULAR, 12));
            prompt.setForeground(ThemeManager.TEXT_SECONDARY);
            line.add(prompt);
            line.add(link(linkText, ThemeManager.BRAND_RED, Weight.BOLD, onClick));
            return line;
        }

        /** Clickable text-xs label, underlined on hover. */
        static JLabel link(String text, Color color, Weight weight, Runnable onClick) {
            Font plain = ThemeManager.font(weight, 12);
            Font underlined = plain.deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON));
            JLabel link = new JLabel(text);
            link.setFont(plain);
            link.setForeground(color);
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    link.setFont(underlined);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    link.setFont(plain);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }
            });
            return link;
        }

        private static String escape(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
