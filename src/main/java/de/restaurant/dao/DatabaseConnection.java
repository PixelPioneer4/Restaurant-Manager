package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Singleton-Klasse für die Datenbankverbindung.
 * Stellt eine einzige gemeinsame Verbindung zur SQLite-Datenbank bereit.
 * Initialisiert das Schema und die Beispieldaten beim ersten Start.
 */
public class DatabaseConnection {

    /** Dateiname der SQLite-Datenbank (wird im Programmverzeichnis erstellt) */
    private static final String DB_URL = "jdbc:sqlite:restaurant.db";

    /** Die einzige Instanz (Singleton-Pattern) */
    private static DatabaseConnection instance;

    /** Die aktive Datenbankverbindung */
    private Connection connection;

    // ---- Privater Konstruktor (Singleton) ----

    /**
     * Privater Konstruktor: Stellt Verbindung her und initialisiert DB.
     * @throws DatabaseException wenn die Verbindung fehlschlägt
     */
    private DatabaseConnection() {
        try {
            // JDBC-Treiber laden
            Class.forName("org.sqlite.JDBC");

            // Verbindung öffnen
            connection = DriverManager.getConnection(DB_URL);

            // Foreign Keys aktivieren (SQLite-spezifisch)
            connection.createStatement().execute("PRAGMA foreign_keys = ON");

            // Schema erstellen (falls nicht vorhanden)
            executeScript("schema.sql");

            // Beispieldaten einfügen (nur beim ersten Start)
            executeScript("sample_data.sql");

            System.out.println("Datenbankverbindung erfolgreich hergestellt.");

        } catch (ClassNotFoundException e) {
            throw new DatabaseException("SQLite JDBC-Treiber nicht gefunden.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Datenbankverbindung fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    // ---- Singleton-Zugriff ----

    /**
     * Gibt die einzige Instanz der Datenbankverbindung zurück.
     * Erstellt diese beim ersten Aufruf (Lazy Initialization).
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Gibt die aktive Connection zurück.
     * @return JDBC-Connection zur SQLite-Datenbank
     */
    public Connection getConnection() {
        return connection;
    }

    // ---- Hilfsmethoden ----

    /**
     * Liest eine SQL-Skriptdatei aus den Ressourcen und führt sie aus.
     * @param resourceName Dateiname im resources-Verzeichnis
     */
    private void executeScript(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                System.err.println("Ressource nicht gefunden: " + resourceName);
                return;
            }

            // Dateiinhalt lesen
            String sql = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Einzelne Statements ausführen (durch Semikolon getrennt)
            Statement stmt = connection.createStatement();
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException e) {
                        // ALTER TABLE Fehler ignorieren (z. B. wenn die Spalte bereits existiert)
                        if (trimmed.toUpperCase().contains("ALTER TABLE")) {
                            System.out.println("Info: ALTER TABLE nicht ausgeführt/fehlgeschlagen: " + e.getMessage());
                        } else {
                            throw e;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Ausführen von " + resourceName + ": " + e.getMessage());
        }
    }

    /**
     * Schließt die Datenbankverbindung (beim Beenden der Anwendung aufrufen).
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Datenbankverbindung geschlossen.");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen der Verbindung: " + e.getMessage());
        }
    }
}
