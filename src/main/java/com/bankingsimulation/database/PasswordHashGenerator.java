/*
 *
 * @author lwisw
 */
package com.bankingsimulation.database;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: PasswordHashGenerator <password>");
            return;
        }

        String password = args[0];

        String hashedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt(12)
        );

        System.out.println("Generated password hash:");
        System.out.println(hashedPassword);
    }
}
