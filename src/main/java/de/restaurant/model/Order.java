package de.restaurant.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model-Klasse: Repräsentiert eine Bestellung (Kopfdaten).
 * Enthält Tischnummer, Datum, Status und eine Liste von Bestellpositionen.
 */
public class Order {

    /** Datenbankkennung */
    private int id;

    /** Zugeordneter Kunde (optional – kann null sein bei Laufkundschaft) */
    private Customer customer;

    /** Tischnummer im Restaurant */
    private int tableNumber;

    /** Datum und Uhrzeit der Bestellung */
    private LocalDateTime orderDate;

    /**
     * Aktueller Status der Bestellung.
     * Mögliche Werte: OFFEN, IN_BEARBEITUNG, FERTIG, STORNIERT
     */
    private String status;

    /** Liste aller Positionen dieser Bestellung */
    private List<OrderItem> items;

    // ---- Konstruktoren ----

    /** Konstruktor für neue Bestellungen */
    public Order(Customer customer, int tableNumber, LocalDateTime orderDate) {
        this.customer   = customer;
        this.tableNumber = tableNumber;
        this.orderDate  = orderDate;
        this.status     = "OFFEN";
        this.items      = new ArrayList<>();
    }

    /** Vollständiger Konstruktor – wird beim Laden aus der DB verwendet */
    public Order(int id, Customer customer, int tableNumber,
                 LocalDateTime orderDate, String status) {
        this.id          = id;
        this.customer    = customer;
        this.tableNumber = tableNumber;
        this.orderDate   = orderDate;
        this.status      = status;
        this.items       = new ArrayList<>();
    }

    // ---- Berechnungsmethoden ----

    /**
     * Berechnet den Gesamtbetrag aller Positionen.
     * @return Summe aller Bestellpositionen in Euro
     */
    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    // ---- Getter und Setter ----

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public Customer getCustomer()               { return customer; }
    public void setCustomer(Customer c)         { this.customer = c; }

    public int getTableNumber()                 { return tableNumber; }
    public void setTableNumber(int t)           { this.tableNumber = t; }

    public LocalDateTime getOrderDate()         { return orderDate; }
    public void setOrderDate(LocalDateTime d)   { this.orderDate = d; }

    public String getStatus()                   { return status; }
    public void setStatus(String s)             { this.status = s; }

    public List<OrderItem> getItems()           { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    /** Fügt eine Bestellposition hinzu */
    public void addItem(OrderItem item) { items.add(item); }

    @Override
    public String toString() {
        return String.format("Bestellung #%d | Tisch %d | %s", id, tableNumber, status);
    }
}
