package de.restaurant.gui.panels;

import de.restaurant.exception.ValidationException;
import de.restaurant.model.Ingredient;
import de.restaurant.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel implements Refreshable {

    private final InventoryService inventoryService;

    // Tabelle
    private JTable table;
    private DefaultTableModel tableModel;

    // Felder
    private JTextField txtName, txtQuantity, txtUnit, txtMinStock;
    private JTextField txtAmount, txtNote;
    private JLabel lblLowStock;

    public InventoryPanel() {
        this.inventoryService = new InventoryService();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Tabelle ----
        String[] cols = {"ID", "Zutat", "Menge", "Einheit", "Mindestbestand", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        // Rote Zeile bei Niedrigbestand
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                    t, val, sel, focus, row, col);
                String status = (String) t.getValueAt(row, 5);
                if ("⚠ Niedrig".equals(status)) {
                    c.setBackground(sel ? new Color(200, 80, 80)
                                       : new Color(255, 200, 200));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(sel ? t.getSelectionBackground()
                                       : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- Rechte Seite ----
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        // Zutat hinzufügen
        rightPanel.add(createSectionLabel("➕ Neue Zutat"));
        txtName     = addField(rightPanel, "Name:");
        txtQuantity = addField(rightPanel, "Menge:");
        txtUnit     = addField(rightPanel, "Einheit (kg/L/St):");
        txtMinStock = addField(rightPanel, "Mindestbestand:");
        JButton btnAdd = new JButton("Zutat hinzufügen");
        btnAdd.setBackground(new Color(60, 150, 80));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnAdd.addActionListener(e -> addIngredient());
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(btnAdd);

        rightPanel.add(Box.createVerticalStrut(15));

        // Warenbewegung
        rightPanel.add(createSectionLabel("📦 Warenbewegung"));
        txtAmount = addField(rightPanel, "Menge:");
        txtNote   = addField(rightPanel, "Notiz:");

        JButton btnIn = new JButton("📥 Wareneingang");
        btnIn.setBackground(new Color(52, 120, 180));
        btnIn.setForeground(Color.WHITE);
        btnIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnIn.addActionListener(e -> stockMovement("EINGANG"));

        JButton btnOut = new JButton("📤 Warenausgang");
        btnOut.setBackground(new Color(210, 95, 45));
        btnOut.setForeground(Color.WHITE);
        btnOut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnOut.addActionListener(e -> stockMovement("AUSGANG"));

        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(btnIn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(btnOut);

        rightPanel.add(Box.createVerticalStrut(15));

        // Löschen
        JButton btnDelete = new JButton("🗑 Zutat löschen");
        btnDelete.setBackground(new Color(180, 50, 50));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnDelete.addActionListener(e -> deleteIngredient());
        rightPanel.add(btnDelete);

        rightPanel.add(Box.createVerticalStrut(15));

        // Niedrigbestand-Alarm
        lblLowStock = new JLabel("✅ Alle Bestände OK");
        lblLowStock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLowStock.setForeground(new Color(60, 150, 80));
        rightPanel.add(lblLowStock);

        add(rightPanel, BorderLayout.EAST);

        // ---- Titel oben ----
        JLabel title = new JLabel("📦 Inventar / Lagerverwaltung");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Ingredient> list = inventoryService.getAllIngredients();
        for (Ingredient i : list) {
            tableModel.addRow(new Object[]{
                i.getId(),
                i.getName(),
                String.format("%.2f", i.getQuantity()),
                i.getUnit(),
                String.format("%.2f", i.getMinStock()),
                i.isLowStock() ? "⚠ Niedrig" : "✅ OK"
            });
        }
        // Alarm aktualisieren
        List<Ingredient> low = inventoryService.getLowStockItems();
        if (low.isEmpty()) {
            lblLowStock.setText("✅ Alle Bestände OK");
            lblLowStock.setForeground(new Color(60, 150, 80));
        } else {
            lblLowStock.setText("⚠ " + low.size() + " Zutaten fast leer!");
            lblLowStock.setForeground(new Color(200, 50, 50));
        }
    }

    private void addIngredient() {
        try {
            inventoryService.addIngredient(
                txtName.getText().trim(),
                Double.parseDouble(txtQuantity.getText().trim()),
                txtUnit.getText().trim(),
                Double.parseDouble(txtMinStock.getText().trim())
            );
            clearFields();
            loadData();
            JOptionPane.showMessageDialog(this, "Zutat hinzugefügt!");
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Fehler", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Bitte gültige Zahlen eingeben.",
                "Fehler", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void stockMovement(String type) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Bitte eine Zutat auswählen.",
                "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            String note = txtNote.getText().trim();
            if (type.equals("EINGANG")) {
                inventoryService.addStock(id, amount, note);
            } else {
                inventoryService.removeStock(id, amount, note);
            }
            loadData();
            JOptionPane.showMessageDialog(this,
                type + " erfolgreich gebucht!");
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Fehler", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Bitte eine gültige Menge eingeben.",
                "Fehler", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteIngredient() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Bitte eine Zutat auswählen.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Zutat '" + name + "' wirklich löschen?",
            "Bestätigen", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventoryService.deleteIngredient(id);
            loadData();
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtQuantity.setText("");
        txtUnit.setText("");
        txtMinStock.setText("");
    }

    private JTextField addField(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(field);
        panel.add(Box.createVerticalStrut(4));
        return field;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return label;
    }

    @Override
    public void refresh() {
        loadData();
    }
}
