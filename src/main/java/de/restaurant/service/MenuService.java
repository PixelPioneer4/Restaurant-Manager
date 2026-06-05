package de.restaurant.service;

import de.restaurant.dao.MenuItemDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.MenuItem;

import java.util.List;

/**
 * Service-Klasse für die Speisekartenverwaltung.
 * Enthält Validierungslogik für Speisekarten-Operationen.
 */
public class MenuService {

    /** Die verfügbaren Kategorien in der Speisekarte */
    public static final String[] CATEGORIES = {
        "Vorspeise", "Hauptgericht", "Dessert", "Getränk"
    };

    /** Datenzugriffsobjekt für Speisekarte */
    private final MenuItemDAO menuItemDAO;

    /** Konstruktor: erzeugt den MenuItemDAO */
    public MenuService() {
        this.menuItemDAO = new MenuItemDAO();
    }

    /**
     * Fügt ein neues Gericht zur Speisekarte hinzu.
     * @param name        Name des Gerichts (Pflichtfeld)
     * @param category    Kategorie (Pflichtfeld)
     * @param price       Preis in Euro (muss > 0 sein)
     * @param description Kurzbeschreibung (optional)
     * @param available   Verfügbarkeit
     * @return Das gespeicherte Gericht mit ID
     * @throws ValidationException wenn Name/Kategorie fehlen oder Preis ungültig
     */
    public MenuItem addMenuItem(String name, String category, double price,
                                String description, boolean available) throws ValidationException {
        validateMenuItem(name, category, price);

        MenuItem item = new MenuItem(name.trim(), category, price, description, available);
        return menuItemDAO.insert(item);
    }

    /**
     * Aktualisiert ein bestehendes Gericht in der Speisekarte.
     * @param item Das Gericht mit aktualisierten Daten
     * @throws ValidationException wenn Eingaben ungültig sind
     */
    public void updateMenuItem(MenuItem item) throws ValidationException {
        validateMenuItem(item.getName(), item.getCategory(), item.getPrice());
        menuItemDAO.update(item);
    }

    /**
     * Löscht ein Gericht aus der Speisekarte.
     * @param id Die ID des Gerichts
     */
    public void deleteMenuItem(int id) {
        menuItemDAO.delete(id);
    }

    /**
     * Gibt alle Gerichte in der Speisekarte zurück.
     * @return Liste aller Gerichte (sortiert nach Kategorie)
     */
    public List<MenuItem> getAllMenuItems() {
        return menuItemDAO.findAll();
    }

    /**
     * Gibt alle verfügbaren Gerichte zurück (für die Bestellmaske).
     * @return Liste der verfügbaren Gerichte
     */
    public List<MenuItem> getAvailableMenuItems() {
        return menuItemDAO.findAvailable();
    }

    // ---- Validierungshilfsmethode ----

    /**
     * Prüft die Eingaben für ein Gericht auf Vollständigkeit und Korrektheit.
     * @throws ValidationException bei ungültigen Eingaben
     */
    private void validateMenuItem(String name, String category, double price) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Der Name des Gerichts darf nicht leer sein.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Bitte eine Kategorie auswählen.");
        }
        if (price <= 0) {
            throw new ValidationException("Der Preis muss größer als 0,00 € sein.");
        }
    }
}
