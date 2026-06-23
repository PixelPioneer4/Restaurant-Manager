package de.restaurant.model;

public class Ingredient {
    private int id;
    private String name;
    private double quantity;
    private String unit;
    private double minStock;

    public Ingredient(int id, String name,
                      double quantity, String unit, double minStock) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.minStock = minStock;
    }

    public Ingredient(String name, double quantity,
                      String unit, double minStock) {
        this(0, name, quantity, unit, minStock);
    }

    public boolean isLowStock() {
        return quantity <= minStock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public double getMinStock() { return minStock; }
    public void setMinStock(double minStock) { this.minStock = minStock; }

    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }
}
