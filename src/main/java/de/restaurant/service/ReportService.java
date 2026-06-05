package de.restaurant.service;

import de.restaurant.dao.DatabaseConnection;
import de.restaurant.exception.DatabaseException;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service-Klasse für Statistiken und Berichte (Bonus-Funktion).
 * Erstellt Umsatzberichte, Top-Gerichte und Tagesstatistiken.
 */
public class ReportService {

    /** Datenbankverbindung (Singleton) */
    private final Connection connection;

    /** Konstruktor: holt die aktive Datenbankverbindung */
    public ReportService() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Gibt den Umsatz der letzten N Tage zurück (nur bezahlte Rechnungen).
     * @param days Anzahl der Tage (z.B. 7 oder 30)
     * @return Map mit Datum (String) → Umsatz (Double)
     */
    public Map<String, Double> getRevenueLastDays(int days) {
        Map<String, Double> revenue = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Für jeden Tag der letzten N Tage den Umsatz laden
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            double dayRevenue = getDailyRevenue(date);
            revenue.put(date, dayRevenue);
        }
        return revenue;
    }

    /**
     * Gibt den Umsatz eines einzelnen Tages zurück.
     * @param date Datum im Format YYYY-MM-DD
     * @return Umsatz in Euro (0.0 wenn keine bezahlten Rechnungen)
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
     * Gibt die Top-N beliebtesten Gerichte zurück (nach Bestellmenge).
     * @param limit Anzahl der Top-Gerichte (z.B. 5)
     * @return Map mit Gerichtsname → Gesamtmenge
     */
    public Map<String, Integer> getTopMenuItems(int limit) {
        String sql = "SELECT m.name, SUM(oi.quantity) AS total_qty " +
                     "FROM order_items oi " +
                     "JOIN menu_items m ON oi.menu_item_id = m.id " +
                     "GROUP BY m.id, m.name " +
                     "ORDER BY total_qty DESC " +
                     "LIMIT ?";
        Map<String, Integer> topItems = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topItems.put(rs.getString("name"), rs.getInt("total_qty"));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Top-Gerichte: " + e.getMessage(), e);
        }
        return topItems;
    }

    /**
     * Gibt die Anzahl der Bestellungen pro Tag für die letzten N Tage zurück.
     * @param days Anzahl der Tage
     * @return Map mit Datum → Anzahl Bestellungen
     */
    public Map<String, Integer> getOrderCountLastDays(int days) {
        String sql = "SELECT DATE(order_date) as day, COUNT(*) as cnt " +
                     "FROM orders " +
                     "WHERE order_date >= DATE('now', ?) " +
                     "GROUP BY day ORDER BY day";
        Map<String, Integer> counts = new LinkedHashMap<>();

        // Alle Tage vorinitialisieren (auch Tage ohne Bestellungen erscheinen mit 0)
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            counts.put(today.minusDays(i).toString(), 0);
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, "-" + (days - 1) + " days");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                counts.put(rs.getString("day"), rs.getInt("cnt"));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Bestellstatistik: " + e.getMessage(), e);
        }
        return counts;
    }

    /**
     * Berechnet den Gesamtumsatz aller Zeit (alle bezahlten Rechnungen).
     * @return Gesamtumsatz in Euro
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices WHERE paid=1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden des Gesamtumsatzes: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Gibt die Gesamtanzahl der Bestellungen zurück.
     * @return Anzahl aller Bestellungen
     */
    public int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Bestellanzahl: " + e.getMessage(), e);
        }
        return 0;
    }
}
