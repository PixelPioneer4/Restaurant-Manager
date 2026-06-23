package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Ausgaben.
 * Kapselt alle SQL-Operationen für die Tabelle 'expenses'.
 */
public class ExpenseDAO {

    private final Connection connection;

    public ExpenseDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Fügt eine neue Ausgabe in die Datenbank ein.
     * @param e Die neue Ausgabe
     * @return Die gespeicherte Ausgabe mit ID
     */
    public Expense insert(Expense e) {
        String sql = "INSERT INTO expenses (category, amount, expense_date, description) VALUES (?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, e.getCategory());
            stmt.setDouble(2, e.getAmount());
            stmt.setString(3, e.getExpenseDate());
            stmt.setString(4, e.getDescription());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                e.setId(keys.getInt(1));
            }
            return e;
        } catch (SQLException ex) {
            throw new DatabaseException("Fehler beim Speichern der Ausgabe: " + ex.getMessage(), ex);
        }
    }

    /**
     * Gibt alle Ausgaben zurück, sortiert nach Datum DESC.
     * @return Liste aller Ausgaben
     */
    public List<Expense> findAll() {
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC";
        List<Expense> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Fehler beim Laden der Ausgaben: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Gibt alle Ausgaben für einen bestimmten Tag zurück.
     * @param date Datum im Format YYYY-MM-DD
     * @return Liste der Ausgaben
     */
    public List<Expense> findByDate(String date) {
        String sql = "SELECT * FROM expenses WHERE expense_date = ? ORDER BY id DESC";
        List<Expense> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Fehler beim Laden der Tagesausgaben: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Berechnet die Summe aller Ausgaben eines bestimmten Tages.
     * @param date Datum im Format YYYY-MM-DD
     * @return Gesamtsumme der Ausgaben an dem Tag
     */
    public double getTotalByDate(String date) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE expense_date = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Fehler beim Berechnen der Tagesausgaben: " + ex.getMessage(), ex);
        }
        return 0.0;
    }

    /**
     * Berechnet die Summe aller Ausgaben aller Zeiten.
     * @return Gesamtsumme aller Ausgaben
     */
    public double getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Fehler beim Berechnen der Gesamtausgaben: " + ex.getMessage(), ex);
        }
        return 0.0;
    }

    /**
     * Löscht eine Ausgabe.
     * @param id Die ID der Ausgabe
     */
    public void delete(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("Fehler beim Löschen der Ausgabe: " + ex.getMessage(), ex);
        }
    }

    private Expense mapResultSet(ResultSet rs) throws SQLException {
        return new Expense(
            rs.getInt("id"),
            rs.getString("category"),
            rs.getDouble("amount"),
            rs.getString("expense_date"),
            rs.getString("description")
        );
    }
}
