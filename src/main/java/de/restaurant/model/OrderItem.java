package de.restaurant.model;

/**
 * Model-Klasse: Repräsentiert eine einzelne Position innerhalb einer Bestellung.
 * Enthält das bestellte Gericht, Menge und den Einzelpreis.
 */
public class OrderItem {

    /** Datenbankkennung */
    private int id;

    /** Zugehörige Bestellungs-ID */
    private int orderId;

    /** Das bestellte Speisekarten-Gericht */
    private MenuItem menuItem;

    /** Bestellte Menge */
    private int quantity;

    /**
     * Einzelpreis zum Zeitpunkt der Bestellung.
     * Wird separat gespeichert, damit Preisänderungen keine alten Bestellungen beeinflussen.
     */
    private double unitPrice;

    // ---- Konstruktoren ----

    /** Konstruktor für neue Bestellpositionen */
    public OrderItem(int orderId, MenuItem menuItem, int quantity) {
        this.orderId   = orderId;
        this.menuItem  = menuItem;
        this.quantity  = quantity;
        this.unitPrice = menuItem.getPrice(); // Preis zum Bestellzeitpunkt übernehmen
    }

    /** Vollständiger Konstruktor – wird beim Laden aus der DB verwendet */
    public OrderItem(int id, int orderId, MenuItem menuItem,
                     int quantity, double unitPrice) {
        this.id        = id;
        this.orderId   = orderId;
        this.menuItem  = menuItem;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    // ---- Berechnungsmethode ----

    /** Berechnet den Gesamtpreis dieser Position (Menge × Einzelpreis) */
    public double getSubtotal() {
        return quantity * unitPrice;
    }

    // ---- Getter und Setter ----

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getOrderId()                 { return orderId; }
    public void setOrderId(int orderId)     { this.orderId = orderId; }

    public MenuItem getMenuItem()           { return menuItem; }
    public void setMenuItem(MenuItem m)     { this.menuItem = m; }

    public int getQuantity()                { return quantity; }
    public void setQuantity(int q)          { this.quantity = q; }

    public double getUnitPrice()            { return unitPrice; }
    public void setUnitPrice(double p)      { this.unitPrice = p; }

    @Override
    public String toString() {
        return String.format("%dx %s – %.2f €", quantity, menuItem.getName(), getSubtotal());
    }
}
