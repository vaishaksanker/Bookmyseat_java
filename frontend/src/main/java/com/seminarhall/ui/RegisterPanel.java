package com.seminarhall.ui;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;
import com.seminarhall.service.ServiceFactory;
import com.seminarhall.service.UserService;
import com.seminarhall.ui.LoginPanel.AuthPage;
import com.seminarhall.ui.components.StyledButton;
import com.seminarhall.ui.components.StyledPasswordField;
import com.seminarhall.ui.components.StyledTextField;

import javax.swing.*;
import java.awt.*;

/**
 * The web app's RegisterPage: "Register Student ID" card over the red theatre photo with
 * Full Name, College Email and Password. Creates a STUDENT account through UserService.
 */
public class RegisterPanel extends JPanel implements NavigationController.RefreshableScreen {
    private static final String CREATE_TEXT = "Create Account →";

    private final NavigationController navController;
    private final UserService userService;

    private StyledTextField nameField;
    private StyledTextField emailField;
    private StyledPasswordField passwordField;
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
        AuthPage.Card card = new AuthPage.Card();
        card.addRow(AuthPage.heading("Register Student ID"), 0);
        card.addRow(AuthPage.subtitle("Access campus seminar halls & workshops"), 4);

        // inputs py-2.5, form space-y-3.5
        nameField = AuthPage.field(new StyledTextField("Full Name"), 38);
        emailField = AuthPage.field(new StyledTextField("College Email"), 38);
        passwordField = AuthPage.field(new StyledPasswordField("Password"), 38);
        nameField.addActionListener(e -> emailField.requestFocusInWindow());
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> performRegistration());

        errorLabel = AuthPage.errorLabel();

        registerBtn = AuthPage.submitButton(CREATE_TEXT, 14, 44); // py-3 text-sm
        registerBtn.addActionListener(e -> performRegistration());

        card.addRow(nameField, 24);       // mb-6
        card.addRow(emailField, 14);
        card.addRow(passwordField, 14);
        card.addRow(errorLabel, 12);
        card.addButton(registerBtn, 14 + 8); // space-y-3.5 + mt-2
        card.addRow(AuthPage.switchLine("Already have an account?", "Sign In", () -> {
            AuthPage.showError(errorLabel, null);
            navController.navigateTo(NavigationController.SCREEN_LOGIN);
        }), 20 - AuthPage.GLOW_BOTTOM); // mt-5

        add(AuthPage.backdrop(card, null), BorderLayout.CENTER);
    }

    @Override
    public void onScreenDisplayed() {
        AuthPage.showError(errorLabel, null);
        registerBtn.setEnabled(true);
        registerBtn.setText(CREATE_TEXT);
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (name.length() < 3) {
            AuthPage.showError(errorLabel, "Please enter your full name (at least 3 characters).");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AuthPage.showError(errorLabel, "Please enter a valid college email address.");
            return;
        }
        if (pass.length() < 6) {
            AuthPage.showError(errorLabel, "Password must be at least 6 characters.");
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating Account...");
        AuthPage.showError(errorLabel, null);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.register(name, email, "", pass, UserRole.STUDENT);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                registerBtn.setText(CREATE_TEXT);
                try {
                    User newUser = get();
                    nameField.setText("");
                    emailField.setText("");
                    passwordField.setText("");
                    navController.onSignedIn(newUser);
                } catch (Exception ex) {
                    AuthPage.showError(errorLabel,
                            AuthPage.errorMessage(ex, "Could not create the account. Please try again."));
                }
            }
        };
        worker.execute();
    }
}
