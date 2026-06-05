package de.restaurant.exception;

/**
 * Eigene Ausnahme für Validierungsfehler bei Benutzereingaben.
 * Wird geworfen, wenn Pflichtfelder leer sind oder Werte ungültig sind.
 */
public class ValidationException extends Exception {

    /** Erstellt eine neue ValidationException mit Nachricht. */
    public ValidationException(String message) {
        super(message);
    }
}
