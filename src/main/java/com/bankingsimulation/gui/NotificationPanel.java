package com.bankingsimulation.gui;

import com.bankingsimulation.database.NotificationDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/** Displays and manages the signed-in customer's notifications. */
public class NotificationPanel extends JPanel {

    private static final Color BACKGROUND = new Color(245, 248, 252);
    private static final Color NAVY = new Color(15, 35, 70);
    private static final Color BLUE = new Color(0, 120, 215);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm");

    private final int customerId;
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final JPanel notificationList = new JPanel();
    private final JLabel unreadLabel = new JLabel();

    public NotificationPanel(int customerId) {
        this.customerId = customerId;

        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);

        notificationList.setLayout(new BoxLayout(notificationList, BoxLayout.Y_AXIS));
        notificationList.setBackground(BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(notificationList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshNotifications();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 4));
        text.setOpaque(false);
        JLabel title = new JLabel("NOTIFICATIONS");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(NAVY);
        unreadLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        unreadLabel.setForeground(Color.DARK_GRAY);
        text.add(title);
        text.add(unreadLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton markAllButton = new JButton("Mark all as read");
        markAllButton.setBackground(BLUE);
        markAllButton.setForeground(Color.WHITE);
        markAllButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshNotifications());
        markAllButton.addActionListener(e -> markAllAsRead());
        actions.add(refreshButton);
        actions.add(markAllButton);

        header.add(text, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void refreshNotifications() {
        notificationList.removeAll();
        try {
            List<NotificationDAO.Notification> notifications =
                    notificationDAO.getNotifications(customerId);
            int unreadCount = 0;

            for (NotificationDAO.Notification notification : notifications) {
                if (!notification.isRead) {
                    unreadCount++;
                }
                notificationList.add(createNotificationCard(notification));
                notificationList.add(Box.createVerticalStrut(10));
            }

            if (notifications.isEmpty()) {
                notificationList.add(createEmptyState());
            }
            unreadLabel.setText(unreadCount == 0
                    ? "You are all caught up."
                    : unreadCount + " unread notification"
                    + (unreadCount == 1 ? "" : "s"));
        } catch (SQLException ex) {
            unreadLabel.setText("Notifications are currently unavailable.");
            notificationList.add(createErrorState(ex));
        }

        notificationList.revalidate();
        notificationList.repaint();
    }

    private JPanel createNotificationCard(NotificationDAO.Notification notification) {
        JPanel card = new JPanel(new BorderLayout(15, 5));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        card.setBackground(notification.isRead ? Color.WHITE : new Color(232, 242, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(15, 18, 15, 18)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(notification.title);
        title.setFont(new Font("Arial", notification.isRead ? Font.PLAIN : Font.BOLD, 16));
        JLabel message = new JLabel("<html>" + escapeHtml(notification.message) + "</html>");
        message.setFont(new Font("Arial", Font.PLAIN, 14));
        message.setForeground(Color.DARK_GRAY);
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(message);

        String timestamp = notification.createdAt == null ? "" : DATE_FORMAT.format(
                notification.createdAt.toInstant().atZone(ZoneId.systemDefault()));
        JLabel date = new JLabel(timestamp, SwingConstants.RIGHT);
        date.setForeground(Color.GRAY);
        date.setFont(new Font("Arial", Font.PLAIN, 12));

        card.add(text, BorderLayout.CENTER);
        card.add(date, BorderLayout.EAST);
        return card;
    }

    private JPanel createEmptyState() {
        JPanel empty = new JPanel(new BorderLayout());
        empty.setAlignmentX(Component.LEFT_ALIGNMENT);
        empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        empty.setBackground(Color.WHITE);
        empty.setBorder(BorderFactory.createEmptyBorder(35, 20, 35, 20));
        JLabel label = new JLabel("No notifications yet.", SwingConstants.CENTER);
        label.setForeground(Color.GRAY);
        empty.add(label, BorderLayout.CENTER);
        return empty;
    }

    private JPanel createErrorState(SQLException ex) {
        JPanel error = new JPanel(new BorderLayout());
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        error.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        error.setBackground(new Color(255, 241, 241));
        error.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        JLabel label = new JLabel("Could not load notifications: " + ex.getMessage());
        label.setForeground(new Color(160, 35, 35));
        error.add(label, BorderLayout.CENTER);
        return error;
    }

    private void markAllAsRead() {
        try {
            notificationDAO.markAllAsRead(customerId);
            refreshNotifications();
        } catch (SQLException ex) {
            unreadLabel.setText("Could not update notifications: " + ex.getMessage());
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
