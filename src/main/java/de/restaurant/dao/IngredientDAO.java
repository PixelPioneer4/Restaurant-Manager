package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.Ingredient;
import de.restaurant.model.StockMovement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {

    private final Connection connection;

    public IngredientDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Ingredient> findAll() {
        String sql = "SELECT * FROM ingredients ORDER BY name";
        List<Ingredient> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden: " + e.getMessage(), e);
        }
        return list;
    }

    public Ingredient findById(int id) {
        String sql = "SELECT * FROM ingredients WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Fehler: " + e.getMessage(), e);
        }
        return null;
    }

    public Ingredient insert(Ingredient i) {
        String sql = "INSERT INTO ingredients (name,quantity,unit,min_stock)"
                   + " VALUES (?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, i.getName());
            stmt.setDouble(2, i.getQuantity());
            stmt.setString(3, i.getUnit());
            stmt.setDouble(4, i.getMinStock());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) i.setId(keys.getInt(1));
            return i;
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Speichern: " + e.getMessage(), e);
        }
    }

    public void update(Ingredient i) {
        String sql = "UPDATE ingredients SET name=?,quantity=?,"
                   + "unit=?,min_stock=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, i.getName());
            stmt.setDouble(2, i.getQuantity());
            stmt.setString(3, i.getUnit());
            stmt.setDouble(4, i.getMinStock());
            stmt.setInt(5, i.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Update: " + e.getMessage(), e);
        }
    }

    public void updateQuantity(int id, double qty) {
        String sql = "UPDATE ingredients SET quantity=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, qty);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM ingredients WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler: " + e.getMessage(), e);
        }
    }

    public void insertMovement(StockMovement m) {
        String sql = "INSERT INTO stock_movements"
                   + "(ingredient_id,movement_type,amount,movement_date,note)"
                   + " VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, m.getIngredientId());
            stmt.setString(2, m.getMovementType());
            stmt.setDouble(3, m.getAmount());
            stmt.setString(4, m.getMovementDate());
            stmt.setString(5, m.getNote());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Fehler: " + e.getMessage(), e);
        }
    }

    public List<StockMovement> findMovements(int ingredientId) {
        String sql = "SELECT * FROM stock_movements"
                   + " WHERE ingredient_id=? ORDER BY movement_date DESC";
        List<StockMovement> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new StockMovement(
                    rs.getInt("ingredient_id"),
                    rs.getString("movement_type"),
                    rs.getDouble("amount"),
                    rs.getString("movement_date"),
                    rs.getString("note")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fehler: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Ingredient> findLowStock() {
        String sql = "SELECT * FROM ingredients WHERE quantity <= min_stock";
        List<Ingredient> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Fehler: " + e.getMessage(), e);
        }
        return list;
    }

    private Ingredient map(ResultSet rs) throws SQLException {
        return new Ingredient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("quantity"),
            rs.getString("unit"),
            rs.getDouble("min_stock")
        );
    }
}
