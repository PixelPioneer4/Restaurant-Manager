package de.restaurant.model;

public class StockMovement {
    private int id;
    private int ingredientId;
    private String movementType;
    private double amount;
    private String movementDate;
    private String note;

    public StockMovement(int ingredientId, String movementType,
                         double amount, String movementDate, String note) {
        this.ingredientId = ingredientId;
        this.movementType = movementType;
        this.amount = amount;
        this.movementDate = movementDate;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIngredientId() { return ingredientId; }
    public String getMovementType() { return movementType; }
    public double getAmount() { return amount; }
    public String getMovementDate() { return movementDate; }
    public String getNote() { return note; }
}
