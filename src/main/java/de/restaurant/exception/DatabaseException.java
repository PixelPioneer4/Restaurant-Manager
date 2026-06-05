package de.restaurant.exception;

/**
 * Eigene Ausnahme für Datenbankfehler.
 * Kapselt SQL-Fehler in eine anwendungsspezifische Exception.
 */
public class DatabaseException extends RuntimeException {

    /** Erstellt eine neue DatabaseException mit Nachricht. */
    public DatabaseException(String message) {
        super(message);
    }

    /** Erstellt eine neue DatabaseException mit Nachricht und Ursache. */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
