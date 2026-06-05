package de.restaurant.dao;

import de.restaurant.exception.DatabaseException;
import de.restaurant.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object für Bestellungen.
 * Kapselt alle SQL-Operationen für die Tabellen 'orders' und 'order_items'.
 */
public class OrderDAO {

    /** Datumsformat für die Datenbankablage */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Datenbankverbindung (Singleton) */
    private final Connection connection;

    /** Hilfsobjekte für verknüpfte Entitäten */
    private final CustomerDAO customerDAO;
    private final MenuItemDAO menuItemDAO;

    /** Konstruktor: initialisiert Datenbankverbindung */
    public OrderDAO() {
        this.connection  = DatabaseConnection.getInstance().getConnection();
        this.customerDAO = new CustomerDAO();
        this.menuItemDAO = new MenuItemDAO();
    }

    // ---- Bestellung-Operationen ----

    /**
     * Legt eine neue Bestellung (Header + Positionen) in der Datenbank an.
     * @param order Die neue Bestellung
     * @return Die gespeicherte Bestellung mit zugewiesener ID
     */
    public Order insert(Order order) {
        String sql = "INSERT INTO orders (customer_id, table_number, order_date, status) VALUES (?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            // customer_id kann null sein (Laufkundschaft)
            if (order.getCustomer() != null) {
                stmt.setInt(1, order.getCustomer().getId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, order.getTableNumber());
            stmt.setString(3, order.getOrderDate().format(FORMATTER));
            stmt.setString(4, order.getStatus());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                order.setId(keys.getInt(1));
            }

            // Bestellpositionen speichern
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                insertOrderItem(item);
            }

            return order;

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Anlegen der Bestellung: " + e.getMessage(), e);
        }
    }

    /**
     * Aktualisiert den Status einer Bestellung.
     * @param orderId Die Bestellungs-ID
     * @param newStatus Der neue Status (OFFEN, IN_BEARBEITUNG, FERTIG, STORNIERT)
     */
    public void updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Aktualisieren des Status: " + e.getMessage(), e);
        }
    }

    /**
     * Gibt alle Bestellungen zurück, sortiert nach Datum (neueste zuerst).
     * @return Liste aller Bestellungen mit ihren Positionen
     */
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = mapResultSet(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Bestellungen: " + e.getMessage(), e);
        }
        return orders;
    }

    /**
     * Sucht eine einzelne Bestellung anhand ihrer ID.
     * @param id Die Bestellungs-ID
     * @return Die gefundene Bestellung oder null
     */
    public Order findById(int id) {
        String sql = "SELECT * FROM orders WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Order order = mapResultSet(rs);
                order.setItems(findOrderItems(order.getId()));
                return order;
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Bestellung: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gibt alle Bestellungen eines bestimmten Datums zurück.
     * Wird für den Tagesumsatz und die Statistiken verwendet.
     * @param date Datum im Format YYYY-MM-DD
     * @return Liste der Bestellungen an diesem Tag
     */
    public List<Order> findByDate(String date) {
        String sql = "SELECT * FROM orders WHERE order_date LIKE ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, date + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSet(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Tagesbestellungen: " + e.getMessage(), e);
        }
        return orders;
    }

    // ---- Bestellposition-Operationen ----

    /**
     * Fügt eine Bestellposition in die Datenbank ein.
     * @param item Die Bestellposition
     */
    public void insertOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price) VALUES (?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getMenuItem().getId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitPrice());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                item.setId(keys.getInt(1));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Speichern der Bestellposition: " + e.getMessage(), e);
        }
    }

    /**
     * Lädt alle Positionen einer Bestellung aus der Datenbank.
     * @param orderId Die Bestellungs-ID
     * @return Liste der Bestellpositionen
     */
    public List<OrderItem> findOrderItems(int orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id=?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Gericht aus der Speisekarte laden
                MenuItem menuItem = menuItemDAO.findById(rs.getInt("menu_item_id"));
                items.add(new OrderItem(
                        rs.getInt("id"),
                        orderId,
                        menuItem,
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                ));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fehler beim Laden der Bestellpositionen: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Hilfsmethode: Wandelt einen ResultSet-Eintrag in ein Order-Objekt um.
     */
    private Order mapResultSet(ResultSet rs) throws SQLException {
        // Kunden laden (falls vorhanden)
        Customer customer = null;
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            customer = customerDAO.findById(customerId);
        }

        return new Order(
                rs.getInt("id"),
                customer,
                rs.getInt("table_number"),
                LocalDateTime.parse(rs.getString("order_date"), FORMATTER),
                rs.getString("status")
        );
    }
}
