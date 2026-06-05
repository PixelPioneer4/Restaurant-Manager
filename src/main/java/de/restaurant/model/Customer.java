package de.restaurant.model;

/**
 * Model-Klasse: Repräsentiert einen Kunden im Restaurant-System.
 * Enthält alle persönlichen Daten und wird in der Datenbank gespeichert.
 */
public class Customer {

    /** Eindeutige Datenbankkennung (0 = noch nicht gespeichert) */
    private int id;

    /** Vollständiger Name des Kunden (Pflichtfeld) */
    private String name;

    /** Telefonnummer (optional) */
    private String phone;

    /** E-Mail-Adresse (optional) */
    private String email;

    // ---- Konstruktoren ----

    /** Konstruktor für neue Kunden (ohne ID – wird von der DB vergeben) */
    public Customer(String name, String phone, String email) {
        this.name  = name;
        this.phone = phone;
        this.email = email;
    }

    /** Vollständiger Konstruktor – wird beim Laden aus der DB verwendet */
    public Customer(int id, String name, String phone, String email) {
        this.id    = id;
        this.name  = name;
        this.phone = phone;
        this.email = email;
    }

    // ---- Getter und Setter ----

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public String getName()         { return name; }
    public void setName(String n)   { this.name = n; }

    public String getPhone()        { return phone; }
    public void setPhone(String p)  { this.phone = p; }

    public String getEmail()        { return email; }
    public void setEmail(String e)  { this.email = e; }

    /** Lesbare Darstellung für Debugging und Comboboxen */
    @Override
    public String toString() {
        return name + (phone != null && !phone.isEmpty() ? " (" + phone + ")" : "");
    }
}
