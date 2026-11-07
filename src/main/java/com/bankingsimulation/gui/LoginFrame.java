package com.bankingsimulation.gui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

import com.bankingsimulation.database.LoginDAO;

public class LoginFrame extends JFrame {

    private final Color NAVY = new Color(15, 35, 70);
    private final Color BLUE = new Color(0, 120, 215);
    private final Color LIGHT = new Color(245, 248, 252);

    public LoginFrame() {

        setTitle("Mobile Banking Simulation");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // ================= LEFT PANEL =================

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(NAVY);
        leftPanel.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        JLabel heading = new JLabel(
            "BANKING MADE SIMPLE",
            SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(Color.WHITE);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        java.net.URL imageURL = getClass().getResource(
            "/bank_building_panel.png"
        );

        if (imageURL != null) {

            ImageIcon icon = new ImageIcon(imageURL);

            Image image = icon.getImage().getScaledInstance(
                500, 450, Image.SCALE_SMOOTH
            );

            imageLabel.setIcon(new ImageIcon(image));

        } else {

            imageLabel.setText("Bank Building Image Not Found");
            imageLabel.setForeground(Color.WHITE);
        }

        JLabel subtitle = new JLabel(
            "Secure. Simple. Convenient.",
            SwingConstants.CENTER
        );

        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));

        leftPanel.add(heading, BorderLayout.NORTH);
        leftPanel.add(imageLabel, BorderLayout.CENTER);
        leftPanel.add(subtitle, BorderLayout.SOUTH);

        // ================= RIGHT PANEL =================

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(LIGHT);
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(12, 35, 12, 35);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // WELCOME TITLE

        JLabel title = new JLabel("Welcome Back!");

        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(NAVY);

        gbc.gridx = 0;
        gbc.gridy = 0;

        rightPanel.add(title, gbc);

        // INSTRUCTION

        JLabel instruction = new JLabel(
            "Login to your account"
        );

        instruction.setForeground(Color.GRAY);

        gbc.gridy = 1;
        rightPanel.add(instruction, gbc);

        // EMAIL LABEL

        JLabel emailLabel = new JLabel("Email Address");

        gbc.gridy = 2;
        rightPanel.add(emailLabel, gbc);

        // EMAIL FIELD

        JTextField emailField = new JTextField(20);

        emailField.setFont(
            new Font("Arial", Font.PLAIN, 15)
        );

        emailField.setPreferredSize(
            new Dimension(280, 48)
        );

        emailField.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(200, 210, 225)
                ),
                BorderFactory.createEmptyBorder(
                    8, 10, 8, 10
                )
            )
        );

        gbc.gridy = 3;
        rightPanel.add(emailField, gbc);

        // PASSWORD LABEL

        JLabel passwordLabel = new JLabel("Password");

        gbc.gridy = 4;
        rightPanel.add(passwordLabel, gbc);

        // PASSWORD FIELD

        JPasswordField passwordField =
            new JPasswordField(20);

        passwordField.setFont(
            new Font("Arial", Font.PLAIN, 15)
        );

        passwordField.setPreferredSize(
            new Dimension(280, 48)
        );

        passwordField.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(200, 210, 225)
                ),
                BorderFactory.createEmptyBorder(
                    8, 10, 8, 10
                )
            )
        );

        gbc.gridy = 5;
        rightPanel.add(passwordField, gbc);

        // ================= SHOW PASSWORD =================

        JCheckBox showPassword =
            new JCheckBox("Show Password");

        showPassword.setBackground(LIGHT);
        showPassword.setForeground(Color.GRAY);

        char defaultEcho = passwordField.getEchoChar();

        showPassword.addActionListener(e -> {

            if (showPassword.isSelected()) {

                passwordField.setEchoChar((char) 0);

            } else {

                passwordField.setEchoChar(defaultEcho);
            }
        });

        gbc.gridy = 6;
        rightPanel.add(showPassword, gbc);

        // ================= LOGIN BUTTON =================

        JButton loginButton = new JButton("LOGIN");

        loginButton.setBackground(BLUE);
        loginButton.setForeground(Color.WHITE);

        loginButton.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        loginButton.setPreferredSize(
            new Dimension(280, 48)
        );

        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);

        loginButton.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        // LOGIN BUTTON HOVER EFFECT

        loginButton.addMouseListener(
            new java.awt.event.MouseAdapter() {

                @Override
                public void mouseEntered(
                    java.awt.event.MouseEvent e
                ) {

                    loginButton.setBackground(
                        new Color(0, 90, 170)
                    );
                }

                @Override
                public void mouseExited(
                    java.awt.event.MouseEvent e
                ) {

                    loginButton.setBackground(BLUE);
                }
            }
        );

        // ================= LOGIN AUTHENTICATION =================

        loginButton.addActionListener(e -> {

            String email = emailField.getText().trim();

            char[] passwordCharacters =
                passwordField.getPassword();

            try {

                if (email.isEmpty()) {

                    JOptionPane.showMessageDialog(
                        this,
                        "Please enter your email address."
                    );

                    emailField.requestFocus();
                    return;
                }

                if (!email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )) {

                    JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid email address."
                    );

                    emailField.requestFocus();
                    return;
                }

                if (passwordCharacters.length == 0) {

                    JOptionPane.showMessageDialog(
                        this,
                        "Please enter your password."
                    );

                    passwordField.requestFocus();
                    return;
                }

                String password =
                    new String(passwordCharacters);

                LoginDAO loginDAO = new LoginDAO();

                int customerId =
                    loginDAO.authenticateAndGetCustomerId(
                        email, password
                    );

                if (customerId != -1) {

                    JOptionPane.showMessageDialog(
                        this,
                        "Login successful! Welcome back."
                    );

                    DashboardFrame dashboard =
                        new DashboardFrame(customerId);

                    dashboard.setVisible(true);

                    dispose();

                } else {

                    JOptionPane.showMessageDialog(
                        this,
                        "Invalid email or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not sign in: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } finally {

                Arrays.fill(passwordCharacters, '\0');
            }
        });

        gbc.gridy = 7;
        rightPanel.add(loginButton, gbc);

        // ================= CREATE ACCOUNT BUTTON =================

        JButton registerButton =
            new JButton("CREATE ACCOUNT");

        registerButton.setForeground(BLUE);
        registerButton.setBackground(LIGHT);

        registerButton.setFont(
            new Font("Arial", Font.BOLD, 14)
        );

        registerButton.setPreferredSize(
            new Dimension(280, 45)
        );

        registerButton.setFocusPainted(false);

        registerButton.setBorder(
            BorderFactory.createLineBorder(BLUE, 1)
        );

        registerButton.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        // CREATE ACCOUNT HOVER EFFECT

        registerButton.addMouseListener(
            new java.awt.event.MouseAdapter() {

                @Override
                public void mouseEntered(
                    java.awt.event.MouseEvent e
                ) {

                    registerButton.setBackground(
                        new Color(225, 238, 250)
                    );
                }

                @Override
                public void mouseExited(
                    java.awt.event.MouseEvent e
                ) {

                    registerButton.setBackground(LIGHT);
                }
            }
        );

        registerButton.addActionListener(e -> {
            new RegistrationFrame().setVisible(true);
            dispose();
        });

        gbc.gridy = 8;
        rightPanel.add(registerButton, gbc);

        // ================= ADD PANELS =================

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
        getRootPane().setDefaultButton(loginButton);
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new LoginFrame().setVisible(true);

        });
    }
}
