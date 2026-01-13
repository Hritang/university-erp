package com.university.erp.ui;

import com.university.erp.ui.controllers.AuthController;
import com.university.erp.ui.dashboard.AdminDashboardFrame;
import com.university.erp.ui.dashboard.InstructorDashboardFrame;
import com.university.erp.ui.dashboard.StudentDashboardFrame;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            System.err.println("FlatLaf not found. UI may look different.");
        }
    }

    // UI Components
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton togglePwdButton = new JButton("\uD83D\uDC41");
    private final JButton loginButton = new JButton("Login");
    private final JLabel changePwdLabel = new JLabel("Change Password?");
    private final JLabel statusLabel = new JLabel(" ");

    // Security Logic
    private int failedAttempts = 0;
    private final int MAX_ATTEMPTS = 5;

    // Colors
    private final Color COL_GRADIENT_TOP = new Color(64, 64, 112);
    private final Color COL_GRADIENT_BOTTOM = new Color(19, 19, 32);
    private final Color COL_CARD_BG = new Color(0x7882EB);
    private final Color COL_PRIMARY = Color.lightGray;
    private final Color COL_PRIMARY_HOVER = new Color(240, 240, 240);
    private final Color COL_TEXT_Heading = new Color(35, 35, 60);
    private final Color COL_TEXT = Color.WHITE;
    private final Color COL_BORDER = new Color(80, 80, 110);
    private final Color COL_INPUT_BG = Color.white;

    private final AuthController controller;

    public LoginFrame() {
        this.controller = new AuthController();
        installLookAndFeel();
        initUI();
    }

    public LoginFrame(AuthController controller) {
        this.controller = controller;
        installLookAndFeel();
        initUI();
    }

    private void initUI() {
        setTitle("University ERP System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // 1. Gradient Background
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, COL_GRADIENT_TOP, 0, getHeight(), COL_GRADIENT_BOTTOM);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(mainPanel);

        // Card Setup
        JPanel card = new RoundedPanel(30, COL_CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(40, 40, 30, 40));
        card.setPreferredSize(new Dimension(400, 530));
        card.setOpaque(false);

        // CENTER PANEL: Title, Inputs, Login Button
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Header
        JLabel title = new JLabel("University ERP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COL_TEXT_Heading);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your credentials");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.lightGray);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(subtitle);
        centerPanel.add(Box.createVerticalStrut(30));

        // Inputs Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COL_CARD_BG);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.insets = new Insets(0, 0, 15, 0);
        gb.weightx = 1.0;
        gb.gridx = 0;

        // Username
        formPanel.add(makeLabel("Username"), gb);
        formPanel.add(createInputContainer(usernameField), gb);

        // Password
        formPanel.add(makeLabel("Password"), gb);

        JPanel passContainer = new RoundedPanel(15, COL_INPUT_BG);
        passContainer.setLayout(new BorderLayout());
        passContainer.setPreferredSize(new Dimension(300, 45));
        passContainer.setOpaque(false);
        passContainer.setBorder(new RoundedBorder(COL_BORDER, 15));

        passwordField.setBorder(new EmptyBorder(0, 15, 0, 0));
        passwordField.setBackground(COL_INPUT_BG);
        passwordField.setOpaque(false);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        togglePwdButton.setFocusPainted(false);
        togglePwdButton.setBorderPainted(false);
        togglePwdButton.setContentAreaFilled(false);
        togglePwdButton.setMargin(new Insets(0, 0, 0, 0));
        togglePwdButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        togglePwdButton.setPreferredSize(new Dimension(40, 45));
        togglePwdButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePwdButton.addActionListener(e -> togglePassword());

        passContainer.add(passwordField, BorderLayout.CENTER);
        passContainer.add(togglePwdButton, BorderLayout.EAST);

        FocusAdapter passFocus = new FocusAdapter() {
            public void focusGained(FocusEvent e) { passContainer.setBorder(new RoundedBorder(COL_PRIMARY, 15)); passContainer.repaint(); }
            public void focusLost(FocusEvent e) { passContainer.setBorder(new RoundedBorder(COL_BORDER, 15)); passContainer.repaint(); }
        };
        passwordField.addFocusListener(passFocus);
        togglePwdButton.addFocusListener(passFocus);

        formPanel.add(passContainer, gb);
        centerPanel.add(formPanel);

        // Status Label
        statusLabel.setForeground(new Color(0xB91C1C));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setPreferredSize(new Dimension(300, 25));
        statusLabel.setMaximumSize(new Dimension(300, 25));
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createVerticalStrut(10));

        // Login Button
        loginButton.setPreferredSize(new Dimension(320, 45));
        loginButton.setMaximumSize(new Dimension(320, 45));
        loginButton.setForeground(new Color(35, 35, 60));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);

        loginButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!loginButton.isEnabled()) g2.setColor(Color.GRAY);
                else if (loginButton.getModel().isPressed()) g2.setColor(COL_PRIMARY.darker());
                else if (loginButton.getModel().isRollover()) g2.setColor(COL_PRIMARY_HOVER);
                else g2.setColor(COL_PRIMARY);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 25, 25);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        loginButton.addActionListener(this::onLogin);
        centerPanel.add(loginButton);

        // Add Center Panel to Card
        card.add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        changePwdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changePwdLabel.setForeground(Color.lightGray);
        changePwdLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        changePwdLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openChangePasswordDialog();
            }
            @Override
            public void mouseEntered(MouseEvent e) { changePwdLabel.setText("<html><u>Change Password?</u></html>"); }
            @Override
            public void mouseExited(MouseEvent e) { changePwdLabel.setText("Change Password?"); }
        });

        southPanel.add(changePwdLabel);

        // Add South Panel to Card
        card.add(southPanel, BorderLayout.SOUTH);

        mainPanel.add(card);

        fixLoginColors();

    }

    // --- Logic & Helpers ---

    private void onLogin(ActionEvent e) {
        if (failedAttempts >= MAX_ATTEMPTS) {
            statusLabel.setText("Account Locked.");
            return;
        }
        setBusy(true);
        statusLabel.setText("Verifying...");
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return controller.login(username, password);
            }
            @Override
            protected void done() {
                setBusy(false);
                try {
                    handleLoginResponse(get(), username);
                } catch (Exception ex) {
                    statusLabel.setText("System Error.");
                }
            }
        };
        worker.execute();
    }

    private void handleLoginResponse(String result, String username) {
        if (result == null) return;
        if (result.equals("FAIL_INPUT")) {
            statusLabel.setText("Please enter username and password.");
        } else if (result.equals("FAIL_BAD_CRED")) {
            handleFailure();
        } else if (result.startsWith("SUCCESS:")) {
            failedAttempts = 0;
            String role = result.substring(8);
            openDashboard(role, username);
        } else {
            statusLabel.setText("Login Failed (" + result + ")");
        }
    }

    private void handleFailure() {
        failedAttempts++;
        int remaining = MAX_ATTEMPTS - failedAttempts;
        if (remaining <= 0) {
            statusLabel.setText("Account Locked.");
            loginButton.setEnabled(false);
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
        } else {
            statusLabel.setText("Invalid Credentials. (" + remaining + " tries left)");
            passwordField.setText("");
        }
    }

    private void openDashboard(String role, String username) {
        JFrame frame = switch (role.toLowerCase()) {
            case "admin" -> new AdminDashboardFrame(username);
            case "instructor" -> new InstructorDashboardFrame(username);
            default -> new StudentDashboardFrame(username);
        };
        frame.setVisible(true);
        dispose();
    }

    private void setBusy(boolean busy) {
        if (failedAttempts < MAX_ATTEMPTS) {
            loginButton.setEnabled(!busy);
            usernameField.setEnabled(!busy);
            passwordField.setEnabled(!busy);
        }
    }

    private JPanel createInputContainer(JTextField field) {
        JPanel container = new RoundedPanel(15, COL_INPUT_BG);
        container.setLayout(new BorderLayout());
        container.setPreferredSize(new Dimension(300, 45));
        container.setOpaque(false);
        container.setBorder(new RoundedBorder(COL_BORDER, 15));
        field.setBackground(COL_INPUT_BG);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 15, 0, 15));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        container.add(field, BorderLayout.CENTER);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { container.setBorder(new RoundedBorder(COL_PRIMARY, 15)); container.repaint(); }
            public void focusLost(FocusEvent e) { container.setBorder(new RoundedBorder(COL_BORDER, 15)); container.repaint(); }
        });
        return container;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(COL_TEXT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    private void togglePassword() {
        if (passwordField.getEchoChar() == 0) {
            passwordField.setEchoChar('•');
            togglePwdButton.setText("\uD83D\uDC41");
        } else {
            passwordField.setEchoChar((char) 0);
            togglePwdButton.setText("\uD83D\uDE48");
        }
    }

    // --- REFACTORED: NO SQL HERE ---
    private void openChangePasswordDialog() {
        String preUsername = usernameField.getText().trim();

        JPasswordField currentPwd = new JPasswordField();
        JPasswordField newPwd = new JPasswordField();
        JPasswordField confirmPwd = new JPasswordField();
        JTextField userField = new JTextField(preUsername);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Username:"), c);
        c.gridx = 1; panel.add(userField, c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Current password:"), c);
        c.gridx = 1; panel.add(currentPwd, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("New password:"), c);
        c.gridx = 1; panel.add(newPwd, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Confirm new password:"), c);
        c.gridx = 1; panel.add(confirmPwd, c);

        int option = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;

        String username = userField.getText().trim();
        String current = new String(currentPwd.getPassword());
        String n1 = new String(newPwd.getPassword());
        String n2 = new String(confirmPwd.getPassword());

        // Basic Validation
        if (username.isEmpty() || current.isEmpty() || n1.isEmpty() || n2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!n1.equals(n2)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (n1.length() < 6) {
            JOptionPane.showMessageDialog(this, "Use a stronger password (at least 6 characters).", "Weak password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verify Old Credentials
        String loginResult = controller.login(username, current);
        if (loginResult == null || !loginResult.startsWith("SUCCESS:")) {
            JOptionPane.showMessageDialog(this, "Current username/password incorrect.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // DELEGATE TO CONTROLLER (Architecture Fix)
        // **IMPORTANT:** Ensure your AuthController has a 'changePassword' method
        boolean success = controller.changePassword(username, n1);

        if (success) {
            JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Password changed.");
        } else {
            JOptionPane.showMessageDialog(this, "Database error or User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Change failed.");
        }
    }

    class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        public RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.backgroundColor = bgColor; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    class RoundedBorder extends AbstractBorder {
        private final Color color; private final int radius;
        RoundedBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }

    private void fixLoginColors() {
        usernameField.setForeground(Color.BLACK);
        passwordField.setForeground(Color.BLACK);
        togglePwdButton.setForeground(Color.BLACK);

        changePwdLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setForeground(new Color(0xB91C1C));

        loginButton.setForeground(new Color(35, 35, 60));
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
