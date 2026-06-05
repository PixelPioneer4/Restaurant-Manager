package de.restaurant.gui.panels;

/**
 * Interface für alle Panels, die nach einem Tab-Wechsel ihre Daten aktualisieren sollen.
 * Wird von MainFrame verwendet, um automatisch refresh() aufzurufen.
 */
public interface Refreshable {
    /**
     * Lädt die Daten des Panels neu aus der Datenbank.
     * Wird automatisch beim Aktivieren des Tabs aufgerufen.
     */
    void refresh();
}
