package de.restaurant;

import de.restaurant.dao.DatabaseConnection;
import de.restaurant.gui.LoginDialog;
import de.restaurant.gui.MainFrame;

import javax.swing.*;

/**
 * Einstiegspunkt der Restaurant Management System Anwendung.
 *
 * Startreihenfolge:
 *  1. Look & Feel auf System-Standard setzen
 *  2. Datenbankverbindung herstellen (Schema + Beispieldaten)
 *  3. Login-Dialog anzeigen
 *  4. Bei Erfolg: Hauptfenster öffnen
 */
public class Main {

    /**
     * Hauptmethode – startet die Anwendung.
     * @param args Kommandozeilenargumente (werden nicht verwendet)
     */
    public static void main(String[] args) {

        // Look & Feel: Betriebssystem-Standard verwenden (Windows, macOS, Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look & Feel konnte nicht gesetzt werden: " + e.getMessage());
        }

        // Swing-Thread: GUI-Erstellung über Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {

            try {
                // Datenbankverbindung herstellen und Schema initialisieren
                DatabaseConnection.getInstance();

                // Login-Dialog anzeigen
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);

                if (login.isLoginSuccessful()) {
                    // Hauptfenster erstellen und anzeigen
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }

            } catch (Exception e) {
                // Schwerwiegender Fehler beim Start → Meldung anzeigen und beenden
                JOptionPane.showMessageDialog(
                        null,
                        "Fehler beim Starten der Anwendung:\n" + e.getMessage(),
                        "Startfehler",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}
