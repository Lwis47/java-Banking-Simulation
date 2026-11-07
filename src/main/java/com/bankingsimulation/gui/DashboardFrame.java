
package com.bankingsimulation.gui;

import com.bankingsimulation.database.BankingDAO;
import com.bankingsimulation.database.NotificationDAO;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DashboardFrame extends JFrame {

    private final int customerId;
    private final BankingDAO bankingDAO = new BankingDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private final Color NAVY = new Color(15, 35, 70);
    private final Color BLUE = new Color(0, 120, 215);
    private final Color LIGHT = new Color(245, 248, 252);

    private JPanel contentPanel;
    private JLabel balanceLabel;
    private JLabel accountNumberLabel;
    private JLabel accountTypeLabel;

    private JTable transactionTable;
    private NotificationPanel notificationsPanel;

    public DashboardFrame(int customerId) {

        this.customerId = customerId;

        setTitle("Mobile Banking - Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 600));

        createDashboard();
        refreshAccount();
    }

    private void createDashboard() {

        JPanel mainPanel = new JPanel(new BorderLayout());

        // SIDEBAR
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(230, 700));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(
                BorderFactory.createEmptyBorder(25, 15, 20, 15));

        JLabel logo = new JLabel("MY BANK");
        logo.setFont(new Font("Arial", Font.BOLD, 25));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(40));

        addMenuButton(sidebar, "Overview", "overview");
        addMenuButton(sidebar, "Deposit Money", "deposit");
        addMenuButton(sidebar, "Withdraw Money", "withdraw");
        addMenuButton(sidebar, "Transfer Money", "transfer");
        addMenuButton(sidebar, "Transactions", "transactions");
        addMenuButton(sidebar, "Beneficiaries", "beneficiaries");
        addMenuButton(sidebar, "Notifications", "notifications");

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setMaximumSize(new Dimension(190, 45));
        logoutButton.setBackground(new Color(190, 55, 55));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);

        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        sidebar.add(logoutButton);

        // MAIN CONTENT
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(LIGHT);

        contentPanel.add(createOverviewPanel(), "overview");
        contentPanel.add(createDepositPanel(), "deposit");
        contentPanel.add(createWithdrawPanel(), "withdraw");
        contentPanel.add(createTransferPanel(), "transfer");
        contentPanel.add(createTransactionsPanel(), "transactions");

        contentPanel.add(
                new BeneficiariesPanel(customerId),
                "beneficiaries"
        );

        notificationsPanel = new NotificationPanel(customerId);
        contentPanel.add(notificationsPanel, "notifications");

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addMenuButton(
            JPanel sidebar, String text, String cardName) {

        JButton button = new JButton(text);

        button.setMaximumSize(new Dimension(200, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(NAVY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> showPanel(cardName));

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(10));
    }

    // SWITCH SCREENS
    private void showPanel(String name) {

        if (name.equals("notifications")) {
            contentPanel.remove(notificationsPanel);

            notificationsPanel = new NotificationPanel(customerId);

            contentPanel.add(notificationsPanel, "notifications");
            contentPanel.revalidate();
            contentPanel.repaint();
        }

        CardLayout layout =
                (CardLayout) contentPanel.getLayout();

        layout.show(contentPanel, name);

        if (name.equals("overview")) {
            refreshAccount();
        }

        if (name.equals("transactions")) {
            refreshTransactions();
        }
    }

    // SAVE NOTIFICATION WITHOUT INTERRUPTING A SUCCESSFUL TRANSACTION
    private void saveNotification(String title, String message) {

        try {
            notificationDAO.createNotification(
                    customerId, title, message);

        } catch (SQLException ex) {
            System.err.println(
                    "Could not save notification: " + ex.getMessage());
        }
    }

    // ACCOUNT OVERVIEW
    private JPanel createOverviewPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(LIGHT);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(500, 300));
        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createEmptyBorder(30, 35, 30, 35));

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("ACCOUNT OVERVIEW");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(NAVY);

        JLabel balanceTitle = new JLabel("Available Balance");
        balanceTitle.setFont(new Font("Arial", Font.PLAIN, 16));

        balanceLabel = new JLabel("KES 0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 35));
        balanceLabel.setForeground(BLUE);

        accountNumberLabel =
                new JLabel("Account Number: Loading...");

        accountTypeLabel =
                new JLabel("Account Type: Loading...");

        card.add(heading);
        card.add(Box.createVerticalStrut(35));
        card.add(balanceTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(30));
        card.add(accountNumberLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(accountTypeLabel);

        panel.add(card);

        return panel;
    }

    // DEPOSIT
    private JPanel createDepositPanel() {

        return createOperationPanel(
                "DEPOSIT MONEY",
                "Enter amount to deposit:",
                amount -> {

                    try {
                        bankingDAO.deposit(customerId, amount);

                        saveNotification(
                                "Deposit Successful",
                                "Your account was credited with KES "
                                        + amount.toPlainString() + "."
                        );

                        refreshAccount();
                        refreshTransactions();

                        JOptionPane.showMessageDialog(
                                this, "Deposit successful!");

                    } catch (SQLException ex) {
                        throw new RuntimeException(
                                "Deposit failed: " + ex.getMessage(), ex);
                    }
                }
        );
    }

    // WITHDRAW
    private JPanel createWithdrawPanel() {

        return createOperationPanel(
                "WITHDRAW MONEY",
                "Enter amount to withdraw:",
                amount -> {

                    try {
                        bankingDAO.withdraw(customerId, amount);

                        saveNotification(
                                "Withdrawal Successful",
                                "KES " + amount.toPlainString()
                                        + " was withdrawn from your account."
                        );

                        refreshAccount();
                        refreshTransactions();

                        JOptionPane.showMessageDialog(
                                this, "Withdrawal successful!");

                    } catch (SQLException ex) {
                        throw new RuntimeException(
                                "Withdrawal failed: "
                                        + ex.getMessage(), ex);
                    }
                }
        );
    }

    // TRANSFER
    private JPanel createTransferPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(LIGHT);

        JPanel form = new JPanel();
        form.setPreferredSize(new Dimension(420, 330));
        form.setBackground(Color.WHITE);
        form.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30));

        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("TRANSFER MONEY");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(NAVY);

        JTextField accountField = new JTextField();
        accountField.setMaximumSize(new Dimension(350, 40));

        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(350, 40));

        JButton transferButton = new JButton("TRANSFER");
        transferButton.setBackground(BLUE);
        transferButton.setForeground(Color.WHITE);
        transferButton.setMaximumSize(new Dimension(350, 45));

        transferButton.addActionListener(e -> {

            try {

                String targetAccount =
                        accountField.getText().trim();

                BigDecimal amount =
                        new BigDecimal(amountField.getText().trim());

                if (targetAccount.isEmpty()
                        || amount.compareTo(BigDecimal.ZERO) <= 0
                        || amount.scale() > 2) {

                    throw new IllegalArgumentException(
                            "Enter a valid account number and amount.");
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Transfer KES " + amount.toPlainString()
                        + " to account " + targetAccount + "?",
                        "Confirm Transfer",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean successful = bankingDAO.transfer(
                        customerId, targetAccount, amount);

                if (successful) {

                    saveNotification(
                            "Transfer Successful",
                            "You transferred KES "
                                    + amount.toPlainString()
                                    + " to account "
                                    + targetAccount + "."
                    );

                    JOptionPane.showMessageDialog(
                            this, "Transfer successful!");

                    accountField.setText("");
                    amountField.setText("");

                    refreshAccount();
                    refreshTransactions();

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Transfer failed.",
                            "Transfer Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this, "Please enter a valid amount.");

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        this, ex.getMessage());

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Transfer failed: " + ex.getMessage(),
                        "Transfer Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        form.add(heading);
        form.add(Box.createVerticalStrut(25));
        form.add(new JLabel("Recipient Account Number"));
        form.add(accountField);
        form.add(Box.createVerticalStrut(15));
        form.add(new JLabel("Amount (KES)"));
        form.add(amountField);
        form.add(Box.createVerticalStrut(25));
        form.add(transferButton);

        panel.add(form);

        return panel;
    }

    // DEPOSIT AND WITHDRAW FORM
    private JPanel createOperationPanel(
            String title,
            String instruction,
            java.util.function.Consumer<BigDecimal> operation) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(LIGHT);

        JPanel form = new JPanel();
        form.setPreferredSize(new Dimension(420, 280));
        form.setBackground(Color.WHITE);
        form.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30));

        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(NAVY);

        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(350, 40));

        JButton submitButton = new JButton("SUBMIT");
        submitButton.setBackground(BLUE);
        submitButton.setForeground(Color.WHITE);
        submitButton.setMaximumSize(new Dimension(350, 45));

        submitButton.addActionListener(e -> {

            try {

                BigDecimal amount =
                        new BigDecimal(amountField.getText().trim());

                if (amount.compareTo(BigDecimal.ZERO) <= 0
                        || amount.scale() > 2) {

                    throw new IllegalArgumentException(
                            "Enter a positive amount with at most 2 decimal places.");
                }

                operation.accept(amount);
                amountField.setText("");

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this, "Please enter a valid amount.");

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Operation Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        form.add(heading);
        form.add(Box.createVerticalStrut(25));
        form.add(new JLabel(instruction));
        form.add(Box.createVerticalStrut(10));
        form.add(amountField);
        form.add(Box.createVerticalStrut(25));
        form.add(submitButton);

        panel.add(form);

        return panel;
    }

    // TRANSACTION HISTORY
    private JPanel createTransactionsPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT);
        panel.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("TRANSACTION HISTORY");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(NAVY);

        String[] columns = {
            "ID", "Type", "Amount (KES)", "Reference", "Date"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(model);
        transactionTable.setRowHeight(28);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(transactionTable),
                BorderLayout.CENTER);

        return panel;
    }

    // REFRESH ACCOUNT DETAILS
    private void refreshAccount() {

        try {

            BankingDAO.AccountInfo account =
                    bankingDAO.getAccount(customerId);

            balanceLabel.setText(
                    "KES " + account.balance.toPlainString());

            accountNumberLabel.setText(
                    "Account Number: " + account.accountNumber);

            accountTypeLabel.setText(
                    "Account Type: " + account.accountType);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load account information: "
                    + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // REFRESH TRANSACTION HISTORY
    private void refreshTransactions() {

        DefaultTableModel model =
                (DefaultTableModel) transactionTable.getModel();

        model.setRowCount(0);

        try {

            List<BankingDAO.TransactionInfo> transactions =
                    bankingDAO.getTransactions(customerId);

            for (BankingDAO.TransactionInfo transaction : transactions) {

                model.addRow(new Object[]{
                    transaction.transactionId,
                    transaction.type,
                    transaction.amount,
                    transaction.reference,
                    transaction.date
                });
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load transactions: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
