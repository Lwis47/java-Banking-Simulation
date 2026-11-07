package com.bankingsimulation.gui;

import com.bankingsimulation.database.RegistrationDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Registration screen for a new banking-simulation customer. */
public class RegistrationFrame extends JFrame {

    private static final Color NAVY = new Color(15, 35, 70);
    private static final Color BLUE = new Color(0, 120, 215);
    private static final Color LIGHT = new Color(245, 248, 252);

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final JTextField firstNameField = new JTextField(22);
    private final JTextField lastNameField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JPasswordField confirmPasswordField = new JPasswordField(22);

    public RegistrationFrame() {
        setTitle("Mobile Banking Simulation - Create account");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 650);
        setMinimumSize(new Dimension(520, 590));
        setLocationRelativeTo(null);
        add(createContent());
    }

    private JPanel createContent() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(LIGHT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setPreferredSize(new Dimension(450, 540));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(28, 35, 28, 35)));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 0, 5, 0);

        JLabel title = new JLabel("Create your account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(NAVY);
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 4, 0);
        form.add(title, constraints);

        JLabel subtitle = new JLabel("Your savings account will be ready immediately.",
                SwingConstants.CENTER);
        subtitle.setForeground(Color.GRAY);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 18, 0);
        form.add(subtitle, constraints);

        addField(form, constraints, "First name", firstNameField);
        addField(form, constraints, "Last name", lastNameField);
        addField(form, constraints, "Email address", emailField);
        addField(form, constraints, "Password (at least 8 characters)", passwordField);
        addField(form, constraints, "Confirm password", confirmPasswordField);

        JButton createButton = new JButton("CREATE ACCOUNT");
        createButton.setBackground(BLUE);
        createButton.setForeground(Color.WHITE);
        createButton.setFont(new Font("Arial", Font.BOLD, 15));
        createButton.setFocusPainted(false);
        createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        constraints.gridy++;
        constraints.insets = new Insets(18, 0, 5, 0);
        form.add(createButton, constraints);

        JButton backButton = new JButton("Back to login");
        backButton.setForeground(BLUE);
        backButton.setBackground(Color.WHITE);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 0, 0);
        form.add(backButton, constraints);

        createButton.addActionListener(e -> register());
        backButton.addActionListener(e -> returnToLogin());
        outer.add(form);
        return outer;
    }

    private void addField(JPanel form, GridBagConstraints constraints,
                          String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        constraints.gridy++;
        constraints.insets = new Insets(5, 0, 1, 0);
        form.add(label, constraints);

        field.setPreferredSize(new Dimension(320, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 5, 0);
        form.add(field, constraints);
    }

    private void register() {
        char[] password = passwordField.getPassword();
        char[] confirmation = confirmPasswordField.getPassword();
        try {
            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException("The passwords do not match.");
            }

            int customerId = registrationDAO.register(firstNameField.getText(),
                    lastNameField.getText(), emailField.getText(), password);
            JOptionPane.showMessageDialog(this,
                    "Your account has been created successfully.",
                    "Welcome", JOptionPane.INFORMATION_MESSAGE);
            new DashboardFrame(customerId).setVisible(true);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your details",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            String message = ex.getSQLState() != null && ex.getSQLState().startsWith("23")
                    ? "An account already exists for this email address."
                    : "Could not create the account: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Registration failed",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
            passwordField.setText("");
            confirmPasswordField.setText("");
        }
    }

    private void returnToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
