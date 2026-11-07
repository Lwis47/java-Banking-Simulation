/**
 *
 * @author lwisw
 */
package com.bankingsimulation.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import org.mindrot.jbcrypt.BCrypt;

public class LoginDAO {

    public int authenticateAndGetCustomerId(
            String email, String password) throws SQLException {

        String sql = "SELECT customer_id, password_hash "
                   + "FROM customers WHERE email = ?";

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email.trim().toLowerCase(Locale.ROOT));

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        String storedHash =
                            rs.getString("password_hash");

                        if (BCrypt.checkpw(password, storedHash)) {
                            return rs.getInt("customer_id");
                        }
                    }
                }
            }

        } catch (IllegalArgumentException ex) {
            return -1;
        }

        return -1;
    }
}
