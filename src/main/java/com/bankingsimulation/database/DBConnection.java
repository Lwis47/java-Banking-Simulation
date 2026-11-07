/*author cypher lwis*/
package com.bankingsimulation.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/banking_simulation";

    private DBConnection() {
    }

    /**
     * Opens a connection using system properties first and environment
     * variables second. This keeps database credentials out of source control.
     *
     * <p>Supported settings are {@code banking.db.url},
     * {@code banking.db.user}, and {@code banking.db.password}, or the
     * environment-variable equivalents {@code BANKING_DB_URL},
     * {@code BANKING_DB_USER}, and {@code BANKING_DB_PASSWORD}.</p>
     */
    public static Connection getConnection() throws SQLException {
        String url = setting("banking.db.url", "BANKING_DB_URL", DEFAULT_URL);
        String user = setting("banking.db.user", "BANKING_DB_USER", "root");
        String password = setting("banking.db.password", "BANKING_DB_PASSWORD", "");

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException(
                    "Unable to connect to the banking database. "
                    + "Check the database settings and ensure MySQL is running.",
                    e
            );
        }
    }

    private static String setting(String property, String environment,
                                  String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
