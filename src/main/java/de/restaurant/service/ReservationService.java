package de.restaurant.service;

import de.restaurant.dao.ReservationDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.Customer;
import de.restaurant.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service-Klasse für Reservierungsverwaltung.
 * Enthält Validierungs- und Verfügbarkeitsprüfungslogik.
 */
public class ReservationService {

    /** Datenzugriffsobjekt für Reservierungen */
    private final ReservationDAO reservationDAO;

    /** Konstruktor: erzeugt den ReservationDAO */
    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
    }

    /**
     * Legt eine neue Reservierung an.
     * @param customer    Kunde (kann null sein)
     * @param tableNumber Tischnummer (1–20)
     * @param date        Reservierungsdatum (darf nicht in der Vergangenheit liegen)
     * @param time        Reservierungszeit
     * @param guestCount  Personenanzahl (mindestens 1)
     * @param notes       Sonderwünsche (optional)
     * @return Die gespeicherte Reservierung mit ID
     * @throws ValidationException bei ungültigen Eingaben
     */
    public Reservation createReservation(Customer customer, int tableNumber,
                                         LocalDate date, LocalTime time,
                                         int guestCount, String notes) throws ValidationException {
        // Eingaben validieren
        if (tableNumber < 1 || tableNumber > 20) {
            throw new ValidationException("Tischnummer muss zwischen 1 und 20 liegen.");
        }
        if (date == null) {
            throw new ValidationException("Bitte ein Datum angeben.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("Das Datum darf nicht in der Vergangenheit liegen.");
        }
        if (time == null) {
            throw new ValidationException("Bitte eine Uhrzeit angeben.");
        }
        if (guestCount < 1) {
            throw new ValidationException("Die Personenanzahl muss mindestens 1 sein.");
        }

        Reservation reservation = new Reservation(customer, tableNumber, date, time, guestCount, notes);
        return reservationDAO.insert(reservation);
    }

    /**
     * Storniert eine Reservierung (löscht sie aus der Datenbank).
     * @param id Die Reservierungs-ID
     */
    public void cancelReservation(int id) {
        reservationDAO.delete(id);
    }

    /**
     * Gibt alle Reservierungen zurück.
     * @return Liste aller Reservierungen (chronologisch)
     */
    public List<Reservation> getAllReservations() {
        return reservationDAO.findAll();
    }

    /**
     * Gibt alle Reservierungen eines bestimmten Datums zurück.
     * @param date Das gewünschte Datum
     * @return Tagesreservierungen
     */
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationDAO.findByDate(date);
    }
}
