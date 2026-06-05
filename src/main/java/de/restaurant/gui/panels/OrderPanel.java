package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.Customer;
import de.restaurant.model.Order;
import de.restaurant.model.OrderItem;
import de.restaurant.model.MenuItem;
import de.restaurant.service.CustomerService;
import de.restaurant.service.MenuService;
import de.restaurant.service.OrderService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI-Panel für die Bestellverwaltung.
 * Ermöglicht das Anlegen neuer Bestellungen und die Statusverwaltung.
 */
public class OrderPanel extends JPanel implements Refreshable {

    // ---- Services ----
    private final OrderService    orderService    = new OrderService();
    private final CustomerService customerService = new CustomerService();
    private final MenuService     menuService     = new MenuService();

    // ---- Bestellübersicht (links) ----
    private JTable orderTable;
    private DefaultTableModel orderTableModel;

    // ---- Positionen-Tabelle (rechts oben) ----
    private JTable itemTable;
    private DefaultTableModel itemTableModel;

    // ---- Neue Bestellung (rechts unten) ----
    private JComboBox<Customer>  cbCustomer;
    private JSpinner             spTableNumber;
    private JComboBox<de.restaurant.model.MenuItem>  cbMenuItem;
    private JSpinner             spQuantity;
    private JButton btnAddItem, btnCreateOrder, btnClearOrder;
    private JLabel lblTotal;

    // ---- Statusverwaltung ----
    private JComboBox<String> cbStatus;
    private JButton btnUpdateStatus;

    /** Temporäre Liste der Positionen für die neue Bestellung */
    private final List<OrderItem> currentItems = new ArrayList<>();

    private static final String[] ORDER_COLUMNS = {"ID", "Tisch", "Kunde", "Datum", "Status", "Gesamt (€)"};
    private static final String[] ITEM_COLUMNS  = {"Gericht", "Menge", "Einzelpreis", "Gesamt"};

