package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.MenuItemIngredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für die Verknüpfung zwischen Gerichten und Zutaten.
 */
public class MenuItemIngredientDAO {

    private final Connection connection;

    public MenuItemIngredientDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Findet alle Zutaten-Verknüpfungen für ein bestimmtes Gericht.
     * @param menuItemId Die ID des Gerichts
     * @return Liste der verknüpften Zutaten und Mengen
     */
    public List<MenuItemIngredient> findByMenuItemId(int menuItemId) {
        String sql = "SELECT * FROM menu_item_ingredients WHERE menu_item_id = ?";
        List<MenuItemIngredient> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, menuItemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new MenuItemIngredient(
                    rs.getInt("menu_item_id"),
                    rs.getInt("ingredient_id"),
                    rs.getDouble("amount_needed")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Rezeptur: " + e.getMessage(), e);
        }
        return list;
    }
}
