package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.Customer;
import de.restaurant.model.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Reservierungen.
 * Kapselt alle SQL-Operationen für die Tabelle 'reservations'.
 */
public class ReservationDAO {

    /** Datenbankverbindung (Singleton) */
    private final Connection connection;

    /** Hilfsobjekt zum Laden von Kundendaten */
    private final CustomerDAO customerDAO;

    /** Konstruktor: initialisiert Datenbankverbindung */
    public ReservationDAO() {
        this.connection  = DatabaseConnection.getInstance().getConnection();
        this.customerDAO = new CustomerDAO();
    }

    // ---- CRUD-Operationen ----

    /**
     * Fügt eine neue Reservierung in die Datenbank ein.
     * @param reservation Die neue Reservierung (ohne ID)
     * @return Die gespeicherte Reservierung mit zugewiesener ID
     */
    public Reservation insert(Reservation reservation) {
        String sql = "INSERT INTO reservations (customer_id, table_number, reservation_date, " +
                     "reservation_time, guest_count, notes) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            if (reservation.getCustomer() != null) {
                stmt.setInt(1, reservation.getCustomer().getId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, reservation.getTableNumber());
            stmt.setString(3, reservation.getReservationDate().toString());
            stmt.setString(4, reservation.getReservationTime().toString());
            stmt.setInt(5, reservation.getGuestCount());
            stmt.setString(6, reservation.getNotes());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                reservation.setId(keys.getInt(1));
            }
            return reservation;

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Anlegen der Reservierung: " + e.getMessage(), e);
        }
    }

    /**
     * Löscht eine Reservierung (Stornierung).
     * @param id Die ID der zu stornierenden Reservierung
     */
    public void delete(int id) {
        String sql = "DELETE FROM reservations WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Stornieren der Reservierung: " + e.getMessage(), e);
        }
    }

    /**
     * Gibt alle Reservierungen zurück, sortiert nach Datum und Uhrzeit.
     * @return Liste aller Reservierungen
     */
    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservations ORDER BY reservation_date, reservation_time";
        List<Reservation> reservations = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Reservierungen: " + e.getMessage(), e);
        }
        return reservations;
    }

    /**
     * Gibt alle Reservierungen eines bestimmten Datums zurück.
     * @param date Das Datum im Format YYYY-MM-DD
     * @return Liste der Reservierungen an diesem Tag
     */
    public List<Reservation> findByDate(LocalDate date) {
        String sql = "SELECT * FROM reservations WHERE reservation_date=? ORDER BY reservation_time";
        List<Reservation> reservations = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, date.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reservations.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Tagesreservierungen: " + e.getMessage(), e);
        }
        return reservations;
    }

    /**
     * Hilfsmethode: Wandelt einen ResultSet-Eintrag in ein Reservation-Objekt um.
     */
    private Reservation mapResultSet(ResultSet rs) throws SQLException {
        Customer customer = null;
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            customer = customerDAO.findById(customerId);
        }

        return new Reservation(
                rs.getInt("id"),
                customer,
                rs.getInt("table_number"),
                LocalDate.parse(rs.getString("reservation_date")),
                LocalTime.parse(rs.getString("reservation_time")),
                rs.getInt("guest_count"),
                rs.getString("notes")
        );
    }
}
