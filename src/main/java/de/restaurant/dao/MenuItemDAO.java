package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Speisekarten-Einträge.
 * Kapselt alle SQL-Operationen für die Tabelle 'menu_items'.
 */
public class MenuItemDAO {

    /** Datenbankverbindung (Singleton) */
    private final Connection connection;

    /** Konstruktor: holt die aktive Datenbankverbindung */
    public MenuItemDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ---- CRUD-Operationen ----

    /**
     * Fügt einen neuen Speisekarten-Eintrag in die Datenbank ein.
     * @param item Das neue Gericht (ohne ID)
     * @return Das gespeicherte Gericht mit zugewiesener ID
     */
    public MenuItem insert(MenuItem item) {
        String sql = "INSERT INTO menu_items (name, category, price, description, available) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCategory());
            stmt.setDouble(3, item.getPrice());
            stmt.setString(4, item.getDescription());
            stmt.setInt(5, item.isAvailable() ? 1 : 0);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                item.setId(keys.getInt(1));
            }
            return item;

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Einfügen des Gerichts: " + e.getMessage(), e);
        }
    }

    /**
     * Aktualisiert einen bestehenden Speisekarten-Eintrag.
     * @param item Das Gericht mit aktualisierten Daten
     */
    public void update(MenuItem item) {
        String sql = "UPDATE menu_items SET name=?, category=?, price=?, description=?, available=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCategory());
            stmt.setDouble(3, item.getPrice());
            stmt.setString(4, item.getDescription());
            stmt.setInt(5, item.isAvailable() ? 1 : 0);
            stmt.setInt(6, item.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Aktualisieren des Gerichts: " + e.getMessage(), e);
        }
    }

    /**
     * Löscht einen Speisekarten-Eintrag anhand seiner ID.
     * @param id Die ID des zu löschenden Gerichts
     */
    public void delete(int id) {
        String sql = "DELETE FROM menu_items WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Löschen des Gerichts: " + e.getMessage(), e);
        }
    }

    /**
     * Gibt alle Speisekarten-Einträge zurück, sortiert nach Kategorie und Name.
     * @return Liste aller Gerichte
     */
    public List<MenuItem> findAll() {
        String sql = "SELECT * FROM menu_items ORDER BY category, name";
        List<MenuItem> items = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Speisekarte: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Gibt alle verfügbaren Gerichte einer bestimmten Kategorie zurück.
     * @param category Die Kategorie (z. B. "Hauptgericht")
     * @return Liste der verfügbaren Gerichte in dieser Kategorie
     */
    public List<MenuItem> findByCategory(String category) {
        String sql = "SELECT * FROM menu_items WHERE category=? AND available=1 ORDER BY name";
        List<MenuItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Kategorie: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Gibt alle verfügbaren Gerichte zurück (für die Bestellmaske).
     * @return Liste aller Gerichte mit available=1
     */
    public List<MenuItem> findAvailable() {
        String sql = "SELECT * FROM menu_items WHERE available=1 ORDER BY category, name";
        List<MenuItem> items = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der verfügbaren Gerichte: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Sucht ein Gericht anhand seiner ID.
     * @param id Die ID des Gerichts
     * @return Das gefundene MenuItem oder null
     */
    public MenuItem findById(int id) {
        String sql = "SELECT * FROM menu_items WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden des Gerichts: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Hilfsmethode: Wandelt einen ResultSet-Eintrag in ein MenuItem-Objekt um.
     */
    private MenuItem mapResultSet(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getString("description"),
                rs.getInt("available") == 1
        );
    }
}
