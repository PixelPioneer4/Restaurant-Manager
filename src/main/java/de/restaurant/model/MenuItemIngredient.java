package de.restaurant.model;

/**
 * Model-Klasse: Repräsentiert die Zutat und die Menge, die für ein Gericht benötigt wird.
 */
public class MenuItemIngredient {
    private int menuItemId;
    private int ingredientId;
    private double amountNeeded;

    public MenuItemIngredient(int menuItemId, int ingredientId, double amountNeeded) {
        this.menuItemId = menuItemId;
        this.ingredientId = ingredientId;
        this.amountNeeded = amountNeeded;
    }

    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }

    public double getAmountNeeded() { return amountNeeded; }
    public void setAmountNeeded(double amountNeeded) { this.amountNeeded = amountNeeded; }
}
