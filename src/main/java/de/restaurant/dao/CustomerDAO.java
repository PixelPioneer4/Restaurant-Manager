package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Kunden.
 * Kapselt alle SQL-Operationen für die Tabelle 'customers'.
 */
public class CustomerDAO {

    /** Datenbankverbindung (Singleton) */
    private final Connection connection;

    /** Konstruktor: holt die aktive Datenbankverbindung */
    public CustomerDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ---- CRUD-Operationen ----

    /**
     * Fügt einen neuen Kunden in die Datenbank ein.
     * @param customer Der neue Kunde (ohne ID)
     * @return Der eingefügte Kunde mit zugewiesener ID
     * @throws DatabaseException bei SQL-Fehlern
     */
    public Customer insert(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.executeUpdate();

            // Automatisch vergebene ID auslesen
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                customer.setId(keys.getInt(1));
            }
            return customer;

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Einfügen des Kunden: " + e.getMessage(), e);
        }
    }

    /**
     * Aktualisiert die Daten eines bestehenden Kunden.
     * @param customer Der Kunde mit aktualisierten Daten
     * @throws DatabaseException bei SQL-Fehlern
     */
    public void update(Customer customer) {
        String sql = "UPDATE customers SET name=?, phone=?, email=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setInt(4, customer.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Aktualisieren des Kunden: " + e.getMessage(), e);
        }
    }

    /**
     * Löscht einen Kunden anhand seiner ID.
     * @param id Die ID des zu löschenden Kunden
     * @throws DatabaseException bei SQL-Fehlern
     */
    public void delete(int id) {
        String sql = "DELETE FROM customers WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Löschen des Kunden: " + e.getMessage(), e);
        }
    }

    /**
     * Gibt alle Kunden aus der Datenbank zurück.
     * @return Liste aller Kunden, alphabetisch sortiert
     * @throws DatabaseException bei SQL-Fehlern
     */
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY name";
        List<Customer> customers = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Kunden: " + e.getMessage(), e);
        }
        return customers;
    }

    /**
     * Sucht Kunden anhand eines Suchbegriffs (Name, Telefon oder E-Mail).
     * @param searchTerm Der Suchbegriff
     * @return Liste der passenden Kunden
     */
    public List<Customer> search(String searchTerm) {
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY name";
        List<Customer> customers = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler bei der Kundensuche: " + e.getMessage(), e);
        }
        return customers;
    }

    /**
     * Sucht einen Kunden anhand seiner ID.
     * @param id Die Kundennummer
     * @return Der gefundene Kunde oder null
     */
    public Customer findById(int id) {
        String sql = "SELECT * FROM customers WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden des Kunden: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Hilfsmethode: Wandelt einen ResultSet-Eintrag in ein Customer-Objekt um.
     * @param rs Der aktuelle ResultSet
     * @return Ein Customer-Objekt
     */
    private Customer mapResultSet(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }
}
