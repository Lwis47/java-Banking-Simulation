/**
 *
 * @author lwisw
 */

package com.bankingsimulation.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public static class Notification {
        public int notificationId;
        public String title;
        public String message;
        public boolean isRead;
        public Timestamp createdAt;

        public Notification(
                int id,
                String title,
                String message,
                boolean isRead,
                Timestamp createdAt) {

            this.notificationId = id;
            this.title = title;
            this.message = message;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }
    }

    // CREATE NOTIFICATION
    public boolean createNotification(
            int customerId,
            String title,
            String message) throws SQLException {

        String sql =
                "INSERT INTO notifications "
                + "(customer_id, title, message) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, customerId);
                ps.setString(2, title);
                ps.setString(3, message);

                return ps.executeUpdate() > 0;
            }
        }
    }

    // GET CUSTOMER NOTIFICATIONS
    public List<Notification> getNotifications(
            int customerId) throws SQLException {

        List<Notification> list = new ArrayList<>();

        String sql =
                "SELECT notification_id, title, message, "
                + "is_read, created_at "
                + "FROM notifications "
                + "WHERE customer_id = ? "
                + "ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        list.add(new Notification(
                                rs.getInt("notification_id"),
                                rs.getString("title"),
                                rs.getString("message"),
                                rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at")
                        ));
                    }
                }
            }
        }

        return list;
    }

    // COUNT UNREAD NOTIFICATIONS
    public int getUnreadCount(
            int customerId) throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM notifications "
                + "WHERE customer_id = ? AND is_read = FALSE";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        return 0;
    }

    // MARK NOTIFICATION AS READ
    public boolean markAsRead(
            int customerId,
            int notificationId) throws SQLException {

        String sql =
                "UPDATE notifications "
                + "SET is_read = TRUE "
                + "WHERE notification_id = ? "
                + "AND customer_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, notificationId);
                ps.setInt(2, customerId);

                return ps.executeUpdate() > 0;
            }
        }
    }

    // MARK ALL NOTIFICATIONS AS READ
    public boolean markAllAsRead(
            int customerId) throws SQLException {

        String sql =
                "UPDATE notifications "
                + "SET is_read = TRUE "
                + "WHERE customer_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, customerId);

                ps.executeUpdate();
                return true;
            }
        }
    }
}
