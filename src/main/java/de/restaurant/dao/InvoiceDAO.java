package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.Invoice;
import de.restaurant.model.Order;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Rechnungen.
 * Kapselt alle SQL-Operationen für die Tabelle 'invoices'.
 */
public class InvoiceDAO {

    /** Datumsformat für die Datenbankablage */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Datenbankverbindung (Singleton) */
    private final Connection connection;

    /** Hilfsobjekt zum Laden von Bestelldaten */
    private final OrderDAO orderDAO;

    /** Konstruktor: initialisiert Datenbankverbindung */
    public InvoiceDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.orderDAO   = new OrderDAO();
    }

    // ---- CRUD-Operationen ----

    /**
     * Erstellt eine neue Rechnung in der Datenbank.
     * @param invoice Die neue Rechnung (ohne ID)
     * @return Die gespeicherte Rechnung mit zugewiesener ID
     */
    public Invoice insert(Invoice invoice) {
        String sql = "INSERT INTO invoices (order_id, total_amount, tax_amount, issue_date, paid) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, invoice.getOrder().getId());
            stmt.setDouble(2, invoice.getTotalAmount());
            stmt.setDouble(3, invoice.getTaxAmount());
            stmt.setString(4, invoice.getIssueDate().format(FORMATTER));
            stmt.setInt(5, invoice.isPaid() ? 1 : 0);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                invoice.setId(keys.getInt(1));
            }
            return invoice;

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Erstellen der Rechnung: " + e.getMessage(), e);
        }
    }

    /**
     * Markiert eine Rechnung als bezahlt.
     * @param invoiceId Die Rechnungs-ID
     */
    public void markAsPaid(int invoiceId) {
        String sql = "UPDATE invoices SET paid=1 WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Markieren der Rechnung: " + e.getMessage(), e);
        }
    }

    /**
     * Prüft, ob für eine Bestellung bereits eine Rechnung existiert.
     * @param orderId Die Bestellungs-ID
     * @return true wenn eine Rechnung existiert
     */
    public boolean existsForOrder(int orderId) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE order_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Fehler bei der Rechnungsprüfung: " + e.getMessage(), e);
        }
    }

    /**
     * Gibt alle Rechnungen zurück, sortiert nach Datum (neueste zuerst).
     * @return Liste aller Rechnungen
     */
    public List<Invoice> findAll() {
        String sql = "SELECT * FROM invoices ORDER BY issue_date DESC";
        List<Invoice> invoices = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                invoices.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Rechnungen: " + e.getMessage(), e);
        }
        return invoices;
    }

    /**
     * Gibt den Gesamtumsatz eines bestimmten Tages zurück (nur bezahlte Rechnungen).
     * @param date Datum im Format YYYY-MM-DD
     * @return Tagesumsatz in Euro
     */
    public double getDailyRevenue(String date) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices " +
                     "WHERE issue_date LIKE ? AND paid=1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, date + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden des Tagesumsatzes: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Hilfsmethode: Wandelt einen ResultSet-Eintrag in ein Invoice-Objekt um.
     */
    private Invoice mapResultSet(ResultSet rs) throws SQLException {
        Order order = orderDAO.findById(rs.getInt("order_id"));
        return new Invoice(
                rs.getInt("id"),
                order,
                rs.getDouble("total_amount"),
                rs.getDouble("tax_amount"),
                LocalDateTime.parse(rs.getString("issue_date"), FORMATTER),
                rs.getInt("paid") == 1
        );
    }
}
