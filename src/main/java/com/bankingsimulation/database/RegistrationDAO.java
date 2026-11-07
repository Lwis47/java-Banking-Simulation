package com.bankingsimulation.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import org.mindrot.jbcrypt.BCrypt;

/** Creates a customer and their initial active account in one transaction. */
public class RegistrationDAO {

    public int register(String firstName, String lastName, String email,
                        char[] password) throws SQLException {
        validate(firstName, lastName, email, password);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String passwordHash = BCrypt.hashpw(new String(password), BCrypt.gensalt(12));

        String insertCustomer = "INSERT INTO customers "
                + "(first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?)";
        String insertAccount = "INSERT INTO accounts "
                + "(customer_id, account_number, account_type, balance, status) "
                + "VALUES (?, ?, 'SAVINGS', 0.00, 'ACTIVE')";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int customerId;
                try (PreparedStatement customer = con.prepareStatement(
                        insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                    customer.setString(1, firstName.trim());
                    customer.setString(2, lastName.trim());
                    customer.setString(3, normalizedEmail);
                    customer.setString(4, passwordHash);
                    customer.executeUpdate();

                    try (ResultSet keys = customer.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not create the customer account.");
                        }
                        customerId = keys.getInt(1);
                    }
                }

                try (PreparedStatement account = con.prepareStatement(insertAccount)) {
                    account.setInt(1, customerId);
                    account.setString(2, accountNumberFor(customerId));
                    account.executeUpdate();
                }

                con.commit();
                return customerId;
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private void validate(String firstName, String lastName, String email,
                          char[] password) {
        if (firstName == null || firstName.trim().isEmpty()
                || lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter your first and last name.");
        }
        if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Your password must contain at least 8 characters.");
        }
    }

    private String accountNumberFor(int customerId) {
        if (customerId > 99_999_999) {
            throw new IllegalStateException("Account number range has been exhausted.");
        }
        return String.format("20%08d", customerId);
    }
}
