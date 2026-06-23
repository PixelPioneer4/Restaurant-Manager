package de.restaurant.model;

/**
 * Model-Klasse: Repräsentiert eine Ausgabe (z.B. Personal, Miete, Lieferanten).
 */
public class Expense {
    private int id;
    private String category; // PERSONAL, MIETE, LIEFERANT, SONSTIGES
    private double amount;
    private String expenseDate; // YYYY-MM-DD
    private String description;

    public Expense(int id, String category, double amount, String expenseDate, String description) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
    }

    public Expense(String category, double amount, String expenseDate, String description) {
        this(0, category, amount, expenseDate, description);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("%s | %.2f € | %s", category, amount, expenseDate);
    }
}
