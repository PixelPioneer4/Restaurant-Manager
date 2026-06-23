package de.restaurant.service;

import de.restaurant.dao.InvoiceDAO;
import de.restaurant.dao.OrderDAO;
import de.restaurant.exception.DatabaseException;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.Invoice;
import de.restaurant.model.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service-Klasse für Rechnungsverwaltung.
 * Erstellt Rechnungen aus Bestellungen und verwaltet Bezahlstatus.
 */
public class InvoiceService {

    /** Datenzugriffsobjekte */
    private final InvoiceDAO invoiceDAO;
    private final OrderDAO   orderDAO;

    /** Konstruktor: erzeugt die DAOs */
    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.orderDAO   = new OrderDAO();
    }

    /**
     * Erstellt eine neue Rechnung für eine abgeschlossene Bestellung.
     * @param orderId Die ID der Bestellung (muss Status FERTIG haben)
     * @return Die erstellte Rechnung
     * @throws ValidationException wenn Bestellung nicht gefunden oder bereits berechnet
     */
    public Invoice createInvoice(int orderId) throws ValidationException {
        // Bestellung laden
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new ValidationException("Bestellung #" + orderId + " nicht gefunden.");
        }

        // Prüfen ob bereits eine Rechnung existiert
        if (invoiceDAO.existsForOrder(orderId)) {
            throw new ValidationException("Für Bestellung #" + orderId + " existiert bereits eine Rechnung.");
        }

        // Bestellpositionen prüfen
        if (order.getItems().isEmpty()) {
            throw new ValidationException("Die Bestellung enthält keine Positionen.");
        }

        // Rechnung erstellen und speichern
        Invoice invoice = new Invoice(order, LocalDateTime.now());
        return invoiceDAO.insert(invoice);
    }

    /**
     * Erstellt eine Rechnung für eine übergebene Bestellung.
     * @param order Die Bestellung
     * @return Die erstellte Rechnung
     */
    public Invoice createForOrder(Order order) {
        return invoiceDAO.insertForOrder(order);
    }

    /**
     * Markiert eine Rechnung als bezahlt.
     * @param invoiceId Die Rechnungs-ID
     */
    public void markAsPaid(int invoiceId) {
        invoiceDAO.markAsPaid(invoiceId);
    }

    /**
     * Gibt alle Rechnungen zurück (neueste zuerst).
     * @return Liste aller Rechnungen
     */
    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }

    /**
     * Gibt den Tagesumsatz für ein bestimmtes Datum zurück.
     * Zählt nur bezahlte Rechnungen.
     * @param date Datum im Format YYYY-MM-DD
     * @return Tagesumsatz in Euro
     */
    public double getDailyRevenue(String date) {
        return invoiceDAO.getDailyRevenue(date);
    }
}
