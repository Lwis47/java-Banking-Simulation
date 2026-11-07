package com.bankingsimulation.bankingsimulation;

import com.bankingsimulation.gui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the Banking Simulation desktop application. */
public class BankingSimulation {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new LoginFrame().setVisible(true);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // The default Swing look and feel is a safe fallback.
        }
    }
}
