package de.restaurant.model;

/**
 * Model-Klasse: Repräsentiert einen Benutzer/Mitarbeiter des Systems.
 * Wird für die Anmeldung und das Berechtigungssystem (Rollen) verwendet.
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String role; // ADMIN, MANAGER, STAFF

    /**
     * Standardkonstruktor
     */
    public User() {
    }

    /**
     * Vollständiger Konstruktor
     */
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ---- Getter und Setter ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return String.format("User #%d | %s (%s)", id, username, role);
    }
}
