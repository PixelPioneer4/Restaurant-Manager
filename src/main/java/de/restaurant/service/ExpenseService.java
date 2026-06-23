package de.restaurant.service;

import de.restaurant.dao.ExpenseDAO;
import de.restaurant.dao.InvoiceDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.Expense;

import java.time.LocalDate;
import java.util.List;

/**
 * Service-Klasse für die Ausgabenverwaltung und das Gewinn- und Verlust-Reporting.
 */
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final InvoiceDAO invoiceDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    /**
     * Fügt eine neue Ausgabe hinzu.
     * @throws ValidationException bei ungültigen Angaben
     */
    public Expense addExpense(String category, double amount, String date, String description) throws ValidationException {
        validate(category, amount, date);
        Expense expense = new Expense(category, amount, date, description);
        return expenseDAO.insert(expense);
    }

    /**
     * Gibt alle Ausgaben zurück.
     */
    public List<Expense> getAllExpenses() {
        return expenseDAO.findAll();
    }

    /**
     * Löscht eine Ausgabe anhand der ID.
     */
    public void deleteExpense(int id) {
        expenseDAO.delete(id);
    }

    /**
     * Gibt die Summe der Ausgaben an einem bestimmten Tag zurück.
     */
    public double getTotalExpenses(String date) {
        return expenseDAO.getTotalByDate(date);
    }

    /**
     * Gibt die Summe aller Ausgaben zurück.
     */
    public double getTotalExpenses() {
        return expenseDAO.getTotalExpenses();
    }

    /**
     * Gibt den Gesamtumsatz (bezahlte Rechnungen) zurück.
     */
    public double getTotalRevenue() {
        // Holen des Gesamtumsatzes von allen bezahlten Rechnungen
        // Wir können die Summe direkt aus der invoices-Tabelle holen
        return invoiceDAO.getDailyRevenue("%"); // nutzt LIKE '%', was alle Rechnungen summiert
    }

    /**
     * Berechnet den Nettogewinn (Gewinn/Verlust) = Gesamtumsatz - Gesamtausgaben.
     */
    public double getNetProfit() {
        double totalRevenue = invoiceDAO.getDailyRevenue("%");
        double totalExpenses = expenseDAO.getTotalExpenses();
        return totalRevenue - totalExpenses;
    }

    private void validate(String category, double amount, String date) throws ValidationException {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Kategorie darf nicht leer sein.");
        }
        
        boolean validCategory = category.equals("PERSONAL") || 
                               category.equals("MIETE") || 
                               category.equals("LIEFERANT") || 
                               category.equals("SONSTIGES");
        
        if (!validCategory) {
            throw new ValidationException("Ungültige Kategorie. Erlaubt: PERSONAL, MIETE, LIEFERANT, SONSTIGES");
        }

        if (amount <= 0) {
            throw new ValidationException("Der Betrag muss größer als 0 sein.");
        }

        if (date == null || date.trim().isEmpty()) {
            throw new ValidationException("Das Datum darf nicht leer sein.");
        }

        try {
            LocalDate.parse(date.trim());
        } catch (Exception ex) {
            throw new ValidationException("Ungültiges Datumsformat. Bitte JJJJ-MM-TT verwenden.");
        }
    }
}
