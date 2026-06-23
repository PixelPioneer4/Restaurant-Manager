package de.restaurant.service;

import de.restaurant.dao.IngredientDAO;
import de.restaurant.exception.ValidationException;
import de.restaurant.model.Ingredient;
import de.restaurant.model.StockMovement;

import java.time.LocalDate;
import java.util.List;

public class InventoryService {

    private final IngredientDAO dao;

    public InventoryService() {
        this.dao = new IngredientDAO();
    }

    public List<Ingredient> getAllIngredients() {
        return dao.findAll();
    }

    public List<Ingredient> getLowStockItems() {
        return dao.findLowStock();
    }

    public Ingredient addIngredient(String name, double quantity,
                                    String unit, double minStock)
                                    throws ValidationException {
        validate(name, quantity, unit, minStock);
        return dao.insert(new Ingredient(name, quantity, unit, minStock));
    }

    public void updateIngredient(Ingredient ingredient)
                                 throws ValidationException {
        validate(ingredient.getName(), ingredient.getQuantity(),
                 ingredient.getUnit(), ingredient.getMinStock());
        dao.update(ingredient);
    }

    public void deleteIngredient(int id) {
        dao.delete(id);
    }

    public void addStock(int id, double amount,
                         String note) throws ValidationException {
        if (amount <= 0)
            throw new ValidationException("Menge muss größer als 0 sein.");
        Ingredient i = dao.findById(id);
        if (i == null)
            throw new ValidationException("Zutat nicht gefunden.");
        dao.updateQuantity(id, i.getQuantity() + amount);
        dao.insertMovement(new StockMovement(
            id, "EINGANG", amount,
            LocalDate.now().toString(), note));
    }

    public void removeStock(int id, double amount,
                            String note) throws ValidationException {
        if (amount <= 0)
            throw new ValidationException("Menge muss größer als 0 sein.");
        Ingredient i = dao.findById(id);
        if (i == null)
            throw new ValidationException("Zutat nicht gefunden.");
        if (i.getQuantity() < amount)
            throw new ValidationException(
                "Nicht genug auf Lager: " + i.getName()
                + " (Verfügbar: " + i.getQuantity() + " " + i.getUnit() + ")");
        dao.updateQuantity(id, i.getQuantity() - amount);
        dao.insertMovement(new StockMovement(
            id, "AUSGANG", amount,
            LocalDate.now().toString(), note));
    }

    public List<StockMovement> getMovements(int ingredientId) {
        return dao.findMovements(ingredientId);
    }

    private void validate(String name, double quantity,
                          String unit, double minStock)
                          throws ValidationException {
        if (name == null || name.trim().isEmpty())
            throw new ValidationException("Name darf nicht leer sein.");
        if (quantity < 0)
            throw new ValidationException("Menge darf nicht negativ sein.");
        if (unit == null || unit.trim().isEmpty())
            throw new ValidationException("Einheit darf nicht leer sein.");
        if (minStock < 0)
            throw new ValidationException("Mindestbestand darf nicht negativ sein.");
    }
}
