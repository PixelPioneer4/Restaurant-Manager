package de.restaurant.service;

import de.restaurant.dao.OrderDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service-Klasse für Bestellverwaltung.
 * Steuert den gesamten Lebenszyklus einer Bestellung.
 */
public class OrderService {

    /** Die möglichen Bestellstatus */
    public static final String[] STATUSES = {
        "OFFEN", "IN_BEARBEITUNG", "FERTIG", "STORNIERT"
    };

    /** Datenzugriffsobjekt für Bestellungen */
    private final OrderDAO orderDAO;

    /** Konstruktor: erzeugt den OrderDAO */
    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    /**
     * Legt eine neue Bestellung an.
     * @param customer    Kunde (kann null sein für Laufkundschaft)
     * @param tableNumber Tischnummer (muss >= 1 sein)
     * @param items       Liste der Bestellpositionen (mindestens 1)
     * @return Die gespeicherte Bestellung mit ID
     * @throws ValidationException wenn Eingaben ungültig sind
     */
    public Order createOrder(Customer customer, int tableNumber,
                             List<OrderItem> items) throws ValidationException {
        // Validierung
        if (tableNumber < 1) {
            throw new ValidationException("Die Tischnummer muss mindestens 1 sein.");
        }
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Eine Bestellung muss mindestens ein Gericht enthalten.");
        }

        // Neue Bestellung erstellen
        Order order = new Order(customer, tableNumber, LocalDateTime.now());
        order.setItems(items);

        // In der Datenbank speichern
        return orderDAO.insert(order);
    }

    /**
     * Ändert den Status einer Bestellung.
     * @param orderId   Die Bestellungs-ID
     * @param newStatus Der neue Status
     */
    public void updateStatus(int orderId, String newStatus) {
        orderDAO.updateStatus(orderId, newStatus);
    }

    /**
     * Gibt alle Bestellungen zurück.
     * @return Liste aller Bestellungen (neueste zuerst)
     */
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    /**
     * Sucht eine Bestellung anhand ihrer ID.
     * @param id Die Bestellungs-ID
     * @return Die gefundene Bestellung oder null
     */
    public Order getOrderById(int id) {
        return orderDAO.findById(id);
    }

    /**
     * Gibt alle Bestellungen eines bestimmten Tages zurück.
     * @param date Datum im Format YYYY-MM-DD
     * @return Tagesliste der Bestellungen
     */
    public List<Order> getOrdersByDate(String date) {
        return orderDAO.findByDate(date);
    }
}
