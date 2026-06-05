package de.restaurant.model;

import java.time.LocalDateTime;

/**
 * Model-Klasse: Repräsentiert eine Rechnung zu einer abgeschlossenen Bestellung.
 * Enthält den Gesamtbetrag, die MwSt. und den Bezahlstatus.
 */
public class Invoice {

    /** Datenbankkennung */
    private int id;

    /** Zugehörige Bestellung */
    private Order order;

    /** Nettobetrag + MwSt. = Gesamtbetrag */
    private double totalAmount;

    /** Mehrwertsteueranteil (19%) */
    private double taxAmount;

    /** Ausstellungsdatum der Rechnung */
    private LocalDateTime issueDate;

    /** Bezahlstatus: false = offen, true = bezahlt */
    private boolean paid;

    // ---- Steuerkonstante ----

    /** MwSt.-Satz in Deutschland */
    public static final double TAX_RATE = 0.19;

    // ---- Konstruktoren ----

    /** Konstruktor zum Erstellen einer neuen Rechnung aus einer Bestellung */
    public Invoice(Order order, LocalDateTime issueDate) {
        this.order       = order;
        this.issueDate   = issueDate;
        this.totalAmount = order.getTotalAmount();
        // MwSt. aus Bruttobetrag herausrechnen (inklusiv)
        this.taxAmount   = totalAmount * TAX_RATE / (1 + TAX_RATE);
        this.paid        = false;
    }

    /** Vollständiger Konstruktor – wird beim Laden aus der DB verwendet */
    public Invoice(int id, Order order, double totalAmount, double taxAmount,
                   LocalDateTime issueDate, boolean paid) {
        this.id          = id;
        this.order       = order;
        this.totalAmount = totalAmount;
        this.taxAmount   = taxAmount;
        this.issueDate   = issueDate;
        this.paid        = paid;
    }

    // ---- Berechnungsmethode ----

    /** Berechnet den Nettobetrag (ohne MwSt.) */
    public double getNetAmount() {
        return totalAmount - taxAmount;
    }

    // ---- Getter und Setter ----

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public Order getOrder()                     { return order; }
    public void setOrder(Order o)               { this.order = o; }

    public double getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(double t)        { this.totalAmount = t; }

    public double getTaxAmount()                { return taxAmount; }
    public void setTaxAmount(double t)          { this.taxAmount = t; }

    public LocalDateTime getIssueDate()         { return issueDate; }
    public void setIssueDate(LocalDateTime d)   { this.issueDate = d; }

    public boolean isPaid()                     { return paid; }
    public void setPaid(boolean p)              { this.paid = p; }

    @Override
    public String toString() {
        return String.format("Rechnung #%d | %.2f € | %s",
                id, totalAmount, paid ? "Bezahlt" : "Offen");
    }
}
