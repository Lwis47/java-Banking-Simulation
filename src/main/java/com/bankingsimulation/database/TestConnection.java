package com.bankingsimulation.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("SUCCESS: Java is connected to MySQL!");
        } catch (SQLException ex) {
            System.err.println("FAILED: Could not connect to MySQL: "
                    + ex.getMessage());
        }
    }
}