    /** Konstruktor */
    public OrderPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 255));

        buildOrderTable();
        buildRightPanel();
        loadOrders();
    }

    // ---- UI-Aufbau ----

    /** Erstellt die Bestellübersichts-Tabelle */
    private void buildOrderTable() {
        orderTableModel = new DefaultTableModel(ORDER_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        orderTable = new JTable(orderTableModel);
        orderTable.setRowHeight(26);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setGridColor(new Color(220, 220, 235));

        orderTable.getColumnModel().getColumn(0).setMaxWidth(50);
        orderTable.getColumnModel().getColumn(1).setMaxWidth(60);

        JScrollPane sp = new JScrollPane(orderTable);
        sp.setBorder(BorderFactory.createTitledBorder("Alle Bestellungen"));
        sp.setPreferredSize(new Dimension(500, 0));
        add(sp, BorderLayout.CENTER);
    }

    /** Erstellt das rechte Panel (neue Bestellung + Statusverwaltung) */
    private void buildRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBackground(new Color(248, 249, 255));

        // Positionen-Tabelle
        itemTableModel = new DefaultTableModel(ITEM_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemTable = new JTable(itemTableModel);
        itemTable.setRowHeight(24);
        itemTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane itemSP = new JScrollPane(itemTable);
        itemSP.setBorder(BorderFactory.createTitledBorder("Positionen der neuen Bestellung"));
        itemSP.setPreferredSize(new Dimension(0, 150));
        rightPanel.add(itemSP, BorderLayout.NORTH);

        // Neue Bestellung – Formular
        rightPanel.add(buildNewOrderForm(), BorderLayout.CENTER);

        // Status-Update-Panel
        rightPanel.add(buildStatusPanel(), BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }

    /** Erstellt das Formular für eine neue Bestellung */
    private JPanel buildNewOrderForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Neue Bestellung erstellen"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Kunde
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Kunde:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        cbCustomer = new JComboBox<>();
        cbCustomer.insertItemAt(null, 0); // "kein Kunde" Option
        panel.add(cbCustomer, gbc);

        // Tischnummer
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Tisch-Nr. *:"), gbc);
        gbc.gridx = 1;
        spTableNumber = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        panel.add(spTableNumber, gbc);

        // Trennlinie
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Gericht
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Gericht:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cbMenuItem = new JComboBox<>();
        panel.add(cbMenuItem, gbc);

        // Menge
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Menge:"), gbc);
        gbc.gridx = 1;
        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        panel.add(spQuantity, gbc);

        // Buttons für Positionen
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel itemBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        itemBtnPanel.setBackground(Color.WHITE);
        btnAddItem = createButton("+ Position hinzufügen", new Color(70, 130, 180));
        itemBtnPanel.add(btnAddItem);
        panel.add(itemBtnPanel, gbc);
        gbc.gridwidth = 1;

        // Gesamtbetrag
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Gesamt:"), gbc);
        gbc.gridx = 1;
        lblTotal = new JLabel("0,00 €");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(new Color(46, 139, 87));
        panel.add(lblTotal, gbc);

        // Bestellung speichern / Leeren
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JPanel orderBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        orderBtnPanel.setBackground(Color.WHITE);
        btnCreateOrder = createButton("✅ Bestellung anlegen", new Color(46, 139, 87));
        btnClearOrder  = createButton("✖ Leeren",               new Color(120, 120, 120));
        orderBtnPanel.add(btnCreateOrder);
        orderBtnPanel.add(btnClearOrder);
        panel.add(orderBtnPanel, gbc);

        // Event-Handler
        btnAddItem.addActionListener(e    -> addItemToOrder());
        btnCreateOrder.addActionListener(e -> createOrder());
        btnClearOrder.addActionListener(e  -> clearOrder());

        loadCustomers();
        loadMenuItems();

        return panel;
    }

    /** Erstellt das Status-Änderungs-Panel */
    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(new Color(240, 240, 255));
        panel.setBorder(BorderFactory.createTitledBorder("Status der ausgewählten Bestellung ändern"));

        cbStatus = new JComboBox<>(OrderService.STATUSES);
        btnUpdateStatus = createButton("Status ändern", new Color(70, 130, 180));

        panel.add(new JLabel("Neuer Status:"));
        panel.add(cbStatus);
        panel.add(btnUpdateStatus);

        btnUpdateStatus.addActionListener(e -> updateOrderStatus());

        return panel;
    }

    // ---- Aktionen ----

    /** Lädt alle Kunden in die ComboBox */
    private void loadCustomers() {
        cbCustomer.removeAllItems();
        cbCustomer.addItem(null); // leere Option (Laufkundschaft)
        customerService.getAllCustomers().forEach(cbCustomer::addItem);
    }

    /** Lädt alle verfügbaren Gerichte in die ComboBox */
    private void loadMenuItems() {
        cbMenuItem.removeAllItems();
        menuService.getAvailableMenuItems().forEach(cbMenuItem::addItem);
    }

    /** Fügt ein Gericht zur temporären Bestellliste hinzu */
    private void addItemToOrder() {
        de.restaurant.model.MenuItem selected = (de.restaurant.model.MenuItem) cbMenuItem.getSelectedItem();
        if (selected == null) { showError("Bitte ein Gericht auswählen."); return; }

        int qty = (int) spQuantity.getValue();
        OrderItem item = new OrderItem(0, selected, qty);
        currentItems.add(item);

        // In Tabelle anzeigen
        itemTableModel.addRow(new Object[]{
                selected.getName(),
                qty,
                String.format("%.2f €", selected.getPrice()),
                String.format("%.2f €", item.getSubtotal())
        });
        updateTotal();
    }

    /** Berechnet und zeigt den Gesamtbetrag der aktuellen Bestellung */
    private void updateTotal() {
        double total = currentItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
        lblTotal.setText(String.format("%.2f €", total));
    }

    /** Legt die aktuelle Bestellung in der Datenbank an */
    private void createOrder() {
        try {
            Customer customer = (Customer) cbCustomer.getSelectedItem();
            int tableNumber   = (int) spTableNumber.getValue();

            Order order = orderService.createOrder(customer, tableNumber, currentItems);
            JOptionPane.showMessageDialog(this,
                    "Bestellung #" + order.getId() + " erfolgreich angelegt!",
                    "Erfolg", JOptionPane.INFORMATION_MESSAGE);
            clearOrder();
            loadOrders();

        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    /** Leert die temporäre Bestellliste */
    private void clearOrder() {
        currentItems.clear();
        itemTableModel.setRowCount(0);
        spQuantity.setValue(1);
        lblTotal.setText("0,00 €");
    }

    /** Ändert den Status der ausgewählten Bestellung */
    private void updateOrderStatus() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { showError("Bitte eine Bestellung in der Tabelle auswählen."); return; }

        int orderId   = (int) orderTableModel.getValueAt(row, 0);
        String status = (String) cbStatus.getSelectedItem();
        orderService.updateStatus(orderId, status);
        loadOrders();
        JOptionPane.showMessageDialog(this, "Status erfolgreich aktualisiert.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Lädt alle Bestellungen in die Tabelle */
    private void loadOrders() {
        orderTableModel.setRowCount(0);
        List<Order> orders = orderService.getAllOrders();

        for (Order o : orders) {
            String customerName = (o.getCustomer() != null) ? o.getCustomer().getName() : "Laufkundschaft";
            orderTableModel.addRow(new Object[]{
                    o.getId(),
                    o.getTableNumber(),
                    customerName,
                    o.getOrderDate().toString().replace("T", " ").substring(0, 16),
                    o.getStatus(),
                    String.format("%.2f", o.getTotalAmount())
            });
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 33));
        return btn;
    }

    @Override
    public void refresh() {
        loadOrders();
        loadCustomers();
        loadMenuItems();
    }
}
