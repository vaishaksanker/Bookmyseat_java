package com.seminarhall.ui;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.SessionManager;
import com.seminarhall.service.UserService;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.StyledPasswordField;
import com.seminarhall.ui.components.StyledTextField;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final NavigationController navController;
    private final UserService userService;

    private StyledTextField emailField;
    private StyledPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;
    private JLabel errorLabel;
    private StyledButton loginBtn;

    public LoginPanel(NavigationController navController) {
        this.navController = navController;
        this.userService = ServiceFactory.getUserService();

        setName(NavigationController.SCREEN_LOGIN);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Two-column split layout: Left Hero Branding (Auditorium Photo Background effect), Right Glass Card
        JPanel mainSplit = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Dark atmospheric auditorium gradient with warm ambient lighting
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(45, 12, 10),
                        w / 2, h, new Color(11, 12, 16)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Decorative auditorium stage glow
                g2.setColor(new Color(255, 68, 56, 18));
                g2.fillOval(-100, -100, 600, 600);

                g2.dispose();
            }
        };
        mainSplit.setOpaque(false);

        JPanel contentBox = new JPanel(new GridBagLayout());
        contentBox.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 30, 20, 30);
        gbc.fill = GridBagConstraints.BOTH;

        // LEFT: Branding block
        JPanel leftBrand = new JPanel();
        leftBrand.setOpaque(false);
        leftBrand.setLayout(new BoxLayout(leftBrand, BoxLayout.Y_AXIS));
        leftBrand.setPreferredSize(new Dimension(380, 480));

        // Red SX Logo Icon
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ThemeManager.ACCENT_RED, getWidth(), getHeight(), ThemeManager.ACCENT_ORANGE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("SX", (getWidth() - fm.stringWidth("SX")) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(50, 50));
        logoIcon.setMaximumSize(new Dimension(50, 50));
        logoIcon.setOpaque(false);
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandName = new JLabel("SeminarX");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 32));
        brandName.setForeground(ThemeManager.TEXT_PRIMARY);
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandTitle = new JLabel("Seminar Hall Event");
        brandTitle.setFont(ThemeManager.FONT_HEADER_XL);
        brandTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        brandTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandTitle2 = new JLabel("Booking System");
        brandTitle2.setFont(ThemeManager.FONT_HEADER_XL);
        brandTitle2.setForeground(ThemeManager.TEXT_PRIMARY);
        brandTitle2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagLine = new JLabel("<html>Discover. Book. Attend.<br>Be a part of Knowledge.</html>");
        tagLine.setFont(ThemeManager.FONT_BODY);
        tagLine.setForeground(ThemeManager.TEXT_MUTED);
        tagLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBrand.add(Box.createVerticalGlue());
        leftBrand.add(logoIcon);
        leftBrand.add(Box.createVerticalStrut(12));
        leftBrand.add(brandName);
        leftBrand.add(Box.createVerticalStrut(10));
        leftBrand.add(brandTitle);
        leftBrand.add(brandTitle2);
        leftBrand.add(Box.createVerticalStrut(16));
        leftBrand.add(tagLine);
        leftBrand.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        gbc.gridy = 0;
        contentBox.add(leftBrand, gbc);

        // RIGHT: Modern Glassmorphic Login Card (Screen 1)
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22, 25, 35, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(ThemeManager.BORDER_CARD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 490));
        card.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel welcomeLbl = new JLabel("Welcome Back");
        welcomeLbl.setFont(ThemeManager.FONT_HEADER_XL);
        welcomeLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        welcomeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loginToCont = new JLabel("Login to continue");
        loginToCont.setFont(ThemeManager.FONT_SMALL);
        loginToCont.setForeground(ThemeManager.TEXT_MUTED);
        loginToCont.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = new StyledTextField("Email address");
        emailField.setMaximumSize(new Dimension(320, 42));

        passwordField = new StyledPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(320, 42));

        // Row: Remember me + Forgot password?
        JPanel optRow = new JPanel(new BorderLayout());
        optRow.setOpaque(false);
        optRow.setMaximumSize(new Dimension(320, 24));

        rememberMeCheckBox = new JCheckBox("Remember me", true);
        rememberMeCheckBox.setFont(ThemeManager.FONT_SMALL);
        rememberMeCheckBox.setForeground(ThemeManager.TEXT_SECONDARY);
        rememberMeCheckBox.setOpaque(false);
        rememberMeCheckBox.setFocusPainted(false);

        JLabel forgotPwd = new JLabel("Forgot password?");
        forgotPwd.setFont(ThemeManager.FONT_SMALL);
        forgotPwd.setForeground(ThemeManager.ACCENT_RED);
        forgotPwd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        optRow.add(rememberMeCheckBox, BorderLayout.WEST);
        optRow.add(forgotPwd, BorderLayout.EAST);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(ThemeManager.FONT_SMALL_BOLD);
        errorLabel.setForeground(ThemeManager.ACCENT_RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn = new StyledButton("Login →", StyledButton.ButtonStyle.PRIMARY);
        loginBtn.setMaximumSize(new Dimension(320, 42));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        // Quick demo buttons pill
        JPanel demoBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoBox.setOpaque(false);

        StyledButton fillStudent = new StyledButton("Student Demo", StyledButton.ButtonStyle.SECONDARY);
        fillStudent.setFont(ThemeManager.FONT_SMALL);
        fillStudent.addActionListener(e -> {
            emailField.setText("student@college.edu");
            passwordField.setText("student123");
            errorLabel.setText("");
        });

        StyledButton fillOrg = new StyledButton("Organizer Demo", StyledButton.ButtonStyle.SECONDARY);
        fillOrg.setFont(ThemeManager.FONT_SMALL);
        fillOrg.addActionListener(e -> {
            emailField.setText("organizer@college.edu");
            passwordField.setText("admin123");
            errorLabel.setText("");
        });

        demoBox.add(fillStudent);
        demoBox.add(fillOrg);

        // Register link
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regRow.setOpaque(false);
        JLabel dontHave = new JLabel("Don't have an account?");
        dontHave.setFont(ThemeManager.FONT_SMALL);
        dontHave.setForeground(ThemeManager.TEXT_MUTED);

        JLabel regLink = new JLabel("Register");
        regLink.setFont(ThemeManager.FONT_SMALL_BOLD);
        regLink.setForeground(ThemeManager.ACCENT_RED);
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                errorLabel.setText("");
                navController.navigateTo(NavigationController.SCREEN_REGISTER);
            }
        });

        regRow.add(dontHave);
        regRow.add(regLink);

        JLabel backToWelcome = new JLabel("← Back to Welcome");
        backToWelcome.setFont(ThemeManager.FONT_SMALL);
        backToWelcome.setForeground(ThemeManager.TEXT_MUTED);
        backToWelcome.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToWelcome.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navController.navigateTo(NavigationController.SCREEN_PRE_LOGIN);
            }
        });

        card.add(welcomeLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(loginToCont);
        card.add(Box.createVerticalStrut(20));
        card.add(emailField);
        card.add(Box.createVerticalStrut(10));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(optRow);
        card.add(Box.createVerticalStrut(6));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(demoBox);
        card.add(Box.createVerticalStrut(8));
        card.add(regRow);
        card.add(Box.createVerticalStrut(6));
        card.add(backToWelcome);

        gbc.gridx = 1;
        contentBox.add(card, gbc);

        mainSplit.add(contentBox);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (email.isEmpty()) {
            errorLabel.setText("Please enter your email address.");
            return;
        }
        if (pass.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        errorLabel.setText("");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.authenticate(email, pass);
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Login →");
                try {
                    User user = get();
                    SessionManager.getInstance().setCurrentUser(user);
                    errorLabel.setText("");

                    if (user.getRole() == UserRole.ORGANIZER) {
                        navController.navigateTo(NavigationController.SCREEN_ORGANIZER_DASHBOARD);
                    } else {
                        navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD);
                    }
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    errorLabel.setText("<html><center>" + msg + "</center></html>");
                }
            }
        };
        worker.execute();
    }
}
