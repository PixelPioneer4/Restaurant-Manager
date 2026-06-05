package de.restaurant.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Model-Klasse: Repräsentiert eine Tischreservierung.
 * Enthält Datum, Uhrzeit, Tischnummer, Personenanzahl und optionale Notizen.
 */
public class Reservation {

    /** Datenbankkennung */
    private int id;

    /** Zugehöriger Kunde (kann null sein) */
    private Customer customer;

    /** Reservierter Tisch */
    private int tableNumber;

    /** Datum der Reservierung */
    private LocalDate reservationDate;

    /** Uhrzeit der Reservierung */
    private LocalTime reservationTime;

    /** Anzahl der Gäste */
    private int guestCount;

    /** Besondere Wünsche oder Notizen */
    private String notes;

    // ---- Konstruktoren ----

    /** Konstruktor für neue Reservierungen */
    public Reservation(Customer customer, int tableNumber, LocalDate date,
                       LocalTime time, int guestCount, String notes) {
        this.customer         = customer;
        this.tableNumber      = tableNumber;
        this.reservationDate  = date;
        this.reservationTime  = time;
        this.guestCount       = guestCount;
        this.notes            = notes;
    }

    /** Vollständiger Konstruktor – wird beim Laden aus der DB verwendet */
    public Reservation(int id, Customer customer, int tableNumber, LocalDate date,
                       LocalTime time, int guestCount, String notes) {
        this.id               = id;
        this.customer         = customer;
        this.tableNumber      = tableNumber;
        this.reservationDate  = date;
        this.reservationTime  = time;
        this.guestCount       = guestCount;
        this.notes            = notes;
    }

    // ---- Getter und Setter ----

    public int getId()                            { return id; }
    public void setId(int id)                     { this.id = id; }

    public Customer getCustomer()                 { return customer; }
    public void setCustomer(Customer c)           { this.customer = c; }

    public int getTableNumber()                   { return tableNumber; }
    public void setTableNumber(int t)             { this.tableNumber = t; }

    public LocalDate getReservationDate()         { return reservationDate; }
    public void setReservationDate(LocalDate d)   { this.reservationDate = d; }

    public LocalTime getReservationTime()         { return reservationTime; }
    public void setReservationTime(LocalTime t)   { this.reservationTime = t; }

    public int getGuestCount()                    { return guestCount; }
    public void setGuestCount(int g)              { this.guestCount = g; }

    public String getNotes()                      { return notes; }
    public void setNotes(String n)                { this.notes = n; }

    @Override
    public String toString() {
        String customerName = (customer != null) ? customer.getName() : "Laufkundschaft";
        return String.format("Tisch %d | %s | %s %s | %d Gäste",
                tableNumber, customerName, reservationDate, reservationTime, guestCount);
    }
}
