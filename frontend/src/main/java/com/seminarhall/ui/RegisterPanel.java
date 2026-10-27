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

public class RegisterPanel extends JPanel {
    private final NavigationController navController;
    private final UserService userService;

    private StyledTextField nameField;
    private StyledTextField emailField;
    private StyledTextField phoneField;
    private StyledPasswordField passwordField;
    private StyledPasswordField confirmPasswordField;
    private JCheckBox termsCheckBox;
    private JLabel errorLabel;
    private StyledButton registerBtn;

    public RegisterPanel(NavigationController navController) {
        this.navController = navController;
        this.userService = ServiceFactory.getUserService();

        setName(NavigationController.SCREEN_REGISTER);
        setOpaque(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel mainSplit = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(45, 12, 10), w / 2, h, new Color(11, 12, 16));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        mainSplit.setOpaque(false);

        JPanel contentBox = new JPanel(new GridBagLayout());
        contentBox.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 30, 20, 30);
        gbc.fill = GridBagConstraints.BOTH;

        // LEFT: Join SeminarX banner with bullet points (Screen 2)
        JPanel leftBrand = new JPanel();
        leftBrand.setOpaque(false);
        leftBrand.setLayout(new BoxLayout(leftBrand, BoxLayout.Y_AXIS));
        leftBrand.setPreferredSize(new Dimension(380, 500));

        JLabel joinLbl = new JLabel("Join");
        joinLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        joinLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel brandLbl = new JLabel("SeminarX");
        brandLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        brandLbl.setForeground(ThemeManager.ACCENT_RED);

        JLabel descLbl = new JLabel("<html>Create your account and be part of amazing events at our college.</html>");
        descLbl.setFont(ThemeManager.FONT_BODY);
        descLbl.setForeground(ThemeManager.TEXT_MUTED);

        leftBrand.add(Box.createVerticalGlue());
        leftBrand.add(joinLbl);
        leftBrand.add(brandLbl);
        leftBrand.add(Box.createVerticalStrut(10));
        leftBrand.add(descLbl);
        leftBrand.add(Box.createVerticalStrut(24));

        String[] perks = {"Seminars", "Workshops", "Hackathons", "Cultural Events"};
        for (String perk : perks) {
            JPanel bullet = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bullet.setOpaque(false);
            JLabel bIcon = new JLabel("●");
            bIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
            bIcon.setForeground(ThemeManager.ACCENT_RED);
            JLabel bText = new JLabel(perk);
            bText.setFont(ThemeManager.FONT_BODY);
            bText.setForeground(ThemeManager.TEXT_SECONDARY);
            bullet.add(bIcon);
            bullet.add(bText);
            leftBrand.add(bullet);
            leftBrand.add(Box.createVerticalStrut(6));
        }
        leftBrand.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        gbc.gridy = 0;
        contentBox.add(leftBrand, gbc);

        // RIGHT: Registration card
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
        card.setPreferredSize(new Dimension(380, 520));
        card.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JLabel title = new JLabel("Create Account");
        title.setFont(ThemeManager.FONT_HEADER_XL);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameField = new StyledTextField("Full Name");
        nameField.setMaximumSize(new Dimension(320, 38));

        emailField = new StyledTextField("Email address");
        emailField.setMaximumSize(new Dimension(320, 38));

        phoneField = new StyledTextField("Phone number");
        phoneField.setMaximumSize(new Dimension(320, 38));

        passwordField = new StyledPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(320, 38));

        confirmPasswordField = new StyledPasswordField("Confirm password");
        confirmPasswordField.setMaximumSize(new Dimension(320, 38));

        termsCheckBox = new JCheckBox("I agree to the Terms & Conditions", true);
        termsCheckBox.setFont(ThemeManager.FONT_SMALL);
        termsCheckBox.setForeground(ThemeManager.TEXT_MUTED);
        termsCheckBox.setOpaque(false);
        termsCheckBox.setFocusPainted(false);
        termsCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(ThemeManager.FONT_SMALL_BOLD);
        errorLabel.setForeground(ThemeManager.ACCENT_RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerBtn = new StyledButton("Create Account →", StyledButton.ButtonStyle.PRIMARY);
        registerBtn.setMaximumSize(new Dimension(320, 42));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> performRegistration());

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setOpaque(false);
        JLabel alrHave = new JLabel("Already have an account?");
        alrHave.setFont(ThemeManager.FONT_SMALL);
        alrHave.setForeground(ThemeManager.TEXT_MUTED);

        JLabel loginLink = new JLabel("Login");
        loginLink.setFont(ThemeManager.FONT_SMALL_BOLD);
        loginLink.setForeground(ThemeManager.ACCENT_RED);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                errorLabel.setText("");
                navController.navigateTo(NavigationController.SCREEN_LOGIN);
            }
        });

        backRow.add(alrHave);
        backRow.add(loginLink);

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

        card.add(title);
        card.add(Box.createVerticalStrut(18));
        card.add(nameField);
        card.add(Box.createVerticalStrut(8));
        card.add(emailField);
        card.add(Box.createVerticalStrut(8));
        card.add(phoneField);
        card.add(Box.createVerticalStrut(8));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(confirmPasswordField);
        card.add(Box.createVerticalStrut(6));
        card.add(termsCheckBox);
        card.add(Box.createVerticalStrut(4));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(backRow);
        card.add(Box.createVerticalStrut(6));
        card.add(backToWelcome);

        gbc.gridx = 1;
        contentBox.add(card, gbc);

        mainSplit.add(contentBox);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();

        if (name.length() < 3) {
            errorLabel.setText("Please enter your full name.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }
        if (pass.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(confirmPass)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating Account...");
        errorLabel.setText("");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.register(name, email, phone, pass, UserRole.STUDENT);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                registerBtn.setText("Create Account →");
                try {
                    User newUser = get();
                    SessionManager.getInstance().setCurrentUser(newUser);
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Account created successfully! Welcome to SeminarX, " + newUser.getFullName());
                    navController.navigateTo(NavigationController.SCREEN_STUDENT_DASHBOARD);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    errorLabel.setText("<html><center>" + msg + "</center></html>");
                }
            }
        };
        worker.execute();
    }
}
