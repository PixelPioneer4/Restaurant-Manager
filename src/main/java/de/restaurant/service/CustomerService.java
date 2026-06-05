package de.restaurant.service;

import de.restaurant.dao.CustomerDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.Customer;

import java.util.List;

/**
 * Service-Klasse für Kundenverwaltung.
 * Enthält Geschäftslogik und Validierung für Kundenoperationen.
 */
public class CustomerService {

    /** Datenzugriffsobjekt für Kunden */
    private final CustomerDAO customerDAO;

    /** Konstruktor: erzeugt den CustomerDAO */
    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    /**
     * Legt einen neuen Kunden an, nach Validierung der Eingaben.
     * @param name  Vollständiger Name (Pflichtfeld)
     * @param phone Telefonnummer (optional)
     * @param email E-Mail-Adresse (optional)
     * @return Der gespeicherte Kunde mit ID
     * @throws ValidationException wenn der Name leer ist
     */
    public Customer createCustomer(String name, String phone, String email) throws ValidationException {
        // Validierung: Name ist Pflichtfeld
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Der Kundenname darf nicht leer sein.");
        }

        Customer customer = new Customer(name.trim(), phone, email);
        return customerDAO.insert(customer);
    }

    /**
     * Aktualisiert einen bestehenden Kunden.
     * @param customer Der Kunde mit aktualisierten Daten
     * @throws ValidationException wenn der Name leer ist
     */
    public void updateCustomer(Customer customer) throws ValidationException {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new ValidationException("Der Kundenname darf nicht leer sein.");
        }
        customerDAO.update(customer);
    }

    /**
     * Löscht einen Kunden aus dem System.
     * @param id Die ID des zu löschenden Kunden
     */
    public void deleteCustomer(int id) {
        customerDAO.delete(id);
    }

    /**
     * Gibt alle Kunden zurück (alphabetisch sortiert).
     * @return Liste aller Kunden
     */
    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    /**
     * Sucht Kunden anhand eines Suchbegriffs.
     * @param term Name, Telefon oder E-Mail (Teilsuche)
     * @return Gefundene Kunden
     */
    public List<Customer> searchCustomers(String term) {
        if (term == null || term.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.search(term.trim());
    }
}
