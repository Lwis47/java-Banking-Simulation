package com.bankingsimulation.database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankingDAO {

    public static class AccountInfo {
        public int accountId;
        public String accountNumber;
        public String accountType;
        public BigDecimal balance;
        public String status;

        public AccountInfo(int id, String number, String type,
                           BigDecimal balance, String status) {
            this.accountId = id;
            this.accountNumber = number;
            this.accountType = type;
            this.balance = balance;
            this.status = status;
        }
    }

    public static class TransactionInfo {
        public int transactionId;
        public String type;
        public BigDecimal amount;
        public String reference;
        public String description;
        public Timestamp date;

        public TransactionInfo(int id, String type,
                               BigDecimal amount, String reference,
                               String description, Timestamp date) {
            this.transactionId = id;
            this.type = type;
            this.amount = amount;
            this.reference = reference;
            this.description = description;
            this.date = date;
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than 2 decimal places.");
        }
    }

    public AccountInfo getAccount(int customerId) throws SQLException {

        String sql = "SELECT account_id, account_number, account_type, "
                   + "balance, status FROM accounts "
                   + "WHERE customer_id = ? AND status = 'ACTIVE' "
                   + "ORDER BY account_id LIMIT 1";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AccountInfo(
                                rs.getInt("account_id"),
                                rs.getString("account_number"),
                                rs.getString("account_type"),
                                rs.getBigDecimal("balance"),
                                rs.getString("status")
                        );
                    }
                }
            }
        }

        throw new SQLException("No active account was found.");
    }

    public boolean deposit(int customerId, BigDecimal amount)
            throws SQLException {

        validateAmount(amount);

        String update = "UPDATE accounts SET balance = balance + ? "
                      + "WHERE account_id = ? AND status = 'ACTIVE'";

        String insert = "INSERT INTO transactions "
                      + "(account_id, transaction_type, amount, "
                      + "reference_number, description) "
                      + "VALUES (?, 'DEPOSIT', ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            con.setAutoCommit(false);

            try {
                AccountInfo account =
                        lockCustomerAccount(con, customerId);

                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, account.accountId);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Deposit failed.");
                    }
                }

                insertTransaction(
                        con, insert, account.accountId,
                        amount, "Deposit into account"
                );

                con.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public boolean withdraw(int customerId, BigDecimal amount)
            throws SQLException {

        validateAmount(amount);

        String update = "UPDATE accounts SET balance = balance - ? "
                      + "WHERE account_id = ? AND balance >= ? "
                      + "AND status = 'ACTIVE'";

        String insert = "INSERT INTO transactions "
                      + "(account_id, transaction_type, amount, "
                      + "reference_number, description) "
                      + "VALUES (?, 'WITHDRAWAL', ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            con.setAutoCommit(false);

            try {
                AccountInfo account =
                        lockCustomerAccount(con, customerId);

                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, account.accountId);
                    ps.setBigDecimal(3, amount);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Insufficient funds.");
                    }
                }

                insertTransaction(
                        con, insert, account.accountId,
                        amount, "Cash withdrawal"
                );

                con.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public boolean transfer(int customerId, String targetNumber,
                            BigDecimal amount) throws SQLException {

        validateAmount(amount);

        if (targetNumber == null || targetNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter a destination account number.");
        }

        String targetSql =
                "SELECT account_id, account_number FROM accounts "
              + "WHERE account_number = ? AND status = 'ACTIVE'";

        String updateSource =
                "UPDATE accounts SET balance = balance - ? "
              + "WHERE account_id = ? AND balance >= ?";

        String updateTarget =
                "UPDATE accounts SET balance = balance + ? "
              + "WHERE account_id = ? AND status = 'ACTIVE'";

        String insert =
                "INSERT INTO transactions "
              + "(account_id, transaction_type, amount, related_account_id, "
              + "reference_number, description) "
              + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            con.setAutoCommit(false);

            try {
                AccountInfo source =
                        lockCustomerAccount(con, customerId);

                int targetId;
                String targetAccountNumber;

                try (PreparedStatement ps =
                        con.prepareStatement(targetSql)) {

                    ps.setString(1, targetNumber.trim());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Destination account not found.");
                        }

                        targetId = rs.getInt("account_id");
                        targetAccountNumber =
                                rs.getString("account_number");
                    }
                }

                if (source.accountId == targetId) {
                    throw new SQLException(
                            "You cannot transfer money to your own account.");
                }

                try (PreparedStatement ps =
                        con.prepareStatement(updateSource)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, source.accountId);
                    ps.setBigDecimal(3, amount);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Insufficient funds.");
                    }
                }

                try (PreparedStatement ps =
                        con.prepareStatement(updateTarget)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, targetId);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Destination account is unavailable.");
                    }
                }

                String reference1 = newReference();
                String reference2 = newReference();

                try (PreparedStatement ps =
                        con.prepareStatement(insert)) {

                    ps.setInt(1, source.accountId);
                    ps.setString(2, "TRANSFER_OUT");
                    ps.setBigDecimal(3, amount);
                    ps.setInt(4, targetId);
                    ps.setString(5, reference1);
                    ps.setString(6,
                            "Transfer to " + targetAccountNumber);
                    ps.executeUpdate();

                    ps.setInt(1, targetId);
                    ps.setString(2, "TRANSFER_IN");
                    ps.setBigDecimal(3, amount);
                    ps.setInt(4, source.accountId);
                    ps.setString(5, reference2);
                    ps.setString(6,
                            "Transfer from " + source.accountNumber);
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private AccountInfo lockCustomerAccount(Connection con,
                                             int customerId)
            throws SQLException {

        String sql = "SELECT account_id, account_number, account_type, "
                   + "balance, status FROM accounts "
                   + "WHERE customer_id = ? AND status = 'ACTIVE' "
                   + "ORDER BY account_id LIMIT 1 FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AccountInfo(
                            rs.getInt("account_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getBigDecimal("balance"),
                            rs.getString("status")
                    );
                }
            }
        }

        throw new SQLException("No active account found.");
    }

    private void insertTransaction(Connection con, String sql,
                                   int accountId, BigDecimal amount,
                                   String description)
            throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, newReference());
            ps.setString(4, description);
            ps.executeUpdate();
        }
    }

    private String newReference() {
        return "TXN-" + UUID.randomUUID()
                .toString().replace("-", "").substring(0, 20)
                .toUpperCase();
    }

    public List<TransactionInfo> getTransactions(int customerId)
            throws SQLException {

        List<TransactionInfo> transactions = new ArrayList<>();

        String sql = "SELECT t.transaction_id, t.transaction_type, "
                   + "t.amount, t.reference_number, t.description, "
                   + "t.created_at FROM transactions t "
                   + "JOIN accounts a ON t.account_id = a.account_id "
                   + "WHERE a.customer_id = ? "
                   + "ORDER BY t.created_at DESC LIMIT 100";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(new TransactionInfo(
                                rs.getInt("transaction_id"),
                                rs.getString("transaction_type"),
                                rs.getBigDecimal("amount"),
                                rs.getString("reference_number"),
                                rs.getString("description"),
                                rs.getTimestamp("created_at")
                        ));
                    }
                }
            }
        }

        return transactions;
    }
}