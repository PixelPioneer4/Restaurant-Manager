package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object für Benutzer.
 * Kapselt alle SQL-Operationen für die Tabelle 'users'.
 */
public class UserDAO {

    private final Connection connection;

    /** Konstruktor: initialisiert Datenbankverbindung */
    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Sucht einen Benutzer nach seinem Benutzernamen.
     * @param username Der Benutzername
     * @return Das User-Objekt oder null falls nicht gefunden
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Suchen des Benutzers nach Name: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Sucht einen Benutzer anhand von Benutzername und Passwort.
     * Wird für den Login-Check verwendet.
     * @param username Der Benutzername
     * @param password Das Passwort
     * @return Das User-Objekt bei erfolgreichem Match, sonst null
     */
    public User findByCredentials(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Abgleichen der Benutzerdaten: " + e.getMessage(), e);
        }
        return null;
    }

    /** Hilfsmethode: Wandelt einen ResultSet-Eintrag in ein User-Objekt um */
    private User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
        );
    }
}
