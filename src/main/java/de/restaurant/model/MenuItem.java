package de.restaurant.model;

/**
 * Model-Klasse: Repräsentiert einen Eintrag in der Speisekarte.
 * Enthält Name, Kategorie, Preis, Beschreibung und Verfügbarkeit.
 */
public class MenuItem {

    /** Datenbankkennung */
    private int id;

    /** Bezeichnung des Gerichts (Pflichtfeld) */
    private String name;

    /** Kategorie: Vorspeise, Hauptgericht, Dessert oder Getränk */
    private String category;

    /** Preis in Euro */
    private double price;

    /** Kurze Beschreibung des Gerichts */
    private String description;

    /** Verfügbarkeit: true = bestellbar, false = nicht verfügbar */
    private boolean available;

    // ---- Konstruktoren ----

    /** Konstruktor für neue Speisekarten-Einträge (ohne ID) */
    public MenuItem(String name, String category, double price,
                    String description, boolean available) {
        this.name        = name;
        this.category    = category;
        this.price       = price;
        this.description = description;
        this.available   = available;
    }

    /** Vollständiger Konstruktor – wird beim Laden aus der DB verwendet */
    public MenuItem(int id, String name, String category, double price,
                    String description, boolean available) {
        this.id          = id;
        this.name        = name;
        this.category    = category;
        this.price       = price;
        this.description = description;
        this.available   = available;
    }

    // ---- Getter und Setter ----

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getName()               { return name; }
    public void setName(String n)         { this.name = n; }

    public String getCategory()           { return category; }
    public void setCategory(String c)     { this.category = c; }

    public double getPrice()              { return price; }
    public void setPrice(double p)        { this.price = p; }

    public String getDescription()        { return description; }
    public void setDescription(String d)  { this.description = d; }

    public boolean isAvailable()              { return available; }
    public void setAvailable(boolean a)       { this.available = a; }

    /** Darstellung für ComboBoxen: "Wiener Schnitzel – 18,90 €" */
    @Override
    public String toString() {
        return String.format("%s – %.2f €", name, price);
    }
}
