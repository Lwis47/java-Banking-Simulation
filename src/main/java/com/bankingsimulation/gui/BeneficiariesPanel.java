/**
 *
 * @author lwisw
 */

package com.bankingsimulation.gui;

import com.bankingsimulation.database.BeneficiaryDAO;
import com.bankingsimulation.database.BeneficiaryDAO.Beneficiary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BeneficiariesPanel extends JPanel {

    private final int customerId;
    private final BeneficiaryDAO dao = new BeneficiaryDAO();

    private JTextField accountField;
    private JTextField nicknameField;
    private JTable beneficiaryTable;
    private DefaultTableModel tableModel;

    private List<Beneficiary> beneficiaries;

    public BeneficiariesPanel(int customerId) {
        this.customerId = customerId;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createTitle(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        refreshBeneficiaries();
    }

    private JPanel createTitle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("Beneficiary Management");
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
            "Save and manage your transfer recipients."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);

        panel.add(createForm(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        accountField = new JTextField(18);
        nicknameField = new JTextField(18);

        JButton addButton = new JButton("Save Beneficiary");
        addButton.addActionListener(e -> addBeneficiary());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Recipient Account Number:"), gbc);

        gbc.gridx = 1;
        panel.add(accountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        panel.add(nicknameField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(addButton, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel heading = new JLabel("Saved Beneficiaries");
        heading.setFont(new Font("Arial", Font.BOLD, 18));

        tableModel = new DefaultTableModel(
            new String[]{"Nickname", "Account Number"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        beneficiaryTable = new JTable(tableModel);
        beneficiaryTable.setRowHeight(30);
        beneficiaryTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(beneficiaryTable);

        JButton deleteButton = new JButton("Remove Selected");
        JButton refreshButton = new JButton("Refresh");

        deleteButton.addActionListener(e -> deleteBeneficiary());
        refreshButton.addActionListener(e -> refreshBeneficiaries());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(refreshButton);
        buttons.add(deleteButton);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void addBeneficiary() {
        String accountNumber = accountField.getText().trim();
        String nickname = nicknameField.getText().trim();

        if (accountNumber.isEmpty() || nickname.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter the account number and nickname."
            );
            return;
        }

        try {
            boolean added = dao.addBeneficiary(
                customerId, accountNumber, nickname
            );

            if (added) {
                JOptionPane.showMessageDialog(
                    this, "Beneficiary saved successfully!"
                );

                accountField.setText("");
                nicknameField.setText("");

                refreshBeneficiaries();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Account not found or account is not active."
                );
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Invalid beneficiary",
                JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not save beneficiary. Check whether it already exists.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void refreshBeneficiaries() {
        try {
            beneficiaries = dao.getBeneficiaries(customerId);

            tableModel.setRowCount(0);

            for (Beneficiary beneficiary : beneficiaries) {
                tableModel.addRow(new Object[]{
                    beneficiary.nickname,
                    beneficiary.accountNumber
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not load beneficiaries.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void deleteBeneficiary() {
        int selectedRow = beneficiaryTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a beneficiary first."
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove this beneficiary?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int beneficiaryId =
            beneficiaries.get(selectedRow).beneficiaryId;

        try {
            boolean deleted = dao.deleteBeneficiary(
                customerId, beneficiaryId
            );

            if (deleted) {
                JOptionPane.showMessageDialog(
                    this, "Beneficiary removed successfully!"
                );
                refreshBeneficiaries();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not remove beneficiary.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }
}

