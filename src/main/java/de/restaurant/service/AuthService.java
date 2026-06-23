package de.restaurant.service;

import de.restaurant.dao.UserDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.User;

/**
 * Service-Klasse für die Benutzerauthentifizierung und Sitzungsverwaltung.
 */
public class AuthService {

    private final UserDAO userDAO;
    
    /** Statische Variable zur Speicherung des aktuell angemeldeten Benutzers (Sitzung) */
    private static User currentUser;

    /** Konstruktor: initialisiert das UserDAO */
    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Führt die Anmeldung eines Benutzers durch.
     * @param username Eingegebener Benutzername
     * @param password Eingegebenes Passwort
     * @return Das angemeldete User-Objekt bei Erfolg
     * @throws ValidationException wenn Felder leer sind oder die Anmeldedaten falsch sind
     */
    public User login(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Benutzername darf nicht leer sein.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Passwort darf nicht leer sein.");
        }

        User user = userDAO.findByCredentials(username.trim(), password);
        if (user == null) {
            throw new ValidationException("Falscher Benutzername oder Passwort");
        }

        currentUser = user;
        return user;
    }

    /**
     * Gibt den aktuell angemeldeten Benutzer zurück.
     * @return Der angemeldete Benutzer oder null falls keine Sitzung aktiv
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Beendet die aktuelle Sitzung (Abmeldung).
     */
    public static void logout() {
        currentUser = null;
    }
}
