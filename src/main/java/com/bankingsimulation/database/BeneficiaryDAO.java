/**
 *
 * @author lwisw
 */

package com.bankingsimulation.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BeneficiaryDAO {

    public static class Beneficiary {
        public int beneficiaryId;
        public String accountNumber;
        public String nickname;

        public Beneficiary(int id, String number, String name) {
            beneficiaryId = id;
            accountNumber = number;
            nickname = name;
        }
    }

    public boolean addBeneficiary(
            int customerId,
            String accountNumber,
            String nickname) throws SQLException {

        if (accountNumber == null || accountNumber.trim().isEmpty()
                || nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter both an account number and a nickname.");
        }

        String findAccount =
            "SELECT account_id, customer_id FROM accounts "
            + "WHERE account_number = ? AND status = 'ACTIVE'";

        String insert =
            "INSERT INTO beneficiaries "
            + "(customer_id, account_id, nickname) "
            + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement find =
                    con.prepareStatement(findAccount)) {

                find.setString(1, accountNumber.trim());

                try (ResultSet rs = find.executeQuery()) {

                    if (!rs.next()) {
                        return false;
                    }

                    int accountId = rs.getInt("account_id");
                    if (rs.getInt("customer_id") == customerId) {
                        throw new IllegalArgumentException(
                                "You cannot add your own account as a beneficiary.");
                    }

                    try (PreparedStatement add =
                            con.prepareStatement(insert)) {

                        add.setInt(1, customerId);
                        add.setInt(2, accountId);
                        add.setString(3, nickname.trim());

                        return add.executeUpdate() > 0;
                    }
                }
            }
        }
    }

    public List<Beneficiary> getBeneficiaries(
            int customerId) throws SQLException {

        List<Beneficiary> list = new ArrayList<>();

        String sql =
            "SELECT b.beneficiary_id, a.account_number, b.nickname "
            + "FROM beneficiaries b "
            + "JOIN accounts a ON b.account_id = a.account_id "
            + "WHERE b.customer_id = ? "
            + "ORDER BY b.created_at DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        list.add(new Beneficiary(
                            rs.getInt("beneficiary_id"),
                            rs.getString("account_number"),
                            rs.getString("nickname")
                        ));
                    }
                }
            }
        }

        return list;
    }

    public boolean deleteBeneficiary(
            int customerId,
            int beneficiaryId) throws SQLException {

        String sql =
            "DELETE FROM beneficiaries "
            + "WHERE beneficiary_id = ? AND customer_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, beneficiaryId);
                ps.setInt(2, customerId);

                return ps.executeUpdate() > 0;
            }
        }
    }
}

